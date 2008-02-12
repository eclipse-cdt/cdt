/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - [197036] new interface
 *******************************************************************************/

package org.eclipse.rse.internal.core.model;

import org.eclipse.core.runtime.IStatus;

/**
 * A profile operation can be executed by the SystemProfileManager. This allows for multiple changes
 * to model objects to be committed all at once when the last level of nesting is completed.
 */
public interface ISystemProfileOperation {
	
	/**
	 * Run this operation. Should not be invoked directly. 
	 * See {@link SystemProfileManager#run(ISystemProfileOperation)}
	 * @return The IStatus of the operation
	 */
	public IStatus run();

}
