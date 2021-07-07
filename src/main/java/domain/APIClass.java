package domain;

import java.util.ArrayList;

public class APIClass {
    private String name;
    public ArrayList<APIMethod> methods;

    public APIClass(String name) {
        this.name = name;
        methods = new ArrayList<>();
    }


    public String getName() {
        return name;
    }

    public APIMethod getMethod(String methodName){
        for(APIMethod method : methods){
            if (method.name.equals(methodName))
                return method;
        }
        return null;
    }
}
