package org.opencds.cqf.cql.data.fhir;

import org.opencds.cqf.cql.file.fhir.FileDataProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class TestFileDataProvider
{
    private FileDataProvider fileDataProvider = new FileDataProvider("src/test/resources/org/opencds/cqf/cql/data/data", "");

    @Test
    public void testPatientRetrieve()
    {
        List<Object> results = (List<Object>) fileDataProvider.retrieve(
                "Patient", "123", "Condition", null, null, null, null, null, null, null, null
        );

        Assert.assertTrue(results != null);
        Assert.assertTrue(results.size() == 1);

        results = (List<Object>) fileDataProvider.retrieve(
                "Patient", "987", "Condition", null, null, null, null, null, null, null, null
        );

        Assert.assertTrue(results != null);
        Assert.assertTrue(results.isEmpty());
    }

    @Test
    public void testPopulationRetrieve()
    {
        List<Object> results = (List<Object>) fileDataProvider.retrieve(
                "Population", null, "Condition", null, null, null, null, null, null, null, null
        );

        Assert.assertTrue(results != null);
        Assert.assertTrue(results.size() >= 2);
    }
}
