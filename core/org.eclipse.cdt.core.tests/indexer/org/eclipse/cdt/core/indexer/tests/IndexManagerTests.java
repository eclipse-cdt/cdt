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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.cdt.internal.core.index.impl.IFileDocument;
import org.eclipse.cdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.sourcedependency.DependencyManager;
import org.eclipse.cdt.internal.core.sourcedependency.DependencyQueryJob;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * @author bgheorgh
 */
public class IndexManagerTests extends TestCase {
	IFile file;
	IFileDocument fileDoc;
	IProject testProject;
	NullProgressMonitor monitor;
    IndexManager indexManager;
    
    public static final int TIMEOUT = 10000;
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
		//Create temp project
		testProject = createProject("IndexerTestProject");
		if (testProject==null)
			fail("Unable to create project");	
			
	
	}
	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() {
		try {
			super.tearDown();
		} catch (Exception e1) {
		}
		//Delete project
		if (testProject.exists()){
			try {
				testProject.delete(true,monitor);
			} catch (ResourceException e) {
			} catch (CoreException e) {
			}
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(IndexManagerTests.class.getName());

		suite.addTest(new IndexManagerTests("testAddNewFileToIndex"));
		suite.addTest(new IndexManagerTests("testRemoveProjectFromIndex"));
		suite.addTest(new IndexManagerTests("testRefs"));
		suite.addTest(new IndexManagerTests("testMacros"));
		suite.addTest(new IndexManagerTests("testForwardDeclarations"));
		//suite.addTest(new IndexManagerTests("testIndexContents"));
		//suite.addTest(new IndexManagerTests("testIndexAll"));
		suite.addTest(new IndexManagerTests("testDependencyTree"));
		suite.addTest(new IndexManagerTests("testIndexShutdown"));

		return suite;
	//	return new TestSuite(IndexManagerTests.class);
	}
	/*
	 * Utils
	 */
	private IProject createProject(String projectName) throws CoreException
	{
	   IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
	   IProject project= root.getProject(projectName);
	   IProject cproject = null;
	   try{
		   if (!project.exists()) {
			 project.create(null);
		   } else {
		     project.refreshLocal(IResource.DEPTH_INFINITE, null);
		   }
		   if (!project.isOpen()) {
			 project.open(null);
		   }  
		  
	       //Fill out a project description
		   IPath defaultPath = Platform.getLocation();
		   IPath newPath = project.getFullPath();
		   if (defaultPath.equals(newPath))
			 newPath = null;
		   IWorkspace workspace = ResourcesPlugin.getWorkspace();
		   IProjectDescription description = workspace.newProjectDescription(project.getName());
		   description.setLocation(newPath);
		   //Create the project
		   cproject = CCorePlugin.getDefault().createCProject(description,project,monitor,CCorePlugin.PLUGIN_ID + ".make"); //.getCoreModel().create(project);
		    
		   if( !cproject.hasNature(CCProjectNature.CC_NATURE_ID) ){
			   addNatureToProject(cproject, CCProjectNature.CC_NATURE_ID, null);
		   }
	   }
	   catch (CoreException e){
		  cproject = project;
		  cproject.open(null);
	   }
	  
	   return cproject;
	   
	}
	
	private IFile importFile(String fileName, String resourceLocation)throws Exception{
	   //Obtain file handle
       file = testProject.getProject().getFile(fileName); 
	   String pluginRoot=org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.core.tests").find(new Path("/")).getFile();
	   //Create file input stream
	   monitor = new NullProgressMonitor();
	   if (!file.exists()){
		 file.create(new FileInputStream(pluginRoot + resourceLocation),false,monitor);
	   }
	   fileDoc = new IFileDocument(file);
	   return file;
	}
	
	private void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
		   IProjectDescription description = proj.getDescription();
		   String[] prevNatures= description.getNatureIds();
		   String[] newNatures= new String[prevNatures.length + 1];
		   System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		   newNatures[prevNatures.length]= natureId;
		   description.setNatureIds(newNatures);
		   proj.setDescription(description, monitor);
	 }
	/*
	 * Start of tests
	 */ 	
	public void testIndexAll() throws Exception {
		//Add a file to the project
		importFile("mail.cpp","resources/indexer/mail.cpp");
		//Enable indexing on the created project
		//By doing this, we force the Index Manager to indexAll()
		indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
		indexManager.setEnabled(testProject,true);
		Thread.sleep(1500);
		IIndex ind = indexManager.getIndex(testProject.getFullPath(),true,true);
		assertTrue("Index exists for project",ind != null);
		
		char[] prefix = "typeDecl/".toCharArray();
		IQueryResult[] qresults = ind.queryPrefix(prefix);
		IEntryResult[] eresults = ind.queryEntries(prefix);
		
		assertTrue("Query Results exist", qresults != null);
		assertTrue("Entry Results exist", eresults != null);
		
		String [] queryResultModel = {"IndexedFile(1: /IndexerTestProject/mail.cpp)"};
		String [] entryResultModel ={"EntryResult: word=typeDecl/C/Mail, refs={ 1 }", "EntryResult: word=typeDecl/C/Unknown, refs={ 1 }", "EntryResult: word=typeDecl/C/container, refs={ 1 }", "EntryResult: word=typeDecl/C/first_class, refs={ 1 }", "EntryResult: word=typeDecl/C/postcard, refs={ 1 }", "EntryResult: word=typeDecl/V/PO_Box, refs={ 1 }", "EntryResult: word=typeDecl/V/x, refs={ 1 }"};
		
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
		importFile("mail.cpp","resources/indexer/mail.cpp");
		//Enable indexing on the created project
		//By doing this, we force the Index Manager to indexAll()
		indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
		indexManager.setEnabled(testProject,true);
		Thread.sleep(TIMEOUT);
		//Make sure project got added to index
		IPath testProjectPath = testProject.getFullPath();
		IIndex ind = indexManager.getIndex(testProjectPath,true,true);
		assertTrue("Index exists for project",ind != null);
		//Add a new file to the project, give it some time to index
		importFile("DocumentManager.h","resources/indexer/DocumentManager.h");
		importFile("DocumentManager.cpp","resources/indexer/DocumentManager.cpp");
		Thread.sleep(10000);
		ind = indexManager.getIndex(testProjectPath,true,true);
		
		char[] prefix = "typeDecl/C/CDocumentManager".toCharArray();
		String [] entryResultModel ={"EntryResult: word=typeDecl/C/CDocumentManager, refs={ 1 }"};
		IEntryResult[] eresults =ind.queryEntries(prefix);
		
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
	  importFile("mail.cpp","resources/indexer/mail.cpp");
	  //Enable indexing on the created project
	  //By doing this, we force the Index Manager to indexAll()
	  indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
	  indexManager.setEnabled(testProject,true);
	  Thread.sleep(TIMEOUT);
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
		try {
			testProject.delete(true,monitor);
		} catch (CoreException e) {
			Thread.sleep(5000);
			testProject.delete(true,monitor);
		}
		
	}

	public void testRemoveFileFromIndex() throws Exception{
	 //Add a file to the project
	 importFile("mail.cpp","resources/indexer/mail.cpp");
	 //Enable indexing on the created project
	 //By doing this, we force the Index Manager to indexAll()
	 indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
	 indexManager.setEnabled(testProject,true);
	 Thread.sleep(TIMEOUT);
	 //Make sure project got added to index
	 IPath testProjectPath = testProject.getFullPath();
	 IIndex ind = indexManager.getIndex(testProjectPath,true,true);
	 assertTrue("Index exists for project",ind != null);
	 //Add a new file to the project
	 importFile("DocumentManager.h","resources/indexer/DocumentManager.h");
	 Thread.sleep(10000);
	 //Do a "before" deletion comparison
	 ind = indexManager.getIndex(testProjectPath,true,true);
	 char[] prefix = "typeDecl/".toCharArray();
	 IEntryResult[] eresults = ind.queryEntries(prefix);
	 assertTrue("Entry result found for typdeDecl/", eresults != null);
	 
	 String [] entryResultBeforeModel ={"EntryResult: word=typeDecl/C/CDocumentManager, refs={ 1 }", "EntryResult: word=typeDecl/C/Mail, refs={ 2 }", "EntryResult: word=typeDecl/C/Unknown, refs={ 2 }", "EntryResult: word=typeDecl/C/container, refs={ 2 }", "EntryResult: word=typeDecl/C/first_class, refs={ 2 }", "EntryResult: word=typeDecl/C/postcard, refs={ 2 }"};
	 if (eresults.length != entryResultBeforeModel.length)
			fail("Entry Result length different from model");	

	 for (int i=0;i<eresults.length; i++)
	 {
		assertEquals(entryResultBeforeModel[i],eresults[i].toString());
	 }
	 //Delete mail.cpp from the project, give some time to remove index
	 IResource resourceHdl = testProject.findMember("mail.cpp") ;
	 resourceHdl.delete(true,monitor);
	 Thread.sleep(10000);
	 //See if the index is still there
	 ind = indexManager.getIndex(testProjectPath,true,true);
	 eresults = ind.queryEntries(prefix);
	 assertTrue("Entry exists", eresults != null);
		
	 String [] entryResultAfterModel ={"EntryResult: word=typeDecl/C/CDocumentManager, refs={ 1 }"};
	 if (eresults.length != entryResultAfterModel.length)
		fail("Entry Result length different from model");
		
	 for (int i=0;i<eresults.length; i++)
     {
		assertEquals(entryResultAfterModel[i],eresults[i].toString());
	 }
	}
	
	public void testIndexContents() throws Exception{
		//Add a new file to the project, give it some time to index
		importFile("extramail.cpp","resources/indexer/extramail.cpp");
		//Enable indexing on the created project
		//By doing this, we force the Index Manager to indexAll()
		indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
		indexManager.setEnabled(testProject,true);
		Thread.sleep(TIMEOUT);
		//Make sure project got added to index
		IPath testProjectPath = testProject.getFullPath();
		IIndex ind = indexManager.getIndex(testProjectPath,true,true);
		assertTrue("Index exists for project",ind != null);
	
		IEntryResult[] typerefreesults = ind.queryEntries(IIndexConstants.TYPE_REF);
		assertTrue("Type Ref Results exist", typerefreesults != null);
		
		String [] typeDeclEntryResultModel ={"EntryResult: word=typeDecl/C/Mail/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/C/Unknown/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/C/container/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/C/first_class/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/C/postcard/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/E/test/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/T/int32, refs={ 1 }", "EntryResult: word=typeDecl/V/PO_Box, refs={ 1 }", "EntryResult: word=typeDecl/V/x, refs={ 1 }", "EntryResult: word=typeDecl/V/x/Z, refs={ 1 }"};
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
				
		String [] fieldResultModel = {"EntryResult: word=fieldDecl/array/container/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/bye/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/cool/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/hi/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/index/container/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/postage/Mail/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/sz/container/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/type/Mail/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/why/Y/X/Z, refs={ 1 }"};
		IEntryResult[] fieldresults =ind.queryEntries(IIndexConstants.FIELD_DECL);
		assertTrue("Field Results exist", fieldresults != null);
		
		if (fieldresults.length != fieldResultModel.length)
				fail("Entry Result length different from model for fieldDecl");
	
		for (int i=0;i<fieldresults.length; i++)
		{
			assertEquals(fieldResultModel[i],fieldresults[i].toString());
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
										//"EntryResult: word=methodDecl/operator=/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/operator=/container/Y/X/Z, refs={ 1 }",
										//"EntryResult: word=methodDecl/operator[]/Y/X/Z, refs={ 1 }",
										"EntryResult: word=methodDecl/operator[]/container/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/postcard/postcard/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/print/Mail/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/print/Unknown/Y/X/Z, refs={ 1 }",
										"EntryResult: word=methodDecl/print/first_class/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/print/postcard/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/size/container/Y/X/Z, refs={ 1 }", 
										"EntryResult: word=methodDecl/~container/container/Y/X/Z, refs={ 1 }" };
									   
									    
									   
	
		IEntryResult[] methodresults =ind.queryEntries(IIndexConstants.METHOD_DECL);
		assertTrue("Entry exists", methodresults != null);
		
		if (methodresults.length != methodResultModel.length)
				fail("Entry Result length different from model for functionDecl");
	
		for (int i=0;i<methodresults.length; i++)
		{
			assertEquals(methodResultModel[i],methodresults[i].toString());
		}
  }
  
  public void testRefs() throws Exception{
		  //Add a new file to the project, give it some time to index
		  importFile("reftest.cpp","resources/indexer/reftest.cpp");
		  //Enable indexing on the created project
		  //By doing this, we force the Index Manager to indexAll()
		  indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
		  indexManager.setEnabled(testProject,true);
		  Thread.sleep(TIMEOUT);
		  //Make sure project got added to index
		  IPath testProjectPath = testProject.getFullPath();
		  IIndex ind = indexManager.getIndex(testProjectPath,true,true);
		  assertTrue("Index exists for project",ind != null);
		  
		  String [] typeRefEntryResultModel ={"EntryResult: word=typeRef/C/C/B/A, refs={ 1 }", "EntryResult: word=typeRef/C/ForwardA/A, refs={ 1 }", "EntryResult: word=typeRef/E/e1/B/A, refs={ 1 }", "EntryResult: word=typeRef/V/x/B/A, refs={ 1 }"};
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
	
  public void testMacros() throws Exception
  {
	  //Add a new file to the project, give it some time to index
	  importFile("extramail.cpp","resources/indexer/extramail.cpp");
	  //Enable indexing on the created project
	  //By doing this, we force the Index Manager to indexAll()
	  indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
	  indexManager.setEnabled(testProject,true);
	  Thread.sleep(TIMEOUT);
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
	 importFile("reftest.cpp","resources/indexer/reftest.cpp");
	 //Enable indexing on the created project
	 //By doing this, we force the Index Manager to indexAll()
	 indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
	 indexManager.setEnabled(testProject,true);
	 Thread.sleep(TIMEOUT);
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
	importFile("reftest.cpp","resources/indexer/reftest.cpp");
	//Enable indexing on the created project
	//By doing this, we force the Index Manager to indexAll()
	indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
	indexManager.setEnabled(testProject,true);
	Thread.sleep(TIMEOUT);
	 //Make sure project got added to index
	 IPath testProjectPath = testProject.getFullPath();
	 IIndex ind = indexManager.getIndex(testProjectPath,true,true);
	 assertTrue("Index exists for project",ind != null);

	 IEntryResult[] fwdDclResults = ind.queryEntries("typeDecl/C/ForwardA/A".toCharArray());
	 assertTrue("Entry exists",fwdDclResults != null);
	 
	 String [] fwdDclModel = {"EntryResult: word=typeDecl/C/ForwardA/A, refs={ 1 }"};
	
	 if (fwdDclResults.length != fwdDclModel.length)
		fail("Entry Result length different from model for forward declarations");

	 for (int i=0;i<fwdDclResults.length; i++)
	 {
	   assertEquals(fwdDclModel[i],fwdDclResults[i].toString());
	 }

	IEntryResult[] fwdDclRefResults = ind.queryEntries("typeRef/C/ForwardA/A".toCharArray());
	assertTrue("Entry exists", fwdDclRefResults!= null);
	
	String [] fwdDclRefModel = {"EntryResult: word=typeRef/C/ForwardA/A, refs={ 1 }"};

	if (fwdDclRefResults.length != fwdDclRefModel.length)
	   fail("Entry Result length different from model for forward declarations refs");

	for (int i=0;i<fwdDclRefResults.length; i++)
	{
	  assertEquals(fwdDclRefModel[i],fwdDclRefResults[i].toString());
	}
  }
  
  public void testFunctionDeclarations2() throws Exception{
  	
  }
  
  
  public void testDependencyTree() throws Exception{
	//Add a file to the project
	IFile depTest = importFile("DepTest.cpp","resources/dependency/DepTest.cpp");
	importFile("DepTest.h","resources/dependency/DepTest.h");
	importFile("a.h","resources/dependency/a.h");
	importFile("c.h","resources/dependency/c.h");
	importFile("d.h","resources/dependency/d.h");
	importFile("Inc1.h","resources/dependency/Inc1.h");
	importFile("DepTest2.h","resources/dependency/DepTest2.h");
	IFile depTest2 = importFile("DepTest2.cpp","resources/dependency/DepTest2.cpp");
	//Enable indexing on the created project
	//By doing this, we force the Dependency Manager to do a g()
	DependencyManager dependencyManager = CCorePlugin.getDefault().getCoreModel().getDependencyManager();
	dependencyManager.setEnabled(testProject,true);
	Thread.sleep(10000);
	String[] depTestModel = {File.separator + "IndexerTestProject" + File.separator + "d.h", File.separator + "IndexerTestProject" + File.separator + "Inc1.h", File.separator + "IndexerTestProject" + File.separator + "c.h", File.separator + "IndexerTestProject" + File.separator + "a.h", File.separator + "IndexerTestProject" + File.separator + "DepTest.h"};
	String[] depTest2Model = {File.separator + "IndexerTestProject" + File.separator + "d.h", File.separator + "IndexerTestProject" + File.separator + "DepTest2.h"};
	
	ArrayList includes = new ArrayList();
	dependencyManager.performConcurrentJob(new DependencyQueryJob(testProject,depTest,dependencyManager,includes),ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,null);
	//Thread.sleep(5000);
	String[] depTestModelLocal = convertToLocalPath(depTestModel);
	String[] depTestIncludes = new String[includes.size()];
	Iterator includesIterator = includes.iterator();
	int i=0;
	while(includesIterator.hasNext()){
		depTestIncludes[i] = (String) includesIterator.next();
		i++;
	}
	
	if (depTestModelLocal.length != depTestIncludes.length)
			fail("Number of included files differsfrom model");
	
	Arrays.sort(depTestModelLocal);
	Arrays.sort(depTestIncludes);
		
	for (i=0;i<depTestIncludes.length; i++)
	{
		assertEquals(depTestModelLocal[i],depTestIncludes[i]);
	}
	
	ArrayList includes2 = new ArrayList();
	dependencyManager.performConcurrentJob(new DependencyQueryJob(testProject,depTest2,dependencyManager,includes2),ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,null);
	//Thread.sleep(5000);
	String[] depTest2ModelLocal = convertToLocalPath(depTest2Model);
	String[] depTest2Includes = new String[includes2.size()];
	Iterator includes2Iterator = includes2.iterator();
	i=0;
	while(includes2Iterator.hasNext()){
		depTest2Includes[i] = (String) includes2Iterator.next();
		i++;
	}
	
	if (depTest2ModelLocal.length != depTest2Includes.length)
			fail("Number of included files differsfrom model");
	
	Arrays.sort(depTest2ModelLocal);
	Arrays.sort(depTest2Includes);
	
	for (i=0;i<depTest2Includes.length; i++)
	{
		assertEquals(depTest2ModelLocal[i],depTest2Includes[i]);
	}
  }

	/**
	 * @param depTestModel
	 * @return
	 */
	private String[] convertToLocalPath(String[] model) {
		IPath defaultPath = Platform.getLocation();
		String[] tempLocalArray = new String[model.length];
		for (int i=0;i<model.length;i++){
			StringBuffer buffer = new StringBuffer();
			buffer.append(defaultPath.toOSString());
			buffer.append(model[i]);
			tempLocalArray[i]=buffer.toString();
		}
		return tempLocalArray;
	}
}
