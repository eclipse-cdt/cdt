/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI var-update.
 */

public class MIVarChange {
	String name;
	boolean inScope;
	boolean changed;

	public MIVarChange(String n) {
		name = n;
	}

	public String getVarName() {
		return name;
	}

	public boolean isInScope() {
		return inScope;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setInScope(boolean b) {
		inScope = b;
	}

	public void setChanged(boolean c) {
		changed = c;
	}
}
