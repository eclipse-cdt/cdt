/********************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - [197167] adding notification and waiting for RSE model
 ********************************************************************************/
package org.eclipse.rse.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * A model initializer creates objects in an RSE profile. For example,
 * initializers can be used to create initial connections, filter pools, and
 * filters.
 * 
 * @since org.eclipse.rse.core 3.0
 */
public interface IRSEModelInitializer {

	/**
	 * Runs the initializer. The initializer should set the monitor to done when complete.
	 * @param monitor the monitor that measures progress of this initializer.
	 * @return an IStatus indicating the success of the initializer. The status will
	 * be logged if it is not an OK status.
	 */
	public IStatus run(IProgressMonitor monitor);

}