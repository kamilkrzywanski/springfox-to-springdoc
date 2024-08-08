package mobiliz.tospringdoc.migrator.impl;

import com.github.javaparser.ast.expr.*;
import io.swagger.annotations.Authorization;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import mobiliz.tospringdoc.core.Attributes;
import mobiliz.tospringdoc.migrator.AbstractAnnotationMigrator;

import java.util.ArrayList;
import java.util.List;

public class AuthorizationMigrator extends AbstractAnnotationMigrator {

    @Override
    public void migrate(NormalAnnotationExpr expr) {
        replaceOrAddImport(expr, Authorization.class, io.swagger.v3.oas.annotations.security.SecurityRequirement.class);
        List<MemberValuePair> pairs = new ArrayList<>(expr.getPairs());
        expr.setName(SecurityRequirement.class.getSimpleName());

        expr.getPairs().clear();
        for (MemberValuePair pair : pairs) {
            switch (pair.getNameAsString()) {
                case Attributes.VALUE:
                    expr.addPair(Attributes.NAME, pair.getValue());
                    break;
                case Attributes.SCOPES:
                    expr.addPair("scopes", (createScopes((ArrayInitializerExpr)pair.getValue())));
                    break;
            }
        }
    }

    private Expression createScopes(ArrayInitializerExpr n){
        for (Expression expression : n.getValues()) {
            NormalAnnotationExpr annotationExpr = expression.asNormalAnnotationExpr();
            List<MemberValuePair> pairs = new ArrayList<>(annotationExpr.getPairs());
            ArrayInitializerExpr expr = new ArrayInitializerExpr();
            for (MemberValuePair pair : pairs) {
                if (pair.getNameAsString().equals(Attributes.SCOPE)) {
                    expr.getValues().add(pair.getValue());
                }
            }

            return expr;
        }
    return null;
    }

    @Override
    public void migrate(MarkerAnnotationExpr expr) {
        // useless case but developers choice
        replaceOrAddImport(expr, Authorization.class, io.swagger.v3.oas.annotations.security.SecurityRequirement.class);
    }
}
