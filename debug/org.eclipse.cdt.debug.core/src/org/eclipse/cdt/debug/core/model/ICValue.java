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

package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.model.IValue;

/**
 *
 * Extends the IValue interface by C/C++ specific functionality. 
 * 
 * @since Sep 9, 2002
 */
public interface ICValue extends IValue
{
	String evaluateAsExpression();

	void dispose();
}
