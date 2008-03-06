package org.eclipse.rse.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.IRSEModelInitializer;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.RSEPreferencesManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.core.model.ISystemRegistry;

public class RSELocalConnectionInitializer implements IRSEModelInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.IRSEModelInitializer#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus run(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
		// create a local host object if one is desired and one has not yet been created in this workspace.
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		ISystemProfileManager profileManager = RSECorePlugin.getTheSystemProfileManager();
		ISystemProfile profile = profileManager.getDefaultPrivateSystemProfile();
		String localConnectionName = RSECoreMessages.RSELocalConnectionInitializer_localConnectionName;
		IHost localHost = registry.getHost(profile, localConnectionName);
		if (localHost == null && RSEPreferencesManager.getCreateLocalConnection()) {
			// create the connection only if the local system type is enabled
			IRSESystemType systemType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(IRSESystemType.SYSTEMTYPE_LOCAL_ID);
			if (systemType != null && systemType.isEnabled()) {
				String userName = System.getProperty("user.name"); //$NON-NLS-1$
				registry.createLocalHost(profile, localConnectionName, userName);
			}
		}
		monitor.done();
		return status;
	}

}
