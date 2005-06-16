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
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorOperation;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.index.IIndexDelta;
import org.eclipse.cdt.core.index.IndexChangeEvent;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.tests.FailingTest;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.cdt.internal.core.index.domsourceindexer.DOMSourceIndexer;
import org.eclipse.cdt.internal.core.index.domsourceindexer.DOMSourceIndexerRunner;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author bgheorgh
 */
public class DOMSourceIndexerTests extends TestCase implements IIndexChangeListener  {
	IFile 					file;
	IProject 				testProject;
	NullProgressMonitor		monitor;
	IndexManager 			indexManager;
	DOMSourceIndexer			sourceIndexer;
	boolean					fileIndexed;
	
	static final String sourceIndexerID = "org.eclipse.cdt.core.originalsourceindexer"; //$NON-NLS-1$
	public static final int TIMEOUT = 50;
	/**
	 * Constructor for IndexManagerTest.
	 * @param name
	 */
	public DOMSourceIndexerTests(String name) {
		super(name);
	}

	public void resetIndexState() {
		fileIndexed = false;
	}
	
	 public void resetIndexer(final String indexerId){
		if ( testProject  != null) {
			ICDescriptorOperation op = new ICDescriptorOperation() {

				public void execute(ICDescriptor descriptor, IProgressMonitor monitor) throws CoreException {
						descriptor.remove(CCorePlugin.INDEXER_UNIQ_ID);
						descriptor.create(CCorePlugin.INDEXER_UNIQ_ID,indexerId);
				}
			};
			try {
				CCorePlugin.getDefault().getCDescriptorManager().runDescriptorOperation(testProject, op, new NullProgressMonitor());
				CCorePlugin.getDefault().getCoreModel().getIndexManager().indexerChangeNotification(testProject);
			} catch (CoreException e) {}
		}
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
	
		if (testProject==null)
			fail("Unable to create project");	 //$NON-NLS-1$
		
		resetIndexer(CCorePlugin.DEFAULT_INDEXER_UNIQ_ID);
	    //The DOM Source Indexer checks to see if a file has any scanner info
	    //set prior to indexing it in order to increase efficiency. We need to let it know
	    //that it is running in test mode in order to allow for this scanner info test to be skipped
	    DOMSourceIndexerRunner.setSkipScannerInfoTest(true);
		  
	    
		indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
		//indexManager.reset();
		//Get the indexer used for the test project
		sourceIndexer = (DOMSourceIndexer) indexManager.getIndexerForProject(testProject);
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
		TestSuite suite = new TestSuite(DOMSourceIndexerTests.class.getName());

		suite.addTest(new DOMSourceIndexerTests("testAddNewFileToIndex")); //$NON-NLS-1$
		suite.addTest(new DOMSourceIndexerTests("testForwardDeclarations")); //$NON-NLS-1$
		suite.addTest(new DOMSourceIndexerTests("testIndexAll")); //$NON-NLS-1$
		suite.addTest(new FailingTest(new DOMSourceIndexerTests("testIndexContents"))); //$NON-NLS-1$
		suite.addTest(new DOMSourceIndexerTests("testMacros")); //$NON-NLS-1$
		suite.addTest(new FailingTest(new DOMSourceIndexerTests("testRefs"))); //$NON-NLS-1$
		suite.addTest(new DOMSourceIndexerTests("testExactDeclarations")); //$NON-NLS-1$
		suite.addTest(new FailingTest(new DOMSourceIndexerTests("testRemoveFileFromIndex"))); //$NON-NLS-1$
		suite.addTest(new DOMSourceIndexerTests("testRemoveProjectFromIndex")); //$NON-NLS-1$
		suite.addTest(new DOMSourceIndexerTests("testIndexShutdown")); //$NON-NLS-1$
	
	
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
		
		IQueryResult[] qresults = ind.getPrefix(IIndex.TYPE, IIndex.ANY, IIndex.DEFINITION);
		IEntryResult[] eresults = ind.getEntries(IIndex.TYPE, IIndex.ANY, IIndex.DEFINITION);
		IEntryResult[] eresultsDecls = ind.getEntries(IIndex.TYPE, IIndex.ANY, IIndex.DECLARATION);
		
		assertTrue("Query Results exist", qresults != null); //$NON-NLS-1$
		assertTrue("Entry Results exist", eresults != null); //$NON-NLS-1$
		
		String [] queryResultModel = {"IndexedFile(1: /IndexerTestProject/mail.cpp)"}; //$NON-NLS-1$
		String [] entryResultModel ={"EntryResult: word=typeDefn/C/Mail, refs={ 1 }, offsets={ [ 294] }",  //$NON-NLS-1$
				"EntryResult: word=typeDefn/C/Unknown, refs={ 1 }, offsets={ [ 2738] }",  //$NON-NLS-1$
				"EntryResult: word=typeDefn/C/container, refs={ 1 }, offsets={ [ 21084] }",  //$NON-NLS-1$
				"EntryResult: word=typeDefn/C/first_class, refs={ 1 }, offsets={ [ 2506] }",  //$NON-NLS-1$
				"EntryResult: word=typeDefn/C/postcard, refs={ 1 }, offsets={ [ 2298] }",  //$NON-NLS-1$
		};
		
		String [] entryResultDeclsModel = {
				"EntryResult: word=typeDecl/D/Mail, refs={ 1 }, offsets={ [ 294] }",  //$NON-NLS-1$
				"EntryResult: word=typeDecl/D/first_class, refs={ 1 }, offsets={ [ 2506] }",  //$NON-NLS-1$ 
				"EntryResult: word=typeDecl/D/postcard, refs={ 1 }, offsets={ [ 2298] }",  //$NON-NLS-1$
		};
		String[] entryResultNameModel = {"Mail","Unknown","container","first_class","postcard"};
		int[] entryResultMetaModel = {IIndex.TYPE};
		int[] entryResultTypeModel = {IIndex.TYPE_CLASS,IIndex.TYPE_CLASS,IIndex.TYPE_CLASS,IIndex.TYPE_CLASS,IIndex.TYPE_CLASS};
		int[] entryResultRefModel = {IIndex.DEFINITION};
		
		if (qresults.length != queryResultModel.length)
			fail("Query Result length different from model"); //$NON-NLS-1$

		if (eresults.length != entryResultModel.length)
			fail("Entry Result length different from model"); //$NON-NLS-1$

		if (eresultsDecls.length != entryResultDeclsModel.length)
			fail("Entry Result length different from model"); //$NON-NLS-1$
			
		for (int i=0; i<qresults.length;i++)
		{
			assertEquals(queryResultModel[i],qresults[i].toString());
		}
	
		for (int i=0;i<eresults.length; i++)
		{
			assertEquals(entryResultNameModel[i],eresults[i].getName());
			assertEquals(entryResultMetaModel[0],eresults[i].getMetaKind());
			assertEquals(entryResultTypeModel[i],eresults[i].getKind());
			assertEquals(entryResultRefModel[0],eresults[i].getRefKind());
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
		
		String name = "CDocumentManager"; //$NON-NLS-1$
		
		String[] entryResultNameModel = {"CDocumentManager"};
		int[] entryResultMetaModel = {IIndex.TYPE};
		int[] entryResultTypeModel = {IIndex.TYPE_CLASS};
		int[] entryResultRefModel = {IIndex.DEFINITION};
		
		IEntryResult[] eresults =ind.getEntries(IIndex.TYPE, IIndex.TYPE_CLASS, IIndex.DEFINITION, name);
		IEntryResult[] bogRe = ind.getEntries(IIndex.TYPE, IIndex.ANY, IIndex.DEFINITION);
		assertTrue("Entry Result exists", eresults != null); //$NON-NLS-1$
		
		if (eresults.length != entryResultNameModel.length)
			fail("Entry Result length different from model"); //$NON-NLS-1$

		for (int i=0;i<eresults.length; i++)
		{
			assertEquals(entryResultNameModel[i],eresults[i].getName());
			assertEquals(entryResultMetaModel[i],eresults[i].getMetaKind());
			assertEquals(entryResultTypeModel[i],eresults[i].getKind());
			assertEquals(entryResultRefModel[i],eresults[i].getRefKind());
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
	 IEntryResult[] eresults = ind.getEntries(IIndex.TYPE, IIndex.ANY, IIndex.DEFINITION);
	 IEntryResult[] eresultsDecls = ind.getEntries(IIndex.TYPE, IIndex.ANY, IIndex.DECLARATION);
	 
	 assertTrue("Entry result found for typdeDefn/", eresults != null); //$NON-NLS-1$
	 
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
	 
	String[] entryResultNameModel = {"CDocumentManager","Mail","Unknown","container","first_class","postcard","PO_Box", "size","temp","x"};
	int[] entryResultMetaModel = {IIndex.TYPE};
	int[] entryResultTypeModel = {IIndex.TYPE_CLASS, IIndex.TYPE_CLASS,IIndex.TYPE_CLASS,IIndex.TYPE_CLASS,IIndex.TYPE_CLASS,IIndex.TYPE_CLASS, IIndex.TYPE, IIndex.TYPE, IIndex.VAR, IIndex.VAR};
	int[] entryResultRefModel = {IIndex.DEFINITION};
		
	 if (eresults.length != entryResultNameModel.length)
			fail("Entry Result length different from model"); //$NON-NLS-1$	 

	 for (int i=0;i<eresults.length; i++)
	 {
			assertEquals(entryResultNameModel[i],eresults[i].getName());
			assertEquals(entryResultMetaModel[0],eresults[i].getMetaKind());
			assertEquals(entryResultTypeModel[i],eresults[i].getKind());
			assertEquals(entryResultRefModel[0],eresults[i].getRefKind());
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
	 eresults = ind.getEntries(IIndex.TYPE, IIndex.ANY, IIndex.DEFINITION);
	 assertTrue("Entry exists", eresults != null);  //$NON-NLS-1$ 
		
	 String [] entryResultAfterModel ={"EntryResult: word=typeDecl/C/CDocumentManager, refs={ 2 }, offsets={ [ 2127] }"};  //$NON-NLS-1$ //$NON-NLS-2$ 
	 
	String[] entryResultANameModel = {"CDocumentManager"};
	int[] entryResultAMetaModel = {IIndex.TYPE};
	int[] entryResultATypeModel = {IIndex.TYPE_CLASS};
	int[] entryResultARefModel = {IIndex.DEFINITION};
		
	 if (eresults.length != entryResultANameModel.length)
		fail("Entry Result length different from model");  //$NON-NLS-1$
		
	 for (int i=0;i<eresults.length; i++)
	 {
			assertEquals(entryResultANameModel[i],eresults[i].getName());
			assertEquals(entryResultAMetaModel[0],eresults[i].getMetaKind());
			assertEquals(entryResultATypeModel[0],eresults[i].getKind());
			assertEquals(entryResultARefModel[0],eresults[i].getRefKind());
	 }
	}
	
	public void testIndexContents() throws Exception{
		 
		//Add a new file to the project
		importFile("extramail.cpp","resources/indexer/extramail.cpp");  //$NON-NLS-1$ //$NON-NLS-2$ 
		
		//Make sure project got added to index
		IPath testProjectPath = testProject.getFullPath();
		IIndex ind = sourceIndexer.getIndex(testProjectPath,true,true);
		assertTrue("Index exists for project",ind != null);  //$NON-NLS-1$ //$NON-NLS-2$ 
	
		IEntryResult[] typerefreesults = ind.getEntries(IIndex.TYPE, IIndex.ANY, IIndex.REFERENCE);
		assertTrue("Type Ref Results exist", typerefreesults != null);  //$NON-NLS-1$
		
		String[] entryResultDeclNameModel = {"Mail/Y/X/Z","first_class/Y/X/Z", "postcard/Y/X/Z", "test/Y/X/Z", "int32", "index", "mail",};
		String[] entryResultDefnNameModel = {"Mail/Y/X/Z", "Unknown/Y/X/Z", "container/Y/X/Z", "first_class/Y/X/Z", "postcard/Y/X/Z", "PO_Box", "size", "temp", "x", "x/Z" };
		int[] entryResultMetaModel = {IIndex.TYPE};
		int[] entryResultDefnTypeModel = {IIndex.TYPE_CLASS, IIndex.TYPE_CLASS, IIndex.TYPE_CLASS, IIndex.TYPE_CLASS, IIndex.TYPE_CLASS,
				 					  IIndex.VAR,IIndex.VAR,IIndex.VAR,IIndex.VAR,IIndex.VAR};
		int[] entryResultDeclTypeModel = {IIndex.TYPE_DERIVED, IIndex.TYPE_DERIVED, IIndex.TYPE_DERIVED, IIndex.TYPE_ENUM, IIndex.TYPE_TYPEDEF,IIndex.VAR,IIndex.VAR}; 
		
		
		IEntryResult[] typedeclresults =ind.getEntries(IIndex.TYPE, IIndex.ANY, IIndex.DECLARATION);
		IEntryResult[] typedefinitionsresults = ind.getEntries(IIndex.TYPE, IIndex.ANY , IIndex.DEFINITION);
		assertTrue("Type Decl Results exist", typedeclresults != null);  //$NON-NLS-1$ 
		
		if (typedefinitionsresults.length != entryResultDefnNameModel.length)
			fail("Entry Result length different from model for typeDefn");  //$NON-NLS-1$
	
		for (int i=0;i<typedeclresults.length; i++)
		{
			assertEquals(entryResultDefnNameModel[i],typedefinitionsresults[i].getName());
			assertEquals(entryResultMetaModel[0],typedefinitionsresults[i].getMetaKind());
			assertEquals(entryResultDefnTypeModel[i],typedefinitionsresults[i].getKind());
			assertEquals(IIndex.DEFINITION,typedefinitionsresults[i].getRefKind());
		}
	
		String[] entryResultTNameModel = {"int32" };
		int[] entryResultTMetaModel = {IIndex.TYPE};
		int[] entryResultTTypeModel = {IIndex.TYPE_TYPEDEF};
		int[] entryResultTRefModel = {IIndex.DECLARATION};
		
		
		IEntryResult[] typedefresults =ind.getEntries(IIndex.TYPE, IIndex.TYPE_TYPEDEF, IIndex.DECLARATION);
		assertTrue("Type Def Results exist", typedefresults != null);  //$NON-NLS-1$  
		
		if (typedefresults.length != entryResultTNameModel.length)
					fail("Entry Result length different from model for typeDef");  //$NON-NLS-1$  
	
		for (int i=0;i<typedefresults.length; i++)
		{
			assertEquals(entryResultTNameModel[i],typedefresults[i].getName());
			assertEquals(entryResultTMetaModel[i],typedefresults[i].getMetaKind());
			assertEquals(entryResultTTypeModel[i],typedefresults[i].getKind());
			assertEquals(entryResultTRefModel[i],typedefresults[i].getRefKind());
		}
	
		String[] entryResultNNameModel = {"X/Z", "Y/X/Z" , "Z" };
		int[] entryResultNMetaModel = {IIndex.NAMESPACE};
		int[] entryResultNRefModel = {IIndex.DEFINITION};
		
		IEntryResult[] namespaceresults =ind.getEntries(IIndex.NAMESPACE, IIndex.ANY, IIndex.DEFINITION);
		assertTrue("Namespace Results exist", namespaceresults != null);  //$NON-NLS-1$  
		
		if (namespaceresults.length != entryResultNNameModel.length)
				fail("Entry Result length different from model for namespace");  //$NON-NLS-1$
	
		for (int i=0;i<namespaceresults.length; i++)
		{
			assertEquals(entryResultNNameModel[i],namespaceresults[i].getName());
			assertEquals(entryResultNMetaModel[0],namespaceresults[i].getMetaKind());
			assertEquals(entryResultNRefModel[0],namespaceresults[i].getRefKind());
		}
				
		String[] entryResultFNameModel = {"array/container/Y/X/Z", "index/container/Y/X/Z" , "postage/Mail/Y/X/Z","sz/container/Y/X/Z", "type/Mail/Y/X/Z"};
		int[] entryResultFMetaModel = {IIndex.FIELD};
		int[] entryResultFRefModel = {IIndex.DEFINITION};
		
		IEntryResult[] fieldresults =ind.getEntries(IIndex.FIELD, IIndex.ANY, IIndex.DEFINITION);
		assertTrue("Field Results exist", fieldresults != null);  //$NON-NLS-1$
		
		if (fieldresults.length != entryResultFNameModel.length)
				fail("Entry Result length different from model for fieldDecl");  //$NON-NLS-1$ 
	
		for (int i=0;i<fieldresults.length; i++)
		{
			assertEquals(entryResultFNameModel[i],fieldresults[i].getName());
			assertEquals(entryResultFMetaModel[0],fieldresults[i].getMetaKind());
			assertEquals(entryResultFRefModel[0],fieldresults[i].getRefKind());
		}
		
		String[] entryResultENameModel = {"bye/Y/X/Z", "cool/Y/X/Z" , "hi/Y/X/Z", "why/Y/X/Z"};
		int[] entryResultEMetaModel = {IIndex.ENUMTOR};
		int[] entryResultERefModel = {IIndex.DECLARATION};
		
		
		IEntryResult[] enumeratorresults =ind.getEntries(IIndex.ENUMTOR, IIndex.ANY, IIndex.DECLARATION);
		assertTrue("Enumerator Results exist", enumeratorresults != null);  //$NON-NLS-1$ 
		
		if (enumeratorresults.length != entryResultENameModel.length)
				fail("Entry Result length different from model for enumtorDecl");  //$NON-NLS-1$ 
	
		for (int i=0;i<enumeratorresults.length; i++)
		{
			assertEquals(entryResultENameModel[i],enumeratorresults[i].getName());
			assertEquals(entryResultEMetaModel[0],enumeratorresults[i].getMetaKind());
			assertEquals(entryResultERefModel[0],enumeratorresults[i].getRefKind());
		}
	
		String[] entryResultFNNameModel = {"doSomething"};
		int[] entryResultFNMetaModel = {IIndex.FUNCTION};
		int[] entryResultFNRefModel = {IIndex.DECLARATION};
		
		IEntryResult[] functionresults =ind.getEntries(IIndex.FUNCTION, IIndex.ANY, IIndex.DECLARATION);
		
		if (functionresults.length != entryResultFNNameModel.length)
					fail("Entry Result length different from model for functionDecl");  //$NON-NLS-1$ 

		for (int i=0;i<functionresults.length; i++)
		{
			assertEquals(entryResultFNNameModel[i],functionresults[i].getName());
			assertEquals(entryResultFNMetaModel[0],functionresults[i].getMetaKind());
			assertEquals(entryResultFNRefModel[0],functionresults[i].getRefKind());
		}
		
									   							   
		String[] entryResultMNameDefnModel = {"Mail/Mail/Y/X/Z", "Unknown/Unknown/Y/X/Z" , "container/container/Y/X/Z", "first_class/first_class/Y/X/Z",
				"print/Unknown/Y/X/Z","print/first_class/Y/X/Z", "print/postcard/Y/X/Z", "size/container/Y/X/Z", "~container/container/Y/X/Z"};
		int[] entryResultMMetaDefnModel = {IIndex.METHOD};
		int[] entryResultMRefDefnModel = {IIndex.DEFINITION};
		
		
		String [] entryResultMNameDeclModel = {"operator =/container/Y/X/Z", "operator []/container/Y/X/Z","print/Mail/Y/X/Z"};
		int[] entryResultMMetaDeclModel = {IIndex.METHOD};
		int[] entryResultMRefDeclModel = {IIndex.DECLARATION};
		
		IEntryResult[] methodresults =ind.getEntries(IIndex.METHOD, IIndex.ANY, IIndex.DECLARATION);
		assertTrue("Entry exists", methodresults != null);  //$NON-NLS-1$ 
		
		if (methodresults.length != entryResultMNameDeclModel.length)
				fail("Entry Result length different from model for functionDecl");  //$NON-NLS-1$  
	
		for (int i=0;i<methodresults.length; i++)
		{
			assertEquals(entryResultMNameDeclModel[i],methodresults[i].getName());
			assertEquals(entryResultMMetaDeclModel[0],methodresults[i].getMetaKind());
			assertEquals(entryResultMRefDeclModel[0],methodresults[i].getRefKind());
		}
	
  }
  
  public void testRefs() throws Exception{
		  //Add a new file to the project
		  importFile("reftest.cpp","resources/indexer/reftest.cpp");  //$NON-NLS-1$ //$NON-NLS-2$ 
		  
		  //Make sure project got added to index
		  IPath testProjectPath = testProject.getFullPath();
		  IIndex ind = sourceIndexer.getIndex(testProjectPath,true,true);
		  assertTrue("Index exists for project",ind != null);  //$NON-NLS-1$ 

		 String[] entryResultNameModel = {"C/B/A","ForwardA/A", "e1/B/A",  "x/B/A"};
		 int[] entryResultMetaModel = {IIndex.TYPE};
		 int[] entryResultTypeModel = {IIndex.TYPE_CLASS, IIndex.TYPE_CLASS, IIndex.TYPE_ENUM, IIndex.VAR};
		 int[] entryResultRefModel = {IIndex.REFERENCE};
		
			
		  IEntryResult[] typerefresults = ind.getEntries(IIndex.TYPE,IIndex.ANY,IIndex.REFERENCE);
		  assertTrue("Entry exists",typerefresults != null); //$NON-NLS-1$ 
		  
		  if (typerefresults.length != entryResultNameModel.length)
			  fail("Entry Result length different from model for typeRef"); //$NON-NLS-1$ 
	
		  for (int i=0;i<typerefresults.length; i++)
		  {
			  assertEquals(entryResultNameModel[i],typerefresults[i].getName());
			  assertEquals(entryResultMetaModel[0],typerefresults[i].getMetaKind());
			  assertEquals(entryResultTypeModel[i],typerefresults[i].getKind());
			  assertEquals(entryResultRefModel[0],typerefresults[i].getRefKind());
		  }
	
		 
		  String[] entryResultFNameModel = {"something/A"};
		  int[] entryResultFMetaModel = {IIndex.FUNCTION};
		  int[] entryResultFRefModel = {IIndex.REFERENCE};
			
			 
		  IEntryResult[] funRefresults = ind.getEntries(IIndex.FUNCTION,IIndex.ANY, IIndex.REFERENCE);
		  assertTrue("Entry exists",funRefresults != null); //$NON-NLS-1$ 
		  
		  if (funRefresults.length != entryResultFNameModel.length)
					  fail("Entry Result length different from model for funcRef"); //$NON-NLS-1$
	
		  for (int i=0;i<funRefresults.length; i++)
		  {
			  assertEquals(entryResultFNameModel[i],funRefresults[i].getName());
			  assertEquals(entryResultFMetaModel[0],funRefresults[i].getMetaKind());
			  assertEquals(entryResultFRefModel[0],funRefresults[i].getRefKind());
		  }
				
		  String [] namespaceRefResultModel = {"EntryResult: word=namespaceRef/A, refs={ 1 }, offsets={ [ 2228, 2241, 2257, 2273, 2292, 2313, 2334] }", 
				  "EntryResult: word=namespaceRef/B/A, refs={ 1 }, offsets={ [ 2231, 2244, 2260, 2276, 2295, 2337] }"}; //$NON-NLS-1$ //$NON-NLS-2$ 
		  
		  
		  String[] entryResultNNameModel = {"A", "B/A"};
		  int[] entryResultNMetaModel = {IIndex.NAMESPACE};
		  int[] entryResultNRefModel = {IIndex.REFERENCE};
		  
		  IEntryResult[] namespacerefresults = ind.getEntries(IIndex.NAMESPACE, IIndex.ANY, IIndex.REFERENCE);
		  assertTrue("Entry exists",namespacerefresults!=null); //$NON-NLS-1$ 
		  
		  if (namespacerefresults.length != entryResultNNameModel.length)
				  fail("Entry Result length different from model for namespaceRef"); //$NON-NLS-1$
	
		  for (int i=0;i<namespacerefresults.length; i++)
		  {
			  assertEquals(entryResultNNameModel[i],namespacerefresults[i].getName());
			  assertEquals(entryResultNMetaModel[0],namespacerefresults[i].getMetaKind());
			  assertEquals(entryResultNRefModel[0],namespacerefresults[i].getRefKind());
		  }
				
		  String[] entryResultFDNameModel = {"y/C/B/A"};
		  int[] entryResultFDMetaModel = {IIndex.FIELD};
		  int[] entryResultFDRefModel = {IIndex.REFERENCE};
		  
		  IEntryResult[] fieldrefresults = ind.getEntries(IIndex.FIELD, IIndex.ANY, IIndex.REFERENCE);
		  assertTrue("Entry exists",fieldrefresults!=null); //$NON-NLS-1$ 
		  
		  if (fieldrefresults.length != entryResultFDNameModel.length)
				  fail("Entry Result length different from model for fieldRef"); //$NON-NLS-1$  
	
		  for (int i=0;i<fieldrefresults.length; i++)
		  {
			  assertEquals(entryResultFDNameModel[i],fieldrefresults[i].getName());
			  assertEquals(entryResultFDMetaModel[0],fieldrefresults[i].getMetaKind());
			  assertEquals(entryResultFDRefModel[0],fieldrefresults[i].getRefKind());
		  }
		  
		  String[] entryResultMNameModel = {"bar/C/B/A"};
		  int[] entryResultMMetaModel = {IIndex.METHOD};
		  int[] entryResultMRefModel = {IIndex.REFERENCE};
		  
		  IEntryResult[] methodrefresults = ind.getEntries(IIndex.METHOD, IIndex.ANY, IIndex.REFERENCE);
		  assertTrue("Entry exists", methodrefresults != null); //$NON-NLS-1$  
		   
		  if (methodrefresults.length != entryResultMNameModel.length)
				  fail("Entry Result length different from model for methodRef");//$NON-NLS-1$  
	
		  for (int i=0;i<methodrefresults.length; i++)
		  {
			  assertEquals(entryResultMNameModel[i],methodrefresults[i].getName());
			  assertEquals(entryResultMMetaModel[0],methodrefresults[i].getMetaKind());
			  assertEquals(entryResultMRefModel[0],methodrefresults[i].getRefKind());
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
	  
	  IEntryResult[] eResult = ind.getEntries(IIndex.TYPE, IIndex.TYPE_CLASS, IIndex.DEFINITION);
	  IQueryResult[] qResult = ind.getPrefix(IIndex.TYPE, IIndex.TYPE_CLASS, IIndex.DEFINITION);
	  
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
	
	  IEntryResult[] macroresults = ind.getEntries(IIndex.MACRO, IIndex.ANY, IIndex.DECLARATION);
	  assertTrue("Entry exists", macroresults != null); //$NON-NLS-1$ 

	  String[] entryResultNameModel = {"CASE", "MAX", "PRINT"};
	  int[] entryResultMetaModel = {IIndex.MACRO};
	  int[] entryResultRefModel = {IIndex.DECLARATION};
		
	  if (macroresults.length != entryResultNameModel.length)
		 fail("Entry Result length different from model for macros"); //$NON-NLS-1$ 

	  for (int i=0;i<macroresults.length; i++)
	  {
		  assertEquals(entryResultNameModel[i],macroresults[i].getName());
		  assertEquals(entryResultMetaModel[0],macroresults[i].getMetaKind());
		  assertEquals(entryResultRefModel[0],macroresults[i].getRefKind());
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
	 IEntryResult[] fwdDclResults = ind.getEntries(IIndex.TYPE, IIndex.TYPE_CLASS, IIndex.DECLARATION, "ForwardA/A" ); //$NON-NLS-1$ 
	 assertTrue("Entry exists",fwdDclResults != null); //$NON-NLS-1$ 
	 
	 String [] fwdDclModel = {"EntryResult: word=typeDecl/G/ForwardA/A, refs={ 1 }, offsets={ [ 225] }"}; //$NON-NLS-1$
	 String[] entryResultNameModel = {"ForwardA/A"};
	 int[] entryResultMetaModel = {IIndex.TYPE};
	 int[] entryResultTypeModel = {IIndex.TYPE_CLASS};
	 int[] entryResultRefModel = {IIndex.DECLARATION};
		
	 if (fwdDclResults.length != fwdDclModel.length)
		fail("Entry Result length different from model for forward declarations"); //$NON-NLS-1$

	 for (int i=0;i<fwdDclResults.length; i++)
	 {
			assertEquals(entryResultNameModel[i],fwdDclResults[i].getName());
			assertEquals(entryResultMetaModel[i],fwdDclResults[i].getMetaKind());
			assertEquals(entryResultTypeModel[i],fwdDclResults[i].getKind());
			assertEquals(entryResultRefModel[i],fwdDclResults[i].getRefKind());
	 }

	IEntryResult[] fwdDclRefResults = ind.getEntries(IIndex.TYPE, IIndex.TYPE_CLASS, IIndex.REFERENCE, "ForwardA/A"); //$NON-NLS-1$ 
	assertTrue("Entry exists", fwdDclRefResults!= null); //$NON-NLS-1$
	
	String [] fwdDclRefModel = {"EntryResult: word=typeRef/G/ForwardA/A, refs={ 1 }, offsets={ [ 237] }"}; //$NON-NLS-1$  
	String[] entryResultName2Model = {"ForwardA/A"};
	int[] entryResultMeta2Model = {IIndex.TYPE};
	int[] entryResultType2Model = {IIndex.TYPE_CLASS};
	int[] entryResultRef2Model = {IIndex.REFERENCE};
	 
	if (fwdDclRefResults.length != fwdDclRefModel.length)
	   fail("Entry Result length different from model for forward declarations refs"); //$NON-NLS-1$  

	for (int i=0;i<fwdDclRefResults.length; i++)
	{
		assertEquals(entryResultName2Model[i],fwdDclRefResults[i].getName());
		assertEquals(entryResultMeta2Model[i],fwdDclRefResults[i].getMetaKind());
		assertEquals(entryResultType2Model[i],fwdDclRefResults[i].getKind());
		assertEquals(entryResultRef2Model[i],fwdDclRefResults[i].getRefKind());
	}
  }

public void indexChanged(IndexChangeEvent event) {
	IIndexDelta delta = event.getDelta();
	if (delta.getDeltaType() == IIndexDelta.MERGE_DELTA){
		fileIndexed = true;
	}
}

}
