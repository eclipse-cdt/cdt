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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.cdt.internal.core.index.impl.IFileDocument;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author bgheorgh
 */
public class IndexManagerTest extends TestCase {
	IFile file;
	IFileDocument fileDoc;
	IProject testProject;
	NullProgressMonitor monitor;
    IndexManager indexManager;
	/**
	 * Constructor for IndexManagerTest.
	 * @param name
	 */
	public IndexManagerTest(String name) {
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
		//Add a file to the project
		importFile("mail.cpp","resources/indexer/mail.cpp");	
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
		return new TestSuite(IndexManagerTest.class);
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
	   if (!project.hasNature(CProjectNature.C_NATURE_ID)) {
		   addNatureToProject(project, CProjectNature.C_NATURE_ID, null);
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
	
	private void importFile(String fileName, String resourceLocation)throws Exception{
	   //Obtain file handle
       file = testProject.getProject().getFile(fileName); 
	   String pluginRoot=org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.core.tests").find(new Path("/")).getFile();
	   //Create file input stream
	   monitor = new NullProgressMonitor();
	   if (!file.exists()){
		 file.create(new FileInputStream(pluginRoot + resourceLocation),false,monitor);
	   }
	   fileDoc = new IFileDocument(file);
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
		//Enable indexing on the created project
		//By doing this, we force the Index Manager to indexAll()
		indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
		indexManager.setEnabled(testProject,true);
		Thread.sleep(15000);
		IIndex ind = indexManager.getIndex(testProject.getFullPath(),true,true);
		char[] prefix = "typeDecl/".toCharArray();
		IQueryResult[] qresults = ind.queryPrefix(prefix);
		IEntryResult[] eresults = ind.queryEntries(prefix);
		String [] queryResultModel = {"IndexedFile(1: /IndexerTestProject/mail.cpp)"};
		String [] entryResultModel ={"EntryResult: word=typeDecl/C/Mail/, refs={ 1 }", "EntryResult: word=typeDecl/C/Unknown/, refs={ 1 }", "EntryResult: word=typeDecl/C/container/, refs={ 1 }", "EntryResult: word=typeDecl/C/first_class/, refs={ 1 }", "EntryResult: word=typeDecl/C/postcard/, refs={ 1 }"};
		
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
		//Enable indexing on the created project
		//By doing this, we force the Index Manager to indexAll()
		indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
		indexManager.setEnabled(testProject,true);
		Thread.sleep(15000);
		//Make sure project got added to index
		IPath testProjectPath = testProject.getFullPath();
		IIndex ind = indexManager.getIndex(testProjectPath,true,true);
		assertTrue("Index exists for project",ind != null);
		//Add a new file to the project, give it some time to index
		importFile("DocumentManager.h","resources/indexer/DocumentManager.h");
		Thread.sleep(10000);
		ind = indexManager.getIndex(testProjectPath,true,true);
		char[] prefix = "typeDecl/C/CDocumentManager/".toCharArray();
		String [] entryResultModel ={"EntryResult: word=typeDecl/C/CDocumentManager/, refs={ 1 }"};
		IEntryResult[] eresults =ind.queryEntries(prefix);
		
		if (eresults.length != entryResultModel.length)
			fail("Entry Result length different from model");
			
		for (int i=0;i<eresults.length; i++)
		{
			assertEquals(entryResultModel[i],eresults[i].toString());
		}
	}

	public void testRemoveProjectFromIndex() throws Exception{
	  //Enable indexing on the created project
	  //By doing this, we force the Index Manager to indexAll()
	  indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
	  indexManager.setEnabled(testProject,true);
	  Thread.sleep(15000);
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
	 //Enable indexing on the created project
	 //By doing this, we force the Index Manager to indexAll()
	 indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
	 indexManager.setEnabled(testProject,true);
	 Thread.sleep(15000);
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
	 String [] entryResultBeforeModel ={"EntryResult: word=typeDecl/C/CDocumentManager/, refs={ 1 }", "EntryResult: word=typeDecl/C/Mail/, refs={ 2 }", "EntryResult: word=typeDecl/C/Unknown/, refs={ 2 }", "EntryResult: word=typeDecl/C/container/, refs={ 2 }", "EntryResult: word=typeDecl/C/first_class/, refs={ 2 }", "EntryResult: word=typeDecl/C/postcard/, refs={ 2 }"};
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
	 String [] entryResultAfterModel ={"EntryResult: word=typeDecl/C/CDocumentManager/, refs={ 1 }"};
	 if (eresults.length != entryResultAfterModel.length)
		fail("Entry Result length different from model");
		
	 for (int i=0;i<eresults.length; i++)
     {
		assertEquals(entryResultAfterModel[i],eresults[i].toString());
	 }
	}
	
}
