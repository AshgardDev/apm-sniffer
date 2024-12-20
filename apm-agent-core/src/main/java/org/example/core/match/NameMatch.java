package org.example.core.match;

import lombok.Getter;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

@Getter
public class NameMatch implements ClassMatch {

    private final String className;

    public NameMatch(String className) {
        this.className = className;
    }

    public ElementMatcher.Junction<? super TypeDescription> buildJunction(){
        return named(className);
    }

    public static NameMatch byClassName(String className) {
        return new NameMatch(className);
    }
}
