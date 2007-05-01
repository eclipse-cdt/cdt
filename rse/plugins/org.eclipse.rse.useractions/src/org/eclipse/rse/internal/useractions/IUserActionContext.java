/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.useractions;

import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;

/**
 * The interface for user action contexts. Clients can implement this interface.
 */
public interface IUserActionContext extends IRSEModelObject {
	
	/**
	 * Returns the profile that the user action context belongs to.
	 * @return the profile that the user action context belongs to.
	 */
	public ISystemProfile getParentProfile();
	
	/**
	 * Returns the subsystem configuration that the user action context is applicable for.
	 * @return the subsystem configuration that the user action context is applicable for.
	 */
	public ISubSystemConfiguration getParentConfiguration();
	
	/**
	 * Returns the supplier of the user action context.
	 * @return the supplier of the user action context.
	 */
	public String getSupplier();
	
	/**
	 * Returns whether the user action context is modifiable.
	 * @return <code>true<code> if the user action context is modifiable, <code>false</code> otherwise.
	 */
	public boolean isModifiable();
}
