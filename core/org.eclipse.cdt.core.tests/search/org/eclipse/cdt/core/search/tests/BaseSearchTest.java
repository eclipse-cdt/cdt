/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jul 22, 2003
 */
package org.eclipse.cdt.core.search.tests;

import java.io.FileInputStream;

import junit.framework.TestCase;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.FileManager;
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BaseSearchTest extends TestCase implements ICSearchConstants {

	static protected ICSearchScope 				scope;	
	static protected IFile 						file;
	static protected IProject 					testProject;
	static protected NullProgressMonitor		monitor;
	static protected IWorkspace 				workspace;
	static protected BasicSearchResultCollector	resultCollector;
	static protected SearchEngine				searchEngine;
	static protected FileManager 				fileManager;
	static final 	 String 					sourceIndexerID = "org.eclipse.cdt.core.originalsourceindexer"; //$NON-NLS-1$
	
	{
		
		(CCorePlugin.getDefault().getCoreModel().getIndexManager()).reset();
		monitor = new NullProgressMonitor();
		
		workspace = ResourcesPlugin.getWorkspace();
		
		try {
			//Create temp project
			testProject = createProject("SearchTestProject");
			
			testProject.setSessionProperty(SourceIndexer.activationKey,new Boolean(true));
			
			//Set the id of the source indexer extension point as a session property to allow
			//index manager to instantiate it
			testProject.setSessionProperty(IndexManager.indexerIDKey, sourceIndexerID);
		} catch (CoreException e) {}
		
		
		
		if (testProject == null)
			fail("Unable to create project");

		//Create file manager
		fileManager = new FileManager();
		
		try {
			//Add a file to the project
			//importFile("mail.cpp", "resources/indexer/mail.cpp");
			importFile("classDecl.cpp", "resources/search/classDecl.cpp");
			importFile("include.h", "resources/search/include.h");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		scope = SearchEngine.createWorkspaceScope();
	
		resultCollector = new BasicSearchResultCollector();
		
		searchEngine = new SearchEngine();
	}

	
	
	public BaseSearchTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
	
	}

	protected void tearDown() {
	}
	
	private IProject createProject(String projectName) throws CoreException {
		ICProject cPrj = CProjectHelper.createCCProject(projectName, "bin");
		return cPrj.getProject();
	}

	private void importFile(String fileName, String resourceLocation) throws Exception{
		//Obtain file handle
		file = testProject.getProject().getFile(fileName); 
		//Create file input stream
		
		if (!file.exists()){
			file.create(new FileInputStream(
					CTestPlugin.getDefault().getFileInPlugin(new Path(resourceLocation))),
					false,monitor);
			fileManager.addFile(file);
		}
	
	}
		
	protected void search(IWorkspace workspace, ICSearchPattern pattern, ICSearchScope scope, ICSearchResultCollector collector) {
		try {
			searchEngine.search( workspace, pattern, scope, collector, false );
		} catch (InterruptedException e) {

		}
	}
	
}
