package org.example.core.plugin;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public interface ClassMatcher {

    ElementMatcher<? super TypeDescription> getClassMatcher ();
}
