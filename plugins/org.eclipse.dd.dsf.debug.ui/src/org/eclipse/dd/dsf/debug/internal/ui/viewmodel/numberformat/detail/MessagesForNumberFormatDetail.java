package org.eclipse.dd.dsf.debug.internal.ui.viewmodel.numberformat.detail;

import org.eclipse.osgi.util.NLS;

class MessagesForNumberFormatDetail extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.dd.dsf.debug.internal.ui.viewmodel.numberformat.detail.messages"; //$NON-NLS-1$

    public static String NumberFormatDetailPane_name;
    public static String NumberFormatDetailPane_description;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, MessagesForNumberFormatDetail.class);
    }

    private MessagesForNumberFormatDetail() {
    }
}
