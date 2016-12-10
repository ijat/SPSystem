package my.ijat.spsystem.data;

public class user_data {

    private int uid;
    private String username;
    private String fullname;
    private String password;
    private String uhash;
    private int ir_id;
    private int counter;

    public user_data() {
        this.ir_id = 0;
        this.counter = 0;
        this.uid = 0;
    }

    public user_data(int id, String un, String fn, String pw, int ir, int counter ) {
        this.uid = id;
        this.username = un;
        this.fullname = fn;
        this.password = pw;
        this.ir_id = ir;
        this.counter = counter;
    }

    public void set(int id, String un, String fn, String pw, int ir, int counter) {
        this.uid = id;
        this.username = un;
        this.fullname = fn;
        this.password = pw;
        this.ir_id = ir;
        this.counter = counter;
    }

    public int getUid() {
        return uid;
    }

    public int getIr_id() {
        return ir_id;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter( int x) {
        this.counter = x;
    }

    public void addCounter() {
        this.counter += 1;
    }

    public String getUsername() {
        return this.username;
    }

    public String getFullname() {
        return this.fullname;
    }

    public String getPassword() {
        return this.password;
    }

    public void setUhash(String x) {
        this.uhash = x;
    }

    public String getUhash() {
        return this.uhash;
    }

}
