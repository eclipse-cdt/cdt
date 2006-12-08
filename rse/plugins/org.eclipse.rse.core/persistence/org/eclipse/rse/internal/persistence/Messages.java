package org.eclipse.rse.internal.persistence;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.rse.internal.persistence.messages"; //$NON-NLS-1$
	public static String PropertyFileProvider_LoadingTaskName;
	public static String PropertyFileProvider_SavingTaskName;
	public static String PropertyFileProvider_UnexpectedException;
	public static String RSEPersistenceManager_DeleteProfileJobName;
	public static String SaveRSEDOMJob_SavingProfileJobName;
	public static String SerializingProvider_UnexpectedException;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
