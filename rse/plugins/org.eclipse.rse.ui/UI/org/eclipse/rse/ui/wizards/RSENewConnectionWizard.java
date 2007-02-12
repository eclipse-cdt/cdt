/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others. All rights reserved.
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
 * Javier Montalvo Orús (Symbian) - Bug 158555 - newConnectionWizardDelegates can only be used once
 * Uwe Stieber (Wind River) - Support newConnectionWizardDelegates registration with dynamically registered system types
 ********************************************************************************/

package org.eclipse.rse.ui.wizards;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;

/**
 * The New Connection wizard. This wizard allows users to create new RSE connections.
 */
public class RSENewConnectionWizard extends AbstractSystemWizard implements IRSENewConnectionWizard {

	private final Map wizardDelegates = new HashMap();
	private IRSENewConnectionWizardDelegate currentWizardDelegate;
	
	private RSENewConnectionWizardMainPage mainPage;
	private IRSESystemType[] restrictedSystemTypes;
	private boolean onlySystemType;
	
	private IRSENewConnectionWizardDelegate defaultDelegate;
	private boolean defaultDelegateCreated;

	/**
	 * Constructor.
	 */
	public RSENewConnectionWizard() {
		super(SystemResources.RESID_NEWCONN_TITLE, RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWCONNECTIONWIZARD_ID));
		readWizardDelegateExtensions();
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizard#restrictToSystemType(org.eclipse.rse.core.IRSESystemType)
	 */
	public void restrictToSystemType(IRSESystemType systemType) {
		IRSESystemType[] types = new IRSESystemType[1];
		types[0] = systemType;
		restrictToSystemTypes(types);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizard#restrictToSystemTypes(org.eclipse.rse.core.IRSESystemType[])
	 */
	public void restrictToSystemTypes(IRSESystemType[] systemTypes) {
		this.restrictedSystemTypes = systemTypes;

		if (systemTypes.length == 1) {
			this.onlySystemType = true;
		} else {
			this.onlySystemType = false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizard#setSelectedSystemType(org.eclipse.rse.core.IRSESystemType)
	 */
	public void setSelectedSystemType(IRSESystemType systemType) {
		setDelegate(systemType);
	}

	/**
	 * Reads the newConnectionWizardDelegates extension point
	 */
	private void readWizardDelegateExtensions() {
		assert wizardDelegates != null;
		
		wizardDelegates.clear();
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(NEW_CONNECTION_WIZARD_DELEGATE_EXTENSION_POINT_ID);

		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];

			if (element.getName().equals(NEW_CONNECTION_WIZARD_DELEGATE_EXTENSION_CONFIG_NAME)) {
				Object obj = null;
				try {
					obj = element.createExecutableExtension(NEW_CONNECTION_WIZARD_DELEGATE_EXTENSION_CONFIG_ATTRIBUTE_CLASS);
					if (obj instanceof IRSENewConnectionWizardDelegate) {
						String systemTypeId = element.getAttribute(NEW_CONNECTION_WIZARD_DELEGATE_EXTENSION_CONFIG_ATTRIBUTE_SYSTEMTYPE);
						
						// if the systemTypeId is null or empty, ask the system types if they accept the newConnectionWizardDelegate
						if (systemTypeId != null && !"".equals(systemTypeId.trim())) { //$NON-NLS-1$
							if (!wizardDelegates.containsKey(systemTypeId)) wizardDelegates.put(systemTypeId, obj);
						} else {
							IRSESystemType[] systemTypes = RSECorePlugin.getDefault().getRegistry().getSystemTypes();
							for (int j = 0; j < systemTypes.length; j++) {
								IRSESystemType systemType = systemTypes[j];
								if (systemType.acceptNewConnectionWizardDelegate(element.getAttribute(NEW_CONNECTION_WIZARD_DELEGATE_EXTENSION_CONFIG_ATTRIBUTE_ID))
										&& !wizardDelegates.containsKey(systemType.getId())) {
									wizardDelegates.put(systemType.getId(), obj);
								}
							}
						}
					} else {
						continue;
					}
				} catch (CoreException e) {
					SystemBasePlugin.logError("Class " + obj + " is not executable extension", e); //$NON-NLS-1$ //$NON-NLS-2$
					continue;
				}
			}
		}
	}

	/**
	 * Get the new connection wizard delegate for the system type.
	 * @param systemType the system type for which we need the delegate.
	 */
	private IRSENewConnectionWizardDelegate setDelegate(IRSESystemType systemType) {
		currentWizardDelegate = (IRSENewConnectionWizardDelegate)(wizardDelegates.get(systemType.getId()));

		// For system types where we don't have a registered wizard delegate, use the default
		// wizard delegate implementation.
		if (currentWizardDelegate == null) {
			if (!defaultDelegateCreated) {
				defaultDelegate = new RSEDefaultNewConnectionWizardDelegate();
				defaultDelegateCreated = true;
			}

			currentWizardDelegate = defaultDelegate;
		}

		// initialize with wizard and system type if not initialized before
		if (!currentWizardDelegate.isInitialized()) {
			currentWizardDelegate.init(this, systemType);
		} else {
			currentWizardDelegate.systemTypeChanged(systemType);
		}

		return currentWizardDelegate;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizard#getDelegate()
	 */
	public IRSENewConnectionWizardDelegate getDelegate() {
		return currentWizardDelegate;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.AbstractSystemWizard#addPages()
	 */
	public void addPages() {
		if (!onlySystemType) {
			mainPage = new RSENewConnectionWizardMainPage(this, "Select System Type", "Select a system type");

			if (restrictedSystemTypes != null) {
				mainPage.restrictToSystemTypes(restrictedSystemTypes);
			}
			addPage(mainPage);
		} else {
			setDelegate(restrictedSystemTypes[0]);
			addPage(currentWizardDelegate.getMainPage());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	public boolean canFinish() {
		boolean result = true;

		if (mainPage != null) {
			result = mainPage.isPageComplete();
		}

		if (result) {
			if (currentWizardDelegate != null) {
				result = currentWizardDelegate.canFinish();
			} else {
				// we do not allow wizard to complete if the delegate is not yet available
				result = false;
			}
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {

		if (mainPage != null && page == mainPage) {
			return super.getNextPage(page);
		} else {

			if (currentWizardDelegate != null) {

				IWizardPage nextPage = currentWizardDelegate.getNextPage(page);

				if (nextPage == null) {
					return super.getNextPage(page);
				} else {
					return nextPage;
				}
			} else {
				return super.getNextPage(page);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (mainPage != null && page == mainPage) {
			return super.getPreviousPage(page);
		} else {
			if (currentWizardDelegate != null) {
				IWizardPage prevPage = currentWizardDelegate.getPreviousPage(page);

				if (prevPage == null) {
					return super.getPreviousPage(page);
				} else {
					return prevPage;
				}
			} else {
				return super.getPreviousPage(page);
			}
		}
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {

		boolean result = true;

		if (mainPage != null) {
			result = mainPage.performFinish();
		}

		if (result) {
			if (currentWizardDelegate != null) {
				result = currentWizardDelegate.performFinish();
			}
		}

		return result;
	}

}