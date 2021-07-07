package inspection;

import caveat.Caveat;
import caveat.CaveatViolation;
import caveat.DescriptionCheckingCaveat;
import collector.DescriptionCheckingCollection;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import domain.APIMethod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DescriptionCheckingInspection extends AbstractBaseJavaLocalInspectionTool {

    private DescriptionCheckingCollection rules = new DescriptionCheckingCollection();

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly){
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                //don't know why to super call
                super.visitMethodCallExpression(expression);
                //resolve the method
//                System.out.println(expression.getResolveScope());
                System.out.println("expression:"+expression);
//                System.out.println("reference"+expression.getFirstChild().getReference().getElement());
                PsiMethod method = expression.resolveMethod();
                if (method != null) {
                    String apiName = method.getName();
                    if (method.getContainingClass() != null) {
                        String className = method.getContainingClass().getQualifiedName();
//                        System.out.println("className:"+className);
//                        System.out.println("method:"+method.getText());
                        //acquire all the parameters from the method
                        PsiParameter[] parameters = method.getParameterList().getParameters();
//                        System.out.println("parameters:"+parameters);
                        Object[] paramTypes = new String[parameters.length];
                        String[] paramNames = new String[parameters.length];
                        apiName = className + "." + apiName + "(";
                        for (int i = 0; i < parameters.length; i++) {
                            apiName += parameters[i].getType().getPresentableText();
                            apiName += " ";
                            apiName += parameters[i].getName().toString();
                            apiName += ", ";
                        }
                        if (parameters.length != 0) {
                            apiName = apiName.substring(0, apiName.length() - 2);
                        }
                        apiName += ")";
                        for (int i = 0; i < parameters.length; i++) {
                            paramTypes[i] = parameters[i].getType().getPresentableText();
                            paramNames[i] = parameters[i].getName();
                        }
                        System.out.println("description className:" + className);
                        APIMethod apiMethod = rules.getApi(className, apiName);
//                        System.out.println("description apiName:" + apiName + "    " + apiMethod.caveats.size());
                        if (apiMethod != null) {
//                            System.out.println("description apiName:"+apiMethod.caveats.size());
                            for (Caveat c : apiMethod.caveats) {
                                if (c != null) {
                                    ArrayList<CaveatViolation> lst = ((DescriptionCheckingCaveat)c).checkViolation(className, apiName);
                                    System.out.println("handling------------------------");
                                    for (CaveatViolation cv : lst) {

                                        holder.registerProblem(expression,
                                                cv.getValue() + "---->" + cv.getName() + "\n" + cv.getReason() + "\n" + apiMethod.name);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void visitNewExpression(PsiNewExpression expression) {
                PsiJavaCodeReferenceElement classReference = expression.getClassReference();
                System.out.println(classReference);
                if (classReference != null) {
                    String className = classReference.getQualifiedName();
                    String apiName = classReference.getReferenceName();
                    System.out.println("expression:"+expression.getArgumentList().getExpressionTypes().toString());
                    System.out.println(className+"   "+apiName);
                    APIMethod apiMethod = rules.getApi(className, className);
                    System.out.println("apiMethod:"+apiMethod);
                    if (apiMethod != null) {
                        DescriptionCheckingCaveat caveat = (DescriptionCheckingCaveat) apiMethod.getCaveat(DescriptionCheckingCaveat.class);
                        if (caveat != null) {
//                            PsiExpression[] expressions = expression.getArgumentList().getExpressions();
//                            String[] args = new String[expressions.length];
//                            for (int i = 0; i < expressions.length; i++) args[i] = expressions[i].getText();
                            ArrayList<CaveatViolation> lst = caveat.checkViolation(className,className);
                            System.out.println(lst);
                            for (CaveatViolation cv : lst) {
                                holder.registerProblem(expression,
                                        cv.getValue() + "---->" + cv.getName() +"\n" + cv.getReason() +"\n" + apiMethod.name);
                            }
                        }
                    }
                }
            }
        };
    }

}
