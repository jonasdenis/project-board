package de.adesso.projectboard.base.application.rest;

import de.adesso.projectboard.base.access.rest.UserAccessController;
import de.adesso.projectboard.base.application.dto.ApplicationDtoFactory;
import de.adesso.projectboard.base.application.dto.ProjectApplicationRequestDTO;
import de.adesso.projectboard.base.application.dto.ProjectApplicationResponseDTO;
import de.adesso.projectboard.base.application.handler.ProjectApplicationHandler;
import de.adesso.projectboard.base.application.persistence.ProjectApplication;
import de.adesso.projectboard.base.application.service.ApplicationService;
import de.adesso.projectboard.base.project.rest.NonPageableProjectController;
import de.adesso.projectboard.base.user.rest.BookmarkController;
import de.adesso.projectboard.base.user.rest.UserController;
import de.adesso.projectboard.base.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * {@link RestController REST Controller} for {@link ProjectApplication}s.
 *
 * @see NonPageableProjectController
 * @see BookmarkController
 * @see UserAccessController
 * @see UserController
 */
@RestController
@RequestMapping("/users")
public class ApplicationController {

    private final ApplicationDtoFactory applicationDtoFactory;

    private final UserService userService;

    private final ApplicationService applicationService;

    private final ProjectApplicationHandler applicationHandler;

    @Autowired
    public ApplicationController(ApplicationDtoFactory applicationDtoFactory,
                                 UserService userService,
                                 ApplicationService applicationService,
                                 ProjectApplicationHandler applicationHandler) {
        this.applicationDtoFactory = applicationDtoFactory;
        this.userService = userService;
        this.applicationService = applicationService;
        this.applicationHandler = applicationHandler;
    }

    @PreAuthorize("(hasPermissionToAccessUser(#userId) && hasPermissionToApply()) || hasRole('admin')")
    @PostMapping(path = "/{userId}/applications")
    public ProjectApplicationResponseDTO createApplicationForUser(@Valid @RequestBody ProjectApplicationRequestDTO requestDTO, @PathVariable String userId) {
        var user = userService.getUserById(userId);

        var application = applicationService.createApplicationForUser(user, requestDTO);

        // call the handler method
        applicationHandler.onApplicationReceived(application);

        return applicationDtoFactory.createDto(application);
    }

    @PreAuthorize("hasPermissionToAccessUser(#userId) || hasRole('admin')")
    @GetMapping(path = "/{userId}/applications")
    public Collection<ProjectApplicationResponseDTO> getApplicationsOfUser(@PathVariable String userId) {
        var user = userService.getUserById(userId);

        return applicationService.getApplicationsOfUser(user).stream()
                .map(applicationDtoFactory::createDto)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasPermissionToAccessUser(#userId) || hasRole('admin')")
    @GetMapping(path = "/{userId}/staff/applications")
    public Collection<ProjectApplicationResponseDTO> getApplicationsOfStaffMembers(@PathVariable String userId, @SortDefault(sort = "applicationDate", direction = Sort.Direction.DESC) Sort sort) {
        var user = userService.getUserById(userId);
        var staffMembers = userService.getStaffMembersOfUser(user);

        return applicationService.getApplicationsOfUsers(null, sort).stream()
                .map(applicationDtoFactory::createDto)
                .collect(Collectors.toList());
    }

}
