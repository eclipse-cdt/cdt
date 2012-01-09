/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.pdom.tests;

import java.util.regex.Pattern;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Tests for verifying whether the PDOM correctly stores information about
 * C++ namespaces.
 *
 * @author Vivian Kong
 */
public class NamespaceTests extends PDOMTestBase {
	protected ICProject project;	
	protected PDOM pdom;
	protected IProgressMonitor NULL_MONITOR = new NullProgressMonitor();
	protected IndexFilter INDEX_FILTER = IndexFilter.ALL;
	
	public static Test suite() {
		return suite(NamespaceTests.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		if (pdom == null) {
			project = createProject("namespaceTests", true);
			pdom = (PDOM)CCoreInternals.getPDOMManager().getPDOM(project);
		}
		pdom.acquireReadLock();
	}
	
	@Override
	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
		if (project != null) {
			project.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
	}
	
	public void testAlias() throws Exception {
		/* Find all the namespace */
		IBinding[] namespaces = pdom.findBindings(Pattern.compile("namespace1"), false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, namespaces.length);
		assertTrue(namespaces[0] instanceof ICPPNamespace);
		ICPPNamespace namespace1 = (ICPPNamespace) namespaces[0];
		IBinding[] members = namespace1.getMemberBindings();
		assertEquals(1, members.length);
		assertTrue(members[0] instanceof ICPPNamespace);
		assertEquals("namespace2", ((ICPPNamespace) members[0]).getName()); //nested namespace
		ICPPNamespace namespace2 = (ICPPNamespace) members[0];

		namespaces = pdom.findBindings(Pattern.compile("namespaceNew"), false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, namespaces.length);
		assertTrue(namespaces[0] instanceof ICPPNamespace);
		assertTrue(namespaces[0] instanceof ICPPNamespaceAlias);
		ICPPNamespaceAlias namespaceAlias = (ICPPNamespaceAlias) namespaces[0];
		
		//TODO PDOM has no alias information
		// namespace2 and namespaceAlias should be referencing the same namespace
		assertEquals(namespace2, namespaceAlias.getBinding());
	}
	
	public void testNested() throws Exception {

		/* Find deeply nested namespace */
		Pattern[] patterns = {Pattern.compile("namespace1"), Pattern.compile("namespace2"), Pattern.compile("namespace3")};
		IBinding[] namespaces = pdom.findBindings(patterns, false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, namespaces.length);
		assertTrue(namespaces[0] instanceof ICPPNamespace);

	}
	
	public void testMemberDefinition() throws Exception {

		/* Find the definition of a member declared in a namespace */
		Pattern[] patterns = {Pattern.compile("namespace1"), Pattern.compile("namespace2"), Pattern.compile("foo")};
		IBinding[] members = pdom.findBindings(patterns, false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, members.length);
		assertTrue(members[0] instanceof ICPPFunction);
		
		IName[] decls = pdom.findNames(members[0], IIndex.FIND_DECLARATIONS);
		assertEquals(1, decls.length);
		IASTFileLocation loc = decls[0].getFileLocation();
		assertEquals(offset("namespace.cpp", "void foo()") + 5, loc.getNodeOffset()); //character offset	

		IName[] defs = pdom.findNames(members[0], IIndex.FIND_DEFINITIONS);
		assertEquals(1, defs.length);
		loc = defs[0].getFileLocation();
		assertEquals(offset("namespace.cpp", "::foo()") + 2, loc.getNodeOffset()); //character offset	

	}
	
	public void testExtend() throws Exception {

		/* Extending a namespace */		
		IBinding[] namespaces = pdom.findBindings(Pattern.compile("ns1"), false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, namespaces.length);
		assertTrue(namespaces[0] instanceof ICPPNamespace);
		ICPPNamespace namespace1 = (ICPPNamespace) namespaces[0];
		Pattern[] patterns = {Pattern.compile("ns1"), Pattern.compile("c")};
		IBinding[] members = pdom.findBindings(patterns, false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, members.length); //c was added by extending the namespace
	}
	
	public void testOverload() throws Exception {
		
		//Function overloading in namespace
		Pattern[] patterns = {Pattern.compile("ns3"), Pattern.compile("blah")};
		IBinding[] functions = pdom.findBindings(patterns, false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, functions.length);
		assertTrue(functions[0] instanceof ICPPFunction);
		ICPPFunction function = (ICPPFunction) functions[0];
		
		IName[] defs = pdom.findNames(function, IIndex.FIND_DEFINITIONS);
		assertEquals(1, defs.length);
		IASTFileLocation loc = defs[0].getFileLocation();
		assertEquals(offset("overload.cpp","void blah(char)") + 5, loc.getNodeOffset()); //character offset	
		
		IName[] decls = pdom.findNames(function, IIndex.FIND_DECLARATIONS_DEFINITIONS);
		assertEquals(1, decls.length);
		loc = decls[0].getFileLocation();
		assertEquals(offset("overload.cpp","void blah(char)") + 5, loc.getNodeOffset()); //character offset	
		
		IName[] refs = pdom.findNames(function, IIndex.FIND_REFERENCES);
		assertEquals(1, refs.length);
		loc = refs[0].getFileLocation();
		assertEquals(offset("overload.cpp","blah('a')"), loc.getNodeOffset()); //character offset	

	}
	
	public void testUnnamed() throws Exception {
		// test case for Bugzilla 162226
		/* Unnamed Namespace */
		IBinding[] functions = pdom.findBindings(Pattern.compile("function1"), true, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, functions.length);
		assertTrue(functions[0] instanceof ICPPFunction);
		ICPPFunction function = (ICPPFunction) functions[0];
		
		IName[] defs = pdom.findNames(function, IIndex.FIND_DEFINITIONS);
		assertEquals(1, defs.length);
		IASTFileLocation loc = defs[0].getFileLocation();
		assertEquals(offset("unnamed.cpp","void function1()") + 5, loc.getNodeOffset()); //character offset	
		
		IName[] decls = pdom.findNames(function, IIndex.FIND_DECLARATIONS_DEFINITIONS);
		assertEquals(1, decls.length);
		loc = decls[0].getFileLocation();
		assertEquals(offset("unnamed.cpp","void function1()") + 5, loc.getNodeOffset()); //character offset	
		
		IName[] refs = pdom.findNames(function, IIndex.FIND_REFERENCES);
		assertEquals(1, refs.length);
		loc = refs[0].getFileLocation();
		assertEquals(offset("unnamed.cpp","function1();"), loc.getNodeOffset()); //character offset	

	}
	
	public void testFriend() throws Exception {
		/* Friend in namespace - function2 is not in Class1*/
		// Bugzilla 162011
		IBinding[] functions = pdom.findBindings(Pattern.compile("function2"), false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, functions.length);
		assertTrue(functions[0] instanceof ICPPFunction);
		ICPPFunction function = (ICPPFunction) functions[0];
		
		IName[] defs = pdom.findNames(function, IIndex.FIND_DEFINITIONS);
		assertEquals(1, defs.length);
		IASTFileLocation loc = defs[0].getFileLocation();
		assertEquals(offset("friend.cpp","void function2(Class1){};") + 5, loc.getNodeOffset()); //character offset	
		
		IName[] decls = pdom.findNames(function, IIndex.FIND_DECLARATIONS);
		assertEquals(1, decls.length);
		loc = decls[0].getFileLocation();
		assertEquals(offset("friend.cpp","friend void function2(Class1);") + 12, loc.getNodeOffset()); //character offset	

		IName[] refs = pdom.findNames(function, IIndex.FIND_REFERENCES);
		assertEquals(1, refs.length);
		loc = refs[0].getFileLocation();
		assertEquals(offset("friend.cpp","ns4::function2(element)") + 5, loc.getNodeOffset()); //character offset	
		
	}
	
	public void testUsingDirective() throws Exception {
		//TODO need to test for PDOM?  or is it more for compiler?
		Pattern[] patterns = {Pattern.compile("ns4"), Pattern.compile("element")};
		IBinding[] variables = pdom.findBindings(patterns, false, INDEX_FILTER, NULL_MONITOR);
		assertEquals(1, variables.length);
		assertTrue(variables[0] instanceof ICPPVariable);
		ICPPVariable variable1 = (ICPPVariable) variables[0];
		
		IName[] defs = pdom.findNames(variable1, IIndex.FIND_DEFINITIONS);
		assertEquals(1, defs.length);
		IASTFileLocation loc = defs[0].getFileLocation();
		assertEquals(offset("friend.cpp","Class1 element;") + 7, loc.getNodeOffset()); //character offset	

		IName[] decls = pdom.findNames(variable1, IIndex.FIND_DECLARATIONS);
		assertEquals(0, decls.length);
		
		IName[] refs = pdom.findNames(variable1, IIndex.FIND_REFERENCES);
		assertEquals(2, refs.length);	
	}
}
