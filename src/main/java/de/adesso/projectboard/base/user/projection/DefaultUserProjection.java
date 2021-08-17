package de.adesso.projectboard.base.user.projection;

import de.adesso.projectboard.base.projection.NamedProjection;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

@NamedProjection(
        name = "withoutpicture",
        target = UserProjectionSource.class,
        defaultProjection = true
)
public interface DefaultUserProjection extends NameAndIdProjection {

    @Value("#{target.data.email}")
    String getEmail();

    @Value("#{target.data.lob}")
    String getLob();

    @Value("#{target.manager}")
    boolean getBoss();

    @Value("#{target.user.applications.size()}")
    long getApplications();

    @Value("#{target.user.bookmarks.size()}")
    long getBookmarks();

    @Value("#{target}")
    AccessSummary getAccessInfo();

    interface AccessSummary {

        @Value("#{@repositoryUserAccessService.userHasActiveAccessInterval(target.user)}")
        boolean getHasAccess();

        @Value("#{@repositoryUserAccessService.userHasActiveAccessInterval(target.user) ? target.user.getLatestAccessInterval().get().startTime : null}")
        LocalDateTime getAccessStart();

        @Value("#{@repositoryUserAccessService.userHasActiveAccessInterval(target.user) ? target.user.getLatestAccessInterval().get().endTime : null}")
        LocalDateTime getAccessEnd();

    }

}
