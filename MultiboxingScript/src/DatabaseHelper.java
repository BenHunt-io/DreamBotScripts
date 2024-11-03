

import java.sql.*;

import static org.dreambot.api.methods.MethodProvider.log;

/**
 * Created by Ben on 7/11/2017.
 */
public class DatabaseHelper {

    private Connection conn;
    private Statement stmt;
    private final static String USERNAME = "USERNAME";
    private final static String REAL_HP = "REAL_HP";
    private final static String BOOSTED_HP = "BOOSTED_HP";
    private final static String USERTABLE = "USERTABLE";
    private final static String CREATE_TABLE = "CREATE TABLE "+USERTABLE+" IF NOT EXISTS ("+ USERNAME + " TEXT PRIMARY KEY,"+ REAL_HP + " INTEGER,"
                    + BOOSTED_HP + " INTEGER);";
    private final static String DROP_TABLE = "DROP TABLE IF EXISTS" + USERTABLE;

    // Fill in the rest in the function
    private final static String INSERT_DATA = "INSERT INTO " +USERTABLE+ " ("+USERNAME+","+REAL_HP+","+BOOSTED_HP+")" +
            " VALUES ";

    public void onCreateDB(){
        try {
            System.out.println("Database");
            log("inDB");
            Class.forName("org.sqlite.JDBC"); // Register the JDBC Driver - Step 2
            log("After ClassFORNAME");
            // Establish Connection. Create Database if none exists else connect to existing one - Step 3
            // url a database url of the form *  <code> jdbc:<em>subprotocol</em>:<em>subname</em></code>
            conn = DriverManager.getConnection("jdbc:sqlite:testdb2");
            log("Before Create Statement");
            stmt = conn.createStatement(); // Create statement - Step 4s
            log("After CreateStatment");
            stmt.execute(CREATE_TABLE); // Execute SQL Statement


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log("test e 1" + e.getMessage() + e.getCause() + "     " + e);
        } catch (SQLException e) {
            e.printStackTrace();
            log("test e 1" + e.getMessage() + e.getCause() + "     " + e);
        }

        log("Database Created");

    }

    public void insertUserData(String USERNAME, int REAL_HP, int BOOSTED_HP){

        String insertStatement = INSERT_DATA +"(" + USERNAME +"," + REAL_HP +","+ BOOSTED_HP+ ");";

        try {
            stmt.execute(insertStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void dropTable() {
        try {
            stmt.execute(DROP_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
