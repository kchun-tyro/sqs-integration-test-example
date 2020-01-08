package com.example.sqsintegrationtestexample.listener

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.QueueAttributeName
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest
import com.example.sqsintegrationtestexample.SqsHandler
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.verify
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
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

    @MockBean
    lateinit var sqsHandler: SqsHandler

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
        assertNotNull(sendMessageResult.messageId)

        // then
        // we need some times for listener to consume the message
        Thread.sleep(500L)
        // There is no obvious why to directly verify consumption of a message therefore,
        // We indirectly verify that the expected services have been interacted with
        verify(sqsHandler, atLeast(1)).handle(payload)

        val receiveMessageRequest = amazonSQS.receiveMessage(queueUrl)
        val messages = receiveMessageRequest.messages
        assertTrue(messages.isEmpty())
    }

    internal class SqsListenerIntegrationTestInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            val sqsEndpointConfiguration = localStackContainer.getEndpointConfiguration(SQS)
            System.setProperty("AMAZON_REGION", sqsEndpointConfiguration.signingRegion)
            System.setProperty("SQS_ENDPOINT", sqsEndpointConfiguration.serviceEndpoint)

            val sqs = AmazonSQSClientBuilder
                    .standard()
                    .withEndpointConfiguration(localStackContainer.getEndpointConfiguration(SQS))
                    .withCredentials(localStackContainer.defaultCredentialsProvider)
                    .build()
            sqs.createQueue(queueName)
            val queueUrl: String = sqs.getQueueUrl(queueName).queueUrl
            val request = SetQueueAttributesRequest()
                    .withQueueUrl(queueUrl)
                    .addAttributesEntry(QueueAttributeName.VisibilityTimeout.toString(), visibilityTimeout)
            sqs.setQueueAttributes(request)

            println("Created queue " + queueName + " with " +
                    "visibility timeout set to " + visibilityTimeout +
                    " seconds.")
        }
    }

    companion object {
        @Container
        val localStackContainer: LocalStackContainer = LocalStackContainer().withServices(SQS)

        private const val queueName = "sqs-integration-test-queue"
        private const val visibilityTimeout = "0"
    }
}
