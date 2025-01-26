import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {
    public static Connection getKoneksi() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/gudang_db";  // Ganti dengan database Anda
        String user = "root";  // Username MySQL
        String password = "";  // Password MySQL (default kosong)

        return DriverManager.getConnection(url, user, password);
    }
}
