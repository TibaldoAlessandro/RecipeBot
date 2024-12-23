// token bot telegram: 8046367516:AAFZi_yHto9CaWvCaiavQ2JrEug367EbKbs
// username: RecipeBot_botbot    nome: RecipeBot

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {
    public static void main(String[] args) {
        String botToken = "8046367516:AAFZi_yHto9CaWvCaiavQ2JrEug367EbKbs";

        TelegramBotsLongPollingApplication app = null;
        try {
            app = new TelegramBotsLongPollingApplication();

            RecipeBot bot = new RecipeBot(botToken);
            app.registerBot(botToken, bot);

            System.out.println("RecipeBot è attivo e funzionante!");

            Thread.currentThread().join();
        } catch (TelegramApiException e) {
            System.err.println("Errore durante l'avvio del bot: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Applicazione interrotta: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Errore generico: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (app != null) {
                try {
                    app.close();
                } catch (Exception e) {
                    System.err.println("Errore durante la chiusura dell'applicazione: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}