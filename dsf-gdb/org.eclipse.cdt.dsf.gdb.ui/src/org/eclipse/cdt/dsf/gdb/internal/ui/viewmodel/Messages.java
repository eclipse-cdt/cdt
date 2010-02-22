package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
    static {
        // initialize resource bundle
        NLS.initializeMessages(Messages.class.getName(), Messages.class);
    }

    private Messages() {}

    public static String Internal_Error;
}
