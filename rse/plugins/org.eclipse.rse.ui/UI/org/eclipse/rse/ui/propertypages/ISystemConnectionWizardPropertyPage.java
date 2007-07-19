/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [174789] [performance] Don't contribute Property Pages to Wizard automatically
 ********************************************************************************/

package org.eclipse.rse.ui.propertypages;


import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.ui.view.SubSystemConfigurationAdapter;

/**
 * interface for a property page that can be shown in the new connection wizard.
 * 
 * @deprecated this class will likely be removed in the future, because the 
 *     underlying mechanism of contributing property pages to a wizard 
 *     does not scale since it activates unrelated plug-ins. Custom wizard
 *     pages should be contributed through 
 *     {@link SubSystemConfigurationAdapter#getNewConnectionWizardPages(ISubSystemConfiguration, IWizard)}
 *     instead. See also Eclipse Bugzilla bug 197129.  
 */
public interface ISystemConnectionWizardPropertyPage 
{
	public boolean applyValues(IConnectorService subsystem);
	public void setSubSystemConfiguration(ISubSystemConfiguration factory);
	public void setHostname(String hostname);
	public void setSystemType(IRSESystemType systemType);
}