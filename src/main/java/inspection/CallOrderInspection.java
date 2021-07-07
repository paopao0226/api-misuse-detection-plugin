package inspection;

import caveat.CallOrderCaveat;
import caveat.Caveat;
import collector.CallOrderCollection;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import domain.APIMethod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class CallOrderInspection extends AbstractBaseJavaLocalInspectionTool {

    private CallOrderCollection rules = new CallOrderCollection();

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly){
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                PsiMethod method = expression.resolveMethod();
                ArrayList<String> violations = new ArrayList<>();
                if(method != null){
                    APIMethod apiMethod = rules.getApi(method.getContainingClass().getQualifiedName(),method.getName());
                    if(apiMethod != null){
                        for(Caveat ca : apiMethod.caveats){
                            CallOrderCaveat caveat = (CallOrderCaveat) ca;
                            if(caveat.rules.start.label.equals("method")){
                                if(caveat.rules.end.label.equals("parameter")){
                                    PsiType[] types = new PsiType[method.getParameterList().getParameters().length];
                                    for(int i=0; i<types.length;i++)
                                        types[i] = method.getParameterList().getParameters()[i].getType();
                                    violations.add(caveat.checkViolation(types));
                                }
                                if(caveat.rules.end.label.equals("method")){
                                    PsiVariable variable = null;
                                    if(caveat.rules.start.className.equals(caveat.rules.end.className)){
                                        if(expression.getParent() instanceof PsiVariable)
                                            variable = (PsiVariable) expression.getParent();
                                        else if(expression.getMethodExpression().getQualifier().getReference().resolve()instanceof PsiVariable)
                                            variable = (PsiVariable) expression.getMethodExpression().getQualifier().getReference().resolve();

                                    }else {
                                        PsiVariable init = (PsiVariable) expression.getMethodExpression().getQualifier().getReference().resolve();
                                        Query<PsiReference> query = ReferencesSearch.search(Objects.requireNonNull(init));
                                        Collection<PsiReference> list = query.findAll();
                                        for(PsiReference r : list){
                                            if(PsiTreeUtil.getParentOfType(r.getElement(),PsiNewExpression.class) != null){
                                                PsiNewExpression ex = PsiTreeUtil.getParentOfType(r.getElement(),PsiNewExpression.class);
                                                if(ex.getParent() instanceof PsiVariable)
                                                    variable = (PsiVariable) ex.getParent();
                                                else if(ex.getParent() instanceof PsiAssignmentExpression){
                                                    PsiReference reference = ex.getParent().getFirstChild().getReference();
                                                    if(reference.resolve() instanceof PsiVariable)
                                                        variable = (PsiVariable) reference.resolve();
                                                }
                                            }
                                        }
                                    }
                                    if(variable != null){
                                        Query<PsiReference> query = ReferencesSearch.search(Objects.requireNonNull(variable));
                                        Collection<PsiReference> list = query.findAll();
                                        ArrayList<PsiMethodCallExpression> methodList =  new ArrayList<>();
                                        for(PsiReference r : list){
                                            if(r.getElement().getParent().getParent() instanceof  PsiMethodCallExpression)
                                                methodList.add((PsiMethodCallExpression) r.getElement().getParent().getParent());
                                        }
                                        violations.add(caveat.checkViolation(methodList,expression.getTextOffset()));
                                    }
                                }
                            }
                        }
                    }
                    for (String violation : violations){
                        if(violation != null)
                            holder.registerProblem(expression,violation);
                    }
                }
            }

            @Override
            public  void  visitNewExpression(PsiNewExpression expression){
                ArrayList<String> violations = new ArrayList<>();
                PsiMethod method = expression.resolveMethod();
                if(method != null){
                    APIMethod apiMethod = rules.getApi(method.getContainingClass().getQualifiedName(),method.getName());
                    if(apiMethod != null){
                        for(Caveat ca : apiMethod.caveats){
                            CallOrderCaveat caveat = (CallOrderCaveat) ca;
                            if(caveat.rules.start.label.equals("newExpression")){
                                if(caveat.rules.end.label.equals("parameter")){
                                    PsiExpression[] expressions = expression.getArgumentList().getExpressions();
                                    violations.add(caveat.checkViolation(expressions));
                                }
                            }
                        }
                    }
                }
                for (String violation : violations){
                    if(violation != null)
                        holder.registerProblem(expression,violation);
                }
            }

            @Override
            public void visitVariable(PsiVariable variable){
                String className = variable.getType().getCanonicalText();
                String name = variable.getType().getPresentableText();
                APIMethod apiMethod = rules.getApi(className,name);
                if(apiMethod != null){
                    ArrayList<String> violations = new ArrayList<>();
                    for(Caveat ca : apiMethod.caveats){
                        CallOrderCaveat caveat = (CallOrderCaveat) ca;
                        if(caveat.rules.start.label.equals("variable")){
                            Query<PsiReference> query = ReferencesSearch.search(variable);
                            Collection<PsiReference> list = query.findAll();
                            ArrayList<PsiMethodCallExpression> methodList =  new ArrayList<>();
                            for(PsiReference r : list){
                                if(r.getElement().getParent().getParent() instanceof  PsiMethodCallExpression)
                                    methodList.add((PsiMethodCallExpression) r.getElement().getParent().getParent());
                            }
                            violations.add(caveat.checkViolation(methodList,variable.getTextOffset()));
                        }
                    }
                    for (String violation : violations){
                        if(violation != null)
                            holder.registerProblem(variable,violation);
                    }

                }
            }
        };
    }

}
