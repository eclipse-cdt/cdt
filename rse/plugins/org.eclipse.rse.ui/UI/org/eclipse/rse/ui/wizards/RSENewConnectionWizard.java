/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.wizards;

import java.util.HashMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;

/**
 *
 */
public class RSENewConnectionWizard extends AbstractSystemWizard implements IRSENewConnectionWizard {
	
	private HashMap map;
	private IRSESystemType systemType;
	private IRSENewConnectionWizardDelegate delegate;
	private RSENewConnectionWizardMainPage mainPage;

	/**
	 * Constructor.
	 */
	public RSENewConnectionWizard() {
		super(SystemResources.RESID_NEWCONN_TITLE, RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWCONNECTIONWIZARD_ID));
		readWizardDelegateExtensions();
		setForcePreviousAndNextButtons(true);
	    setNeedsProgressMonitor(true);
	}
	
	/**
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizard#setSystemType(org.eclipse.rse.core.IRSESystemType)
	 */
	public void setSystemType(IRSESystemType systemType) {
		this.systemType = systemType;
		getDelegate(systemType);
	}

	/**
	 *
	 */
	private void readWizardDelegateExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(NEW_CONNECTION_WIZARD_DELEGATE_EXTENSION_POINT_ID);
	}
	
	/**
	 * Get the new connection wizard delegate for the system type.
	 * @param systemType the system type for which we need the delegate.
	 */
	public IRSENewConnectionWizardDelegate getDelegate(IRSESystemType systemType) {
		
		if (map != null) {
			delegate = (IRSENewConnectionWizardDelegate)(map.get(systemType.getId()));
		}
		
		if (delegate == null) {
			delegate = new RSEDefaultNewConnectionWizardDelegate();
		}
		
		// set the wizard
		delegate.init(this, systemType);
		
		return delegate;
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		mainPage = new RSENewConnectionWizardMainPage(this, "Select System Type", "Select a system type");
		addPage(mainPage);
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	public boolean canFinish() {
		
		boolean result = mainPage.isPageComplete();
		
		if (result) {
			
			if (delegate != null) {
				result = delegate.canFinish();
			}
			// we do not allow wizard to complete if the delegate is not yet available
			else {
				result = false;
			}
		}
		
		return result;
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		
		if (page == mainPage) {
			return super.getNextPage(page);
		}
		else {
			
			if (delegate != null) {
			
				IWizardPage nextPage = delegate.getNextPage(page);
		
				if (nextPage == null) {
					return super.getNextPage(page);
				}
				else {
					return nextPage;
				}
			}
			else {
				return super.getNextPage(page);
			}
		}
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getPreviousPage(IWizardPage page) {
		
		if (page == mainPage) {
			return super.getPreviousPage(page);
		}
		else {
			
			if (delegate != null) {
				IWizardPage prevPage = delegate.getPreviousPage(page);
		
				if (prevPage == null) {
					return super.getPreviousPage(page);
				}
				else {
					return prevPage;
				}
			}
			else {
				return super.getPreviousPage(page);
			}
		}
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		
		boolean result = mainPage.performFinish();
		
		if (result) {
			
			if (delegate != null) {
				result = delegate.performFinish();
			}
		}
		
		return result;
	}
}