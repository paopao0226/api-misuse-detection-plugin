package domain;

import caveat.Caveat;
import caveat.StateCheckingCaveat;

import java.util.ArrayList;

public class APIMethod {
    public String name;

    public ArrayList<Caveat> caveats;

    public APIMethod(String name) {
        this.name = name;
        this.caveats = new ArrayList<>();
    }

    public Caveat getCaveat(Class<?> cls) {
        for(Caveat c : caveats) {
            if (c.getClass() == cls) return c;
        }
        return null;
    }

}
