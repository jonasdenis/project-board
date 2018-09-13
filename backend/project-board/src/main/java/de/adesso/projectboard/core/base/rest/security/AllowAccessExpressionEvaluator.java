package de.adesso.projectboard.core.base.rest.security;

import de.adesso.projectboard.core.base.rest.user.persistence.User;
import org.springframework.security.core.Authentication;

/**
 * Auto-configured implementation of the {@link ExpressionEvaluator} interface. Method implementations
 * always return <i>true</i>.
 *
 * @see ExpressionEvaluator
 */
public class AllowAccessExpressionEvaluator implements ExpressionEvaluator {

    /**
     *
     * @param authentication
     *          The {@link Authentication} object.
     *
     * @param user
     *          The {@link User} object of the currently authenticated user.
     *
     * @return
     *          <i>true</i>
     */
    @Override
    public boolean hasAccessToProjects(Authentication authentication, User user) {
        return true;
    }

    /**
     *
     * @param authentication
     *          The {@link Authentication} object.
     *
     * @param user
     *          The {@link User} object of the currently authenticated user.
     *
     * @param projectId
     *          The id of the {@link de.adesso.projectboard.core.base.rest.project.persistence.AbstractProject}
     *          the user wants to access.
     *
     * @return
     *          <i>true</i>
     */
    @Override
    public boolean hasAccessToProject(Authentication authentication, User user, long projectId) {
        return true;
    }

    /**
     *
     * @param authentication
     *          The {@link Authentication} object.
     *
     * @param user
     *          The {@link User} object of the currently authenticated user.
     *
     * @return
     *          <i>true</i>
     */
    @Override
    public boolean hasPermissionToApply(Authentication authentication, User user) {
        return true;
    }

    /**
     *
     * @param authentication
     *          The {@link Authentication} object.
     *
     * @param user
     *          The {@link User} object of the currently authenticated user.
     *
     * @param userId
     *          The id of the {@link de.adesso.projectboard.core.base.rest.user.persistence.User}
     *          the current user wants to access.
     *
     * @return
     *          <i>true</i>
     */
    @Override
    public boolean hasPermissionToAccessUser(Authentication authentication, User user, String userId) {
        return true;
    }

    /**
     *
     * @param authentication
     *          The {@link Authentication} object.
     *
     * @param user
     *          The {@link User} object of the currently authenticated user.
     *
     * @param userId
     *          The id of the {@link de.adesso.projectboard.core.base.rest.user.persistence.User}
     *          the current user wants to access.
     *
     * @return
     *          <i>true</i>
     */
    @Override
    public boolean hasElevatedAccessToUser(Authentication authentication, User user, String userId) {
        return true;
    }

}
