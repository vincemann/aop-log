package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.CustomLogger;
import com.github.vincemann.aoplog.api.annotation.CustomToString;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomToStringInfo {

    // method name of method that should get called instead of toString()
    private String methodName;
    private LoggableMethodPart methodPart;

    public CustomToStringInfo(CustomToString toString) {
        this.methodName = toString.toStringMethod();
        this.methodPart = LoggableMethodPart.from(toString.key());
    }
}
