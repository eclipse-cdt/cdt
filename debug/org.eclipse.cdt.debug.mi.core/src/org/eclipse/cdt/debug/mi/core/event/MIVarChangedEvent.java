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
	boolean inScope;

	public MIVarChangedEvent(int token, String var, boolean scope) {
		super(token);
		varName = var;
		inScope = scope;
	}

	public String getVarName() {
		return varName;
	}

	public boolean isInScope() {
		return inScope;
	}
}
