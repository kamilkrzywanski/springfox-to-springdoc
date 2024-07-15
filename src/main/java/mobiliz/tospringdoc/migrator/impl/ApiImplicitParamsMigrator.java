package mobiliz.tospringdoc.migrator.impl;

import com.github.javaparser.ast.expr.*;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.media.Schema;
import mobiliz.tospringdoc.migrator.AbstractAnnotationMigrator;

public class ApiImplicitParamsMigrator extends AbstractAnnotationMigrator {

    private final ApiParamMigrator apiResponseMigrator = new ApiParamMigrator();

    @Override
    public void migrate(NormalAnnotationExpr expr) {
        replaceOrAddImport(expr, ApiImplicitParams.class, io.swagger.v3.oas.annotations.Parameters.class);
        expr.setName(io.swagger.v3.oas.annotations.Parameters.class.getSimpleName());
        MemberValuePair valuePair = expr.getPairs().get(0);
        if (valuePair.getValue() instanceof NormalAnnotationExpr) {
            apiResponseMigrator.migrate((NormalAnnotationExpr) valuePair.getValue());
            return;
        }

        if (valuePair.getValue() instanceof ArrayInitializerExpr) {
            ArrayInitializerExpr responseAnnos = (ArrayInitializerExpr) valuePair.getValue();
            for (Expression respAnno : responseAnnos.getValues()) {
                apiResponseMigrator.migrate((NormalAnnotationExpr) respAnno);
            }
        }
    }

    @Override
    public void migrate(MarkerAnnotationExpr expr) {
        replaceOrAddImport(expr, ApiImplicitParams.class, io.swagger.v3.oas.annotations.Parameters.class);
    }
}
