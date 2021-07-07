package rule;

public class DescriptionCheckingRule {
    public String className;
    public String apiName;
    public String value;
    public String kind;
    public String name;

    public DescriptionCheckingRule(String className, String apiName, String value, String kind,String name){
        this.className = className;
        this.apiName = apiName;
        this.value = value;
        this.kind = kind;
        this.name = name;
    }
}
