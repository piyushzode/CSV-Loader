import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import au.com.bytecode.opencsv.CSVReader;
 
public class CSVLoader {

    //private static final String SQL_INSERT_STATEMENT = "INSERT INTO ${table}(${keys}) VALUES(${values})"; //assuming that csv files will have the exact columns.
    private static final String SQL_INSERT_STATEMENT = "INSERT INTO ${table} VALUES(${values})";
    private static final String TABLE_REGEX_STATEMENT = "\\$\\{table\\}";
    private static final String KEYS_REGEX_STATEMENT = "\\$\\{keys\\}";
    private static final String VALUES_REGEX_STATEMENT = "\\$\\{values\\}";
 
    private Connection connection;
    private char seprator;

    public CSVLoader(Connection connection) {
        this.connection = connection;
        //Setting the default separator
        this.seprator = ',';
    }
     
    
    public void loadCSV(String csvFile, String tableName, boolean truncateBeforeLoad) throws Exception {
        CSVReader csvReader = null;
        if(null == this.connection) {
            throw new Exception("Not a valid connection.");
        }
        try {
            csvReader = new CSVReader(new FileReader(csvFile), this.seprator);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error occured while executing file. " + e.getMessage());
        }
 
        String[] headerRow = csvReader.readNext();
 
        if (null == headerRow) {
            throw new FileNotFoundException("No columns defined in given CSV file." +"Please check the CSV file format.");
        }
 
        String questionmarks = StringUtils.repeat("?,", headerRow.length);
        questionmarks = (String) questionmarks.subSequence(0, questionmarks.length() - 1);
 
        //Comment below 3 lines just for loading Players csv and Match Results as date and time are involved
        String query = SQL_INSERT_STATEMENT.replaceFirst(TABLE_REGEX_STATEMENT, tableName);
        query = query.replaceFirst(KEYS_REGEX_STATEMENT, StringUtils.join(headerRow, ","));
        query = query.replaceFirst(VALUES_REGEX_STATEMENT, questionmarks);
        
        System.out.println(headerRow);
        
        //uncomment only for players
        //String query = "Insert into Players (PLAYER_ID,NAME,FNAME,LNAME,DOB,COUNTRY,HEIGHT,CLUB,POSITION,CAPS_FOR_COUNTRY,IS_CAPTAIN) VALUES(?,?,?,?,TO_DATE(?,'yyyy-mm-dd'),?,?,?,?,?,?)";
        
        //uncomment only for match_results as time is involved and to retrieve the time in hh:mm:ss, we will use the  function to_char(start_time_of_match,'hh24:mi:ss')
        //String query = "Insert into MATCH_RESULTS (MATCH_ID,DATE_OF_MATCH,START_TIME_OF_MATCH,TEAM1,TEAM2,TEAM1_SCORE,TEAM2_SCORE,STADIUM_NAME,HOST_CITY) VALUES(?,TO_DATE(?,'yyyy-MM-dd'),TO_DATE(?,'HH24:MI:SS'),?,?,?,?,?,?)";
        
        System.out.println("Query is : " + query);
 
        String[] nextLine;
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = this.connection;
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(query);
 
            if(truncateBeforeLoad) {
                //delete data from table before loading csv
                conn.createStatement().execute("DELETE FROM " + tableName);
            }
 
            final int batchSize = 1000;
            int count = 0;
            Date date = null;
            while ((nextLine = csvReader.readNext()) != null) {
 
                if (null != nextLine) {
                    int index = 1;
                    for (String string : nextLine) {
                        date = DateUtil.convertToDate(string);
                        if (null != date) {
                            ps.setDate(index++, new java.sql.Date(date.getTime()));
                        } else {
                            ps.setString(index++, string);
                        }
                    }
                    ps.addBatch();
                }
                if (++count % batchSize == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch(); // insert remaining records
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            e.printStackTrace();
            throw new Exception("Error occured while loading data from file to database."+ e.getMessage());
        } finally {
            if (null != ps)
                ps.close();
            if (null != conn)
                conn.close();
 
            csvReader.close();
        }
    }
    
    public char getSeprator() {
        return seprator;
    }
 
    public void setSeprator(char seprator) {
        this.seprator = seprator;
    }
}