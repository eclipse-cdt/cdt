/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
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
 *     Alvaro Sanchez-Leon (Ericsson) - Make Registers View specific to a frame (Bug 323552)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.mi.service.MIFormat;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataListRegisterValuesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 *
 *       -data-list-register-values FMT [ ( REGNO )*]
 *
 *    Display the registers' contents.  FMT is the format according to
 * which the registers' contents are to be returned, followed by an
 * optional list of numbers specifying the registers to display.  A
 * missing list of numbers indicates that the contents of all the
 * registers must be returned.
 *
 */
public class MIDataListRegisterValues extends MICommand<MIDataListRegisterValuesInfo> {

	int[] regnums;
	int fFmt;

	/**
	 * @since 4.3
	 */
	public MIDataListRegisterValues(IFrameDMContext ctx, int fmt) {
		this(ctx, fmt, null);
	}

	/**
	 * @since 4.3
	 */
	public MIDataListRegisterValues(IFrameDMContext ctx, int fmt, int[] regnos) {
		super(ctx, "-data-list-register-values"); //$NON-NLS-1$
		init(fmt, regnos);
	}

	private void init(int fmt, int[] regnos) {
		regnums = regnos;

		String format = "x"; //$NON-NLS-1$
		switch (fmt) {
		case MIFormat.NATURAL:
			format = "N"; //$NON-NLS-1$
			break;
		case MIFormat.RAW:
			format = "r"; //$NON-NLS-1$
			break;
		case MIFormat.DECIMAL:
			format = "d"; //$NON-NLS-1$
			break;
		case MIFormat.BINARY:
			format = "t"; //$NON-NLS-1$
			break;
		case MIFormat.OCTAL:
			format = "o"; //$NON-NLS-1$
			break;
		case MIFormat.HEXADECIMAL:
			format = "x"; //$NON-NLS-1$
			break;
		default:
			format = "x"; //$NON-NLS-1$
			break;
		}

		fFmt = fmt;

		setOptions(new String[] { format });

		if (regnos != null && regnos.length > 0) {
			String[] array = new String[regnos.length];
			for (int i = 0; i < regnos.length; i++) {
				array[i] = Integer.toString(regnos[i]);
			}
			setParameters(array);
		}
	}

	public int[] getRegList() {
		return regnums;
	}

	@Override
	public MIDataListRegisterValuesInfo getResult(MIOutput output) {
		return new MIDataListRegisterValuesInfo(output);
	}

	/*
	 * Takes the supplied command and coalesces it with this one.
	 * The result is a new third command which represent the two
	 * original command.
	 */
	@Override
	public MIDataListRegisterValues coalesceWith(ICommand<? extends ICommandResult> command) {
		/*
		 * Can coalesce only with other DsfMIDataListRegisterValues commands.
		 */
		if (!(command instanceof MIDataListRegisterValues))
			return null;

		IDMContext context = getContext();

		/*
		 * Make sure we are coalescing over the same context
		 */
		if (!command.getContext().equals(context)) {
			return null;
		}

		MIDataListRegisterValues cmd = (MIDataListRegisterValues) command;

		/*
		 * If the format is different then this cannot be added to the list.
		 */
		if (fFmt != cmd.fFmt)
			return null;

		int[] newregnos = new int[regnums.length + cmd.regnums.length];

		/*
		 * We need to add the new register #'s to the list. If one is already there
		 * then do not add it twice. So copy the original list of this command.
		 */

		for (int idx = 0; idx < regnums.length; idx++) {
			newregnos[idx] = regnums[idx];
		}

		int curloc = regnums.length;

		for (int ndx = 0; ndx < cmd.regnums.length; ndx++) {

			int curnum = cmd.regnums[ndx];
			int ldx;

			/*
			 * Search the current list to see if this entry is in it.
			 */

			for (ldx = 0; ldx < regnums.length; ldx++) {
				if (newregnos[ldx] == curnum) {
					break;
				}
			}

			if (ldx == regnums.length) {

				/*
				 *  Since we did not find a match add it at the end of the list.
				 */
				newregnos[curloc] = curnum;
				curloc++;
			}
		}

		/*
		 * Create a final proper array set of the new combined list.
		 */
		int[] finalregnums = new int[curloc];

		for (int fdx = 0; fdx < curloc; fdx++) {
			finalregnums[fdx] = newregnos[fdx];
		}

		/*
		 *  Now construct a new one. The format we will use is this command.
		 */
		return new MIDataListRegisterValues((IFrameDMContext) getContext(), fFmt, finalregnums);
	}
}
