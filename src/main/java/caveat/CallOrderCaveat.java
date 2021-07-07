package caveat;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import rule.CallOrderRule;

import java.util.ArrayList;

public class CallOrderCaveat implements Caveat {

    public CallOrderRule rules;

    public CallOrderCaveat(CallOrderRule rules){
        this.rules = rules;
    }

    public String checkViolation(PsiType[] types){
        for (PsiType type : types) {
            String name = type.getCanonicalText();
            if (name.equals(rules.end.className))
                return null;
        }
        return rules.description;
    }

    public String checkViolation(ArrayList<PsiMethodCallExpression> methodList, int position){
        ArrayList<PsiMethod> list = new ArrayList<>();
        if(rules.relation.equals("follow")){
            for(PsiMethodCallExpression e : methodList){
                if(e.getTextOffset() > position)
                    list.add(e.resolveMethod());
            }
        }
        if(rules.relation.equals("before")){
            for(PsiMethodCallExpression e : methodList){
                if(e.getTextOffset() < position)
                    list.add(e.resolveMethod());
            }
        }
        for(PsiMethod method: list){
            String name = getMethodMessage(method)[0];
            String className = getMethodMessage(method)[1];
            if(name.equals(rules.end.methodName) && className.equals(rules.end.className))
                return null;
        }
        return rules.description;
    }

    public String checkViolation(PsiExpression[] expressions){
        for(PsiExpression e : expressions){
            if(e instanceof PsiLiteralExpression || e instanceof  PsiReferenceExpression){
                if(e.getType().getCanonicalText().equals(rules.end.className))
                    return null;
            }
            if(e instanceof PsiMethodCallExpression){
                while(e != null){
                    PsiMethod method = ((PsiMethodCallExpression) e).resolveMethod();
                    String name = getMethodMessage(method)[0];
                    String className = getMethodMessage(method)[1];
                    if(name.equals(rules.end.methodName)&&className.equals(rules.end.className))
                        return null;
                    e = PsiTreeUtil.findChildOfType(e,PsiMethodCallExpression.class);
                }
            }
        }
        return rules.description;
    }

    private String[] getMethodMessage(PsiMethod method){
        String[] names = new String[2];
        try {
            names[0] = method.getName();
            names[1] = method.getContainingClass().getQualifiedName();
            return names;
        } catch (Exception e){
            names[0] = "";
            names[1] = "";
            return names;
        }
    }
}
