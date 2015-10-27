/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * @since 4.9
 */
public class MIITSet extends MIInterpreterExecConsole<MIInfo> {

	private static final String BASE_COMMAND = "itset"; //$NON-NLS-1$

	protected static final String VIEW = "view";  //$NON-NLS-1$
	protected static final String DEFINE = "define";  //$NON-NLS-1$
	protected static final String UNDEFINE = "undefine";  //$NON-NLS-1$
	protected static final String WHICH = "which";  //$NON-NLS-1$

	public MIITSet(ICommandControlDMContext ctx, String subCommand) {
		super(ctx, BASE_COMMAND + " " + subCommand); //$NON-NLS-1$
	}
}
