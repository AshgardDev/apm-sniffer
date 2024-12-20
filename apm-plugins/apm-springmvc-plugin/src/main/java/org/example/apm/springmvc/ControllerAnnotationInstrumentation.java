package org.example.apm.springmvc;

import org.example.core.match.ClassAnnotationNameMatch;
import org.example.core.match.ClassMatch;

public class ControllerAnnotationInstrumentation extends SpringMvcCommonInstrumentation {

    private static final String CONTROLLER_NAME = "org.springframework.stereotype.Controller";

    @Override
    public ClassMatch enhanceClass() {
        return ClassAnnotationNameMatch.byAnnotationMatch(CONTROLLER_NAME);
    }
}
