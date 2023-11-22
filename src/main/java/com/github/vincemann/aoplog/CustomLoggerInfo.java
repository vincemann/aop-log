package com.github.vincemann.aoplog;

import com.github.vincemann.aoplog.api.CustomLogger;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomLoggerInfo {

    private CustomLogger logger;
    private LoggableMethodPart methodPart;
}
