import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

public class RecipeBot implements LongPollingSingleThreadUpdateConsumer {

    private TelegramClient telegramClient = new OkHttpTelegramClient("8046367516:AAFZi_yHto9CaWvCaiavQ2JrEug367EbKbs");

    // Metodo per inviare il menu principale con bottoni di categorie
    private void sendCategoryMenu(long chatId) {
        SendMessage message = new SendMessage(chatId+"", "");
        message.setChatId(String.valueOf(chatId));
        message.setText("Seleziona una categoria:");

        //rows.add(createButtonRow("Primi", "primi"));
        //rows.add(createButtonRow("Secondi", "secondi"));
        //rows.add(createButtonRow("Dolci", "dolci"));


        message.setReplyMarkup(InlineKeyboardMarkup
                .builder()
                .keyboardRow(
                        new InlineKeyboardRow(InlineKeyboardButton
                                .builder()
                                .text("Antipasti")
                                .callbackData("antipasti")
                                .build()
                        )
                )
                .build());

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Metodo per creare una riga di bottoni
    private List<InlineKeyboardButton> createButtonRow(String text, String callbackData) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        row.add(button);
        return row;
    }

    // Metodo per inviare la lista di piatti di una categoria
    private void sendDishList(long chatId, int messageId, String category) {
        String dishes = getDishList(category); // Ottiene i piatti per la categoria (dummy data)

        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setMessageId(messageId);
        message.setText("Ecco i 10 piatti della categoria " + category + ":\n" + dishes +
                "\n\nDigita /nome_piatto per selezionare un piatto.");

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Metodo per simulare la lista di piatti per ogni categoria (puoi sostituirlo con i dati veri)
    private String getDishList(String category) {
        StringBuilder result = new StringBuilder();
        switch (category) {
            case "antipasti":
                result.append("1. Antipasto di verdure\n2. Crostini misti\n3. Bruschetta\n4. Insalata russa\n...");
                break;
            case "primi":
                result.append("1. Spaghetti alla carbonara\n2. Risotto ai funghi\n3. Lasagna\n4. Pasta al pesto\n...");
                break;
            case "secondi":
                result.append("1. Scaloppine al limone\n2. Pollo alla cacciatora\n3. Filetto di manzo\n4. Pesce al forno\n...");
                break;
            case "dolci":
                result.append("1. Tiramisu\n2. Panna cotta\n3. Cheesecake\n4. Profiteroles\n...");
                break;
        }
        return result.toString();
    }

    // Metodo per inviare un messaggio semplice
    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage(chatId+"", text);
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void consume(Update update) {
        // Controlla se il messaggio contiene testo
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText(); // Ottieni il testo del messaggio
            long chatId = update.getMessage().getChatId(); // Ottieni l'ID della chat

            if (messageText.equals("/start")) {
                sendCategoryMenu(chatId); // Mostra i bottoni delle categorie
            } else if (messageText.startsWith("/")) {
                // Gestisce la selezione del piatto tramite comando
                String dishName = messageText.substring(1); // Rimuove "/"
                sendDishList(chatId, dishName);
            } else {
                sendMessage(chatId, "Comando non riconosciuto. Usa /start per iniziare.");
            }
        } else if (update.hasCallbackQuery()) {
            // Gestione della callback dei bottoni
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();

            if (callbackData.equals("antipasti") || callbackData.equals("primi") ||
                    callbackData.equals("secondi") || callbackData.equals("dolci")) {
                // Mostra la lista dei piatti per la categoria selezionata
                sendDishList(chatId, messageId, callbackData);
            }
        }
    }
}
