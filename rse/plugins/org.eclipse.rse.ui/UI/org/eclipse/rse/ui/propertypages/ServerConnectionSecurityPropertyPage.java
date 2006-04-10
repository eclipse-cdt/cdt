/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.propertypages;

import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.comm.SystemKeystoreProviderManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.widgets.ServerConnectionSecurityForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public class ServerConnectionSecurityPropertyPage extends SystemBasePropertyPage implements ISystemConnectionWizardPropertyPage
{


	private ServerConnectionSecurityForm _form;
	private IConnectorService _connectorService;

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
		SystemWidgetHelpers.setCompositeHelp(parent, SystemPlugin.HELPPREFIX + "ssls0001");
		
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
		Object subsystem = getElement();
		if (subsystem instanceof ISubSystem)
		{
			ISubSystem ss = (ISubSystem) subsystem;
			if (ss instanceof SubSystem)
			{
				_connectorService = ss.getConnectorService();
			}
		

			// is ssl to be used for server communications
			boolean useSSL = _connectorService.isUsingSSL();

			// enable/disable as appropriate
			_form.setUseSSL(useSSL);
		}
		
		
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

	public void setSubSystemFactory(ISubSystemConfiguration factory)
	{
	}

	public boolean applyValues(IConnectorService connectorService)
	{

			boolean useSSL = _form.getUseSSL();
			if (useSSL != connectorService.isUsingSSL())
			{
				connectorService.setIsUsingSSL(useSSL);
	
				try
				{
					connectorService.commit();
				}
				catch (Exception e)
				{
				}
			}
	
		return true;
	}
	
	public void setHostname(String name)
	{		
	}

	public void setSystemType(String systemType)
	{

	}
}