package rule;

public class CallOrderRule {
    public Start start = new Start();
    public Start end = new Start();
    public String relation;
    public String description;

    public CallOrderRule(String startClass,String startMethod, String endClass,String endName, String relation, String description,
                         String startLabel, String endLabel){
        start.className = startClass;
        start.methodName = startMethod;
        end.className = endClass;
        end.methodName = endName;
        this.relation = relation;
        this.description = description;
        start.label = startLabel;
        end.label = endLabel;
    }

    public class Start {
        public String className;
        public String methodName;
        public String label;
        public Start(){

        }
    }
}
