/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.gdb.breakpoints.GDBDynamicPrintfUtils;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * -dprintf-insert [ -t ] [ -f ] [ -d ]
 *       [ -c CONDITION ] [ -i IGNORE-COUNT ]
 *       [ -p THREAD ] [ LOCATION ] [ FORMAT ] [ ARGUMENT ]
 *
 * If specified, LOCATION, can be one of:
 *  * function
 *  * filename:linenum
 *  * filename:function
 *  * *address
 *
 *
 * The possible optional parameters of this command are:
 *
 * '-t'
 *      Insert a temporary dprintf.
 *
 * '-c CONDITION'
 *      Make the dprintf conditional on CONDITION.
 *
 * '-i IGNORE-COUNT'
 *      Initialize the IGNORE-COUNT.
 *
 * '-f'
 *      If location cannot be parsed (for example if it refers to unknown files or
 *      functions), create a pending dprintf. Without this flag, if a location
 *      cannot be parsed, the dprintf will not be created and an error will be
 *      reported.
 *
 * '-d'
 *      Create a disabled dprintf.
 *
 * '-p THREAD'
 *      THREAD on which to apply the dprintf
 *
 * Available with GDB 7.7.
 *
 * @since 4.4
 */
public class MIDPrintfInsert extends MICommand<MIBreakInsertInfo> {
	public MIDPrintfInsert(IBreakpointsTargetDMContext ctx, boolean isTemporary, String condition, int ignoreCount,
			int tid, boolean disabled, boolean allowPending, String location, String printfStr) {
		super(ctx, "-dprintf-insert"); //$NON-NLS-1$

		// Determine the number of optional parameters that are present
		// and allocate a corresponding string array
		int i = 0;
		if (isTemporary) {
			i++;
		}
		if (condition != null && !condition.isEmpty()) {
			i += 2;
		}
		if (ignoreCount > 0) {
			i += 2;
		}
		if (tid > 0) {
			i += 2;
		}
		if (disabled) {
			i++;
		}
		if (allowPending) {
			i++;
		}

		String[] opts = new String[i];

		// Fill in the optional parameters
		i = 0;
		if (isTemporary) {
			opts[i] = "-t"; //$NON-NLS-1$
			i++;
		}
		if (condition != null && !condition.isEmpty()) {
			opts[i] = "-c"; //$NON-NLS-1$
			i++;
			opts[i] = condition;
			i++;
		}
		if (ignoreCount > 0) {
			opts[i] = "-i"; //$NON-NLS-1$
			i++;
			opts[i] = Integer.toString(ignoreCount);
			i++;
		}
		if (tid > 0) {
			opts[i] = "-p"; //$NON-NLS-1$
			i++;
			opts[i] = Integer.toString(tid);
			i++;
		}
		if (disabled) {
			opts[i] = "-d"; //$NON-NLS-1$
			i++;
		}
		if (allowPending) {
			opts[i] = "-f"; //$NON-NLS-1$
			i++;
		}

		if (opts.length > 0) {
			setOptions(opts);
		}

		setParameters(createParameters(location, printfStr));
	}

	private Adjustable[] createParameters(String location, String printfStr) {
		List<Adjustable> paramsList = new ArrayList<>();

		paramsList.add(new MIStandardParameterAdjustable(location));

		GDBDynamicPrintfUtils.GDBDynamicPrintfString parsedStr = new GDBDynamicPrintfUtils.GDBDynamicPrintfString(
				printfStr);

		if (parsedStr.isValid()) {
			paramsList.add(new DPrintfAdjustable(parsedStr.getString()));
			for (String arg : parsedStr.getArguments()) {
				paramsList.add(new MIStandardParameterAdjustable(arg));
			}
		}

		return paramsList.toArray(new Adjustable[paramsList.size()]);
	}

	@Override
	public MIBreakInsertInfo getResult(MIOutput output) {
		return new MIBreakInsertInfo(output);
	}

	/**
	 * This adjustable makes sure that the dprintf parameters will not be modified
	 * any further.  The reason for that is that the -dprintf-insert command
	 * accepts the quoted string and any \n directly.
	 */
	private class DPrintfAdjustable extends MICommandAdjustable {
		public DPrintfAdjustable(String value) {
			super(value);
		}

		@Override
		public String getAdjustedValue() {
			return getValue();
		}
	}
}
