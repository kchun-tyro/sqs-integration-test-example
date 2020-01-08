package com.example.sqsintegrationtestexample.publisher

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ContextConfiguration(initializers = [SqsPublisherIntegrationTest.SqsListenerIntegrationTestInitializer::class])
@Testcontainers
class SqsPublisherIntegrationTest {

    @Autowired
    lateinit var amazonSQSClient: AmazonSQSAsync

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
    internal fun `should publish a message to a queue`() {
        // given
        val queueUrl = amazonSQS.getQueueUrl(queueName).queueUrl
        val payload = "sqs-publisher-integration-test-payload"

        // when
        amazonSQSClient.sendMessage(queueUrl, payload)

        // then
        val receiveMessageRequest = amazonSQS.receiveMessage(queueUrl)
        val messages = receiveMessageRequest.messages
        Assertions.assertTrue(messages.isNotEmpty())
        Assertions.assertEquals(payload, messages.first().body)
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

        private const val queueName: String = "sqs-publisher-integration-test-queue"
    }
}
