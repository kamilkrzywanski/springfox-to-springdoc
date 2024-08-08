package mobiliz.tospringdoc.migrator.impl;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import mobiliz.tospringdoc.core.Attributes;
import mobiliz.tospringdoc.migrator.AbstractAnnotationMigrator;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractApiParamMigrator extends AbstractAnnotationMigrator {

    final BinaryExpressionVisitor printer = new BinaryExpressionVisitor();

    protected abstract Class<? extends Annotation> getFoxAnnotation();

    @Override
    public void migrate(NormalAnnotationExpr expr) {
        NodeList<MemberValuePair> newPairs = new NodeList<>();
        for (MemberValuePair pair : expr.getPairs()) {
            switch (pair.getNameAsString()) {
                case Attributes.NAME:
                case Attributes.REQUIRED:
                case Attributes.EXAMPLE:
                case Attributes.ALLOW_EMPTY_VALUE:
                    newPairs.add(pair);
                    break;
                case Attributes.VALUE:
                    newPairs.add(new MemberValuePair(Attributes.DESCRIPTION, pair.getValue()));
                    break;
                case Attributes.ALLOWABLE_VALUES:
                      ;
                    newPairs.stream().filter(mvp -> mvp.getName().asString().equals(Attributes.SCHEMA)).findFirst()
                                    .ifPresentOrElse(existingPair -> ((NormalAnnotationExpr)existingPair.getValue()).addPair(
                                            Attributes.ALLOWABLE_VALUES , createNodeArrayFromAllowableValues(
                                           extractAllowableValues(pair))
                                            )
                                    ,() ->  newPairs.add(
                                                    new MemberValuePair(Attributes.SCHEMA,
                                            createSchemaExpr(createNodeArrayFromAllowableValues(extractAllowableValues(pair)),
                                                    Attributes.ALLOWABLE_VALUES)))
                                    );
                    break;

                case Attributes.PARAM_TYPE:
                    String paramType = pair.getValue().asStringLiteralExpr().getValue();
                    expr.tryAddImportToParentCompilationUnit(ParameterIn.class);
                    newPairs.add(new MemberValuePair(Attributes.IN, createInExpr(paramType)));
                    break;
                default:
                    System.out.printf("@ApiImplicitParam::%s property cannot be migrated%n", pair.getNameAsString());
            }
        }
        expr.setPairs(newPairs);
        replaceAnnotation(expr);
    }

    private String extractAllowableValues(MemberValuePair pair){
      return pair.getValue() instanceof StringLiteralExpr? pair.getValue().asStringLiteralExpr().asString() :
                printer.visit((BinaryExpr) pair.getValue());
    }

    public static NormalAnnotationExpr createSchemaExpr(Expression expression, String attribute) {
        NormalAnnotationExpr expr = new NormalAnnotationExpr();
        expr.setName(Schema.class.getSimpleName());
        expr.addPair(attribute, expression);
        return expr;
    }

    private static ArrayInitializerExpr createNodeArrayFromAllowableValues(String allowableValues){
        NodeList list = new NodeList();
        list.addAll(Arrays.stream(allowableValues.split(",")).map(String::trim).map(StringLiteralExpr::new).toList());
        return new ArrayInitializerExpr(list);
    }

    @Override
    public void migrate(MarkerAnnotationExpr expr) {
        replaceAnnotation(expr);
    }

    private void replaceAnnotation(AnnotationExpr expr) {
        expr.setName(Parameter.class.getSimpleName());
        replaceOrAddImport(expr, getFoxAnnotation(), Parameter.class);
    }

    private FieldAccessExpr createInExpr(String paramTye) {
        ParameterIn in;
        switch (paramTye) {
            case "path":
                in = ParameterIn.PATH;
                break;
            case "query":
                in = ParameterIn.QUERY;
                break;
            case "header":
                in = ParameterIn.HEADER;
                break;
            default:
                throw new RuntimeException(String.format("%s is not valid or supported for paramType", paramTye));
        }
        FieldAccessExpr expr = new FieldAccessExpr();
        expr.setScope(new NameExpr(ParameterIn.class.getSimpleName()));
        expr.setName(in.name());
        return expr;
    }


    private static class BinaryExpressionVisitor {
        public String visit(BinaryExpr binaryExpr) {
            String concatenatedString = "";
            if (binaryExpr.getOperator() == BinaryExpr.Operator.PLUS) {
                Expression left = binaryExpr.getLeft();
                Expression right = binaryExpr.getRight();

                // Check if both sides are string literals
                if (left.isStringLiteralExpr() && right.isStringLiteralExpr()) {
                    StringLiteralExpr leftString = (StringLiteralExpr) left;
                    StringLiteralExpr rightString = (StringLiteralExpr) right;

                    // Concatenate the string values
                    concatenatedString = leftString.getValue() + rightString.getValue();

                    // Replace the binary expression with the concatenated string
                    binaryExpr.replace(new StringLiteralExpr(concatenatedString));
                }
            }

            return concatenatedString;
        }
    }
    private static final Map<String, String> typeMap = new HashMap<>();

    static {
        typeMap.put("string", String.class.getSimpleName());
        typeMap.put("int", Integer.class.getSimpleName());
        typeMap.put("integer", Integer.class.getSimpleName());
        typeMap.put("long", Long.class.getSimpleName());
        typeMap.put("double", Double.class.getSimpleName());
        typeMap.put("float", Float.class.getSimpleName());
        typeMap.put("boolean", Boolean.class.getSimpleName());
        typeMap.put("char", Character.class.getSimpleName());
        typeMap.put("character", Character.class.getSimpleName());
        // Add more mappings as needed
    }

    public static String mapStringToClass(String typeName) {
        typeName = typeName.replaceAll("\"", "");

        return typeMap.getOrDefault(typeName.toLowerCase(), typeName);
    }

}
