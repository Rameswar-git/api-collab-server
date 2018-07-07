package io.apicollab.server.controller;

import io.apicollab.server.domain.Application;
import io.apicollab.server.dto.ApplicationDTO;
import io.apicollab.server.dto.ApplicationListDTO;
import io.apicollab.server.mapper.ApplicationMapper;
import io.apicollab.server.service.ApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@Slf4j
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationMapper applicationMapper;

    @GetMapping("/applications")
    public ApplicationListDTO get() {
        ApplicationListDTO applications = ApplicationListDTO.builder().applications(applicationMapper.toDtos(applicationService.getAll())).build();
        applications.getApplications().forEach(application ->
                application.add(linkTo(methodOn(ApplicationController.class).getOne(application.getApplicationId())).withSelfRel())
        );
        return applications;
    }

    @GetMapping("/applications/{id}")
    public ApplicationDTO getOne(@PathVariable String id) {
        ApplicationDTO application = applicationMapper.toDto(applicationService.findById(id));
        application.add(ControllerLinkBuilder.linkTo(methodOn(ApiController.class).getApplicationApis(application.getApplicationId())).withRel("apis"));
        return application;
    }

    @PostMapping("/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationDTO create(@RequestBody @Valid ApplicationDTO input) {
        Application application = applicationMapper.toEntity(input);
        application = applicationService.create(application);
        ApplicationDTO applicationDTO = applicationMapper.toDto(application);
        applicationDTO.add(linkTo(methodOn(ApplicationController.class).getOne(application.getId())).withSelfRel());
        return applicationDTO;
    }

    @PutMapping("/applications/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApplicationDTO update(@PathVariable String id, @RequestBody @Valid ApplicationDTO input) {
        input.setApplicationId(id);
        Application application = applicationMapper.toEntity(input);
        application = applicationService.save(application);
        ApplicationDTO applicationDTO = applicationMapper.toDto(application);
        applicationDTO.add(linkTo(methodOn(ApplicationController.class).getOne(application.getId())).withSelfRel());
        return applicationDTO;
    }

    @DeleteMapping("/applications/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        applicationService.delete(id);
    }
}
