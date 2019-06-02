package org.opencds.cqf.cql.execution;

import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.Library;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.opencds.cqf.cql.utils.TranslatorUtils;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public abstract class CqlExecutionTestBase
{
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
            File cqlFile = new File(URLDecoder.decode(CqlExecutionTestBase.class.getResource(fileName + ".cql").getFile(), "UTF-8"));

            library = TranslatorUtils.translateLibrary(cqlFile, modelManager, libraryManager, ucumService);
            libraries.put(fileName, library);
        }
    }
}
