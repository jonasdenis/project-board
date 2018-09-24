package de.adesso.projectboard.core.base.rest.project.persistence;

import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * {@link IdentifierGenerator} strategy to generate a {@link String} ID for {@link Project}
 * entities if the ID is not set already.
 * <p>
 *     IDs are generated in a <i>[prefix][sequence number]</i> form.
 * </p>
 *
 * @see Project
 */
public class ProjectIdGenerator implements IdentifierGenerator, Configurable {

    private String prefix = "";

    /**
     * Generates a ID for a {@link Project} when the {@link Project#getId()}
     * is {@code null} or <i>empty</i>. Returns the set ID otherwise.
     *
     * @param session
     *          The {@link SharedSessionContractImplementor} session.
     *
     * @param obj
     *          The {@link Object} to generate a ID for.
     *
     * @return
     *          A unused {@link String} ID.
     *
     * @throws IllegalArgumentException
     *          In case the given {@code obj} is not a {@link Project}.
     *
     * @see #getSequenceNumber(SharedSessionContractImplementor, Object)
     */
    @Override
    public String generate(SharedSessionContractImplementor session, Object obj) throws IllegalArgumentException {
        if(obj instanceof Project) {
            Project project = (Project) obj;

            if(project.getId() == null || project.getId().isEmpty()) {
                return prefix + getSequenceNumber(session, obj);
            } else {
                return project.getId();
            }

        } else {
            throw new IllegalArgumentException("The object has to be of type Project!");
        }
    }

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        this.prefix = params.getProperty("prefix");
    }

    /**
     * Returns a unused sequence number to append to the {@code prefix} by
     * getting a list of all used IDs, removing non-matching IDs and
     *
     *
     * @param session
     *          The {@link SharedSessionContractImplementor} session.
     *
     * @param obj
     *          The {@link Object} to generate the sequence number for.
     *
     * @return
     *          A unused sequence number to append to the prefix.
     *
     * @see #generate(SharedSessionContractImplementor, Object)
     */
    @SuppressWarnings("unchecked")
    private long getSequenceNumber(SharedSessionContractImplementor session, Object obj) {
        // get all used IDs as a stream
        String queryString = String.format("SELECT %s FROM %s",
                session.getEntityPersister(obj.getClass().getName(), obj)
                        .getIdentifierPropertyName(),
                obj.getClass().getSimpleName());

        Stream idStream = session.createQuery(queryString).stream();

        // regex to filter out IDs not generated by this
        // generator
        String prefixIdRegex = prefix + ".+";

        // get the highest long after the prefix
        Optional idOptional = idStream
                .map(Object::toString)
                .filter(id -> ((String) id).matches(prefixIdRegex))
                .map(id -> ((String) id).substring(prefix.length()))
                .max((firstId, secondId) -> {
                    long firstIdLong = Long.parseLong((String) firstId);
                    long secondIdLong = Long.parseLong((String) secondId);

                    return Long.compare(firstIdLong, secondIdLong);
                });

        // return a incremented ID or 1 if it's the first
        // ID generated
        if(idOptional.isPresent()) {
            String currentMaxIdString = (String) idOptional.get();

            return Long.parseLong(currentMaxIdString) + 1L;
        } else {
            return 1L;
        }
    }



}
