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

package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;

/**
 * 
 * The signal manager manages the collection of signals defined 
 * for the debug session.
 * Auto update is off by default.
 * @since Jul 9, 2002
 */
public interface ICDISignalManager extends ICDIManager {

	/**
	 * Returns the array of signals defined for this session.
	 * 
	 * @return the array of signals
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDISignal[] getSignals() throws CDIException;

}
