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
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDICondition;

/**
 */
public class Condition implements ICDICondition {

	int ignoreCount;
	String expression;

	public Condition(int ignore, String exp) {
		ignoreCount = ignore;
		expression = (exp == null) ? new String() : exp;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDICondition#getIgnoreCount()
	 */
	public int getIgnoreCount() {
		return ignoreCount;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDICondition#getExpression()
	 */
	public String getExpression() {
		return expression;
	}
}
