package collector;

import caveat.ValueCheckingCaveat;
import com.google.gson.Gson;
import domain.APIClass;
import domain.APIMethod;
import rule.ValueCheckingRule;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;


public class ValueCheckingCollection {

    private HashMap<String, APIClass> classToCaveats = new HashMap<>();

    public ValueCheckingCollection(){loadRule();}
    private class SProperty {
        String className;
        String name;
    }
    private class EProperty {
        String name;
    }
    private class RelationProperty{
        String expected_state;
        String description;
    }

    private class Start {
        int identity;
        String[] labels;
        SProperty properties;
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
        EProperty properties;
    }

    private class Segment{
        Start start;
        Relationship relationship;
        End end;
    }
    private class Violation{
        Segment[] segments;
        String length;
    }
    private void loadRule()  {
        InputStream resource = getClass().getResourceAsStream("../ValueChecking.json");
        try {
            Gson gson = new Gson();
            if(resource != null){
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                }
                Violation[] rules = gson.fromJson(stringBuilder.toString(), Violation[].class);
//                for (Violation rule:rules){
//                    System.out.println("rule:"+rule.segments[0].start.properties.name);
//                }
                for (Violation r : rules){
                    for (Segment segment : r.segments){
                        //----------------------handle the apiName---------------------
                        String[] names = segment.start.properties.name.toString().split("\\.");
                        String seg_name = "";
                        for (int i = 0;i < names.length-1;i++){
                            seg_name+=names[i];
                            seg_name+=".";
                        }
                        seg_name = seg_name.substring(0,seg_name.length()-1);
//                        System.out.println(seg_name);
                        //----------------------handle the param_name------------------
                        String paramName = names[names.length-1];
                        if(classToCaveats.containsKey(segment.start.properties.className)){
                            APIClass cls = classToCaveats.get(segment.start.properties.className);
                            APIMethod method = cls.getMethod(seg_name);
                            if(method == null){
                                APIMethod newMethod = new APIMethod(seg_name);
                                ValueCheckingRule newRule = new ValueCheckingRule(segment.start.properties.className,seg_name,paramName,segment.end.properties.name,segment.relationship.properties.expected_state);
                                newMethod.caveats.add(new ValueCheckingCaveat(newRule));
                                cls.methods.add(newMethod);
                            }
                            else{
                                ValueCheckingRule newRule = new ValueCheckingRule(segment.start.properties.className,seg_name,paramName,segment.end.properties.name,segment.relationship.properties.expected_state);
                                method.caveats.add(new ValueCheckingCaveat(newRule));
                                cls.methods.add(method);
                            }
                        } else {
                            APIClass cls = new APIClass(segment.start.properties.className);
                            APIMethod method = new APIMethod(seg_name);
                            ValueCheckingRule newRule = new ValueCheckingRule(segment.start.properties.className,seg_name,paramName,segment.end.properties.name,segment.relationship.properties.expected_state);
                            method.caveats.add(new ValueCheckingCaveat(newRule));
                            cls.methods.add(method);
                            classToCaveats.put(segment.start.properties.className, cls);
                        }
                    }
                }
            }
//            ValueCheckingCaveat caveat = (ValueCheckingCaveat)classToCaveats.get("com.sun.jdi.ArrayReference").getMethod("com.sun.jdi.ArrayReference.getValue(int index)").caveats.get(0);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public APIMethod getApi(String className, String apiName){
        if(classToCaveats.containsKey(className)){
            APIClass apiClass = classToCaveats.get(className);
            APIMethod apiMethod = apiClass.getMethod(apiName);
//            System.out.println(apiMethod.caveats.size());
            return apiMethod;
        }
        return null;
    }

}
