/********************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others. All rights reserved.
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
 * Uwe Stieber (Wind River) - Reworked new connection wizard extension point.
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Javier Montalvo Orus (Symbian) - [188146] Incorrect "FTP Settings" node in Property Sheet for Linux connection
 * Martin Oberhuber (Wind River) - [190231] Move ISubSystemPropertiesWizardPage from UI to Core
 * David Dykstal (IBM) - [217556] remove service subsystem types
 * David Dykstal (IBM) - [168976][api] move ISystemNewConnectionWizardPage from core to UI
 * David Dykstal (IBM) - [232585] adding help for new subsystem wizard page
 ********************************************************************************/

package org.eclipse.rse.ui.wizards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.DummyHost;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.widgets.services.ConnectorServiceElement;
import org.eclipse.rse.ui.widgets.services.FactoryServiceElement;
import org.eclipse.rse.ui.widgets.services.PropertySetServiceElement;
import org.eclipse.rse.ui.widgets.services.RootServiceElement;
import org.eclipse.rse.ui.widgets.services.ServerLauncherPropertiesServiceElement;
import org.eclipse.rse.ui.widgets.services.ServiceElement;
import org.eclipse.rse.ui.widgets.services.ServicesForm;
import org.eclipse.rse.ui.wizards.newconnection.RSEDefaultNewConnectionWizard;
import org.eclipse.rse.ui.wizards.newconnection.RSEDefaultNewConnectionWizardMainPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public class SubSystemServiceWizardPage extends AbstractSystemNewConnectionWizardPage
{
	private ServicesForm _form;
	private ISubSystemConfiguration _selectedConfiguration;
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
		SystemWidgetHelpers.setHelp(parent, "org.eclipse.rse.ui.ServiceSubsystemWizardPage"); //$NON-NLS-1$
		_form = new ServicesForm(parent.getShell(), getMessageLine());
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


			ISubSystemConfiguration currentFactory = getSubSystemConfiguration();

			IRSESystemType systemType = getMainPage() != null && getMainPage().getWizard() instanceof RSEDefaultNewConnectionWizard ? ((RSEDefaultNewConnectionWizard)getMainPage().getWizard()).getSystemType() : null;
			ISubSystemConfiguration[] factories = getServiceSubSystemConfigurations(systemType, currentFactory.getServiceType());

			IHost dummyHost = null;
			if (getWizard() instanceof RSEDefaultNewConnectionWizard)
			{
				RSEDefaultNewConnectionWizard wizard = (RSEDefaultNewConnectionWizard)getWizard();
				if (wizard.getStartingPage() instanceof RSEDefaultNewConnectionWizardMainPage) {
					dummyHost = new DummyHost(((RSEDefaultNewConnectionWizardMainPage)wizard.getStartingPage()).getSystemConnectionForm().getHostName(),
																		 wizard.getSystemType());
				}
			}

			// create elements for each
			_serviceElements = new ServiceElement[factories.length];
			for (int i = 0; i < factories.length; i++)
			{
				ISubSystemConfiguration factory = factories[i];
				_serviceElements[i] = new FactoryServiceElement(dummyHost, factory);


				//if (factory == currentFactory)
				if (i == 0) // use first
				{
					_serviceElements[i].setSelected(true);
				}

			}
		}
		return _serviceElements;
	}

	/**
	 * @param systemType
	 * @param serviceType
	 * @return
	 * @since 3.0 returning ISubSystemConfiguration instead of
	 *        IServiceSubSystemConfiguration
	 */
	protected ISubSystemConfiguration[] getServiceSubSystemConfigurations(IRSESystemType systemType, Class serviceType)
	{
		List results = new ArrayList();
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		ISubSystemConfiguration[] configs = sr.getSubSystemConfigurationsBySystemType(systemType, false);

		for (int i = 0; i < configs.length; i++)
		{
			ISubSystemConfiguration config = configs[i];
			if (config.getServiceType() == serviceType)
			{
				results.add(config);
			}
		}

		return (ISubSystemConfiguration[])results.toArray(new ISubSystemConfiguration[results.size()]);
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

			_selectedConfiguration = ((FactoryServiceElement)_form.getSelectedService()).getFactory();
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
				if(el.isSelected())
				{
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
									if(cch[0] instanceof ServerLauncherPropertiesServiceElement)
									{
										ServerLauncherPropertiesServiceElement result = (ServerLauncherPropertiesServiceElement)cch[0];
										results.add(result);
									}
								}
							}
						}
					}
				}
			}
		}
		return (ServerLauncherPropertiesServiceElement[])results.toArray(new ServerLauncherPropertiesServiceElement[results.size()]);
	}

	public boolean applyValues(ISubSystem ss) {
		if (_selectedConfiguration != null) {
			ISubSystemConfiguration currentConfiguration = ss.getSubSystemConfiguration();
			if (currentConfiguration != null) {
				if (_selectedConfiguration != currentConfiguration) {
					ss.switchServiceFactory(_selectedConfiguration);
				}
				if (_root != null) {
					IConnectorService connectorService = ss.getConnectorService();
					// process server launcher properties
					ServerLauncherPropertiesServiceElement[] elements = getPropertiesServiceElement();
					if (elements.length > 0) {
						ServerLauncherPropertiesServiceElement element = elements[0];
						if (element.userModified()) {
							IServerLauncherProperties properties = element.getServerLauncherProperties();
							properties.saveToProperties();
							connectorService.setRemoteServerLauncherProperties(properties);
						}
					}
					/*
					 * Process connector service property sets
					 * The connector service element (there should be only one) is attached to a dummy host.
					 * Therefore the property sets containing the new values can me moved from the (dummy)
					 * connector service to the real connector service.
					 */
					List connectorServiceElements = getConnectorServiceElements(_root);
					for (Iterator z = connectorServiceElements.iterator(); z.hasNext();) {
						ConnectorServiceElement element = (ConnectorServiceElement) z.next();
						PropertySetServiceElement[] psElements = element.getPropertySets();
						for (int i = 0; i < psElements.length; i++) {
							PropertySetServiceElement psElement = psElements[i];
							IPropertySet set = psElement.getPropertySet();
							connectorService.addPropertySet(set); // moves the property set, replacing the old one
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Returns the list of connector service elements from a given service element.
	 * @param root The root element from which to search
	 * @return A list of all found connector service elements. The list will be empty if non
	 * are found.
	 */
	private List getConnectorServiceElements(ServiceElement root) {
		List result = new ArrayList(10);
		if (!(root instanceof FactoryServiceElement) || root.isSelected())
		{
			if (root instanceof ConnectorServiceElement) {
				result.add(root);
			}

			ServiceElement[] children = root.getChildren();
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					ServiceElement child = children[i];
					result.addAll(getConnectorServiceElements(child));
				}
			}
		}
		return result;
	}

	/**
	 * @since 3.0 taking ISubSystemConfiguration instead of
	 *        IServiceSubSystemConfiguration
	 */
	protected IConnectorService getCustomConnectorService(ISubSystemConfiguration config)
	{
		ServiceElement[] children = _root.getChildren();
		for (int i = 0; i < children.length; i++)
		{
			ServiceElement child = children[i];
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