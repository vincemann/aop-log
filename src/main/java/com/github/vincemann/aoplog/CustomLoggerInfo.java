package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.CustomLogger;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomLoggerInfo {


    enum Type{
        ARG,
        RET
    }

    private CustomLogger logger;
    private Type type;
    private Integer argNum;
}
