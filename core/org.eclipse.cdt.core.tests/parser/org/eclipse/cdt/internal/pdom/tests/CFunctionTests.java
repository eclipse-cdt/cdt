/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Tests for verifying whether the PDOM correctly stores information about
 * C functions.
 */
public class CFunctionTests extends PDOMTestBase {

	protected ICProject project;
	protected PDOM pdom;

	public static Test suite() {
		return suite(CFunctionTests.class);
	}

	protected void setUp() throws Exception {
		project = createProject("functionTests");
		pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(project);
		pdom.acquireReadLock();
	}

	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
		if (project != null) {
			project.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
	}

	public void testExternCFunction() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "externCFunction");
		assertEquals(1, bindings.length);
		assertTrue(((IFunction) bindings[0]).isExtern());
	}

	public void testStaticCFunction() throws Exception {
		// static elements cannot be found on global scope, see bug 161216
		IBinding[] bindings = findQualifiedName(pdom, "staticCFunction");
		assertEquals(0, bindings.length);
//		assertTrue(((IFunction) bindings[0]).isStatic());
	}

	public void testInlineCFunction() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "inlineCFunction");
		assertEquals(1, bindings.length);
		assertTrue(((IFunction) bindings[0]).isInline());
	}

	public void testVarArgsCFunction() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "varArgsCFunction");
		assertEquals(1, bindings.length);
		assertTrue(((IFunction) bindings[0]).takesVarArgs());
	}

}
