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
 * David McKnight   (IBM)        - [197129] Removing obsolete  ISystemConnectionWizardPropertyPage and SystemSubSystemsPropertiesWizardPage
 ********************************************************************************/

package org.eclipse.rse.internal.ui.propertypages;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.comm.SystemKeystoreProviderManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.ui.widgets.ServerConnectionSecurityForm;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.propertypages.SystemBasePropertyPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public class ServerConnectionSecurityPropertyPage extends SystemBasePropertyPage 
{


	private ServerConnectionSecurityForm _form;

	public ServerConnectionSecurityPropertyPage()
	{
		super();
	}

	/**
	 * @see org.eclipse.rse.ui.propertypages.SystemBasePropertyPage#createContentArea(Composite)
	 */
	protected Control createContentArea(Composite parent)
	{
		// Create property page UI
		_form = new ServerConnectionSecurityForm(getShell(), getMessageLine());
		_form.createContents(parent);

		initialize();
		SystemWidgetHelpers.setCompositeHelp(parent, RSEUIPlugin.HELPPREFIX + "ssls0001"); //$NON-NLS-1$
		
		return parent;
	}

	/**
	 * @see org.eclipse.rse.ui.propertypages.SystemBasePropertyPage#verifyPageContents()
	 */
	protected boolean verifyPageContents()
	{
		return true;
	}

	private void initialize()
	{
		IPreferenceStore prefStore = RSEUIPlugin.getDefault().getPreferenceStore();

		boolean alertSSL = prefStore.getBoolean(ISystemPreferencesConstants.ALERT_SSL);
		boolean alertNonSSL = prefStore.getBoolean(ISystemPreferencesConstants.ALERT_NONSSL);

		// enable/disable as appropriate
		_form.setAlertSSL(alertSSL);
		_form.setAlertNonSSL(alertNonSSL);	
		
		// if there is no keystore provider then this needs to be disabled
		boolean hasProvider = SystemKeystoreProviderManager.getInstance().hasProvider();
		if (!hasProvider)
		{
			_form.disable();
		}
		else
		{
			_form.enable();
		}
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk()
	{
		Object subsystem = getElement();

		if (subsystem instanceof ISubSystem)
		{
			return applyValues(((ISubSystem)subsystem).getConnectorService());
		}
		return true;
	}

	public void setSubSystemConfiguration(ISubSystemConfiguration factory)
	{
	}

	public boolean applyValues(IConnectorService connectorService)
	{
		boolean alertSSL = _form.getAlertSSL();
		boolean alertNonSSL = _form.getAlertNonSSL();
		IPreferenceStore prefStore = RSEUIPlugin.getDefault().getPreferenceStore();
		prefStore.setValue(ISystemPreferencesConstants.ALERT_SSL, alertSSL);
		prefStore.setValue(ISystemPreferencesConstants.ALERT_NONSSL, alertNonSSL);
		return true;
	}
	
	public void setHostname(String name)
	{		
	}

	public void setSystemType(IRSESystemType systemType)
	{

	}
}