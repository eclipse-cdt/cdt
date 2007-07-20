/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/ 

package org.eclipse.cdt.internal.index.tests;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class IndexBugsTests extends BaseTestCase {
	private static final int INDEX_WAIT_TIME = 8000;
	private ICProject fCProject;
	protected IIndex fIndex;

	public IndexBugsTests(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return suite(IndexBugsTests.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		fCProject= CProjectHelper.createCCProject("__bugsTest__", "bin", IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().reindex(fCProject);
		fIndex= CCorePlugin.getIndexManager().getIndex(fCProject);
	}
	
	protected void tearDown() throws Exception {
		if (fCProject != null) {
			CProjectHelper.delete(fCProject);
		}
		super.tearDown();
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

	private void waitForIndexer() {
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(INDEX_WAIT_TIME, NPM));
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

	// class A {
	// public:
	//   void one() {}
	//   void two() {}
	// };	
	
	// class A {
	// public:
	//   void three() {}
	//   void four() {}
	//   void five() {}
	// };
	public void test154563() throws Exception {
		// because of fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=193779
		// this test case passes. However https://bugs.eclipse.org/bugs/show_bug.cgi?id=154563
		// remains to be fixed.
		StringBuffer[] content= getContentsForTest(3);
		
		IFile file= createFile(getProject(), "header.h", content[0].toString());
		waitUntilFileIsIndexed(file, INDEX_WAIT_TIME);
		
		IIndex index= CCorePlugin.getIndexManager().getIndex(fCProject);
		index.acquireReadLock();
		try {
			IBinding[] bs= index.findBindings("A".toCharArray(), IndexFilter.ALL, NPM);
			assertEquals(1, bs.length); 
			assertTrue(bs[0] instanceof ICPPClassType);
			assertEquals(2, ((ICPPClassType)bs[0]).getDeclaredMethods().length);
		} finally {
			index.releaseReadLock();
		}
		
		file.setContents(new ByteArrayInputStream(content[1].toString().getBytes()), true, false, NPM);
		waitUntilFileIsIndexed(file, INDEX_WAIT_TIME);
		
		index= CCorePlugin.getIndexManager().getIndex(fCProject);
		index.acquireReadLock();
		try {
			IBinding[] bs= index.findBindings("A".toCharArray(), IndexFilter.ALL, NPM);
			assertEquals(1, bs.length); 
			assertTrue(bs[0] instanceof ICPPClassType);
			assertEquals(3, ((ICPPClassType)bs[0]).getDeclaredMethods().length);
		} finally {
			index.releaseReadLock();
		}
	}
	
	// class A {};
	// class B {};
	// A var;
	
	// class A {};
	// class B {};
	// B var;
	public void _test173997_2() throws Exception {
		StringBuffer[] content= getContentsForTest(3);
		
		IFile file= createFile(getProject(), "header.h", content[0].toString());
		waitUntilFileIsIndexed(file, INDEX_WAIT_TIME);
		
		IIndex index= CCorePlugin.getIndexManager().getIndex(fCProject);
		index.acquireReadLock();
		try {
			IBinding[] bs= index.findBindings("var".toCharArray(), IndexFilter.ALL, NPM);
			assertEquals(1, bs.length); 
			assertTrue(bs[0] instanceof ICPPVariable);
			assertTrue(((ICPPVariable)bs[0]).getType() instanceof ICPPClassType);
			assertEquals("A", ((ICPPClassType)(((ICPPVariable)bs[0]).getType())).getName());
		} finally {
			index.releaseReadLock();
		}
		
		file.setContents(new ByteArrayInputStream(content[1].toString().getBytes()), true, false, NPM);
		waitUntilFileIsIndexed(file, INDEX_WAIT_TIME);
		
		index= CCorePlugin.getIndexManager().getIndex(fCProject);
		index.acquireReadLock();
		try {
			IBinding[] bs= index.findBindings("var".toCharArray(), IndexFilter.ALL, NPM);
			assertEquals(1, bs.length); 
			assertTrue(bs[0] instanceof ICPPVariable);
			assertTrue(((ICPPVariable)bs[0]).getType() instanceof ICPPClassType);
			assertEquals("B", ((ICPPClassType)(((ICPPVariable)bs[0]).getType())).getName());
		} finally {
			index.releaseReadLock();
		}
	}
	
    //  namespace ns162011 {
    //    class Class162011 {
    //      friend void function162011(Class162011); 
    //    };
    //    void function162011(Class162011 x){};
    //  }
    public void testBug162011() throws Exception {
		String content = getContentsForTest(1)[0].toString();
		String fileName = "bug162011.cpp";
		String funcName = "function162011";

		int indexOfDecl = content.indexOf(funcName);
		int indexOfDef  = content.indexOf(funcName, indexOfDecl+1);
		IFile file= createFile(getProject(), fileName, content);
		waitUntilFileIsIndexed(file, INDEX_WAIT_TIME);
		
		// make sure the ast is correct
		ITranslationUnit tu= (ITranslationUnit) fCProject.findElement(new Path(fileName));
		IASTTranslationUnit ast= tu.getAST();
		IASTName name= (IASTName) ast.selectNodeForLocation(tu.getLocation().toOSString(), indexOfDecl, funcName.length());
		IBinding astBinding= name.resolveBinding();

		IName[] astDecls= ast.getDeclarations(astBinding);
		assertEquals(2, astDecls.length);
		int i1= astDecls[0].getFileLocation().getNodeOffset();
		int i2= astDecls[1].getFileLocation().getNodeOffset();
		assertEquals(indexOfDecl, Math.min(i1, i2));
		assertEquals(indexOfDef, Math.max(i1, i2));

		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings= fIndex.findBindings(getPattern("ns162011::function162011"), true, IndexFilter.ALL, NPM);
			assertEquals(1, bindings.length);
			
			IIndexBinding binding= bindings[0];
			
			// check if we have the declaration
			IIndexName[] decls= fIndex.findNames(binding, IIndex.FIND_DECLARATIONS);
			assertEquals(1, decls.length);
			assertEquals(indexOfDecl, decls[0].getNodeOffset());

			// check if we have the definition
			decls= fIndex.findNames(binding, IIndex.FIND_DEFINITIONS);
			assertEquals(1, decls.length);
			assertEquals(indexOfDef, decls[0].getNodeOffset());
		}
		finally {
			fIndex.releaseReadLock();
		}
    }
    
    public void testBug150906() throws Exception {
    	String fileName= "bug150906.c";
    	String varName= "arrayDataSize";
    	StringBuffer content= new StringBuffer();
    	content.append("unsigned char arrayData[] = {\n");
    	for(int i=0; i<1024*250-1; i++) {
    		content.append("0x00,");
    	}
    	content.append("0x00};\n"); 
    	content.append("unsigned int arrayDataSize = sizeof(arrayData);\n");
		int indexOfDecl = content.indexOf(varName);

		assertTrue(CCorePlugin.getIndexManager().joinIndexer(INDEX_WAIT_TIME, NPM));
		IFile file= createFile(getProject(), fileName, content.toString());
		// must be done in a reasonable amount of time
		waitUntilFileIsIndexed(file, INDEX_WAIT_TIME);
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] bindings= fIndex.findBindings(getPattern("arrayDataSize"), true, IndexFilter.ALL, NPM);
			assertEquals(1, bindings.length);
			
			IIndexBinding binding= bindings[0];
			
			// check if we have the definition
			IIndexName[] decls= fIndex.findNames(binding, IIndex.FIND_DEFINITIONS);
			assertEquals(1, decls.length);
			assertEquals(indexOfDecl, decls[0].getNodeOffset());
		}
		finally {
			fIndex.releaseReadLock();
		}
    }

	public void test164360_1() throws Exception {
		waitForIndexer();
		IFile include= TestSourceReader.createFile(fCProject.getProject(), "test164360.h", "");
		TestScannerProvider.sIncludeFiles= new String[]{include.getLocation().toOSString()};
		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test164360.cpp", "");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(IndexLocationFactory.getWorkspaceIFL(file));
			assertNotNull(ifile);
			IIndexInclude[] includes= ifile.getIncludes();
			assertEquals(1, includes.length);
			IIndexInclude i= includes[0];
			assertEquals(file.getLocationURI(), i.getIncludedByLocation().getURI());
			assertEquals(include.getLocationURI(), i.getIncludesLocation().getURI());
			assertEquals(true, i.isSystemInclude());
			assertEquals(0, i.getNameOffset());
			assertEquals(0, i.getNameLength());
		}
		finally {
			fIndex.releaseReadLock();
		}
	}

	public void test164360_2() throws Exception {
		waitForIndexer();
		IFile include= TestSourceReader.createFile(fCProject.getProject(), "test164360.h", "");
		TestScannerProvider.sMacroFiles= new String[]{include.getLocation().toOSString()};
		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test164360.cpp", "");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(IndexLocationFactory.getWorkspaceIFL(file));
			assertNotNull(ifile);
			IIndexInclude[] includes= ifile.getIncludes();
			assertEquals(1, includes.length);
			IIndexInclude i= includes[0];
			assertEquals(file.getLocationURI(), i.getIncludedByLocation().getURI());
			assertEquals(include.getLocationURI(), i.getIncludesLocation().getURI());
			assertEquals(true, i.isSystemInclude());
			assertEquals(0, i.getNameOffset());
			assertEquals(0, i.getNameLength());
		}
		finally {
			fIndex.releaseReadLock();
		}
	}

	public void test160281_1() throws Exception {
		waitForIndexer();
		IFile include= TestSourceReader.createFile(fCProject.getProject(), "inc/test160281_1.h", "");
		TestScannerProvider.sIncludes= new String[]{include.getLocation().removeLastSegments(1).toString()};
		TestScannerProvider.sIncludeFiles= new String[]{include.getName()};
		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test160281_1.cpp", "");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(IndexLocationFactory.getWorkspaceIFL(file));
			assertNotNull(ifile);
			IIndexInclude[] includes= ifile.getIncludes();
			assertEquals(1, includes.length);
			IIndexInclude i= includes[0];
			assertEquals(file.getLocationURI(), i.getIncludedByLocation().getURI());
			assertEquals(include.getLocationURI(), i.getIncludesLocation().getURI());
			assertEquals(true, i.isSystemInclude());
			assertEquals(0, i.getNameOffset());
			assertEquals(0, i.getNameLength());
		}
		finally {
			fIndex.releaseReadLock();
		}
	}

	public void test160281_2() throws Exception {
		waitForIndexer();
		IFile include= TestSourceReader.createFile(fCProject.getProject(), "inc/test160281_2.h", "#define X y\n");
		TestScannerProvider.sIncludes= new String[]{include.getLocation().removeLastSegments(1).toString()};
		TestScannerProvider.sMacroFiles= new String[]{include.getName()};
		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test160281_2.cpp", "int X;");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(IndexLocationFactory.getWorkspaceIFL(file));
			assertNotNull(ifile);
			IIndexInclude[] includes= ifile.getIncludes();
			assertEquals(1, includes.length);
			IIndexInclude i= includes[0];
			assertEquals(file.getLocationURI(), i.getIncludedByLocation().getURI());
			assertEquals(include.getLocationURI(), i.getIncludesLocation().getURI());
			assertEquals(true, i.isSystemInclude());
			assertEquals(0, i.getNameOffset());
			assertEquals(0, i.getNameLength());
			IIndexBinding[] bindings= fIndex.findBindings("y".toCharArray(), IndexFilter.ALL, NPM);
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof IVariable);
		}
		finally {
			fIndex.releaseReadLock();
		}
	}

	// #define macro164500 1
	// #undef macro164500
	// #define macro164500 2
	public void test164500() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0].toString();

		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test164500.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(IndexLocationFactory.getWorkspaceIFL(file));
			assertNotNull(ifile);
			IIndexMacro[] macros= ifile.getMacros();
			assertEquals(2, macros.length);
			IIndexMacro m= macros[0];
			assertEquals("1", new String(m.getExpansion()));
			assertEquals("macro164500", new String(m.getName()));

			m= macros[1];
			assertEquals("2", new String(m.getExpansion()));
			assertEquals("macro164500", new String(m.getName()));
		}
		finally {
			fIndex.releaseReadLock();
		}
	}

	// class A {}; class B {}; class C {};
	public void testIndexContentOverProjectDelete() throws Exception {
		waitForIndexer();

		/* Check that when a project is deleted, its index contents do not
         * appear in the initial index of a newly created project of the same name */
         
		String pname = "deleteTest"+System.currentTimeMillis();
		ICProject cproject = CProjectHelper.createCCProject(pname, "bin", IPDOMManager.ID_FAST_INDEXER);
		IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
		String content= getContentsForTest(1)[0].toString();
		IFile file= TestSourceReader.createFile(cproject.getProject(), "content.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(index, file, INDEX_WAIT_TIME);
		CProjectHelper.delete(cproject);

		cproject = CProjectHelper.createCCProject(pname, "bin", IPDOMManager.ID_FAST_INDEXER);
		index = CCorePlugin.getIndexManager().getIndex(cproject);
		index.acquireReadLock();
		try {
			IBinding[] bindings = index.findBindings(Pattern.compile(".*"), false, IndexFilter.ALL, new NullProgressMonitor());
			assertEquals(0, bindings.length);
		}
		finally {
			index.releaseReadLock();
			CProjectHelper.delete(cproject);
		}
	}

	// class A {}; class B {}; class C {}; class D {};
	public void testIndexContentOverProjectMove() throws Exception {
		waitForIndexer();

		/* Check that the contents of an index is preserved over a project
         * move operation */

		ICProject cproject = CProjectHelper.createCCProject("moveTest", "bin", IPDOMManager.ID_FAST_INDEXER);
		IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
		String content= getContentsForTest(1)[0].toString();
		IFile file= TestSourceReader.createFile(cproject.getProject(), "content.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(index, file, INDEX_WAIT_TIME);

		// move the project to a random new location
		File newLocation = CProjectHelper.freshDir();
		IProjectDescription description = cproject.getProject().getDescription();
		description.setLocationURI(newLocation.toURI());
		cproject.getProject().move(description, IResource.FORCE | IResource.SHALLOW, new NullProgressMonitor());	
		
		index = CCorePlugin.getIndexManager().getIndex(cproject);
		index.acquireReadLock();
		try {
			IBinding[] bindings = index.findBindings(Pattern.compile(".*"), false, IndexFilter.ALL_DECLARED, new NullProgressMonitor());
			assertEquals(4, bindings.length);
		}
		finally {
			index.releaseReadLock();
			CProjectHelper.delete(cproject);
		}
	}
	
	// // header.h
	// class E {};
	
	// #include "header.h"
	// E var;
	
	// // header.h	
	// enum E {A,B,C};
	public void _test171834() throws Exception {
		waitForIndexer();

		ICProject cproject = CProjectHelper.createCCProject("seq1", "bin", IPDOMManager.ID_FAST_INDEXER);
		try {
			IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
			StringBuffer[] testData = getContentsForTest(3);
			IFile header= TestSourceReader.createFile(cproject.getProject(), "header.h", testData[0].toString());
			IFile referer= TestSourceReader.createFile(cproject.getProject(), "content.cpp", testData[1].toString());
			TestSourceReader.waitUntilFileIsIndexed(index, referer, INDEX_WAIT_TIME);

			index.acquireReadLock();
			try {
				IBinding[] bindings = index.findBindings(Pattern.compile("var"), true, IndexFilter.ALL, new NullProgressMonitor());
				assertEquals(1, bindings.length);
				IType type = ((ICPPVariable)bindings[0]).getType();
				assertTrue(type instanceof ICPPClassType);
				assertEquals("var is not of type class", ICPPClassType.k_class, ((ICPPClassType)type).getKey());
			} finally {
				index.releaseReadLock();
			}

			InputStream in = new ByteArrayInputStream(testData[2].toString().getBytes()); 
			header.setContents(in, IResource.FORCE, null);
			TestSourceReader.waitUntilFileIsIndexed(index, header, INDEX_WAIT_TIME);

			index.acquireReadLock();
			try {
				IBinding[] bindings = index.findBindings(Pattern.compile("var"), true, IndexFilter.ALL, new NullProgressMonitor());
				assertEquals(1, bindings.length);

				IType type = ((ICPPVariable)bindings[0]).getType();
				assertTrue(type instanceof IEnumeration);
			} finally {
				index.releaseReadLock();
			}
		} finally {
			CProjectHelper.delete(cproject);
		}
	}
	
	// typedef struct S20070201 {
	//    int a;
	// } S20070201;
	public void test172454_1() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0].toString();

		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test172454.c", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IBinding[] bindings= fIndex.findBindings("S20070201".toCharArray(), IndexFilter.getFilter(ILinkage.C_LINKAGE_ID), NPM);
			assertEquals(2, bindings.length);
			
			IBinding struct, typedef;
			if (bindings[0] instanceof ICompositeType) {
				struct= bindings[0];
				typedef= bindings[1];
			}
			else {
				struct= bindings[1];
				typedef= bindings[0];
			}
			
			assertTrue(struct instanceof ICompositeType);
			assertTrue(typedef instanceof ITypedef);
			assertTrue(((ITypedef) typedef).getType() instanceof ICompositeType);
			assertTrue(((ITypedef) typedef).isSameType((ICompositeType) struct));
		}
		finally {
			fIndex.releaseReadLock();
		}
	}

	// typedef struct S20070201 {
	//    int a;
	// } S20070201;
	public void test172454_2() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0].toString();

		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test172454.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IBinding[] bindings= fIndex.findBindings("S20070201".toCharArray(), IndexFilter.getFilter(ILinkage.CPP_LINKAGE_ID), NPM);
			assertEquals(2, bindings.length);
			
			IBinding struct, typedef;
			if (bindings[0] instanceof ICPPClassType) {
				struct= bindings[0];
				typedef= bindings[1];
			}
			else {
				struct= bindings[1];
				typedef= bindings[0];
			}
			
			assertTrue(struct instanceof ICPPClassType);
			assertTrue(((ICPPClassType)struct).getKey()==ICompositeType.k_struct);
			assertTrue(typedef instanceof ITypedef);
			IType aliased = ((ITypedef) typedef).getType();
			assertTrue(aliased instanceof ICPPClassType);
			assertTrue(((ICPPClassType)aliased).getKey()==ICompositeType.k_struct);
			assertTrue(((ITypedef) typedef).isSameType((ICompositeType) struct));
		}
		finally {
			fIndex.releaseReadLock();
		}
	}

	// enum {e20070206};
	public void test156671() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0].toString();

		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test156671.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IBinding[] bindings= fIndex.findBindings("e20070206".toCharArray(), IndexFilter.getFilter(ILinkage.CPP_LINKAGE_ID), NPM);
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof IEnumerator);
		}
		finally {
			fIndex.releaseReadLock();
		}
	}
	
	// typedef int T20070213;
	public void _test173997() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0].toString();

		IFile file= TestSourceReader.createFile(fCProject.getProject(), "test173997.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IBinding[] bindings= fIndex.findBindings("T20070213".toCharArray(), IndexFilter.getFilter(ILinkage.CPP_LINKAGE_ID), NPM);
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof ITypedef);
			ITypedef td= (ITypedef) bindings[0];
			IType type= td.getType();
			assertTrue(type instanceof IBasicType);
			IBasicType btype= (IBasicType) type;
			assertEquals(IBasicType.t_int, btype.getType());
		}
		finally {
			fIndex.releaseReadLock();
		}
		
		long timestamp= file.getLocalTimeStamp();
		content= "int UPDATED20070213;\n" + content.replaceFirst("int", "float");
		file= TestSourceReader.createFile(fCProject.getProject(), "test173997.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			// double check if file was indexed
			IBinding[] bindings= fIndex.findBindings("UPDATED20070213".toCharArray(), IndexFilter.getFilter(ILinkage.CPP_LINKAGE_ID), NPM);
			assertEquals(1, bindings.length);
			
			bindings= fIndex.findBindings("T20070213".toCharArray(), IndexFilter.getFilter(ILinkage.CPP_LINKAGE_ID), NPM);
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof ITypedef);
			ITypedef td= (ITypedef) bindings[0];
			IType type= td.getType();
			assertTrue(type instanceof IBasicType);
			IBasicType btype= (IBasicType) type;
			assertTrue(IBasicType.t_int != btype.getType());
			assertEquals(IBasicType.t_float, btype.getType());
		}
		finally {
			fIndex.releaseReadLock();
		}
	}
	
	// class a {};
	// class A {};
	// namespace aa {
	//   class a {
	//     class e {
	//      class AA {class A{};};
	//     };
	//   };
	// };
	public void testFindBindingsWithPrefix() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0].toString();

		IFile file= TestSourceReader.createFile(fCProject.getProject(), "testFBWP.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			final IndexFilter NON_FUNCTIONS = new IndexFilter() {
				public boolean acceptBinding(IBinding binding) {
					return !(binding instanceof IFunction);
				}
			};
			
			IBinding[] bindings= fIndex.findBindingsForPrefix(new char[] {'a'}, true, NON_FUNCTIONS, null);
			assertEquals(3,bindings.length);
			
			bindings= fIndex.findBindingsForPrefix(new char[] {'a'}, false, NON_FUNCTIONS, null);
			assertEquals(6,bindings.length);
			
			bindings= fIndex.findBindingsForPrefix(new char[] {'a','A'}, true, NON_FUNCTIONS, null);
			assertEquals(1,bindings.length);
			
			bindings= fIndex.findBindingsForPrefix(new char[] {'a','A'}, false, NON_FUNCTIONS, null);
			assertEquals(2, bindings.length);
		}
		finally {
			fIndex.releaseReadLock();
		}		
	}
	
	// class a { class b { class c { void f(); }; }; };
	public void testFilterFindBindingsFQCharArray() throws Exception {
		waitForIndexer();
		String content= getContentsForTest(1)[0].toString();

		IFile file= TestSourceReader.createFile(fCProject.getProject(), "testFilterFindBindingsFQCharArray.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, INDEX_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			final IndexFilter NON_CLASS = new IndexFilter() {
				public boolean acceptBinding(IBinding binding) {
					return !(binding instanceof ICPPClassType);
				}
			};
			
			IBinding[] bindings= fIndex.findBindings(new char[][]{{'a'},{'b'},{'c'},{'f'}}, NON_CLASS, NPM);
			assertEquals(1,bindings.length);
		}
		finally {
			fIndex.releaseReadLock();
		}		
	}
	
    // typedef struct {
    //    float   fNumber;
    //    int     iIdx;
    // } StructA_T;
	
	// #include "../__bugsTest__/common.h"
	// StructA_T gvar1;
	
	// #include "../__bugsTest__/common.h"
	// StructA_T gvar2;
	public void testFileInMultipleFragments_bug192352() throws Exception {
		StringBuffer[] contents= getContentsForTest(3);

		
		ICProject p2 = CProjectHelper.createCCProject("__bugsTest_2_", "bin", IPDOMManager.ID_FAST_INDEXER);
		try {
			IFile f1= TestSourceReader.createFile(fCProject.getProject(), "common.h", contents[0].toString());
			IFile f2= TestSourceReader.createFile(fCProject.getProject(), "src.cpp", contents[1].toString());
			IFile f3= TestSourceReader.createFile(p2.getProject(), "src.cpp", contents[2].toString());
			waitForIndexer();

			IIndex index= CCorePlugin.getIndexManager().getIndex(new ICProject[]{fCProject, p2});
			index.acquireReadLock();
			try {
				IIndexBinding[] bindings= index.findBindings("StructA_T".toCharArray(), IndexFilter.ALL, NPM);
				assertEquals(1, bindings.length);
				IIndexBinding binding= bindings[0];
				IIndexName[] names= index.findReferences(binding);
				assertEquals(2, names.length);
				names= index.findDeclarations(binding);
				assertEquals(1, names.length);
			}
			finally {
				index.releaseReadLock();
			}
		}
		finally {
			CProjectHelper.delete(p2);
		}
	}
	
	// #ifndef _h1
	// #define _h1
	// #define M v
	// #endif
	
	// #ifndef _h1
	// #include "header1.h"
	// #endif
	
	// #include "header1.h"
	// #include "header2.h"
	
	// #include "header2.h"
	// int M;
	
	// #include "header2.h"
	// #ifndef _h1
	// #include "header1.h"
	// #endif
	public void testIncludeGuardsOutsideOfHeader_Bug167100() throws Exception {
		final IIndexManager indexManager = CCorePlugin.getIndexManager();
		StringBuffer[] contents= getContentsForTest(5);
		IFile f1= TestSourceReader.createFile(fCProject.getProject(), "header1.h", contents[0].toString());
		IFile f2= TestSourceReader.createFile(fCProject.getProject(), "header2.h", contents[1].toString());
		IFile f3= TestSourceReader.createFile(fCProject.getProject(), "src.cpp", contents[2].toString());
		indexManager.reindex(fCProject);
		waitForIndexer();
		IFile f4= TestSourceReader.createFile(fCProject.getProject(), "src2.cpp", contents[3].toString());
		IFile f5= TestSourceReader.createFile(fCProject.getProject(), "src3.cpp", contents[4].toString());
		waitForIndexer();
		
		IIndex index= indexManager.getIndex(fCProject);
		index.acquireReadLock();
		try {
			IIndexBinding[] bindings = index.findBindings("v".toCharArray(), IndexFilter.ALL, NPM);
			assertEquals(1, bindings.length);
			IIndexBinding binding = bindings[0];
			assertTrue(binding instanceof IVariable);
			IIndexName[] names = index.findNames(binding,
					IIndex.FIND_ALL_OCCURENCES);
			assertEquals(1, names.length);
			assertEquals(f4.getFullPath().toString(), names[0].getFile().getLocation().getFullPath());
			
			IIndexFile idxFile= index.getFile(IndexLocationFactory.getWorkspaceIFL(f5));
			IIndexInclude[] includes= idxFile.getIncludes();
			assertEquals(2, includes.length);
			assertTrue(includes[0].isActive());
			assertTrue(includes[0].isResolved());
			assertFalse(includes[1].isActive());
			assertTrue(includes[1].isResolved());
		} finally {
			index.releaseReadLock();
		}
	}
}
