package rule;

public class ValueCheckingRule {
    public String className;
    public String apiName;
    public String parameter;
    public String relation;
    public String value;
//    public String param;

    public ValueCheckingRule(String className,String apiName,String parameter, String value, String relation){
        this.className = className;
        this.apiName = apiName;
        this.parameter = parameter;
        this.value = value;
        this.relation = relation;
    }

}
