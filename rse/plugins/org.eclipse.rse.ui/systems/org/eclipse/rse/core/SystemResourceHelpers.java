/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.core;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;

/**
 * A class with helper methods for working with the underlying Eclipse resources
 *  needed for filters, filter pools and filter pool managers.
 */
public class SystemResourceHelpers implements FileFilter
{
	
	private static SystemResourceHelpers defaultInstance = null;
	
    //private SystemLogFile logFile = null;
	
	// variables to affect the list method for subsetting folder contents
	private boolean filesOnly = false;
	private boolean foldersOnly = false;
	private String namePrefix = null;
	private String nameSuffix = null;
    
       
    /**
     * Constructor
     */
    public SystemResourceHelpers()
    {
    }
    
    /**
     * Return common instance if unique instance not required.
     */
    public static SystemResourceHelpers getResourceHelpers()
    {
    	if (defaultInstance == null)
    	  defaultInstance = new SystemResourceHelpers();
    	return defaultInstance;
    }
    
    // ---------------------------
    // GENERIC RESOURCE METHODS...
    // ---------------------------

    /**
     * Method to delete a folder or file and absorb the exception
     */
    public void deleteResource(IResource fileOrFolder)
    {
    	try {
    	  if (fileOrFolder instanceof IFile)
    	    deleteFile((IFile)fileOrFolder);
    	  else
    	    deleteFolder((IFolder)fileOrFolder);
    	} catch (Exception exc)
    	{
    	   logException("Exception deleting resource " + fileOrFolder.getName(), exc);
    	}
    }
    /**
     * Method to rename a folder or file and absorb the exception
     */    
    public void renameResource(IResource fileOrFolder, String newName)
    {
    	//System.out.println("inside renameResource for "+fileOrFolder.getFullPath() + ". newName = " + newName);
    	if (fileOrFolder.getName().equals(newName))
    	{
    		//System.out.println("same name! ");
    		Exception exc = new Exception("Rename to same name: " + newName);
    		exc.fillInStackTrace();
    		exc.printStackTrace();
    		return;
    	}
    	try {
    	  if (fileOrFolder instanceof IFile)
    	    renameFile((IFile)fileOrFolder, newName);
    	  else
    	    renameFolder((IFolder)fileOrFolder, newName);
    	} catch (Exception exc) 
    	{
    	   logException("Exception rename resource " + fileOrFolder.getName() + " to " + newName, exc);
    	}
    }    
    
    /**
     * Refresh a resource from disk. Turns off resource event listening to avoid recursion.
     * Does a DEPTH_INFINITE refresh.
     */
    public void refreshResource(IResource fileOrFolder)
    {
        SystemResourceManager.turnOffResourceEventListening();
        try 
        {
			if (!fileOrFolder.getWorkspace().isTreeLocked())
	    	   fileOrFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (Exception exc) {}
        SystemResourceManager.turnOnResourceEventListening();
    }
    /**
     * Refresh a resource from disk. Turns off resource event listening to avoid recursion.
     * Does a DEPTH_ZERO refresh, so children are not refreshed. Should be more efficient than
     * deep refresh.
     */
    public void refreshResourceShallow(IResource fileOrFolder)
    {
        SystemResourceManager.turnOffResourceEventListening();
        try 
        {
			if (!fileOrFolder.getWorkspace().isTreeLocked())
	    	   fileOrFolder.refreshLocal(IResource.DEPTH_ZERO, null);
        } catch (Exception exc) {}
        SystemResourceManager.turnOnResourceEventListening();
    }

    /**
     * Test if a resource is in use, prior to attempting to rename or delete it.
     * @return true if it is in use or read only, false if it is not.
     */
    public static boolean testIfResourceInUse(IResource resource)
    {
    	boolean inUse = resource.getResourceAttributes().isReadOnly() || !resource.isAccessible();
    	if (!inUse) // keep testing..
    	{
    	    IPath localOSLocation = resource.getLocation();
    	    if (localOSLocation != null)
    	    {
    	        File osFile = new File(localOSLocation.toOSString());
    	        inUse = !osFile.canWrite();
    	        if (!inUse && (resource instanceof IFile))
    	        {
    	        	try {
    	        	  //System.out.println("testing " + osFile.getAbsolutePath());
                      FileWriter outFileStream = new FileWriter(osFile.getAbsolutePath(),true);
                      outFileStream.close();
    	        	} 
    	        	catch (IOException exc)
    	        	{
    	        		inUse = true;
    	        		//System.out.println("...file is locked!");
    	        	}
    	        }
    	    }   	     
    	}
    	//System.out.println("Testing in-use of resource " + resource.getName() + ": " + inUse);
    	return inUse;
    }
    
    // --------------------------
    // FOLDER SPECIFIC METHODS...
    // --------------------------
    
    /**
     * Create a folder, if it does not already exist.
     */
    public IFolder getOrCreateFolder(IContainer parentFolder, String folderName)
    {
    	/*
    	if (folderName.equals("Private"))
    	{
    		String msg = "Someone asked to create Private folder!";
    		Exception e = new Exception(msg);
    		e.fillInStackTrace();
    		SystemPlugin.logError(msg, e);
    	}
    	*/
    	boolean ok = true;
		IFolder folder = getFolder(parentFolder, folderName);
		if (!exists(folder))
		  ok = createFolder(folder);    	
    	return folder;
    }
    /**
     * Create a folder, if it does not already exist.
     */
    public boolean ensureFolderExists(IFolder folder)
    {
    	boolean ok = true;
		if (!exists(folder))
		{
		  ok = createFolder(folder);    	
		}
    	return ok;
    }
    
    /**
     * Get a folder whose parent is either a project or a folder
     */
    public IFolder getFolder(IContainer parent, String folderName)
    {
        SystemResourceManager.turnOffResourceEventListening();
    	IFolder folder = null;
    	if (parent instanceof IProject)
        {
    	  folder = ((IProject)parent).getFolder(folderName);    	
        }
    	else
    	  folder = ((IFolder)parent).getFolder(folderName);
        SystemResourceManager.turnOnResourceEventListening();
    	return folder;
    }    
    /**
     * Re-get a folder which has been renamed.
     */
    public IFolder getRenamedFolder(IFolder oldFolder, String newFolderName)
    {
        SystemResourceManager.turnOffResourceEventListening();
    	IFolder folder = null;
    	IContainer parent = oldFolder.getParent();
    	if (parent instanceof IProject)
    	  folder = ((IProject)parent).getFolder(newFolderName);    	
    	else
    	  folder = ((IFolder)parent).getFolder(newFolderName);
        SystemResourceManager.turnOnResourceEventListening();
    	return folder;
    }    


    /**
     * Create new folder
     */
    public boolean createFolder(IFolder folder)
    {
    	boolean ok = true;    	
	    try 
	    {    	
           SystemResourceManager.turnOffResourceEventListening();
    	   folder.create(true,true,null);
           SystemResourceManager.turnOnResourceEventListening();
    	   /*
    	   if (folder.getName().equals("Private"))
    	   {		  
    		 String msg = "Someone asked to create Private folder!";
    		 Exception e = new Exception(msg);
    		 e.fillInStackTrace();
    		 SystemPlugin.logError(msg, e);
    	   }
           */
	    } catch (CoreException e)
	    {
           SystemResourceManager.turnOnResourceEventListening();
	       IStatus status = e.getStatus();
	       if (status!=null)
	       {
	         int code = status.getCode();
	         if (code != IResourceStatus.RESOURCE_EXISTS)
	         {
	           logException("error creating folder "+folder.getName(),e);	    	
	           ok = false;	
	         }
	       }
	    }
    	return ok;
    }
    
    /**
     * Delete a folder
     */    
    public boolean deleteFolder(IFolder folder)
                    throws Exception
    {
        // to ensure success, we force a refresh local action...
        refreshResource(folder);
        boolean existsInFileSystem = exists(folder);
    	if (!existsInFileSystem)
    	  return true;
    	boolean existsInWorkSpace = folder.exists();
    	if (existsInFileSystem && !existsInWorkSpace)
    	{
          logMessage("...deleteFolder error: folder "+folder.getLocation().toOSString()+" exists in file system but not in workspace! Cannot delete it.");
          existsInWorkSpace = folder.exists(); // for debugging. set breakpoint here
    	}
    	boolean ok = true;    	
    	//String name = folder.getName();
        SystemResourceManager.turnOffResourceEventListening();
    	folder.delete(true,false,null); // force-yes, keep-history-no, no progress monitor
        SystemResourceManager.turnOnResourceEventListening();
    	return ok;    	
    }
    /**
     * Rename a folder
     */    
    public boolean renameFolder(IFolder folder, String newName)
                    throws Exception
    {
    	//System.out.println("inside renameFolder for "+folder.getFullPath() + ". exists? = " + folder.exists());
    	if (!exists(folder))
    	  return true;
    	boolean ok = true;    	
		IPath newPath = folder.getFullPath().removeLastSegments(1).append(newName);    	
		//System.out.println("new path = " + newPath.toOSString());
		try {
           SystemResourceManager.turnOffResourceEventListening();
           if (!newPath.toFile().exists())
        	   folder.move(newPath, true, false, null);		
           SystemResourceManager.turnOnResourceEventListening();
		  //System.out.println("...path after move = " + folder.getFullPath().toOSString());  	      
		} catch (Exception exc)
		{
           SystemResourceManager.turnOnResourceEventListening();
			//exc.printStackTrace();
			throw exc;
		}
    	return ok;    	
    }    
    /**
     * Move a folder
     */    
    public boolean moveFolder(IFolder newParent, IFolder folder)
                    throws Exception
    {
    	boolean ok = true;    	
    	if (!exists(folder))
    	  return true;    	
        SystemResourceManager.turnOffResourceEventListening();
    	folder.move(newParent.getFullPath().append(folder.getName()),true,false,null);
        SystemResourceManager.turnOnResourceEventListening();
    	return ok;    	
    }

    /**
     * Return the path of the given folder
     */
    public String getFolderPath(IFolder folder)
    {
    	return folder.getLocation().toOSString();
    }

    /**
     * Return a list of child folders in the given container
     * @param projectOrFolder The parent container to search.
     * @return an array of IFolder objects
     */
    public IFolder[] listFolders(IContainer projectOrFolder)
    {
    	if (!projectOrFolder.exists())
    	  return (new IFolder[0]);

        SystemResourceManager.turnOffResourceEventListening();           
           
    	Vector folders = new Vector();
    	try
    	{
            IResource[] members = projectOrFolder.members();
            if (members != null)
              for (int idx=0; idx<members.length; idx++)
                 if (members[idx].getType() == IResource.FOLDER)		
                   folders.addElement(members[idx]);
    	} catch (Exception exc)
    	{
    		logException("Error retrieving folder list",exc);
    	}

        SystemResourceManager.turnOnResourceEventListening();           
           
    	return convertToFolderArray(folders);
    }

    /**
     * Return a list of child folders in the given container,
     *  which contain a file of the given name.
     * @param projectOrFolder The parent container to search.
     * @param fileName The name of the file which must exist 
     *         in the folder in order to be included in the returned list
     * @return an array of IFolder objects
     */
    public IFolder[] listFolders(IContainer projectOrFolder, String fileName)
    {
    	if (!exists(projectOrFolder))
    	  return (new IFolder[0]);

        SystemResourceManager.turnOffResourceEventListening();           
           
    	Vector folders = new Vector();
        // to ensure we get everything, we force a refresh local action...
        try {
        	refreshResource(projectOrFolder);
        } catch (Exception exc) {}
    	try
    	{
    		// ok, now we can query the children...
            IResource[] members = projectOrFolder.members();
            if (members != null)
            {
              for (int idx=0; idx<members.length; idx++)
              {
                 if (members[idx].getType() == IResource.FOLDER)		
                 {
                   if (exists(getFile((IFolder)members[idx], fileName)))
                     folders.addElement(members[idx]);
                 }
              }
            }
    	} catch (Exception exc)
    	{
    		logException("Error retrieving folder list",exc);
    	}
    	
        SystemResourceManager.turnOnResourceEventListening();           
           
    	return convertToFolderArray(folders);
    }
    
    /**
     * Return a list of names of folders in the given container
     * @param folder The folder to query
     * @param namePrefix Optional prefix all names should match. Can be null.
     * @param nameSuffix Optional suffix all names should match. Can be null.
     */
    public String[] listFolders(IFolder folder, String namePrefix, String nameSuffix)
    {
        setListValues(false, true, namePrefix, nameSuffix);   
        if (!folder.exists())
        {
            return new String[0];
        }
            
    	File dirFile = new File(folder.getLocation().toOSString());
    	//String[] folders = dirFile.listFiles(this);  
    	File[] fileObjs = dirFile.listFiles(this);
    	String[] folders = null;
    	if (fileObjs != null)
    	{
    	  folders = new String[fileObjs.length];
    	  for (int idx=0; idx<folders.length; idx++)
    	     folders[idx] = fileObjs[idx].getName();
    	}
    	else
    	  folders = new String[0];    	      
    	return folders;
    }

    // ------------------------
    // FILE SPECIFIC METHODS...
    // ------------------------
 
    /**
     * Get a file whose parent is either a project or a folder
     */
    public IFile getFile(IContainer parent, String fileName)
    {
    	IFile file = null;

        SystemResourceManager.turnOffResourceEventListening();           
           
    	if (parent instanceof IProject)
    	  file = ((IProject)parent).getFile(fileName);    	
    	else
    	  file = ((IFolder)parent).getFile(fileName);

        SystemResourceManager.turnOnResourceEventListening();           
           
    	return file;
    }    
    /**
     * Check if a file exists in the file system.
     * For some reason, the exists() method of IFile cannot be trusted to
     * report actual file system existence.
     */
    public boolean fileExists(IFile file)
    {
    	String fileLocation = file.getLocation().toOSString();
    	//System.out.println("File name = "+file.getName()+" File location = " + fileLocation);
    	return (new File(fileLocation)).exists();
    }
    
    /**
     * Delete a file
     */    
    public boolean deleteFile(IFile file)
                    throws Exception
    {
        SystemResourceManager.turnOffResourceEventListening();
        // to ensure success, we force a refresh local action...
        try {
    	   file.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (Exception exc) {}

    	boolean ok = true;    	
    	String name = file.getName();
     	file.delete(true,false,null); // force-yes, keep-history-no, no progress monitor
        SystemResourceManager.turnOnResourceEventListening();
    	return ok;    	
    }
    /**
     * Rename a file
     * @param newName - the name name for the file, unqualified!
     */    
    public boolean renameFile(IFile file, String newName)
                    throws Exception
    {
    	boolean ok = true;    	
		IPath newPath = file.getFullPath().removeLastSegments(1).append(newName);    	
        SystemResourceManager.turnOffResourceEventListening();
        // to ensure success, we force a refresh local action...
        try {
    	   file.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (Exception exc) {}
        try {
  	      file.move(newPath, true, false, null); // path, force, keep-history, progress monitor
        } catch (Exception exc)
        {
            SystemResourceManager.turnOnResourceEventListening();
        	throw exc;
        }
        SystemResourceManager.turnOnResourceEventListening();
    	return ok;    	
    }    
    /**
     * Move a file
     */    
    public boolean moveFile(IFolder newParent, IFile file)
                    throws Exception
    {
    	boolean ok = true;    	
        SystemResourceManager.turnOffResourceEventListening();
        // to ensure success, we force a refresh local action...
        try {
    	   file.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (Exception exc) {}
        try
        {
     	  file.move(newParent.getFullPath().append(file.getName()),true,false,null);
        } catch (Exception exc)
        {
            SystemResourceManager.turnOnResourceEventListening();        	
        	throw exc;
        }
        SystemResourceManager.turnOnResourceEventListening();
    	return ok;    	
    }

    /**
     * Return the path of the given file
     */
    public static String getFilePath(IFile file)
    {
    	return file.getLocation().toOSString();
    }



    /**
     * Return a list of child files in the given container
     * @param projectOrFolder The parent container to search.
     * @return an array of IFile objects
     */
    public IFile[] listFiles(IContainer projectOrFolder)
    {
    	if (!exists(projectOrFolder))
    	  return (new IFile[0]);

        SystemResourceManager.turnOffResourceEventListening();           
           
    	Vector files = new Vector();
        // to ensure we get everything, we force a refresh local action...
        try {
        	refreshResource(projectOrFolder);
        } catch (Exception exc) {}
    	try
    	{
            IResource[] members = projectOrFolder.members();
            if (members != null)
              for (int idx=0; idx<members.length; idx++)
                 if (members[idx].getType() == IResource.FILE)		
                   files.addElement(members[idx]);
    	} catch (Exception exc)
    	{
    		logException("Error retrieving file list",exc);
    	}

        SystemResourceManager.turnOnResourceEventListening();           
    	return convertToFileArray(files);
    }

    /**
     * Return a list of names of files in the given container
     * @param folder The folder to query
     * @param namePrefix Optional prefix all names should match. Can be null.
     * @param nameSuffix Optional suffix all names should match. Can be null.
     */
    public String[] listFiles(IFolder folder, String namePrefix, String nameSuffix)
    {
        setListValues(true, false, namePrefix, nameSuffix);    	
    	File dirFile = new File(folder.getLocation().toOSString());
    	//String[] files = dirFile.list(this);        
    	File[] fileObjs = dirFile.listFiles(this);
    	String[] files = null;
    	if (fileObjs != null)
    	{
    	  files = new String[fileObjs.length];
    	  for (int idx=0; idx<files.length; idx++)
    	     files[idx] = fileObjs[idx].getName();
    	}
    	else
    	  files = new String[0];
    	return files;
    }

    // ------------------------
    // UTILITY METHODS...
    // ------------------------

    /**
     * Convert a vector of IFolder objects to an array of IFolder.
     * Result is only null if input is null.
     */
    public IFolder[] convertToFolderArray(Vector vector)
    {
    	if (vector == null)
    	  return null;
    	IFolder[] folders = new IFolder[vector.size()];
    	for (int idx=0; idx<vector.size(); idx++)
    	   folders[idx] = (IFolder)vector.elementAt(idx);
    	return folders;
    }   
    /**
     * Convert a vector of IFile objects to an array of IFile.
     * Result is only null if input is null.
     */
    public IFile[] convertToFileArray(Vector vector)
    {
    	if (vector == null)
    	  return null;
    	IFile[] files = new IFile[vector.size()];
    	for (int idx=0; idx<vector.size(); idx++)
    	   files[idx] = (IFile)vector.elementAt(idx);
    	return files;
    }   

    /**
     * Convert a name array to a vector. If array is null or empty, an empty vector is returned.
     */
    public Vector convertToVector(String[] array)
    {
    	Vector v = new Vector();
    	if (array != null)
    	  for (int idx=0; idx<array.length; idx++)
    	     v.addElement(array[idx]);
    	return v;
    }   
    /**
     * Convert a name array to a vector. As each name is copied
     *  to the vector, its prefix and suffix are stripped off.
     * If array is null or empty, an empty vector is returned.
     */
    public Vector convertToVectorAndStrip(String[] array, String namePrefix, String nameSuffix)
    {
    	Vector v = new Vector();
    	if (array != null)
    	  for (int idx=0; idx<array.length; idx++)
    	  {
    	  	 String name = array[idx];
    	  	 if (namePrefix != null)
    	  	   name = name.substring(namePrefix.length());
    	  	 if (nameSuffix != null)
    	  	   name = name.substring(0,name.lastIndexOf(nameSuffix));
    	     v.addElement(name);
    	  }
    	return v;
    }   
        
    // ------------------------
    // INTERNAL METHODS...
    // ------------------------

    /**
     * Method to set variables to affect the folder content subsetting
     */
    public void setListValues(boolean filesOnly, boolean foldersOnly, String namePrefix, String nameSuffix)
    {
    	this.filesOnly = filesOnly;
    	this.foldersOnly = foldersOnly;
    	this.namePrefix = namePrefix;
    	this.nameSuffix = nameSuffix;
    }
    
    /**
     * Method required by FilenameFilter interface, used by java.io.File.list()
     */
    //public boolean accept(File file, String name)
    public boolean accept(File file)
    {
    	boolean match = true;
    	if (filesOnly)
    	{
    	  if (!file.isFile())
    	    return false;
    	}
    	else if (foldersOnly)
    	{
    	  if (!file.isDirectory())
    	    return false;
    	}
    	String name = file.getName();
    	if (nameSuffix != null)
    	{
    	  if (!name.endsWith(nameSuffix))
    	    return false;    	    
    	}
    	if (namePrefix != null)
    	{
    	  if (!name.startsWith(namePrefix))
    	    return false;
    	}
    	return match;
    }
        
    public void logMessage(String msg)
    {
        SystemBasePlugin.logWarning(msg);
    }
    public void logException(String msg, Exception exc)
    {
    	SystemBasePlugin.logError(msg, exc);
    }            


    /**
     * For some reason the exists() method on IResource is fundamentally not reliable.
     * Because of this, we resort to the looking ourselves at the file system.
     */
    public boolean exists(IResource resource)
    {       	
    	// DKM - doing a refresh while in a wizard causes stack overflow
    	//  defect #57739
    	//     I think this is an unnecessary step since in most cases the resource will
    	//     already exist. 
    	if (resource.exists())
    	{
    		return true;
    	}
    	
    	// it appears the serious reliability problems are due to out of synch problems.
    	// so, I have decided to do a refresh and see if that is cheaper than creating
    	// hundreds of these java.io.File objects for every touch of every resource... phil. 10/21/2002. 	
		refreshResourceShallow(resource);
		return resource.exists();
		/*
    	IPath localOSLocation = resource.getLocation();
    	if (localOSLocation == null)
    	  return true; // what else?    	
    	File osFile = new File(localOSLocation.toOSString());
    	return osFile.exists();
    	*/
    }    
    

}