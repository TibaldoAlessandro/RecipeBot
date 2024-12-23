import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Passaggio {
    private String descrizione;
    private int ordine;
    private int ricettaId;

    public Passaggio(String descrizione, int ordine, int ricettaId) {
        this.descrizione = descrizione;
        this.ordine = ordine;
        this.ricettaId = ricettaId;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public int getOrdine() {
        return ordine;
    }

    public void inserisciInDatabase(Connection connection) {
        String query = "INSERT INTO Passaggi (descrizione, ordine, ricetta_id) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, descrizione);
            statement.setInt(2, ordine);
            statement.setInt(3, ricettaId);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Errore nell'inserimento del passaggio nel database: " + e.getMessage());
        }
    }

    // Metodo per recuperare i passaggi di una ricetta
    public static List<Passaggio> getPassaggiByRicettaId(int ricettaId, Connection connection) {
        List<Passaggio> passaggi = new ArrayList<>();
        String query = "SELECT descrizione, ordine FROM Passaggi WHERE ricetta_id = ? ORDER BY ordine";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, ricettaId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                passaggi.add(new Passaggio(rs.getString("descrizione"), rs.getInt("ordine"), ricettaId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return passaggi;
    }
}