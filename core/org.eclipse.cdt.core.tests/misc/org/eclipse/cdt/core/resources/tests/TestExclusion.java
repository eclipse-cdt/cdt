package org.eclipse.cdt.core.resources.tests;

import org.eclipse.cdt.core.resources.RefreshExclusion;
import org.eclipse.core.resources.IResource;

public class TestExclusion extends RefreshExclusion {

	@Override
	public String getName() {
		return "TestExclusion";
	}

	@Override
	public boolean testExclusion(IResource resource) {
		// if the resource name ends in a 2, then we pass
		String name = resource.getName();
		return name.endsWith("2");
	}
	
}