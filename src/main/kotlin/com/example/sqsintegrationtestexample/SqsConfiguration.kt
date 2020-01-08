package com.example.sqsintegrationtestexample

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.aws.messaging.config.annotation.SqsConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(SqsConfiguration::class)
class SqsConfiguration {

    @Bean
    fun amazonSQS(
            @Value("#{environment.AMAZON_REGION}")
            amazonRegion: String,
            @Value("#{environment.SQS_ENDPOINT}")
            sqsEndpoint: String
    ): AmazonSQSAsync {
        return AmazonSQSAsyncClient
                .asyncBuilder()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(sqsEndpoint, amazonRegion))
                .build()
    }

}
