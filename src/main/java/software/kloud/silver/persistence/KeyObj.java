package software.kloud.silver.persistence;

public class KeyObj {
    private String service;
    private String key;

    public KeyObj() {
    }

    public KeyObj(String service, String key) {
        this.service = service;
        this.key = key;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
