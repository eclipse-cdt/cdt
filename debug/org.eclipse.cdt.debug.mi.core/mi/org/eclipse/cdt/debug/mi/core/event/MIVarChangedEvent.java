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
public class MIVarChangedEvent extends MIChangedEvent {

	String varName;

	public MIVarChangedEvent(String var) {
		this(0, var);
	}

	public MIVarChangedEvent(int token, String var) {
		super(token);
		varName = var;
	}

	public String getVarName() {
		return varName;
	}

}
