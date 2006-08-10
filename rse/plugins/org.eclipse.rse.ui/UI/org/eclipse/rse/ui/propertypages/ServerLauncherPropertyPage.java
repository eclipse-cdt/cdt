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

import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.core.subsystems.util.ISubSystemConfigurationAdapter;
import org.eclipse.rse.ui.ISystemVerifyListener;
import org.eclipse.rse.ui.widgets.IServerLauncherForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * Property page for editing persistant environment variables for an 
 * RSE connection.
 */
public class ServerLauncherPropertyPage extends SystemBasePropertyPage implements ISystemConnectionWizardPropertyPage, ISystemConnectionWizardErrorUpdater
{

	private IServerLauncherForm _form;
	protected String _systemType;
	private IServerLauncherProperties sl;
	private ISubSystemConfiguration _factory;

	/**
	 * Constructor for ServerLauncherPropertyPage.
	 */
	public ServerLauncherPropertyPage()
	{
		super();
	}

	/**
	 * @see org.eclipse.rse.ui.propertypages.SystemBasePropertyPage#createContentArea(Composite)
	 */
	protected Control createContentArea(Composite parent)
	{
		
		ISubSystemConfiguration factory = null;
		Object input = getElement();
		IConnectorService connectorService = null;
		if (input instanceof ISubSystem)
		{
			ISubSystem ss = (ISubSystem)input;
			connectorService = ss.getConnectorService();
			sl = connectorService.getRemoteServerLauncherProperties();
			factory = ss.getSubSystemConfiguration();
		}
		else if (input instanceof ISubSystemConfiguration)
		{
			factory = (ISubSystemConfiguration)input;
		}	
		else
		{
			factory =_factory;
		}
	
		ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)factory.getAdapter(ISubSystemConfigurationAdapter.class);
		_form = adapter.getServerLauncherForm(factory, getShell(), getMessageLine());

		// Create property page UI
		_form.createContents(parent);
		if (sl != null)
		{
			_form.setHostname(connectorService.getHost().getHostName());
			_form.initValues(sl);				
		}

		return parent;
	}

	/**
	 * @see org.eclipse.rse.ui.propertypages.SystemBasePropertyPage#verifyPageContents()
	 */
	protected boolean verifyPageContents()
	{
		return true;
	}

	public void setSubSystemConfiguration(ISubSystemConfiguration factory)
	{
		_factory = factory;
	}

	public void setHostname(String hostName)
	{
	    _form.setHostname(hostName);
	}
	
	public boolean applyValues(IConnectorService connectorService)
	{
		if (_form == null)
			return true;
		
		if (getElement() instanceof SubSystem)
		{
			SubSystem ss = (SubSystem)getElement();
			if (!ss.getConnectorService().supportsServerLaunchProperties())
			{
				return true;
			}
		}
		
		if (_form.verify())
		{
			if (sl == null)
			{
				sl = connectorService.getRemoteServerLauncherProperties();
			}
			
			if (_form.updateValues(sl))
			{
				try
				{
					connectorService.commit();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				return true;
			}
		}
		return false; 
	}
	
	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk()
	{
		if (_form.isDirty())
		{
			return applyValues(sl.getConnectorService());
		}
		return true;
	}
	
	public boolean isPageComplete()
	{
		if (_form.isDirty())
		{
			return _form.verify();
		}
		else
		{
			return true;
		}
	}
	
	public void addVerifyListener(ISystemVerifyListener listener)
	{
		if (_form instanceof ISystemConnectionWizardErrorUpdater)
		((ISystemConnectionWizardErrorUpdater)_form).addVerifyListener(listener);
	}
	
	public String getTheErrorMessage()
	{
		return getErrorMessage();
	}

	public void setSystemType(String systemType)
	{
		_systemType = systemType;		
	}
}