/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation.
 *******************************************************************************/
package org.eclipse.rse.ui.wizards.newconnection;

import org.eclipse.rse.core.IRSESystemType;

/**
 * Interface to be implemented from dynamic RSE new connection
 * wizards in order to influence attributes and behaviour of the
 * wizard based on the current system state.
 */
public interface IRSEDynamicNewConnectionWizard {

	/**
	 * Validate the catgory id the wizard is proposed to be associated with.
	 * Dependent on the specified system type, the wizard may change the category
	 * id or just return the proposed category id.
	 * 
	 * @param systemType The system type. Must be not <code>null</code>.
	 * @param proposedCategoryId The proposed category id. Might be <code>null</code>.
	 * @return The category id or <code>null</code>.
	 */
	public String validateCategoryId(IRSESystemType systemType, String proposedCategoryId);
}
