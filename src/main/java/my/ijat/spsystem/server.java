package my.ijat.spsystem;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Random;

import javafx.stage.WindowEvent;
import my.ijat.spsystem.data.user_data;

import static spark.Spark.*;
import com.google.gson.*;
import com.fazecast.jSerialComm.*;

public class server extends Application {

    // String constants
    public static String title = "SPSystem";
    public static String title_sub = "Server Mode";
    public static String version = "0.2";
    public static String salt = "zistwCHCkFrnomqIB7FycBnvaTURNDuoaKqgr0hghC00iBiZLGWjhex256QUwGICxunJZ7CLjASU0Y3k9vRV2V4f9xJxwMVy389D";

    // Scenes
    private Stage primaryStage;
    private Scene primaryScene;
    private BorderPane root;

    // Controls
    private TextArea tA;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YY HH:mm:ss");

    public void runSerialMonitor() {
        try {
            addLog("Serial Monitor Initializing...");
            SerialPort comPort = SerialPort.getCommPorts()[0];
            comPort.openPort();
            comPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                        return;
                    if (comPort.bytesAvailable() < 20)
                        return;
                    byte[] newData = new byte[comPort.bytesAvailable()];
                    int numRead = comPort.readBytes(newData, newData.length);

                    //System.out.println("Read " + numRead + " bytes.");
                    String str = new String(newData, StandardCharsets.UTF_8);
                    //System.out.println(str);

                    try {
                        org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
                        org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(str);

                        MySQL db = new MySQL();

                        Long ir_id = (long) json.get("IR");
                        Long status = (long) json.get("STATUS");

                        System.out.println(json.toJSONString());

                        if (status.intValue() == 1) {
                            addLog("BOX:" + ir_id.toString() + " UPDATE");
                            db.updateCounter(ir_id.intValue());
                        } else {
                            addLog("BOX:" + ir_id.toString() + " RESET");
                            db.resetCounter(ir_id.intValue());
                        }

                    } catch (Exception e) {
                        addLog("Serial invalid data");
                        e.printStackTrace();
                    }
                }
            });
            addLog("Serial Monitor: OK");
        } catch (Exception e) {
            addLog("Serial Monitor: FAILED");
        }
    }

    public void runWebAPI() {
        int maxThreads = 8;
        threadPool(maxThreads);
        port(6789);

        File f = new File("src/main/resources/KeyStore.jks");
        if (f.exists())
            secure("src/main/resources/KeyStore.jks", "123456", null, null);
        else
            secure("KeyStore.jks", "123456", null, null);

        get("/", (req, res) -> {
            res.type("text/html");
            return "<HTML><h2>" + title + " " + title_sub + " API</h2></HTML>";
        });

        get("/user/get", (req, res) -> {
            try {
                String tUsr = req.headers("uid");
                String tApiKey = req.headers("api_key");
                MySQL db = new MySQL();
                user_data data = db.read(Integer.valueOf(tUsr), tApiKey);
                Gson json = new Gson();

                addLog(tUsr + ": USERGET OK");
                res.type("application/json");
                return json.toJson(data);
            } catch (Exception e) {
                addLog(req.ip() + ": USERGET FAILED");
                res.type("application/json");
                return "{\"status\":\"FAIL\"}";
            }
        });

        post("/user/add", (req, res) -> {
            try {
                String tUsr = req.headers("username");
                String tPwd = req.headers("password");
                String tFullName = req.headers("fullname");
                String ir_id = req.headers("ir_id");

                // Generate random number for UID
                Random r = new Random();
                int Low = 100000;
                int High = 99999999;
                int uid = r.nextInt(High - Low) + Low;

                user_data mydata = new user_data(uid,tUsr,tFullName,tPwd,Integer.valueOf(ir_id),0);
                MySQL mydb = new MySQL();
                mydb.write(mydata);

                addLog(tUsr + ": USERADD OK");
                res.type("application/json");
                return "{\"status\":\"OK\"}";
            } catch (Exception e) {
                addLog(req.ip() + ": USERADD FAILED");
                res.type("application/json");
                return "{\"status\":\"FAIL\"}";
            }
        });

        get("/user/login", (req, res) -> {
            try {
                String tUsr = req.headers("username");
                String tPwd = req.headers("password");

                MySQL mydb = new MySQL();
                user_data mydata = mydb.read(tUsr,tPwd);
                Gson json = new Gson();

                addLog(tUsr + ": USERLOGIN OK");
                res.type("application/json");
                return json.toJson(mydata);
            } catch (Exception e) {
                e.printStackTrace();
                addLog(req.ip() + ": USERLOGIN FAILED");
                res.type("application/json");
                return "{\"status\":\"FAIL\"}";
            }
        });
    }

    public void runMain() throws Exception {
        addLog(title + " (" + title_sub + ") v" + version + " by Ijat.my");
        addLog("Starting server...");

        // JDBC
        addLog("Initializing JDBC...");
        try {
            addLog("JDBC: OK");
        } catch (Exception e) {
            addLog("JDBC: FAILED - " + e.toString());
            e.printStackTrace();
        }

        runWebAPI();
        runSerialMonitor();
    }

    @Override
    public void start(Stage pS) throws Exception {
        this.primaryStage = pS;
        this.primaryStage.setTitle(title + " " + title_sub);

        this.root = new BorderPane();

        Label label = new Label("Hello World, JavaFX !");
        this.tA = new TextArea();

        tA.setPrefSize(600,312);
        tA.setFont(new Font("Arial", 14));
        tA.setEditable(false);

        tA.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                                Object newValue) {
                tA.selectPositionCaret(tA.getLength());
                tA.deselect();
            }
        });

        this.root.setTop(tA);

        this.primaryScene = new Scene(root);
        this.primaryStage.setScene(this.primaryScene);
        this.primaryStage.setMinWidth(600);
        this.primaryStage.setMinHeight(350);
        this.primaryStage.setMaxWidth(600);
        this.primaryStage.setMaxHeight(350);
        this.primaryStage.show();

        this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                try {
                    addLog("Closing...");
                    stop();
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // End of GUI -- Start main function
        runMain();
    }

    public void addLog(String s) {
        Timestamp t = new Timestamp(System.currentTimeMillis());
        tA.appendText("[" + sdf.format(t) + "]  " + s);
        tA.appendText("\n");
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}
