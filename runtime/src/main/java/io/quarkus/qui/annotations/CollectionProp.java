package io.quarkus.qui.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a method in Props with this
 * if you want the set method to add
 * to a collection instead of overriding
 * the field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CollectionProp {
    /**
     * Specifies what method to call
     * to add to the collection. Defaults
     * to addAll for Collections, putAll
     * for Maps. Must be specified if
     * the collection type is not
     * a Collection or Map.
     */
    String addAll() default "";

    /**
     * Specifies what method to call
     * to clear the collection. Defaults
     * to clear for Collections and Maps.
     * Must be specified if the collection
     * type is not a Collection or Map.
     */
    String reset() default "clear";
}
