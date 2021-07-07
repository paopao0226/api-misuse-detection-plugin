package inspection.handler;

import com.intellij.psi.*;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

public class ConditionHandler {
    public static Expr solver(PsiExpression expression, Context context){
        if(expression instanceof PsiMethodCallExpression){
            return context.mkBoolConst(expression.getText());
        }
        if(expression instanceof PsiBinaryExpression){
            PsiExpression left = ((PsiBinaryExpression) expression).getLOperand();
            PsiExpression right = ((PsiBinaryExpression) expression).getROperand();
            PsiJavaToken sign = ((PsiBinaryExpression) expression).getOperationSign();
            Expr exprLeft = solver(left,context);
            Expr exprRight = solver(right,context);
            if(exprLeft == null || exprRight == null)
                return context.mkTrue();
            if(sign.getTokenType().toString().equals("ANDAND"))
                return context.mkAnd(exprLeft,exprRight);
            if (sign.getTokenType().toString().equals("OROR"))
                return context.mkOr(exprLeft,exprRight);
            if(sign.getTokenType().toString().equals("EQEQ"))
                return context.mkEq(exprLeft,exprRight);
            if(sign.getTokenType().toString().equals("GE"))
                return context.mkGe(exprLeft,exprRight);
            if(sign.getTokenType().toString().equals("LE"))
                return context.mkLe(exprLeft,exprRight);
            if(sign.getTokenType().toString().equals("GT"))
                return context.mkGt(exprLeft,exprRight);
            if(sign.getTokenType().toString().equals("LT"))
                return context.mkLt(exprLeft,exprRight);
            if(sign.getTokenType().toString().equals("NE"))
                return context.mkNot(context.mkEq(exprLeft,exprRight));

        }
        if(expression instanceof PsiPrefixExpression){
            PsiJavaToken sign = ((PsiPrefixExpression) expression).getOperationSign();
            PsiExpression newExpression = ((PsiPrefixExpression) expression).getOperand();
            Expr expr = solver(newExpression,context);
            if(sign.getTokenType().toString().equals("EXCL"))
                return context.mkNot(expr);
        }
        if(expression instanceof PsiParenthesizedExpression)
            return solver(((PsiParenthesizedExpression) expression).getExpression(),context);
        if(expression instanceof  PsiReferenceExpression){
            try {
                PsiVariable v = (PsiVariable) expression.getReference().resolve();
                if(v.getTypeElement().getText().equals("int"))
                    return context.mkIntConst(expression.getText());
                else
                    return context.mkBoolConst(expression.getText());
            } catch (NullPointerException e){
                   return null;
            }
        }

        if (expression instanceof PsiLiteralExpression){
            if((expression.getText().equals("true")))
                return context.mkTrue();
            else if (expression.getText().equals("false"))
                return context.mkFalse();
            else if (expression.getText().equals("null"))
                return context.mkTrue();
            else
                return context.mkInt(Integer.valueOf(expression.getText()));
        }
        return null;
    }

    public static String genericErase(String proto){
        if(proto.contains("<")){
            return proto.split("<")[0];
        }
        return proto;
    }
}
