/*******************************************************************************
 * Copyright (c) 2008, 2015 Ericsson and others.
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
 *     Sergey Prigogin (Google)
 *     Marc Khouzam (Ericsson) - Support empty arguments (bug 412471)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import java.util.ArrayList;

import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.utils.CommandLineUtil;

/**
 *      -gdb-set args ARGS
 *
 * Set the inferior program arguments, to be used in the next `-exec-run'.
 * @since 1.1
 */
public class MIGDBSetArgs extends MIGDBSet {

	/** @since 4.0 */
	public MIGDBSetArgs(IMIContainerDMContext dmc) {
		this(dmc, new String[0]);
	}

	/** @since 4.0 */
	public MIGDBSetArgs(IMIContainerDMContext dmc, String[] arguments) {
		super(dmc, null);
		fParameters = new ArrayList<>();
		fParameters.add(new MIStandardParameterAdjustable("args")); //$NON-NLS-1$
		/*
		 * GDB-MI terminates the -gdb-set on the newline, so we have to encode
		 * newlines or we get an MI error. Some platforms (e.g. Bash on
		 * non-windows) can encode newline into something that is received as a
		 * newline to the program, other platforms (windows) cannot encode the
		 * newline in anyway that is recived as a newline, so it is encoded as
		 * whitepsace.
		 */
		String args = CommandLineUtil.argumentsToString(arguments, true);
		fParameters.add(new MINoChangeAdjustable(args));
	}
}
