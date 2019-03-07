package no.nav.pam.feed.search;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
public class SearchConnector {

    private static final Logger LOG = LoggerFactory.getLogger(SearchConnector.class);
    private static final String API_PATH = "/ad/_search";

    private final int pagesize;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String searchApiUrl;


    @Autowired
    public SearchConnector(ObjectMapper mapper,
                           RestTemplate resttemplate,
                           @Value("${pam.search-api.url}") String searchApiUrl,
                           @Value("${feed.pagesize:20}") int pagesize) {
        this.mapper = mapper;
        this.restTemplate = resttemplate;
        this.searchApiUrl = searchApiUrl;
        this.pagesize = pagesize;
    }

    public JsonNode fetchSearchResponse() {

        HttpEntity<String> entity = new HttpEntity<>(null, null);

        ResponseEntity<JsonNode> response = restTemplate.exchange(URI.create(searchApiUrl), HttpMethod.GET, entity, JsonNode.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            //TODO
        }

        return response.getBody();
    }
}
