/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.internal.dstore.universal.miners.filesystem;

import org.eclipse.rse.services.clientserver.FileTypeMatcher;
import org.eclipse.rse.services.clientserver.IClientServerConstants;
import org.eclipse.rse.services.clientserver.IMatcher;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;


public class UniversalFileSystemFilter implements java.io.FilenameFilter {


   protected  String filter;
   protected  boolean allowDirs;
   protected  boolean allowFiles;
   protected  boolean caseSensitive = true;
   protected IMatcher matcher = null;
   protected NamePatternMatcher folderNameMatcher = null;	
   protected int includeFilesOrFolders;
  
   /**
    * @param fString the filter string
    */
   public UniversalFileSystemFilter(String fString, boolean files, boolean folders, boolean caseSensitive) {
      this.filter  = fString;      
      this.allowFiles = files;
      this.allowDirs =  folders; 
      this.caseSensitive = caseSensitive;
   }

	/**
	 * Tests if a specified file should be included in a file list.
	 *
	 * @param   dir    the directory in which the file was found.
	 * @param   name   the name of the file.
	 * @return  <code>true</code> if and only if the name should be
	 * included in the file list; <code>false</code> otherwise.
	 */
   public boolean accept(java.io.File dir, String name) {
   	
   	  boolean match = true; 
   	  java.io.File file = new java.io.File(dir, name);
      if (!allowDirs && file.isDirectory()) 
      	 return false;
      
      if (allowDirs && (file.isDirectory() || ArchiveHandlerManager.getInstance().isArchive(file))) 
         return true;

      if (!allowFiles && file.isFile()) 
         return false;
      
      if (allowDirs && allowFiles)
         setListValues(IClientServerConstants.INCLUDE_ALL, filter);
      else if (allowDirs)
         setListValues(IClientServerConstants.INCLUDE_FOLDERS_ONLY, filter); 
      else if (allowFiles)
         setListValues(IClientServerConstants.INCLUDE_FILES_ONLY, filter);     
      else
         return false;
          
      if ((matcher == null) && (folderNameMatcher == null))
    	  return true;
      if (includeFilesOrFolders != IClientServerConstants.INCLUDE_ALL)
    	  match = matcher.matches(name);
      else {
    	if (file.isFile()) {
    	  if (matcher!=null)
    	  match = matcher.matches(name);
    	} else {
    	  if (folderNameMatcher!=null)
    	  match = folderNameMatcher.matches(name);
    	}
      }
      return match;
    }
   
    protected void setListValues(int includeFilesOrFolders, String nameFilter)
    {
    	this.includeFilesOrFolders = includeFilesOrFolders;
    	if ((nameFilter!=null) && !nameFilter.equals("*")) //$NON-NLS-1$
    	{
    	  if (nameFilter.endsWith(",")) //$NON-NLS-1$
    	    matcher = new FileTypeMatcher(FileTypeMatcher.parseTypes(nameFilter), true);
    	  else
    	    matcher = new NamePatternMatcher(nameFilter, true, caseSensitive);
    	}
    	else
    	  matcher = null;
    	folderNameMatcher = null;
    }
    /**
     * Overloaded method to set variables to affect the folder content subsetting,
     * when there is separate filters for both folder names and filter names.
     * @param includeFilesOrFolders A constant from {IFileConstants}
     * @param folderNameFilter The pattern to filter the folder names by. Can be null to include all folders
     * @param fileNameFilter The pattern to filter the file names by. Can be null to include all files
     */
    protected void setListValues(int includeFilesOrFolders, String folderNameFilter, String fileNameFilter)
    {
    	setListValues(includeFilesOrFolders, fileNameFilter);
    	if ((folderNameFilter!=null) && !folderNameFilter.equals("*")) //$NON-NLS-1$
    	  folderNameMatcher = new NamePatternMatcher(folderNameFilter, true, caseSensitive);
    }
   
 }
