package caveat;

import rule.DescriptionCheckingRule;

import java.util.ArrayList;

public class DescriptionCheckingCaveat implements Caveat {

    public DescriptionCheckingRule rules;

    public DescriptionCheckingCaveat(DescriptionCheckingRule rules){
        this.rules = rules;
    }

    public ArrayList<CaveatViolation> checkViolation(String className, String apiName){
        ArrayList<CaveatViolation> violations = new ArrayList<>();
        if(apiName.equals(rules.apiName)){
            String checking_kind = "";
            if(rules.kind.equals("r_c")){
                checking_kind = "redundant-checking";
            }
            else if(rules.kind.equals("d_c")){
                checking_kind = "duplicate-checking";
            }
            else if(rules.kind.equals("sy_c")){
                checking_kind = "synchronization-checking";
            }
            violations.add(new CaveatViolation(apiName,checking_kind,rules.value));
        }
        return violations;
    }

}
