package collector;

import caveat.RedundantCheckingCaveat;
import com.google.gson.Gson;
import domain.APIClass;
import domain.APIMethod;
import rule.RedundantCheckingRule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class RedundantCheckingCollection extends AbstractCollection{
    private HashMap<String, APIClass> classToCaveats = new HashMap<>();
    private class  SEProperty {
        String className;
        String name;
    }

    private class RelationProperty{
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

    public RedundantCheckingCollection(){loadRule();}

    @Override
    protected void loadRule(){
        InputStream resource = getClass().getResourceAsStream("../Redundant.json");
        Gson gson = new Gson();
        if(resource != null){
            Violation[] rules = gson.fromJson(resourceToString(resource), Violation[].class);
            for(Violation v : rules){
                for(Segment s : v.segments){
                    RedundantCheckingRule rule = new RedundantCheckingRule(s.start.properties.name,s.start.properties.className,s.end.properties.name,
                            s.end.properties.className,s.relationship.type,s.relationship.properties.description,s.start.labels[0],s.end.labels[0]);
                    if(classToCaveats.containsKey(rule.start.className)){
                        APIClass apiClass = classToCaveats.get(rule.start.className);
                        if(apiClass.getMethod(rule.start.name) == null){
                            APIMethod newMethod = new APIMethod(rule.start.name);
                            newMethod.caveats.add(new RedundantCheckingCaveat(rule));
                            apiClass.methods.add(newMethod);
                        } else {
                            APIMethod method = apiClass.getMethod(rule.start.name);
                            method.caveats.add(new RedundantCheckingCaveat(rule));
                        }
                    }else {
                        APIClass newClass = new APIClass(rule.start.className);
                        APIMethod newMethod = new APIMethod(rule.start.name);
                        newMethod.caveats.add(new RedundantCheckingCaveat(rule));
                        newClass.methods.add(newMethod);
                        classToCaveats.put(rule.start.className,newClass);
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
