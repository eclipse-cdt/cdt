/********************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others. All rights reserved.
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
 ********************************************************************************/

package org.eclipse.rse.ui.wizards.newconnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemNewConnectionWizardPage;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.util.ISubSystemConfigurationAdapter;
import org.eclipse.rse.internal.ui.view.SystemPerspectiveHelpers;
import org.eclipse.rse.model.SystemStartHere;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemConnectionForm;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.messages.SystemMessageDialog;


/**
 *
 */
public class RSEDefaultNewConnectionWizard extends RSEAbstractNewConnectionWizard {

	private RSEDefaultNewConnectionWizardMainPage mainPage;
	private ISystemNewConnectionWizardPage[] subsystemFactorySuppliedWizardPages;
	private Map ssfWizardPagesPerSystemType = new Hashtable();
	private String defaultUserId;
	private String defaultConnectionName;
	private String defaultHostName;
	private String[] activeProfileNames = null;
	private int privateProfileIndex = -1;
	private ISystemProfile privateProfile = null;
	private IHost selectedContext = null;
	private static String lastProfile = null;

	/**
	 * Constructor.
	 */
	public RSEDefaultNewConnectionWizard() {
		String[] profiles = SystemStartHere.getSystemProfileManager().getActiveSystemProfileNames();
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
		subsystemFactorySuppliedWizardPages = null;
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
			subsystemFactorySuppliedWizardPages = getAdditionalWizardPages(systemType.getName());
		}
	}
	
	/**
	 * Creates the wizard pages. This method is an override from the parent Wizard class.
	 */
	public void addPages() {
		try {
			mainPage = createMainPage(getSystemType());
		
			SystemConnectionForm form = mainPage.getSystemConnectionForm();
			if (form != null) {
				form.setCurrentlySelectedConnection(selectedContext);

				if (defaultUserId != null) form.setUserId(defaultUserId);
				if (defaultConnectionName != null) form.setConnectionName(defaultConnectionName);
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
		mainPage = new RSEDefaultNewConnectionWizardMainPage(this, getPageTitle(), SystemResources.RESID_NEWCONN_PAGE1_DESCRIPTION);
		mainPage.setTitle(getPageTitle());
		mainPage.setSystemType(systemType);
		subsystemFactorySuppliedWizardPages = getAdditionalWizardPages(systemType.getName());

		return mainPage;
	}

	public String getPageTitle() {

		String pageTitle = null;

		if (getSystemType() == null) {
			pageTitle = SystemResources.RESID_NEWCONN_PAGE1_TITLE;
		} else {
			String onlySystemType = getSystemType().getLabel();

			if (onlySystemType.equals(IRSESystemType.SYSTEMTYPE_LOCAL)) {
				pageTitle = SystemResources.RESID_NEWCONN_PAGE1_LOCAL_TITLE;
			} else {
				pageTitle = SystemResources.RESID_NEWCONN_PAGE1_REMOTE_TITLE;
				pageTitle = SystemMessage.sub(pageTitle, "&1", onlySystemType); //$NON-NLS-1$
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
				ISystemProfile defaultPrivateProfile = RSEUIPlugin.getDefault().getSystemRegistry().getSystemProfileManager().getDefaultPrivateSystemProfile();
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
			for (int idx = 0; ok && (idx < subsystemFactorySuppliedWizardPages.length); idx++) {
				ok = subsystemFactorySuppliedWizardPages[idx].performFinish();
				if (!ok)
					setPageError((IWizardPage)subsystemFactorySuppliedWizardPages[idx]);
			}
		}
		if (ok) {
			boolean cursorSet = true;
			setBusyCursor(true);
			ISystemRegistry sr = RSEUIPlugin.getDefault().getSystemRegistry();

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
					String sysType = getSystemType() != null ? getSystemType().getName() : null;
					SystemConnectionForm form = mainPage.getSystemConnectionForm();
					IHost conn = sr.createHost(form.getProfileName(), sysType, form.getConnectionName(), form.getHostName(),
																			form.getConnectionDescription(), form.getDefaultUserId(), form.getUserIdLocation(),
																			subsystemFactorySuppliedWizardPages);

					setBusyCursor(false);
					cursorSet = false;

					// a tweak that is the result of UCD feedback. Phil
					if ((conn != null) && SystemPerspectiveHelpers.isRSEPerspectiveActive()) {
						if (sysType.equals(IRSESystemType.SYSTEMTYPE_ISERIES)) {
							ISubSystem[] objSubSystems = sr.getSubSystemsBySubSystemConfigurationCategory("nativefiles", conn); //$NON-NLS-1$
							if ((objSubSystems != null) && (objSubSystems.length > 0))// might be in product that doesn't have iSeries plugins
								sr.expandSubSystem(objSubSystems[0]);
							else
								sr.expandHost(conn);
						} else
							sr.expandHost(conn);
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

	/*
	 * Private method to get all the wizard pages from all the subsystem factories, given a
	 *  system type.
	 */
	protected ISystemNewConnectionWizardPage[] getAdditionalWizardPages(String systemType) {
		// this query is expensive, so only do it once...
		subsystemFactorySuppliedWizardPages = (ISystemNewConnectionWizardPage[])ssfWizardPagesPerSystemType.get(systemType);
		if (subsystemFactorySuppliedWizardPages == null) {
			// query all affected subsystems for their list of additional wizard pages...
			Vector additionalPages = new Vector();
			ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
			ISubSystemConfiguration[] factories = sr.getSubSystemConfigurationsBySystemType(systemType, true);
			for (int idx = 0; idx < factories.length; idx++) {
				ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)factories[idx].getAdapter(ISubSystemConfigurationAdapter.class);

				ISystemNewConnectionWizardPage[] pages = adapter.getNewConnectionWizardPages(factories[idx], this);
				if (pages != null) {
					for (int widx = 0; widx < pages.length; widx++) {
						if (pages[widx] instanceof IWizardPage) ((IWizardPage)pages[widx]).setWizard(this);
						additionalPages.addElement(pages[widx]);
					}
				}
			}
			subsystemFactorySuppliedWizardPages = new ISystemNewConnectionWizardPage[additionalPages.size()];
			for (int idx = 0; idx < subsystemFactorySuppliedWizardPages.length; idx++)
				subsystemFactorySuppliedWizardPages[idx] = (ISystemNewConnectionWizardPage)additionalPages.elementAt(idx);
			ssfWizardPagesPerSystemType.put(systemType, subsystemFactorySuppliedWizardPages);
		}
		return subsystemFactorySuppliedWizardPages;
	}

	/**
	 * Return true if there are additional pages. This decides whether to enable the Next button 
	 *  on the main page
	 */
	protected boolean hasAdditionalPages() {
		return (subsystemFactorySuppliedWizardPages != null) && (subsystemFactorySuppliedWizardPages.length > 0);
	}

	/**
	 * Return the first additional page to show when user presses Next on the main page
	 */
	protected ISystemNewConnectionWizardPage getFirstAdditionalPage() {
		if ((subsystemFactorySuppliedWizardPages != null) && (subsystemFactorySuppliedWizardPages.length > 0)) {
			IWizardPage previousPage = mainPage;
			for (int idx = 0; idx < subsystemFactorySuppliedWizardPages.length; idx++) {
				((IWizardPage)subsystemFactorySuppliedWizardPages[idx]).setPreviousPage(previousPage);
				previousPage = (IWizardPage)subsystemFactorySuppliedWizardPages[idx];
			}
			return subsystemFactorySuppliedWizardPages[0];
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
			if ((index == (subsystemFactorySuppliedWizardPages.length - 1)))
				// last page or page not found
				return null;
			return (IWizardPage)subsystemFactorySuppliedWizardPages[index + 1];
		}
	}

	/**
	 * @see org.eclipse.rse.ui.wizards.newconnection.RSEAbstractNewConnectionWizard#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getPreviousPage(IWizardPage page) {
		return null;
	}

	private int getAdditionalPageIndex(IWizardPage page) {
		for (int idx = 0; idx < subsystemFactorySuppliedWizardPages.length; idx++) {
			if (page == subsystemFactorySuppliedWizardPages[idx])
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
			for (int idx = 0; ok && (idx < subsystemFactorySuppliedWizardPages.length); idx++)
				ok = subsystemFactorySuppliedWizardPages[idx].isPageComplete();
		}
		return ok;
	}

}