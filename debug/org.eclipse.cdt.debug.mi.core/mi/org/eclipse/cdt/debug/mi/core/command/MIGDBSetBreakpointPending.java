/*******************************************************************************
 * Copyright (c) 2005 QnX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Qnx Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 * MIGDBSetBreakpointPending
 *
 */
public class MIGDBSetBreakpointPending extends MIGDBSet {

	public MIGDBSetBreakpointPending(String miVersion, boolean set) {
		super(miVersion, new String[] {"breakpoint", "pending", (set) ? "on" : "off"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

}
