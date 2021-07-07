package collector;

import caveat.SyncCaveat;
import com.google.gson.Gson;
import domain.APIClass;
import domain.APIMethod;
import rule.SyncRule;

import java.io.InputStream;
import java.util.HashMap;

public class SyncCollection extends AbstractCollection {
    private HashMap<String, APIClass> classToCaveats = new HashMap<>();

    private class StartProperty{
        String className;
        String name;
    }

    private class Start{
        int identity;
        String[] labels;
        StartProperty properties;
    }

    private class RelationProperty{
        String description;
    }

    private class Relationship{
        int identity;
        int start;
        int end;
        String type;
        RelationProperty properties;
    }

    private class EndProperty{
        String name;
    }

    private class End{
        int identity;
        String[] labels;
        EndProperty properties;
    }

    private class Segment{
        Start start;
        Relationship relationship;
        End end;
    }

    private class Violation{
        Segment[] segments;
        double length;
    }

    public SyncCollection(){
        loadRule();
    }

    @Override
    protected void loadRule() {
        InputStream resource = getClass().getResourceAsStream("../SyncChecking.json");
        if(resource != null){
            Gson gson = new Gson();
            Violation[] rules = gson.fromJson(resourceToString(resource),Violation[].class);
            for(Violation v: rules){
                for(Segment s: v.segments){
                    SyncRule rule = new SyncRule(s.start.properties.className,s.start.properties.name,s.start.labels[0],
                            s.relationship.type, s.relationship.properties.description, s.end.properties.name);
                    if(classToCaveats.containsKey(rule.getStart().getClassName())){
                        APIClass apiClass = classToCaveats.get(rule.getStart().getClassName());
                        if(apiClass.getMethod(rule.getStart().getName())==null){
                            APIMethod newMethod = new APIMethod(rule.getStart().getName());
                            newMethod.caveats.add(new SyncCaveat(rule));
                            apiClass.methods.add(newMethod);
                        }else {
                            APIMethod apiMethod = apiClass.getMethod(rule.getStart().getName());
                            apiMethod.caveats.add(new SyncCaveat(rule));
                        }
                    }else {
                        APIClass newClass = new APIClass(rule.getStart().getClassName());
                        APIMethod newMethod = new APIMethod(rule.getStart().getName());
                        newMethod.caveats.add(new SyncCaveat(rule));
                        newClass.methods.add(newMethod);
                        classToCaveats.put(newClass.getName(),newClass);
                    }
                }
            }
        }
    }

    public APIMethod getApi(String className, String apiName){
        if(classToCaveats.containsKey(className)){
            APIClass apiClass = classToCaveats.get(className);
            return apiClass.getMethod(apiName);
        }
        return null;
    }
}
