package edu.cwru.rise.hyperservice;

import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

import edu.cwru.rise.hslang.HSlangBaseVisitor;
import edu.cwru.rise.hslang.HSlangParser;

public class TypeVisitor extends HSlangBaseVisitor<String> {

    class Field {
        String name;
        String type;
    }

    class Type {
        String name;
        List<Field> fields = new ArrayList<>();

        @Override
        public String toString() {

            System.out.println("struct " + name + " {");
            for (Field f: fields
                 ) {
                System.out.println("\t " + f.type + " " + f.name);
            }
            System.out.println("}");

            return super.toString();
        }
    }

    class Parameter{
        StringBuilder res = new StringBuilder();
        List<Field> fields = new ArrayList<>();

        @Override
        public String toString() {
            for(int i = 0; i < fields.size(); i++){
                Field tmp = fields.get(i);
                if(i < fields.size()-1) {
                    res.append(tmp.type + " " + tmp.name + ", ");
                }
                else{
                    res.append(tmp.type + " " + tmp.name);
                }
            }
            return res.toString();
        }
    }

    class Function{
        String name;
        String parameters;
        String result;
        List<Statement> stats = new ArrayList<>();

        @Override
        public String toString() {
            System.out.println("func " + name + "(" + parameters + ") " + result + "{");
            for(Statement s: stats){
                System.out.println("\t " + s.text + ";");
            }
            System.out.println("}");
            return super.toString();
        }
    }

    class Statement{
        String text;
    }

    @Override
    public String visitFunctionDecl(HSlangParser.FunctionDeclContext ctx) {
        System.out.println("func: " + ctx.getText());
        Function newFunc = new Function();

        newFunc.name = ctx.IDENTIFIER().getText();

        HSlangParser.FunctionContext funcspec = ctx.function();
        Parameter parameter = new Parameter();
        HSlangParser.SignatureContext sigPar =funcspec.signature();
        for (HSlangParser.ParameterDeclContext context : sigPar.parameters().parameterList().parameterDecl()){
            Field f = new Field();
            f.name = context.identifierList().getText();
            f.type = context.type().getText();
            parameter.fields.add(f);
        }

        newFunc.parameters = parameter.toString();
        newFunc.result = sigPar.result().getText();


        HSlangParser.StatementListContext statements= funcspec.block().statementList();
        for(HSlangParser.StatementContext stats : statements.statement()){
            Statement tmp = new Statement();

            if(stats.simpleStmt() != null){
                tmp.text = stats.getText();
                newFunc.stats.add(tmp);
            }

            if(stats.returnStmt() != null){
                String tmps = "return ";
                for(HSlangParser.ExpressionContext expressionContext: stats.returnStmt().expressionList().expression()){
                    tmps = tmps+ expressions(expressionContext);
                }
                tmp.text = tmps;
                newFunc.stats.add(tmp);
            }
        }

        funcs.add(newFunc);
        return super.visitFunctionDecl(ctx);
    }

    List<Type> types = new ArrayList<>();
    List<Function> funcs = new ArrayList<>();

    private String expressions(HSlangParser.ExpressionContext ctx){
        StringBuilder res = new StringBuilder();

        if(ctx.unaryExpr() != null){
            res.append(unaryExprContext(ctx.unaryExpr()));
        }
        else{
            if(ctx.expression().size() != 0){
                for(HSlangParser.ExpressionContext tmp : ctx.expression()){
                    res.append(expressions(tmp));
                }
            }
        }
        return res.toString();
    }

    private String unaryExprContext(HSlangParser.UnaryExprContext ctx){
        StringBuilder res = new StringBuilder();

        if(ctx.primaryExpr() != null){
            res.append(primaryExprContext(ctx.primaryExpr()));
        }
        else{
            if(ctx.unaryExpr() != null){
                res.append(ctx.unaryExpr());
            }
        }

        return res.toString();
    }

    private String primaryExprContext(HSlangParser.PrimaryExprContext ctx){
        StringBuilder res = new StringBuilder();
        if(ctx.operand() != null){
            if(ctx.operand().operandName() != null){
                return res.append(ctx.operand().operandName().getText()).toString();
            }
        }
        return res.toString();
    }

    @Override
    public String visitSourceFile(HSlangParser.SourceFileContext ctx) {
        return super.visitSourceFile(ctx);
    }

    @Override
    public String visitTypeDecl(HSlangParser.TypeDeclContext ctx) {

        System.out.println("type:" + ctx.getText());

        Type newtype = new Type();

        for (HSlangParser.TypeSpecContext typespec : ctx.typeSpec()) {
            System.out.println("name: " + typespec.IDENTIFIER());
            newtype.name = typespec.IDENTIFIER().getText();
            HSlangParser.StructTypeContext structype = typespec.type().typeLit().structType();
            for (HSlangParser.FieldDeclContext field : structype.fieldDecl()) {
                Field f = new Field();
                for (TerminalNode idname : field.identifierList().IDENTIFIER()) {
                    f.name = idname.getText();
                }
                f.type = field.type().typeName().getText();
                newtype.fields.add(f);
            }
        }

        types.add(newtype);
        return super.visitTypeDecl(ctx);
    }
}
