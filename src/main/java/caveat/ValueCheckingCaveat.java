package caveat;

import org.apache.commons.lang3.math.NumberUtils;
import rule.CallOrderRule;
import rule.ValueCheckingRule;

import java.util.ArrayList;
import java.util.Arrays;

public class ValueCheckingCaveat implements Caveat {

    public ValueCheckingRule rule;

    public ValueCheckingCaveat(ValueCheckingRule rule){
        this.rule = rule;
    }

    public ArrayList<CaveatViolation> checkViolation(String[] args,String[] names) {
        ArrayList<CaveatViolation> violations = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
                if (rule.parameter.equals(names[i])) {
                    System.out.println(names[i]+"   "+rule.value);
                    if (rule.value.equals("null")) {
                        if (args[i].equals("null")) {
                            if (rule.relation.equals("==")) {
                                violations.add(new CaveatViolation(names[i],args[i], "must be null"));
                                System.out.println("must be null");
                            }
                            else if (rule.relation.equals("!=")) {
                                violations.add(new CaveatViolation(names[i],args[i], "must not be null"));
                                System.out.println("must not be null");
                            }
                        }
                    } else if(NumberUtils.isNumber(rule.value)) { // constraint is an int
                        try {
                            int arg = Integer.parseInt(args[i]);
                            int constraint = Integer.parseInt(rule.value);
                            if (rule.relation.equals("<") && arg >= constraint)
                                violations.add(new CaveatViolation(names[i],args[i], "must less than "+rule.value));
                            else if (rule.relation.equals("<=") && arg > constraint)
                                violations.add(new CaveatViolation(names[i],args[i], "nust less than or equal to "+rule.value));
                            else if (rule.relation.equals(">") && arg <= constraint)
                                violations.add(new CaveatViolation(names[i],args[i], "must greater than "+rule.value));
                            else if (rule.relation.equals(">=") && arg < constraint)
                                violations.add(new CaveatViolation(names[i],args[i], "must greater than or equal to "+rule.value));
                            else if (rule.relation.equals("==") && arg != constraint)
                                violations.add(new CaveatViolation(names[i],args[i], "must equal to "+rule.value));
                            else if (rule.relation.equals("!=") && arg == constraint)
                                violations.add(new CaveatViolation(names[i],args[i], "must not be equal to "+rule.value));
                        } catch (Exception e) {
                            // do nothing if parsing fails
                        }
                    }else { // constraint is String
                        if (rule.relation.equals("==")){
                            String value = "\""+rule.value+"\"";
                            if (!args[i].contains(rule.value)) {
                                violations.add(new CaveatViolation(names[i],args[i], ("must contain "+rule.value)));
                            }
                        }
                        else if(rule.relation.equals("!=")){
                            String value = "\""+rule.value+"\"";
                            if (args[i].contains(rule.value)) {
                                violations.add(new CaveatViolation(names[i],args[i], ("must not contain "+rule.value)));
                            }
                        }
                    }
                }
        }
        return violations;
    }

//    @Override
//    public String toString() {
//        return "Invalid argument rules: " + Arrays.toString(rangeRules);
//    }
}