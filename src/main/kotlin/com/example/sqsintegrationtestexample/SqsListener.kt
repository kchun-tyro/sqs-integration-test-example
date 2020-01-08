package com.example.sqsintegrationtestexample

import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener
import org.springframework.stereotype.Service

@Service
class SqsListener(val sqsHandler: SqsHandler) {
    @SqsListener(value = ["sqs-integration-test-queue"], deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    fun listen(payload: String) {
        sqsHandler.handle(payload)
    }
}
