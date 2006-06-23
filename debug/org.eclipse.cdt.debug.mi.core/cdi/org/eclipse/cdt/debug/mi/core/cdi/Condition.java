/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	String[] tids;

	public Condition(int ignore, String exp, String[] ids) {
		ignoreCount = ignore;
		expression = (exp == null) ? new String() : exp;
		tids = (ids == null) ? new String[0] : ids;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDICondition#getThreadId()
	 */
	public String[] getThreadIds() {
		return tids;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(ICDICondition obj) {
		if (obj instanceof Condition) {
			Condition cond = (Condition)obj;
			if (cond.getIgnoreCount() != this.getIgnoreCount())
				return false;
			if (cond.getExpression().compareTo(this.getExpression()) != 0)
				return false;
			if (cond.getThreadIds().length != this.getThreadIds().length)
				return false;
			for (int i = 0; i < cond.getThreadIds().length; ++i) {
				if (cond.getThreadIds()[i].compareTo(this.getThreadIds()[i]) != 0) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
