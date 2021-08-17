package de.adesso.projectboard.reader.configuration;

import de.adesso.projectboard.reader.JiraProjectReader;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * The configuration properties for the {@link JiraProjectReader}.
 *
 * @see JiraProjectReader
 */
@ConfigurationProperties(prefix = "projectboard.jira")
@Validated
@Data
public class JiraConfigurationProperties {

    /**
     * Whether or not to enable the JIRA project reader.
     * Enabled by default.
     */
    private boolean enabled = true;

    /**
     * The request URL for JIRA. It must contain a string placeholder
     * like <i>{jqlQuery}</i>. The string placeholder is replaced
     * with the actual JQL query.
     */
    @Pattern(
            regexp = ".*jql=\\{.+\\}.*",
            message = "The JIRA URL must contain a placeholder for the JQL query!"
    )
    @NotEmpty
    private String requestUrl;

    /**
     * The request URL of the serverInfo API endpoint.
     */
    @NotEmpty
    private String serverInfoUrl;

    /**
     * The URL the project ID gets appended to
     * to refer to the JIRA issue.
     */
    @Pattern(
            regexp = ".*/issues/",
            message = "The issue URL must end with '/issues/'!"
    )
    @NotEmpty
    private String issueUrl;

    /**
     * The username to use for authorization.
     */
    private String username = "";

    /**
     * The password to use for authorization.
     */
    private String password = "";

}
