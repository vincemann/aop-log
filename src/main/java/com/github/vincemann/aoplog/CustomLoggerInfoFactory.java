package com.github.vincemann.aoplog;


import com.github.vincemann.aoplog.api.annotation.CustomLogger;

import java.util.Set;

public interface CustomLoggerInfoFactory {
    CustomLoggerInfo createCustomLoggerInfo(CustomLogger customLogger);
}
