package utils;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;

/**
 * A factory of {@link Matcher}s that can be used in
 * {@link com.google.inject.AbstractModule}.bindListener(matcher,listener)
 *
 * @author Mathias Bogaert
 */
public class MoreMatchers {
    private static class SubClassesOf extends AbstractMatcher<TypeLiteral<?>> {
        private final Class<?> baseClass;

        private SubClassesOf(Class<?> baseClass) {
            this.baseClass = baseClass;
        }

        @Override
        public boolean matches(TypeLiteral<?> t) {
            return baseClass.isAssignableFrom(t.getRawType());
        }
    }

    /**
     * Matcher matches all classes that extends, implements or is the same as baseClass
     *
     * @param baseClass the class to match subclasses for
     * @return Matcher
     */
    public static Matcher<TypeLiteral<?>> subclassesOf(Class<?> baseClass) {
        return new SubClassesOf(baseClass);
    }
}
