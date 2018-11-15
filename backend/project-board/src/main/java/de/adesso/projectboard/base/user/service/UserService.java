package de.adesso.projectboard.base.user.service;

import de.adesso.projectboard.base.access.service.UserAccessService;
import de.adesso.projectboard.base.exceptions.UserNotFoundException;
import de.adesso.projectboard.base.user.persistence.User;
import de.adesso.projectboard.base.user.persistence.data.UserData;
import de.adesso.projectboard.base.user.persistence.structure.OrganizationStructure;
import de.adesso.projectboard.base.util.Sorting;

import java.util.List;
import java.util.Objects;

/**
 * Service interface to provide functionality to manage {@link User}s and their
 * corresponding data.
 *
 * @see UserAccessService
 */
public interface UserService {

    /**
     *
     * @return
     *          The currently authenticated {@link User}.
     */
    default User getAuthenticatedUser() {
        return getUserById(getAuthenticatedUserId());
    }

    /**
     *
     * @return
     *          The currently authenticated {@link User}'s
     *          {@link User#id ID}.
     */
    String getAuthenticatedUserId();

    /**
     *
     * @param userId
     *          The {@link User#id ID} of the {@link User}.
     *
     * @return
     *          {@code true}, iff a {@link User} instance with the
     *          given {@code userId} exists.
     */
    boolean userExists(String userId);

    /**
     *
     * @param user
     *          The {@link User}.
     *
     * @return
     *          {@code true}, iff the {@link User} is a manager.
     */
    boolean userIsManager(User user);

    /**
     *
     * @param user
     *          The {@code User} to get the {@link OrganizationStructure}
     *          for.
     *
     * @return
     *          The {@link OrganizationStructure} instance for the given
     *          {@code user}.
     */
    OrganizationStructure getStructureForUser(User user);

    /**
     *
     * @param user
     *          The {@link User} to get the {@link UserData} for.
     *
     * @return
     *          The {@link UserData user data} for the given {@code user}.
     */
    UserData getUserData(User user);

    /**
     *
     * @param userId
     *          The {@link User#id ID} of the {@link User}.
     *
     * @return
     *          The corresponding {@link User} instance iff
     *          {@link #userExists(String)} returns {@code true}.
     *
     * @throws UserNotFoundException
     *          When no {@link User} with the given {@code userId} was found.
     */
    User getUserById(String userId) throws UserNotFoundException;

    /**
     *
     * @param user
     *          The {@link User} to check.
     *
     * @param staffMember
     *          The {@link User} instance of the staff member.
     *
     * @return
     *          {@code true}, iff the {@code user} has the given {@code staffMember}
     *          as a staff member.
     */
    boolean userHasStaffMember(User user, User staffMember);

    /**
     *
     * @param user
     *          The {@link User} to get the manager of.
     *
     * @return
     *          The manager of the {@code user}.
     */
    User getManagerOfUser(User user);

    /**
     *
     * @param user
     *          The {@link User} to get the staff members'
     *          {@link UserData} of.
     *
     * @param sorting
     *          The {@link Sorting} instance to sort by.
     *
     * @return
     *          A {@link List} of the {@link UserData} belonging to
     *          the staff members.
     */
    List<UserData> getStaffMemberDataOfUser(User user, Sorting sorting);

    /**
     *
     * @param user
     *          The {@link User} to save.
     *
     * @return
     *          The saved user instance.
     */
    User save(User user);

    /**
     *
     * @param user
     *          The {@link User} to delete.
     */
    void delete(User user);

    /**
     *
     * @param userId
     *          The {@link User#id ID} of the {@link User} to delete.
     */
    void deleteUserById(String userId);

    /**
     * Method to validate the existence of a given {@link User}
     * instance.
     *
     * @param user
     *          The {@link User} to validate.
     *
     * @return
     *          The given {@code user}.
     *
     * @throws UserNotFoundException
     *          When the no {@link User} with the given {@code user}'s
     *          {@link User#id ID} exists.
     *
     * @see #userExists(String)
     */
    default User validateExistence(User user) throws UserNotFoundException {
        User givenUser = Objects.requireNonNull(user);

        if(userExists(givenUser.getId())) {
            return givenUser;
        } else {
            throw new UserNotFoundException();
        }
    }

}
