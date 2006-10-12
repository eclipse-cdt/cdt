/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.IPath;

/**
 * @author Doug Schaefer
 *
 */
public class IncludesTests extends PDOMTestBase {

	protected ICProject project;
	protected IIndex pdom;

	protected void setUp() throws Exception {
		if (pdom == null) {
			project = createProject("includesTests");
			pdom = CCorePlugin.getIndexManager().getIndex(project);
		}
		pdom.acquireReadLock();
	}

	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
	}
	
	public void test1() throws Exception {
		IPath loc = project.getProject().getLocation().append("I2.h");
		IIndexFile file = pdom.getFile(loc);
		assertNotNull(file);
		IIndexInclude[] allIncludedBy = pdom.findIncludedBy(file, -1);
		assertEquals(9, allIncludedBy.length); // i.e. all of them
	}

}
