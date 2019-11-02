package edu.ucsb.cs56.mapache_search;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {

    @Autowired   
    private SearchService searchService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("searchObject", new SearchResult());
        return "index";
    }

    @GetMapping("/searchResults")
    public String search(
        @RequestParam(name = "query", required = true) 
        String query,
        Model model
        ) {
        model.addAttribute("query", query);

        searchService.getJSON(query);

        return "searchResults"; // corresponds to src/main/resources/templates/searchResults.html
    }

}