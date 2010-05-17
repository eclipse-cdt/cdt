/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Tests for verifying whether the PDOM correctly stores information about
 * C++ non-member functions.
 */
public class CPPFunctionTests extends PDOMTestBase {

	protected ICProject project;

	protected PDOM pdom;

	public static Test suite() {
		return suite(CPPFunctionTests.class);
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
	
	public void testPointerToFunctionType() throws Exception {
		assertDeclarationCount(pdom, "int2intPtr", 1);
		IIndexFragmentBinding[] b= pdom.findBindings(new char[][] {"int2intPtr".toCharArray()}, IndexFilter.ALL, npm());
		assertEquals(1, b.length);
		assertInstance(b[0], ICPPVariable.class);
		ICPPVariable v= (ICPPVariable) b[0];
		assertInstance(v.getType(), IPointerType.class);
		IPointerType pt= (IPointerType) v.getType();
		assertInstance(pt.getType(), IFunctionType.class);
		IFunctionType ft= (IFunctionType) pt.getType();
		assertInstance(ft.getReturnType(), ICPPBasicType.class);
		assertEquals(1, ft.getParameterTypes().length);
		assertInstance(ft.getParameterTypes()[0], ICPPBasicType.class);
	}
	
	public void testFunctionType() throws Exception {
		assertType(pdom, "normalDeclaration1", ICPPFunction.class);		
		assertType(pdom, "normalDeclaration2", ICPPFunction.class);		
	}
	
	public void testFunctionDeclarations() throws Exception {
		assertDeclarationCount(pdom, "normalDeclaration1", 1);
		assertDeclarationCount(pdom, "normalDeclaration2", 1);
	}

	public void testFunctionDefinitions() throws Exception {
		assertDefinitionCount(pdom, "normalDeclaration1", 1);
		assertDefinitionCount(pdom, "normalDeclaration2", 1);
	}

	public void testFunctionReferences() throws Exception {
		assertReferenceCount(pdom, "normalDeclaration1", 2);
		assertReferenceCount(pdom, "normalDeclaration2", 3);
		assertReferenceCount(pdom, "forwardDeclaration", 2);
	}

	public void testParameters() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "normalCPPFunction");
		assertEquals(1, bindings.length);
		ICPPFunction function = (ICPPFunction) bindings[0];
		IParameter[] parameters = function.getParameters();
		assertEquals(IBasicType.t_int, ((ICPPBasicType) parameters[0].getType()).getType());
		assertEquals("p1", parameters[0].getName());
		assertEquals(IBasicType.t_char, ((ICPPBasicType) parameters[1].getType()).getType());
		assertEquals("p2", parameters[1].getName());
		assertEquals(IBasicType.t_float, ((ICPPBasicType) parameters[2].getType()).getType());
		assertEquals("p3", parameters[2].getName());
	}
	
	public void testStorageClassSpecParameters() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "storageClassCPPFunction");
		assertEquals(1, bindings.length);
		ICPPFunction function = (ICPPFunction) bindings[0];
		IParameter[] parameters = function.getParameters();
		assertEquals(2, parameters.length);
		assertEquals(true, parameters[0].isRegister());
	}
	
	public void testExternCPPFunction() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "externCPPFunction");
		assertEquals(1, bindings.length);
		assertTrue(((ICPPFunction) bindings[0]).isExtern());
	}

	public void testStaticCPPFunction() throws Exception {
		// static elements cannot be found on global scope, see bug 161216
		IBinding[] bindings = findUnqualifiedName(pdom, "staticCPPFunction");
		assertEquals(1, bindings.length);
		assertTrue(((ICPPFunction) bindings[0]).isStatic());
	}

	public void testInlineCPPFunction() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "inlineCPPFunction");
		assertEquals(1, bindings.length);
		assertTrue(((ICPPFunction) bindings[0]).isInline());
	}
	
	public void testVarArgsCPPFunction() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "varArgsCPPFunction");
		assertEquals(1, bindings.length);
		assertTrue(((ICPPFunction) bindings[0]).takesVarArgs());
	}
	
	public void testForwardDeclarationType() throws Exception {
		assertType(pdom, "forwardDeclaration", ICPPFunction.class);		
	}
	
	public void testForwardDeclaration() throws Exception {
		assertDeclarationCount(pdom, "forwardDeclaration", 2);
		assertDefinitionCount(pdom, "forwardDeclaration", 1);
	}
	
	public void testVoidFunction() throws Exception {
		assertReturnType(pdom, "voidCPPFunction", IBasicType.t_void);
	}

	public void testIntFunction() throws Exception {
		assertReturnType(pdom, "intCPPFunction", IBasicType.t_int);
	}

	public void testDoubleFunction() throws Exception {
		assertReturnType(pdom, "doubleCPPFunction", IBasicType.t_double);
	}

	public void testCharFunction() throws Exception {
		assertReturnType(pdom, "charCPPFunction", IBasicType.t_char);
	}

	public void testFloatFunction() throws Exception {
		assertReturnType(pdom, "floatCPPFunction", IBasicType.t_float);
	}

	public void testOverloadedFunction() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "overloadedFunction");
		assertEquals(2, bindings.length);
		boolean[] seen = new boolean[2];

		for (int i = 0; i < 2; i++) {
			ICPPFunction function = (ICPPFunction) bindings[i];
			assertEquals(1, pdom.findNames(function, IIndex.FIND_DECLARATIONS_DEFINITIONS).length);
			assertEquals(1, pdom.findNames(function, IIndex.FIND_DEFINITIONS).length);
			IParameter[] parameters = function.getParameters();
			switch (parameters.length) {
			case 0:
				assertFalse(seen[0]);
				assertEquals(1, pdom.findNames(function, IIndex.FIND_REFERENCES).length);
				seen[0] = true;
				break;
			case 1:
				assertFalse(seen[1]);
				assertEquals(2, pdom.findNames(function, IIndex.FIND_REFERENCES).length);
				assertEquals("p1", parameters[0].getName());
				assertEquals(IBasicType.t_int, ((ICPPBasicType) parameters[0].getType()).getType());
				seen[1] = true;
				break;
			default:
				fail();
			}
		}

		for (boolean element : seen) {
			assertTrue(element);
		}
	}

	private void assertReturnType(PDOM pdom, String name, int type) throws CoreException, DOMException {
		IBinding[] bindings = findQualifiedName(pdom, name);
		assertEquals(1, bindings.length);
		IFunction function = (IFunction) bindings[0];
		IFunctionType functionType = function.getType();
		assertEquals(type, ((ICPPBasicType) functionType.getReturnType()).getType());
	}
}
