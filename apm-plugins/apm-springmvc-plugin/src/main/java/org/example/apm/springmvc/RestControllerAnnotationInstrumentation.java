package org.example.apm.springmvc;

import org.example.core.plugin.match.ClassAnnotationNameMatch;
import org.example.core.plugin.match.ClassMatch;

public class RestControllerAnnotationInstrumentation extends SpringMvcCommonInstrumentation{

    private static final String REST_CONTROLLER_NAME = "org.springframework.web.bind.annotation.RestController";

    @Override
    public ClassMatch enhanceClass() {
        return ClassAnnotationNameMatch.byAnnotationMatch(REST_CONTROLLER_NAME);
    }
}
