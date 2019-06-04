import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/* Класс для внесения записей в таблицы USER, ROLE и USER_ROLE */
public class RowInserter {

    /* Метод для внесения записи в таблицу USER */
    public static void insertRow(Connection cn, int user_id, String name, String birthday, String login_id,
                                 String city, String email, String description) throws SQLException {
        String insert = "INSERT INTO \"USER\" (user_id, name, birthday, login_id, city, email, description)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?);";
        PreparedStatement prepStat = cn.prepareStatement(insert);
        prepStat.setInt(1, user_id);
        prepStat.setString(2, name);
        prepStat.setTimestamp(3, stringToTimestampConverter(birthday));
        prepStat.setTimestamp(4, stringToTimestampConverter(login_id));
        prepStat.setString(5, city);
        prepStat.setString(6, email);
        prepStat.setString(7, description);
        prepStat.execute();
        prepStat.close();
    }

    /* Перегрузка метода для внесения записи в таблицу ROLE */
    public static void insertRow(Connection cn, int role_id, String name, String description) throws SQLException {
        String insert = "INSERT INTO \"ROLE\" (role_id, name, description)" +
                "VALUES (?, ?, ?);";
        PreparedStatement prepStat = cn.prepareStatement(insert);
        prepStat.setInt(1,role_id);
        prepStat.setString(2,name);
        prepStat.setString(3, description);
        prepStat.execute();
        prepStat.close();
    }

    /* Перегрузка метода для внесения записи в таблицу USER_ROLE */
    public static void insertRow(Connection cn, int id, int role_id, int user_id) throws SQLException {
        String insert = "INSERT INTO \"USER_ROLE\" (id, role_id, user_id)" +
                "VALUES (?, ?, ?);";
        PreparedStatement prepStat = cn.prepareStatement(insert);
        prepStat.setInt(1, id);
        prepStat.setInt(2,role_id);
        prepStat.setInt(3,user_id);
        prepStat.execute();
        prepStat.close();
    }

    /* Хелпер метод для преобразования стрингов в TimeStamp */
    public static Timestamp stringToTimestampConverter(String givenString){
        java.sql.Timestamp ts = java.sql.Timestamp.valueOf(givenString);
        return ts;
    }
}
