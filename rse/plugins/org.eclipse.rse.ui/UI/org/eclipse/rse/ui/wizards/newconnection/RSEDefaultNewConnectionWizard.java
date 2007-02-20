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

import java.util.Arrays;
import java.util.Hashtable;
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
	private IHost currentlySelectedConnection = null;
	private static String lastProfile = null;

	/**
	 * Constructor.
	 */
	public RSEDefaultNewConnectionWizard() {
		activeProfileNames = SystemStartHere.getSystemProfileManager().getActiveSystemProfileNames();
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
		currentlySelectedConnection = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.AbstractNewConnectionWizard#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		if (mainPage != null && getSystemType() != null) {
			mainPage.restrictSystemType(getSystemType().getName());
			mainPage.setTitle(getPageTitle());
			systemTypeSelected(getSystemType().getName(), true);
		}
	}

	/**
	 * Creates the wizard pages. This method is an override from the parent Wizard class.
	 */
	public void addPages() {
		try {
			mainPage = createMainPage(getSystemType());
			mainPage.setConnectionNameValidators(SystemConnectionForm.getConnectionNameValidators());
			mainPage.setCurrentlySelectedConnection(currentlySelectedConnection);

			if (defaultUserId != null)
				mainPage.setUserId(defaultUserId);
			if (defaultConnectionName != null)
				mainPage.setConnectionName(defaultConnectionName);
			if (defaultHostName != null)
				mainPage.setHostName(defaultHostName);

			if (mainPage != null && getSystemType() != null)
				mainPage.restrictSystemType(getSystemType().getName());

			String defaultProfileName = RSEUIPlugin.getDefault().getSystemRegistry().getSystemProfileManager().getDefaultPrivateSystemProfile().getName();

			mainPage.setProfileNames(activeProfileNames);
			// if there is no connection currently selected, default the profile to
			// place the new connection into to be the first of:
			// 1. the profile the last connection was created in, in this session
			// 3. the default profile.
			if (currentlySelectedConnection == null) {
				if (lastProfile != null && "".equals(lastProfile))lastProfile = null; //$NON-NLS-1$
				if (lastProfile == null && activeProfileNames != null) {
					List profileNames = Arrays.asList(activeProfileNames);
					if (defaultProfileName != null && profileNames.contains(defaultProfileName))
						lastProfile = defaultProfileName;

					// find the first non empty profile if any.
					for (int i = 0; i < activeProfileNames.length && lastProfile == null; i++) {
						if (!"".equals(activeProfileNames[i])) { //$NON-NLS-1$
							lastProfile = activeProfileNames[i];
						}
					}
				}

				if (lastProfile != null)
					mainPage.setProfileNamePreSelection(lastProfile);
			}

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
		return mainPage;
	}

	public String getPageTitle() {

		String pageTitle = null;

		if (getSystemType() == null) {
			pageTitle = SystemResources.RESID_NEWCONN_PAGE1_TITLE;
		} else {
			String onlySystemType = getSystemType().getName();

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
	 * Set the currently selected connection. Used to better default entry fields.
	 */
	public void setCurrentlySelectedConnection(IHost conn) {
		this.currentlySelectedConnection = conn;
	}

	/**
	 * For "new" mode, allows setting of the initial user Id. Sometimes subsystems
	 *  like to have their own default userId preference page option. If so, query
	 *  it and set it here by calling this.
	 */
	public void setUserId(String userId) {
		defaultUserId = userId;
		if (mainPage != null)
			mainPage.setUserId(userId);
	}

	/**
	 * Preset the connection name
	 */
	public void setConnectionName(String name) {
		defaultConnectionName = name;
		if (mainPage != null)
			mainPage.setConnectionName(name);
	}

	/**
	 * Preset the host name
	 */
	public void setHostName(String name) {
		defaultHostName = name;
		if (mainPage != null)
			mainPage.setHostName(name);
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
		boolean ok = mainPage.performFinish();
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
					IHost conn = sr.createHost(mainPage.getProfileName(), sysType, mainPage.getConnectionName(), mainPage.getHostName(),
																			mainPage.getConnectionDescription(), mainPage.getDefaultUserId(), mainPage.getDefaultUserIdLocation(),
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

					lastProfile = mainPage.getProfileName();
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

	// callbacks from rename page

	/**
	 * Set the new profile name specified on the rename profile page...
	 */
	protected void setNewPrivateProfileName(String newName) {
		activeProfileNames[privateProfileIndex] = newName;
		if (mainPage != null) {
			mainPage.setProfileNames(activeProfileNames);
			mainPage.setProfileNamePreSelection(newName);
		}
	}

	/**
	 * Return the main page of this wizard
	 */
	public IWizardPage getMainPage() {
		if (mainPage == null) {
			addPages();
		}

		return mainPage;
	}

	/**
	 * Return the form of the main page of this wizard
	 */
	//    public SystemConnectionForm getMainPageForm()
	//    {
	//    	return (mainPage).getForm();
	//    }
	// ----------------------------------------
	// CALLBACKS FROM SYSTEM CONNECTION PAGE...
	// ----------------------------------------
	/**
	 * Event: the user has selected a system type.
	 */
	public void systemTypeSelected(String systemType, boolean duringInitialization) {
		subsystemFactorySuppliedWizardPages = getAdditionalWizardPages(systemType);
		if (!duringInitialization)
			getContainer().updateButtons();
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
					for (int widx = 0; widx < pages.length; widx++)
						additionalPages.addElement(pages[widx]);
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