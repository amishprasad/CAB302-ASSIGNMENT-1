package com.kineticfitness.util;

import com.kineticfitness.model.BodyPart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Strategy for deciding which exercises the Workout History page shows. Each filter knows
 * its own chip label and its own rule, so the page never compares strings. Adding a
 * {@link BodyPart} adds a chip and a working filter with no other edits.
 *
 * <p>A filter works on a {@link BodyPart} (null means none was recorded), so it has no
 * dependency on any view class.</p>
 */
public interface BodyPartFilter {

    /** Text shown on the filter chip. */
    String label();

    /** Whether an exercise recorded against this body part (possibly null) passes the filter. */
    boolean matches(BodyPart part);

    /**
     * The items that pass this filter, in their original order. The input is not modified.
     *
     * @param partOf how to read the body part from an item, e.g. {@code Entry::getBodyPart}
     */
    default <T> List<T> apply(List<T> items, Function<T, BodyPart> partOf) {
        return items.stream().filter(item -> matches(partOf.apply(item))).collect(Collectors.toList());
    }

    /** Lets everything through, including exercises with no body part. */
    static BodyPartFilter all() {
        return new BodyPartFilter() {
            @Override public String label() { return "All"; }
            @Override public boolean matches(BodyPart part) { return true; }
        };
    }

    /** Lets through only exercises recorded against the given body part. */
    static BodyPartFilter forPart(BodyPart target) {
        return new BodyPartFilter() {
            @Override public String label() {
                String name = target.name();
                return name.charAt(0) + name.substring(1).toLowerCase();
            }
            @Override public boolean matches(BodyPart part) { return part == target; }
        };
    }

    /** "All" followed by one filter per body part, in {@link BodyPart} declaration order. */
    static List<BodyPartFilter> standard() {
        List<BodyPartFilter> filters = new ArrayList<>();
        filters.add(all());
        for (BodyPart part : BodyPart.values()) {
            filters.add(forPart(part));
        }
        return Collections.unmodifiableList(filters);
    }
}