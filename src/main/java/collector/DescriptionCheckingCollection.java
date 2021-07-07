package collector;

import caveat.DescriptionCheckingCaveat;
import caveat.ValueCheckingCaveat;
import com.intellij.util.containers.hash.HashMap;
import com.google.gson.Gson;
import domain.APIClass;
import domain.APIMethod;
import rule.DescriptionCheckingRule;
import rule.ValueCheckingRule;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class DescriptionCheckingCollection {

    private HashMap<String, APIClass> classToCaveats = new HashMap<>();

    public DescriptionCheckingCollection() { loadRule();}

    private class  SProperty {
        String className;
        String name;
    }
    private class EProperty {
        String name;
    }

    private class RelationProperty{
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
    }

    private void loadRule()  {
        InputStream resource = getClass().getResourceAsStream("../RedundantChecking.json");
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
//                System.out.println(rules[1].segments[0].start.properties.className);
//                System.out.println(rules[1].segments[0].start.properties.name);
//                System.out.println(rules[1].segments[0].relationship.properties.description);

                for (Violation r : rules){
                    for (Segment segment : r.segments){
                        if(classToCaveats.containsKey(segment.start.properties.className)){
                            APIClass cls = classToCaveats.get(segment.start.properties.className);
                            APIMethod method = cls.getMethod(segment.start.properties.name);
                                if(method == null){
                                    APIMethod newMethod = new APIMethod(segment.start.properties.name);
                                    DescriptionCheckingRule newRule = new DescriptionCheckingRule(segment.start.properties.className, segment.start.properties.name, segment.relationship.properties.description, segment.relationship.type,segment.end.properties.name);
                                    newMethod.caveats.add(new DescriptionCheckingCaveat(newRule));
                                    cls.methods.add(newMethod);
                                }
                                else{
                                    DescriptionCheckingRule newRule = new DescriptionCheckingRule(segment.start.properties.className, segment.start.properties.name, segment.relationship.properties.description, segment.relationship.type,segment.end.properties.name);
                                    method.caveats.add(new DescriptionCheckingCaveat(newRule));
                                    cls.methods.add(method);
                                }
                            } else {
                                APIClass cls = new APIClass(segment.start.properties.className);
                                APIMethod method = new APIMethod(segment.start.properties.name);
                                DescriptionCheckingRule newRule = new DescriptionCheckingRule(segment.start.properties.className, segment.start.properties.name, segment.relationship.properties.description, segment.relationship.type,segment.end.properties.name);
                                method.caveats.add(new DescriptionCheckingCaveat(newRule));
                                cls.methods.add(method);
                                classToCaveats.put(segment.start.properties.className, cls);
                            }
                    }
                }

            }
//            DescriptionCheckingCaveat caveat = (DescriptionCheckingCaveat) classToCaveats.get("javax.crypto.Cipher").getMethod("javax.crypto.Cipher").caveats.get(0);
//            System.out.println("caveat_className:"+caveat.rules.className);
//            System.out.println("caveat_name:"+caveat.rules.apiName);
//            System.out.println("caveat_value:"+caveat.rules.value);
            System.out.println("classToCaveats:"+"         "+classToCaveats.get("javax.crypto.Cipher").methods.size());

        } catch (Exception e){
            e.printStackTrace();
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
