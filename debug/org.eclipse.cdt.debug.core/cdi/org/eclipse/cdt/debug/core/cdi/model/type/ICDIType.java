/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;


/**
 * 
 * Represents the type of a variable.
 * 
 * @since Apr 15, 2003
 */
public interface ICDIType extends ICDIObject {

	/**
	 * Returns the name.
	 * 
	 * @return  the name of the data type
	 * @throws CDIException if this method fails.
	 */
	String getTypeName();

	/**
	 * Returns a more desciptive name.
	 * @return
	 */
	String getDetailTypeName();	
}
