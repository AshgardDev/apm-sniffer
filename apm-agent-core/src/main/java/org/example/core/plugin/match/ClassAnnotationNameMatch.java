package org.example.core.plugin.match;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * 类上的注解匹配器 and
 */
public class ClassAnnotationNameMatch implements IndirectMatch {

    private final String[] annotations;

    public ClassAnnotationNameMatch(String... annotations) {
        if(annotations == null || annotations.length == 0) {
            throw new IllegalArgumentException("类注解列表不能为空");
        }
        this.annotations = annotations;
    }

    @Override
    public ElementMatcher.Junction<? super TypeDescription> buildJunction() {
        ElementMatcher.Junction<? super TypeDescription> matcher = null;
        for (String annotation : annotations) {
            if (matcher == null) {
                matcher = isAnnotatedWith(named(annotation));
            } else {
                matcher = matcher.and(isAnnotatedWith(named(annotation)));
            }
        }
        return matcher;
    }

    @Override   public boolean isMatch(TypeDescription typeDescription) {
        AnnotationList declaredAnnotations = typeDescription.getDeclaredAnnotations();
        // Arrays.asList()方法生成的list是不能修改的，即不能remove 元素
        List<String> annotationList = new ArrayList<>(Arrays.asList(annotations));
        for (AnnotationDescription declaredAnnotation : declaredAnnotations) {
            annotationList.remove(declaredAnnotation.getAnnotationType().getActualName());
        }
        return annotationList.isEmpty();
    }

    public static IndirectMatch byAnnotationMatch(String... annotations) {
        return new ClassAnnotationNameMatch(annotations);
    }
}
