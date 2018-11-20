/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
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
 *  *stopped,reason="end-stepping-range",thread-id="0",frame={addr="0x08048538",func="main",args=[{name="argc",value="1"},{name="argv",value="0xbffff18c"}],file="hello.c",line="13"}
 */
@Immutable
public class MISteppingRangeEvent extends MIStoppedEvent {

	protected MISteppingRangeEvent(IExecutionDMContext ctx, int token, MIResult[] results, MIFrame frame) {
		super(ctx, token, results, frame);
	}

	/**
	 * @since 1.1
	 */
	public static MISteppingRangeEvent parse(IExecutionDMContext dmc, int token, MIResult[] results) {
		MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(dmc, token, results);
		return new MISteppingRangeEvent(stoppedEvent.getDMContext(), token, results, stoppedEvent.getFrame());
	}
}
