/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.util.StringUtil;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import junit.framework.Test;

/**
 * Test the correctness of C/C++ searches.
 *
 * @author Vivian Kong
 */
public class PDOMSearchTest extends PDOMTestBase {
	final Comparator<IBinding> BINDING_COMPARATOR = new Comparator<IBinding>() {
		@Override
		public int compare(IBinding b1, IBinding b2) {
			return b1.getName().compareTo(b2.getName());
		}
	};

	protected ICProject project;
	protected PDOM pdom;
	protected IProgressMonitor NULL_MONITOR = new NullProgressMonitor();
	protected IndexFilter INDEX_FILTER = IndexFilter.ALL_DECLARED;

	public static Test suite() {
		return suite(PDOMSearchTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		project = createProject("searchTests", true);
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

	/**
	 * Tests the members inside namespaces
	 */
	public void testNamespaces() throws Exception {
		/* Members in the namespace */
		IBinding[] namespaces = pdom.findBindings(Pattern.compile("namespace1"), false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, namespaces.length);
		assertTrue(namespaces[0] instanceof ICPPNamespace);
		ICPPNamespace namespace1 = (ICPPNamespace) namespaces[0];

		/* Consistent search results */

		// Searching for "namespace1::namespace2"
		Pattern[] patterns = { Pattern.compile("namespace1"), Pattern.compile("namespace2") };
		namespaces = pdom.findBindings(patterns, true, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, namespaces.length);
		assertTrue(namespaces[0] instanceof ICPPNamespace);
		ICPPNamespace namespace2 = (ICPPNamespace) namespaces[0];

		// Searching for "namespace2"
		namespaces = pdom.findBindings(Pattern.compile("namespace2"), false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, namespaces.length);
		assertTrue(namespaces[0] instanceof ICPPNamespace);
		assertEquals(namespace2, namespaces[0]);

		/* Namespace references */
		IName[] refs = pdom.findNames(namespace1, IIndex.FIND_REFERENCES);
		assertEquals(3, refs.length);
		IASTFileLocation loc = refs[0].getFileLocation();
		assertEquals(offset("main.cpp", "namespace1::Class1"), loc.getNodeOffset()); //character offset
		loc = refs[1].getFileLocation();
		assertEquals(offset("Class1.cpp", "namespace1::Class1::~Class1()"), loc.getNodeOffset()); //character offset
		loc = refs[2].getFileLocation();
		assertEquals(offset("Class1.cpp", "namespace1::Class1::Class1()"), loc.getNodeOffset()); //character offset

		/* Namespace declaration */
		IName[] decls = pdom.findNames(namespace1, IIndex.FIND_DECLARATIONS);
		assertEquals(0, decls.length);

		/* Namespace definition */
		IName[] defs = pdom.findNames(namespace1, IIndex.FIND_DEFINITIONS);
		assertEquals(1, defs.length);
		loc = defs[0].getFileLocation();
		assertEquals(offset("Class1.h", "namespace namespace1") + 10, loc.getNodeOffset()); //character offset
	}

	public void testAnonymousNamespace_460646() throws Exception {
		char[][] name = new char[][] { "ns1".toCharArray(), "ns2".toCharArray(), "Class3".toCharArray() };
		IIndexBinding[] bindings = pdom.findBindings(name, IndexFilter.ALL, npm());
		assertEquals(1, bindings.length);
		assertTrue(bindings[0] instanceof ICPPClassType);
		assertEquals("ns1::ns2::Class3", getQualifiedName(bindings[0]));
	}

	public void testClasses_160913() throws Exception {
		// classes and nested classes

		/* Search for "Class1" */
		IBinding[] class1s = pdom.findBindings(Pattern.compile("Class1"), false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(4, class1s.length);
		assertTrue(class1s[0] instanceof ICPPClassType);
		assertTrue(class1s[1] instanceof ICPPClassType);
		assertTrue(class1s[2] instanceof ICPPClassType);
		assertTrue(class1s[3] instanceof ICPPMethod);

		/** result #1 * */
		ICPPClassType class1 = (ICPPClassType) class1s[0];
		assertEquals("Class1", getQualifiedName(class1));
		IBinding[] methods = class1.getDeclaredMethods();
		assertEquals(0, methods.length);

		/** result #2 * */
		ICPPClassType class2 = (ICPPClassType) class1s[1];
		assertEquals("namespace1::Class1", getQualifiedName(class2));

		/* Members in this class */

		// methods
		methods = class2.getDeclaredMethods();
		assertEquals(2, methods.length);
		if (methods[0].getName().equals("~Class1")) {
			IBinding h = methods[1];
			methods[1] = methods[0];
			methods[0] = h;
		}
		assertEquals("Class1", methods[0].getName());
		assertEquals("~Class1", methods[1].getName());

		// nested class
		IBinding[] nested = class2.getNestedClasses();
		assertEquals(1, nested.length);
		assertEquals("Class2", nested[0].getName());

		// fields
		IBinding[] fields = class2.getFields();
		assertEquals(2, fields.length);
		Arrays.sort(fields, BINDING_COMPARATOR);
		assertEquals("class1x", fields[0].getName());
		assertEquals("class1y", fields[1].getName());

		/** result #3 * */
		ICPPMethod method3 = (ICPPMethod) class1s[3];
		assertEquals("namespace1::Class1::Class1", getQualifiedName(method3));
		assertEquals(method3, methods[0]);

		/** result #4 * */
		ICPPClassType class4 = (ICPPClassType) class1s[2];
		assertEquals("namespace1::namespace2::Class1", getQualifiedName(class4));
		methods = class4.getDeclaredMethods();
		assertEquals(0, methods.length);

		/* Search for "Class2" */
		IBinding[] class2s = pdom.findBindings(Pattern.compile("Class2"), false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(4, class2s.length);
		assertTrue(class2s[0] instanceof ICPPClassType);
		assertTrue(class2s[1] instanceof ICPPClassType);
		assertTrue(class2s[2] instanceof ICPPClassType);
		assertTrue(class2s[3] instanceof ICPPMethod);

		/** result #1 * */
		ICPPClassType cls1 = (ICPPClassType) class2s[0];
		assertEquals("Class2", getQualifiedName(cls1));
		methods = cls1.getDeclaredMethods();
		assertEquals(3, methods.length);
		Arrays.sort(methods, BINDING_COMPARATOR);
		assertEquals("Class2", methods[0].getName());
		assertEquals("~Class2", methods[2].getName());
		assertEquals("foo", methods[1].getName());

		/** result #2 * */
		ICPPMethod meth2 = (ICPPMethod) class2s[3];
		assertEquals("Class2::Class2", getQualifiedName(meth2));
		assertEquals(meth2, methods[0]);

		/** result #3 * */
		ICPPClassType cls3 = (ICPPClassType) class2s[1];
		assertEquals("namespace1::Class1::Class2", getQualifiedName(cls3));

		/** result #3 * */
		ICPPClassType cls4 = (ICPPClassType) class2s[2];
		assertEquals("namespace1::Class2", getQualifiedName(cls4));

		/* Nested class references - namespace1::Class1::Class2 */
		IName[] refs = pdom.findNames(cls3, IIndex.FIND_REFERENCES);
		assertEquals(0, refs.length);

		/* Nested class declaration */
		IName[] decls = pdom.findNames(cls3, IIndex.FIND_DECLARATIONS);
		assertEquals(0, decls.length);

		/* Nested class definition */
		IName[] defs = pdom.findNames(cls3, IIndex.FIND_DEFINITIONS);
		assertEquals(1, defs.length);
		IASTFileLocation loc = defs[0].getFileLocation();
		assertEquals(offset("Class1.h", "class Class2 { //namespace1::Class1::Class2") + 6, loc.getNodeOffset()); //character offset
	}

	public void testFunction() throws Exception {
		IBinding[] functions = pdom.findBindings(Pattern.compile("foo2"), false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, functions.length);
		assertTrue(functions[0] instanceof ICPPFunction);
		assertEquals("foo2", getQualifiedName(functions[0]));

		functions = pdom.findBindings(Pattern.compile("main"), false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, functions.length);
		assertTrue(functions[0] instanceof ICPPFunction);
		assertEquals("main", getQualifiedName(functions[0]));
	}

	public void testMethods() throws Exception {
		IBinding[] methods = pdom.findBindings(Pattern.compile("~Class2"), false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, methods.length);
		assertTrue(methods[0] instanceof ICPPMethod);
		assertEquals("Class2::~Class2", getQualifiedName(methods[0]));
	}

	public void testFields() throws Exception {
		IBinding[] fields = pdom.findBindings(Pattern.compile("class1x"), false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, fields.length);
		assertTrue(fields[0] instanceof ICPPField);
		assertEquals("namespace1::Class1::class1x", getQualifiedName(fields[0]));

		fields = pdom.findBindings(Pattern.compile("class1y"), false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, fields.length);
		assertTrue(fields[0] instanceof ICPPField);
		assertEquals("namespace1::Class1::class1y", getQualifiedName(fields[0]));
	}

	public void testVariables() throws Exception {
		IBinding[] variables = pdom.findBindings(Pattern.compile("var"), false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, variables.length);
		assertTrue(variables[0] instanceof ICPPVariable);
		assertEquals("var", getQualifiedName(variables[0]));

		/* Variable references */
		IName[] refs = pdom.findNames(variables[0], IIndex.FIND_REFERENCES);
		assertEquals(1, refs.length);
		IASTFileLocation loc = refs[0].getFileLocation();
		assertEquals(offset("main.cpp", "var = 0;"), loc.getNodeOffset()); // character offset

		/* Variable declaration */
		IName[] decls = pdom.findNames(variables[0], IIndex.FIND_DECLARATIONS_DEFINITIONS);
		assertEquals(1, decls.length);
		loc = decls[0].getFileLocation();
		assertEquals(offset("main.cpp", "int var;") + 4, loc.getNodeOffset()); // character offset

		/* Variable definition */
		IName[] defs = pdom.findNames(variables[0], IIndex.FIND_DEFINITIONS);
		assertEquals(1, defs.length);
		loc = defs[0].getFileLocation();
		assertEquals(offset("main.cpp", "int var;") + 4, loc.getNodeOffset()); // character offset
	}

	/**
	 * Returns the fully qualified name for a given binding.
	 */
	private String getQualifiedName(IBinding binding) throws CoreException {
		PDOMBinding pdomBinding = pdom.getLinkageImpls()[0].adaptBinding(binding);
		return StringUtil.join(pdomBinding.getQualifiedName(), "::");
	}
}
