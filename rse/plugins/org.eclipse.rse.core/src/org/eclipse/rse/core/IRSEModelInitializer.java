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
 * A model initializer creates objects an RSE profile.
 * For example, initializers can be used to create initial connections, filter pools, and filters.
 * 
 */
public interface IRSEModelInitializer {

	/**
	 * Runs the initializer. The initializer should set the monitor to done when complete.
	 * @param monitor the monitor that measures progress of this initializer.
	 * @return an IStatus indicating the success of the initializer. The status will
	 * be logged if it is not an OK status. If a status is an IStatus.Error then the 
	 * initializer will be assumed to have failed and will not be queried for its 
	 * completion status.
	 */
	public IStatus run(IProgressMonitor monitor);

	/**
	 * Reports if an initializer is complete. If an initializer runs synchronously then it must
	 * report true immediately after it is run.
	 * An initializer may choose to do some of its work asynchronously. If so, it must 
	 * report true when the initializer considers its work to be complete.
	 * @return true if the initializer has completed its initialization.
	 */
	public boolean isComplete();
}