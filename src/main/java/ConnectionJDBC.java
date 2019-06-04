import java.sql.*;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectionJDBC {
    private static final Logger logger = LogManager.getLogger(ConnectionJDBC.class);

    public static void main(String[] args) throws SQLException {

        /* Вызов метода класса ConnectionFactory для установки связи с БД */
        Connection connection = ConnectionFactory.getConnection();

        /* Вызов основного метода класса TablesCreator для наполнения базы */
        TablesCreator.createTables(connection);

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
            logger.info("Сообщение JDBC номер #{} уровня INFO.", 1);
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
            logger.error("Сообщение JDBC номер #{} уровня ERROR.", 1);
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

        /* Выполнение 2-х SQL операций Insert и установка логической точки сохранения(SAVEPOINT) */
        Savepoint savepointOne = connection.setSavepoint("SavepointOne");
        try{
            logger.info("Сообщение JDBC номер #{} уровня INFO.", 2);
            RowInserter.insertRow(connection, 1,"Вася", "1984-04-15 14:00:00", "2019-05-29 00:29:00", "Новосибирск",
                    "1@temp.com", "любит пельмени");
            RowInserter.insertRow(connection, 0, 0, 0);
            RowInserter.insertRow(connection, 1, 1, 1);
            connection.commit();
        }catch(SQLException e){
            logger.error("Сообщение JDBC номер #{} уровня ERROR.", 2);
            connection.rollback(savepointOne);
            System.out.println("Cработал SQLException");
        }

        /* Выполнение 2-х SQL операций Insert, установка логической точки сохранения(SAVEPOINT A), ввод некорректных данных */
        Savepoint savepointA = connection.setSavepoint("SavepointA");
        try{
            logger.info("Сообщение JDBC номер #{} уровня INFO.", 3);
            RowInserter.insertRow(connection, 3, "Некорректные данные", "не любит пельмени");
        }catch(SQLException e){
            logger.error("Сообщение JDBC номер #{} уровня ERROR.", 3, e);
            connection.rollback(savepointA);
            System.out.println("Cработал генерируемый SQLException, запускается rollback() к точке A");
        }

        /* Печатаем содержимое таблицы APP_LOGS */
        System.out.println("Печатаем содержимое таблицы APP_LOGS: ");
        String queryLogs = "SELECT * FROM APP_LOGS;";
        Statement statementLogs = connection.createStatement();
        ResultSet resultSetLogs = statementLogs.executeQuery(queryLogs);
        ResultSetMetaData rsmd = resultSetLogs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        while (resultSetLogs.next()) {
            for(int i = 1; i < columnsNumber; i++)
                System.out.print(resultSetLogs.getString(i) + " ");
            System.out.println();
        }

        /* Закрываем connection */
        connection.close();
    }
}