package rule;

public class RedundantCheckingRule {
    public Start start = new Start();
    public Start end = new Start();
    public String type;
    public String description;

    public RedundantCheckingRule(String start,String startClass, String end,String endClass, String type,String description,String startLabel, String endLabel){
        this.start.name = start;
        this.start.className = startClass;
        this.end.name = end;
        this.end.className = endClass;
        this.type = type;
        this.description = description;
        this.start.label = startLabel;
        this.end.label = endLabel;
    }

    public class Start{
        public String name;
        public String className;
        public String label;
        private Start(){

        }
    }
}
