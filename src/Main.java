// token bot telegram: 8046367516:AAFZi_yHto9CaWvCaiavQ2JrEug367EbKbs
// username: RecipeBot_botbot    nome: RecipeBot

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {
    public static void main(String[] args) {
        String botToken = "8046367516:AAFZi_yHto9CaWvCaiavQ2JrEug367EbKbs"; // Sostituisci con il tuo token

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            // Usa MyTelegramBot al posto di RecipeBot
            botsApplication.registerBot(botToken, new MyTelegramBot(botToken));
            System.out.println("MyTelegramBot è attivo e in esecuzione!");
            Thread.currentThread().join(); // Mantiene l'applicazione in esecuzione
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}