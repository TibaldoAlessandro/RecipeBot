import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecipeBot implements LongPollingUpdateConsumer {

    // Configurazioni per il database
    private static final String DB_URL = "jdbc:mysql://localhost:3306/recipebot";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private final TelegramClient telegramClient;

    public RecipeBot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    // Metodo per inviare il menu principale con bottoni di categorie
    private void sendCategoryMenu(long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text("Benvenuto su RecipeBot!\n" +
                        "Scegli la categoria di piatti che vuoi cucinare e io ti aiuterò dandoti tutti gli ingredienti e i passaggi necessari.\n" +
                        "Diventerai uno chef stellare!")
                .replyMarkup(createCategoryButtons()) // Imposta i bottoni interattivi
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Metodo per creare i bottoni interattivi con disposizione in righe da 2
    private InlineKeyboardMarkup createCategoryButtons() {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        // Creazione dei bottoni
        InlineKeyboardButton buttonAntipasti = InlineKeyboardButton.builder()
                .text("Antipasti")
                .callbackData("antipasto")
                .build();

        InlineKeyboardButton buttonPrimi = InlineKeyboardButton.builder()
                .text("Primi")
                .callbackData("primo")
                .build();

        InlineKeyboardButton buttonSecondi = InlineKeyboardButton.builder()
                .text("Secondi")
                .callbackData("secondo")
                .build();

        InlineKeyboardButton buttonDolci = InlineKeyboardButton.builder()
                .text("Dolci")
                .callbackData("dolce")
                .build();

        // Aggiungere i bottoni a righe (2 bottoni per riga)
        InlineKeyboardRow row1 = new InlineKeyboardRow();
        row1.add(buttonAntipasti);
        row1.add(buttonPrimi);

        InlineKeyboardRow row2 = new InlineKeyboardRow();
        row2.add(buttonSecondi);
        row2.add(buttonDolci);

        // Aggiungi le righe alla tastiera
        rows.add(row1);
        rows.add(row2);

        // Restituisci la tastiera completa
        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    // Metodo per inviare la lista di piatti di una categoria
    private void sendDishList(long chatId, String category) {
        String dishes = getDishList(category); // Ottiene i piatti per la categoria (query al DB)

        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text("Ecco alcuni piatti della categoria " + category + ":\n" + dishes +
                        "\n\nDigita /ricetta <nome_piatto> per vedere la ricetta.")
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Metodo per recuperare i piatti dalla categoria nel database
    private String getDishList(String category) {
        StringBuilder result = new StringBuilder();
        String query = "SELECT nome FROM Ricette WHERE categoria = ? LIMIT 10";  // Query SQL

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, category); // Imposta il parametro della categoria

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                result.append(rs.getString("nome")).append("\n"); // Aggiungi il piatto alla risposta
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Si è verificato un errore durante il recupero dei piatti.";
        }

        return result.length() > 0 ? result.toString() : "Non ci sono piatti disponibili per questa categoria.";
    }

    // Metodo per ottenere i dettagli della ricetta (ingredienti, passaggi, URL fonte e immagine)
    private Ricetta getRecipeDetails(String recipeName) {
        Ricetta ricetta = null;
        String query = "SELECT nome, categoria, url_fonte, url_immagine FROM Ricette WHERE nome = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, recipeName);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ricetta = new Ricetta(
                        rs.getString("nome"),
                        rs.getString("categoria"),
                        rs.getString("url_fonte"),
                        rs.getString("url_immagine")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ricetta;
    }

    // Metodo per inviare la ricetta con ingredienti, passaggi e URL fonte
    private void sendRecipeDetails(long chatId, String recipeName) {
        Ricetta ricetta = getRecipeDetails(recipeName);
        if (ricetta != null) {
            // Invia i dettagli della ricetta
            SendMessage message = SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text("Ecco la ricetta per " + ricetta.getNome() + ":\n" +
                            "Categoria: " + ricetta.getCategoria() + "\n" +
                            "Fonte: " + ricetta.getUrlFonte() + "\n\n" +
                            "Ingredienti:\n" + getIngredients(ricetta.getId()) + "\n\n" +
                            "Passaggi:\n" + getSteps(ricetta.getId()))
                    .build();

            try {
                telegramClient.execute(message);
                sendImage(chatId, ricetta.getUrlImmagine()); // Invia immagine
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            sendMessage(chatId, "Ricetta non trovata.");
        }
    }

    // Metodo per ottenere gli ingredienti della ricetta
    private String getIngredients(int recipeId) {
        StringBuilder ingredients = new StringBuilder();
        String query = "SELECT nome FROM Ingredienti WHERE ricetta_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, recipeId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ingredients.append(rs.getString("nome")).append("\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ingredients.length() > 0 ? ingredients.toString() : "Nessun ingrediente trovato.";
    }

    // Metodo per ottenere i passaggi della ricetta
    private String getSteps(int recipeId) {
        StringBuilder steps = new StringBuilder();
        String query = "SELECT descrizione FROM Passaggi WHERE ricetta_id = ? ORDER BY ordine";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, recipeId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                steps.append(rs.getString("descrizione")).append("\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return steps.length() > 0 ? steps.toString() : "Nessun passaggio trovato.";
    }

    // Metodo per inviare un'immagine tramite URL
    private void sendImage(long chatId, String imageUrl) {
        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(String.valueOf(chatId))
                .photo(new InputFile(imageUrl))  // L'URL dell'immagine
                .build();
        try {
            telegramClient.execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();

                if (messageText.equals("/start")) {
                    sendCategoryMenu(chatId); // Mostra i bottoni delle categorie
                } else if (messageText.startsWith("/ricetta")) {
                    String recipeName = messageText.substring(9).trim(); // Estrai il nome del piatto
                    sendRecipeDetails(chatId, recipeName); // Mostra la ricetta
                } else {
                    sendMessage(chatId, "Comando non riconosciuto.");
                }
            } else if (update.hasCallbackQuery()) {
                String callbackData = update.getCallbackQuery().getData();
                long chatId = update.getCallbackQuery().getMessage().getChatId();

                // Gestire la selezione di categoria con i bottoni
                if (callbackData.equals("antipasto") || callbackData.equals("primo") ||
                        callbackData.equals("secondo") || callbackData.equals("dolce")) {
                    sendDishList(chatId, callbackData); // Mostra i piatti della categoria selezionata
                }
            }
        }
    }
}