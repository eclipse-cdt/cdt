/*******************************************************************************
 *  Copyright (c) 2011, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
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

	@Override
	public boolean supportsExclusionInstances() {
		return false;
	}

	@Override
	public Object clone() {
		TestExclusion clone = new TestExclusion();
		copyTo(clone);
		return clone;
	}
}