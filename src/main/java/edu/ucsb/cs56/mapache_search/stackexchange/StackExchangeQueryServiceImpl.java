package edu.ucsb.cs56.mapache_search.stackexchange;

import edu.ucsb.cs56.mapache_search.stackexchange.objects.Questions;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StackExchangeQueryServiceImpl implements StackExchangeQueryService {
    private static Logger logger = LoggerFactory.getLogger(StackExchangeQueryServiceImpl.class);

    @Value("${stackapps_key}")
    private String apiKey;

    private static final String QUESTIONS_ENDPOINT =
        "https://api.stackexchange.com/2.2/questions/{questions}?order=desc&sort=votes&site={site}&key={key}&filter={filter}";

    @Override
    public Questions findQuestions(String site, List<Integer> questionIds) {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
            new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        String questions = questionIds.stream()
            .map(Object::toString)
            .collect(Collectors.joining(";"));

        Map<String, String> uriVariables =
            Map.of(
                "questions", questions,
                "site", site,
                "key", apiKey,
                "filter", "!LVBj29mB6Y(89*hufApoU5"
            );

        try {
            ResponseEntity<String> response =
                restTemplate.exchange(QUESTIONS_ENDPOINT, HttpMethod.GET, entity, String.class, uriVariables);

            return Questions.fromJSON(response.getBody());
        } catch (IOException e) {
            logger.error("IOException", e);
            return new Questions();
        }
    }
}
