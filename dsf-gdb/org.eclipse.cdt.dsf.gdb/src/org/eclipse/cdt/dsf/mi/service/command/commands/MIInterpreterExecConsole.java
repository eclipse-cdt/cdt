/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson 		  	- Modified for additional features in DSF Reference implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * MIInterpreterExecConsole
 */
public class MIInterpreterExecConsole<V extends MIInfo> extends MIInterpreterExec<V> {

	/**
	 * @param interpreter
	 * @param cmd
	 */
	public MIInterpreterExecConsole(IDMContext ctx, String cmd) {
		super(ctx, "console", cmd); //$NON-NLS-1$
	}

}
