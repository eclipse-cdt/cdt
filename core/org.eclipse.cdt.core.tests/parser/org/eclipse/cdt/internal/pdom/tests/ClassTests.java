/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.util.regex.Pattern;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;

/**
 * @author Doug Schaefer
 *
 */
public class ClassTests extends PDOMTestBase {

	protected PDOM pdom;

	public static Test suite() {
		return suite(ClassTests.class, "_");
	}

	@Override
	protected void setUp() throws Exception {
		if (pdom == null) {
			ICProject project = createProject("classTests");
			pdom = (PDOM)CCoreInternals.getPDOMManager().getPDOM(project);
		}
		pdom.acquireReadLock();
	}

	@Override
	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
	}
	
	public void test1() throws Exception {
		IBinding[] Bs = pdom.findBindings(Pattern.compile("B"), true, IndexFilter.ALL, npm());
		assertEquals(1, Bs.length);
		ICPPClassType B = (ICPPClassType)Bs[0];
		ICPPMethod[] Bmethods = B.getAllDeclaredMethods();
		assertEquals(4, Bmethods.length);
		assertNotNull(findMethod(Bmethods, "B"));
		assertNotNull(findMethod(Bmethods, "A"));
		assertNotNull(findMethod(Bmethods, "bf"));
		ICPPMethod Bf = findMethod(Bmethods, "f");
		assertNotNull(Bf);
		IName [] Bf_refs = pdom.findNames(Bf, IIndex.FIND_REFERENCES);
		assertEquals(1, Bf_refs.length);
		IASTFileLocation loc = Bf_refs[0].getFileLocation();
		assertEquals(offset("class.cpp", "b.f()") + 2, loc.getNodeOffset());
	}
	
	private ICPPMethod findMethod(ICPPMethod[] bmethods, String name) {
		for (ICPPMethod method : bmethods) {
			if (method.getName().equals(name)) {
				return method;
			}
		}
		return null;
	}

	public void testNested() throws Exception {
		IBinding[] bindings = pdom.findBindings(Pattern.compile("NestedA"), false, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, bindings.length);
		ICPPClassType NestedA = (ICPPClassType)bindings[0];
		ICPPClassType[] nested = NestedA.getNestedClasses();
		assertEquals(1, nested.length);
		ICPPClassType NestedB = nested[0];
		assertEquals("NestedB", NestedB.getName());
		IField[] fields = NestedB.getFields();
		assertEquals(1, fields.length);
		IField NestedB_x = fields[0];
		
		IName[] refs = pdom.findNames(NestedB, IIndex.FIND_REFERENCES);
		assertEquals(1, refs.length);
		IASTFileLocation loc = refs[0].getFileLocation();
		assertEquals(offset("nested.cpp", "::NestedB") + 2, loc.getNodeOffset());
		
		refs = pdom.findNames(NestedB_x, IIndex.FIND_REFERENCES);
		assertEquals(1, refs.length);
		loc = refs[0].getFileLocation();
		assertEquals(offset("nested.cpp", "x.x") + 2, loc.getNodeOffset());
	}
	
	public void test147903() throws Exception {
		IBinding[] bindings = pdom.findBindings(Pattern.compile("pr147903"), false, IndexFilter.ALL, npm());
		assertEquals(1, bindings.length);
		ICPPNamespaceScope ns = ((ICPPNamespace)bindings[0]).getNamespaceScope();
		bindings = ns.find("testRef");
		assertEquals(1, bindings.length);
		IName[] refs = pdom.findNames(bindings[0], IIndex.FIND_REFERENCES);
//		for (int i = 0; i < refs.length; ++i)
//			System.out.println(refs[i].getFileLocation().getNodeOffset());
		assertEquals(5, refs.length);
	}
	
	/* Test friend relationships between classes */
	public void testFriend() throws Exception {
		IBinding[] bindings = pdom.findBindings(Pattern.compile("ClassA"), true, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, bindings.length);
		ICPPClassType classA = (ICPPClassType) bindings[0];

		bindings = pdom.findBindings(Pattern.compile("ClassC"), true, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, bindings.length);
		ICPPClassType classC = (ICPPClassType) bindings[0];

		bindings = pdom.findBindings(Pattern.compile("functionB"), false, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, bindings.length);
		ICPPFunction funcB = (ICPPFunction) bindings[0];

		IBinding[] friends = classA.getFriends();
		assertEquals(1, friends.length);
		assertEquals(classC, friends[0]); //ClassC is a friend class of ClassA
		
		friends = classC.getFriends();
		assertEquals(1, friends.length);
		assertEquals(funcB, friends[0]); //functionB is a friend of ClassC
	}
	
	public void noTest_testConstructor() throws Exception {
		// the source does not define Class1, so it is no surprise that the test is failing.
		//TODO PDOM doesn't have information on constructor
		IBinding[] bindings = pdom.findBindings(Pattern.compile("Class1"), false, IndexFilter.ALL, npm());
		assertEquals(2, bindings.length);
		assertTrue(bindings[0] instanceof ICPPClassType);
		assertTrue(bindings[1] instanceof ICPPMethod);
		
		IName[] decls = pdom.findNames(bindings[1], IIndex.FIND_DECLARATIONS_DEFINITIONS);
		assertEquals(2, decls.length);
		IASTFileLocation loc = decls[0].getFileLocation();
		assertEquals(offset("constructor.cpp","Class1(int num);"), loc.getNodeOffset()); //character offset	
		
		loc = decls[1].getFileLocation();
		assertEquals(offset("constructor.cpp","Class1::Class1") + 8, loc.getNodeOffset()); //character offset
		
		/* Member initialization */
		bindings = pdom.findBindings(Pattern.compile("number"), false, IndexFilter.ALL, npm());
		assertEquals(1, bindings.length);
		
		IName[] refs = pdom.findNames(bindings[0], IIndex.FIND_REFERENCES);
		assertEquals(1, refs.length);
		loc = refs[0].getFileLocation();
		assertEquals(offset("constructor.cpp","number(num)"), loc.getNodeOffset()); //character offset	
		
		assertEquals(bindings[0], ((PDOMName)refs[0]).getBinding());
	}
	
	public void testAbsenceOfDefaultConstructorWhenExplicitNonDefaultPresentA() throws Exception {
		IndexFilter JUST_CONSTRUCTORS= new IndexFilter() {
			@Override
			public boolean acceptBinding(IBinding binding) {
				return binding instanceof ICPPConstructor;
			}
		};
		IBinding[] bindings = pdom.findBindings(Pattern.compile("C"), false, JUST_CONSTRUCTORS, npm());
		// expecting C(int) and C(const C &)
		assertEquals(2, bindings.length);
	}
	
	public void testAbsenceOfDefaultConstructorWhenExplicitNonDefaultPresentB() throws Exception {
		IndexFilter JUST_CONSTRUCTORS= new IndexFilter() {
			@Override
			public boolean acceptBinding(IBinding binding) {
				return binding instanceof ICPPConstructor;
			}
		};
		IBinding[] bindings = pdom.findBindings(Pattern.compile("D"), false, JUST_CONSTRUCTORS, npm());
		// expecting just D(D &)
		assertEquals(1, bindings.length);
	}
	
	public void testClassScope_bug185408() throws Exception {
		char[][] name= {"B".toCharArray(), "bf".toCharArray()};
		IBinding[] bindings= pdom.findBindings(name, IndexFilter.ALL, npm());
		assertEquals(1, bindings.length);
		IScope classScope= bindings[0].getScope();
		
		assertTrue(classScope instanceof ICPPClassScope);
		bindings= classScope.find("bf");
		ICPPMethod method= extractSingleMethod(bindings);
		assertEquals(method.getQualifiedName()[0], "B");

		bindings= classScope.find("f");
		method= extractSingleMethod(bindings);
		assertEquals(method.getQualifiedName()[0], "A");

		bindings= classScope.find("B");
		ICPPClassType classType= extractSingleClass(bindings);
		assertEquals(classType.getQualifiedName()[0], "B");

		bindings= classScope.find("A");
		classType= extractSingleClass(bindings);
		assertEquals(classType.getQualifiedName()[0], "A");
	}

	private ICPPMethod extractSingleMethod(IBinding[] bindings) {
		assertEquals(1, bindings.length);
		assertTrue(bindings[0] instanceof ICPPMethod);
		return (ICPPMethod) bindings[0];
	}
	
	private ICPPClassType extractSingleClass(IBinding[] bindings) {
		assertEquals(1, bindings.length);
		assertTrue(bindings[0] instanceof ICPPClassType);
		return (ICPPClassType) bindings[0];
	}
}
