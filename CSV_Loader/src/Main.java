import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
 
public class Main {
	
    private static String JDBC_CONNECTION_URL = "jdbc:oracle:thin:@localhost:1521:ORCL";
 
    public static void main(String[] args) throws Exception {
    	//Connect to Oracle DB with username and password 
    	String url = "jdbc:oracle:thin:@localhost:1521:ORCL";
    	Properties props = new Properties();
        props.setProperty("user", "system");
        props.setProperty("password", "piyushz");
        Connection connection = DriverManager.getConnection(url,props);
        
    	CSVLoader loader = new CSVLoader(connection);
    	
    	//uncomment the following lines for table specific loading
        
    	//For Country Table
    	loader.loadCSV("C:\\Users\\Piyush\\DB1_Project_Code\\Input_Data\\Country.csv", "COUNTRY", true);
    	
    	//For players table
    	//loader.loadCSV("C:\\Users\\Piyush\\DB1_Project_Code\\Input_Data\\Players.csv", "PLAYERS", true);
    
    	//For PLAYER_CARDS
    	//loader.loadCSV("C:\\Users\\Piyush\\DB1_Project_Code\\Input_Data\\Player_Cards.csv", "PLAYER_CARDS", true);
    	
    	//For PLAYER_ASSISTS_GOALS table
    	//loader.loadCSV("C:\\Users\\Piyush\\DB1_Project_Code\\Input_Data\\Player_Assists_Goals.csv", "PLAYER_ASSISTS_GOALS", true);
    	
    	//For MATCH_RESULTS table
    	//loader.loadCSV("C:\\Users\\Piyush\\DB1_Project_Code\\Input_Data\\match_results.csv", "MATCH_RESULTS", true);
    	
    	System.out.println("Data loaded successfully!");
    }
}