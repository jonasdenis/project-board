package de.adesso.projectboard.ldap.service;

import de.adesso.projectboard.base.user.persistence.User;
import de.adesso.projectboard.base.user.persistence.data.UserData;
import de.adesso.projectboard.ldap.configuration.LdapConfigurationProperties;
import de.adesso.projectboard.ldap.service.util.StructureMapper;
import de.adesso.projectboard.ldap.service.util.UserDataMapper;
import de.adesso.projectboard.ldap.service.util.data.StringStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Profile("adesso-ad")
@Service
public class LdapService {

    private final LdapTemplate ldapTemplate;

    private final LdapConfigurationProperties ldapProperties;

    @Autowired
    public LdapService(LdapTemplate ldapTemplate, LdapConfigurationProperties ldapProperties) {
        this.ldapTemplate = ldapTemplate;
        this.ldapProperties = ldapProperties;
    }

    /**
     *
     * @param userId
     *          The {@link User#id ID} of the {@link User}.
     *
     * @return
     *          {@code true}, iff the LDAP query searching for
     *          a <i>person</i> at the configured {@link LdapConfigurationProperties#getLdapBase() base}
     *          where the configured {@link LdapConfigurationProperties#getUserIdAttribute() user ID attribute}
     *          is equal to the given {@code userId} is <b>not empty</b>.
     *
     * @see LdapConfigurationProperties#getLdapBase()
     * @see LdapConfigurationProperties#getUserIdAttribute()
     */
    public boolean userExists(String userId) {
        String idAttribute = ldapProperties.getUserIdAttribute();
        String base = ldapProperties.getLdapBase();

        LdapQuery query = query()
                .countLimit(1)
                .base(base)
                .attributes(idAttribute, "objectClass")
                .where("objectClass").is("person")
                .and(idAttribute).isPresent()
                .and(idAttribute).is(userId);

        List<String> resultList = ldapTemplate.search(query, (AttributesMapper<String>) attributes -> {
            return (String) attributes.get(idAttribute).get();
        });

        return !resultList.isEmpty();
    }

    /**
     *
     * @param user
     *          The {@link User}.
     *
     * @return
     *          {@code true}, iff the LDAP query searching for
     *          a <i>person</i> at the configured {@link LdapConfigurationProperties#getLdapBase() base},
     *          where the configured {@link LdapConfigurationProperties#getUserIdAttribute() user ID attribute}
     *          is equal to the given {@code userId} and the <i>directReports</i> attribute is
     *          present, is <i>not empty</i>.
     */
    public boolean isManager(User user) {
        String idAttribute = ldapProperties.getUserIdAttribute();
        String base = ldapProperties.getLdapBase();

        LdapQuery query = query()
                .countLimit(1)
                .base(base)
                .attributes(idAttribute, "objectClass", "directReports")
                .where("objectClass").is("person")
                .and("directReports").isPresent()
                .and(idAttribute).isPresent()
                .and(idAttribute).is(user.getId());

        return !ldapTemplate.search(query, (AttributesMapper<String>) attributes -> (String) attributes.get(idAttribute).get()).isEmpty();
    }

    /**
     *
     * @param user
     *          The {@link User} to get the data from.
     *
     * @return
     *          The {@link UserData} instance for the fiven {@code user}
     *
     * @throws IllegalStateException
     *          When the query results length differed from {@code 1}.
     */
    public UserData getUserData(User user) throws IllegalStateException {
        String idAttribute = ldapProperties.getUserIdAttribute();
        String base = ldapProperties.getLdapBase();

        LdapQuery query = query()
                .countLimit(1)
                .base(base)
                .attributes(idAttribute, "name", "givenName", "userPrincipalName", "mail", "division")
                .where("name").isPresent()
                .and("givenName").isPresent()
                .and("division").isPresent()
                .and(idAttribute).isPresent()
                .and(idAttribute).is(user.getId());

        List<UserData> userDataList = ldapTemplate.search(query, new UserDataMapper(user));

        validateResult(userDataList, 1);

        return userDataList.get(0);
    }

    /**
     *
     * @param user
     *          The {@link User}
     *
     * @return
     *          A {@link StringStructure} instance, where the {@link StringStructure#staffMembers staff members}
     *          and {@link StringStructure#manager manager} fields contain the configured
     *          {@link LdapConfigurationProperties#getUserIdAttribute() user ID attribute} value.
     *
     * @throws IllegalStateException
     *          When the query results length differed from {@code 1}.
     */
    public StringStructure getIdStructure(User user) throws IllegalStateException {
        String idAttribute = ldapProperties.getUserIdAttribute();
        String base = ldapProperties.getLdapBase();

        LdapQuery query = query()
                .base(base)
                .attributes(idAttribute, "objectClass", "manager", "directReports")
                .where("objectClass").is("person")
                .and(idAttribute).isPresent()
                .and(idAttribute).is(user.getId());

        List<StringStructure> resultList = ldapTemplate.search(query, new StructureMapper(user));

        validateResult(resultList, 1);

        // get the IDs of the user for the corresponding distinguished name
        StringStructure dnStructure = resultList.get(0);

        Set<String> staffMemberIDs = dnStructure.getStaffMembers()
                .stream()
                .map(this::getUserIdByDN)
                .collect(Collectors.toSet());

        String managerID = getUserIdByDN(dnStructure.getManager());

        return new StringStructure(user, managerID, staffMemberIDs);
    }

    /**
     *
     * @param user
     *          The {@link User} to get the manager's {@link User#id ID}
     *          of.
     *
     * @return
     *          The manager's ID.
     *
     * @throws IllegalStateException
     *          When the query result length differed from {@code 1}.
     *
     * @see #getUserIdByDN(String)
     */
    public String getManagerId(User user) throws IllegalStateException {
        String idAttribute = ldapProperties.getUserIdAttribute();
        String base = ldapProperties.getLdapBase();

        LdapQuery query = query()
                .countLimit(1)
                .base(base)
                .attributes(idAttribute, "objectClass", "manager")
                .where("objectClass").is("person")
                .and(idAttribute).isPresent()
                .and(idAttribute).is(user.getId());

        List<String> managerList = ldapTemplate.search(query, (AttributesMapper<String>) attributes -> {
            return (String) attributes.get("manager").get();
        });

        validateResult(managerList, 1);

        return getUserIdByDN(managerList.get(0));
    }

    /**
     *
     * @param dn
     *          The distinguished name of the user.
     *
     * @return
     *          The configured {@link LdapConfigurationProperties#getUserIdAttribute() user ID attribute}'s
     *          value.
     *
     * @throws IllegalStateException
     *          When the query results length differed from {@code 1}.
     *
     */
    String getUserIdByDN(String dn) throws IllegalStateException {
        String idAttribute = ldapProperties.getUserIdAttribute();

        LdapQuery query = query()
                .countLimit(1)
                .base(dn)
                .attributes("distinguishedName", idAttribute)
                .where("distinguishedName").isPresent()
                .and(idAttribute).isPresent();

        List<String> idList = ldapTemplate.search(query, (AttributesMapper<String>) attributes -> {
            return (String) attributes.get(idAttribute).get();
        });

        validateResult(idList, 1);

        return idList.get(0);
    }

    void validateResult(List<?> resultList, int expectedSize) throws IllegalStateException {
        if(resultList != null) {
            if(resultList.size() != expectedSize) {
                throw new IllegalStateException("Illegal result count: " + resultList.size());
            }
        }
    }

}
