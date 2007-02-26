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

package org.eclipse.cdt.internal.index.tests;

import java.io.IOException;
import java.util.regex.Pattern;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public class EnclosingNamesTest extends BaseTestCase {
	private ICProject fCProject;
	protected IIndex fIndex;

	public EnclosingNamesTest(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return suite(EnclosingNamesTest.class);
	}

	protected void setUp() throws CoreException {
		fCProject= CProjectHelper.createCCProject("__encNamesTest__", "bin", IPDOMManager.ID_FAST_INDEXER);
		CCoreInternals.getPDOMManager().reindex(fCProject);
		fIndex= CCorePlugin.getIndexManager().getIndex(fCProject);
	}

	protected void tearDown() throws CoreException {
		if (fCProject != null) {
			CProjectHelper.delete(fCProject);
		}
	}

	protected IProject getProject() {
		return fCProject.getProject();
	}

	protected StringBuffer[] getContentsForTest(int blocks) throws IOException {
		return TestSourceReader.getContentsForTest(
				CTestPlugin.getDefault().getBundle(), "parser", getClass(), getName(), blocks);
	}

	protected IFile createFile(IContainer container, String fileName, String contents) throws Exception {
		return TestSourceReader.createFile(container, new Path(fileName), contents);
	}

	protected void waitForIndexer() {
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(10000, NPM));
	}

	protected Pattern[] getPattern(String qname) {
		String[] parts= qname.split("::");
		Pattern[] result= new Pattern[parts.length];
		for (int i = 0; i < result.length; i++) {
			result[i]= Pattern.compile(parts[i]);			
		}
		return result;
	}

	protected void waitUntilFileIsIndexed(IFile file, int time) throws Exception {
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, time);
	}
	
	// void func();
	// int var;
	//
	// void main() {
	//    func();
	//    var=1;
	// };
	public void testNestingWithFunction() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject().getProject(), "test.cpp", content);
		waitUntilFileIsIndexed(file, 4000);
		
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] mainBS= fIndex.findBindings(getPattern("main"), true, IndexFilter.ALL, NPM);
			assertLength(1, mainBS);
			IIndexBinding mainB= mainBS[0];
			
			IIndexName[] names= fIndex.findDefinitions(mainB);
			assertLength(1, names);
			IIndexName main= names[0];
			
			assertNull(main.getEnclosingDefinition());
			IIndexName[] enclosed= main.getEnclosedNames();
			assertLength(2, enclosed);
			assertName("func", enclosed[0]);
			assertName("var", enclosed[1]);
			
			IIndexName enclosing= enclosed[0].getEnclosingDefinition();
			assertNotNull(enclosing);
			assertName("main", enclosing);

			enclosing= enclosed[1].getEnclosingDefinition();
			assertNotNull(enclosing);
			assertName("main", enclosing);			
		}
		finally {
			fIndex.releaseReadLock();
		}
	}

	private void assertName(String name, IIndexName iname) {
		assertEquals(name, new String(iname.toCharArray()));
	}

	private void assertLength(int length, Object[] array) {
		assertNotNull(array);
		assertEquals(length, array.length);
	}
	
	// class C {
	// public:
	//    void func();
	//    int var;
	// };
	//
	// void main() {
	//    C c;
	//    c.func();
	//    c.var=1;
	// };
	// void C::func() {
	//    func();
	//    var=1;
	// };
	
	
	public void testNestingWithMethod() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject().getProject(), "test.cpp", content);
		waitUntilFileIsIndexed(file, 4000);
		
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] mainBS= fIndex.findBindings(getPattern("main"), true, IndexFilter.ALL, NPM);
			assertLength(1, mainBS);
			IIndexBinding mainB= mainBS[0];
			
			IIndexName[] names= fIndex.findDefinitions(mainB);
			assertLength(1, names);
			IIndexName main= names[0];
			
			assertNull(main.getEnclosingDefinition());
			IIndexName[] enclosed= main.getEnclosedNames();
			assertLength(3, enclosed);
			assertName("C", enclosed[0]);
			assertName("func", enclosed[1]);
			assertName("var", enclosed[2]);
			
			IIndexName enclosing= enclosed[0].getEnclosingDefinition();
			assertNotNull(enclosing);
			assertName("main", enclosing);

			enclosing= enclosed[1].getEnclosingDefinition();
			assertNotNull(enclosing);
			assertName("main", enclosing);			

			enclosing= enclosed[2].getEnclosingDefinition();
			assertNotNull(enclosing);
			assertName("main", enclosing);			

			IIndexBinding funcB= fIndex.findBinding(enclosed[1]);
			assertNotNull(funcB);
			names= fIndex.findDefinitions(funcB);
			assertLength(1, names);
			IIndexName funcdef= names[0];
			
			assertNull(funcdef.getEnclosingDefinition());
			enclosed= funcdef.getEnclosedNames();
			assertLength(3, enclosed);
			assertName("C", enclosed[0]);
			assertName("func", enclosed[1]);
			assertName("var", enclosed[2]);
			
			enclosing= enclosed[0].getEnclosingDefinition();
			assertNotNull(enclosing);
			assertName("func", enclosing);

			enclosing= enclosed[1].getEnclosingDefinition();
			assertNotNull(enclosing);
			assertName("func", enclosing);			

			enclosing= enclosed[2].getEnclosingDefinition();
			assertNotNull(enclosing);
			assertName("func", enclosing);			
		}
		finally {
			fIndex.releaseReadLock();
		}
	}

}
