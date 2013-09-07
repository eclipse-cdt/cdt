/*******************************************************************************
 * Copyright (c) 2013 Sebastian Bauer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sebastian Bauer - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IDescription;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Tests for verifying whether the PDOM correctly stores information about
 * C functions.
 */
public class DoxygenTests extends PDOMTestBase {
	protected ICProject project;
	protected PDOM pdom;

	public static Test suite() {
		return suite(DoxygenTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		project = createProject("doxygenTests");
		pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(project);
		pdom.acquireReadLock();
	}

	@Override
	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
		if (project != null) {
			project.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
	}

	public void testDocInSource() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "docinsource");
		assertEquals(1, bindings.length);
		IDescription desc = (IDescription) bindings[0].getAdapter(IDescription.class);
		assertNotNull(desc);
		assertEquals("This is a test function with 3 parameters and an int return type. " +
					 "It is documented in the source.", desc.getDescription());
	}

	public void testDocInHeader() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "docinheader");
		assertEquals(1, bindings.length);
		IDescription desc = (IDescription) bindings[0].getAdapter(IDescription.class);
		assertNotNull(desc);
		assertEquals("This is a test function with 2 parameters and an int return type. " +
					 "It is documented in the header.", desc.getDescription());
	}
}
