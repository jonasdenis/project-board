package de.adesso.projectboard.base.access.rest;

import de.adesso.projectboard.base.access.handler.UserAccessHandler;
import de.adesso.projectboard.base.access.payload.UserAccessPayload;
import de.adesso.projectboard.base.access.service.UserAccessService;
import de.adesso.projectboard.base.user.projection.DefaultUserProjection;
import de.adesso.projectboard.base.user.projection.UserProjectionFactory;
import de.adesso.projectboard.base.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserAccessController {

    private final UserService userService;

    private final UserAccessService userAccessService;

    private final UserAccessHandler userAccessHandler;

    private final UserProjectionFactory projectionFactory;

    @Autowired
    public UserAccessController(UserService userService,
                                UserAccessService userAccessService,
                                UserAccessHandler userAccessHandler,
                                UserProjectionFactory projectionFactory) {
        this.userService = userService;
        this.userAccessService = userAccessService;
        this.userAccessHandler = userAccessHandler;
        this.projectionFactory = projectionFactory;
    }

    @PreAuthorize("hasElevatedAccessToUser(#userId) || hasRole('admin')")
    @PostMapping(path = "/{userId}/access")
    public ResponseEntity<?> createAccessForUser(@Valid @RequestBody UserAccessPayload payload, @PathVariable String userId) {
        var user = userService.getUserById(userId);
        var updatedUser = userAccessService.giveUserAccessUntil(user, payload.getAccessEnd());

        // call handler method
        userAccessHandler.onAccessGranted(updatedUser);

        return ResponseEntity.ok(projectionFactory.createProjection(updatedUser, DefaultUserProjection.class));
    }

    @PreAuthorize("hasElevatedAccessToUser(#userId) || hasRole('admin')")
    @DeleteMapping(path = "/{userId}/access")
    public ResponseEntity<?> deleteAccessForUser(@PathVariable String userId) {
        var user = userService.getUserById(userId);
        var updatedUser = userAccessService.removeAccessFromUser(user);

        return ResponseEntity.ok(projectionFactory.createProjection(updatedUser, DefaultUserProjection.class));
    }

}
