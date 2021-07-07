package inspection;

import caveat.Caveat;
import caveat.TriggerCaveat;
import collector.TriggerCollection;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.ui.DocumentAdapter;
import domain.APIMethod;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import javax.swing.event.DocumentEvent;

public class TriggerInspection extends AbstractBaseJavaLocalInspectionTool {

    private TriggerCollection rules = new TriggerCollection();

    @SuppressWarnings({"WeakerAccess"})
    @NonNls
    public String CHECKED_CLASSES = "true;false";

    @Override
    public JComponent createOptionsPanel(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JTextField checkedClasses = new JTextField(CHECKED_CLASSES);
        checkedClasses.getDocument().addDocumentListener(new DocumentAdapter() {
            public void textChanged(@NotNull DocumentEvent event) {
                CHECKED_CLASSES = checkedClasses.getText();
            }
        });
        panel.add(checkedClasses);
        return panel;
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly){
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression){
                PsiMethod method = null;
                APIMethod apiMethod = null;
                ArrayList<String> violations = new ArrayList<>();

                try{
                    method = expression.resolveMethod();
                    apiMethod = rules.getApi(method.getContainingClass().getQualifiedName(),method.getName());
                }catch (NullPointerException e){
                    holder.registerProblem(expression,"There is no such p");
                }
                if(apiMethod != null){
                    for(Caveat ca : apiMethod.caveats){
                        TriggerCaveat caveat = (TriggerCaveat) ca;
                        violations.add(caveat.checkViolation(expression));
                    }
                }

                for(String s: violations){
                    if(s != null)
                        holder.registerProblem(expression,s);
                }
            }
        };
    }
}
