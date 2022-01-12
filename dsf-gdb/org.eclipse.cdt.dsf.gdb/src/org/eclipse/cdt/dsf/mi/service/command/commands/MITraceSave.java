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

import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * -trace-save [-r] FILENAME
 *
 * Saves the collected trace data to FILENAME.  Without the '-r' option, the data is downloaded
 * from the target and saved in a local file.  With the '-r' option the target is asked to perform
 * the save.
 *
 * Available with GDB 7.1
 *
 * @since 3.0
 */
public class MITraceSave extends MICommand<MIInfo> {

	public MITraceSave(ITraceTargetDMContext ctx, String file, boolean remoteSave) {
		super(ctx, "-trace-save", null, new String[] { file }); //$NON-NLS-1$
		if (remoteSave) {
			setOptions(new String[] { "-r" }); //$NON-NLS-1$
		}
	}

}
