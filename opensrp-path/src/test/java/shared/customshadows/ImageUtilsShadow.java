package shared.customshadows;

import org.opensrp.api.constants.Gender;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Photo;
import org.smartregister.path.R;

import util.ImageUtils;

/**
 * Created by onadev on 15/06/2017.
 */
@Implements(ImageUtils.class)
public class ImageUtilsShadow extends Shadow {

    @Implementation
    public static Photo profilePhotoByClient(CommonPersonObjectClient client) {
        return new Photo();
    }


    @Implementation
    public static int profileImageResourceByGender(String gender) {
        return R.drawable.child_boy_infant;
    }

    @Implementation
    public static int profileImageResourceByGender(Gender gender) {
        return R.drawable.child_transgender_inflant;
    }

}
