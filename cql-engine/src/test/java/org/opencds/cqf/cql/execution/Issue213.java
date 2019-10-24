package org.opencds.cqf.cql.execution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.opencds.cqf.cql.elm.execution.EquivalentEvaluator;
import org.opencds.cqf.cql.runtime.Date;
import org.opencds.cqf.cql.runtime.Interval;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Issue213 extends CqlExecutionTestBase {
    @Test
    public void testInterval() {
        Context context = new Context(library);
        Object result = context.resolveExpressionRef("Collapsed Treatment Intervals").getExpression().evaluate(context);
        assertThat(((List)result).size(), is(2));
        Assert.assertTrue(EquivalentEvaluator.equivalent(((Interval)((List)result).get(0)).getStart(), new Date(2018, 1, 1)));
        Assert.assertTrue(EquivalentEvaluator.equivalent(((Interval)((List)result).get(0)).getEnd(), new Date(2018, 8, 28)));
        Assert.assertTrue(EquivalentEvaluator.equivalent(((Interval)((List)result).get(1)).getStart(), new Date(2018, 8, 30)));
        Assert.assertTrue(EquivalentEvaluator.equivalent(((Interval)((List)result).get(1)).getEnd(), new Date(2018, 10, 15)));
    }

    public void testMeetingInterval() {
        Context context = new Context(library);
        Object result = context.resolveExpressionRef("Collapsed Meeting Treatment Intervals").getExpression().evaluate(context);
        assertThat(((List)result).size(), is(1));
        Assert.assertTrue(EquivalentEvaluator.equivalent(((Interval)((List)result).get(0)).getStart(), new Date(2018, 1, 1)));
        Assert.assertTrue(EquivalentEvaluator.equivalent(((Interval)((List)result).get(0)).getEnd(), new Date(2018, 10, 15)));
    }

    public void testOverlappingInterval() {
        Context context = new Context(library);
        Object result = context.resolveExpressionRef("Collapsed Overlapping Treatment Intervals").getExpression().evaluate(context);
        assertThat(((List)result).size(), is(1));
        Assert.assertTrue(EquivalentEvaluator.equivalent(((Interval)((List)result).get(0)).getStart(), new Date(2018, 1, 1)));
        Assert.assertTrue(EquivalentEvaluator.equivalent(((Interval)((List)result).get(0)).getEnd(), new Date(2018, 10, 15)));
    }
}
