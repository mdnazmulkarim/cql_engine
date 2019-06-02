package org.opencds.cqf.cql.data.fhir;

import org.opencds.cqf.cql.execution.Context;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestFhirExecution extends FhirExecutionTestBase
{
    @Test
    public void testAgeInYearsAt()
    {
        Context context = new Context(library);
        context.registerDataProvider("http://hl7.org/fhir", fileDataProvider);
        context.enterContext("Patient");
        context.setContextValue("Patient", "123");
        Object result = context.resolveExpressionRef("Test AgeInYears").getExpression().evaluate(context);
        Assert.assertTrue(result != null);
    }
}
