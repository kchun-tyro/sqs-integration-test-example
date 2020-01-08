package com.example.sqsintegrationtestexample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SqsIntegrationTestApplication

fun main(args: Array<String>) {
	runApplication<SqsIntegrationTestApplication>(*args)
}
