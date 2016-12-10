package my.ijat.spsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import my.ijat.spsystem.data.user_data;
import org.apache.commons.codec.binary.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MySQL {
    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    private static String sql_host = "localhost";
    private static String sql_user = "sps_server";
    private static String sql_pass = "sps_passwd";
    private static String sql_db = "spsdb_v1";

    public void write(user_data data) throws Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager.getConnection("jdbc:mysql://"+sql_host+"/"+sql_db+"?" + "user="+sql_user+"&password="+sql_pass+"&useSSL=false");

            preparedStatement = connect.prepareStatement("insert into  spsdb_v1.user_data values (?, ?, ?, ?, ?, ?, ?)");

            preparedStatement.setInt(1,data.getUid());
            preparedStatement.setString(2,data.getUsername());
            preparedStatement.setString(3,data.getFullname());

            Mac sha256_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret_key = new SecretKeySpec(server.salt.getBytes(), "HmacSHA512");
            sha256_HMAC.init(secret_key);
            String hex_pwd = Base64.encodeBase64String(sha256_HMAC.doFinal(data.getPassword().getBytes()));
            preparedStatement.setString(4, hex_pwd);

            preparedStatement.setInt(5, data.getIr_id());
            preparedStatement.setInt(6, data.getCounter());

            Mac sha256_HMAC2 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret_key2 = new SecretKeySpec(data.getPassword().getBytes(), "HmacSHA512");
            sha256_HMAC2.init(secret_key2);
            String hex_pwd2 = Base64.encodeBase64String(sha256_HMAC2.doFinal(data.getUsername().getBytes()));
            preparedStatement.setString(7, hex_pwd2);

            preparedStatement.executeUpdate();

        } finally {
            close();
        }
    }

    /*public user_data read(int uid, String pwd) throws Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://"+sql_host+"/"+sql_db+"?"
                            + "user="+sql_user+"&password="+sql_pass+"&useSSL=false");

            Mac sha256_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret_key = new SecretKeySpec(server.salt.getBytes(), "HmacSHA512");
            sha256_HMAC.init(secret_key);
            String hex_pwd = Base64.encodeBase64String(sha256_HMAC.doFinal(pwd.getBytes()));

            preparedStatement = connect.prepareStatement("SELECT * from spsdb_v1.user_data WHERE UID="+ uid + " and PASS=\'" + hex_pwd + "\'");
            resultSet = preparedStatement.executeQuery();

            resultSet.next();
            return new user_data(resultSet.getInt("UID"),
                    resultSet.getString("USERNAME"),
                    resultSet.getString("FULLNAME"),
                    resultSet.getString("PASS"),
                    resultSet.getInt("IR_ID"),
                    resultSet.getInt("COUNTER")
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            close();
        }
        throw new Exception();
        //return null;
    }*/

    public user_data read(int id, String uh) throws Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://"+sql_host+"/"+sql_db+"?"
                            + "user="+sql_user+"&password="+sql_pass+"&useSSL=false");

            preparedStatement = connect.prepareStatement("SELECT * from spsdb_v1.user_data WHERE UHASH=\'"+ uh + "\' and UID=" + id + "");
            resultSet = preparedStatement.executeQuery();

            resultSet.next();
            return new user_data(resultSet.getInt("UID"),
                    null,
                    resultSet.getString("FULLNAME"),
                    null,
                    resultSet.getInt("IR_ID"),
                    resultSet.getInt("COUNTER")
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            close();
        }
        throw new Exception();
        //return null;
    }

    public user_data read(String un, String pw) throws Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://"+sql_host+"/"+sql_db+"?"
                            + "user="+sql_user+"&password="+sql_pass+"&useSSL=false");

            Mac sha256_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret_key = new SecretKeySpec(server.salt.getBytes(), "HmacSHA512");
            sha256_HMAC.init(secret_key);
            String pwd = Base64.encodeBase64String(sha256_HMAC.doFinal(pw.getBytes()));

            preparedStatement = connect.prepareStatement("SELECT * from spsdb_v1.user_data WHERE PASS=\'"+ pwd + "\' and USERNAME=\'" + un + "\'");
            resultSet = preparedStatement.executeQuery();

            resultSet.next();

            user_data mydata = new user_data(resultSet.getInt("UID"),
                    null,
                    resultSet.getString("FULLNAME"),
                    null,
                    resultSet.getInt("IR_ID"),
                    resultSet.getInt("COUNTER")
            );
            mydata.setUhash(resultSet.getString("UHASH"));
            return mydata;

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            close();
        }
        throw new Exception();
        //return null;
    }

    public boolean updateCounter(int ir_id) throws Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager
                    .getConnection("jdbc:mysql://"+sql_host+"/"+sql_db+"?"
                            + "user="+sql_user+"&password="+sql_pass+"&useSSL=false");

            preparedStatement = connect.prepareStatement("SELECT UID,COUNTER from spsdb_v1.user_data WHERE IR_ID="+ ir_id);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();

            int count = resultSet.getInt("COUNTER");
            int uid = resultSet.getInt("UID");

            ++count;

            preparedStatement = connect.prepareStatement("UPDATE spsdb_v1.user_data SET COUNTER="+ count +" WHERE UID="+ uid);
            preparedStatement.executeUpdate();
            close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    public boolean resetCounter(int ir_id) throws Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager
                    .getConnection("jdbc:mysql://"+sql_host+"/"+sql_db+"?"
                            + "user="+sql_user+"&password="+sql_pass+"&useSSL=false");

            preparedStatement = connect.prepareStatement("UPDATE spsdb_v1.user_data SET COUNTER="+ 0 +" WHERE IR_ID="+ ir_id);
            preparedStatement.executeUpdate();
            close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    private void readDataBase() throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://"+sql_host+"/"+sql_db+"?"
                            + "user="+sql_user+"&password="+sql_pass+"&useSSL=false");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();
            // Result set get the result of the SQL query
            resultSet = statement
                    .executeQuery("select * from feedback.comments");
            writeResultSet(resultSet);

            // PreparedStatements can use variables and are more efficient
            preparedStatement = connect
                    .prepareStatement("insert into  feedback.comments values (default, ?, ?, ?, ? , ?, ?)");
            // "myuser, webpage, datum, summary, COMMENTS from feedback.comments");
            // Parameters start with 1
            preparedStatement.setString(1, "Test");
            preparedStatement.setString(2, "TestEmail");
            preparedStatement.setString(3, "TestWebpage");
            preparedStatement.setDate(4, new java.sql.Date(2009,1,1));
            preparedStatement.setString(5, "TestSummary");
            preparedStatement.setString(6, "TestComment");
            preparedStatement.executeUpdate();

            preparedStatement = connect
                    .prepareStatement("SELECT myuser, webpage, datum, summary, COMMENTS from feedback.comments");
            resultSet = preparedStatement.executeQuery();
            writeResultSet(resultSet);

            // Remove again the insert comment
            preparedStatement = connect
                    .prepareStatement("delete from feedback.comments where myuser= ? ; ");
            preparedStatement.setString(1, "Test");
            preparedStatement.executeUpdate();

            resultSet = statement
                    .executeQuery("select * from feedback.comments");
            //writeMetaData(resultSet);

        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }

    }

    private void writeMetaData(ResultSet resultSet) throws SQLException {
        //         Now get some metadata from the database
        // Result set get the result of the SQL query

        System.out.println("The columns in the table are: ");

        System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
            System.out.println("Column " + i + " " + resultSet.getMetaData().getColumnName(i));
        }
    }


    private void writeResultSet(ResultSet resultSet) throws SQLException {
        // ResultSet is initially before the first data set
        while (resultSet.next()) {
            // It is possible to get the columns via name
            // also possible to get the columns via the column number
            // which starts at 1
            // e.g. resultSet.getSTring(2);
            String user = resultSet.getString("myuser");
            String website = resultSet.getString("webpage");
            String summary = resultSet.getString("summary");
            Date date = resultSet.getDate("datum");
            String comment = resultSet.getString("comments");
            System.out.println("User: " + user);
            System.out.println("Website: " + website);
            System.out.println("summary: " + summary);
            System.out.println("Date: " + date);
            System.out.println("Comment: " + comment);
        }
    }

    private void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
                resultSet = null;
            }

            if (statement != null) {
                statement.close();
                statement = null;
            }

            if (connect != null) {
                connect.close();
                connect = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

