/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.output;


/**
 * -break-watch buf
 * ^done,wpt={number="2",exp="buf"}
 */
public class MIBreakWatchInfo extends MIBreakInsertInfo {

	public MIBreakWatchInfo(MIOutput rr) {
		super(rr);
	}
}
