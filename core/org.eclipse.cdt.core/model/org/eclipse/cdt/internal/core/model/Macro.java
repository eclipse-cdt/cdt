package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMacro;

public class Macro extends SourceManipulation implements IMacro {
	
	public Macro(ICElement parent, String name) {
		super(parent, name, CElement.C_MACRO);
	}

	public String getIdentifierList() {
		return "";
	}

	public String getTokenSequence() {
		return "";
	}

	protected CElementInfo createElementInfo () {
		return new SourceManipulationInfo(this);
	}
}
