package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;

public class Include extends SourceManipulation implements IInclude {
	
	private final boolean standard; 
	
	public Include(ICElement parent, String name, boolean isStandard) {
		super(parent, name, CElement.C_INCLUDE);
		standard = isStandard;
	}

	public String getIncludeName() {
		return getElementName();
	}

	public boolean isStandard() {
		return standard;
	}

	protected CElementInfo createElementInfo () {
		return new SourceManipulationInfo(this);
	}
}
