/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;



/**
 *
 *  ^running
 */
public class MIRunningEvent extends MIEvent {

	public static final int CONTINUE = 0;
	public static final int NEXT = 1;
	public static final int NEXTI = 2;
	public static final int STEP = 3;
	public static final int STEPI = 4;
	public static final int FINISH = 5;
	public static final int UNTIL = 6;

	int type;

	public MIRunningEvent(int t) {
		type = t;
	}

	public int getType() {
		return type;
	}

	public String toString() {
		return "Running";
	}
}
