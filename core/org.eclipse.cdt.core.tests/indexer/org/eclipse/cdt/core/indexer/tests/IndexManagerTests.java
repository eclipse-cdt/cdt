/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
***********************************************************************/
/*
 * Created on Jun 19, 2003
 */
package org.eclipse.cdt.core.indexer.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.index.IIndexDelta;
import org.eclipse.cdt.core.index.IndexChangeEvent;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.cdt.internal.core.index.impl.IFileDocument;
import org.eclipse.cdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.testplugin.CProjectHelper;
import org.eclipse.cdt.testplugin.CTestPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author bgheorgh
 */
public class IndexManagerTests extends TestCase implements IIndexChangeListener  {
	IFile 					file;
	IFileDocument 			fileDoc;
	IProject 				testProject;
	NullProgressMonitor		monitor;
	IndexManager 			indexManager;
	boolean					fileIndexed;
	
	public static final int TIMEOUT = 50;
	/**
	 * Constructor for IndexManagerTest.
	 * @param name
	 */
	public IndexManagerTests(String name) {
		super(name);
	}

	public static void main(String[] args) {
	}
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		monitor = new NullProgressMonitor();
		
		//Create temp project
		testProject = createProject("IndexerTestProject");
		IPath pathLoc = CCorePlugin.getDefault().getStateLocation();
		
		File indexFile = new File(pathLoc.append("3915980774.index").toOSString());
		if (indexFile.exists())
			indexFile.delete();
		
		
		testProject.setSessionProperty(IndexManager.activationKey,new Boolean(true));
		
		if (testProject==null)
			fail("Unable to create project");	
		
		indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
		indexManager.reset();
		indexManager.addIndexChangeListener(this);
	}
	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() {
		try {
			super.tearDown();
			indexManager.removeIndexChangeListener(this);
		} catch (Exception e1) {
		}
		//Delete project
		if (testProject.exists()) {
			try {
				System.gc();
				System.runFinalization();
				testProject.delete(true, monitor);
			} catch (CoreException e) {
				fail(getMessage(e.getStatus()));
			}
		}
	}

	private String getMessage(IStatus status) {
		StringBuffer message = new StringBuffer("[");
		message.append(status.getMessage());
		if (status.isMultiStatus()) {
			IStatus children[] = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				message.append(getMessage(children[i]));
			}
		}
		message.append("]");
		return message.toString();
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(IndexManagerTests.class.getName());

		suite.addTest(new IndexManagerTests("testAddNewFileToIndex"));
		suite.addTest(new IndexManagerTests("testForwardDeclarations"));
		suite.addTest(new IndexManagerTests("testIndexAll"));
		suite.addTest(new IndexManagerTests("testIndexContents"));
		suite.addTest(new IndexManagerTests("testMacros"));
		suite.addTest(new IndexManagerTests("testRefs"));
		suite.addTest(new IndexManagerTests("testExactDeclarations"));
		suite.addTest(new IndexManagerTests("testRemoveFileFromIndex"));
		suite.addTest(new IndexManagerTests("testRemoveProjectFromIndex"));
		suite.addTest(new IndexManagerTests("testIndexShutdown"));
	
		return suite;
	
	}
	/*
	 * Utils
	 */
	private IProject createProject(String projectName) throws CoreException {
		ICProject cPrj = CProjectHelper.createCCProject(projectName, "bin");
		return cPrj.getProject();
	}
	
	private IFile importFile(String fileName, String resourceLocation)throws Exception {
		//Obtain file handle
		file = testProject.getProject().getFile(fileName);
		//Create file input stream
		monitor = new NullProgressMonitor();
		if (!file.exists()) {
			file.create(new FileInputStream(
					CTestPlugin.getDefault().getFileInPlugin(new Path(resourceLocation))),
					false, monitor);
		}
		fileDoc = new IFileDocument(file);
		return file;
	}

	/*
	 * Start of tests
	 */ 	
	public void testIndexAll() throws Exception {
		
		//Add a file to the project
		fileIndexed = false;
		importFile("mail.cpp","resources/indexer/mail.cpp");
		while (fileIndexed != true){ Thread.sleep(TIMEOUT);}
		
		IIndex ind = indexManager.getIndex(testProject.getFullPath(),true,true);
		assertTrue("Index exists for project",ind != null);
		
		char[] prefix = "typeDecl/".toCharArray();
		IQueryResult[] qresults = ind.queryPrefix(prefix);
		IEntryResult[] eresults = ind.queryEntries(prefix);
		
		assertTrue("Query Results exist", qresults != null);
		assertTrue("Entry Results exist", eresults != null);
		
		String [] queryResultModel = {"IndexedFile(1: /IndexerTestProject/mail.cpp)"};
		String [] entryResultModel ={"EntryResult: word=typeDecl/C/Mail, refs={ 1 }", "EntryResult: word=typeDecl/C/Unknown, refs={ 1 }", "EntryResult: word=typeDecl/C/container, refs={ 1 }", "EntryResult: word=typeDecl/C/first_class, refs={ 1 }", "EntryResult: word=typeDecl/C/postcard, refs={ 1 }","EntryResult: word=typeDecl/D/Mail, refs={ 1 }", "EntryResult: word=typeDecl/D/first_class, refs={ 1 }", "EntryResult: word=typeDecl/D/postcard, refs={ 1 }","EntryResult: word=typeDecl/V/PO_Box, refs={ 1 }", "EntryResult: word=typeDecl/V/index, refs={ 1 }", "EntryResult: word=typeDecl/V/mail, refs={ 1 }","EntryResult: word=typeDecl/V/size, refs={ 1 }", "EntryResult: word=typeDecl/V/temp, refs={ 1 }", "EntryResult: word=typeDecl/V/x, refs={ 1 }"};
		
		
		if (qresults.length != queryResultModel.length)
			fail("Query Result length different from model");

		if (eresults.length != entryResultModel.length)
			fail("Entry Result length different from model");

		for (int i=0; i<qresults.length;i++)
		{
			assertEquals(queryResultModel[i],qresults[i].toString());
		}
	
		for (int i=0;i<eresults.length; i++)
		{
			assertEquals(entryResultModel[i],eresults[i].toString());
		}
	}
	
	public void testAddNewFileToIndex() throws Exception{
		
		
		//Add a file to the project
		fileIndexed = false;
		importFile("mail.cpp","resources/indexer/mail.cpp");
		while (fileIndexed != true){ Thread.sleep(TIMEOUT);}
		
		//Make sure project got added to index
		IPath testProjectPath = testProject.getFullPath();
		IIndex ind = indexManager.getIndex(testProjectPath,true,true);
		assertTrue("Index exists for project",ind != null);
		//Add a new file to the project, give it some time to index
		fileIndexed = false;
		importFile("DocumentManager.h","resources/indexer/DocumentManager.h");
		while (fileIndexed != true){ Thread.sleep(TIMEOUT);}
		
		fileIndexed = false;
		importFile("DocumentManager.cpp","resources/indexer/DocumentManager.cpp");
		while (fileIndexed != true){ Thread.sleep(TIMEOUT);}
		
		ind = indexManager.getIndex(testProjectPath,true,true);
		
		char[] prefix = "typeDecl/C/CDocumentManager".toCharArray();
		String [] entryResultModel ={"EntryResult: word=typeDecl/C/CDocumentManager, refs={ 2 }"};
		IEntryResult[] eresults =ind.queryEntries(prefix);
		IEntryResult[] bogRe = ind.queryEntries(IIndexConstants.TYPE_DECL);
		assertTrue("Entry Result exists", eresults != null);
		
		if (eresults.length != entryResultModel.length)
			fail("Entry Result length different from model");

		for (int i=0;i<eresults.length; i++)
		{
			assertEquals(entryResultModel[i],eresults[i].toString());
		}
	}

	public void testRemoveProjectFromIndex() throws Exception{
	  
		
	  //Add a file to the project
	  fileIndexed = false;
	  importFile("mail.cpp","resources/indexer/mail.cpp");
	  while (fileIndexed != true){ Thread.sleep(TIMEOUT);}
	  
	  //Make sure project got added to index
	  IPath testProjectPath = testProject.getFullPath();
	  IIndex ind = indexManager.getIndex(testProjectPath,true,true);
	  assertTrue("Index exists for project",ind != null);
	  //Delete the project
	  safeDelete(testProject);
	  
	  //See if the index is still there
	  ind = indexManager.getIndex(testProjectPath,true,true);
	  assertTrue("Index deleted",ind == null);
	}
	
	/**
	 * @param testProject
	 */
	private void safeDelete(IProject testProject) throws InterruptedException, CoreException {
		System.gc();
		System.runFinalization();
		try {
			testProject.delete(true, monitor);
		} catch (CoreException e) {
			Thread.sleep(5000);
			testProject.delete(true, monitor);
		}

	}

	public void testRemoveFileFromIndex() throws Exception{
     
    //Add a file to the project
	fileIndexed = false;
	importFile("mail.cpp","resources/indexer/mail.cpp");
	while (fileIndexed != true){ Thread.sleep(TIMEOUT);}
	
	//Make sure project got added to index
	IPath testProjectPath = testProject.getFullPath();
	IIndex ind = indexManager.getIndex(testProjectPath,true,true);
	assertTrue("Index exists for project",ind != null);
	//Add a new file to the project, give it some time to index
	fileIndexed = false;
	importFile("DocumentManager.h","resources/indexer/DocumentManager.h");
	while (fileIndexed != true){ Thread.sleep(TIMEOUT);}
	
	fileIndexed = false;
	importFile("DocumentManager.cpp","resources/indexer/DocumentManager.cpp");
	while (fileIndexed != true){ Thread.sleep(TIMEOUT);}
	
	ind = indexManager.getIndex(testProjectPath,true,true);
		 
	 //Do a "before" deletion comparison
	 //ind = indexManager.getIndex(testProjectPath,true,true);
	 char[] prefix = "typeDecl/".toCharArray();
	 IEntryResult[] eresults = ind.queryEntries(prefix);
	 assertTrue("Entry result found for typdeDecl/", eresults != null);
	 
	 String [] entryResultBeforeModel ={"EntryResult: word=typeDecl/C/CDocumentManager, refs={ 2 }", "EntryResult: word=typeDecl/C/Mail, refs={ 3 }", "EntryResult: word=typeDecl/C/Unknown, refs={ 3 }", "EntryResult: word=typeDecl/C/container, refs={ 3 }", "EntryResult: word=typeDecl/C/first_class, refs={ 3 }", "EntryResult: word=typeDecl/C/postcard, refs={ 3 }",  "EntryResult: word=typeDecl/D/Mail, refs={ 3 }", "EntryResult: word=typeDecl/D/first_class, refs={ 3 }", "EntryResult: word=typeDecl/D/postcard, refs={ 3 }", "EntryResult: word=typeDecl/V/, refs={ 1, 2 }", "EntryResult: word=typeDecl/V/PO_Box, refs={ 3 }", "EntryResult: word=typeDecl/V/index, refs={ 3 }", "EntryResult: word=typeDecl/V/mail, refs={ 3 }", "EntryResult: word=typeDecl/V/size, refs={ 3 }", "EntryResult: word=typeDecl/V/temp, refs={ 3 }", "EntryResult: word=typeDecl/V/x, refs={ 3 }"};
	 if (eresults.length != entryResultBeforeModel.length)
			fail("Entry Result length different from model");	

	 for (int i=0;i<eresults.length; i++)
	 {
		assertEquals(entryResultBeforeModel[i],eresults[i].toString());
	 }
	 //Delete mail.cpp from the project, give some time to remove index
	 IResource resourceHdl = testProject.findMember("mail.cpp") ;
	 // Cleaning up file handles before delete
	 System.gc();
	 System.runFinalization();
	 resourceHdl.delete(true,monitor);
	 Thread.sleep(10000);
	 //See if the index is still there
	 ind = indexManager.getIndex(testProjectPath,true,true);
	 eresults = ind.queryEntries(prefix);
	 assertTrue("Entry exists", eresults != null);
		
	 String [] entryResultAfterModel ={"EntryResult: word=typeDecl/C/CDocumentManager, refs={ 2 }", "EntryResult: word=typeDecl/V/, refs={ 1, 2 }"};
	 if (eresults.length != entryResultAfterModel.length)
		fail("Entry Result length different from model");
		
	 for (int i=0;i<eresults.length; i++)
	 {
		assertEquals(entryResultAfterModel[i],eresults[i].toString());
	 }
	}
	
	public void testIndexContents() throws Exception{
		 
		//Add a new file to the project, give it some time to index
		fileIndexed = false;
		importFile("extramail.cpp","resources/indexer/extramail.cpp");
		while (fileIndexed != true){ Thread.sleep(TIMEOUT);}
		
		//Make sure project got added to index
		IPath testProjectPath = testProject.getFullPath();
		IIndex ind = indexManager.getIndex(testProjectPath,true,true);
		assertTrue("Index exists for project",ind != null);
	
		IEntryResult[] typerefreesults = ind.queryEntries(IIndexConstants.TYPE_REF);
		assertTrue("Type Ref Results exist", typerefreesults != null);
		
		String [] typeDeclEntryResultModel ={"EntryResult: word=typeDecl/C/Mail/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/C/Unknown/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/C/container/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/C/first_class/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/C/postcard/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/D/Mail/Y/X/Z, refs={ 1 }", "EntryResult: word=typeDecl/D/first_class/Y/X/Z, refs={ 1 }", "EntryResult: word=typeDecl/D/postcard/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/E/test/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/T/int32, refs={ 1 }", "EntryResult: word=typeDecl/V/PO_Box, refs={ 1 }","EntryResult: word=typeDecl/V/index, refs={ 1 }", "EntryResult: word=typeDecl/V/mail, refs={ 1 }", "EntryResult: word=typeDecl/V/size, refs={ 1 }", "EntryResult: word=typeDecl/V/temp, refs={ 1 }", "EntryResult: word=typeDecl/V/x, refs={ 1 }", "EntryResult: word=typeDecl/V/x/Z, refs={ 1 }"};

		IEntryResult[] typedeclresults =ind.queryEntries(IIndexConstants.TYPE_DECL);
		assertTrue("Type Decl Results exist", typedeclresults != null);
		
		if (typedeclresults.length != typeDeclEntryResultModel.length)
			fail("Entry Result length different from model for typeDecl");
	
		for (int i=0;i<typedeclresults.length; i++)
		{
			assertEquals(typeDeclEntryResultModel[i],typedeclresults[i].toString());
		}
	
		String [] typeDefEntryResultModel ={"EntryResult: word=typeDecl/T/int32, refs={ 1 }"};
		IEntryResult[] typedefresults =ind.queryEntries(IIndexConstants.TYPEDEF_DECL);
		assertTrue("Type Def Results exist", typedefresults != null);
		
		if (typedefresults.length != typeDefEntryResultModel.length)
					fail("Entry Result length different from model for typeDef");
	
		for (int i=0;i<typedefresults.length; i++)
		{
		 assertEquals(typeDefEntryResultModel[i],typedefresults[i].toString());
		}
				
		String [] namespaceResultModel = {"EntryResult: word=namespaceDecl/X/Z, refs={ 1 }", "EntryResult: word=namespaceDecl/Y/X/Z, refs={ 1 }", "EntryResult: word=namespaceDecl/Z, refs={ 1 }"};
		IEntryResult[] namespaceresults =ind.queryEntries(IIndexConstants.NAMESPACE_DECL);
		assertTrue("Namespace Results exist", namespaceresults != null);
		
		if (namespaceresults.length != namespaceResultModel.length)
				fail("Entry Result length different from model for namespace");
	
		for (int i=0;i<namespaceresults.length; i++)
		{
			assertEquals(namespaceResultModel[i],namespaceresults[i].toString());
		}
				
		String [] fieldResultModel = {"EntryResult: word=fieldDecl/array/container/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/index/container/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/postage/Mail/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/sz/container/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/type/Mail/Y/X/Z, refs={ 1 }"};
		IEntryResult[] fieldresults =ind.queryEntries(IIndexConstants.FIELD_DECL);
		assertTrue("Field Results exist", fieldresults != null);
		
		if (fieldresults.length != fieldResultModel.length)
				fail("Entry Result length different from model for fieldDecl");
	
		for (int i=0;i<fieldresults.length; i++)
		{
			assertEquals(fieldResultModel[i],fieldresults[i].toString());
		}
		
		String [] enumeratorResultModel = {"EntryResult: word=enumtorDecl/bye/Y/X/Z, refs={ 1 }", "EntryResult: word=enumtorDecl/cool/Y/X/Z, refs={ 1 }", "EntryResult: word=enumtorDecl/hi/Y/X/Z, refs={ 1 }", "EntryResult: word=enumtorDecl/why/Y/X/Z, refs={ 1 }"};
		IEntryResult[] enumeratorresults =ind.queryEntries(IIndexConstants.ENUMTOR_DECL);
		assertTrue("Enumerator Results exist", enumeratorresults != null);
		
		if (enumeratorresults.length != enumeratorResultModel.length)
				fail("Entry Result length different from model for enumtorDecl");
	
		for (int i=0;i<enumeratorresults.length; i++)
		{
			assertEquals(enumeratorResultModel[i],enumeratorresults[i].toString());
		}
	
		String [] functionResultModel = {"EntryResult: word=functionDecl/doSomething, refs={ 1 }", "EntryResult: word=functionDecl/main/Y/X/Z, refs={ 1 }"};
		IEntryResult[] functionresults =ind.queryEntries(IIndexConstants.FUNCTION_DECL);
		
		if (functionresults.length != functionResultModel.length)
					fail("Entry Result length different from model for functionDecl");

		for (int i=0;i<functionresults.length; i++)
		{
			assertEquals(functionResultModel[i],functionresults[i].toString());
		}
		
		String [] methodResultModel = {"EntryResult: word=methodDecl/Mail/Mail/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/Unknown/Unknown/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/container/container/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/first_class/first_class/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/operator =/container/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/operator []/container/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/postcard/postcard/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/print/Mail/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/print/Unknown/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/print/first_class/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/print/postcard/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/size/container/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/~container/container/Y/X/Z, refs={ 1 }"};
									   
									    
									   
	
		IEntryResult[] methodresults =ind.queryEntries(IIndexConstants.METHOD_DECL);
		assertTrue("Entry exists", methodresults != null);
		
		if (methodresults.length != methodResultModel.length)
				fail("Entry Result length different from model for functionDecl");
	
		for (int i=0;i<methodresults.length; i++)
		{
			assertEquals("Index is " +i , methodResultModel[i],methodresults[i].toString());
		}
  }
  
  public void testRefs() throws Exception{
		  //Add a new file to the project, give it some time to index 
  		  fileIndexed = false;
		  importFile("reftest.cpp","resources/indexer/reftest.cpp");
		  while (fileIndexed != true){ Thread.sleep(TIMEOUT);}
		  
		  //Make sure project got added to index
		  IPath testProjectPath = testProject.getFullPath();
		  IIndex ind = indexManager.getIndex(testProjectPath,true,true);
		  assertTrue("Index exists for project",ind != null);
		  
		  String [] typeRefEntryResultModel ={"EntryResult: word=typeRef/C/C/B/A, refs={ 1 }", "EntryResult: word=typeRef/E/e1/B/A, refs={ 1 }", "EntryResult: word=typeRef/G/ForwardA/A, refs={ 1 }", "EntryResult: word=typeRef/V/x/B/A, refs={ 1 }"};
		  IEntryResult[] typerefresults = ind.queryEntries(IIndexConstants.TYPE_REF);
		  assertTrue("Entry exists",typerefresults != null);
		  
		  if (typerefresults.length != typeRefEntryResultModel.length)
			  fail("Entry Result length different from model for typeRef");
	
		  for (int i=0;i<typerefresults.length; i++)
		  {
			  assertEquals(typeRefEntryResultModel[i],typerefresults[i].toString());
		  }
	
		  String [] funRefEntryResultModel ={"EntryResult: word=functionRef/something/A, refs={ 1 }"};
		  IEntryResult[] funRefresults = ind.queryEntries(IIndexConstants.FUNCTION_REF);
		  assertTrue("Entry exists",funRefresults != null);
		  
		  if (funRefresults.length != funRefEntryResultModel.length)
					  fail("Entry Result length different from model for funcRef");
	
		  for (int i=0;i<funRefresults.length; i++)
		  {
		   assertEquals(funRefEntryResultModel[i],funRefresults[i].toString());
		  }
				
		  String [] namespaceRefResultModel = {"EntryResult: word=namespaceRef/A, refs={ 1 }", "EntryResult: word=namespaceRef/B/A, refs={ 1 }"};
		  IEntryResult[] namespacerefresults = ind.queryEntries(IIndexConstants.NAMESPACE_REF);
		  assertTrue("Entry exists",namespacerefresults!=null);
		  
		  if (namespacerefresults.length != namespaceRefResultModel.length)
				  fail("Entry Result length different from model for namespaceRef");
	
		  for (int i=0;i<namespacerefresults.length; i++)
		  {
			  assertEquals(namespaceRefResultModel[i],namespacerefresults[i].toString());
		  }
				
		  String [] fieldRefResultModel = {"EntryResult: word=fieldRef/y/C/B/A, refs={ 1 }"};
		  IEntryResult[] fieldrefresults = ind.queryEntries(IIndexConstants.FIELD_REF);
		  assertTrue("Entry exists",fieldrefresults!=null);
		  
		  if (fieldrefresults.length != fieldRefResultModel.length)
				  fail("Entry Result length different from model for fieldRef");
	
		  for (int i=0;i<fieldrefresults.length; i++)
		  {
			  assertEquals(fieldRefResultModel[i],fieldrefresults[i].toString());
		  }
	
		  String [] methodRefResultModel = {"EntryResult: word=methodRef/bar/C/B/A, refs={ 1 }"};	
		  IEntryResult[] methodrefresults = ind.queryEntries(IIndexConstants.METHOD_REF);
		  assertTrue("Entry exists", methodrefresults != null); 
		   
		  if (methodrefresults.length != methodRefResultModel.length)
				  fail("Entry Result length different from model for methodRef");
	
		  for (int i=0;i<methodrefresults.length; i++)
		  {
			  assertEquals(methodRefResultModel[i],methodrefresults[i].toString());
		  }
	}
	
  public void testExactDeclarations() throws Exception
  {
  	 
	 fileIndexed = false;
  	 importFile("a.h","resources/dependency/a.h");
	 while (fileIndexed != true){ Thread.sleep(TIMEOUT);}
	 
  	  //Make sure project got added to index
	  IPath testProjectPath = testProject.getFullPath();
	  IIndex ind = indexManager.getIndex(testProjectPath,true,true);
	  assertTrue("Index exists for project",ind != null);
	  
	  fileIndexed = false;
	  importFile("DepTest3.h","resources/dependency/DepTest3.h");
	  while (fileIndexed != true){ Thread.sleep(TIMEOUT);}
	  
	  fileIndexed = false;
	  importFile("DepTest3.cpp","resources/dependency/DepTest3.cpp");
	  while (fileIndexed != true){ Thread.sleep(TIMEOUT);}

	  
	  IEntryResult[] eResult = ind.queryEntries(IIndexConstants.CLASS_DECL);
	  IQueryResult[] qResult = ind.queryPrefix(IIndexConstants.CLASS_DECL);
	  
	  assertTrue("Expected 2 files indexed", qResult.length == 2);
	  assertTrue("Checking DepTest3.h location", qResult[0].getPath().equals("/IndexerTestProject/DepTest3.h"));
	  assertTrue("Checking a.h location", qResult[1].getPath().equals("/IndexerTestProject/a.h"));
	  
	  assertTrue("Expect 2 class declaration entries", eResult.length == 2);
	  
	  int[] DepTest3FileRefs = {2};
	
	  int[] fileRefs = eResult[0].getFileReferences();
	
	  assertTrue("Check DepTest3 File Refs number", fileRefs.length == 1);
	  
	  for (int i=0; i<fileRefs.length; i++){
	  	assertTrue("Verify DepTest3 File Ref",fileRefs[i] == DepTest3FileRefs[i]);
	  }
	  
	  int[] aFileRefs = {3};
	
	  fileRefs = eResult[1].getFileReferences();
	 
	  assertTrue("Check a.h File Refs number", fileRefs.length == 1);
	  
	  for (int i=0; i<fileRefs.length; i++){
	  	assertTrue("Verify a.h File Ref",fileRefs[i] == aFileRefs[i]);
	  }
	  
  }
  
  public void testMD5() throws Exception
  {
  	fileIndexed = false;
  	importFile("extramail.cpp","resources/indexer/extramail.cpp");
  	//importFile("mail.cpp","resources/indexer/mail.cpp");
  	
	while (fileIndexed != true){ Thread.sleep(TIMEOUT);}
  	
	MessageDigest md = MessageDigest.getInstance("MD5");
	//MessageDigest md = MessageDigest.getInstance("SHA");
	String fileName = testProject.getFile("extramail.cpp").getLocation().toOSString();
	//String fileName = testProject.getFile("mail.cpp").getLocation().toOSString();
	
	long startTime = System.currentTimeMillis();
	
	FileInputStream stream = new FileInputStream(fileName);
	FileChannel channel = stream.getChannel();
	
	ByteBuffer byteBuffer = ByteBuffer.allocate((int)channel.size());
	channel.read(byteBuffer);
	byteBuffer.rewind();
	
	md.update(byteBuffer.array());
	byte[] messageDigest = md.digest();

  	//System.out.println("Elapsed Time: " + (System.currentTimeMillis() - startTime) + " ms");
	
  	 
  }
  
  public void testMacros() throws Exception
  {
	  //Add a new file to the project, give it some time to index
  	  fileIndexed = false;
	  importFile("extramail.cpp","resources/indexer/extramail.cpp");
	  while (fileIndexed != true){ Thread.sleep(TIMEOUT);}
	
	  //Make sure project got added to index
	  IPath testProjectPath = testProject.getFullPath();
	  IIndex ind = indexManager.getIndex(testProjectPath,true,true);
	  assertTrue("Index exists for project",ind != null);
	
	  IEntryResult[] macroresults = ind.queryEntries(IIndexConstants.MACRO_DECL);
	  assertTrue("Entry exists", macroresults != null);
	  
	  String [] macroResultModel = {"EntryResult: word=macroDecl/CASE, refs={ 1 }", "EntryResult: word=macroDecl/MAX, refs={ 1 }", "EntryResult: word=macroDecl/PRINT, refs={ 1 }"};
	   
	  if (macroresults.length != macroResultModel.length)
		 fail("Entry Result length different from model for macros");

	  for (int i=0;i<macroresults.length; i++)
	  {
		assertEquals(macroResultModel[i],macroresults[i].toString());
	  }
  }
  
  public void testIndexShutdown() throws Exception{
	//Add a new file to the project, give it some time to index
  	 fileIndexed = false;
	 importFile("reftest.cpp","resources/indexer/reftest.cpp");
	 while (fileIndexed != true){ Thread.sleep(TIMEOUT);}
	 
	 //Make sure project got added to index
	 IPath testProjectPath = testProject.getFullPath();
	 IIndex ind = indexManager.getIndex(testProjectPath,true,true);
	 assertTrue("Index exists for project",ind != null);
	 
	 //Create an empty index file
	 String badIndexFile = CCorePlugin.getDefault().getStateLocation().append("badIndex.index").toOSString();
	 FileWriter writer = null;
	 try {
		writer = new FileWriter(badIndexFile);
		writer.flush();
		writer.close();
	 }
	 catch (IOException e){}
	 
	File indexesDirectory = new File(CCorePlugin.getDefault().getStateLocation().toOSString());

	//This should get rid of the empty index file from the metadata and 
	//remove the index from the indexes (since its .index file is missing)
	indexManager.shutdown();
	
	File[] indexesFiles = indexesDirectory.listFiles();
	if (indexesFiles != null) {
		for (int i = 0, indexesFilesLength = indexesFiles.length; i < indexesFilesLength; i++) {
				if(indexesFiles[i].getName().equals("badIndex.index")){
					fail("Shutdown did not delete .index file");
				}
		}
	}
  }
  
  public void testForwardDeclarations() throws Exception{
	//Add a new file to the project, give it some time to index
  	fileIndexed = false;
	importFile("reftest.cpp","resources/indexer/reftest.cpp");
	while (fileIndexed != true){ Thread.sleep(TIMEOUT);}
	
	 //Make sure project got added to index
	 IPath testProjectPath = testProject.getFullPath();
	 IIndex ind = indexManager.getIndex(testProjectPath,true,true);
	 assertTrue("Index exists for project",ind != null);

	//IEntryResult[] fwdDclResults = ind.queryEntries("typeDecl/C/ForwardA/A".toCharArray());
	 IEntryResult[] fwdDclResults = ind.queryEntries("typeDecl/G/ForwardA/A".toCharArray());
	 assertTrue("Entry exists",fwdDclResults != null);
	 
	 String [] fwdDclModel = {"EntryResult: word=typeDecl/G/ForwardA/A, refs={ 1 }"};
	
	 if (fwdDclResults.length != fwdDclModel.length)
		fail("Entry Result length different from model for forward declarations");

	 for (int i=0;i<fwdDclResults.length; i++)
	 {
	   assertEquals(fwdDclModel[i],fwdDclResults[i].toString());
	 }

	IEntryResult[] fwdDclRefResults = ind.queryEntries("typeRef/G/ForwardA/A".toCharArray());
	assertTrue("Entry exists", fwdDclRefResults!= null);
	
	String [] fwdDclRefModel = {"EntryResult: word=typeRef/G/ForwardA/A, refs={ 1 }"};

	if (fwdDclRefResults.length != fwdDclRefModel.length)
	   fail("Entry Result length different from model for forward declarations refs");

	for (int i=0;i<fwdDclRefResults.length; i++)
	{
	  assertEquals(fwdDclRefModel[i],fwdDclRefResults[i].toString());
	}
  }

public void indexChanged(IndexChangeEvent event) {
	IIndexDelta delta = event.getDelta();
	if (delta.getDeltaType() == IIndexDelta.MERGE_DELTA){
		fileIndexed = true;
	}
}

}
