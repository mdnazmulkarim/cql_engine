package org.opencds.cqf.cql.elm.execution;

import org.opencds.cqf.cql.exception.InvalidOperatorArgument;
import org.opencds.cqf.cql.execution.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
Split(stringToSplit String, separator String) List<String>

The Split operator splits a string into a list of strings using a separator.
If the stringToSplit argument is null, the result is null.
If the stringToSplit argument does not contain any appearances of the separator,
  the result is a list of strings containing one element that is the value of the stringToSplit argument.
*/

public class SplitEvaluator extends org.cqframework.cql.elm.execution.Split {

    public static Object split(Object stringToSplit, Object separator) {
        if (stringToSplit == null) {
            return null;
        }

        if (stringToSplit instanceof String) {
            List<Object> result = new ArrayList<>();
            if (separator == null) {
                result.add(stringToSplit);
            }
            else {
                Collections.addAll(result, ((String) stringToSplit).split((String) separator));
            }
            return result;
        }

        throw new InvalidOperatorArgument(
                "Split(String, String)",
                String.format("Split(%s, %s)", stringToSplit.getClass().getName(), separator.getClass().getName())
        );
    }

    @Override
    protected Object internalEvaluate(Context context) {
        Object stringToSplit = getStringToSplit().evaluate(context);
        Object separator = getSeparator().evaluate(context);
        return split(stringToSplit, separator);
    }
}
