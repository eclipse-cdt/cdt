/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * David McKnight   (IBM)        - [272882] [api] Handle exceptions in IService.initService()
 *******************************************************************************/
package org.eclipse.rse.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;

import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

/**
 * Abstract default implementation of an RSE Service. Clients are expected to
 * extend this class.
 * 
 * @see IService
 * @since org.eclipse.rse.services 3.0
 */
public abstract class AbstractService extends PlatformObject implements IService {

	public String getDescription() {
		return getName();
	}

	public String getName() {
		return this.getClass().getName();
	}

	/**
	 * Default implementation of initService. Extenders who override this method
	 * must call <code>super.initService(monitor)</code> as the first call in
	 * their implementation.
	 */
	public void initService(IProgressMonitor monitor) throws SystemMessageException {
		// Do nothing by default
	}

	/**
	 * Default implementation of uninitService. Extenders who override this
	 * method must call <code>super.uninitService(monitor)</code> as the last
	 * call in their implementation.
	 */
	public void uninitService(IProgressMonitor monitor) {
		// Do nothing by default
	}

}
