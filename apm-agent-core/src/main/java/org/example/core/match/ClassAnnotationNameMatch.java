package org.example.core.match;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class ClassAnnotationNameMatch implements IndirectMatch {

    private final String[] annotations;

    public ClassAnnotationNameMatch(String... annotations) {
        this.annotations = annotations;
    }

    @Override
    public ElementMatcher.Junction<? super TypeDescription> buildJunction() {
        ElementMatcher.Junction<? super TypeDescription> matcher = null;
        if (annotations != null) {
            for (String annotation : annotations) {
                if (matcher == null) {
                    matcher = isAnnotatedWith(named(annotation));
                } else {
                    matcher = matcher.and(isAnnotatedWith(named(annotation)));
                }
            }
        }
        return matcher;
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        AnnotationList declaredAnnotations = typeDescription.getDeclaredAnnotations();
        List<String> annotationList = Arrays.asList(annotations);
        for (AnnotationDescription declaredAnnotation : declaredAnnotations) {
            annotationList.remove(declaredAnnotation.getAnnotationType().getActualName());
        }
        return annotationList.isEmpty();
    }

    public static IndirectMatch byAnnotationMatch(String... annotations) {
        return new ClassAnnotationNameMatch(annotations);
    }
}
