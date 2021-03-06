package org.opencds.cqf.cql.elm.execution;

import org.opencds.cqf.cql.execution.Context;
import org.opencds.cqf.cql.runtime.Code;
import org.opencds.cqf.cql.terminology.CodeSystemInfo;

public class CodeDefEvaluator extends org.cqframework.cql.elm.execution.CodeDef {

    @Override
    protected Object internalEvaluate(Context context) {
        CodeSystemInfo info = (CodeSystemInfo) getCodeSystem().evaluate(context);
        return new Code().withCode(this.getId()).withSystem(info.getId());
    }
}
