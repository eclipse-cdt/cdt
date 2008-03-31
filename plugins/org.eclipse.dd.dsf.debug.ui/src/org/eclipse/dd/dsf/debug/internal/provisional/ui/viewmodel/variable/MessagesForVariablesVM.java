package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.variable;

import org.eclipse.osgi.util.NLS;

public class MessagesForVariablesVM extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.variable.messages"; //$NON-NLS-1$

    public static String VariableColumnPresentation_name;

    public static String VariableColumnPresentation_type;

    public static String VariableColumnPresentation_value;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, MessagesForVariablesVM.class);
    }

    private MessagesForVariablesVM() {
    }
}
