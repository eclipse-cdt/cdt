package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.register;

import org.eclipse.osgi.util.NLS;

public class MessagesForRegisterVM extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.register.messages"; //$NON-NLS-1$

    public static String RegisterColumnPresentation_description;

    public static String RegisterColumnPresentation_name;

    public static String RegisterColumnPresentation_value;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, MessagesForRegisterVM.class);
    }

    private MessagesForRegisterVM() {
    }
}
