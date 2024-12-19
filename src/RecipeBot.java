import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

public class RecipeBot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    public RecipeBot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    // Metodo per inviare il menu principale con bottoni di categorie
    private void sendCategoryMenu(long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text("Benvenuto su RecipeBot!\n" +
                        "Scegli pure che tipologia di piatti vuoi cucinare e io ti aiuterò dandoti tutti gli ingredienti e i passaggi necessari.\n" +
                        "Diventerai uno chef stellare!")
                .replyMarkup(createCategoryButtons()) // Imposta i bottoni interattivi
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Metodo per creare i bottoni interattivi
    private InlineKeyboardMarkup createCategoryButtons() {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        // Aggiungi i bottoni in righe
        rows.add(createButtonRow("Antipasti", "antipasti"));
        rows.add(createButtonRow("Primi", "primi"));
        rows.add(createButtonRow("Secondi", "secondi"));
        rows.add(createButtonRow("Dolci", "dolci"));

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    // Metodo per creare una riga di bottoni
    private InlineKeyboardRow createButtonRow(String text, String callbackData) {
        InlineKeyboardRow row = new InlineKeyboardRow();
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
        row.add(button);
        return row;
    }

    // Metodo per inviare la lista di piatti di una categoria
    private void sendDishList(long chatId, String category) {
        String dishes = getDishList(category); // Ottiene i piatti per la categoria (dummy data)

        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text("Ecco i 10 piatti della categoria " + category + ":\n" + dishes +
                        "\n\nDigita /nome_piatto per selezionare un piatto.")
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Metodo per simulare la lista di piatti per ogni categoria
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
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();

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
            } else {
                sendMessage(chatId, "Comando non riconosciuto. Usa /start per iniziare.");
            }
        } else if (update.hasCallbackQuery()) {
            // Gestione della callback dei bottoni
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals("antipasti") || callbackData.equals("primi") ||
                    callbackData.equals("secondi") || callbackData.equals("dolci")) {
                // Mostra la lista dei piatti per la categoria selezionata
                sendDishList(chatId, callbackData);
            }
        }
    }
}