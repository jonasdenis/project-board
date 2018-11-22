package de.adesso.projectboard.ldap.user;

import de.adesso.projectboard.base.exceptions.UserNotFoundException;
import de.adesso.projectboard.base.security.AuthenticationInfo;
import de.adesso.projectboard.base.user.persistence.User;
import de.adesso.projectboard.base.user.persistence.UserRepository;
import de.adesso.projectboard.base.user.persistence.data.UserData;
import de.adesso.projectboard.base.user.persistence.data.UserDataRepository;
import de.adesso.projectboard.base.user.persistence.structure.OrganizationStructure;
import de.adesso.projectboard.base.user.persistence.structure.OrganizationStructureRepository;
import de.adesso.projectboard.base.user.service.UserService;
import de.adesso.projectboard.ldap.service.LdapService;
import de.adesso.projectboard.ldap.service.util.data.StringStructure;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link UserService} implementation that uses LDAP queries to retrieve
 * user data from a AD.
 *
 * @see LdapService
 * @see UserRepository
 * @see AuthenticationInfo
 */
@Profile("adesso-ad")
@Service
public class LdapUserService implements UserService {

    private final UserRepository userRepo;

    private final UserDataRepository dataRepo;

    private final LdapService ldapService;

    private final OrganizationStructureRepository structureRepo;

    private final AuthenticationInfo authInfo;

    public LdapUserService(UserRepository userRepo,
                           UserDataRepository dataRepo,
                           LdapService ldapService,
                           OrganizationStructureRepository structureRepo,
                           AuthenticationInfo authInfo) {
        this.userRepo = userRepo;
        this.dataRepo = dataRepo;
        this.structureRepo = structureRepo;
        this.ldapService = ldapService;
        this.authInfo = authInfo;
    }

    @Override
    public User getAuthenticatedUser() throws UserNotFoundException {
        return getUserById(authInfo.getUserId());
    }

    @Override
    public String getAuthenticatedUserId() {
        return authInfo.getUserId();
    }

    /**
     * {@inheritDoc}
     *
     * @see UserRepository#existsById(Object)
     */
    @Override
    public boolean userExists(String userId) {
        return userRepo.existsById(userId) || ldapService.userExists(userId);
    }

    /**
     * Returns {@code true} iff the persisted {@link OrganizationStructure}'s
     * {@link OrganizationStructure#getStaffMembers() staff members collection}
     * is <b>not empty</b> or the result of {@link LdapService#isManager(String)}
     * if none is present.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean userIsManager(User user) {
        if(structureRepo.existsByUser(user)) {
            return structureRepo.existsByUserAndUserIsManager(user, true);
        } else {
            return ldapService.isManager(user.getId());
        }
    }

    /**
     * Returns the persisted {@link OrganizationStructure} instance
     * for the {@code user} or a new instance based on the
     * returned {@link StringStructure} by {@link LdapService#getIdStructure(User)}
     * after persisting it if none is present.
     *
     * {@inheritDoc}
     *
     * @see LdapService#getIdStructure(User)
     */
    @Override
    public OrganizationStructure getStructureForUser(User user) {
        // return the structure saved in the repo if one is present
        // or get the latest structure from the AD
        return structureRepo.findByUser(user).orElseGet(() -> {
            validateExistence(user);

            StringStructure idStructure = ldapService.getIdStructure(user);

            // get the corresponding manager instance
            String managerId = idStructure.getManager();
            User manager = userRepo.findById(managerId).orElseGet(() -> {
                return userRepo.save(new User(managerId));
            });

            // get the corresponding staff member instances
            Set<User> staffMembers = idStructure.getStaffMembers()
                    .parallelStream()
                    .map(userId -> {
                        return userRepo.findById(userId)
                                .orElseGet(() -> userRepo.save(new User(userId)));
                    })
                    .collect(Collectors.toSet());

            // a user is a manager when he has at least one staff member
            OrganizationStructure structure = new OrganizationStructure(user, manager, staffMembers, !staffMembers.isEmpty());

            // return the newly created, persisted instance
            return structureRepo.save(structure);
        });
    }

    /**
     * Returns the persisted {@link UserData} instance for the {@code user} iff
     * one is present. Returns the returned instance of {@link LdapService#getUserData(List)}
     * after persisting it.
     *
     * {@inheritDoc}
     *
     * @see LdapService#getUserData(List)
     */
    @Override
    public UserData getUserData(User user) {
        Optional<UserData> dataOptional = dataRepo.findByUser(user);

        // return the persisted instance if it is present
        // or retrieve the data and return it after persisting it
        return dataOptional.orElseGet(() -> {
            validateExistence(user);

            UserData data = ldapService.getUserData(Collections.singletonList(user)).get(0);

            return dataRepo.save(data);
        });
    }

    /**
     * Lazily initializes a {@link User} when there is no user with the
     * given {@code userId} present in the repository but a user exists
     * in the AD.
     * <p>
     * {@inheritDoc}
     *
     * @see LdapService#userExists(String)
     */
    @Override
    public User getUserById(String userId) throws UserNotFoundException {
        Optional<User> userOptional = userRepo.findById(userId);

        return userOptional.orElseGet(() -> {
            if (ldapService.userExists(userId)) {
                return userRepo.save(new User(userId));
            }

            throw new UserNotFoundException();
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see #getStructureForUser(User)
     */
    @Override
    public boolean userHasStaffMember(User user, User staffMember) {
        return getStructureForUser(user)
                .getStaffMembers()
                .contains(staffMember);
    }

    /**
     * Returns the referenced manager of the persisted {@link OrganizationStructure}
     * for the {@link User} with the given {@code userId} in case it is present and
     * returns the manager with the ID of the structure returned by
     * {@link LdapService#getIdStructure(User)}.
     *
     * {@inheritDoc}
     */
    @Override
    public User getManagerOfUser(User user) {
        Optional<OrganizationStructure> structureOptional = structureRepo.findByUser(user);

        if (structureOptional.isPresent()) {
            return structureOptional.get().getManager();
        } else {
            validateExistence(user);

            String managerId = ldapService.getManagerId(user);

            return getUserById(managerId);
        }
    }

    @Override
    public List<UserData> getStaffMemberDataOfUser(User user, Sort sort) {
        OrganizationStructure structureForUser = getStructureForUser(user);
        if (structureForUser.getStaffMembers().isEmpty()) {
            return Collections.emptyList();
        }

        // assure that data of all users is present in the repo
        List<User> nonCachedUsers = structureForUser
                .getStaffMembers()
                .stream()
                .filter(staffMember -> !dataRepo.existsByUser(staffMember))
                .collect(Collectors.toList());

        if(!nonCachedUsers.isEmpty()) {
            dataRepo.saveAll(ldapService.getUserData(nonCachedUsers));
        }

        return dataRepo.findByUserIn(structureForUser.getStaffMembers(), sort);
    }

    /**
     * {@inheritDoc}
     *
     * @see UserRepository#save(Object)
     */
    @Override
    public User save(User user) {
        return userRepo.save(user);
    }

    @Override
    public void delete(User user) {
        // intentionally left blank
    }

    @Override
    public void deleteUserById(String userId) {
        // intentionally left blank
    }

    @Override
    public Map<User, Boolean> usersAreManagers(Set<User> users) {
        Map<User, Boolean> userManagerMap = new HashMap<>();

        if(Objects.requireNonNull(users).isEmpty()) {
            return userManagerMap;
        }

        // get the cached OrganizationStructure instance for every user
        // does NOT have to contain every instance
        List<OrganizationStructure> existingStructures = structureRepo.findAllByUserIn(users);

        // add it to the map for every existing instance
        existingStructures.forEach(structure -> userManagerMap.put(structure.getUser(), structure.isUserIsManager()));

        // remove all users that have a cached structure
        List<User> usersWithStructs = users.stream()
                .filter(user -> existingStructures.stream()
                        .anyMatch(struct -> struct.getUser().equals(user))
                )
                .collect(Collectors.toList());
        users.removeAll(usersWithStructs);

        // call the ldap service method for every user that has no
        // cached instance and add it to the map
        Map<User, Boolean> ldapMap = users.parallelStream()
                .collect(Collectors.toMap(user -> user, user -> ldapService.isManager(user.getId())));
        userManagerMap.putAll(ldapMap);

        return userManagerMap;
    }

}
