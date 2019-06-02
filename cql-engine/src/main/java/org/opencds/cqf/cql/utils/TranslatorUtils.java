package org.opencds.cqf.cql.utils;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.opencds.cqf.cql.execution.CqlLibraryReader;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TranslatorUtils
{
    public static Library translateLibrary(File cqlFile, ModelManager modelManager,
                                           LibraryManager libraryManager, UcumService ucumService)
            throws UcumException
    {
        try {
            ArrayList<CqlTranslator.Options> options = new ArrayList<>();
            options.add(CqlTranslator.Options.EnableDateRangeOptimization);
            CqlTranslator translator = CqlTranslator.fromFile(cqlFile, modelManager, libraryManager, ucumService, options.toArray(new CqlTranslator.Options[options.size()]));

            if (translator.getErrors().size() > 0) {
                System.err.println("Translation failed due to errors:");
                ArrayList<String> errors = new ArrayList<>();
                for (CqlTranslatorException error : translator.getErrors()) {
                    TrackBack tb = error.getLocator();
                    String lines = tb == null ? "[n/a]" : String.format("[%d:%d, %d:%d]",
                            tb.getStartLine(), tb.getStartChar(), tb.getEndLine(), tb.getEndChar());
                    System.err.printf("%s %s%n", lines, error.getMessage());
                    errors.add(lines + error.getMessage());
                }
                throw new IllegalArgumentException(errors.toString());
            }

            assertThat(translator.getErrors().size(), is(0));

            File xmlFile = new File(cqlFile.getParent(), cqlFile.getName() + ".xml");

            String xml = translator.toXml();

            PrintWriter pw = new PrintWriter(xmlFile, "UTF-8");
            pw.println(xml);
            pw.println();
            pw.close();
            return CqlLibraryReader.read(xmlFile);
        }
        catch (IOException | JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException("Error encountered during translation: " + e.getMessage());
        }
    }

}
