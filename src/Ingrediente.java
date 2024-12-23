import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Ingrediente {
    private String nome;
    private int ricettaId;

    public Ingrediente(String nome, int ricettaId) {
        this.nome = nome;
        this.ricettaId = ricettaId;
    }

    public String getNome() {
        return nome;
    }

    public void inserisciInDatabase(Connection connection) {
        String query = "INSERT INTO Ingredienti (nome, ricetta_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, nome);
            statement.setInt(2, ricettaId);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Errore nell'inserimento dell'ingrediente nel database: " + e.getMessage());
        }
    }

    // Metodo per recuperare gli ingredienti per una ricetta
    public static List<Ingrediente> getIngredientiByRicettaId(int ricettaId, Connection connection) {
        List<Ingrediente> ingredienti = new ArrayList<>();
        String query = "SELECT nome FROM Ingredienti WHERE ricetta_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, ricettaId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ingredienti.add(new Ingrediente(rs.getString("nome"), ricettaId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ingredienti;
    }
}