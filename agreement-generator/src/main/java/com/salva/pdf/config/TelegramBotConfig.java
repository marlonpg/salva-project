package com.salva.pdf.config;

import com.salva.pdf.bot.AgreementGeneratorBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = false)
public class TelegramBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean(destroyMethod = "stop")
    public BotSession botSession(TelegramBotsApi telegramBotsApi, AgreementGeneratorBot agreementBot)
            throws TelegramApiException {
        BotSession session = telegramBotsApi.registerBot(agreementBot);
        log.info("Registered Telegram bot @{} with polling running={}",
                agreementBot.getBotUsername(), session.isRunning());
        return session;
    }
}
