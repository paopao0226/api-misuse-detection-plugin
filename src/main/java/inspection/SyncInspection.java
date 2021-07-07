package inspection;

import caveat.Caveat;
import caveat.SyncCaveat;
import collector.SyncCollection;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import domain.APIMethod;
import inspection.handler.ConditionHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SyncInspection extends AbstractBaseJavaLocalInspectionTool {

    private SyncCollection rules = new SyncCollection();

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly){
        return new JavaElementVisitor() {
            @Override
            public void visitVariable(PsiVariable variable) {
                APIMethod method;
                try {
                    String className = variable.getType().getCanonicalText();
                    className = ConditionHandler.genericErase(className);
                    String name = variable.getType().getPresentableText();
                    name = ConditionHandler.genericErase(name);
                    method = rules.getApi(className,name);
                } catch (NullPointerException e){
                    method = null;
                }
                if(method != null){
                    ArrayList<String> violations = new ArrayList<>();
                    for(Caveat ca: method.caveats){
                        SyncCaveat caveat = (SyncCaveat) ca;
                        violations.add(caveat.checkViolation(variable));
                    }
                    for(String s: violations){
                        if(s != null)
                            holder.registerProblem(variable,s);
                    }

                }
            }
        };
    }
}
