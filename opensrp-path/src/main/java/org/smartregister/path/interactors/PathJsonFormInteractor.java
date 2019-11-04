package org.smartregister.path.interactors;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import org.smartregister.path.widgets.PathDatePickerFactory;
import org.smartregister.path.widgets.PathEditTextFactory;

/**
 * Created by keyman on 11/04/2017.
 */
public class PathJsonFormInteractor extends JsonFormInteractor {

    private static final JsonFormInteractor PATH_INTERACTOR_INSTANCE = new PathJsonFormInteractor();

    private PathJsonFormInteractor() {
        super();
    }

    @Override
    protected void registerWidgets() {
        super.registerWidgets();
        map.put(JsonFormConstants.EDIT_TEXT, new PathEditTextFactory());
        map.put(JsonFormConstants.DATE_PICKER, new PathDatePickerFactory());
    }

    public static JsonFormInteractor getPathInteractorInstance() {
        return PATH_INTERACTOR_INSTANCE;
    }
}
