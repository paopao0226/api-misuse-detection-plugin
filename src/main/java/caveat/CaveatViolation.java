package caveat;

public class CaveatViolation {
    String name;
    String value;
    String reason;

    CaveatViolation(String name,String value, String reason) {
        this.name = name;
        this.value = value;
        this.reason = reason;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
