/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 * This command sets up a connection with a trace data  file.
 * 
 * Available with GDB 7.1
 *
 * @since 3.0
 */
public class MITargetSelectTFile extends MITargetSelect {

	public MITargetSelectTFile(IDMContext ctx, String traceFilePath) {
		super(ctx, new String[] { "tfile", traceFilePath}); //$NON-NLS-1$
	}
}
