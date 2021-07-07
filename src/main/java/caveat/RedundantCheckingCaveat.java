package caveat;

import rule.RedundantCheckingRule;

public class RedundantCheckingCaveat implements Caveat {

    public RedundantCheckingRule rule;

    public RedundantCheckingCaveat(RedundantCheckingRule rule){
        this.rule = rule;
    }

}
