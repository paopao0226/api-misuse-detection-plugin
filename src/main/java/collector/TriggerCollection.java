package collector;

import caveat.TriggerCaveat;
import com.google.gson.Gson;
import domain.APIClass;
import domain.APIMethod;
import rule.TriggerRule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class TriggerCollection {
    private HashMap<String, APIClass> classToCaveats;
    private class  StartProperty {
        String className;
        String name;
    }

    private class RelationProperty{
        String description;
    }

    private class Start {
        int identity;
        String[] labels;
        StartProperty properties;
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
        EndProperties properties;
    }

    private class EndProperties{
        String name;
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

    public TriggerCollection(){
        classToCaveats = new HashMap<>();
        loadRule();
    }

    private void loadRule(){
        InputStream resource = getClass().getResourceAsStream("../Trigger.json");
        Gson gson = new Gson();
        if(resource != null){
            StringBuilder builder = new StringBuilder();
            String line;
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))){
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            Violation[] rules = gson.fromJson(builder.toString(), Violation[].class);
            for(Violation v: rules){
                for(Segment s: v.segments){
                    TriggerRule rule = new TriggerRule(s.start.properties.className,s.start.properties.name,s.start.labels[0],
                            s.relationship.type,s.relationship.properties.description,s.end.properties.name);
                    if(classToCaveats.containsKey(rule.getStart().getClassName())){
                        APIClass apiClass = classToCaveats.get(rule.getStart().getClassName());
                        if(apiClass.getMethod(rule.getStart().getName())==null){
                            APIMethod newMethod = new APIMethod(rule.getStart().getName());
                            newMethod.caveats.add(new TriggerCaveat(rule));
                            apiClass.methods.add(newMethod);
                        }else {
                            APIMethod method = apiClass.getMethod(rule.getStart().getName());
                            method.caveats.add(new TriggerCaveat(rule));
                        }
                    }else {
                        APIClass newClass = new APIClass(rule.getStart().getClassName());
                        APIMethod newMethod = new APIMethod(rule.getStart().getName());
                        newMethod.caveats.add(new TriggerCaveat(rule));
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
