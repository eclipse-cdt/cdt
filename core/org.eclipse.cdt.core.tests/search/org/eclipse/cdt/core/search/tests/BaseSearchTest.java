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
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.index.impl.IFileDocument;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.ui.search.CSearchResultCollector;
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
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BaseSearchTest extends TestCase implements ICSearchConstants {

	ICSearchScope 			scope;
	IFile 					file;
	IFileDocument 			fileDoc;
	IProject 				testProject;
	NullProgressMonitor		monitor;
	IWorkspace 				workspace;
	CSearchResultCollector	resultCollector;
	SearchEngine			searchEngine;

	public BaseSearchTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();

		monitor = new NullProgressMonitor();
		
		workspace = ResourcesPlugin.getWorkspace();
		
		//Create temp project
		testProject = createProject("SearchTestProject");

		if (testProject == null)
			fail("Unable to create project");

		//Add a file to the project
		importFile("mail.cpp", "resources/indexer/mail.cpp");
		importFile("classDecl.cpp", "resources/search/classDecl.cpp");

		scope = SearchEngine.createWorkspaceScope();
		
		IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
		indexManager.setEnabled(testProject,true);
		
		resultCollector = new CSearchResultCollector( true );
		resultCollector.setProgressMonitor( monitor );
		
		searchEngine = new SearchEngine();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		//Delete project
		if (testProject.exists()){
			testProject.delete( true, monitor );
		}
	}
	
	private IProject createProject(String projectName) throws CoreException {
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(projectName);
		
		if( !project.exists() ) {
			project.create( null );
		} else {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		}
		
		if( !project.isOpen() ) {
			project.open( null );
		}

		//Fill out a project description
		IPath defaultPath = Platform.getLocation();
		IPath newPath = project.getFullPath();
		
		if (defaultPath.equals(newPath))
			newPath = null;
		
		IProjectDescription description = workspace.newProjectDescription(project.getName());
		
		description.setLocation(newPath);
		
		//Create the project
		IProject cproject = CCorePlugin.getDefault().createCProject( description,
																	 project,
																	 monitor,
																	 CCorePlugin.PLUGIN_ID + ".make");

		if( !project.hasNature(CProjectNature.C_NATURE_ID) ){
			addNatureToProject(project, CProjectNature.C_NATURE_ID, null);
		}

		return cproject;
	}

	private void importFile(String fileName, String resourceLocation)throws Exception{
		//Obtain file handle
		file = testProject.getProject().getFile(fileName); 
		String pluginRoot=org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.core.tests").find(new Path("/")).getFile();
		//Create file input stream
		
		if (!file.exists()){
			file.create(new FileInputStream(pluginRoot + resourceLocation),false,monitor);
		}
		fileDoc = new IFileDocument(file);
	}
	
	private void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = proj.getDescription();
		
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[ prevNatures.length + 1 ];
		
		System.arraycopy( prevNatures, 0, newNatures, 0, prevNatures.length );
		newNatures[ prevNatures.length ] = natureId;
		description.setNatureIds( newNatures );
		proj.setDescription( description, monitor );
	}
	
	protected void search(IWorkspace workspace, ICSearchPattern pattern, ICSearchScope scope, ICSearchResultCollector collector) {
		resultCollector.setProgressMonitor( monitor );
		searchEngine.search( workspace, pattern, scope, collector );
	}
}
