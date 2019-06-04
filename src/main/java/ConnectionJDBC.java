import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectionJDBC {
    private static final Logger logger = LogManager.getLogger(ConnectionJDBC.class);

    public static void main(String[] args) throws SQLException {
//        String url = "jdbc:postgresql://localhost:5432/postgres_v2";
//        String user = "postgres";
//        String password = "jU3y3gIV";

        /* Вызов метода для установки связи с БД */
        Connection connection = ConnectionFactory.getConnection();

        /* Проектирование базы */
        List<String> tablesSQL = new ArrayList<String>();
        tablesSQL.add("CREATE TABLE IF NOT EXISTS \"USER\"(\n" +
                "user_id serial PRIMARY KEY,\n" +
                "name varchar(50) UNIQUE NOT NULL,\n" +
                "birthday TIMESTAMP NOT NULL,\n" +
                "login_id TIMESTAMP NOT NULL,\n" +
                "city varchar(355) NOT NULL,\n" +
                "email varchar(355) UNIQUE NOT NULL,\n" +
                "description TEXT NOT NULL);");

        tablesSQL.add("CREATE TABLE IF NOT EXISTS \"ROLE\"(\n" +
                "role_id serial PRIMARY KEY,\n" +
                "name varchar(50) UNIQUE NOT NULL,\n" +
                "CHECK (name = 'Administration' OR name = 'Clients' OR name = 'Billing'),\n" +
                "description TEXT NOT NULL);");

        tablesSQL.add("CREATE TABLE IF NOT EXISTS \"USER_ROLE\"(\n" +
                "id INTEGER UNIQUE NOT NULL,\n" +
                "role_id integer NOT NULL,\n" +
                "user_id integer NOT NULL,\n" +
                "PRIMARY KEY (role_id, user_id),\t\n" +
                "CONSTRAINT USER_ROLE_user_id_fkey FOREIGN KEY (user_id)\n" +
                "\tREFERENCES \"USER\" (user_id) MATCH SIMPLE\n" +
                "\tON UPDATE NO ACTION ON DELETE NO ACTION,\n" +
                "CONSTRAINT ROLE_role_id_fkey FOREIGN KEY (role_id)\n" +
                "\tREFERENCES \"ROLE\" (role_id) MATCH SIMPLE\n" +
                "\tON UPDATE NO ACTION ON DELETE NO ACTION);");

//        tablesSQL.add("CREATE TABLE IF NOT EXISTS \"LOGS\"(\n" +
//                "ID INTEGER UNIQUE NOT NULL,\n" +
//                "DATE TIMESTAMP NOT NULL,\n" +
//                "LOG_LEVEL TEXT NOT NULL,\n" +
//                "MESSAGE TEXT NOT NULL,\n" +
//                "EXCEPTION TEXT NOT NULL);");
        tablesSQL.add("create table IF NOT EXISTS APP_LOGS(\n" +
//                "LOG_ID varchar(100) primary key,\n" +
                "LOG_ID varchar(100) primary key\n" +
//                "ENTRY_DATE timestamp,\n" +
//                "LOGGER varchar(100),\n" +
//                "LOG_LEVEL varchar(100),\n" +
//                "MESSAGE TEXT,\n" +
//                "EXCEPTION TEXT\n" +
                ");");


        /* Вызов метода для создания базы */
        createTables(connection, tablesSQL);

        /* Параметризированный запрос INSERT */
        String insertQuery = "INSERT INTO \"USER\" (user_id, name, birthday, login_id, city, email, description)\n" +
                "VALUES (?, ?, '1987-09-06 21:45:00', '2019-05-28 21:09:00', 'Innopolis', 'iluha_gia@mail.ru', 'любит вареники');";
        PreparedStatement insert = connection.prepareStatement(insertQuery);
        insert.setInt(1, 0);
        insert.setString(2,"Илья");
        insert.execute();
        insert.close();

        /* Параметризированный запрос INSERT, используя Batch процесс */
        List<String> names = Arrays.asList("Administration", "Clients", "Billing");
        List<String> descriptions = Arrays.asList("умеет готовить вареники", "не умеет готовить", "продает вареники");
        String insertBatchQuery = "INSERT INTO \"ROLE\" (role_id, name, description)\n" +
                "VALUES (?, ?, ?); ";
        try{
            PreparedStatement insertBatch = connection.prepareStatement(insertBatchQuery);
            connection.setAutoCommit(false);
            for(int i=0; i<= 2;i++){
                insertBatch.setInt(1, i);
                insertBatch.setString(2, names.get(i));
                insertBatch.setString(3, descriptions.get(i));
                insertBatch.addBatch();
            }
            insertBatch.executeBatch();
            connection.commit();
            insertBatch.close();
        }catch(Exception e){
            e.printStackTrace();
            connection.rollback();
        }

        /* Параметризированная выборка по login_ID и name одновременно из ResultSet*/
        String query = "SELECT * FROM \"USER\";";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        while(resultSet.next()){
            String resultSetLogin = resultSet.getString("login_ID");
            String resultSetName = resultSet.getString("name");
            System.out.println("Параметризированная выборка: " + "login_ID: " + resultSetLogin + "; name: " + resultSetName);
        }
        resultSet.close();
        statement.close();

        /* Выполнение 2-х SQL операции Insert и установка логической точки сохранения(SAVEPOINT) */
        Savepoint savepointOne = connection.setSavepoint("SavepointOne");
        try{
            insertRow(connection, 1,"Вася", "1984-04-15 14:00:00", "2019-05-29 00:29:00", "Новосибирск",
                    "1@temp.com", "любит пельмени");
            insertRow(connection, 0, 0, 0);
            insertRow(connection, 1, 1, 1);
            connection.commit();
            //logger.info("This is JDBC message #{} at INFO level.", 1);
        }catch(SQLException e){
            connection.rollback(savepointOne);
            System.out.println("Cработал SQLException");
        }

        /* Выполнение 2-х SQL операции Insert, установка логической точки сохранения(SAVEPOINT A), ввод некорректных данных */
        Savepoint savepointA = connection.setSavepoint("SavepointA");
        try{
            insertRow(connection, 3, "Некорректные данные", "не любит пельмени");
            logger.info("This is JDBC message #{} at INFO level.", 1);
        //}catch(SQLException e){
        }catch(Exception e){
            connection.rollback(savepointA);
            System.out.println("Cработал генерируемый SQLException, запускается rollback() к точке A");
            logger.error("Ошибочка...");
        }

        /* Закрываем connection */
        connection.close();
    }

    /* Метод для установки связи с БД */
//    public static Connection connectJDBC(String url, String user, String password) throws SQLException {
//        Connection cn = null;
//        DriverManager.registerDriver(new org.postgresql.Driver());
//        cn = DriverManager.getConnection(url, user, password);
//        return cn;
//    }

    /* Метод для создания базы */
    public static void createTables(Connection cn, List<String> tablesSQL) throws SQLException {
        PreparedStatement ps = null;
        for (String elem : tablesSQL){
            ps = cn.prepareStatement(elem);
            ps.executeUpdate();
        }
        ps.close();
    }

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

    /* Перегразка метода для внесения записи в таблицу ROLE */
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

    /* Перегразка метода для внесения записи в таблицу USER_ROLE */
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