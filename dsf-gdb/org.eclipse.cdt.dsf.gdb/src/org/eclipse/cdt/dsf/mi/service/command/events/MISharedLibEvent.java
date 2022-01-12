/*******************************************************************************
 * Copyright (c) 2009, 2010 QNX Software Systems and others.
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
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.events;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFrame;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;

/**
 *
 */
@Immutable
public class MISharedLibEvent extends MIStoppedEvent {

	/** See {@link #getLibrary()} */
	private String fLibrary;

	/**
	 * @since 3.0
	 */
	protected MISharedLibEvent(IExecutionDMContext ctx, int token, MIResult[] results, MIFrame frame, String library) {
		super(ctx, token, results, frame);
		fLibrary = library;
	}

	/** The library that was loaded, as reported by gdb.
	 * @since 3.0*/
	public String getLibrary() {
		return fLibrary;
	}

	/**
	 * @since 3.0
	 */
	public static MIStoppedEvent parse(IExecutionDMContext dmc, int token, MIResult[] results, String library) {
		MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(dmc, token, results);
		return new MISharedLibEvent(stoppedEvent.getDMContext(), token, results, stoppedEvent.getFrame(), library);
	}
}
