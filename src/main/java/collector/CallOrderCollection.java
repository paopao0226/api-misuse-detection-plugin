package collector;

import caveat.CallOrderCaveat;
import com.intellij.util.containers.hash.HashMap;
import com.google.gson.Gson;
import domain.APIClass;
import domain.APIMethod;
import rule.CallOrderRule;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CallOrderCollection extends AbstractCollection{

    private HashMap<String, APIClass> classToCaveats = new HashMap<>();

    public CallOrderCollection() { loadRule();}

    private class  SEProperty {
        String className;
        String name;
    }

    private class RelationProperty{
        String value;
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
    }

    @Override
    protected void loadRule()  {
        InputStream resource = getClass().getResourceAsStream("../CallOrder.json");
        Gson gson = new Gson();
        if(resource != null){
            Violation[] rules = gson.fromJson(resourceToString(resource), Violation[].class);
            for (Violation r : rules){
                for (Segment segment : r.segments){
                    String startClass = segment.start.properties.className;
                    String startMethod = segment.start.properties.name;
                    String startLabel = segment.start.labels[0];
                    String endClass = segment.end.properties.className;
                    String endMethod = segment.end.properties.name;
                    String endLabel = segment.end.labels[0];
                    String description = segment.relationship.properties.description;
                    CallOrderRule newRule = new CallOrderRule(startClass,startMethod,endClass,endMethod,segment.relationship.properties.value,
                            description, startLabel, endLabel);
                    if(classToCaveats.containsKey(startClass)){
                        APIClass cls = classToCaveats.get(startClass);
                        APIMethod method = cls.getMethod(startMethod);
                        if(method == null){
                            APIMethod newMethod = new APIMethod(startMethod);
                            newMethod.caveats.add(new CallOrderCaveat(newRule));
                            cls.methods.add(newMethod);
                        }
                        else{
                            method.caveats.add(new CallOrderCaveat(newRule));
                        }
                    } else {
                        APIClass cls = new APIClass(startClass);
                        APIMethod method = new APIMethod(startMethod);
                        method.caveats.add(new CallOrderCaveat(newRule));
                        cls.methods.add(method);
                        classToCaveats.put(startClass, cls);
                    }
                }
            }
        }
    }

    public APIMethod getApi(String className, String apiName){
        if(classToCaveats.containsKey(className)){
            APIClass apiClass = classToCaveats.get(className);
            APIMethod apiMethod = apiClass.getMethod(apiName);
            return apiMethod;
        }
        return null;
    }
}
