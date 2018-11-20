/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
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

/**
 * This command sets up a connection with a core file.
 * @since 2.0
 */
public class MITargetSelectCore extends MITargetSelect {

	public MITargetSelectCore(IDMContext ctx, String coreFilePath) {
		super(ctx, new String[] { "core", coreFilePath }); //$NON-NLS-1$
	}
}
