/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.engine.queue.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 22/11/12
 * Time: 15:45
 */
public class MyExecutionForTest implements Serializable {

        private String executionId;
        private Long runningExecutionPlanId;
        private Long position;
        private List<String> contextsNames;

        public MyExecutionForTest(){

        }
        public  MyExecutionForTest(String executionId, Long runningExecutionPlanId, Long position, List<String> contextsNames) {
            this.executionId = executionId;
            this.runningExecutionPlanId = runningExecutionPlanId;
            this.position = position;
            this.contextsNames = contextsNames;
        }

        public List<String> getContextsNames() {
            return contextsNames;
        }

        public void setContextsNames(List<String> contextsNames) {
            this.contextsNames = contextsNames;
        }

        public String getExecutionId() {
            return executionId;
        }

        public void setExecutionId(String executionId) {
            this.executionId = executionId;
        }

        public Long getRunningExecutionPlanId() {
            return runningExecutionPlanId;
        }

        public void setRunningExecutionPlanId(Long runningExecutionPlanId) {
            this.runningExecutionPlanId = runningExecutionPlanId;
        }

        public Long getPosition() {
            return position;
        }

        public void setPosition(Long position) {
            this.position = position;
        }
    }
