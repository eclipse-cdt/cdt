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
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer;
import org.eclipse.cdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
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
public class SourceIndexerTests extends TestCase implements IIndexChangeListener  {
	IFile 					file;
	IProject 				testProject;
	NullProgressMonitor		monitor;
	IndexManager 			indexManager;
	SourceIndexer			sourceIndexer;
	boolean					fileIndexed;
	
	static final String sourceIndexerID = "org.eclipse.cdt.core.originalsourceindexer"; //$NON-NLS-1$
	public static final int TIMEOUT = 50;
	/**
	 * Constructor for IndexManagerTest.
	 * @param name
	 */
	public SourceIndexerTests(String name) {
		super(name);
	}

	public void resetIndexState() {
		fileIndexed = false;
	}
	
	public void waitForIndex(int maxSec) throws Exception {
		int delay = 0;
		while (fileIndexed != true && delay < (maxSec * 1000))
		{ 
			Thread.sleep(TIMEOUT);
			delay += TIMEOUT;
		}
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
		testProject = createProject("IndexerTestProject"); //$NON-NLS-1$
		IPath pathLoc = CCorePlugin.getDefault().getStateLocation();
		
		File indexFile = new File(pathLoc.append("3915980774.index").toOSString()); //$NON-NLS-1$
		if (indexFile.exists())
			indexFile.delete();
		
		//Set the id of the source indexer extension point as a session property to allow
		//index manager to instantiate it
		testProject.setSessionProperty(IndexManager.indexerIDKey, sourceIndexerID);
		
		//Enable indexing on test project
		testProject.setSessionProperty(SourceIndexer.activationKey,new Boolean(true));
		
		if (testProject==null)
			fail("Unable to create project");	 //$NON-NLS-1$
		
		indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
		//indexManager.reset();
		//Get the indexer used for the test project
		sourceIndexer = (SourceIndexer) indexManager.getIndexerForProject(testProject);
		sourceIndexer.addIndexChangeListener(this);
	}
	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() {
		try {
			super.tearDown();
			sourceIndexer.removeIndexChangeListener(this);
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
		StringBuffer message = new StringBuffer("["); //$NON-NLS-1$
		message.append(status.getMessage());
		if (status.isMultiStatus()) {
			IStatus children[] = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				message.append(getMessage(children[i]));
			}
		}
		message.append("]"); //$NON-NLS-1$
		return message.toString();
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(SourceIndexerTests.class.getName());

		suite.addTest(new SourceIndexerTests("testAddNewFileToIndex")); //$NON-NLS-1$
		suite.addTest(new SourceIndexerTests("testForwardDeclarations")); //$NON-NLS-1$
		suite.addTest(new SourceIndexerTests("testIndexAll")); //$NON-NLS-1$
		suite.addTest(new SourceIndexerTests("testIndexContents")); //$NON-NLS-1$
		suite.addTest(new SourceIndexerTests("testMacros")); //$NON-NLS-1$
		suite.addTest(new SourceIndexerTests("testRefs")); //$NON-NLS-1$
		suite.addTest(new SourceIndexerTests("testExactDeclarations")); //$NON-NLS-1$
		suite.addTest(new SourceIndexerTests("testRemoveFileFromIndex")); //$NON-NLS-1$
		suite.addTest(new SourceIndexerTests("testRemoveProjectFromIndex")); //$NON-NLS-1$
		suite.addTest(new SourceIndexerTests("testIndexShutdown")); //$NON-NLS-1$
	
		return suite;
	
	}
	/*
	 * Utils
	 */
	private IProject createProject(String projectName) throws CoreException {
		ICProject cPrj = CProjectHelper.createCCProject(projectName, "bin"); //$NON-NLS-1$
		return cPrj.getProject();
	}
	
	private IFile importFile(String fileName, String resourceLocation)throws Exception {
		resetIndexState();
		//Obtain file handle
		file = testProject.getProject().getFile(fileName);
		//Create file input stream
		monitor = new NullProgressMonitor();
		if (!file.exists()) {
			file.create(new FileInputStream(
					CTestPlugin.getDefault().getFileInPlugin(new Path(resourceLocation))),
					false, monitor);
		}
		waitForIndex(20); // only wait 20 seconds max.
		return file;
	}

	/*
	 * Start of tests
	 */ 	
	public void testIndexAll() throws Exception {
		
		//Add a file to the project
		
		importFile("mail.cpp","resources/indexer/mail.cpp"); //$NON-NLS-1$ //$NON-NLS-2$
		
		IIndex ind = sourceIndexer.getIndex(testProject.getFullPath(),true,true);
		assertTrue("Index exists for project",ind != null); //$NON-NLS-1$
		
		char[] prefix = "typeDecl/".toCharArray(); //$NON-NLS-1$
		IQueryResult[] qresults = ind.queryPrefix(prefix);
		IEntryResult[] eresults = ind.queryEntries(prefix);
		
		assertTrue("Query Results exist", qresults != null); //$NON-NLS-1$
		assertTrue("Entry Results exist", eresults != null); //$NON-NLS-1$
		
		String [] queryResultModel = {"IndexedFile(1: /IndexerTestProject/mail.cpp)"}; //$NON-NLS-1$
		String [] entryResultModel ={"EntryResult: word=typeDecl/C/Mail, refs={ 1 }, offsets={ [ 294] }",  //$NON-NLS-1$
				"EntryResult: word=typeDecl/C/Unknown, refs={ 1 }, offsets={ [ 2738] }",  //$NON-NLS-1$
				"EntryResult: word=typeDecl/C/container, refs={ 1 }, offsets={ [ 21084] }",  //$NON-NLS-1$
				"EntryResult: word=typeDecl/C/first_class, refs={ 1 }, offsets={ [ 2506] }",  //$NON-NLS-1$
				"EntryResult: word=typeDecl/C/postcard, refs={ 1 }, offsets={ [ 2298] }",  //$NON-NLS-1$
				"EntryResult: word=typeDecl/D/Mail, refs={ 1 }, offsets={ [ 294] }",  //$NON-NLS-1$
				"EntryResult: word=typeDecl/D/first_class, refs={ 1 }, offsets={ [ 2506] }",  //$NON-NLS-1$ 
				"EntryResult: word=typeDecl/D/postcard, refs={ 1 }, offsets={ [ 2298] }",  //$NON-NLS-1$
				"EntryResult: word=typeDecl/V/PO_Box, refs={ 1 }, offsets={ [ 21371] }",  //$NON-NLS-1$
				"EntryResult: word=typeDecl/V/index, refs={ 1 }, offsets={ [ 21303, 21846] }",  //$NON-NLS-1$
				"EntryResult: word=typeDecl/V/mail, refs={ 1 }, offsets={ [ 21336, 21912] }",  //$NON-NLS-1$
				"EntryResult: word=typeDecl/V/size, refs={ 1 }, offsets={ [ 21927] }",  //$NON-NLS-1$
				"EntryResult: word=typeDecl/V/temp, refs={ 1 }, offsets={ [ 21964] }",  //$NON-NLS-1$
				"EntryResult: word=typeDecl/V/x, refs={ 1 }, offsets={ [ 21201, 21526] }"}; //$NON-NLS-1$ 
		
		
		if (qresults.length != queryResultModel.length)
			fail("Query Result length different from model"); //$NON-NLS-1$

		if (eresults.length != entryResultModel.length)
			fail("Entry Result length different from model"); //$NON-NLS-1$

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
		importFile("mail.cpp","resources/indexer/mail.cpp"); //$NON-NLS-1$  //$NON-NLS-2$
		
		//Make sure project got added to index
		IPath testProjectPath = testProject.getFullPath();
		IIndex ind = sourceIndexer.getIndex(testProjectPath,true,true);
		assertTrue("Index exists for project",ind != null); //$NON-NLS-1$
		//Add a new file to the project, give it some time to index

		importFile("DocumentManager.h","resources/indexer/DocumentManager.h"); //$NON-NLS-1$ //$NON-NLS-2$
		 
		importFile("DocumentManager.cpp","resources/indexer/DocumentManager.cpp"); //$NON-NLS-1$ //$NON-NLS-2$
	
		ind = sourceIndexer.getIndex(testProjectPath,true,true);
		
		char[] prefix = "typeDecl/C/CDocumentManager".toCharArray(); //$NON-NLS-1$
		String [] entryResultModel ={"EntryResult: word=typeDecl/C/CDocumentManager, refs={ 2 }, offsets={ [ 2127] }"}; //$NON-NLS-1$
		IEntryResult[] eresults =ind.queryEntries(prefix);
		IEntryResult[] bogRe = ind.queryEntries(IIndexConstants.TYPE_DECL);
		assertTrue("Entry Result exists", eresults != null); //$NON-NLS-1$
		
		if (eresults.length != entryResultModel.length)
			fail("Entry Result length different from model"); //$NON-NLS-1$

		for (int i=0;i<eresults.length; i++)
		{
			assertEquals(entryResultModel[i],eresults[i].toString());
		}
	}

	public void testRemoveProjectFromIndex() throws Exception{
	  
		
	  //Add a file to the project
	  importFile("mail.cpp","resources/indexer/mail.cpp"); //$NON-NLS-1$ //$NON-NLS-2$

	  //Make sure project got added to index
	  IPath testProjectPath = testProject.getFullPath();
	  IIndex ind = sourceIndexer.getIndex(testProjectPath,true,true);
	  assertTrue("Index exists for project",ind != null); //$NON-NLS-1$
	  //Delete the project
	  safeDelete(testProject);
	  
	  //See if the index is still there
	  ind = sourceIndexer.getIndex(testProjectPath,true,true);
	  assertTrue("Index deleted",ind == null); //$NON-NLS-1$
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
	importFile("mail.cpp","resources/indexer/mail.cpp"); //$NON-NLS-1$ //$NON-NLS-2$
	
	//Make sure project got added to index
	IPath testProjectPath = testProject.getFullPath();
	IIndex ind = sourceIndexer.getIndex(testProjectPath,true,true);
	assertTrue("Index exists for project",ind != null); //$NON-NLS-1$
	importFile("DocumentManager.h","resources/indexer/DocumentManager.h"); //$NON-NLS-1$ //$NON-NLS-2$
	importFile("DocumentManager.cpp","resources/indexer/DocumentManager.cpp"); //$NON-NLS-1$ //$NON-NLS-2$
	
	ind = sourceIndexer.getIndex(testProjectPath,true,true);
		 
	 //Do a "before" deletion comparison
	 //ind = indexManager.getIndex(testProjectPath,true,true);
	 char[] prefix = "typeDecl/".toCharArray(); //$NON-NLS-1$
	 IEntryResult[] eresults = ind.queryEntries(prefix);
	 assertTrue("Entry result found for typdeDecl/", eresults != null); //$NON-NLS-1$
	 
	 String [] entryResultBeforeModel ={"EntryResult: word=typeDecl/C/CDocumentManager, refs={ 2 }, offsets={ [ 2127] }",
			 "EntryResult: word=typeDecl/C/Mail, refs={ 3 }, offsets={ [ 294] }",
			 "EntryResult: word=typeDecl/C/Unknown, refs={ 3 }, offsets={ [ 2738] }",
			 "EntryResult: word=typeDecl/C/container, refs={ 3 }, offsets={ [ 21084] }", 
			 "EntryResult: word=typeDecl/C/first_class, refs={ 3 }, offsets={ [ 2506] }", 
			 "EntryResult: word=typeDecl/C/postcard, refs={ 3 }, offsets={ [ 2298] }", 
			 "EntryResult: word=typeDecl/D/Mail, refs={ 3 }, offsets={ [ 294] }",
			 "EntryResult: word=typeDecl/D/first_class, refs={ 3 }, offsets={ [ 2506] }", 
			 "EntryResult: word=typeDecl/D/postcard, refs={ 3 }, offsets={ [ 2298] }", 
			 "EntryResult: word=typeDecl/V/PO_Box, refs={ 3 }, offsets={ [ 21371] }", 
			 "EntryResult: word=typeDecl/V/index, refs={ 3 }, offsets={ [ 21303, 21846] }", 
			 "EntryResult: word=typeDecl/V/mail, refs={ 3 }, offsets={ [ 21336, 21912] }", 
			 "EntryResult: word=typeDecl/V/size, refs={ 3 }, offsets={ [ 21927] }", 
			 "EntryResult: word=typeDecl/V/temp, refs={ 3 }, offsets={ [ 21964] }", 
			 "EntryResult: word=typeDecl/V/x, refs={ 3 }, offsets={ [ 21201, 21526] }"};
	 
	 if (eresults.length != entryResultBeforeModel.length)
			fail("Entry Result length different from model"); //$NON-NLS-1$	 

	 for (int i=0;i<eresults.length; i++)
	 {
		assertEquals(entryResultBeforeModel[i],eresults[i].toString());
	 }
	 //Delete mail.cpp from the project, give some time to remove index
	 IResource resourceHdl = testProject.findMember("mail.cpp") ; //$NON-NLS-1$
	 // Cleaning up file handles before delete
	 System.gc();
	 System.runFinalization();
	 resetIndexState();
	 resourceHdl.delete(true,monitor);
	 waitForIndex(10); // wait up to 10 seconds for the index to be deleted.
	 
	 //See if the index is still there
	 ind = sourceIndexer.getIndex(testProjectPath,true,true);
	 eresults = ind.queryEntries(prefix);
	 assertTrue("Entry exists", eresults != null);  //$NON-NLS-1$ 
		
	 String [] entryResultAfterModel ={"EntryResult: word=typeDecl/C/CDocumentManager, refs={ 2 }, offsets={ [ 2127] }"};  //$NON-NLS-1$ //$NON-NLS-2$ 
	 if (eresults.length != entryResultAfterModel.length)
		fail("Entry Result length different from model");  //$NON-NLS-1$
		
	 for (int i=0;i<eresults.length; i++)
	 {
		assertEquals(entryResultAfterModel[i],eresults[i].toString());
	 }
	}
	
	public void testIndexContents() throws Exception{
		 
		//Add a new file to the project
		importFile("extramail.cpp","resources/indexer/extramail.cpp");  //$NON-NLS-1$ //$NON-NLS-2$ 
		
		//Make sure project got added to index
		IPath testProjectPath = testProject.getFullPath();
		IIndex ind = sourceIndexer.getIndex(testProjectPath,true,true);
		assertTrue("Index exists for project",ind != null);  //$NON-NLS-1$ //$NON-NLS-2$ 
	
		IEntryResult[] typerefreesults = ind.queryEntries(IIndexConstants.TYPE_REF);
		assertTrue("Type Ref Results exist", typerefreesults != null);  //$NON-NLS-1$
		
		String [] typeDeclEntryResultModel ={"EntryResult: word=typeDecl/C/Mail/Y/X/Z, refs={ 1 }, offsets={ [ 2335] }","EntryResult: word=typeDecl/C/Unknown/Y/X/Z, refs={ 1 }, offsets={ [ 21063] }",  //$NON-NLS-1$ //$NON-NLS-2$ 
				"EntryResult: word=typeDecl/C/container/Y/X/Z, refs={ 1 }, offsets={ [ 21445] }","EntryResult: word=typeDecl/C/first_class/Y/X/Z, refs={ 1 }, offsets={ [ 2804] }",  //$NON-NLS-1$ //$NON-NLS-2$ 
				"EntryResult: word=typeDecl/C/postcard/Y/X/Z, refs={ 1 }, offsets={ [ 2572] }","EntryResult: word=typeDecl/D/Mail/Y/X/Z, refs={ 1 }, offsets={ [ 2335] }",  //$NON-NLS-1$ //$NON-NLS-2$ 
				"EntryResult: word=typeDecl/D/first_class/Y/X/Z, refs={ 1 }, offsets={ [ 2804] }", "EntryResult: word=typeDecl/D/postcard/Y/X/Z, refs={ 1 }, offsets={ [ 2572] }",  //$NON-NLS-1$ //$NON-NLS-2$ 
				"EntryResult: word=typeDecl/E/test/Y/X/Z, refs={ 1 }, offsets={ [ 2302] }","EntryResult: word=typeDecl/T/int32, refs={ 1 }, offsets={ [ 2200] }",  //$NON-NLS-1$ //$NON-NLS-2$ 
				"EntryResult: word=typeDecl/V/PO_Box, refs={ 1 }, offsets={ [ 21792] }","EntryResult: word=typeDecl/V/index, refs={ 1 }, offsets={ [ 21706, 22333] }",  //$NON-NLS-1$ //$NON-NLS-2$ 
				"EntryResult: word=typeDecl/V/mail, refs={ 1 }, offsets={ [ 21742, 22402] }", "EntryResult: word=typeDecl/V/size, refs={ 1 }, offsets={ [ 22423] }",   //$NON-NLS-1$ //$NON-NLS-2$ 
				"EntryResult: word=typeDecl/V/temp, refs={ 1 }, offsets={ [ 22463] }", "EntryResult: word=typeDecl/V/x, refs={ 1 }, offsets={ [ 21589, 21965] }",  //$NON-NLS-1$ //$NON-NLS-2$ 
				"EntryResult: word=typeDecl/V/x/Z, refs={ 1 }, offsets={ [ 2259] }"}; //$NON-NLS-1$ 
		
		IEntryResult[] typedeclresults =ind.queryEntries(IIndexConstants.TYPE_DECL);
		assertTrue("Type Decl Results exist", typedeclresults != null);  //$NON-NLS-1$ 
		
		if (typedeclresults.length != typeDeclEntryResultModel.length)
			fail("Entry Result length different from model for typeDecl");  //$NON-NLS-1$
	
		for (int i=0;i<typedeclresults.length; i++)
		{
			assertEquals(typeDeclEntryResultModel[i],typedeclresults[i].toString());
		}
	
		String [] typeDefEntryResultModel ={"EntryResult: word=typeDecl/T/int32, refs={ 1 }, offsets={ [ 2200] }"};  //$NON-NLS-1$
		IEntryResult[] typedefresults =ind.queryEntries(IIndexConstants.TYPEDEF_DECL);
		assertTrue("Type Def Results exist", typedefresults != null);  //$NON-NLS-1$  
		
		if (typedefresults.length != typeDefEntryResultModel.length)
					fail("Entry Result length different from model for typeDef");  //$NON-NLS-1$  
	
		for (int i=0;i<typedefresults.length; i++)
		{
		 assertEquals(typeDefEntryResultModel[i],typedefresults[i].toString());
		}
				
		String [] namespaceResultModel = {"EntryResult: word=namespaceDecl/X/Z, refs={ 1 }, offsets={ [ 2274] }", "EntryResult: word=namespaceDecl/Y/X/Z, refs={ 1 }, offsets={ [ 2290] }",  //$NON-NLS-1$ //$NON-NLS-2$ 
				"EntryResult: word=namespaceDecl/Z, refs={ 1 }, offsets={ [ 2250] }"};  //$NON-NLS-1$ 
		
		IEntryResult[] namespaceresults =ind.queryEntries(IIndexConstants.NAMESPACE_DECL);
		assertTrue("Namespace Results exist", namespaceresults != null);  //$NON-NLS-1$  
		
		if (namespaceresults.length != namespaceResultModel.length)
				fail("Entry Result length different from model for namespace");  //$NON-NLS-1$
	
		for (int i=0;i<namespaceresults.length; i++)
		{
			assertEquals(namespaceResultModel[i],namespaceresults[i].toString());
		}
				
		String [] fieldResultModel = {"EntryResult: word=fieldDecl/array/container/Y/X/Z, refs={ 1 }, offsets={ [ 21485] }", "EntryResult: word=fieldDecl/index/container/Y/X/Z, refs={ 1 }, offsets={ [ 21500] }", 
				"EntryResult: word=fieldDecl/postage/Mail/Y/X/Z, refs={ 1 }, offsets={ [ 2469] }", "EntryResult: word=fieldDecl/sz/container/Y/X/Z, refs={ 1 }, offsets={ [ 21515] }", 
				"EntryResult: word=fieldDecl/type/Mail/Y/X/Z, refs={ 1 }, offsets={ [ 2488] }"};  //$NON-NLS-1$ //$NON-NLS-2$ 
		IEntryResult[] fieldresults =ind.queryEntries(IIndexConstants.FIELD_DECL);
		assertTrue("Field Results exist", fieldresults != null);  //$NON-NLS-1$
		
		if (fieldresults.length != fieldResultModel.length)
				fail("Entry Result length different from model for fieldDecl");  //$NON-NLS-1$ 
	
		for (int i=0;i<fieldresults.length; i++)
		{
			assertEquals(fieldResultModel[i],fieldresults[i].toString());
		}
		
		String [] enumeratorResultModel = {"EntryResult: word=enumtorDecl/bye/Y/X/Z, refs={ 1 }, offsets={ [ 2315] }", "EntryResult: word=enumtorDecl/cool/Y/X/Z, refs={ 1 }, offsets={ [ 2307] }", 
				"EntryResult: word=enumtorDecl/hi/Y/X/Z, refs={ 1 }, offsets={ [ 2312] }", "EntryResult: word=enumtorDecl/why/Y/X/Z, refs={ 1 }, offsets={ [ 2319] }"}; 
		
		IEntryResult[] enumeratorresults =ind.queryEntries(IIndexConstants.ENUMTOR_DECL);
		assertTrue("Enumerator Results exist", enumeratorresults != null);  //$NON-NLS-1$ 
		
		if (enumeratorresults.length != enumeratorResultModel.length)
				fail("Entry Result length different from model for enumtorDecl");  //$NON-NLS-1$ 
	
		for (int i=0;i<enumeratorresults.length; i++)
		{
			assertEquals(enumeratorResultModel[i],enumeratorresults[i].toString());
		}
	
		String [] functionResultModel = {"EntryResult: word=functionDecl/doSomething, refs={ 1 }, offsets={ [ 2222] }", "EntryResult: word=functionDecl/main/Y/X/Z, refs={ 1 }, offsets={ [ 21765] }"};  //$NON-NLS-1$ //$NON-NLS-2$ 
		IEntryResult[] functionresults =ind.queryEntries(IIndexConstants.FUNCTION_DECL);
		
		if (functionresults.length != functionResultModel.length)
					fail("Entry Result length different from model for functionDecl");  //$NON-NLS-1$ 

		for (int i=0;i<functionresults.length; i++)
		{
			assertEquals(functionResultModel[i],functionresults[i].toString());
		}
		
		String [] methodResultModel = {"EntryResult: word=methodDecl/Mail/Mail/Y/X/Z, refs={ 1 }, offsets={ [ 2362] }", 
				"EntryResult: word=methodDecl/Unknown/Unknown/Y/X/Z, refs={ 1 }, offsets={ [ 21152] }", 
				"EntryResult: word=methodDecl/container/container/Y/X/Z, refs={ 1 }, offsets={ [ 21535] }", 
				"EntryResult: word=methodDecl/first_class/first_class/Y/X/Z, refs={ 1 }, offsets={ [ 2852] }", 
				"EntryResult: word=methodDecl/operator =/container/Y/X/Z, refs={ 1 }, offsets={ [ 21724, 22384] }", 
				"EntryResult: word=methodDecl/operator []/container/Y/X/Z, refs={ 1 }, offsets={ [ 21691, 22318] }", 
				"EntryResult: word=methodDecl/postcard/postcard/Y/X/Z, refs={ 1 }, offsets={ [ 2617] }", 
				"EntryResult: word=methodDecl/print/Mail/Y/X/Z, refs={ 1 }, offsets={ [ 2388] }", 
				"EntryResult: word=methodDecl/print/Unknown/Y/X/Z, refs={ 1 }, offsets={ [ 21293] }", 
				"EntryResult: word=methodDecl/print/first_class/Y/X/Z, refs={ 1 }, offsets={ [ 2923] }", 
				"EntryResult: word=methodDecl/print/postcard/Y/X/Z, refs={ 1 }, offsets={ [ 2681] }", 
				"EntryResult: word=methodDecl/size/container/Y/X/Z, refs={ 1 }, offsets={ [ 21661] }", 
				"EntryResult: word=methodDecl/~container/container/Y/X/Z, refs={ 1 }, offsets={ [ 21563] }"};
									   
									    
									   
	
		IEntryResult[] methodresults =ind.queryEntries(IIndexConstants.METHOD_DECL);
		assertTrue("Entry exists", methodresults != null);  //$NON-NLS-1$ 
		
		if (methodresults.length != methodResultModel.length)
				fail("Entry Result length different from model for functionDecl");  //$NON-NLS-1$  
	
		for (int i=0;i<methodresults.length; i++)
		{
			assertEquals("Index is " +i , methodResultModel[i],methodresults[i].toString());  //$NON-NLS-1$  
		}
  }
  
  public void testRefs() throws Exception{
		  //Add a new file to the project
		  importFile("reftest.cpp","resources/indexer/reftest.cpp");  //$NON-NLS-1$ //$NON-NLS-2$ 
		  
		  //Make sure project got added to index
		  IPath testProjectPath = testProject.getFullPath();
		  IIndex ind = sourceIndexer.getIndex(testProjectPath,true,true);
		  assertTrue("Index exists for project",ind != null);  //$NON-NLS-1$ 
		  
		  String [] typeRefEntryResultModel ={"EntryResult: word=typeRef/C/C/B/A, refs={ 1 }, offsets={ [ 2142] }", 
				  "EntryResult: word=typeRef/E/e1/B/A, refs={ 1 }, offsets={ [ 2104] }", 
				  "EntryResult: word=typeRef/G/ForwardA/A, refs={ 1 }, offsets={ [ 225] }", 
				  "EntryResult: word=typeRef/V/x/B/A, refs={ 1 }, offsets={ [ 2128] }"}; 
		  
		  IEntryResult[] typerefresults = ind.queryEntries(IIndexConstants.TYPE_REF);
		  assertTrue("Entry exists",typerefresults != null); //$NON-NLS-1$ 
		  
		  if (typerefresults.length != typeRefEntryResultModel.length)
			  fail("Entry Result length different from model for typeRef"); //$NON-NLS-1$ 
	
		  for (int i=0;i<typerefresults.length; i++)
		  {
			  assertEquals(typeRefEntryResultModel[i],typerefresults[i].toString());
		  }
	
		  String [] funRefEntryResultModel ={"EntryResult: word=functionRef/something/A, refs={ 1 }, offsets={ [ 259] }"};//$NON-NLS-1$ 
		  IEntryResult[] funRefresults = ind.queryEntries(IIndexConstants.FUNCTION_REF);
		  assertTrue("Entry exists",funRefresults != null); //$NON-NLS-1$ 
		  
		  if (funRefresults.length != funRefEntryResultModel.length)
					  fail("Entry Result length different from model for funcRef"); //$NON-NLS-1$
	
		  for (int i=0;i<funRefresults.length; i++)
		  {
		   assertEquals(funRefEntryResultModel[i],funRefresults[i].toString());
		  }
				
		  String [] namespaceRefResultModel = {"EntryResult: word=namespaceRef/A, refs={ 1 }, offsets={ [ 210] }", "EntryResult: word=namespaceRef/B/A, refs={ 1 }, offsets={ [ 288] }"}; //$NON-NLS-1$ //$NON-NLS-2$ 
		  IEntryResult[] namespacerefresults = ind.queryEntries(IIndexConstants.NAMESPACE_REF);
		  assertTrue("Entry exists",namespacerefresults!=null); //$NON-NLS-1$ 
		  
		  if (namespacerefresults.length != namespaceRefResultModel.length)
				  fail("Entry Result length different from model for namespaceRef"); //$NON-NLS-1$
	
		  for (int i=0;i<namespacerefresults.length; i++)
		  {
			  assertEquals(namespaceRefResultModel[i],namespacerefresults[i].toString());
		  }
				
		  String [] fieldRefResultModel = {"EntryResult: word=fieldRef/y/C/B/A, refs={ 1 }, offsets={ [ 2161] }"}; //$NON-NLS-1$
		  IEntryResult[] fieldrefresults = ind.queryEntries(IIndexConstants.FIELD_REF);
		  assertTrue("Entry exists",fieldrefresults!=null); //$NON-NLS-1$ 
		  
		  if (fieldrefresults.length != fieldRefResultModel.length)
				  fail("Entry Result length different from model for fieldRef"); //$NON-NLS-1$  
	
		  for (int i=0;i<fieldrefresults.length; i++)
		  {
			  assertEquals(fieldRefResultModel[i],fieldrefresults[i].toString());
		  }
	
		  String [] methodRefResultModel = {"EntryResult: word=methodRef/bar/C/B/A, refs={ 1 }, offsets={ [ 2184] }"}; //$NON-NLS-1$	 
		  IEntryResult[] methodrefresults = ind.queryEntries(IIndexConstants.METHOD_REF);
		  assertTrue("Entry exists", methodrefresults != null); //$NON-NLS-1$  
		   
		  if (methodrefresults.length != methodRefResultModel.length)
				  fail("Entry Result length different from model for methodRef");//$NON-NLS-1$  
	
		  for (int i=0;i<methodrefresults.length; i++)
		  {
			  assertEquals(methodRefResultModel[i],methodrefresults[i].toString());
		  }
	}
	
  public void testExactDeclarations() throws Exception
  {
  	 importFile("a.h","resources/dependency/a.h");//$NON-NLS-1$ //$NON-NLS-2$ 
	 
  	  //Make sure project got added to index
	  IPath testProjectPath = testProject.getFullPath();
	  IIndex ind = sourceIndexer.getIndex(testProjectPath,true,true);
	  assertTrue("Index exists for project",ind != null); //$NON-NLS-1$ 
	  
	  importFile("DepTest3.h","resources/dependency/DepTest3.h");//$NON-NLS-1$ //$NON-NLS-2$ 
	  importFile("DepTest3.cpp","resources/dependency/DepTest3.cpp");//$NON-NLS-1$ //$NON-NLS-2$ 
	  
	  IEntryResult[] eResult = ind.queryEntries(IIndexConstants.CLASS_DECL);
	  IQueryResult[] qResult = ind.queryPrefix(IIndexConstants.CLASS_DECL);
	  
	  assertTrue("Expected 2 files indexed", qResult.length == 2); //$NON-NLS-1$ 
	  assertTrue("Checking DepTest3.h location", qResult[0].getPath().equals("/IndexerTestProject/DepTest3.h")); //$NON-NLS-1$ //$NON-NLS-2$ 
	  assertTrue("Checking a.h location", qResult[1].getPath().equals("/IndexerTestProject/a.h")); //$NON-NLS-1$ //$NON-NLS-2$ 
	  
	  assertTrue("Expect 2 class declaration entries", eResult.length == 2); //$NON-NLS-1$ 
	  
	  int[] DepTest3FileRefs = {2};
	
	  int[] fileRefs = eResult[0].getFileReferences();
	
	  assertTrue("Check DepTest3 File Refs number", fileRefs.length == 1); //$NON-NLS-1$ 
	  
	  for (int i=0; i<fileRefs.length; i++){
	  	assertTrue("Verify DepTest3 File Ref",fileRefs[i] == DepTest3FileRefs[i]); //$NON-NLS-1$ 
	  }
	  
	  int[] aFileRefs = {3};
	
	  fileRefs = eResult[1].getFileReferences();
	 
	  assertTrue("Check a.h File Refs number", fileRefs.length == 1); //$NON-NLS-1$  
	  
	  for (int i=0; i<fileRefs.length; i++){
	  	assertTrue("Verify a.h File Ref",fileRefs[i] == aFileRefs[i]); //$NON-NLS-1$  
	  }
	  
  }
  
  public void testMD5() throws Exception
  {
  	importFile("extramail.cpp","resources/indexer/extramail.cpp"); //$NON-NLS-1$ //$NON-NLS-2$ 
  	//importFile("mail.cpp","resources/indexer/mail.cpp");
  	
	MessageDigest md = MessageDigest.getInstance("MD5"); //$NON-NLS-1$ 
	//MessageDigest md = MessageDigest.getInstance("SHA");
	String fileName = testProject.getFile("extramail.cpp").getLocation().toOSString(); //$NON-NLS-1$ 
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
	  //Add a new file to the project
	  importFile("extramail.cpp","resources/indexer/extramail.cpp"); //$NON-NLS-1$ //$NON-NLS-2$ 
	
	  //Make sure project got added to index
	  IPath testProjectPath = testProject.getFullPath();
	  IIndex ind = sourceIndexer.getIndex(testProjectPath,true,true);
	  assertTrue("Index exists for project",ind != null); //$NON-NLS-1$ 
	
	  IEntryResult[] macroresults = ind.queryEntries(IIndexConstants.MACRO_DECL);
	  assertTrue("Entry exists", macroresults != null); //$NON-NLS-1$ 
	  
	  String [] macroResultModel = {"EntryResult: word=macroDecl/CASE, refs={ 1 }, offsets={ [ 2131] }",
			  "EntryResult: word=macroDecl/MAX, refs={ 1 }, offsets={ [ 2156] }", 
			  "EntryResult: word=macroDecl/PRINT, refs={ 1 }, offsets={ [ 296] }"};
	    
	  if (macroresults.length != macroResultModel.length)
		 fail("Entry Result length different from model for macros"); //$NON-NLS-1$ 

	  for (int i=0;i<macroresults.length; i++)
	  {
		assertEquals(macroResultModel[i],macroresults[i].toString());
	  }
  }
  
  public void testIndexShutdown() throws Exception{
	//Add a new file to the project
	 importFile("reftest.cpp","resources/indexer/reftest.cpp"); //$NON-NLS-1$ //$NON-NLS-2$ 
	 
	 //Make sure project got added to index
	 IPath testProjectPath = testProject.getFullPath();
	 IIndex ind = sourceIndexer.getIndex(testProjectPath,true,true);
	 assertTrue("Index exists for project",ind != null); //$NON-NLS-1$ 
	 
	 //Create an empty index file
	 String badIndexFile = CCorePlugin.getDefault().getStateLocation().append("badIndex.index").toOSString(); //$NON-NLS-1$ 
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
				if(indexesFiles[i].getName().equals("badIndex.index")){ //$NON-NLS-1$ 
					fail("Shutdown did not delete .index file"); //$NON-NLS-1$
				}
		}
	}
  }
  
  public void testForwardDeclarations() throws Exception{
	//Add a new file to the project
	importFile("reftest.cpp","resources/indexer/reftest.cpp"); //$NON-NLS-1$ //$NON-NLS-2$ 
	
	 //Make sure project got added to index
	 IPath testProjectPath = testProject.getFullPath();
	 IIndex ind = sourceIndexer.getIndex(testProjectPath,true,true);
	 assertTrue("Index exists for project",ind != null); //$NON-NLS-1$ 
	//IEntryResult[] fwdDclResults = ind.queryEntries("typeDecl/C/ForwardA/A".toCharArray());
	 IEntryResult[] fwdDclResults = ind.queryEntries("typeDecl/G/ForwardA/A".toCharArray()); //$NON-NLS-1$ 
	 assertTrue("Entry exists",fwdDclResults != null); //$NON-NLS-1$ 
	 
	 String [] fwdDclModel = {"EntryResult: word=typeDecl/G/ForwardA/A, refs={ 1 }, offsets={ [ 225] }"}; //$NON-NLS-1$
	
	 if (fwdDclResults.length != fwdDclModel.length)
		fail("Entry Result length different from model for forward declarations"); //$NON-NLS-1$

	 for (int i=0;i<fwdDclResults.length; i++)
	 {
	   assertEquals(fwdDclModel[i],fwdDclResults[i].toString());
	 }

	IEntryResult[] fwdDclRefResults = ind.queryEntries("typeRef/G/ForwardA/A".toCharArray()); //$NON-NLS-1$ 
	assertTrue("Entry exists", fwdDclRefResults!= null); //$NON-NLS-1$
	
	String [] fwdDclRefModel = {"EntryResult: word=typeRef/G/ForwardA/A, refs={ 1 }, offsets={ [ 225] }"}; //$NON-NLS-1$  

	if (fwdDclRefResults.length != fwdDclRefModel.length)
	   fail("Entry Result length different from model for forward declarations refs"); //$NON-NLS-1$  

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
