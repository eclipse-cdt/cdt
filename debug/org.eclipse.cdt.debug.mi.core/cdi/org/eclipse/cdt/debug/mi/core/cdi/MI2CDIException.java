/**********************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.mi.core.MIException;

/**
 */
public class MI2CDIException extends CDIException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MI2CDIException(MIException e) {
		super(e.getMessage(), e.getLogMessage());
	}

}
