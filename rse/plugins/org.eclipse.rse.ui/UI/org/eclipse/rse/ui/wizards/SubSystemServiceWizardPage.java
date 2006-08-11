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

package org.eclipse.rse.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.servicesubsystem.IServiceSubSystem;
import org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.model.DummyHost;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.widgets.services.ConnectorServiceElement;
import org.eclipse.rse.ui.widgets.services.FactoryServiceElement;
import org.eclipse.rse.ui.widgets.services.PropertyElement;
import org.eclipse.rse.ui.widgets.services.RootServiceElement;
import org.eclipse.rse.ui.widgets.services.ServerLauncherPropertiesServiceElement;
import org.eclipse.rse.ui.widgets.services.ServiceElement;
import org.eclipse.rse.ui.widgets.services.ServicesForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public class SubSystemServiceWizardPage extends AbstractSystemNewConnectionWizardPage implements ISubSystemPropertiesWizardPage
{
	private ServicesForm _form;
	private IServiceSubSystemConfiguration _selectedFactory;
	private ServiceElement _root;
	private ServiceElement[] _serviceElements;
	
	public SubSystemServiceWizardPage(IWizard wizard, ISubSystemConfiguration parentFactory, String pageName, String pageTitle, String pageDescription)
	{
		super(wizard, parentFactory, pageName, pageTitle, pageDescription);
	}

	public SubSystemServiceWizardPage(IWizard wizard, ISubSystemConfiguration parentFactory, String pageDescription)
	{
		super(wizard, parentFactory, pageDescription);
	}

	public SubSystemServiceWizardPage(IWizard wizard, ISubSystemConfiguration parentFactory)
	{
		super(wizard, parentFactory);
	}

	public Control createContents(Composite parent)
	{
		_form = new ServicesForm(getMessageLine());
		Control control = _form.createContents(parent);
		
		ServiceElement[] elements = getServiceElements();
		_root = new RootServiceElement(elements);
		_form.init(_root);
		
		return control;
	}

	protected ServiceElement[] getServiceElements()
	{
		if (_serviceElements == null)
		{

			
			IServiceSubSystemConfiguration currentFactory = (IServiceSubSystemConfiguration)getSubSystemConfiguration();
			
			String systemTypeStr = getMainPage().getSystemType();
			IRSESystemType systemType = RSECorePlugin.getDefault().getRegistry().getSystemType(systemTypeStr);
			
			IServiceSubSystemConfiguration[] factories = getServiceSubSystemFactories(systemTypeStr, currentFactory.getServiceType());
			
			IHost dummyHost = null;
			if (getWizard() instanceof RSENewConnectionWizard)
			{
				dummyHost = ((RSENewConnectionWizard)getWizard()).getDelegate().getDummyHost();
			}
			
			// create elements for each 
			_serviceElements = new ServiceElement[factories.length];
			for (int i = 0; i < factories.length; i++)
			{	
				IServiceSubSystemConfiguration factory = factories[i];
				_serviceElements[i] = new FactoryServiceElement(dummyHost, factory);
			
				
				if (factory == currentFactory)
				{
					_serviceElements[i].setSelected(true);
				}
			}
		}		
		return _serviceElements;
	}
	
	protected IServiceSubSystemConfiguration[] getServiceSubSystemFactories(String systemType, Class serviceType)
	{
		List results = new ArrayList();
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		ISubSystemConfiguration[] factories = sr.getSubSystemConfigurationsBySystemType(systemType);
		
		for (int i = 0; i < factories.length; i++)
		{
			ISubSystemConfiguration factory = factories[i];
			if (factory instanceof IServiceSubSystemConfiguration)
			{
				IServiceSubSystemConfiguration sfactory = (IServiceSubSystemConfiguration)factory;
				if (sfactory.getServiceType() == serviceType)
				{
					
					results.add(sfactory);
				}
			}
		}
		
		return (IServiceSubSystemConfiguration[])results.toArray(new IServiceSubSystemConfiguration[results.size()]);
	}
	
	public boolean isPageComplete()
	{
		return true;
	}

	public boolean performFinish()
	{
		if (_root != null)
		{
			_root.commit();
			
			_selectedFactory = ((FactoryServiceElement)_form.getSelectedService()).getFactory();
		}
		return true;
	}
	
	protected ServerLauncherPropertiesServiceElement[] getPropertiesServiceElement()
	{
		List results = new ArrayList();
		for (int i = 0; i < _serviceElements.length; i++)
		{
			{
				ServiceElement el = _serviceElements[i];
				ServiceElement[] children = el.getChildren();
				if (children != null)
				{
					for (int c = 0; c < children.length; c++)
					{
						ServiceElement child = children[c];
						if (child instanceof ConnectorServiceElement)
						{
							ServiceElement[] cch = child.getChildren();
							if (cch != null && cch.length > 0) 
							{
								ServerLauncherPropertiesServiceElement result = (ServerLauncherPropertiesServiceElement)cch[0];
								results.add(result);
							}
						}
					}
				}
			}
		}
		return (ServerLauncherPropertiesServiceElement[])results.toArray(new ServerLauncherPropertiesServiceElement[results.size()]);
	}

	public boolean applyValues(ISubSystem ss)
	{
		if (_selectedFactory != null)
		{
			IServiceSubSystemConfiguration currentFactory = (IServiceSubSystemConfiguration)ss.getSubSystemConfiguration();
			if (currentFactory != null)
			{
				if (_selectedFactory != currentFactory)
				{
					((IServiceSubSystem)ss).switchServiceFactory(_selectedFactory);
				}
				IHost realHost = ss.getHost();
				if (_root != null)
				{ 
					{
						ServerLauncherPropertiesServiceElement[] elements = getPropertiesServiceElement();
						if (elements.length > 0)
						{
							IServerLauncherProperties properties = elements[0].getServerLauncherProperties();
						
							IConnectorService rserv = ss.getConnectorService();
							properties.saveToProperties();
								rserv.setRemoteServerLauncherProperties(properties);
								/*
								PropertyElement[] properties = elements[i].getProperties();
								for (int p = 0; p < properties.length; p++)
								{
									PropertyElement pel = properties[p];
									
								}
								*/
								//rserv.addPropertySets(sets);
		
						}
					}
				}
			
			}
		}
		return true;
	}
	
	protected IConnectorService getCustomConnectorService(IServiceSubSystemConfiguration config)
	{
		ServiceElement[] children = _root.getChildren();
		for (int i = 0; i < children.length; i++)
		{
			ServiceElement child = (ServiceElement)children[i];
			if (child instanceof FactoryServiceElement)
			{
				FactoryServiceElement fchild = (FactoryServiceElement)child;
				if (fchild.getFactory() == config)
				{
					return fchild.getConnectorService();
				}
			}
		}
		return null;		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void handleVerifyComplete() 
	{
		boolean complete = isPageComplete();
		clearErrorMessage();
		setPageComplete(complete);
	}
}