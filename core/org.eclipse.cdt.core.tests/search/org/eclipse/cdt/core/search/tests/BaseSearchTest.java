/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Jul 22, 2003
 */
package org.eclipse.cdt.core.search.tests;

import java.io.FileInputStream;

import junit.framework.TestCase;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorOperation;
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
import org.eclipse.cdt.internal.core.index.cindexstorage.Index;
import org.eclipse.cdt.internal.core.index.domsourceindexer.DOMSourceIndexer;
import org.eclipse.cdt.internal.core.index.domsourceindexer.DOMSourceIndexerRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
	static protected DOMSourceIndexer				sourceIndexer;
	{
		
		//(CCorePlugin.getDefault().getCoreModel().getIndexManager()).reset();
		monitor = new NullProgressMonitor();
		
		workspace = ResourcesPlugin.getWorkspace();
		CCorePlugin.getDefault().getPluginPreferences().setValue(CCorePlugin.PREF_INDEXER, CCorePlugin.DEFAULT_INDEXER_UNIQ_ID);
		
		try {
			//Create temp project
			testProject = createProject("SearchTestProject");
			
			testProject.setSessionProperty(DOMSourceIndexer.activationKey,new Boolean(true));
			
			//Set the id of the source indexer extension point as a session property to allow
			//index manager to instantiate it
			//testProject.setSessionProperty(IndexManager.indexerIDKey, sourceIndexerID);
			sourceIndexer = (DOMSourceIndexer) CCorePlugin.getDefault().getCoreModel().getIndexManager().getIndexerForProject(testProject);
		} catch (CoreException e) {}
		
		 resetIndexer(CCorePlugin.DEFAULT_INDEXER_UNIQ_ID);
		  //The DOM Source Indexer checks to see if a file has any scanner info
		  //set prior to indexing it in order to increase efficiency. We need to let it know
		  //that it is running in test mode in order to allow for this scanner info test to be skipped
		  DOMSourceIndexerRunner.setSkipScannerInfoTest(true);
		
		
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
		
	protected char[] getSearchPattern(int meta_kind, int kind, int ref, String name) {
		return Index.encodeEntry(meta_kind, kind, ref, name);
	}
	
	public void assertEquals(char [] first, char [] second) {
		assertEquals( new String(first), new String(second));
	}
		
	protected void search(IWorkspace workspace, ICSearchPattern pattern, ICSearchScope scope, ICSearchResultCollector collector) {
		try {
			searchEngine.search( workspace, pattern, scope, collector, false );
		} catch (InterruptedException e) {

		}
	}
	
	public void resetIndexer(final String indexerId){
		if ( testProject != null) {
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
	
}
