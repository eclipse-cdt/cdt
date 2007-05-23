/********************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Javier Montalvo Orus (Symbian) - initial API and implementation
 * Javier Montalvo Orus (Symbian) - [plan] Improve Discovery and Autodetect in RSE
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186523] Move subsystemConfigurations from UI to core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 ********************************************************************************/

package org.eclipse.rse.internal.discovery;


import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemRefreshAllAction;
import org.eclipse.tm.discovery.model.Pair;
import org.eclipse.tm.discovery.model.Service;
import org.eclipse.tm.discovery.model.ServiceType;
import org.eclipse.tm.internal.discovery.wizard.ServiceDiscoveryWizardDisplayPage;
import org.eclipse.tm.internal.discovery.wizard.ServiceDiscoveryWizardMainPage;

/**
 * Service Discovery Wizard
 */

public class ServiceDiscoveryWizard extends Wizard {
	
	private ServiceDiscoveryWizardMainPage serviceDiscoveryMainPage;
	private ServiceDiscoveryWizardDisplayPage serviceDiscoveryPage = null;

	/**
	 * Service Discovery Wizard constructor
	 */
	public ServiceDiscoveryWizard() {
		super();
		setNeedsProgressMonitor(false);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {

		serviceDiscoveryMainPage = new ServiceDiscoveryWizardMainPage();
		addPage(serviceDiscoveryMainPage);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {

		if (page instanceof ServiceDiscoveryWizardMainPage) {
			if (serviceDiscoveryPage == null) {
				serviceDiscoveryPage = new ServiceDiscoveryWizardDisplayPage(serviceDiscoveryMainPage.getQuery(), serviceDiscoveryMainPage.getAddress(), serviceDiscoveryMainPage.getTransport(), serviceDiscoveryMainPage.getProtocol(), serviceDiscoveryMainPage.getTimeOut());
				addPage(serviceDiscoveryPage);
			} else {
				serviceDiscoveryPage.update(serviceDiscoveryMainPage.getQuery(), serviceDiscoveryMainPage.getAddress(), serviceDiscoveryMainPage.getTransport(), serviceDiscoveryMainPage.getProtocol(), serviceDiscoveryMainPage.getTimeOut());
			}
		}
		return super.getNextPage(page);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {

		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.rse.core","subsystemConfigurations"); //$NON-NLS-1$ //$NON-NLS-2$
		IConfigurationElement[] ce = ep.getConfigurationElements();
		
		SystemRefreshAllAction systemRefreshAllAction = new SystemRefreshAllAction(null);
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
				
		String[] addresses = serviceDiscoveryPage.getAddresses();
		for (int i = 0; i < addresses.length; i++) {

			String hostName = addresses[i];
			Vector discoveredServices = serviceDiscoveryPage.getSelectedServices(addresses[i]);
			Vector subSystemConfigurationVector = new Vector();
		
			Enumeration serviceEnumeration = discoveredServices.elements();
			
			IHost conn = null;
			try {
				IRSESystemType discoveryType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(IRSESystemType.SYSTEMTYPE_DISCOVERY_ID);
				conn = registry.createHost(discoveryType, "Discovery@" + hostName, hostName, "Discovered services in "+hostName);//$NON-NLS-1$ //$NON-NLS-2$
			} catch (Exception e) {
				RSECorePlugin.getTheSystemRegistry().deleteHost(conn);
				return false;
			} 
			
			while (serviceEnumeration.hasMoreElements()) {
				
				Service service = (Service) serviceEnumeration.nextElement();
				
				//discovered service name
				String serviceName = ((ServiceType) service.eContainer()).getName();
				
				//discovered transport (tcp, udp)
				String transport = null;
				
				Iterator pairIterator = service.getPair().iterator();
				while (pairIterator.hasNext()) {
					
					Pair pair = (Pair) pairIterator.next();
					if(pair.getKey().equals("transport")) //$NON-NLS-1$
					{
						transport = pair.getValue();
					}
				}
				
				//find the SubSystemConfiguration plugin that matches the name+transport
				for (int j = 0; j < ce.length; j++) {
					String typesList = ce[j].getAttribute("serviceType"); //$NON-NLS-1$
					if(typesList!=null)
					{
						String[] types =  typesList.split(";"); //$NON-NLS-1$
						
						for (int k = 0; k < types.length; k++) {
							if(types[k].equals("_"+serviceName+"._"+transport)) //$NON-NLS-1$ //$NON-NLS-2$
							{
								ISubSystemConfiguration config = registry.getSubSystemConfiguration(ce[j].getAttribute("id")); //$NON-NLS-1$
								IConnectorService connector = config.getConnectorService(conn);
								IPropertySet propertySet;
								pairIterator = service.getPair().iterator();
								
								while (pairIterator.hasNext()) {
									
									Pair pair = (Pair) pairIterator.next();
									
									if((propertySet = connector.getPropertySet(Messages.ServiceDiscoveryWizard_DiscoveryPropertySet))==null) 
									{
										propertySet = connector.createPropertySet(Messages.ServiceDiscoveryWizard_DiscoveryPropertySet);
									}
									propertySet.addProperty(pair.getKey(), pair.getValue());
								}
								
								subSystemConfigurationVector.add(config);
							}
						}
					}	
				}
			}	
				
			ISubSystemConfiguration[] subSystemConfiguration = new ISubSystemConfiguration[subSystemConfigurationVector.size()];
				
			for (int j = 0; j < subSystemConfiguration.length; j++) {
				subSystemConfiguration[j]=(ISubSystemConfiguration)subSystemConfigurationVector.elementAt(j);
			}
			
			ISubSystem[] subSystem = registry.createSubSystems(conn, subSystemConfiguration);
			
			for (int j = 0; j < subSystem.length; j++) {
				
				
				IConnectorService connector = subSystem[j].getConnectorService();
				IPropertySet propertySet = connector.getPropertySet(Messages.ServiceDiscoveryWizard_DiscoveryPropertySet); 
				if(propertySet.getProperty(Messages.ServiceDiscoveryWizard_Port)!=null)
				{
					int port = Integer.parseInt(propertySet.getPropertyValue(Messages.ServiceDiscoveryWizard_Port));
					connector.setPort(port);
					
				}
			}
	
			RSEUIPlugin.getTheSystemRegistryUI().expandHost(conn);			
		}
		
		systemRefreshAllAction.run();
		
		return true;
	}

}