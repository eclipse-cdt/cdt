package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

class VariableInfo extends SourceManipulationInfo {

	protected int flags;

	protected VariableInfo (CElement element) {
		super(element);
		flags = 0;
	}

	protected int getAccessControl() {
		return flags;
	}

	protected void setAccessControl(int flags) {
		this.flags = flags;
	}
}
