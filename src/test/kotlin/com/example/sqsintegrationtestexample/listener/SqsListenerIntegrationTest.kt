package com.example.sqsintegrationtestexample.listener

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ContextConfiguration(initializers = [SqsListenerIntegrationTest.SqsListenerIntegrationTestInitializer::class])
@Testcontainers
class SqsListenerIntegrationTest {

    private lateinit var amazonSQS: AmazonSQS

    @BeforeEach
    internal fun setUp() {
        amazonSQS = AmazonSQSClientBuilder
                .standard()
                .withEndpointConfiguration(localStackContainer.getEndpointConfiguration(SQS))
                .withCredentials(localStackContainer.defaultCredentialsProvider)
                .build()
    }

    @Test
    internal fun `should consume a message`() {
        // given
        val payload = "sqs-listener-integration-test-payload"
        val queueUrl = amazonSQS.getQueueUrl(queueName).queueUrl

        // when
        val sendMessageResult = amazonSQS.sendMessage(queueUrl, payload)
        Assertions.assertNotNull(sendMessageResult.messageId)

        // then
        val receiveMessageRequest = amazonSQS.receiveMessage(queueUrl)
        val messages = receiveMessageRequest.messages
        Assertions.assertTrue(messages.isEmpty())
    }

    internal class SqsListenerIntegrationTestInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            val sqsEndpointConfiguration = localStackContainer.getEndpointConfiguration(SQS)
            System.setProperty("AMAZON_REGION", sqsEndpointConfiguration.signingRegion)
            System.setProperty("SQS_ENDPOINT", sqsEndpointConfiguration.serviceEndpoint)

            AmazonSQSClientBuilder
                    .standard()
                    .withEndpointConfiguration(localStackContainer.getEndpointConfiguration(SQS))
                    .withCredentials(localStackContainer.defaultCredentialsProvider)
                    .build().apply {
                        this.createQueue(queueName)
                    }
        }
    }

    companion object {
        @Container
        val localStackContainer: LocalStackContainer = LocalStackContainer().withServices(SQS)

        private const val queueName: String = "sqs-integration-test-queue"
    }
}
