/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpoints;

/**
 * This interface can be implemented by FieldEditors used in {@extensionPoint org.eclipse.cdt.debug.ui.breakpointContribution} extension.
 * This allow to connect field back to parent that creates it {@link ICBreakpointsUIContribution} 
*/
public interface ICBreakpointsUIContributionUser {
	/**
	 * sets the contribution
	 * @param contribution
	 */
	void setContribution(ICBreakpointsUIContribution contribution);

	/**
	 * get the contribution
	 * @return contribution
	 */
	ICBreakpointsUIContribution getContribution();
}
