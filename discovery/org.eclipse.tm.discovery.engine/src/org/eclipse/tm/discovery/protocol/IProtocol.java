/********************************************************************************
 * Copyright (c) 2006, 2008 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.tm.discovery.protocol;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.tm.discovery.transport.ITransport;

/**
 * Interface for service discovery protocol implementations.</br>
 * Standard protocols that can be implementated from this interface are:
 * <ul>
 * <li>DNS-SD (Zeroconf)
 * <li>Simple Service Discovery Protocol (SSDP)
 * <li>Service Location Protocol (SPL)
 * </ul>
 * As well as propietary/customised protocols
 *
 */
public interface IProtocol {


	/**
	 * Instantiates a Job to populate a model given an initial query, a resource
	 * containing the model to be populated and an ITranport implementation.
	 * 
	 * @param query Initial query to be sent to the target. This initial query
	 *            can be obtained from the getQueries() method.
	 * 
	 * @param resource Resource containing the model to be populated with the
	 *            results of the service discovery action.
	 * 
	 * @param transport ITransport implementation of the transport to be used
	 *            for the discovery action.
	 * 
	 * @return Job containing the service discovery action.
	 * 
	 * @see Job
	 * @see Resource
	 * @see ITransport
	 * 
	 */
	public abstract Job getDiscoveryJob(String query, Resource resource, ITransport transport);


	/**
	 * Gets the list of recommended queries to start the service discovery process. This queries can be used in getDiscoveryJob().
	 * 
	 * @return
	 * An array containing the recommended queries.
	 */
	public abstract String[] getQueries();

}
