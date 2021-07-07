package caveat;

import rule.StateCheckingRule;

import java.util.ArrayList;

public class StateCheckingCaveat implements Caveat {

    public StateCheckingRule rule;

    public StateCheckingCaveat(StateCheckingRule rule){
        this.rule = rule;
    }


    public String  checkViolation(String expectedState){
        if(rule.type.equals("s_c")){
            if(!expectedState.equals(rule.expected_state)){
                String violation = rule.violation+":"+rule.start.className+"."+rule.start.methodName+" expected "+
                        rule.end.methodName + " = " + rule.expected_state;
                return violation;
            }
        }
        return null;
    }


}
