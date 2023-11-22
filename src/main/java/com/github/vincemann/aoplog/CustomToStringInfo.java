package com.github.vincemann.aoplog;

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

}
