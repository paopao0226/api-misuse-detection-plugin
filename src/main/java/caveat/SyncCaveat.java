package caveat;

import com.intellij.psi.PsiSynchronizedStatement;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.util.PsiTreeUtil;
import rule.SyncRule;

public class SyncCaveat implements Caveat {

    private SyncRule rule;

    public SyncCaveat(SyncRule rule){
        this.rule = rule;
    }

    public String checkViolation(PsiVariable variable){
        if(PsiTreeUtil.getParentOfType(variable, PsiSynchronizedStatement.class) == null)
            return rule.getDescription();
        return null;
    }
}
