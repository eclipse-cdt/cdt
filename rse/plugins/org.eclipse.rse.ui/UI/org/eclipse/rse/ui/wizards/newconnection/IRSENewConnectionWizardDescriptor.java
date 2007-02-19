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

import org.eclipse.rse.ui.wizards.registries.IRSEWizardDescriptor;

/**
 * RSE new connection wizard descriptor.
 */
public interface IRSENewConnectionWizardDescriptor extends IRSEWizardDescriptor {

	/**
	 * Returns a semicolon separated list of system type id's this
	 * wizard is used for. The system type id's might contain wildcards
	 * ('*' or '?'). The method will return <code>null</code> if the
	 * attribute is not set.
	 * 
	 * @return The list of system type id's or <code>null</code>.
	 */
	public String getDeclaredSystemTypeIds();
	
	/**
	 * Returns the list of system type ids the wizard is supporting.
	 * The list is combined from the list of currently registered
	 * system types cleaned up by the ones not matching the declared
	 * system type ids.
	 *  
	 * @return The list of supported system type ids.  May be empty,
   *         but never <code>null</code>.
	 */
	public String[] getSystemTypeIds();
}
