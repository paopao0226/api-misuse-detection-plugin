package rule;

public class TriggerRule {

    private Start start;
    private String type;
    private String description;
    private String end;

    public TriggerRule(String className,String name,String label, String type, String description, String end){
        start = new Start();
        start.className = className;
        start.name = name;
        start.label = label;
        this.type = type;
        this.description = description;
        this.end = end;
    }

    public Start getStart() {
        return start;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getEnd() {
        return end;
    }

    public class Start{
        private String name;
        private String className;
        private String label;
        private Start(){

        }

        public String getName() {
            return name;
        }

        public String getClassName() {
            return className;
        }

        public String getLabel() {
            return label;
        }
    }
}
