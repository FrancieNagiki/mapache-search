package edu.ucsb.cs56.mapache_search;

import edu.ucsb.cs56.mapache_search.repositories.SearchResultRepository;
import edu.ucsb.cs56.mapache_search.entities.SearchResultEntity;
import edu.ucsb.cs56.mapache_search.repositories.UserRepository;
import edu.ucsb.cs56.mapache_search.search.SearchResult;
import edu.ucsb.cs56.mapache_search.search.Item;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/searchResults")
    public String search(@RequestParam(name = "query", required = true) String query, Model model, OAuth2AuthenticationToken token) throws IOException {
        model.addAttribute("query", query);

        String apiKey = userRepository.findByUid(controllerAdvice.getUid(token)).get(0).getApikey();
        String json = searchService.getJSON(query, apiKey);

        SearchResult sr = SearchResult.fromJSON(json);
        model.addAttribute("searchResult", sr);
        model.addAttribute("searchObject", new SearchObject());
        model.addAttribute("previousSearch", query);
        
        return "searchResults"; // corresponds to src/main/resources/templates/searchResults.html
    }

    public class ResultVoteWrapper implements Comparable<ResultVoteWrapper> {
        private Item googleResult;
        private SearchResultEntity dbResult;
        private int position;

        public ResultVoteWrapper(Item googleResult, SearchResultEntity dbResult, int position) {
            this.googleResult = googleResult;
            this.dbResult = dbResult;
            this.position = position;
        }

        public SearchResultEntity getDBResult() {
            return dbResult;
        }

        public Item getGoogleResult() {
            return googleResult;
        }

        public int getPosition() {
            return position;
        }

        public int compareTo(ResultVoteWrapper oWrapper) {
            if (getDBResult().getVotecount() > oWrapper.getDBResult().getVotecount()) {
                return 1;
            }
            else if (getDBResult().getVotecount() < oWrapper.getDBResult().getVotecount()) {
                return -1;
            } else {
                if (getPosition() < oWrapper.getPosition()) {
                    return 1;
                } else if (getPosition() > oWrapper.getPosition()) {
                    return -1;
                }
                return 0;
            }
        }
    }

    @GetMapping("/searchUpDownResults")
    public String searchUpDown(@RequestParam(name = "query", required = true) String query, Model model, OAuth2AuthenticationToken token) throws IOException {
        model.addAttribute("query", query);

        String apiKey = userRepository.findByUid(controllerAdvice.getUid(token)).get(0).getApikey();
        String json = searchService.getJSON(query, apiKey);

        SearchResult sr = SearchResult.fromJSON(json);
        model.addAttribute("searchResult", sr);

        if(sr.getKind() != "error"){
            List<ResultVoteWrapper> voteResults = new ArrayList<>();
            int count = 0;
            for(Item item : sr.getItems()) {
                List<SearchResultEntity> matchingResults = searchRepository.findByUrl(item.getLink());
                SearchResultEntity result;
                if (matchingResults.isEmpty()) {
                    result = new SearchResultEntity();
                    result.setUrl(item.getLink());
                    result.setVotecount((long) 0);
                    searchRepository.save(result);
                } else {
                    result = matchingResults.get(0);
                }
                voteResults.add(new ResultVoteWrapper(item, result, count));

                if (++count == 10)
                    break;
            }
            System.out.println(voteResults.size());
            Collections.sort(voteResults, Collections.reverseOrder());
            model.addAttribute("voteResult", voteResults);
        }
        
        return "searchUpDownResults"; // corresponds to src/main/resources/templates/searchResults.html
    }

    @GetMapping("/updateVote")
    public String searchUpDown(@RequestParam(name = "direction", required = true) String direction, @RequestParam(name = "query", required = true) String query, @RequestParam(name = "id", required = true) long id, Model model, OAuth2AuthenticationToken token) throws IOException {
            List<SearchResultEntity> matchingResults = searchRepository.findById(id);
            if (!matchingResults.isEmpty()) {
                SearchResultEntity result = matchingResults.get(0);
                if (direction.equals("up")){
                    result.setVotecount(result.getVotecount() + 1l);
                }
                if(direction.equals("down")){
                    result.setVotecount(result.getVotecount() - 1l);
                }
                searchRepository.save(result);
            }
        
        return "forward:/searchUpDownResults"; // brings you back to results view
    }


}
