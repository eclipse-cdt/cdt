/********************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 ********************************************************************************/

package org.eclipse.tm.internal.discovery.engine;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.tm.discovery.model.ModelPackage;
import org.eclipse.tm.discovery.protocol.IProtocol;
import org.eclipse.tm.discovery.transport.ITransport;
import org.eclipse.tm.internal.discovery.model.util.ModelResourceFactoryImpl;

/**
 * Engine for service discovery. 
 * Instantiates the model that contains the discovered services information.
 * Provides methods to launch a discovery job given an implementation of ITransport and IProtocol to populate the model.
 * 
 * @see ITransport
 * @see IProtocol
 * 
 */

public class ServiceDiscoveryEngine {

	private static ServiceDiscoveryEngine SERVICE_DISCOVERY_ENGINE = null;
	
	private final URI SERVICE_DISCOVERY_URI = URI.createFileURI(Messages.getString("ServiceDiscoveryEngine.DiscoveryModelFileURI")); //$NON-NLS-1$

	private Resource resource;

	/**
	 * Gets an instance of the service discovery engine
	 */
	public static ServiceDiscoveryEngine getInstance() {
		if (SERVICE_DISCOVERY_ENGINE == null)
			SERVICE_DISCOVERY_ENGINE = new ServiceDiscoveryEngine();

		return SERVICE_DISCOVERY_ENGINE;
	}
	
	/*
	 * Private constructor of the ServiceDiscoveryEngine
	 */
	private ServiceDiscoveryEngine() {
	
		// get model
		ResourceSet resourceSet = new ResourceSetImpl();

		// Register the appropriate resource factory to handle all file
		// extentions.
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new ModelResourceFactoryImpl());

		// Register the package to ensure it is available during loading.
		resourceSet.getPackageRegistry().put(ModelPackage.eNS_URI,ModelPackage.eINSTANCE);

		try {
			resource = resourceSet.getResource(SERVICE_DISCOVERY_URI, true);
		} catch (WrappedException e) {
			resource = resourceSet.createResource(SERVICE_DISCOVERY_URI);
		}
	}

	/**
	 * Gets the resource
	 * 
	 * @return the static reference to the resource model
	 */
	
	public Resource getResource() {
		return resource;
	}
	
	/**
	 * Starts a service discovery job given an implementation of IProtocol and ITransport
	 * 
	 * @param transport
	 * 			the selected transport
	 * @see ITransport
	 * @see IProtocol
	 */
	
	public void doServiceDiscovery(String query, IProtocol protocol, ITransport transport)
	{
		protocol.getDiscoveryJob(query, resource,transport).schedule();
	}
	

	
}
