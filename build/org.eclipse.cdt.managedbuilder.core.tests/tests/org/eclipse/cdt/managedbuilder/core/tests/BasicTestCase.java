/*******************************************************************************
 * Copyright (c) 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class BasicTestCase extends TestCase {
	private List<IProject> fProjList = new LinkedList<IProject>();
	
	protected void addProject(IProject project){
		fProjList.add(project);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		for(Iterator<IProject> iter = fProjList.iterator(); iter.hasNext();){
			IProject proj = iter.next();
			try {
				proj.delete(true, null);
			} catch (CoreException e){
			}
		}
		super.tearDown();
	}
}
