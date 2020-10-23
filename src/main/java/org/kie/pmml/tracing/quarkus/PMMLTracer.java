/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.pmml.tracing.quarkus;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.kie.api.pmml.PMML4Result;
import org.kie.kogito.Application;
import org.kie.kogito.prediction.PredictionModels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PMMLTracer {

    private static final String INPUT_TOPIC_NAME = "kogito-tracing-prediction-input";
    private static final String OUTPUT_TOPIC_NAME = "kogito-tracing-prediction-output";
    private static final String PMML_MODEL = "pmmlModel";
    private static final String INPUT_DATA = "inputData";
    private static final String OK = "OK";
    private static final String  FAIL= "FAIL";

    private static final Logger logger = LoggerFactory.getLogger(PMMLTracer.class);

    private PredictionModels predictionModels;

    @Inject
    public PMMLTracer(Application application) {
        this.predictionModels = application.predictionModels();
    }

    @Incoming(INPUT_TOPIC_NAME)
    @Outgoing(OUTPUT_TOPIC_NAME)
    @Broadcast
    public PMML4Result process(Map<String, Object> rawInput) {
        logger.info("Received {}", rawInput);
        PMML4Result toReturn;
        if (!rawInput.containsKey(PMML_MODEL)) {
            logger.error("Missing required data '{}'", PMML_MODEL);
            toReturn = new PMML4Result();
            toReturn.setResultCode(FAIL);
        } else if (!rawInput.containsKey(INPUT_DATA)) {
            logger.error("Missing required data '{}'", INPUT_DATA);
            toReturn = new PMML4Result();
            toReturn.setResultCode(FAIL);
        } else {
            String pmmlModel = (String) rawInput.get(PMML_MODEL);
            Map<String, Object> inputData = (Map<String, Object>) rawInput.get(INPUT_DATA);
            toReturn = evaluatePMML(pmmlModel, inputData);
        }
        return toReturn;
    }

    private PMML4Result evaluatePMML(String pmmlModel, Map<String, Object> inputData) {
        try {
            org.kie.kogito.prediction.PredictionModel prediction = predictionModels.getPredictionModel(pmmlModel);
            return prediction.evaluateAll(prediction.newContext(inputData));
        } catch (Exception e) {
            logger.error("Failed to evaluate model {}", e.getMessage(), e);
            PMML4Result toReturn = new PMML4Result();
            toReturn.setResultCode(FAIL);
            return toReturn;
        }
    }
}
