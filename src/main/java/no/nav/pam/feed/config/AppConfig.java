package no.nav.pam.feed.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.pam.feed.Application;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@ComponentScan(basePackageClasses = Application.class)
public class AppConfig {

    @Bean
    public ObjectMapper jacksonMapper() {
        return new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder, HttpClientBuilder httpClientBuilder) {

        CloseableHttpClient httpClient = httpClientBuilder.build();
        ClientHttpRequestFactory rf = new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate = builder
                .setConnectTimeout(Duration.ofMillis(60 * 1000))
                .setReadTimeout(Duration.ofMillis(60 * 1000))
                .build();
        restTemplate.setRequestFactory(rf);

        return restTemplate;
    }


    @Bean
    @Profile("prod")
    public HttpClientBuilder httpClientBuilder() {
        return HttpClientBuilder.create();
    }

    @Bean
    @Profile({"dev", "test"})
    public HttpClientBuilder unsafeHttpClientBuilder() throws Exception {
        return UnsafeSSLContextUtil.unsafeHttpClientBuilder();
    }
}
