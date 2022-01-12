/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
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

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 *   -remove-inferior GROUPID
 *   ^done
 *
 *   Remove the specified inferior.
 *
 *   @since 4.0
 */
public class MIRemoveInferior extends MICommand<MIInfo> {
	public MIRemoveInferior(ICommandControlDMContext dmc, String groupId) {
		super(dmc, "-remove-inferior", new String[] { groupId }); //$NON-NLS-1$
	}
}
