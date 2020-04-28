package org.opencds.cqf.cql.engine.elm.execution;

import java.util.HashMap;

import org.opencds.cqf.cql.engine.execution.Context;

public class TupleEvaluator extends org.cqframework.cql.elm.execution.Tuple {

  @Override
  protected Object internalEvaluate(Context context) {
    HashMap<String, Object> ret = new HashMap<>();
    for (org.cqframework.cql.elm.execution.TupleElement element : this.getElement()) {
      ret.put(element.getName(), element.getValue().evaluate(context));
    }
    return new org.opencds.cqf.cql.engine.runtime.Tuple().withElements(ret);
  }
}
