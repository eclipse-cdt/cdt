/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;
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

	@Override
	protected void setUp() throws Exception {
		project = createProject("functionTests");
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

	public void testExternCFunction() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "externCFunction");
		assertEquals(1, bindings.length);
		assertTrue(((IFunction) bindings[0]).isExtern());
	}

	public void testStaticCFunction() throws Exception {
		// static elements cannot be found on global scope, see bug 161216
		IBinding[] bindings = findUnqualifiedName(pdom, "staticCFunction");
		assertEquals(1, bindings.length);
		assertTrue(((IFunction) bindings[0]).isStatic());
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

	public void testNoReturnCFunction() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "noReturnCFunction");
		assertEquals(1, bindings.length);
		assertTrue(((IFunction) bindings[0]).isNoReturn());
	}

	public void testKnRStyleFunctionWithProblemParameters() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "KnRfunctionWithProblemParameters");
		assertEquals(1, bindings.length);
		IFunction f= (IFunction) bindings[0];
		IParameter[] params= f.getParameters();
		assertEquals(3, params.length);
		assertNull(params[0].getType()); // It's a problem binding in the DOM 
		assertTrue(params[1].getType() instanceof ICBasicType);
		assertTrue(params[2].getType() instanceof ICBasicType); 
	}
	
	public void testFunctionWithRegisterParam() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "storageClassCFunction");
		assertEquals(1, bindings.length);
		IFunction f= (IFunction) bindings[0];
		IParameter[] params= f.getParameters();
		assertEquals(2, params.length);
		assertEquals(true, params[0].isRegister());
		assertEquals(false, params[1].isRegister());
	}
}
