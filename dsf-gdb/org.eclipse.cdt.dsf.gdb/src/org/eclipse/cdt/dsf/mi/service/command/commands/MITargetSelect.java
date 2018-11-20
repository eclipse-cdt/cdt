/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * This command connects to a remote target.
 */
public class MITargetSelect extends MICommand<MIInfo> {

	/**
	 * @since 3.0
	 */
	public MITargetSelect(IDMContext ctx, String[] params) {
		super(ctx, "-target-select", null, params); //$NON-NLS-1$
	}

	/**
	 * @since 1.1
	 */
	public MITargetSelect(IDMContext ctx, String host, String port, boolean extended) {
		super(ctx, "-target-select", new String[] { extended ? "extended-remote" : "remote", host + ":" + port }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	/**
	 * @since 1.1
	 */
	public MITargetSelect(IDMContext ctx, String serialDevice, boolean extended) {
		super(ctx, "-target-select", new String[] { extended ? "extended-remote" : "remote", serialDevice }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
