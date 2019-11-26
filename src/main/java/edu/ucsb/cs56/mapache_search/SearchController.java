package edu.ucsb.cs56.mapache_search;

import edu.ucsb.cs56.mapache_search.entities.AppUser;
import edu.ucsb.cs56.mapache_search.repositories.SearchResultRepository;
import edu.ucsb.cs56.mapache_search.entities.SearchResultEntity;
import edu.ucsb.cs56.mapache_search.repositories.UserRepository;
import edu.ucsb.cs56.mapache_search.search.SearchResult;
import edu.ucsb.cs56.mapache_search.search.Item;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class SearchController {

    private Logger logger = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private SearchService searchService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SearchResultRepository searchRepository;

    @Autowired
    private AuthControllerAdvice controllerAdvice;

    @Autowired
    public SearchController(SearchResultRepository searchRepository) {
        this.searchRepository = searchRepository;   
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("searchObject", new SearchObject());
        return "index";
    }

    @GetMapping("/UpDownSearch")
    public String upDownSearch(Model model) {
        model.addAttribute("searchObject", new SearchObject());
        return "upDownIndex";
    }

    public class ResultVoteWrapper {
        Item googleResult;
        SearchResultEntity dbResult;

        public ResultVoteWrapper(Item googleResult, SearchResultEntity dbResult) {
            this.googleResult = googleResult;
            this.dbResult = dbResult;
        }
    }

    @GetMapping("/searchResults")
    public String search(@RequestParam(name = "query", required = true) String query, Model model, OAuth2AuthenticationToken token) throws IOException {
        model.addAttribute("query", query);

        String apiKey = userRepository.findByUid(controllerAdvice.getUid(token)).get(0).getApikey();
        String json = searchService.getJSON(query, apiKey);

        SearchResult sr = SearchResult.fromJSON(json);
        model.addAttribute("searchResult", sr);

        List<ResultVoteWrapper> voteResults = new ArrayList<>();
        int count = 0;
        for(Item item : sr.getItems()) {
            if (searchRepository.findByUrl(item.getLink()).isEmpty()) {
                SearchResultEntity result = new SearchResultEntity();
                result.setUrl(item.getLink());
                result.setVoteCount(new Long(0));
                searchRepository.save(result);

                // searchRepository.findByUrl(item.getLink()).get(0);
                voteResults.add(new ResultVoteWrapper(item, result));
            }
            
            if (++count == 10)
                break;
        }
        model.addAttribute("voteResult", voteResults);
        
        return "searchResults"; // corresponds to src/main/resources/templates/searchResults.html
    }

    @GetMapping("/searchUpDownResults")
    public String searchUpDown(@RequestParam(name = "query", required = true) String query, Model model, OAuth2AuthenticationToken token) throws IOException {
        model.addAttribute("query", query);

        String apiKey = userRepository.findByUid(controllerAdvice.getUid(token)).get(0).getApikey();
        String json = searchService.getJSON(query, apiKey);

        SearchResult sr = SearchResult.fromJSON(json);
        model.addAttribute("searchResult", sr);
        
        return "searchUpDownResults"; // corresponds to src/main/resources/templates/searchResults.html
    }

}
