package caveat;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiTryStatement;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import rule.TriggerRule;

public class TriggerCaveat implements Caveat{

    private TriggerRule rule;

    public TriggerCaveat(TriggerRule rule){
        this.rule = rule;
    }

    public TriggerRule getRule() {
        return rule;
    }

    public String checkViolation(PsiMethodCallExpression expression){
        if(PsiTreeUtil.getParentOfType(expression, PsiTryStatement.class)==null)
            return "Method Call "+ expression.resolveMethod().getName()+" should be surrounded by try-catch statement";
        else{
            PsiTryStatement statement = PsiTreeUtil.getParentOfType(expression, PsiTryStatement.class);
            try {
                for(int i=0; i<statement.getCatchBlockParameters().length; i++){
                    PsiType type = statement.getCatchBlockParameters()[i].getType();
                    System.out.println("type is :"+type.getCanonicalText());
                    if(type.getCanonicalText().equals(rule.getEnd()))
                        break;
                    System.out.println(Class.forName(rule.getEnd()).getCanonicalName());
                    System.out.println(Class.forName(type.getCanonicalText()).getCanonicalName());
                    if(Class.forName(type.getCanonicalText()).isAssignableFrom(Class.forName(rule.getEnd())))
                        break;
                    if(i == statement.getCatchBlockParameters().length-1)
                        return rule.getDescription();
                }
            }catch (NullPointerException e){
                return "Method Call "+ expression.resolveMethod().getName()+" should be surrounded by try-catch statement";
            }catch (ClassNotFoundException f){
                f.printStackTrace();
            }
        }
        return null;
    }
}
