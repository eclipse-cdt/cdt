package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IVariableDeclaration;

public class VariableDeclaration extends SourceManipulation implements IVariableDeclaration {
	
	public VariableDeclaration(ICElement parent, String name) {
		super(parent, name, CElement.C_VARIABLE_DECLARATION);
	}

	public String getType() {
		return "";
	}

	public int getAccessControl() {
		return 0;
	}

	protected CElementInfo createElementInfo () {
		return new SourceManipulationInfo(this);
	}
}
