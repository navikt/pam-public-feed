package no.nav.pam.feed.search;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private SearchConnector connector;

    @Autowired
    public SearchService(SearchConnector connector) {
        this.connector = connector;
    }

    public String fetchResults() {
        return connector.fetchSearchResponse().toString();
    }
}
