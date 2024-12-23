import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Ricetta {
    private String nome;
    private String categoria;
    private String urlFonte;
    private String urlImmagine;
    private int id;

    public Ricetta(String nome, String categoria, String urlFonte, String urlImmagine) {
        this.nome = nome;
        this.categoria = categoria;
        this.urlFonte = urlFonte;
        this.urlImmagine = urlImmagine;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getUrlFonte() {
        return urlFonte;
    }

    public String getUrlImmagine() {
        return urlImmagine;
    }

    // Metodo per inserire la ricetta nel database
    public void inserisciInDatabase(Connection connection) {
        String query = "INSERT INTO Ricette (nome, categoria, url_fonte, url_immagine) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, nome);
            statement.setString(2, categoria);
            statement.setString(3, urlFonte);
            statement.setString(4, urlImmagine);
            statement.executeUpdate();

            // Ottieni l'ID generato
            try (var rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    this.id = rs.getInt(1); // Imposta l'ID
                }
            }
        } catch (SQLException e) {
            System.out.println("Errore nell'inserimento della ricetta nel database: " + e.getMessage());
        }
    }

    // Metodo per recuperare una ricetta dal database
    public static Ricetta getRicettaByNome(String nome, Connection connection) {
        String query = "SELECT * FROM Ricette WHERE nome = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nome);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Ricetta(
                        rs.getString("nome"),
                        rs.getString("categoria"),
                        rs.getString("url_fonte"),
                        rs.getString("url_immagine")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Se non trova la ricetta
    }
}