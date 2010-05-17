/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial implementation
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class CPPFunctionTemplateTests extends PDOMTestBase {
	protected PDOM pdom;
	protected ICProject cproject;
	
	public static Test suite() {
		return suite(CPPFunctionTemplateTests.class);
	}
	
	@Override
	public void setUp() throws Exception {
		cproject= CProjectHelper.createCCProject("functionTemplateTests"+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);		
	}
	
	protected void setUpSections(int sections) throws Exception {
		StringBuffer[] contents= TestSourceReader.getContentsForTest(
				CTestPlugin.getDefault().getBundle(), "parser", getClass(), getName(), sections);
		for (StringBuffer content : contents) {
			IFile file= TestSourceReader.createFile(cproject.getProject(), new Path("refs.cpp"), content.toString());
		}
		IndexerPreferences.set(cproject.getProject(), IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().reindex(cproject);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));
		pdom= (PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject);
		pdom.acquireReadLock();
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(pdom!=null) {
			pdom.releaseReadLock();
		}
		pdom= null;
		cproject.getProject().delete(true, npm());
	}
	
	/*************************************************************************/

	//	template<typename X>
	//	void foo(X x) {}
	//
	//	template<typename A, typename B>
	//	void foo(A a, B b) {}
	//
	//	class C1 {}; class C2 {}; class C3 {};
	//
	//	void bar() {
	//		foo<C1>(*new C1());
	//		foo<C2>(*new C2());
	//		foo<C3>(*new C3());
	//      foo<C1,C2>(*new C1(), *new C2());
	//      foo<C2,C3>(*new C2(), *new C3());
	//      foo<C3,C1>(*new C3(), *new C1());
	//      foo<C2,C1>(*new C2(), *new C1());
	//      foo<C3,C2>(*new C3(), *new C2());
	//      foo<C1,C3>(*new C1(), *new C3());
	//	}
	public void testSimpleInstantiation() throws Exception {
		setUpSections(1);
		IBinding[] bs= pdom.findBindings(new char[][]{"foo".toCharArray()}, IndexFilter.ALL_DECLARED, npm());
		assertEquals(2, bs.length);
		assertInstance(bs[0], ICPPFunctionTemplate.class);
		assertInstance(bs[1], ICPPFunctionTemplate.class);
		
		boolean b= ((ICPPFunctionTemplate)bs[0]).getTemplateParameters().length==1;
		ICPPFunctionTemplate fooX= (ICPPFunctionTemplate) bs[b ? 0 : 1];
		ICPPFunctionTemplate fooAB= (ICPPFunctionTemplate) bs[b ? 1 : 0];
		
		assertNameCount(pdom, fooX, IIndexFragment.FIND_REFERENCES, 3);
		assertNameCount(pdom, fooAB, IIndexFragment.FIND_REFERENCES, 6);
	}
}
