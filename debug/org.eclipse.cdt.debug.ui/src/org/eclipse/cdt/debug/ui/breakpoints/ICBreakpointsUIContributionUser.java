/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
