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
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.pdom.tests;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;

import junit.framework.Test;

/**
 * Tests for verifying whether the PDOM correctly stores information about
 * C variable declarations.
 */
public class CVariableTests extends PDOMTestBase {

	protected ICProject project;
	protected PDOM pdom;

	public static Test suite() {
		return suite(CVariableTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		project = createProject("variableTests");
		pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(project);
		pdom.acquireReadLock();
	}

	@Override
	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
		if (project != null) {
			project.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT,
					new NullProgressMonitor());
		}
	}

	public void testCAutoVariable() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "autoCVariable");
		assertEquals(1, bindings.length);
		IVariable variable = (IVariable) bindings[0];
		assertTrue(variable.isAuto());
	}

	public void testCExternVariable() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "externCVariable");
		assertEquals(1, bindings.length);
		IVariable variable = (IVariable) bindings[0];
		assertTrue(variable.isExtern());
	}

	public void testCRegisterVariable() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "registerCVariable");
		assertEquals(1, bindings.length);
		IVariable variable = (IVariable) bindings[0];
		assertTrue(variable.isRegister());
	}

	public void testCStaticVariable() throws Exception {
		// static elements cannot be found on global scope, see bug 161216
		IBinding[] bindings = findUnqualifiedName(pdom, "staticCVariable");
		assertEquals(1, bindings.length);
		IVariable variable = (IVariable) bindings[0];
		assertTrue(variable.isStatic());
	}

}
