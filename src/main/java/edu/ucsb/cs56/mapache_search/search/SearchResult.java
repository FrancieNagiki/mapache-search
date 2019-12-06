package edu.ucsb.cs56.mapache_search.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SearchResult object, represents the top level JSON object returned by the
 * Google Custom Search JSON API <a href=
 * "https://developers.google.com/custom-search/v1/introduction">https://developers.google.com/custom-search/v1/introduction</a>
 */

public class SearchResult {

    private static Logger logger = LoggerFactory.getLogger(SearchResult.class);

    private String kind;
    private List<Item> items;
    private SearchInformation searchInformation;

    public String getKind() {
        return this.kind;
    }

    public List<Item> getItems() {
        return this.items;
    }

    public SearchInformation getSearchInformation() {
        return this.searchInformation;
    }

    public int getPageCount() {
        long totalResults = Long.parseLong(searchInformation.getTotalResults());

        // api caps search results at 100 and page size at 10, so there can only ever be a maximum of 10 pages
        // totalResults still shows all matching documents, even if the api only allows us to see the first 100
        // https://developers.google.com/custom-search/v1/cse/list#start
        return (int) Math.min(Math.ceil(totalResults / 10.0), 10);
    }

    /**
     * Create a CoursePage object from json representation
     *
     * @param json String of json returned by API endpoint {@code /classes/search}
     * @return a new CoursePage object
     * @throws IOException
     * @see <a href=
     *      "https://developer.ucsb.edu/content/academic-curriculums">https://developer.ucsb.edu/content/academic-curriculums</a>
     */
    public static SearchResult fromJSON(String json) throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            if(json == "{\"error\": \"401: Unauthorized\"}"){
                SearchResult sr = SearchResult.handleApiError();
                return sr;
            } else {
                SearchResult sr = objectMapper.readValue(json, SearchResult.class);
                return sr;
            }
        } catch (JsonProcessingException jpe) {
            logger.error("JsonProcessingException:" + jpe);
            return null;
        }
}

    private static SearchResult handleApiError() {
        SearchResult e = new SearchResult();
        e.kind = "error";
        return e;
    }
}
