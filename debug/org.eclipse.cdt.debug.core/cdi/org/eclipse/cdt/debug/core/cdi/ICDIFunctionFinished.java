/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

/*
 * ICDIFunctionFinished 
 */
public interface ICDIFunctionFinished extends ICDIEndSteppingRange {

	/**
	 * Return the type of the return value of
	 * the function.
	 * 
	 * @return ICDIType returnType value
	 */
	ICDIType getReturnType() throws CDIException;

	/**
	 * The return value of the function.
	 * 
	 * @return
	 */
	ICDIValue getReturnValue() throws CDIException;
}
