package org.opencds.cqf.cql.elm.execution;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.opencds.cqf.cql.execution.Context;
import org.opencds.cqf.cql.runtime.BaseTemporal;
import org.opencds.cqf.cql.runtime.CqlList;
import org.opencds.cqf.cql.runtime.Interval;
import org.opencds.cqf.cql.runtime.Precision;
import org.opencds.cqf.cql.runtime.Quantity;

/*
collapse(argument List<Interval<T>>) List<Interval<T>>
collapse(argument List<Interval<T>>, per Quantity) List<Interval<T>>

The collapse operator returns the unique set of intervals that completely covers the ranges present in the given list of intervals.
    In other words, adjacent intervals within a sorted list are merged if they either overlap or meet.

Note that because the semantics for overlaps and meets are themselves defined in terms of the interval successor and predecessor operators,
    sets of date/time-based intervals that are only defined to a particular precision will calculate meets and overlaps at that precision.
    For example, a list of DateTime-based intervals where the boundaries are all specified to the hour will collapse at the hour precision,
        unless the collapse precision is overridden with the per argument.

The per argument determines the precision at which the collapse will be performed, and must be a quantity value that is compatible with the
    point type of the input intervals. For numeric intervals, this means a default unit ('1'). For date/time intervals, this means a temporal duration.

If the list of intervals is empty, the result is empty. If the list of intervals contains a single interval, the result is a list with that interval.
    If the list of intervals contains nulls, they will be excluded from the resulting list.

If the list argument is null, the result is null.

If the per argument is null, the default unit interval for the point type of the intervals involved will be used
    (i.e. the interval that has a width equal to the result of the successor function for the point type).
*/

public class CollapseEvaluator extends org.cqframework.cql.elm.execution.Collapse {

    public static List<Interval> collapse(Iterable<Interval> list, Quantity per) {
        if (list == null)
        {
            return null;
        }

        List<Interval> intervals = CqlList.toList(list, false);

        if (intervals.isEmpty())
        {
            return intervals;
        }

        intervals.sort(new CqlList().valueSort);

        boolean isTemporal =
                intervals.get(0).getStart() instanceof BaseTemporal
                        || intervals.get(0).getEnd() instanceof BaseTemporal;

        boolean isInteger = intervals.get(0).getStart() instanceof Integer
                        || intervals.get(0).getEnd() instanceof Integer;

        
        boolean isQuantity = intervals.get(0).getStart() instanceof Quantity
                        || intervals.get(0).getEnd() instanceof Quantity;

                
        boolean isDecimal = intervals.get(0).getStart() instanceof BigDecimal
                        || intervals.get(0).getEnd() instanceof BigDecimal;


        if (per == null)
        {
            if (isTemporal) {
                List<BaseTemporal> endpoints = intervals.stream().map(x -> Arrays.asList(x.getStart(), x.getEnd()))
                    .flatMap(x -> x.stream())
                    .filter(x -> x != null)
                    .map(x -> (BaseTemporal)x)
                    .collect(Collectors.toList());

                String precision = BaseTemporal.getLowestPrecision(endpoints.toArray(BaseTemporal[]::new));
                per = new Quantity().withUnit(precision).withValue(new BigDecimal(1.0));
            }
            else if (isInteger) {
                per = new Quantity().withDefaultUnit().withValue(new BigDecimal(1.0));
            }
            else if (isDecimal) {
                List<BigDecimal> endpoints = intervals.stream()
                    .map(x -> Arrays.asList(x.getStart(), x.getEnd()))
                    .flatMap(x -> x.stream())
                    .filter(x -> x != null)
                    .map(x -> (BigDecimal)x)
                    .collect(Collectors.toList());

                BigDecimal coarsestPrecision = endpoints.stream()
                    .map(x -> Math.max(0, x.scale()))
                    .map(x -> x == 0 ? new BigDecimal("1") : new BigDecimal("0." + "0".repeat(x - 1) + "1"))
                    .max(Comparator.naturalOrder()).get();

                per = new Quantity().withDefaultUnit().withValue(coarsestPrecision);
            }
            else if (isQuantity) {
                // 1 "unit"
                per = new Quantity().withValue(new BigDecimal(1.0));
            }
            else {
                per = new Quantity().withDefaultUnit();
            }
        }

        // This precision applies to temporal precision only.
        Precision precision = isTemporal ? Precision.fromString(per.getUnit()) : null;

        Object anchor = intervals.get(0).getStart();

        List<Interval> collapsedIntervals = new ArrayList<Interval>();
        for (int i = 0; i < intervals.size(); ++i)
        {
            Interval current = intervals.get(i);

            Object start = current.getStart();
            if (LessEvaluator.less(start, anchor)) {
                start = anchor;
            }

            start = quantize(anchor, start, per, precision);

            if (LessEvaluator.less(start, current.getStart())) {
                start =  AddEvaluator.add(start, 
                isTemporal ? per :
                isInteger ?  per.getValue().intValue() :
                per.getValue());
            }

            Object end = current.getEnd();
            if (LessEvaluator.less(end, start)) {
                end = start;
            }

            end = quantize(start, end, per, precision);

            anchor = end;

            // No interval to construct
            if (EqualEvaluator.equal(start, end)) {
                continue;
            }

            Interval next = new Interval(start, true, end, true);
            if (collapsedIntervals.isEmpty()) {
                collapsedIntervals.add(next);
                continue;
            }

            Interval last = collapsedIntervals.get(collapsedIntervals.size() - 1);

            if (
                OverlapsEvaluator.overlaps(last, next, precision != null ? precision.toString() : null) || 
                MeetsEvaluator.meets(last, next, precision != null ? precision.toString() : null)) {
                    next = new Interval(last.getStart(), true, next.getEnd(), true);
                collapsedIntervals.set(collapsedIntervals.size() - 1, next);
            }
            else {
                collapsedIntervals.add(next);
            }
        }

        return collapsedIntervals;
    }

    // The target value is quantized in units of "per" from the reference
    // Temporal values are compared with "precision"
    private static Object quantize(Object reference, Object target, Quantity per, Precision precision) {
        // The distance between the reference and the target
        Object distance = precision != null ? 
            // Duration parameters are reversed compared to subtract
            DurationBetweenEvaluator.duration(reference, target, precision) : 
            SubtractEvaluator.subtract(target, reference);

        Object offsetPers = TruncatedDivideEvaluator.div(distance, distance instanceof Integer ? per.getValue().intValue(): per.getValue());
        Object offset = MultiplyEvaluator.multiply(offsetPers, offsetPers instanceof Integer ? per.getValue().intValue(): per.getValue());
        return AddEvaluator.add(reference, precision != null ? 
            new Quantity().withUnit(per.getUnit()).withValue(new BigDecimal((Integer)offset)) : 
            offset);
    }

    @Override
    protected Object internalEvaluate(Context context)
    {
        Iterable<Interval> list = (Iterable<Interval>) getOperand().get(0).evaluate(context);
        Quantity per = (Quantity) getOperand().get(1).evaluate(context);

        return collapse(list, per);
    }
}