package de.adesso.projectboard.base.application.rest;

import de.adesso.projectboard.base.application.handler.ProjectApplicationHandler;
import de.adesso.projectboard.base.application.payload.ProjectApplicationPayload;
import de.adesso.projectboard.base.application.projection.FullApplicationProjection;
import de.adesso.projectboard.base.application.projection.ReducedApplicationProjection;
import de.adesso.projectboard.base.application.service.ApplicationService;
import de.adesso.projectboard.base.projection.BaseProjectionFactory;
import de.adesso.projectboard.base.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
public class ApplicationController {

    private final UserService userService;

    private final ApplicationService applicationService;

    private final ProjectApplicationHandler applicationHandler;

    private final BaseProjectionFactory projectionFactory;

    @Autowired
    public ApplicationController(UserService userService,
                                 ApplicationService applicationService,
                                 ProjectApplicationHandler applicationHandler,
                                 BaseProjectionFactory projectionFactory) {
        this.userService = userService;
        this.applicationService = applicationService;
        this.applicationHandler = applicationHandler;
        this.projectionFactory = projectionFactory;
    }

    @PreAuthorize("(hasPermissionToAccessUser(#userId) && hasPermissionToApply()) || hasElevatedAccessToUser(#userId) || hasRole('admin')")
    @PostMapping(path = "/{userId}/applications")
    public ResponseEntity<?> createApplicationForUser(@Valid @RequestBody ProjectApplicationPayload payload, @PathVariable String userId) {
        var user = userService.getUserById(userId);

        var application = applicationService.createApplicationForUser(user, payload.getProjectId(), payload.getComment());

        // call the handler method
        applicationHandler.onApplicationReceived(application);

        var projection = projectionFactory.createProjectionForAuthenticatedUser(application,
                ReducedApplicationProjection.class, FullApplicationProjection.class);
        return ResponseEntity.ok(projection);
    }

    @PreAuthorize("hasPermissionToAccessUser(#userId) || hasRole('admin')")
    @GetMapping(path = "/{userId}/applications")
    public ResponseEntity<?> getApplicationsOfUser(@PathVariable String userId) {
        var user = userService.getUserById(userId);
        var staffApplications = applicationService.getApplicationsOfUser(user);

        var projections = projectionFactory.createProjectionsForAuthenticatedUser(staffApplications,
                ReducedApplicationProjection.class, FullApplicationProjection.class);
        return ResponseEntity.ok(projections);
    }

    @PreAuthorize("hasPermissionToAccessUser(#userId) || hasRole('admin')")
    @GetMapping(path = "/{userId}/staff/applications")
    public ResponseEntity<?> getApplicationsOfStaffMembers(@PathVariable String userId, @SortDefault(sort = "applicationDate", direction = Sort.Direction.DESC) Sort sort) {
        var user = userService.getUserById(userId);
        var staffMembers = userService.getStaffMembersOfUser(user);
        var applications =  applicationService.getApplicationsOfUsers(staffMembers, sort);

        // managers are allowed to see every project field
        var projections = projectionFactory.createProjections(applications,
                FullApplicationProjection.class);
        return ResponseEntity.ok(projections);
    }

}
