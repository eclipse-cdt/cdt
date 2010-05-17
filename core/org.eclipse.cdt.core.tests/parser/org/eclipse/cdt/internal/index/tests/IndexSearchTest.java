/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.index.tests;

import java.util.LinkedList;
import java.util.regex.Pattern;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.core.runtime.CoreException;

public class IndexSearchTest extends IndexTestBase {

	private static final IndexFilter INDEX_FILTER = IndexFilter.ALL_DECLARED;

	public static TestSuite suite() {
		TestSuite suite= suite(IndexSearchTest.class, "_");
		suite.addTest(new IndexSearchTest("deleteProject"));
		return suite;
	}

	private ICProject fProject= null;
	private IIndex fIndex= null;
	
	public IndexSearchTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		if (fProject == null) {
			fProject= createProject(true, "resources/indexTests/search");
		}
		fIndex= CCorePlugin.getIndexManager().getIndex(fProject);
		fIndex.acquireReadLock();
	}
	
	@Override
	public void tearDown() throws Exception {
		fIndex.releaseReadLock();
		super.tearDown();
	}
		
	public void deleteProject() {
		if (fProject != null) {
			CProjectHelper.delete(fProject);
		}
	}

	private void checkIsClass(IIndexBinding binding) {
		assertTrue(binding instanceof ICPPClassType);
	}

	private void checkIsNamespace(IIndexBinding binding) {
		assertTrue(binding instanceof ICPPNamespace);
	}

	private void checkIsEnumerator(IIndexBinding binding) {
		assertTrue(binding instanceof IEnumerator);
	}

	private void checkIsEnumeration(IIndexBinding binding) {
		assertTrue(binding instanceof IEnumeration);
	}

	private void checkIsFunction(IIndexBinding binding) {
		assertTrue(binding instanceof IFunction);
		assertTrue(!(binding instanceof ICPPMethod));
	}

	private void checkIsVariable(IIndexBinding binding) {
		assertTrue(binding instanceof IVariable);
		assertTrue(!(binding instanceof ICPPField));
	}

	public void testFindClassInNamespace() throws CoreException {
		String scl = "C160913";
		Pattern pcl= Pattern.compile(scl);
		String sns = "ns160913";
		Pattern pns= Pattern.compile(sns);
		
		IIndexBinding[] bindings;
		
		bindings= fIndex.findBindings(pcl, true, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsClass(bindings[0]);

		bindings= fIndex.findBindings(scl.toCharArray(), INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsClass(bindings[0]);

		bindings= fIndex.findBindings(pcl, false, INDEX_FILTER, npm());
		assertEquals(3, bindings.length);
		checkIsClass(bindings[0]);
		checkIsClass(bindings[1]);
		checkIsClass(bindings[2]);

		bindings= fIndex.findBindings(new Pattern[]{pns, pcl}, true, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsClass(bindings[0]);

		bindings= fIndex.findBindings(new char[][]{sns.toCharArray(), scl.toCharArray()}, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsClass(bindings[0]);

		bindings= fIndex.findBindings(new Pattern[]{pns, pcl}, false, INDEX_FILTER, npm());
		assertEquals(2, bindings.length);
		checkIsClass(bindings[0]);
		checkIsClass(bindings[1]);

		bindings= fIndex.findBindings(new Pattern[]{pns, pns, pcl}, true, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsClass(bindings[0]);

		bindings= fIndex.findBindings(new char[][]{sns.toCharArray(), sns.toCharArray(), scl.toCharArray()}, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsClass(bindings[0]);

		bindings= fIndex.findBindings(new Pattern[]{pns, pns, pcl}, false, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsClass(bindings[0]);
	}
	
	public void testFindNamespaceInNamespace() throws CoreException {
		Pattern pcl= Pattern.compile("C160913");
		Pattern pns= Pattern.compile("ns160913");
		char[] scl= pcl.pattern().toCharArray();
		char[] sns= pns.pattern().toCharArray();
		
		IIndexBinding[] bindings;

		bindings= fIndex.findBindings(pns, true, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsNamespace(bindings[0]);

		bindings= fIndex.findBindings(sns, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsNamespace(bindings[0]);

		bindings= fIndex.findBindings(pns, false, INDEX_FILTER, npm());
		assertEquals(2, bindings.length);
		checkIsNamespace(bindings[0]);
		checkIsNamespace(bindings[1]);

		bindings= fIndex.findBindings(new Pattern[]{pns, pns}, true, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsNamespace(bindings[0]);

		bindings= fIndex.findBindings(new char[][]{sns, sns}, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsNamespace(bindings[0]);

		bindings= fIndex.findBindings(new Pattern[]{pns, pns}, false, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsNamespace(bindings[0]);
	}

	public void testClassInUnnamedNamespace() throws CoreException {
		Pattern pcl= Pattern.compile("CInUnnamed160913");
		
		IIndexBinding[] bindings;

		// the binding in the unnamed namespace is not visible in global scope.
		bindings= fIndex.findBindings(pcl, true, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		assertTrue(bindings[0].isFileLocal());

		bindings= fIndex.findBindings(pcl.pattern().toCharArray(), INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		assertTrue(bindings[0].isFileLocal());
}

	public void testFindEnumerator() throws CoreException {
		Pattern pEnumeration= Pattern.compile("E20061017");
		Pattern pEnumerator= Pattern.compile("e20061017");
		char[] sEnumeration= pEnumeration.pattern().toCharArray();
		char[] sEnumerator= pEnumerator.pattern().toCharArray();

		IIndexBinding[] bindings;
		
		// enumerators are found in global scope
		bindings= fIndex.findBindings(pEnumerator, true, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsEnumerator(bindings[0]);

		bindings= fIndex.findBindings(pEnumerator, false, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsEnumerator(bindings[0]);

		bindings= fIndex.findBindings(new Pattern[]{pEnumeration, pEnumerator}, true, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);

		bindings= fIndex.findBindings(new char[][]{sEnumeration, sEnumerator}, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);

		bindings= fIndex.findBindings(new Pattern[]{pEnumeration, pEnumerator}, false, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		
		bindings= fIndex.findBindings(pEnumeration, true, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsEnumeration(bindings[0]);

		bindings= fIndex.findBindings(sEnumeration, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsEnumeration(bindings[0]);

		bindings= fIndex.findBindings(pEnumeration, false, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		checkIsEnumeration(bindings[0]);
	}

	public void testCaseInsensitivePatternSearch_239669() throws CoreException {
		IIndexBinding[] bindings;

		Pattern pEnumAndEnumeration= Pattern.compile("E20061017", Pattern.CASE_INSENSITIVE);
		Pattern pEnumeration= Pattern.compile("E20061017");
		bindings= fIndex.findBindings(pEnumAndEnumeration, true, INDEX_FILTER, npm());
		assertEquals(2, bindings.length);
		bindings= fIndex.findBindings(pEnumeration, true, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);

		pEnumAndEnumeration= Pattern.compile("E2006101.*", Pattern.CASE_INSENSITIVE);
		pEnumeration= Pattern.compile("E2006101.*");
		bindings= fIndex.findBindings(pEnumAndEnumeration, true, INDEX_FILTER, npm());
		assertEquals(2, bindings.length);
		bindings= fIndex.findBindings(pEnumeration, true, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
		
		Pattern macro1= Pattern.compile("Foo", Pattern.CASE_INSENSITIVE);
		Pattern macro2= Pattern.compile("Foo");
		bindings= fIndex.findMacroContainers(macro1, INDEX_FILTER, npm());
		assertEquals(2, bindings.length);
		bindings= fIndex.findMacroContainers(macro2, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);

		macro1= Pattern.compile("Foo.*", Pattern.CASE_INSENSITIVE);
		macro2= Pattern.compile("Foo.*");
		bindings= fIndex.findMacroContainers(macro1, INDEX_FILTER, npm());
		assertEquals(2, bindings.length);
		bindings= fIndex.findMacroContainers(macro2, INDEX_FILTER, npm());
		assertEquals(1, bindings.length);
	}

	
	public void testFindStatic_161216() throws CoreException {
		Pattern pFunc= Pattern.compile("staticFunc20061017");
		Pattern pVar= Pattern.compile("staticVar20061017");
		
		IIndexBinding[] bindings;
		
		bindings= fIndex.findBindings(pFunc, false, INDEX_FILTER, npm());
		assertEquals(2, bindings.length);
		checkIsFunction(bindings[0]);
		checkIsFunction(bindings[1]);

		bindings= fIndex.findBindings(pVar, false, INDEX_FILTER, npm());
		assertEquals(2, bindings.length);
		checkIsVariable(bindings[0]);
		checkIsVariable(bindings[1]);
	}
	
	public void testSanityOfMayHaveChildren() throws CoreException {
		PDOM pdom= (PDOM) ((CIndex) fIndex).getPrimaryFragments()[0];
		pdom.accept(new IPDOMVisitor() {
			LinkedList stack= new LinkedList();
			public boolean visit(IPDOMNode node) throws CoreException {
				if (!stack.isEmpty()) {
					Object last= stack.getLast();
					if (last instanceof PDOMBinding) {
						assertTrue(((PDOMBinding) last).mayHaveChildren());
					}
				}
				stack.add(node);
				return true;
			}
			public void leave(IPDOMNode node) throws CoreException {
				assertEquals(stack.removeLast(), node);
			}
		});
	}
}
