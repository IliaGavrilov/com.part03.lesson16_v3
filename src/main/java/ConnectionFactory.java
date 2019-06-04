import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;

public class ConnectionFactory {

    //private static BasicDataSource dataSource;
//    String url = "jdbc:postgresql://localhost:5432/postgres_2";
//    String user = "postgres";
//    String password = "jU3y3gIV";

    public ConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException {
            BasicDataSource dataSource;
        //if (dataSource == null) {
            dataSource = new BasicDataSource();
            dataSource.setUrl("jdbc:postgresql://localhost:5432/postgres_v2");
            //dataSource.setUrl("jdbc:mysql://localhost:3306/BORAJI?useSSL=false");
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUsername("postgres");
            dataSource.setPassword("jU3y3gIV");
        //}
        return dataSource.getConnection();
        //return dataSource;
    }
}