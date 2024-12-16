import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MyTelegramBot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        // Controlla se il messaggio è un testo
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String receivedText = update.getMessage().getText();

            // Crea una risposta
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Hai scritto: " + receivedText);

            // Invia il messaggio
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "IlTuoBotUsername"; // Sostituisci con il nome del bot
    }

    @Override
    public String getBotToken() {
        return "IL_TUO_TOKEN"; // Sostituisci con il token ottenuto da BotFather
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String receivedText = update.getMessage().getText();

            SendMessage message = new SendMessage();
            message.setChatId(chatId);

            if (receivedText.equals("/start")) {
                message.setText("Benvenuto! Sono il tuo bot Telegram.");
            } else if (receivedText.equals("/help")) {
                message.setText("Comandi disponibili:\n/start - Avvia il bot\n/help - Mostra questo messaggio");
            } else {
                message.setText("Hai scritto: " + receivedText);
            }

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}