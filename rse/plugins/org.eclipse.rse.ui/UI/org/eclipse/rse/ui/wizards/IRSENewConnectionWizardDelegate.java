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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.model.IHost;

/**
 * Interface for RSE new connection wizard delegate.
 */
public interface IRSENewConnectionWizardDelegate {

	public void init(RSENewConnectionWizard wizard, IRSESystemType systemType);
	
	public void systemTypeChanged(IRSESystemType systemType);
	
	public RSENewConnectionWizard getWizard();
	
	public IRSESystemType getSystemType();
	
	public void addPages();
	
	public boolean performFinish();
	
	public boolean canFinish();
	
	public IWizardPage getMainPage();
	
	public IWizardPage getNextPage(IWizardPage page);
	
	public IWizardPage getPreviousPage(IWizardPage page);
	
	public IHost getDummyHost();
}