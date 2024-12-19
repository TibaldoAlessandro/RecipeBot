// token bot telegram: 8046367516:AAFZi_yHto9CaWvCaiavQ2JrEug367EbKbs
// username: RecipeBot_botbot    nome: RecipeBot

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {
    public static void main(String[] args) {
        String botToken = "8046367516:AAFZi_yHto9CaWvCaiavQ2JrEug367EbKbs";

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new RecipeBot(botToken));
            System.out.println("RecipeBot è attivo e in esecuzione!");
            Thread.currentThread().join(); // Mantiene l'applicazione in esecuzione
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}