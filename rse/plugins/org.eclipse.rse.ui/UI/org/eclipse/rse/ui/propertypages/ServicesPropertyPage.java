/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

import org.eclipse.rse.core.servicesubsystem.IServiceSubSystem;
import org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.ui.widgets.services.FactoryServiceElement;
import org.eclipse.rse.ui.widgets.services.RootServiceElement;
import org.eclipse.rse.ui.widgets.services.ServiceElement;
import org.eclipse.rse.ui.widgets.services.ServicesForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public abstract class ServicesPropertyPage extends SystemBasePropertyPage 
{
	protected ServicesForm _form;
	protected String _hostname;
	protected String _hosttype;
	protected ServiceElement _rootElement;
	
	protected Control createContentArea(Composite parent)
	{
		_form = new ServicesForm(getMessageLine());
		_form.createContents(parent);
		
		// init services
		initServices();
		
		return parent;
	}
	

	protected boolean verifyPageContents()
	{
		return _form.verify();
	}
	
	protected void initServices()
	{
		ServiceElement[] elements = getServiceElements();
		_rootElement = new RootServiceElement(elements);
		_form.init(_rootElement);
	}

	protected IServiceSubSystem getServiceSubSystem()
	{
		return (IServiceSubSystem)getElement();
	}
	
	protected abstract ServiceElement[] getServiceElements();
	protected abstract IServiceSubSystemConfiguration getCurrentServiceSubSystemConfiguration();

	public boolean performOk()
	{
		boolean result = super.performOk();
		if (result)
		{
			commitChanges();
			return applyValues(getServiceSubSystem().getConnectorService());
		}
		else
		{
			return result;
		}
	}
	
	protected void commitChanges()
	{
		_rootElement.commit();
	}
	
	protected void revertChanges()
	{
		_rootElement.revert();
	}

	public boolean applyValues(IConnectorService connectorService)
	{
		
		
		FactoryServiceElement selectedService = (FactoryServiceElement)_form.getSelectedService();	
		
		IServiceSubSystemConfiguration factory = (IServiceSubSystemConfiguration)selectedService.getFactory();
		IServiceSubSystemConfiguration currentFactory = getCurrentServiceSubSystemConfiguration();
		if (factory != currentFactory)		
		{
			getServiceSubSystem().switchServiceFactory(factory);
		}
		
		return true;
	}



	public void setHostname(String hostname)
	{
		_hostname = hostname;
	}
	
	public void setSystemType(String systemType)
	{
		_hosttype = systemType;
	}
	
	public String getHostname()
	{
		return _hostname;
	}
	
	public String getSystemType()
	{
		return _hosttype;
	}


	public boolean performCancel()
	{
		_rootElement.revert();
		return super.performCancel();
	}
}