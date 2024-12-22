import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class WebCrawler {

    private static final String BASE_URL = "https://www.giallozafferano.it/";
    private static final String[] CATEGORIES = {"Antipasti", "Primi", "Secondi-piatti", "Dolci-e-Desserts"};

    private static final String DB_URL = "jdbc:mysql://localhost:3306/recipebot"; // Cambia con il tuo database
    private static final String DB_USER = "root"; // Cambia con il tuo utente
    private static final String DB_PASSWORD = ""; // Cambia con la tua password

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Connessione al database riuscita!");

            for (String category : CATEGORIES) {
                System.out.println("Processando categoria: " + category);
                processCategory(connection, category);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processCategory(Connection connection, String category) {
        try {
            String categoryUrl = BASE_URL + "ricette-cat/" + category + "/";
            Document categoryPage = Jsoup.connect(categoryUrl).get();

            Elements recipeLinks = categoryPage.select(".gz-title a"); // Aggiorna il selettore in base al sito
            int count = 0;

            for (Element link : recipeLinks) {
                if (count >= 10) break; // Limita a 10 ricette per categoria

                String recipeUrl = link.attr("href");
                String recipeName = link.text();
                System.out.println("Trovata ricetta: " + recipeName);

                Document recipePage = Jsoup.connect(recipeUrl).get();
                Elements ingredients = recipePage.select(".gz-ingredient"); // Selettore per gli ingredienti
                Elements steps = recipePage.select(".gz-content-recipe-step > p"); // Selettore per i passaggi

                saveRecipeToDatabase(connection, recipeName, recipeUrl, category, ingredients, steps);
                count++;
            }

        } catch (Exception e) {
            System.err.println("Errore durante l'elaborazione della categoria: " + category);
            e.printStackTrace();
        }
    }

    private static void saveRecipeToDatabase(Connection connection, String recipeName, String recipeUrl, String category, Elements ingredients, Elements steps) {
        String insertRecipeQuery = "INSERT INTO Ricette (nome, categoria, url_fonte) VALUES (?, ?, ?)";
        String insertIngredientQuery = "INSERT INTO Ingredienti (nome, ricetta_id) VALUES (?, ?)";
        String insertStepQuery = "INSERT INTO Passaggi (descrizione, ordine, ricetta_id) VALUES (?, ?, ?)";

        try {
            String mappedCategory = mapCategory(category);

            PreparedStatement recipeStatement = connection.prepareStatement(insertRecipeQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            recipeStatement.setString(1, recipeName);
            recipeStatement.setString(2, mappedCategory);
            recipeStatement.setString(3, recipeUrl);
            recipeStatement.executeUpdate();

            int recipeId;
            try (var rs = recipeStatement.getGeneratedKeys()) {
                if (rs.next()) {
                    recipeId = rs.getInt(1);
                } else {
                    throw new Exception("Impossibile ottenere l'ID della ricetta.");
                }
            }

            for (Element ingredient : ingredients) {
                String ingredientName = ingredient.text(); // Salva il testo completo dell'ingrediente

                PreparedStatement ingredientStatement = connection.prepareStatement(insertIngredientQuery);
                ingredientStatement.setString(1, ingredientName);
                ingredientStatement.setInt(2, recipeId);
                ingredientStatement.executeUpdate();
            }

            int stepOrder = 1;
            for (Element step : steps) {
                String stepDescription = step.text().trim();

                if (!stepDescription.isEmpty()) {
                    PreparedStatement stepStatement = connection.prepareStatement(insertStepQuery);
                    stepStatement.setString(1, stepDescription);
                    stepStatement.setInt(2, stepOrder++);
                    stepStatement.setInt(3, recipeId);
                    stepStatement.executeUpdate();
                }
            }

            System.out.println("Salvata ricetta: " + recipeName + " con " + ingredients.size() + " ingredienti e " + steps.size() + " passaggi.");

        } catch (Exception e) {
            System.err.println("Errore durante il salvataggio della ricetta: " + recipeName);
            e.printStackTrace();
        }
    }

    private static String mapCategory(String category) {
        switch (category.toLowerCase()) {
            case "antipasti":
                return "antipasto";
            case "primi":
                return "primo";
            case "secondi-piatti":
                return "secondo";
            case "dolci-e-desserts":
                return "dolce";
            default:
                throw new IllegalArgumentException("Categoria non valida: " + category);
        }
    }
}