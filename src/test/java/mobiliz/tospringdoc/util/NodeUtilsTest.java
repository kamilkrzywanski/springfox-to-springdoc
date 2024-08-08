package mobiliz.tospringdoc.util;

import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import mobiliz.tospringdoc.core.Attributes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NodeUtilsTest {

    @Test
    public void getsPairByName() {
        NormalAnnotationExpr expr = new NormalAnnotationExpr();
        expr.addPair(Attributes.NAME, new StringLiteralExpr("hede"));
        expr.addPair(Attributes.RESPONSE_CODE, new StringLiteralExpr("200"));
        MemberValuePair pair = NodeUtils.getPair(expr, Attributes.RESPONSE_CODE);
        Assertions.assertEquals(Attributes.RESPONSE_CODE, pair.getNameAsString());
        Assertions.assertEquals("200", pair.getValue().asStringLiteralExpr().getValue());
    }

    @Test
    public void returnsNullIfPairDoesNotExist() {
        Assertions.assertNull(NodeUtils.getPair(new NormalAnnotationExpr(), Attributes.NAME));
    }
}
