/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Sept 28, 2004
 */
package org.eclipse.cdt.core.parser.tests.ast2;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.FileManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author dsteffle
 */
public class DOMFileBasePluginTest extends TestCase {
    static NullProgressMonitor		monitor;
    static IWorkspace 				workspace;
    static IProject 				project;
    static FileManager 				fileManager;
    static int						numProjects = 0;
    static Class					className;
	static ICProject cPrj; 

    private void initialize(Class aClassName){
        if( CCorePlugin.getDefault() != null && CCorePlugin.getDefault().getCoreModel() != null){
			//(CCorePlugin.getDefault().getCoreModel().getIndexManager()).reset();
			monitor = new NullProgressMonitor();
			
			workspace = ResourcesPlugin.getWorkspace();
			
	        try {
	        	cPrj = CProjectHelper.createCCProject("ParserTestProject", "bin"); //$NON-NLS-1$ //$NON-NLS-2$
	        	
	            project = cPrj.getProject();
	            
	            // ugly
	            if (className == null || !className.equals(aClassName)) {
	            	className = aClassName;
	            	numProjects++;
	            }
	        } catch ( CoreException e ) {
	            /*boo*/
	        }
			if (project == null)
				throw new NullPointerException("Unable to create project"); //$NON-NLS-1$
	
			//Create file manager
			fileManager = new FileManager();
        }
    }

    public DOMFileBasePluginTest(String name, Class className)
    {
    	super(name);
    	initialize(className);
    }
	    
    public void cleanupProject() throws Exception {
    	numProjects--;
    	
    	try{
    		if (numProjects == 0) {
    			project.delete( true, false, monitor );
    			project = null;
    		}
	    } catch( Throwable e ){
	        /*boo*/
	    }
    }

    protected void tearDown() throws Exception {
        if( project == null || !project.exists() ) 
            return;
        
        IResource [] members = project.members();
        for( int i = 0; i < members.length; i++ ){
            if( members[i].getName().equals( ".project" ) || members[i].getName().equals( ".cdtproject" ) ) //$NON-NLS-1$ //$NON-NLS-2$
                continue;
            try{
                members[i].delete( false, monitor );
            } catch( Throwable e ){
                /*boo*/
            }
        }
	}

    // below can be used to work with large files (too large for memory)
//    protected IFile importFile(String fileName) throws Exception {
//		IFile file = cPrj.getProject().getFile(fileName);
//		if (!file.exists()) {
//			try{
//				FileInputStream fileIn = new FileInputStream(
//						CTestPlugin.getDefault().getFileInPlugin(new Path("resources/parser/" + fileName))); 
//				file.create(fileIn,false, monitor);        
//			} catch (CoreException e) {
//				e.printStackTrace();
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		return file;
//    }
    
    protected IFolder importFolder(String folderName) throws Exception {
    	IFolder folder = project.getProject().getFolder(folderName);
		
		//Create file input stream
		if( !folder.exists() )
			folder.create( false, false, monitor );
		
		return folder;
    }
    public IFile importFile(String fileName, String contents ) throws Exception{
		//Obtain file handle
		IFile file = project.getProject().getFile(fileName);
		
		InputStream stream = new ByteArrayInputStream( contents.getBytes() ); 
		//Create file input stream
		if( file.exists() )
		    file.setContents( stream, false, false, monitor );
		else
			file.create( stream, false, monitor );
		
		fileManager.addFile(file);
		
		return file;
	}
            
}
