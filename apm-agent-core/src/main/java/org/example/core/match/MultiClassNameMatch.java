package org.example.core.match;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class MultiClassNameMatch implements IndirectMatch {

    private final List<String> needMatchClassNames;

    public MultiClassNameMatch(String... classNames) {
        if (classNames == null) {
            throw new IllegalArgumentException("MultiClassMatch's needMatchClassNames must not be null");
        } else {
            this.needMatchClassNames = Arrays.asList(classNames);
        }
    }

    @Override
    public ElementMatcher.Junction<? super TypeDescription> buildJunction() {
        ElementMatcher.Junction<? super TypeDescription> junction = null;
        if (needMatchClassNames != null && !needMatchClassNames.isEmpty()) {
            for (String name : needMatchClassNames) {
                if (junction == null) {
                    junction = named(name);
                } else {
                    junction.or(named(name));
                }
            }
        }
        return junction;
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        return needMatchClassNames.contains(typeDescription.getActualName());
    }

    public static IndirectMatch byMultiClassMatch(String... classNames) {
        return new MultiClassNameMatch(classNames);
    }
}
