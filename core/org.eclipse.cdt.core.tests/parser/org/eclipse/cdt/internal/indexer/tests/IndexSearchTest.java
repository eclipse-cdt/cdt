/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.indexer.tests;

import java.util.LinkedList;
import java.util.regex.Pattern;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class IndexSearchTest extends BaseTestCase {

	private static final IndexFilter INDEX_FILTER = new IndexFilter();
	private static final IProgressMonitor NPM= new NullProgressMonitor();

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

	public void setUp() throws Exception {
		super.setUp();
		if (fProject == null) {
			createProject();
		}
		fIndex= CCorePlugin.getIndexManager().getIndex(fProject);
	}
	
	public void tearDown() throws Exception {
		fIndex.releaseReadLock();
		super.tearDown();
	}
		
	
	private void createProject() throws CoreException {
		// Create the project
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				fProject= CProjectHelper.createCProject("IndexSearchTest_" + System.currentTimeMillis(), null);
				
				CCorePlugin.getPDOMManager().setIndexerId(fProject, IPDOMManager.ID_NO_INDEXER);
				CProjectHelper.importSourcesFromPlugin(fProject, CTestPlugin.getDefault().getBundle(), "resources/indexTests/search");
				CCorePlugin.getPDOMManager().setIndexerId(fProject, IPDOMManager.ID_FAST_INDEXER);
				
				// wait until the indexer is done
				assertTrue(CCorePlugin.getIndexManager().joinIndexer(5000, new NullProgressMonitor()));
			}
		}, null);
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

	public void testFindClassInNamespace() throws CoreException {
		Pattern pcl= Pattern.compile("C160913");
		Pattern pns= Pattern.compile("ns160913");
		
		IIndexBinding[] bindings;
		
		bindings= fIndex.findBindings(pcl, true, INDEX_FILTER, NPM);
		assertEquals(1, bindings.length);
		checkIsClass(bindings[0]);

		bindings= fIndex.findBindings(pcl, false, INDEX_FILTER, NPM);
		assertEquals(3, bindings.length);
		checkIsClass(bindings[0]);
		checkIsClass(bindings[1]);
		checkIsClass(bindings[2]);

		bindings= fIndex.findBindings(new Pattern[]{pns, pcl}, true, INDEX_FILTER, NPM);
		assertEquals(1, bindings.length);
		checkIsClass(bindings[0]);

		bindings= fIndex.findBindings(new Pattern[]{pns, pcl}, false, INDEX_FILTER, NPM);
		assertEquals(2, bindings.length);
		checkIsClass(bindings[0]);
		checkIsClass(bindings[1]);

		bindings= fIndex.findBindings(new Pattern[]{pns, pns, pcl}, true, INDEX_FILTER, NPM);
		assertEquals(1, bindings.length);
		checkIsClass(bindings[0]);

		bindings= fIndex.findBindings(new Pattern[]{pns, pns, pcl}, false, INDEX_FILTER, NPM);
		assertEquals(1, bindings.length);
		checkIsClass(bindings[0]);
	}
	
	public void testFindNamespaceInNamespace() throws CoreException {
		Pattern pcl= Pattern.compile("C160913");
		Pattern pns= Pattern.compile("ns160913");
		
		IIndexBinding[] bindings;

		bindings= fIndex.findBindings(pns, true, INDEX_FILTER, NPM);
		assertEquals(1, bindings.length);
		checkIsNamespace(bindings[0]);

		bindings= fIndex.findBindings(pns, false, INDEX_FILTER, NPM);
		assertEquals(2, bindings.length);
		checkIsNamespace(bindings[0]);
		checkIsNamespace(bindings[1]);

		bindings= fIndex.findBindings(new Pattern[]{pns, pns}, true, INDEX_FILTER, NPM);
		assertEquals(1, bindings.length);
		checkIsNamespace(bindings[0]);

		bindings= fIndex.findBindings(new Pattern[]{pns, pns}, false, INDEX_FILTER, NPM);
		assertEquals(1, bindings.length);
		checkIsNamespace(bindings[0]);
	}

	public void testClassInUnnamedNamespace1() throws CoreException {
		Pattern pcl= Pattern.compile("CInUnnamed160913");
		
		IIndexBinding[] bindings;

		bindings= fIndex.findBindings(pcl, false, INDEX_FILTER, NPM);
		assertEquals(1, bindings.length);
		checkIsClass(bindings[0]);
	}

	public void _testClassInUnnamedNamespace2() throws CoreException {
		Pattern pcl= Pattern.compile("CInUnnamed160913");
		
		IIndexBinding[] bindings;

		// the binding in the unnamed namespace is not visible in global scope.
		bindings= fIndex.findBindings(pcl, true, INDEX_FILTER, NPM);
		assertEquals(0, bindings.length);
	}

	public void testFindEnumerator() throws CoreException {
		Pattern pEnumeration= Pattern.compile("E20061017");
		Pattern pEnumerator= Pattern.compile("e20061017");
		
		IIndexBinding[] bindings;
		
		// enumerators are found in global scope
		bindings= fIndex.findBindings(pEnumerator, true, INDEX_FILTER, NPM);
		assertEquals(1, bindings.length);
		checkIsEnumerator(bindings[0]);

		bindings= fIndex.findBindings(pEnumerator, false, INDEX_FILTER, NPM);
		assertEquals(1, bindings.length);
		checkIsEnumerator(bindings[0]);

		bindings= fIndex.findBindings(new Pattern[]{pEnumeration, pEnumerator}, true, INDEX_FILTER, NPM);
		assertEquals(0, bindings.length);

		bindings= fIndex.findBindings(new Pattern[]{pEnumeration, pEnumerator}, false, INDEX_FILTER, NPM);
		assertEquals(0, bindings.length);
		
		bindings= fIndex.findBindings(pEnumeration, true, INDEX_FILTER, NPM);
		assertEquals(1, bindings.length);
		checkIsEnumeration(bindings[0]);

		bindings= fIndex.findBindings(pEnumeration, false, INDEX_FILTER, NPM);
		assertEquals(1, bindings.length);
		checkIsEnumeration(bindings[0]);
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
