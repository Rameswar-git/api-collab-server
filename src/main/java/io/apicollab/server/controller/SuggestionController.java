package io.apicollab.server.controller;

import io.apicollab.server.dto.CollectionWrapperDTO;
import io.apicollab.server.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SuggestionController {

    @Autowired
    private SuggestionService service;

    @GetMapping("/suggestions/{partialWord}")
    public CollectionWrapperDTO<String> getSuggestions(@PathVariable String partialWord){
        return new CollectionWrapperDTO<>(service.search(partialWord));
    }
}
