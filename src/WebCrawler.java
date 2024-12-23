import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.DriverManager;

public class WebCrawler {

    private static final String BASE_URL = "https://www.giallozafferano.it/";
    private static final String[] CATEGORIES = {"Antipasti", "Primi", "Secondi-piatti", "Dolci-e-Desserts"};

    private static final String DB_URL = "jdbc:mysql://localhost:3306/recipebot";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

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

            Elements recipeLinks = categoryPage.select(".gz-title a");
            int count = 0;

            for (Element link : recipeLinks) {
                if (count >= 10) break; // Limita a 10 ricette per categoria

                String recipeUrl = link.attr("href");
                String recipeName = link.text();
                System.out.println("Trovata ricetta: " + recipeName);

                Document recipePage = Jsoup.connect(recipeUrl).get();
                Elements ingredients = recipePage.select(".gz-ingredient");
                Elements steps = recipePage.select(".gz-content-recipe-step > p");

                String imageUrl = extractImageUrl(recipePage);

                // Crea e salva la ricetta nel database
                Ricetta ricetta = new Ricetta(recipeName, mapCategory(category), recipeUrl, imageUrl);
                ricetta.inserisciInDatabase(connection);

                // Salva gli ingredienti
                for (Element ingredient : ingredients) {
                    Ingrediente ingrediente = new Ingrediente(ingredient.text(), ricetta.getId());
                    ingrediente.inserisciInDatabase(connection);
                }

                // Salva i passaggi
                int stepOrder = 1;
                for (Element step : steps) {
                    Passaggio passaggio = new Passaggio(step.text().trim(), stepOrder++, ricetta.getId());
                    passaggio.inserisciInDatabase(connection);
                }

                count++;
            }

        } catch (Exception e) {
            System.err.println("Errore durante l'elaborazione della categoria: " + category);
            e.printStackTrace();
        }
    }

    private static String extractImageUrl(Document recipePage) {
        Element imageElement = recipePage.selectFirst(".gz-content-recipe .gz-featured-image img");
        if (imageElement != null) {
            return imageElement.attr("src");
        } else {
            return "";
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