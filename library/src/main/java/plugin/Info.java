package plugin;

public class Info {
    private final String info;

    public Info() {
       this("Default info");
    }

    public Info(String info) {
       this.info = info;
    }
    
    public String getInfo() {
        return info;
    }
}