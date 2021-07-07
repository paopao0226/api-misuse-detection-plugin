package collector;

import caveat.CallOrderCaveat;
import caveat.StateCheckingCaveat;
import com.google.gson.Gson;
import domain.APIClass;
import domain.APIMethod;
import rule.CallOrderRule;
import rule.StateCheckingRule;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class StateCheckingCollection extends AbstractCollection{

    private HashMap<String, APIMethod> methodToCaveats = new HashMap<>();
    private HashMap<String, APIClass>classToCaveats = new HashMap<>();

    private class  SEProperty {
        String className;
        String name;
    }

    private class RelationProperty{
        String expected_state;
        String violation;
        String description;
    }

    private class Start {
        int identity;
        String[] labels;
        SEProperty properties;
    }

    private class Relationship {
        int identity;
        int start;
        int end;
        String type;
        RelationProperty properties;
    }

    private class End {
        int identity;
        String[] labels;
        SEProperty properties;
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

    public StateCheckingCollection(){
        loadRule();
    }

    @Override
    protected void loadRule(){
        InputStream resource = getClass().getResourceAsStream("../StateChecking.json");
        Gson gson = new Gson();
        if(resource != null){
            Violation[] rules = gson.fromJson(resourceToString(resource), Violation[].class);
            for(Violation v: rules){
                for(Segment s: v.segments){
                    StateCheckingRule rule = new StateCheckingRule(s.start.properties.name,s.start.properties.className,s.end.properties.name,
                            s.end.properties.className,s.relationship.type,s.relationship.properties.expected_state,s.relationship.properties.violation,
                            s.relationship.properties.description,s.start.labels[0],s.end.labels[0]);
                    if(classToCaveats.containsKey(s.start.properties.className)){
                        APIClass apiClass = classToCaveats.get(s.start.properties.className);
                        if(apiClass.getMethod(s.start.properties.name) == null){
                            APIMethod newMethod = new APIMethod(s.start.properties.name);
                            newMethod.caveats.add(new StateCheckingCaveat(rule));
                            apiClass.methods.add(newMethod);
                        } else {
                            APIMethod method = apiClass.getMethod(s.start.properties.name);
                            method.caveats.add(new StateCheckingCaveat(rule));
                        }
                    } else {
                        APIClass newClass = new APIClass(s.start.properties.className);
                        APIMethod newMethod = new APIMethod(s.start.properties.name);
                        newMethod.caveats.add(new StateCheckingCaveat(rule));
                        newClass.methods.add(newMethod);
                        classToCaveats.put(s.start.properties.className,newClass);
                    }
                }
            }
        }
    }

    public APIClass getAPI(String className){
        if(classToCaveats.containsKey(className))
            return classToCaveats.get(className);
        return null;
    }

}
