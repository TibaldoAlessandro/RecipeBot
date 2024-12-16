// token bot telegram: 8046367516:AAFZi_yHto9CaWvCaiavQ2JrEug367EbKbs
// username: RecipeBot_botbot    nome: RecipeBot

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {

    public static void main(String[] args) {

            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            // Registra il bot
            try {
                botsApplication.registerBot("8046367516:AAFZi_yHto9CaWvCaiavQ2JrEug367EbKbs", new RecipeBot());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Bot Telegram avviato con successo!");

    }
}
