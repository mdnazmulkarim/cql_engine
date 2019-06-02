package org.opencds.cqf.cql.data.fhir;

import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.Library;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.opencds.cqf.cql.file.fhir.FileDataProvider;
import org.opencds.cqf.cql.utils.TranslatorUtils;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public abstract class FhirExecutionTestBase
{
    BaseFhirDataProvider dstu2Provider = new FhirDataProviderDstu2().setEndpoint("http://fhirtest.uhn.ca/baseDstu2");
    BaseFhirDataProvider dstu3Provider = new FhirDataProviderStu3().setEndpoint("http://measure.eval.kanvix.com/cqf-ruler/baseDstu3");
//    BaseFhirDataProvider dstu3Provider = new FhirDataProviderStu3().setEndpoint("http://localhost:8080/cqf-ruler/baseDstu3");
    BaseFhirDataProvider hl7Provider = new FhirDataProviderHL7().setEndpoint("http://fhirtest.uhn.ca/baseDstu2");

    FileDataProvider fileDataProvider = new FileDataProvider("src/test/resources/org/opencds/cqf/cql/data/data", "");

    private static Map<String, Library> libraries = new HashMap<>();
    Library library = null;

    @BeforeMethod
    public void beforeEachTestMethod()
            throws IOException, UcumException
    {
        String fileName = this.getClass().getSimpleName();
        library = libraries.get(fileName);

        if (library == null)
        {
            ModelManager modelManager = new ModelManager();
            LibraryManager libraryManager = new LibraryManager(modelManager);
            UcumService ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
            File cqlFile = new File(URLDecoder.decode(FhirExecutionTestBase.class.getResource(fileName + ".cql").getFile(), "UTF-8"));

            library = TranslatorUtils.translateLibrary(cqlFile, modelManager, libraryManager, ucumService);
            libraries.put(fileName, library);
        }
    }
}
