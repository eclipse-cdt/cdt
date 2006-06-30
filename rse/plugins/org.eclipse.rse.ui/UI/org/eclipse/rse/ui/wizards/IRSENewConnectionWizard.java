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

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.ui.INewWizard;

public interface IRSENewConnectionWizard extends INewWizard {

	public static final String NEW_CONNECTION_WIZARD_DELEGATE_EXTENSION_POINT_ID = "org.eclipse.rse.ui.newConnectionWizardDelegate";
	public static final String NEW_CONNECTION_WIZARD_DELEGATE_EXTENSION_CONFIG_NAME = "newConnectionWizardDelegate";
	public static final String NEW_CONNECTION_WIZARD_DELEGATE_EXTENSION_CONFIG_ATTRIBUTE_SYSTEMTYPE = "systemType";
	public static final String NEW_CONNECTION_WIZARD_DELEGATE_EXTENSION_CONFIG_ATTRIBUTE_CLASS = "class";
	
	public IRSENewConnectionWizardDelegate getDelegate();
	
	/**
	 * Restrict system types. Users will only be able to choose from the given system types.
	 * @param systemTypes the system types to restrict to.
	 */
	public void restrictToSystemTypes(IRSESystemType[] systemTypes);
	
	/**
	 * Restrict to a single system type. Users will not be shown the system type selection page in
	 * the wizard.
	 * @param systemType the system type to restrict to.
	 */
	public void restrictToSystemType(IRSESystemType systemType);
	
	/**
	 * Sets the system type that was selected in the wizard. This will only be called if the wizard
	 * shows the system type selection page, i.e. if the wizard is not restricted to a single system type.
	 * @param systemType the system type.
	 */
	public void setSelectedSystemType(IRSESystemType systemType);
}