/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;



/**
 * This can not be detected yet by gdb/mi.
 *
 */
public class MIVarCreatedEvent extends MICreatedEvent {

	String varName;

	public MIVarCreatedEvent(String var) {
		super(0);
		varName = var;
	}

	public MIVarCreatedEvent(int token, String var) {
		super(token);
		varName = var;
	}

	public String getVarName() {
		return varName;
	}

}
