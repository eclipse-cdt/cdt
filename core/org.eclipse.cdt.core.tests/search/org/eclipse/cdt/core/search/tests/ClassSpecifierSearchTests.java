/*
 * Created on Jul 11, 2003
 */
package org.eclipse.cdt.core.search.tests;

import java.io.FileInputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchResultCollector;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.index.impl.IFileDocument;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.search.PathCollector;
import org.eclipse.cdt.internal.core.search.PatternSearchJob;
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author bgheorgh
 */
public class ClassSpecifierSearchTests extends TestCase {
	IFile file;
	IFileDocument fileDoc;
	IProject testProject;
	NullProgressMonitor monitor;
	

	/**
	 * @param name
	 */
	public ClassSpecifierSearchTests(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public static Test suite() { 
			return new TestSuite(ClassSpecifierSearchTests.class);
	}
	
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
   
	   if (!project.hasNature(CProjectNature.C_NATURE_ID)) {
				 addNatureToProject(project, CProjectNature.C_NATURE_ID, null);
		}
			 
	   return cproject; 
	}

	/**
	 * @param project
	 * @param string
	 * @param object
	 */
	private void addNatureToProject(IProject project, String string, Object object) {
		// TODO Auto-generated method stub
		
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
	 
	 public void search(ICSearchPattern pattern, ICSearchResultCollector collector){
	
	 }
	/*
	 * Start of tests
	 */ 
	public void testSearchSimpleName() throws Exception {
		//ICSearchPattern pattern = SearchEngine.createSearchPattern( "Mail", ICSearchConstants.TYPE, ICSearchConstants.DECLARATIONS , false );
		//Create a new result collector
		//CSearchResultCollector collector= new CSearchResultCollector();
		
			
	}
	
	
}
