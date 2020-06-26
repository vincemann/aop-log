/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.vincemann.aoplog.service;

import com.github.vincemann.aoplog.Severity;
import com.github.vincemann.aoplog.api.LogInteraction;
import com.github.vincemann.aoplog.api.LogException;
import com.github.vincemann.aoplog.api.Lp;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

/**
 * Simple service, implementation of {@link FooService}.
 */
@Service
public class SimpleFooService implements FooService {

   @LogInteraction
//    @LogTrace
    @Override
    public void voidMethodZero() {
        // nothing to do
    }


    @Override
    public String stringMethodOne(String first) {
        return "stringMethodOne:" + first;
    }

    @LogInteraction
    @Override
    public String stringMethodTwo(String first, @Lp String second) {
        return "stringMethodTwo:" + first + ":" + second;
    }

    @LogInteraction
    @Override
    public String stringMethodThree(String first, String second, String third) {
        return "stringMethodThree:" + first + ":" + second + ":" + third;
    }

    @LogInteraction(Severity.TRACE)
    @LogException
    @Override
    public String stringMethodTwoVarargs(String first, @Lp String... second) {
        return "stringMethodTwoVarargs:" + first + ":" + Arrays.toString(second);
    }

    @LogInteraction
    @LogException(value = {@LogException.Exc(value = Exception.class, stacktrace = true)}, warn = {@LogException.Exc({IllegalArgumentException.class, IOException.class})})
    @Override
    public void voidExcMethodZero() throws IOException {
        throw new IOException("io fail");
    }
}
