/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;

import org.eclipse.cdt.debug.mi.core.output.MIExecAsyncOutput;
import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;



/**
 *
 */
public class MISharedLibEvent extends MIStoppedEvent {

	public MISharedLibEvent(MIExecAsyncOutput async) {
		super(async);
		parse();
	}
 
	public MISharedLibEvent(MIResultRecord record) {
		super(record);
		parse();
	}

}
