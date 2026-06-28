package com.darksoul;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.util.Map;

/**
 *
 * Created by DarkSoul on 6/17/2026
 **/

public class CatsHandler implements RequestHandler<Map<String, Object>, String> {

  // Initialize client statically outside the handler loop to leverage warm-start caching
  private static final SecretsManagerClient secretsClient = SecretsManagerClient.builder()
      .region(Region.US_EAST_1)
      .build();
  private static final String SECRET_NAME = "cats-labmda/secret";
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final TelegramCatBot bot = new TelegramCatBot(mapper);

  private static TelegramCatBot.Secret cachedSecret = null;

  @Override
  public String handleRequest(Map<String, Object> input, Context context) {
    var logger = context.getLogger();

    try {
      logger.log("Cron trigger activated at 5:00 AM UTC! Starting execution of CatsHandler...");

      TelegramCatBot.Secret secret = getSecretValue(context);
      logger.log("Fetching cat and sending to Telegram...");
      bot.sendRandomCatToTelegram(secret, context);

      logger.log("CatsHandler: Execution completed successfully.");

      return "Success";
    } catch (Exception e) {
      logger.log("Error during execution: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private static TelegramCatBot.Secret getSecretValue(Context context) throws JsonProcessingException {
    if (cachedSecret == null) {
      String secretJSONValue = secretsClient.getSecretValue(builder -> builder.secretId(SECRET_NAME)).secretString();
      context.getLogger().log("Successfully retrieved secret value: " + secretJSONValue);
      cachedSecret = mapper.readValue(secretJSONValue, TelegramCatBot.Secret.class);
    }
    return cachedSecret;
  }
}
