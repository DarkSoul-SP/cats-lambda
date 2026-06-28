package com.darksoul;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class TelegramCatBot {

  private static final String CATAAS_URL = "https://cataas.com/cat?json=true";

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  public TelegramCatBot(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
  }

  /**
   * Steps:
   * 1. Fetches metadata from the CATAAS endpoint.
   * 2. Extracts the partial or complete "url" parameter string.
   * 3. Dispatches the resulting URL inside a JSON body to the Telegram Bot API.
   */
  public void sendRandomCatToTelegram(Secret secret, Context context) {
    try {
      // Step 1: Fetch the cat metadata payload
      CataasResponse catData = fetchCatData();
      if (catData == null || catData.url() == null) {
        System.err.println("Cat's fetch failed.");
        context.getLogger().log("Cat's fetch failed. CataasResponse: " + catData);
        return;
      }

      // Fix relative path if API return "/cat/XYZ"
      final String catImageUrl = catData.url().startsWith("/")
          ? "https://cataas.com" + catData.url()
          : catData.url();

      // Step 2: Post request to Telegram endpoint
      final String telegramUrl = "https://api.telegram.org/" + secret.botToken + "/sendPhoto";

      TelegramPhotoRequest telegramRequest = new TelegramPhotoRequest(secret.chatId, catImageUrl, secret.imgCaption);
      final String jsonPayload = objectMapper.writeValueAsString(telegramRequest);

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(telegramUrl))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      context.getLogger().log("Tg cats bot response: " + response);
    } catch (IOException | InterruptedException e) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      context.getLogger().log("Network request to tg cats bot interrupted or failed execution: " + sw);
    }
  }

  private CataasResponse fetchCatData() throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(CATAAS_URL))
        .GET()
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      return null;
    }

    return objectMapper.readValue(response.body(), CataasResponse.class);
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public record CataasResponse(
      String id,
      String createdAt,
      String url,
      String mimetype,
      List<String> tags
  ) {}

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public record TelegramPhotoRequest(
      String chatId,
      String photo,
      String caption
  ) {}

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @JsonNaming(PropertyNamingStrategies.UpperSnakeCaseStrategy.class)
  public record Secret(
      String botToken,
      String chatId,
      String imgCaption
  ) {}

//  static void main() {
//    // Execution safely contained within an efficient JVM virtual thread
//    Thread.ofVirtual().start(() -> {
//      TelegramCatBot bot = new TelegramCatBot();
//      System.out.println("Fetching cat and sending to Telegram...");
//      bot.sendRandomCatToTelegram(MY_CHAT_ID, IMG_CAPTION);
//    });
//
//    // Let background execution finish before closing the main runner
//    try { Thread.sleep(4000); } catch (InterruptedException _) {}
//  }
}