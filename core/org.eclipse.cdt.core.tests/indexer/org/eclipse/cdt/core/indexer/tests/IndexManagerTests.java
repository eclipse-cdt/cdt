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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
    public static final int TIMEOUT = 1500;
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
	protected void tearDown() throws Exception {
		super.tearDown();
		//Delete project
		if (testProject.exists()){
			testProject.delete(true,monitor);
		}
	}

	public static Test suite() {
		//TestSuite suite = new TestSuite();
		//suite.addTest(new IndexManagerTests("testDependencyTree"));
		//return suite;
		return new TestSuite(IndexManagerTests.class);
	}
	/*
	 * Utils
	 */
	private IProject createProject(String projectName) throws CoreException
	{
	   IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
	   IProject project= root.getProject(projectName);
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
	   IProject cproject = CCorePlugin.getDefault().createCProject(description,project,monitor,CCorePlugin.PLUGIN_ID + ".make"); //.getCoreModel().create(project);
	    
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
		char[] prefix = "typeDecl/".toCharArray();
		IQueryResult[] qresults = ind.queryPrefix(prefix);
		IEntryResult[] eresults = ind.queryEntries(prefix);
		String [] queryResultModel = {"IndexedFile(1: /IndexerTestProject/mail.cpp)"};
		String [] entryResultModel ={"EntryResult: word=typeDecl/C/Mail, refs={ 1 }", "EntryResult: word=typeDecl/C/Unknown, refs={ 1 }", "EntryResult: word=typeDecl/C/container, refs={ 1 }", "EntryResult: word=typeDecl/C/first_class, refs={ 1 }", "EntryResult: word=typeDecl/C/postcard, refs={ 1 }"};
		
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
		Thread.sleep(10000);
		ind = indexManager.getIndex(testProjectPath,true,true);
		char[] prefix = "typeDecl/C/CDocumentManager".toCharArray();
		String [] entryResultModel ={"EntryResult: word=typeDecl/C/CDocumentManager, refs={ 1 }"};
		IEntryResult[] eresults =ind.queryEntries(prefix);
		
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
	  testProject.delete(true,monitor);
	  //See if the index is still there
	  ind = indexManager.getIndex(testProjectPath,true,true);
	  assertTrue("Index deleted",ind == null);
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
	
		String [] typeDeclEntryResultModel ={"EntryResult: word=typeDecl/C/Mail/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/C/Unknown/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/C/container/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/C/first_class/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/C/postcard/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/E/test/Y/X/Z, refs={ 1 }","EntryResult: word=typeDecl/V/x/Z, refs={ 1 }"};
		IEntryResult[] typedeclresults =ind.queryEntries(IIndexConstants.TYPE_DECL);

		if (typedeclresults.length != typeDeclEntryResultModel.length)
			fail("Entry Result length different from model for typeDecl");
	
		for (int i=0;i<typedeclresults.length; i++)
		{
			assertEquals(typeDeclEntryResultModel[i],typedeclresults[i].toString());
		}
	
		String [] typeDefEntryResultModel ={"EntryResult: word=typedefDecl/int32, refs={ 1 }"};
		IEntryResult[] typedefresults =ind.queryEntries(IIndexConstants.TYPEDEF_DECL);
		
		if (typedefresults.length != typeDefEntryResultModel.length)
					fail("Entry Result length different from model for typeDef");
	
		for (int i=0;i<typedefresults.length; i++)
		{
		 assertEquals(typeDefEntryResultModel[i],typedefresults[i].toString());
		}
				
		String [] namespaceResultModel = {"EntryResult: word=namespaceDecl/X/Z, refs={ 1 }", "EntryResult: word=namespaceDecl/Y/X/Z, refs={ 1 }", "EntryResult: word=namespaceDecl/Z, refs={ 1 }"};
		IEntryResult[] namespaceresults =ind.queryEntries(IIndexConstants.NAMESPACE_DECL);
		
		if (namespaceresults.length != namespaceResultModel.length)
				fail("Entry Result length different from model for namespace");
	
		for (int i=0;i<namespaceresults.length; i++)
		{
			assertEquals(namespaceResultModel[i],namespaceresults[i].toString());
		}
				
		String [] fieldResultModel = {"EntryResult: word=fieldDecl/array/container/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/bye/test/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/cool/test/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/hi/test/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/index/container/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/postage/Mail/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/sz/container/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/type/Mail/Y/X/Z, refs={ 1 }", "EntryResult: word=fieldDecl/why/test/Y/X/Z, refs={ 1 }"};
		IEntryResult[] fieldresults =ind.queryEntries(IIndexConstants.FIELD_DECL);
	
		if (fieldresults.length != fieldResultModel.length)
				fail("Entry Result length different from model for fieldDecl");
	
		for (int i=0;i<fieldresults.length; i++)
		{
			assertEquals(fieldResultModel[i],fieldresults[i].toString());
		}
	
		String [] functionResultModel = {"EntryResult: word=functionDecl/doSomething, refs={ 1 }"};	
		IEntryResult[] functionresults =ind.queryEntries(IIndexConstants.FUNCTION_DECL);
		
		if (functionresults.length != functionResultModel.length)
					fail("Entry Result length different from model for functionDecl");
	
		for (int i=0;i<functionresults.length; i++)
		{
			assertEquals(functionResultModel[i],functionresults[i].toString());
		}
		
		String [] methodResultModel = {"EntryResult: word=methodDecl/operator<</Mail/Y/X/Z, refs={ 1 }","EntryResult: word=methodDecl/operator=/container/Y/X/Z, refs={ 1 }","EntryResult: word=methodDecl/operator[]/container/Y/X/Z, refs={ 1 }","EntryResult: word=methodDecl/print/Mail/Y/X/Z, refs={ 1 }"};	
		IEntryResult[] methodresults =ind.queryEntries(IIndexConstants.METHOD_DECL);
		
		if (methodresults.length != methodResultModel.length)
				fail("Entry Result length different from model for functionDecl");
	
		for (int i=0;i<methodresults.length; i++)
		{
			assertEquals(methodResultModel[i],methodresults[i].toString());
		}
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
	String[] depTestModel = {"\\IndexerTestProject\\d.h", "\\IndexerTestProject\\Inc1.h", "\\IndexerTestProject\\c.h", "\\IndexerTestProject\\a.h", "\\IndexerTestProject\\DepTest.h"};
	String[] depTest2Model = {"\\IndexerTestProject\\d.h", "\\IndexerTestProject\\DepTest2.h"};
	
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
