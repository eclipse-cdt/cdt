package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;

public class Include extends SourceManipulation implements IInclude {
	
	public Include(ICElement parent, String name) {
		super(parent, name, CElement.C_INCLUDE);
	}

	public String getIncludeName() {
		return getElementName();
	}

	public boolean isStandard() {
		return true;
	}

	protected CElementInfo createElementInfo () {
		return new SourceManipulationInfo(this);
	}
}
