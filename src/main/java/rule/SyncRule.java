package rule;

public class SyncRule {
    private String type;
    private String description;
    private String end;
    private Start start;

    public SyncRule(String startClass,String startName, String startLabel, String type, String description,String end){
        start = new Start();
        start.className = startClass;
        start.name = startName;
        start.label = startLabel;
        this.type = type;
        this.description = description;
        this.end = end;
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

    public Start getStart(){
        return start;
    }

    public class Start{
        private String label;
        private String name;
        private String className;

        public String getClassName() {
            return className;
        }

        public String getName() {
            return name;
        }

        public String getLabel() {
            return label;
        }
    }
}
