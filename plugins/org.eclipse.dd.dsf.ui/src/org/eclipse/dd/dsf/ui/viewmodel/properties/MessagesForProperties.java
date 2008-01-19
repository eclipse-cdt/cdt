package org.eclipse.dd.dsf.ui.viewmodel.properties;

import org.eclipse.osgi.util.NLS;

class MessagesForProperties extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.dd.dsf.ui.viewmodel.properties.messages"; //$NON-NLS-1$

    public static String DefaultLabelMessage_label;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, MessagesForProperties.class);
    }

    private MessagesForProperties() {
    }
}
