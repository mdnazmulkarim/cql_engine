package org.opencds.cqf.cql.file.fhir;

import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderStu3;
import org.opencds.cqf.cql.runtime.Code;
import org.opencds.cqf.cql.runtime.Interval;
import org.opencds.cqf.cql.terminology.ValueSetInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileDataProvider extends FhirDataProviderStu3
{
    private Path pathToData;
    private Path pathToTerminology;

    private IParser jsonParser = super.fhirContext.newJsonParser();
    private IParser xmlParser = super.fhirContext.newXmlParser();

    public FileDataProvider(String pathToData, String pathToTerminology)
    {
        super();
        this.pathToData = Paths.get(pathToData);
        this.pathToTerminology = Paths.get(pathToTerminology);
    }

    @Override
    public Iterable<Object> retrieve(String context, Object contextValue, String dataType, String templateId,
                                     String codePath, Iterable<Code> codes, String valueSet, String datePath,
                                     String dateLowPath, String dateHighPath, Interval dateRange)
    {
        List<Object> results = new ArrayList<>();

        if (dataType == null)
        {
            throw new RuntimeException("Cannot perform retrieve with undefined data type.");
        }

        if (context == null)
        {
            // default to Patient
            // TODO: this will not be the case in CQL 1.4 as the data model will determine the default context
            context = "Patient";
        }

        if (context.equals("Patient") && contextValue == null)
        {
            return null;
        }

        List<Object> unfilteredResults;
        if (context.equals("Patient"))
        {
            Path pathToPatient;
            try
            {
                pathToPatient = pathToData.resolve(contextValue.toString().toLowerCase());
                if (!Files.exists(pathToPatient))
                {
                    // TODO: could make an argument to return null instead of an exception here...
                    throw new RuntimeException("Unable to resolve Patient directory: " + pathToPatient + "/" + contextValue.toString().toLowerCase());
                }
            }
            catch (InvalidPathException ipe)
            {
                // TODO: could make an argument to return null instead of an exception here...
                throw new RuntimeException("Unable to resolve Patient directory: " + pathToData + "/" + contextValue.toString().toLowerCase());
            }
            try
            {
                pathToPatient = pathToPatient.resolve(dataType.toLowerCase());
                if (!Files.exists(pathToPatient))
                {
                    // if a directory with the data type name cannot be found, return empty list
                    return results;
                }
            }
            catch (InvalidPathException ipe)
            {
                // if a directory with the data type name cannot be found, return empty list
                return results;
            }

            unfilteredResults = getPatientResources(pathToPatient);
        }

        else if (context.equals("Population"))
        {
            unfilteredResults = getPopulationResources(dataType);
        }

        else
        {
            throw new UnsupportedOperationException("Unknown context: " + context);
        }

        if (codePath == null && (codes != null || valueSet != null))
        {
            throw new IllegalArgumentException("A code path must be provided when filtering on codes or a valueset.");
        }

        List<Object> filteredCodeResults = new ArrayList<>();

//        if (codePath != null && !codePath.equals(""))
//        {
//            ValueSetInfo valueSetInfo = null;
//            if (valueSet != null && !valueSet.equals(""))
//            {
//                valueSetInfo = new ValueSetInfo().withId(valueSet);
//            }
//
//            if (codes == null && valueSetInfo != null && terminologyProvider != null)
//            {
//                codes = terminologyProvider.expand(valueSetInfo);
//            }
//
//            if (codes != null)
//            {
//                for (Object resource : unfilteredResults)
//                {
//                    Object code = resolvePath(resource, codePath);
//                    if (terminologyProvider != null)
//                    {
//                        if ()
//                    }
//                }
//            }
//        }

        return unfilteredResults;
    }

    private List<Object> getPatientResources(Path pathToData)
    {
        List<File> resourceFiles = new ArrayList<>();
        if (!Files.exists(pathToData))
        {
            return Collections.emptyList();
        }
        try (Stream<Path> paths = Files.walk(pathToData))
        {
            paths.filter(Files::isRegularFile)
                    .forEach(x -> resourceFiles.add(x.toFile()));
        }
        catch (IOException ioe)
        {
            throw new RuntimeException("Error reading resource file: " + ioe.getMessage());
        }

        return resourceFiles.stream()
                .map(
                        x ->
                        {
                            try
                            {
                                return x.getName().endsWith(".json")
                                        ? jsonParser.parseResource(new FileReader(x))
                                        : xmlParser.parseResource(new FileReader(x));
                            }
                            catch (FileNotFoundException | DataFormatException e)
                            {
                                throw new RuntimeException("Error reading file: " + x.getAbsolutePath() + " with error: " + e.getMessage());
                            }
                        }
                )
                .collect(Collectors.toList());
    }

    private List<Object> getPopulationResources(String dataType)
    {
        List<Object> resources = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(pathToData))
        {
            paths.filter(x -> !x.toString().equals(pathToData.toString()))
                    .filter(Files::isDirectory)
                    .forEach(x -> resources.addAll(getPatientResources(x.resolve(dataType.toLowerCase()))));
        }
        catch (IOException ioe)
        {
            throw new RuntimeException("Error reading resource file: " + ioe.getMessage());
        }

        return resources;
    }


}
