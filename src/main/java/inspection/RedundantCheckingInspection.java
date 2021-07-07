package inspection;

import caveat.Caveat;
import caveat.RedundantCheckingCaveat;
import collector.RedundantCheckingCollection;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import domain.APIMethod;
import inspection.handler.ConditionHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class RedundantCheckingInspection extends AbstractBaseJavaLocalInspectionTool {

    private RedundantCheckingCollection rule = new RedundantCheckingCollection();

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly){
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression){
                PsiMethod method = null;
                APIMethod apiMethod = null;
                PsiVariable variable =null;

                ArrayList<PsiBinaryExpression> expressionToCheck = new ArrayList<>();
                try {
                    method = expression.resolveMethod();
                } catch (NullPointerException e){
                    holder.registerProblem(expression,"There is no such p");
                }

                if(method != null){
                    apiMethod = rule.getApi(method.getContainingClass().getQualifiedName(),method.getName());
                    PsiAssignmentExpression assignExpression = PsiTreeUtil.getParentOfType(expression,PsiAssignmentExpression.class);
                    if(PsiTreeUtil.getParentOfType(expression,PsiVariable.class)!=null){
                        variable = PsiTreeUtil.getParentOfType(expression,PsiVariable.class);
                    }
                    if(assignExpression != null){
                        variable = (PsiVariable) assignExpression.getLExpression().getReference().resolve();
                    }
                    if(variable != null){
                        Query<PsiReference> query = ReferencesSearch.search(variable);
                        Collection<PsiReference> list = query.findAll();
                        if(!list.isEmpty()){
                            for (PsiReference reference : list){
                                if(PsiTreeUtil.getParentOfType(reference.getElement(),PsiBinaryExpression.class)!=null){
                                    PsiBinaryExpression be = PsiTreeUtil.getParentOfType(reference.getElement(),PsiBinaryExpression.class);
                                    PsiIfStatement is = PsiTreeUtil.getParentOfType(reference.getElement(),PsiIfStatement.class);
                                    if(is != null && PsiTreeUtil.isAncestor(is.getCondition(),be,false)){
                                        expressionToCheck.add(be);
                                    }
                                }
                            }
                        }
                    }
                }
                if(apiMethod != null){
                    for(Caveat ca : apiMethod.caveats){
                        RedundantCheckingCaveat caveat = (RedundantCheckingCaveat) ca;
                        for(PsiBinaryExpression ex : expressionToCheck){
                            String text;
                            try {
                                text = ex.getROperand().getText();
                            } catch (NullPointerException e){
                                text = "";
                            }
                            if(!(ex.getOperationTokenType().toString().equals("NE") && text.equals(caveat.rule.end.name)))
                                continue;
                            PsiIfStatement is = PsiTreeUtil.getParentOfType(ex,PsiIfStatement.class);
                            Context context = new Context();
                            Expr expr = ConditionHandler.solver(is.getCondition(),context);
                            expr = context.mkAnd(expr,context.mkEq(context.mkBoolConst(ex.getLOperand().getText()),context.mkFalse()));
                            Solver s = context.mkSolver();
                            System.out.println(expr);
                            s.add(expr);
                            if(s.check() == Status.SATISFIABLE)
                                holder.registerProblem(ex,caveat.rule.description);
                        }
                    }
                }
            }
        };
    }
}
