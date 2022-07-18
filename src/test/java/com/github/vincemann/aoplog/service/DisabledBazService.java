package com.github.vincemann.aoplog.service;

import com.github.vincemann.aoplog.Severity;
import com.github.vincemann.aoplog.api.annotation.LogConfig;
import com.github.vincemann.aoplog.api.annotation.LogInteraction;

@LogInteraction(disabled = true)
@LogConfig(ignoreSetters = true)
public class DisabledBazService extends AbstractBazService {

    @Override
    public void inImpl(String dFirst, String dSecond) {

    }

    @Override
    public void inInterface(String dFirst, String dSecond) {

    }

    @Override
    public void getInImpl(String dFirst, String dSecond) {

    }

    @LogInteraction(Severity.INFO)
    @Override
    public void setInImpl(String dFirst, String dSecond) {

    }
}
