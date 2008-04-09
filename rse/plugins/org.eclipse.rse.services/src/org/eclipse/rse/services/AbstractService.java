/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Abstract default implementation of an RSE Service. Clients are expected to
 * extend this class.
 *
 * @see IService
 * @since org.eclipse.rse.core 3.0
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
	public void initService(IProgressMonitor monitor) {
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
