/********************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Uwe Stieber (Wind River) - Reworked new connection wizard extension point.
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [175680] Deprecate obsolete ISystemRegistry methods
 * Uwe Stieber (Wind River) - [192202] Default RSE new connection wizard does not allow to query created host instance anymore
 * Martin Oberhuber (Wind River) - [cleanup] Avoid using SystemStartHere in production code
 * David Dykstal (IBM) - [168976][api] move ISystemNewConnectionWizardPage from core to UI
 * Martin Oberhuber (Wind River) - [218304] Improve deferred adapter loading
 * David McKnight   (IBM)        - [243332] Removing wizard page caused subsystem to be removed
 ********************************************************************************/

package org.eclipse.rse.ui.wizards.newconnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISubSystemConfigurator;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.view.SystemPerspectiveHelpers;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemConnectionForm;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter;

/**
 * Standard RSE new connection wizard implementation.
 */
public class RSEDefaultNewConnectionWizard extends RSEAbstractNewConnectionWizard {

	private RSEDefaultNewConnectionWizardMainPage mainPage;
	private ISystemNewConnectionWizardPage[] subsystemConfigurationSuppliedWizardPages;

	private Map ssfWizardPagesPerSystemType = new Hashtable();
	private String defaultUserId;
	private String defaultConnectionName;
	private String defaultHostName;
	private String[] activeProfileNames = null;
	private int privateProfileIndex = -1;
	private ISystemProfile privateProfile = null;
	private IHost selectedContext = null;
	private static String lastProfile = null;
	private IHost createdHost = null;

	/**
	 * Constructor.
	 */
	public RSEDefaultNewConnectionWizard() {
		String[] profiles = RSECorePlugin.getTheSystemProfileManager().getActiveSystemProfileNames();
		// normalize the profiles by sorting our null or empty profile names
		List normalized = new LinkedList();
		for (int i = 0; i < profiles.length; i++) {
			if (profiles[i] != null && !"".equals(profiles[i].trim())) normalized.add(profiles[i]); //$NON-NLS-1$
		}
		activeProfileNames = (String[])normalized.toArray(new String[normalized.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#dispose()
	 */
	public void dispose() {
		super.dispose();

		mainPage = null;
		subsystemConfigurationSuppliedWizardPages = null;
		ssfWizardPagesPerSystemType.clear();
		defaultUserId = null;
		defaultHostName = null;
		defaultConnectionName = null;
		activeProfileNames = null;
		privateProfileIndex = -1;
		privateProfile = null;
		selectedContext = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.AbstractNewConnectionWizard#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		if (mainPage != null && getSystemType() != null) {
			IRSESystemType systemType = getSystemType();
			mainPage.setTitle(getPageTitle());
			mainPage.setSystemType(systemType);
			subsystemConfigurationSuppliedWizardPages = getAdditionalWizardPages(systemType);
		}
	}

	/**
	 * Creates the wizard pages. This method is an override from the parent Wizard class.
	 */
	public void addPages() {
		try {
			// reset the remembered created host instance
			createdHost = null;

			mainPage = createMainPage(getSystemType());

			SystemConnectionForm form = mainPage.getSystemConnectionForm();
			if (form != null) {
				form.setCurrentlySelectedConnection(selectedContext);

				if (defaultUserId != null) form.setUserId(defaultUserId);

				// bugzilla#175153: setCurrentlySelectedConnection is filling in the name from the selected
				// connection. As this is not wanted, set the connection name field always to be empty, except
				// there had been a default connection name explicitly set from outside.
				if (defaultConnectionName != null) form.setConnectionName(defaultConnectionName);
				else form.setConnectionName(""); //$NON-NLS-1$

				if (defaultHostName != null) form.setHostName(defaultHostName);
			}

			if (mainPage != null && getSystemType() != null) mainPage.setSystemType(getSystemType());

			updateDefaultSelectedProfile();

			addPage(mainPage);
		} catch (Exception exc) {
			SystemBasePlugin.logError("New connection: Error in createPages: ", exc); //$NON-NLS-1$
		}
	}

	/**
	 * Creates the wizard's main page. This method is an override from the parent class.
	 */
	protected RSEDefaultNewConnectionWizardMainPage createMainPage(IRSESystemType systemType) {
		if (mainPage == null) {
			mainPage = new RSEDefaultNewConnectionWizardMainPage(this, getPageTitle(), SystemResources.RESID_NEWCONN_PAGE1_DESCRIPTION);
			mainPage.setTitle(getPageTitle());
			mainPage.setSystemType(systemType);
			subsystemConfigurationSuppliedWizardPages = systemType != null ? getAdditionalWizardPages(systemType) : null;
		}

		return mainPage;
	}

	public String getPageTitle() {

		String pageTitle = null;

		if (getSystemType() == null) {
			pageTitle = SystemResources.RESID_NEWCONN_PAGE1_TITLE;
		} else {
			IRSESystemType onlySystemType = getSystemType();

			if (onlySystemType.isLocal()) {
				pageTitle = SystemResources.RESID_NEWCONN_PAGE1_LOCAL_TITLE;
			} else {
				pageTitle = SystemResources.RESID_NEWCONN_PAGE1_REMOTE_TITLE;
				pageTitle = SystemMessage.sub(pageTitle, "&1", onlySystemType.getLabel()); //$NON-NLS-1$
			}
		}

		return pageTitle;
	}

	/**
	 * Calculates the default profile name to propose on the default new
	 * connection wizard main page.
	 *
	 * <pre>
	 * Expected order of default profile selection:
	 *   1. If a connection is selected, the default profile is the one from the connection.
	 *   2. If the wizard remembered the last profile and this last remembered profile is still
	 *      available and active, the remembered last profile is the default profile.
	 *   3. If the default private system profile is availabe and active, the default private system profile
	 *      is the default profile.
	 *   4. The first non-empty profile from the list of active profiles is the default profile.
	 *
	 *   In case a profile name is not in the list of currently active profiles, the logic will
	 *   fall trough to the next lower level.
	 * </pre>
	 */
	protected void updateDefaultSelectedProfile() {
		if (mainPage == null) return;

		List profileNames = activeProfileNames != null ? Arrays.asList(activeProfileNames) : new ArrayList();
		mainPage.getSystemConnectionForm().setProfileNames(activeProfileNames);

		// 1. If a connection is selected, the default profile is the one from the connection.
		String defaultProfileName = selectedContext != null ? selectedContext.getSystemProfileName() : null;
		if (defaultProfileName == null || !profileNames.contains(defaultProfileName)) {
			// 3. If the wizard is invoked the 2nd time and a last profile is remembered, the last
			//    profile is the default profile.
			if (lastProfile != null && "".equals(lastProfile)) lastProfile = null; //$NON-NLS-1$
			defaultProfileName = lastProfile;
			if (defaultProfileName == null || !profileNames.contains(defaultProfileName)) {
				// 2. If the wizard is invoked the 1st time, the default private system profile is the
				//    default profile.
				ISystemProfile defaultPrivateProfile = RSECorePlugin.getTheSystemRegistry().getSystemProfileManager().getDefaultPrivateSystemProfile();
				if (defaultPrivateProfile != null) defaultProfileName = defaultPrivateProfile.getName();
				if (defaultProfileName == null || !profileNames.contains(defaultProfileName)) {
					// 4. The first non-empty profile from the list of active profiles is the default profile.
					//    Note: The profile names get normalized within the constructor.
					if (profileNames.size() > 0) defaultProfileName = (String)profileNames.get(0);
				}
			}
		}

		// set the default profile to the page and remember it.
		if (defaultProfileName != null) {
			mainPage.getSystemConnectionForm().setProfileNamePreSelection(defaultProfileName);
			// do not update the last selected profile marker if the default profile
			// name came for the selected context.
			if (selectedContext == null || !defaultProfileName.equals(selectedContext.getSystemProfileName()))
				lastProfile = defaultProfileName;
		}

	}

	/**
	 * Set the currently selected context. Used to better default entry fields.
	 */
	public void setSelectedContext(IHost selectedContext) {
		this.selectedContext = selectedContext;
		updateDefaultSelectedProfile();
	}

	/**
	 * For "new" mode, allows setting of the initial user Id. Sometimes subsystems
	 *  like to have their own default userId preference page option. If so, query
	 *  it and set it here by calling this.
	 */
	public void setUserId(String userId) {
		defaultUserId = userId;
		if (mainPage != null) mainPage.getSystemConnectionForm().setUserId(userId);
	}

	/**
	 * Preset the connection name
	 */
	public void setConnectionName(String name) {
		defaultConnectionName = name;
		if (mainPage != null) mainPage.getSystemConnectionForm().setConnectionName(name);
	}

	/**
	 * Preset the host name
	 */
	public void setHostName(String name) {
		defaultHostName = name;
		if (mainPage != null) mainPage.getSystemConnectionForm().setHostName(name);
	}

	/**
	 * Set's an error message to the wizard if a page, which is not the current page
	 * is having a page error.
	 */
	private void setPageError(IWizardPage page) {
		IWizardPage currentPage = getContainer().getCurrentPage();
		if (currentPage != page) {
			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_WIZARD_PAGE_ERROR);
			if (currentPage instanceof WizardPage)
				((WizardPage)currentPage).setErrorMessage(msg.getLevelOneText());
		}
	}
	
	private ISubSystemConfigurator[] getSubSystemConfigurators()
	{		
		// what kind of subsystems do we have here?
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		IRSESystemType systemType = getSystemType();
		ISubSystemConfiguration[] configurations = sr.getSubSystemConfigurationsBySystemType(systemType, true);
		
		ArrayList configList = new ArrayList();
		for (int i = 0; i < configurations.length; i++){
			ISubSystemConfiguration configuration = configurations[i];
			ISubSystemConfigurator firstMatch = null;
			for (int j = 0; j < subsystemConfigurationSuppliedWizardPages.length; j++){
				ISystemNewConnectionWizardPage page = subsystemConfigurationSuppliedWizardPages[j];
				ISubSystemConfiguration pageConfiguration = page.getSubSystemConfiguration();
				if (configuration == pageConfiguration){ // found a match
					configList.add(page);  // could be more than one
					if (firstMatch == null){
						firstMatch = page;
					}
				}
			}
			if (firstMatch == null){ // no match found so need to provide alternative
				class DefaultConfigurator implements ISubSystemConfigurator {
					private ISubSystemConfiguration _configuration;
					public DefaultConfigurator(ISubSystemConfiguration configuration){
						_configuration = configuration;
					}
					
					public boolean applyValues(ISubSystem ss) {
						return true;
					}

					public ISubSystemConfiguration getSubSystemConfiguration() {
						return _configuration;
					}						
				}
				configList.add(new DefaultConfigurator(configuration));
			}				
		}	
		return (ISubSystemConfigurator[])configList.toArray(new ISubSystemConfigurator[configList.size()]);

	}

	/**
	 * Completes processing of the wizard. If this
	 * method returns true, the wizard will close;
	 * otherwise, it will stay active.
	 * This method is an override from the parent Wizard class.
	 *
	 * @return whether the wizard finished successfully
	 */
	public boolean performFinish() {
		boolean ok = mainPage.getSystemConnectionForm().verify(true);
		if (!ok)
			setPageError(mainPage);
		else if (ok && hasAdditionalPages()) {
			for (int idx = 0; ok && (idx < subsystemConfigurationSuppliedWizardPages.length); idx++) {
				ok = subsystemConfigurationSuppliedWizardPages[idx].performFinish();
				if (!ok)
					setPageError((IWizardPage)subsystemConfigurationSuppliedWizardPages[idx]);
			}
		}
		if (ok) {
			boolean cursorSet = true;
			setBusyCursor(true);
			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();

			// if private profile is not null, then we have to rename the private profile
			// with the new profile name
			if (privateProfile != null) {
				try {
					String newName = activeProfileNames[privateProfileIndex];
					sr.renameSystemProfile(privateProfile, newName);
				} catch (SystemMessageException exc) {
					SystemMessageDialog.displayMessage(getShell(), exc);

					ok = false;
				} catch (Exception exc) {
					setBusyCursor(false);
					cursorSet = false;
					String msg = "Exception renaming profile "; //$NON-NLS-1$
					SystemBasePlugin.logError(msg, exc);
					SystemMessageDialog.displayExceptionMessage(getShell(), exc);
					ok = false;
				}
			}

			if (ok) {
				try {
					
					ISubSystemConfigurator[] configurators = getSubSystemConfigurators();
					
					IRSESystemType systemType = getSystemType();
					SystemConnectionForm form = mainPage.getSystemConnectionForm();
					createdHost = sr.createHost(form.getProfileName(), systemType, form.getConnectionName(), form.getHostName(),
																			form.getConnectionDescription(), form.getDefaultUserId(), form.getUserIdLocation(),
																			configurators);

					setBusyCursor(false);
					cursorSet = false;

					// a tweak that is the result of UCD feedback. Phil
					if ((createdHost != null) && SystemPerspectiveHelpers.isRSEPerspectiveActive()) {
						if (systemType.getId().equals(IRSESystemType.SYSTEMTYPE_ISERIES_ID)) {
							ISubSystem[] objSubSystems = sr.getSubSystemsBySubSystemConfigurationCategory("nativefiles", createdHost); //$NON-NLS-1$
							if ((objSubSystems != null) && (objSubSystems.length > 0))// might be in product that doesn't have iSeries plugins
								RSEUIPlugin.getTheSystemRegistryUI().expandSubSystem(objSubSystems[0]);
							else
								RSEUIPlugin.getTheSystemRegistryUI().expandHost(createdHost);
						} else
							RSEUIPlugin.getTheSystemRegistryUI().expandHost(createdHost);
					}

					lastProfile = form.getProfileName();
				} catch (Exception exc) {
					if (cursorSet)
						setBusyCursor(false);
					cursorSet = false;
					String msg = "Exception creating connection "; //$NON-NLS-1$
					SystemBasePlugin.logError(msg, exc);
					SystemMessageDialog.displayExceptionMessage(getShell(), exc);
					ok = false;
				}
			}
			//getShell().setCursor(null);
			//busyCursor.dispose();
			if (cursorSet)
				setBusyCursor(false);
			return ok;
		}
		return ok;
	}

	/**
	 * Returns the create host instance once the user pressed finished. The created
	 * host instance will be reset to <code>null</code> once the wizard got disposed.
	 *
	 * @return The created host instance or <code>null</code>.
	 */
	public IHost getCreatedHost() {
		return createdHost;
	}

	/**
	 * Private method to get all the wizard pages from all the subsystem factories, given a
	 * system type.
	 *
	 * @param systemType The system type to query the additional subsystem service pages for. Must be not <code>null</code>.
	 */
	private ISystemNewConnectionWizardPage[] getAdditionalWizardPages(IRSESystemType systemType) {
		assert systemType != null;
		// this query is expensive, so only do it once...
		subsystemConfigurationSuppliedWizardPages = (ISystemNewConnectionWizardPage[])ssfWizardPagesPerSystemType.get(systemType);
		if (subsystemConfigurationSuppliedWizardPages == null) {
			// query all affected subsystems for their list of additional wizard pages...
			Vector additionalPages = new Vector();
			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
			ISubSystemConfiguration[] factories = sr.getSubSystemConfigurationsBySystemType(systemType, true);
			for (int idx = 0; idx < factories.length; idx++) {
				ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)factories[idx].getAdapter(ISubSystemConfigurationAdapter.class);
				if (adapter == null) {
					// try to activate bundle - FIXME should perhaps be done
					// earlier in the action that shows the wizard dialog?
					// And, is it really necessary to get the wizard pages that
					// early already?
					Platform.getAdapterManager().loadAdapter(factories[idx], ISubSystemConfigurationAdapter.class.getName());
					adapter = (ISubSystemConfigurationAdapter) factories[idx].getAdapter(ISubSystemConfigurationAdapter.class);
				}

				ISystemNewConnectionWizardPage[] pages = adapter.getNewConnectionWizardPages(factories[idx], this);
				if (pages != null) {
					for (int widx = 0; widx < pages.length; widx++) {
						if (pages[widx] instanceof IWizardPage){
							((IWizardPage)pages[widx]).setWizard(this);
						}
						
						additionalPages.addElement(pages[widx]);
					}
				}
			}
			
			subsystemConfigurationSuppliedWizardPages = (ISystemNewConnectionWizardPage[])additionalPages.toArray(new ISystemNewConnectionWizardPage[additionalPages.size()]);
			
			ssfWizardPagesPerSystemType.put(systemType, subsystemConfigurationSuppliedWizardPages);
		}
		return subsystemConfigurationSuppliedWizardPages;
	}

	/**
	 * Return true if there are additional pages. This decides whether to enable the Next button
	 *  on the main page
	 */
	protected boolean hasAdditionalPages() {
		return (subsystemConfigurationSuppliedWizardPages != null) && (subsystemConfigurationSuppliedWizardPages.length > 0);
	}

	/**
	 * Return the first additional page to show when user presses Next on the
	 * main page. In RSE 3.0, the ISystemNewConnectionWizardPage return type was
	 * moved from org.eclipse.rse.core into a UI plugin.
	 * 
	 * @since 3.0
	 */
	protected ISystemNewConnectionWizardPage getFirstAdditionalPage() {
		if ((subsystemConfigurationSuppliedWizardPages != null) && (subsystemConfigurationSuppliedWizardPages.length > 0)) {
			IWizardPage previousPage = mainPage;
			for (int idx = 0; idx < subsystemConfigurationSuppliedWizardPages.length; idx++) {
				((IWizardPage)subsystemConfigurationSuppliedWizardPages[idx]).setPreviousPage(previousPage);
				previousPage = (IWizardPage)subsystemConfigurationSuppliedWizardPages[idx];
			}
			return subsystemConfigurationSuppliedWizardPages[0];
		} else
			return null;
	}

	// --------------------
	// PARENT INTERCEPTS...
	// --------------------

	/**
	 * Intercept of Wizard method so we can get the Next button behaviour to work right for the
	 *  dynamically managed additional wizard pages.
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		if (!hasAdditionalPages())
			return null;
		else {
			int index = getAdditionalPageIndex(page);
			if ((index == (subsystemConfigurationSuppliedWizardPages.length - 1)))
				// last page or page not found
				return null;
			return (IWizardPage)subsystemConfigurationSuppliedWizardPages[index + 1];
		}
	}

	/**
	 * @see org.eclipse.rse.ui.wizards.newconnection.RSEAbstractNewConnectionWizard#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getPreviousPage(IWizardPage page) {
		return null;
	}

	private int getAdditionalPageIndex(IWizardPage page) {
		for (int idx = 0; idx < subsystemConfigurationSuppliedWizardPages.length; idx++) {
			if (page == subsystemConfigurationSuppliedWizardPages[idx])
				return idx;
		}
		return -1;
	}

	/**
	 * Intercept of Wizard method so we can take into account our additional pages
	 */
	public boolean canFinish() {
		boolean ok = mainPage.isPageComplete();

		if (ok && hasAdditionalPages()) {
			for (int idx = 0; ok && (idx < subsystemConfigurationSuppliedWizardPages.length); idx++)
				ok = subsystemConfigurationSuppliedWizardPages[idx].isPageComplete();
		}
		return ok;
	}

}