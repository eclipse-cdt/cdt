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

import java.io.IOException;
import java.util.regex.Pattern;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public class IndexNamesTests extends BaseTestCase {
	private ICProject fCProject;
	protected IIndex fIndex;

	public IndexNamesTests(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return suite(IndexNamesTests.class);
	}

	@Override
	protected void setUp() throws CoreException {
		fCProject= CProjectHelper.createCCProject("__encNamesTest__", "bin", IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().reindex(fCProject);
		fIndex= CCorePlugin.getIndexManager().getIndex(fCProject);
	}

	@Override
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
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(10000, npm()));
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
			IIndexBinding[] mainBS= fIndex.findBindings(getPattern("main"), true, IndexFilter.ALL, npm());
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
			IIndexBinding[] mainBS= fIndex.findBindings(getPattern("main"), true, IndexFilter.ALL, npm());
			assertLength(1, mainBS);
			IIndexBinding mainB= mainBS[0];
			
			IIndexName[] names= fIndex.findDefinitions(mainB);
			assertLength(1, names);
			IIndexName main= names[0];
			
			assertNull(main.getEnclosingDefinition());
			IIndexName[] enclosed= main.getEnclosedNames();
			assertLength(4, enclosed);
			assertName("C", enclosed[0]); // Class reference
			assertName("C", enclosed[1]); // Implicit ctor call
			assertName("func", enclosed[2]);
			assertName("var", enclosed[3]);
			
			IIndexName enclosing= enclosed[0].getEnclosingDefinition();
			assertNotNull(enclosing);
			assertName("main", enclosing);

			enclosing= enclosed[1].getEnclosingDefinition();
			assertNotNull(enclosing);
			assertName("main", enclosing);			

			enclosing= enclosed[2].getEnclosingDefinition();
			assertNotNull(enclosing);
			assertName("main", enclosing);

			enclosing= enclosed[3].getEnclosingDefinition();
			assertNotNull(enclosing);
			assertName("main", enclosing);			

			IIndexBinding funcB= fIndex.findBinding(enclosed[2]);
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

	//	class X {
	//		public:
	//			virtual void vm() {
	//			}
	//		};
	//
	//	class Y : public X {
	//	public:
	//		virtual void vm() {
	//		}	
	//		void test();
	//	};
	//	void Y::test() {
	//		X* x= this;
	//		X& xr= *this;
	//		X xc= *this;
	//		
	//		vm();		// polymorphic
	//		X::vm(); 	// call to X::vm()
	//		x->vm(); 	// polymorphic
	//		x->X::vm(); // call to X::vm()
	//		xr.vm(); 	// polymorphic
	//		xr.X::vm(); // call to X::vm()
	//		xc.vm();    // call to X::vm()
	//		xc.X::vm(); // call to X::vm()
	//	}
	public void testCouldBePolymorphicMethodCall_Bug156691() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject().getProject(), "test.cpp", content);
		waitUntilFileIsIndexed(file, 4000);

		boolean[] couldbepolymorphic= {true, false, true, false, true, false, false, false};
		String[] container= 		  {"Y",  "X",   "X",  "X",   "X",  "X",   "X",   "X"  };  

		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
			IIndexName[] names= ifile.findNames(0, content.length());
			int j= 0;
			for (IIndexName indexName : names) {
				if (indexName.isReference() && indexName.toString().equals("vm")) {
					assertEquals(couldbepolymorphic[j], indexName.couldBePolymorphicMethodCall());
					assertEquals(container[j], fIndex.findBinding(indexName).getQualifiedName()[0]);
					j++;
				}
				else {
					assertEquals(false, indexName.couldBePolymorphicMethodCall());
				}
			}
			assertEquals(couldbepolymorphic.length, j);
		}
		finally {
			fIndex.releaseReadLock();
		}
	}

	
	//	int _i, ri, wi, rwi;
	//  int* rp; int* wp; int* rwp;
	//  const int* cip= &ri;
	//  int* bla= &rwi;
	//  void fi(int);
	//  void fp(int*);
	//  void fcp(const int*);
	//  void fpp(int**);
	//  void fcpp(int const**);
	//  void fpcp(int *const*);
	//  void fcpcp(int const *const*);
	//
	//	void test() {
	//      _i; 	
	//		wi= ri, _i, _i; 
	//      rwi %= ri;     
	//      ri ? _i : _i;   
	//      (ri ? wi : wi)= ri; 
	//      if (ri) _i;
	//      for(wi=1; ri>ri; rwi++) _i;
	//		do {_i;} while (ri);
	//      while(ri) {_i;};
	//      switch(ri) {case ri: _i;};
	//      fi(ri); fp(&rwi); fcp(&ri);
	//      fi(*rp); fp(rp); fcp(rp); fpp(&rwp); fcpp(&rwp); fpcp(&rp); fcpcp(&rp);
	//      return ri;
	//	}
	public void testReadWriteFlagsC() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject().getProject(), "test.c", content);
		waitUntilFileIsIndexed(file, 4000);

		checkReadWriteFlags(file, ILinkage.C_LINKAGE_ID, 41);
	}

	private void checkReadWriteFlags(IFile file, int linkageID, int count) throws InterruptedException,
			CoreException {
		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(linkageID, IndexLocationFactory.getWorkspaceIFL(file));
			IIndexName[] names= ifile.findNames(0, Integer.MAX_VALUE);
			int j= 0;
			for (int i = 0; i < names.length; i++) {
				IIndexName indexName = names[i];
				final String name = indexName.toString();
				final char c0= name.length() > 0 ? name.charAt(0) : 0;
				if ((c0 == '_' || c0 == 'r' || c0 == 'w') && indexName.isReference()) {
					boolean isRead= name.charAt(0) == 'r' || name.charAt(0) == 'u';
					boolean isWrite= name.charAt(isRead ? 1 : 0) == 'w' || name.charAt(0) == 'u';
					String msg= "i=" + i + ", " + name + ":";
					assertEquals(msg, isRead, indexName.isReadAccess());
					assertEquals(msg, isWrite, indexName.isWriteAccess());
					j++;
				}
				else {
					assertEquals(false, indexName.couldBePolymorphicMethodCall());
				}
			}
			assertEquals(count, j);
		}
		finally {
			fIndex.releaseReadLock();
		}
	}

	//	int _i, ri, wi, rwi, rfind, ui;
	//  int* rp; int* wp; int* rwp; int* up; 
	//  int* const rpc= 0;
	//  const int * const rcpc= 0;
	//  const int* cip= &ri;
	//  int* bla= &rwi;
	//  void fi(int);
	//  void fp(int*);
	//  void fr(int&);
	//  void fcp(const int*);
	//  void fcr(const int&);
	//  void fpp(int**);
	//  void fpr(int*&);
	//  void fcpp(int const**);
	//  void fcpr(int const*&);
	//  void fpcp(int *const*);
	//  void fpcr(int *const&);
	//  void fcpcp(int const *const*);
	//  void fcpcr(int const *const&);
	//
	//	int test() {
	//      _i; 	
	//		wi= ri, _i, _i; // expr-list
	//      rwi %= ri;     // assignment
	//      ri ? _i : _i;   // conditional
	//      (ri ? wi : wi)= ri; // conditional
	//      if (ri) _i;
	//      for(wi=1; ri>ri; rwi++) _i;
	//		do {_i;} while (ri);
	//      while(ri) {_i;};
	//      switch(ri) {case ri: _i;};
	//      fi(ri); fp(&rwi); fcp(&ri); 
	//      fi(*rp); fp(rp); fcp(rp); fpp(&rwp); fcpp(&up); fpcp(&rpc); fcpcp(&rcpc); 
	//      fr(rwi); fcr(ri); fpr(&ui); 
	//      fcpr(&ui); fpcr(&rwi); fcpcr(&ri);
	//      fpr(rwp); fcpr(up); fpcr(rp); fcpcr(rp);
	//      return ri;
	//	}
	public void testReadWriteFlagsCpp() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0].toString();
		IFile file= createFile(getProject().getProject(), "test.cpp", content);
		waitUntilFileIsIndexed(file, 4000);

		checkReadWriteFlags(file, ILinkage.CPP_LINKAGE_ID, 47);
	}
}
