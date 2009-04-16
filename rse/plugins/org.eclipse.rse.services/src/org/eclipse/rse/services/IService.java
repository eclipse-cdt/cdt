/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable and add Javadoc
 * David McKnight   (IBM)        - [271244] [sftp files] "My Home" filter not working
 *******************************************************************************/

package org.eclipse.rse.services;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * IService is the base interface for any non-UI service contributions to RSE.
 *
 * An actual Service is free to perform any operations at all - the only
 * commonality between services is that they have a name, can be initialized,
 * and are adaptable for future extension.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 *              Service implementations must subclass
 *              {@link AbstractService} rather than implementing this
 *              interface directly.
 */
public interface IService extends IAdaptable
{
	/**
	 * Get the name of this Service as a translated, UI-visible String.
	 * Extenders are expected to override this method.
	 *
	 * @return the name of this Service.
	 */
	public String getName();

	/**
	 * Get the description of this Service as a translated, UI-visible String.
	 * Extenders are expected to override this method.
	 *
	 * @return the description of this Service.
	 */
	public String getDescription();

	/**
	 * Initialize this Service to make it ready for operation. This method may
	 * be long-running and normally gets called after a connection has 
	 * been established.  
	 *
	 * Extenders are expected to override this method.
	 *
	 * @param monitor A progress monitor to provide progress of long-running
	 *            operation. There is no guarantee that cancellation is actually
	 *            supported by a Service since it would leave the service in a
	 *            potentially inconsistent, partially initialized state.
	 */
	public void initService(IProgressMonitor monitor);

	/**
	 * Clean up this Service. This method is called by clients as part of a
	 * disconnect operation and should clean up any local status that the
	 * Service might have.
	 *
	 * Extenders are expected to override this method.
	 *
	 * @param monitor A progress monitor to provide progress of long-running
	 *            operation. There is no guarantee that cancellation is actually
	 *            supported by a Service since it would leave the service in a
	 *            potentially inconsistent, partially initialized state.
	 */
	public void uninitService(IProgressMonitor monitor);
}
