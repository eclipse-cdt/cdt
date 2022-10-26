/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for verifying whether the PDOM correctly stores information about
 * C functions.
 */
public class CFunctionTests extends PDOMTestBase {
	protected ICProject project;
	protected PDOM pdom;

	@BeforeEach
	protected void beforeEach() throws Exception {
		project = createProject("functionTests");
		pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(project);
		pdom.acquireReadLock();
	}

	@AfterEach
	protected void afterEach() throws Exception {
		pdom.releaseReadLock();
		if (project != null) {
			project.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT,
					new NullProgressMonitor());
		}
	}

	@Test
	public void testExternCFunction() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "externCFunction");
		assertEquals(1, bindings.length);
		assertTrue(((IFunction) bindings[0]).isExtern());
	}

	@Test
	public void testStaticCFunction() throws Exception {
		// static elements cannot be found on global scope, see bug 161216
		IBinding[] bindings = findUnqualifiedName(pdom, "staticCFunction");
		assertEquals(1, bindings.length);
		assertTrue(((IFunction) bindings[0]).isStatic());
	}

	@Test
	public void testInlineCFunction() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "inlineCFunction");
		assertEquals(1, bindings.length);
		assertTrue(((IFunction) bindings[0]).isInline());
	}

	@Test
	public void testVarArgsCFunction() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "varArgsCFunction");
		assertEquals(1, bindings.length);
		assertTrue(((IFunction) bindings[0]).takesVarArgs());
	}

	@Test
	public void testNoReturnCFunction() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "noReturnCFunction");
		assertEquals(1, bindings.length);
		assertTrue(((IFunction) bindings[0]).isNoReturn());
	}

	@Test
	public void testKnRStyleFunctionWithProblemParameters() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "KnRfunctionWithProblemParameters");
		assertEquals(1, bindings.length);
		IFunction f = (IFunction) bindings[0];
		IParameter[] params = f.getParameters();
		assertEquals(3, params.length);
		assertNull(params[0].getType()); // It's a problem binding in the DOM
		assertTrue(params[1].getType() instanceof ICBasicType);
		assertTrue(params[2].getType() instanceof ICBasicType);
	}

	@Test
	public void testFunctionWithRegisterParam() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "storageClassCFunction");
		assertEquals(1, bindings.length);
		IFunction f = (IFunction) bindings[0];
		IParameter[] params = f.getParameters();
		assertEquals(2, params.length);
		assertEquals(true, params[0].isRegister());
		assertEquals(false, params[1].isRegister());
	}
}
