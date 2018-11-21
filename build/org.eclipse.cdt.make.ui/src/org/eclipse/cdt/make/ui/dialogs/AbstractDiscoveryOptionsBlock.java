/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.preferences.TabFolderLayout;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;

/**
 * Fremework for loading profile option pages
 *
 * @author vhirsl
 */
public abstract class AbstractDiscoveryOptionsBlock extends AbstractCOptionPage {
	/**
	 * @deprecated since CDT 6.1
	 */
	@Deprecated
	protected static final String PREFIX = "ScannerConfigOptionsDialog"; //$NON-NLS-1$

	private static final String UNSAVEDCHANGES_TITLE = "ScannerConfigOptionsDialog.unsavedchanges.title"; //$NON-NLS-1$
	private static final String UNSAVEDCHANGES_MESSAGE = "ScannerConfigOptionsDialog.unsavedchanges.message"; //$NON-NLS-1$
	private static final String UNSAVEDCHANGES_BSAVE = "ScannerConfigOptionsDialog.unsavedchanges.button.save"; //$NON-NLS-1$
	private static final String UNSAVEDCHANGES_BCANCEL = "ScannerConfigOptionsDialog.unsavedchanges.button.cancel"; //$NON-NLS-1$
	private static final String ERROR_TITLE = "ScannerConfigOptionsDialog.error.title"; //$NON-NLS-1$
	private static final String ERROR_MESSAGE = "ScannerConfigOptionsDialog.error.message"; //$NON-NLS-1$
	private static final String PROFILE_PAGE = "profilePage"; //$NON-NLS-1$
	private static final String PROFILE_ID = "profileId"; //$NON-NLS-1$

	private Preferences fPrefs;
	private IScannerConfigBuilderInfo2 fBuildInfo;
	private boolean fInitialized = false;
	private String fPersistedProfileId = null;

	private Map<String, DiscoveryProfilePageConfiguration> fProfilePageMap = null;

	// Composite parent provided by the block.
	private Composite fCompositeParent;
	private AbstractDiscoveryPage fCurrentPage;

	/**
	 * @return Returns the project.
	 */
	public IProject getProject() {
		return getContainer().getProject();
	}

	/**
	 * @return Returns the fPrefs.
	 */
	public Preferences getPrefs() {
		return fPrefs;
	}

	/**
	 * @return Returns the fBuildInfo.
	 */
	public IScannerConfigBuilderInfo2 getBuildInfo() {
		return fBuildInfo;
	}

	/**
	 * @return Returns the fInitialized.
	 */
	public boolean isInitialized() {
		return fInitialized;
	}

	/**
	 * @param initialized The fInitialized to set.
	 */
	public void setInitialized(boolean initialized) {
		fInitialized = initialized;
	}

	/**
	 * @return true - OK to continue
	 */
	public boolean checkDialogForChanges() {
		boolean rc = true;
		if (isProfileDifferentThenPersisted()) {
			String title = MakeUIPlugin.getResourceString(UNSAVEDCHANGES_TITLE);
			String message = MakeUIPlugin.getResourceString(UNSAVEDCHANGES_MESSAGE);
			String[] buttonLabels = new String[] { MakeUIPlugin.getResourceString(UNSAVEDCHANGES_BSAVE),
					MakeUIPlugin.getResourceString(UNSAVEDCHANGES_BCANCEL), };
			MessageDialog dialog = new MessageDialog(getShell(), title, null, message, MessageDialog.QUESTION,
					buttonLabels, 0);
			int res = dialog.open();
			if (res == 0) { // OK
				callPerformApply();
				rc = true;
			} else if (res == 1) { // CANCEL
				rc = false;
			}
		}
		return rc;
	}

	public boolean isProfileDifferentThenPersisted() {
		return (fPersistedProfileId != null && !fPersistedProfileId.equals(getBuildInfo().getSelectedProfileId()));
	}

	public void updatePersistedProfile() {
		fPersistedProfileId = getBuildInfo().getSelectedProfileId();
	}

	/**
	 * Create a profile page only on request
	 *
	 * @author vhirsl
	 */
	protected static class DiscoveryProfilePageConfiguration {

		AbstractDiscoveryPage page;
		IConfigurationElement fElement;

		public DiscoveryProfilePageConfiguration(IConfigurationElement element) {
			fElement = element;
		}

		public AbstractDiscoveryPage getPage() throws CoreException {
			if (page == null) {
				page = (AbstractDiscoveryPage) fElement.createExecutableExtension("class"); //$NON-NLS-1$
			}
			return page;
		}

		public String getName() {
			return fElement.getAttribute("name"); //$NON-NLS-1$
		}
	}

	public AbstractDiscoveryOptionsBlock(String title) {
		super(title);
		initializeProfilePageMap();
	}

	public AbstractDiscoveryOptionsBlock(String title, ImageDescriptor image) {
		super(title, image);
		initializeProfilePageMap();
	}

	/**
	 *
	 */
	private void initializeProfilePageMap() {
		fProfilePageMap = new HashMap<>(5);

		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(MakeUIPlugin.getPluginId(),
				"DiscoveryProfilePage"); //$NON-NLS-1$
		IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
		for (int i = 0; i < infos.length; i++) {
			if (infos[i].getName().equals(PROFILE_PAGE)) {
				String id = infos[i].getAttribute(PROFILE_ID);
				fProfilePageMap.put(id, new DiscoveryProfilePageConfiguration(infos[i]));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#setContainer(org.eclipse.cdt.ui.dialogs.ICOptionContainer)
	 */
	@Override
	public void setContainer(ICOptionContainer container) {
		super.setContainer(container);

		fPrefs = getContainer().getPreferences();
		IProject project = getContainer().getProject();

		fInitialized = true;
		if (project != null) {
			try {
				fBuildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(project);
			} catch (CoreException e) {
				// missing builder information (builder disabled or legacy project)
				fInitialized = false;
				fBuildInfo = null;
			}
		} else {
			fBuildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(fPrefs, false);
		}
		if (fBuildInfo != null) {
			fPersistedProfileId = fBuildInfo.getSelectedProfileId();
		}
	}

	public void updateContainer() {
		getContainer().updateContainer();
	}

	protected void createBuildInfo() {
		if (getProject() != null) {
			try {
				// get the project properties
				fBuildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(getProject());
			} catch (CoreException e) {
				fBuildInfo = null;
			}
		} else {
			// get the preferences
			fBuildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(fPrefs, false);
		}
	}

	/**
	 * Create build info based on preferences
	 */
	protected void createDefaultBuildInfo() {
		// Populate with the default values
		if (getProject() != null) {
			// get the preferences
			fBuildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(fPrefs, false);
		} else {
			// get the defaults
			fBuildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(fPrefs, true);
		}
	}

	protected Composite getCompositeParent() {
		return fCompositeParent;
	}

	protected void setCompositeParent(Composite parent) {
		fCompositeParent = parent;
		fCompositeParent.setLayout(new TabFolderLayout());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			handleDiscoveryProfileChanged();
		}
	}

	/**
	 * Notification that the user changed the selection of the SCD profile.
	 */
	protected void handleDiscoveryProfileChanged() {
		if (getCompositeParent() == null) {
			return;
		}
		String profileId = getCurrentProfileId();
		AbstractDiscoveryPage page = getDiscoveryProfilePage(profileId);
		if (page != null) {
			if (page.getControl() == null) {
				Composite parent = getCompositeParent();
				page.setContainer(this);
				page.createControl(parent);
				parent.layout(true);
			}
			if (fCurrentPage != null) {
				fCurrentPage.setVisible(false);
			}
			page.setVisible(true);
		}
		setCurrentPage(page);
	}

	protected AbstractDiscoveryPage getCurrentPage() {
		return fCurrentPage;
	}

	protected void setCurrentPage(AbstractDiscoveryPage page) {
		fCurrentPage = page;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.AbstractDiscoveryPage#isValid()
	 */
	@Override
	public boolean isValid() {
		return (getCurrentPage() == null) ? true : getCurrentPage().isValid();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		return getCurrentPage().getErrorMessage();
	}

	protected AbstractDiscoveryPage getDiscoveryProfilePage(String profileId) {
		DiscoveryProfilePageConfiguration configElement = fProfilePageMap.get(profileId);
		if (configElement != null) {
			try {
				return configElement.getPage();
			} catch (CoreException e) {
			}
		}
		return null;
	}

	protected String getDiscoveryProfileName(String profileId) {
		DiscoveryProfilePageConfiguration configElement = fProfilePageMap.get(profileId);
		if (configElement != null) {
			return configElement.getName();
		}
		return null;
	}

	protected String getDiscoveryProfileId(String profileName) {
		Set<String> profileIds = fProfilePageMap.keySet();
		for (String profileId : profileIds) {
			String confProfileName = getDiscoveryProfileName(profileId);
			if (profileName.equals(confProfileName)) {
				return profileId;
			}
		}
		return null;
	}

	protected List<String> getDiscoveryProfileIdList() {
		return new ArrayList<>(fProfilePageMap.keySet());
	}

	protected abstract String getCurrentProfileId();

	/**
	 *
	 */
	public void callPerformApply() {
		try {
			new ProgressMonitorDialog(getShell()).run(false, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						performApply(monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}

			});
		} catch (InvocationTargetException e) {
			String title = MakeUIPlugin.getResourceString(ERROR_TITLE);
			String message = MakeUIPlugin.getResourceString(ERROR_MESSAGE);
			ExceptionHandler.handle(e, getShell(), title, message);
		} catch (InterruptedException e) {
			// cancelled
		}
	}

}
