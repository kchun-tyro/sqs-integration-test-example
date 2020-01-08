package com.example.sqsintegrationtestexample

import org.springframework.stereotype.Service

@Service
class SqsHandler {
    fun handle(payload: String) {
        println("Handling a payload: $payload")
    }
}
