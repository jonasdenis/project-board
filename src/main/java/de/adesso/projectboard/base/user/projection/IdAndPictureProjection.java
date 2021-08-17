package de.adesso.projectboard.base.user.projection;

import de.adesso.projectboard.base.projection.NamedProjection;
import org.springframework.beans.factory.annotation.Value;

@NamedProjection(
        name = "pictureonly",
        target = UserProjectionSource.class
)
public interface IdAndPictureProjection {

    @Value("#{target.user.id}")
    String getId();

    @Value("#{target.data.picture}")
    byte[] getPicture();

}
