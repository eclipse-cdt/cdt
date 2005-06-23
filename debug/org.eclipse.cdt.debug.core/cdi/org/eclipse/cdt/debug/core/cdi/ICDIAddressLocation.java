/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi;

import java.math.BigInteger;

/**
 * 
 * Represents a line location in the debuggable program.
 * 
 */
public interface ICDIAddressLocation extends ICDILocation {

	/**
	 * Returns the address of this location.
	 * 
	 * @return BigInteger - the address of this location
	 */
	BigInteger getAddress();

}
