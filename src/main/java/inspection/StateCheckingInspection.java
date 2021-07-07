package inspection;

import caveat.Caveat;
import caveat.StateCheckingCaveat;
import collector.StateCheckingCollection;
import com.intellij.codeInspection.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import com.microsoft.z3.*;
import domain.APIClass;
import domain.APIMethod;
import inspection.handler.ConditionHandler;
import org.jetbrains.annotations.NotNull;
import rule.StateCheckingRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static inspection.handler.ConditionHandler.solver;


public class StateCheckingInspection extends AbstractBaseJavaLocalInspectionTool {
    private static final Logger LOG = Logger.getInstance("#com.intellij.codeInspection.StateCheckingInspection");
    private StateCheckingCollection collection = new StateCheckingCollection();

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor(){
            @Override
            public void visitVariable(PsiVariable variable){

            }

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression){
                PsiMethod method = null;
                try {
                    method = expression.resolveMethod();
                } catch (NullPointerException e){
                    holder.registerProblem(expression,"There is no such p");
                }
                if(method != null){
                    APIClass apiClass = collection.getAPI(method.getContainingClass().getQualifiedName());
                    APIMethod apiMethod = null;
                    if(apiClass != null){
                        apiMethod = apiClass.getMethod(method.getName());
                    }
                    if(apiMethod != null){
                        ArrayList<String> violations = new ArrayList<>();
                        for(Caveat oldCaveat : apiMethod.caveats){
                            StateCheckingCaveat caveat = ((StateCheckingCaveat) oldCaveat);
                            StateCheckingRule rule = caveat.rule;
                            if(rule.start.label.equals("method")&& rule.end.label.equals("variable")){
                                PsiVariable referVariable = PsiTreeUtil.getParentOfType(expression,PsiVariable.class);
                                if(referVariable != null){
                                    Query<PsiReference> query = ReferencesSearch.search(referVariable);
                                    Collection<PsiReference> list = query.findAll();
                                    ArrayList<PsiReference> checkRefer  = new ArrayList<>();
                                    ArrayList<PsiReference> callRefer = new ArrayList<>();
                                    if(!list.isEmpty()){
                                        for (PsiReference reference : list){
                                            if(reference.getElement().getParent() instanceof PsiAssignmentExpression)
                                                break;
                                            if(reference.getElement().getParent() instanceof PsiBinaryExpression)
                                                checkRefer.add(reference);
                                            else
                                                callRefer.add(reference);
                                        }
                                    }
                                    for(PsiReference callReference : callRefer){
                                        PsiIfStatement statement = null;
                                        for(PsiReference checkReference : checkRefer){
                                            PsiIfStatement ifStatement  = PsiTreeUtil.getParentOfType(checkReference.getElement(),PsiIfStatement.class);
                                            if(PsiTreeUtil.isAncestor(ifStatement,callReference.getElement(),false))
                                                statement = ifStatement;
                                        }
                                        if(statement == null){
                                            holder.registerProblem(callReference.getElement(),rule.violation+":"+rule.description);
                                        } else {
                                            Context context = new Context();
                                            Solver s = context.mkSolver();
                                            Expr expr = solver(statement.getCondition(),context);
                                            Expr trueCase = context.mkAnd(expr,context.mkEq(context.mkBoolConst(callReference.getElement().getText()),context.mkTrue()));
                                            s.add(trueCase);
                                            if(s.check() == Status.SATISFIABLE){
                                                if(caveat.checkViolation("true") != null)
                                                    holder.registerProblem(callReference.getElement(),rule.violation+":"+rule.description);
                                            }
                                            s.reset();
                                            Expr falseCase = context.mkAnd(expr,context.mkEq(context.mkBoolConst(callReference.getElement().getText()),context.mkFalse()));
                                            s.add(falseCase);
                                            if(s.check() == Status.SATISFIABLE){
                                                if(caveat.checkViolation("false") != null)
                                                    holder.registerProblem(callReference.getElement(),rule.violation+":"+rule.description);
                                            }
                                        }
                                    }
                                }
                            }
                            if(rule.start.label.equals("method")&& rule.end.label.equals("method")){
                                PsiVariable variable;
                                PsiVariable referVariable = null;
                                if(expression.getMethodExpression().getQualifier().getReference() != null)
                                    variable = (PsiVariable) expression.getMethodExpression().getQualifier().getReference().resolve();
                                else{
                                    variable = (PsiVariable) expression.getMethodExpression().getQualifier().getFirstChild().getFirstChild().getReference().resolve();
                                    referVariable = variable;
                                }

                                if(rule.end.className.equals(rule.start.className)){
                                    referVariable = variable;
                                }else if(referVariable == null){
                                    if(variable.getInitializer() == null){
                                        Query<PsiReference> query = ReferencesSearch.search(variable);
                                        Collection<PsiReference> list = query.findAll();
                                        for(PsiReference r:list){
                                            if(r.getElement().getParent() instanceof PsiAssignmentExpression){
                                                PsiMethodCallExpression e  =(PsiMethodCallExpression) ((PsiAssignmentExpression) r.getElement().getParent()).getRExpression();
                                                referVariable = (PsiVariable) e.getMethodExpression().getQualifier().getReference().resolve();
                                            }
                                        }
                                    }else{
                                        PsiMethodCallExpression e = (PsiMethodCallExpression)variable.getInitializer();
                                        referVariable = (PsiVariable) e.getMethodExpression().getQualifier().getReference().resolve();
                                    }
                                }
                                boolean isContained = false;
                                Query<PsiReference> query = ReferencesSearch.search(Objects.requireNonNull(referVariable));
                                Collection<PsiReference> list = query.findAll();
                                for(PsiReference refer : list){
                                    PsiMethodCallExpression call = null;
                                    String name = "0";
                                    if(refer.getElement().getParent().getParent() instanceof  PsiMethodCallExpression){
                                        call = (PsiMethodCallExpression)refer.getElement().getParent().getParent();
                                        if(call.getParent().getParent() instanceof PsiMethodCallExpression){
                                            call = (PsiMethodCallExpression) call.getParent().getParent();
                                        }
                                        PsiMethod psiMethod = call.resolveMethod();
                                        name = psiMethod.getName();
                                    }
                                    if(name.equals(rule.end.methodName)){
                                        Context context = new Context();
                                        PsiWhileStatement  ws = PsiTreeUtil.getParentOfType(call,PsiWhileStatement.class);
                                        PsiIfStatement is = PsiTreeUtil.getParentOfType(call,PsiIfStatement.class);
                                        if(is != null &&(ws == null || PsiTreeUtil.isAncestor(ws, is,false))){
                                            if(is.getElseBranch() != null){
                                                if(PsiTreeUtil.isAncestor(is.getElseBranch(),expression,false)){
                                                    isSatisfied(is.getCondition(),context,call,violations,caveat,true);
                                                    isContained = true;
                                                    break;
                                                }
                                            }
                                            if(is.getThenBranch() != null){
                                                if(PsiTreeUtil.isAncestor(is.getThenBranch(), expression,false)){
                                                    isSatisfied(is.getCondition(),context,call,violations,caveat,false);
                                                    isContained = true;
                                                    break;
                                                }
                                            }
                                        }else if(ws != null &&(is == null || PsiTreeUtil.isAncestor(is, ws,false))){
                                            if(PsiTreeUtil.isAncestor(ws,expression,false)){
                                                isSatisfied(ws.getCondition(),context,call,violations,caveat,false);
                                                isContained = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                for(String s: violations){
                                    if(s != null)
                                        holder.registerProblem(expression,s);
                                }
                                if(!isContained){
                                    String expected = rule.expected_state;
                                    String description = method.getName()+" should have state check "+ rule.end.methodName+",and expected "+expected;
                                    holder.registerProblem(expression,description);

                                }

                            }

                        }
                    }
                }

            }

        };
    }

    private void isSatisfied(PsiExpression expression, Context context,PsiMethodCallExpression call,
                                 ArrayList<String> violations, StateCheckingCaveat caveat, boolean isElse){
        Expr expr = ConditionHandler.solver(expression, context);
        if(isElse){
            expr = context.mkNot(expr);
        }
        Solver s = context.mkSolver();
        s.add(context.mkAnd(expr,context.mkEq(context.mkBoolConst(call.getText()),context.mkTrue())));
        Status t = s.check();
        s.reset();
        s.add(context.mkAnd(expr,context.mkEq(context.mkBoolConst(call.getText()),context.mkFalse())));
        Status f = s.check();
        if(t == Status.SATISFIABLE)
            violations.add(caveat.checkViolation("true"));
        if(f == Status.SATISFIABLE)
            violations.add(caveat.checkViolation("false"));
    }

}
