/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.event;

import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.output.MIExecAsyncOutput;

/**
 * @since 7.0
 */
public class MICatchpointHitEvent extends MIStoppedEvent {

	/**
	 * See catcpointType parameter in constructor
	 */
	private String fCatchpointType;

	/**
	 * @param source
	 * @param async
	 * @param catchpointType
	 *            the type of catchpoint as reported by gdb via the gdb console
	 *            when the catchpoint is hit. We parse the stream record to get
	 *            this.
	 */
	public MICatchpointHitEvent(MISession source, MIExecAsyncOutput async, String catchpointType) {
		super(source, async);
		fCatchpointType = catchpointType;
	}
 
	public String getCatchpointType() {
		return fCatchpointType;
	}
}
