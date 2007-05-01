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
 * The interface for user action model. Clients can implement this interface.
 */
public interface IUserActionModel extends IRSEModelObject {
	
	public static final String USER_ACTION_TYPE = "USER_ACTION_GENERIC"; //$NON-NLS-1$
	public static final String USER_ACTION_COMPILE = "USER_ACTION_COMPILE"; //$NON-NLS-1$
	
	/**
	 * Returns the profile that the user action belongs to.
	 * @return the profile that the user action belongs to.
	 */
	public ISystemProfile getParentProfile();
	
	/**
	 * Returns the subsystem configuration that the user action is applicable for.
	 * @return the subsystem configuration that the user action is applicable for.
	 */
	public ISubSystemConfiguration getParentConfiguration();
	
	/**
	 * The type of the user action.
	 * @return the type of the user action.
	 */
	public String getType();
	
	/**
	 * Returns the supplier of the user action.
	 * @return the supplier of the user action.
	 */
	public String getSupplier();
	
	/**
	 * Returns the command of the user action.
	 * @return the command of the user action.
	 */
	public String getCommand();
	
	/**
	 * Returns the contexts to which the user action applies.
	 * @return array of user action contexts, or an empty array if there are no contexts to which the user action applies.
	 */
	public IUserActionContext[] getContexts();
	
	/**
	 * Returns whether the user action is modifiable.
	 * @return <code>true<code> if the user action is modifiable, <code>false</code> otherwise.
	 */
	public boolean isModifiable();
}