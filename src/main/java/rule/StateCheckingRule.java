package rule;

public class StateCheckingRule {
    public Start start = new Start();
    public Start end = new Start();
    public String type;
    public String expected_state;
    public String violation;
    public String description;

    public StateCheckingRule(String start,String startClass,  String end,String endClass, String type, String expected_state,
                             String violation,String description,String startLabel, String endLabel){
        this.start.methodName = start;
        this.start.className = startClass;
        this.end.methodName = end;
        this.end.className = endClass;
        this.type = type;
        this.expected_state = expected_state;
        this.violation = violation;
        this.description = description;
        this.start.label = startLabel;
        this.end.label = endLabel;
    }

    public class Start{
        public String methodName;
        public String className;
        public String label;
        private Start(){

        }
    }
}
