package inspection;

import caveat.CallOrderCaveat;
import caveat.Caveat;
import caveat.CaveatViolation;
import caveat.ValueCheckingCaveat;
import collector.CallOrderCollection;
import collector.ValueCheckingCollection;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.impl.EmptySubstitutorImpl;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import domain.APIMethod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class ValueCheckingInspection extends AbstractBaseJavaLocalInspectionTool {

    private ValueCheckingCollection rules = new ValueCheckingCollection();

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly){
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                //don't know why to super call
                super.visitMethodCallExpression(expression);
                //resolve the method
                PsiMethod method = expression.resolveMethod();
//                System.out.println("method:"+method);
                if (method != null) {
                    String apiName = method.getName();
                    if (method.getContainingClass() != null) {
                        String className = method.getContainingClass().getQualifiedName();
                        System.out.println("value className:"+className);
//                        System.out.println("value method:"+method.getText());
                        //acquire all the parameters from the method
                        PsiParameter[] parameters = method.getParameterList().getParameters();
//                        System.out.println("parameters:"+parameters);
                        Object[] paramTypes = new String[parameters.length];
                        String[] paramNames = new String[parameters.length];
                        apiName = className +"."+ apiName + "(";
                        for (int i = 0; i < parameters.length; i++) {
                            apiName += parameters[i].getType().getPresentableText();
                            apiName += " ";
                            apiName += parameters[i].getName().toString();
                            apiName += ", ";
                        }
                        apiName = apiName.substring(0,apiName.length()-2);
                        apiName += ")";
                        for (int i = 0;i < parameters.length;i++){
                            paramTypes[i] = parameters[i].getType().getPresentableText();
                            paramNames[i] = parameters[i].getName();
                        }
                        System.out.println("value apiFullName:"+apiName);

                        APIMethod apiMethod = rules.getApi(className, apiName);
                        if (apiMethod != null) {
                            for(Caveat c:apiMethod.caveats) {
                                if (c != null) {
                                    System.out.println("argument:"+expression.getArgumentList());
                                    PsiExpression[] expressions = expression.getArgumentList().getExpressions();
                                    System.out.println("expression:" + expressions);
                                    String[] args = new String[expressions.length];
                                    System.out.println(args.length);
                                    for (int i = 0; i < expressions.length; i++) {
                                        args[i] = expressions[i].getText();
                                        System.out.println("args[i]:" + args[i]);
                                    }
                                    ValueCheckingCaveat vc = (ValueCheckingCaveat) c;
                                    ArrayList<CaveatViolation> lst = vc.checkViolation(args, paramNames);
                                    System.out.println("handling------------------------");
                                    if (!lst.isEmpty()) {
                                        for (CaveatViolation cv : lst) {
                                            holder.registerProblem(expression,
                                                    "Parameter \"" + cv.getName() + "\" " + cv.getReason() + "\n" + apiMethod.name
                                            );
                                        }
                                    }
                                    
                                }
                            }
                        }
                    }

                }
            }
        };
    }

}
