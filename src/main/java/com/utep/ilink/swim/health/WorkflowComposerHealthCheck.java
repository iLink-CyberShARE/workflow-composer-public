package com.utep.ilink.swim.health;

import com.codahale.metrics.health.HealthCheck;

public class WorkflowComposerHealthCheck extends HealthCheck {

    public WorkflowComposerHealthCheck() {
    }

    @Override
    protected Result check() throws Exception {
        
        //TODO define health check
        return Result.healthy();
    }
}
