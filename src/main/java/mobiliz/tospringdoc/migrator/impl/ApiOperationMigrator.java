package mobiliz.tospringdoc.migrator.impl;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import mobiliz.tospringdoc.core.Attributes;
import mobiliz.tospringdoc.migrator.AbstractAnnotationMigrator;
import mobiliz.tospringdoc.util.NodeUtils;
import mobiliz.tospringdoc.util.ResponseUtils;

public class ApiOperationMigrator extends AbstractAnnotationMigrator {

    private final ApiResponseMigrator apiResponseMigrator = new ApiResponseMigrator();

    @Override
    public void migrate(NormalAnnotationExpr expr) {
        replaceOrAddImport(expr, ApiOperation.class, Operation.class);
        expr.setName(Operation.class.getSimpleName());
        String response = null;
        String responseContainer = null;
        for (MemberValuePair pair : expr.getPairs()) {
            String name = pair.getName().asString();
            switch (name) {
                case Attributes.NICKNAME:
                    pair.setName(Attributes.OPERATIONID);
                    break;
                case Attributes.VALUE:
                    pair.setName(new SimpleName(Attributes.SUMMARY));
                    break;
                case Attributes.NOTES:
                    pair.setName(new SimpleName(Attributes.DESCRIPTION));
                    break;
                case Attributes.RESPONSE:
                    response = pair.getValue().toString();
                    break;
                case Attributes.RESPONSE_CONTAINER:
                    responseContainer = pair.getValue().asStringLiteralExpr().getValue();
                    break;
                case Attributes.AUTHORIZATIONS:
                    pair.setName(Attributes.SECURITY);
                    NormalAnnotationExpr expr1;
                    if(pair.getValue() instanceof ArrayInitializerExpr arrayInitializerExpr)
                        expr1 = arrayInitializerExpr.getValues().get(0).asNormalAnnotationExpr();
                    else
                        expr1 = pair.getValue().asNormalAnnotationExpr();
                    createSecurity(expr1);
                    break;
            }
        }
        if (response != null) {
            applyResponseOk(expr, response, responseContainer);
        }
        expr.getPairs().removeIf(pair -> Attributes.RESPONSE.equals(pair.getNameAsString()) || Attributes.RESPONSE_CONTAINER.equals(pair.getNameAsString()));
    }

    private void createSecurity(Expression value) {
        AuthorizationMigrator authorizationMigrator = new AuthorizationMigrator();
        authorizationMigrator.migrate((NormalAnnotationExpr) value);
    }

    @Override
    public void migrate(MarkerAnnotationExpr expr) {
        replaceOrAddImport(expr, ApiOperation.class, Operation.class);
        expr.setName(Operation.class.getSimpleName());
    }

    private void applyResponseOk(NormalAnnotationExpr expr, String response, String responseContainer) {
        NormalAnnotationExpr responseOkExpr = getResponseOkExpr(expr);
        apiResponseMigrator.migrate(responseOkExpr);
        if (NodeUtils.getPair(responseOkExpr, Attributes.RESPONSE_CODE) == null) {
            responseOkExpr.addPair(Attributes.RESPONSE_CODE, new StringLiteralExpr("200"));
        }
        NodeUtils.applyResponse(responseOkExpr, response, responseContainer);
    }


    private NormalAnnotationExpr getResponseOkExpr(NormalAnnotationExpr expr) {
        Node parentNode = expr.getParentNode().get();
        List<NormalAnnotationExpr> exprs = parentNode.findAll(NormalAnnotationExpr.class);
        NormalAnnotationExpr responseOk = null;
        for (NormalAnnotationExpr e : exprs) {
            if (e.getNameAsString().equals(ApiResponse.class.getSimpleName())) {
                NodeList<MemberValuePair> pairs = e.getPairs();
                if (pairs != null && !pairs.isEmpty()) {
                    for (MemberValuePair pair : pairs) {
                        if (pair.getNameAsString().equals(Attributes.CODE)) {
                            Integer responseCode = ResponseUtils.resolveResponseCode(pair.getValue().toString());
                            if (responseCode != null && responseCode >= 200 && responseCode < 400) {
                                responseOk = e;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (responseOk != null) {
            return responseOk;
        }
        return ((NodeWithAnnotations) parentNode).addAndGetAnnotation(ApiResponse.class);
    }
}
