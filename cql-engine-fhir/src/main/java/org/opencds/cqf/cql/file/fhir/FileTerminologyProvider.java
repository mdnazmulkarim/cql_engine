package org.opencds.cqf.cql.file.fhir;

import org.opencds.cqf.cql.runtime.Code;
import org.opencds.cqf.cql.terminology.CodeSystemInfo;
import org.opencds.cqf.cql.terminology.TerminologyProvider;
import org.opencds.cqf.cql.terminology.ValueSetInfo;

public class FileTerminologyProvider implements TerminologyProvider
{

    public FileTerminologyProvider(String pathToTerminology)
    {

    }

    @Override
    public boolean in(Code code, ValueSetInfo valueSet)
    {
        return false;
    }

    @Override
    public Iterable<Code> expand(ValueSetInfo valueSet)
    {
        return null;
    }

    @Override
    public Code lookup(Code code, CodeSystemInfo codeSystem)
    {
        return null;
    }
}
