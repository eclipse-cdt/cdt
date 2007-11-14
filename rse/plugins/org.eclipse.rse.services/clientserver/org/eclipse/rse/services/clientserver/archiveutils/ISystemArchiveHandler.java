/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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
 * Xuan Chen (IBM) - [160775] [api] rename (at least within a zip) blocks UI thread
 ********************************************************************************/

package org.eclipse.rse.services.clientserver.archiveutils;

import java.io.File;
import java.io.InputStream;

import org.eclipse.rse.services.clientserver.ISystemFileTypes;
import org.eclipse.rse.services.clientserver.ISystemOperationMonitor;
import org.eclipse.rse.services.clientserver.search.SystemSearchLineMatch;
import org.eclipse.rse.services.clientserver.search.SystemSearchStringMatcher;


/**
 * @author mjberger
 * An interface that allows implementing classes to create their own
 * Handlers for various types of Archive files, ie: zip, jar, tar, rpm
 */
public interface ISystemArchiveHandler 
{
	
	/**
	 * Turns the archive that this handler represents into a new, empty archive.
	 * (The archive could not exist before, in which case this would be a true
	 * creation).
	 * @return Whether or not the blank archive was successfuly created.
	 */
	public boolean create();
	
	/**
	 * @return an array containing all the entries in the archive file
	 * in a flat format, where the entries' filenames are prepended by
	 * the path to the entry within the virtual file system. If there
	 * are no entries in the file, returns an array of size 0.
	 * @param archiveOperationMonitor the operation progress monitor
	 */
	public VirtualChild[] getVirtualChildrenList(ISystemOperationMonitor archiveOperationMonitor);

	/**
	 * @return an array containing all the entries in the archive file
	 * in a flat format, whose full paths begin with the String <code>parent</code>.
	 * Returns an array of length 0 if there are no such entries.
	 * @param parent full path of the parent
	 * @param archiveOperationMonitor the operation progress monitor
	 */
	public VirtualChild[] getVirtualChildrenList(String parent, ISystemOperationMonitor archiveOperationMonitor);
	
	/**
	 * @return an array containing the virtual children of the virtual
	 * directory named <code>fullVirtualName</code>. If <code>fullVirtualName</code> is "",
	 * returns the top level in the virtual file system tree. If there are no 
	 * values to return, returns null.
	 * @param fullVirtualName full virtual path of the parent
	 * @param archiveOperationMonitor the operation progress monitor
	 */
	public VirtualChild[] getVirtualChildren(String fullVirtualName, ISystemOperationMonitor archiveOperationMonitor);

	/**
	 * @return an array containing the virtual children of the virtual
	 * directory named <code>fullVirtualName</code> that are themselves directories. 
	 * If <code>fullVirtualName</code> is "",
	 * returns the top level of directories in the virtual file system tree. 
	 * If there are no values to return, returns null.
	 * @param fullVirtualName full virtual path of the parent
	 * @param archiveOperationMonitor the operation progress monitor
	 */
	public VirtualChild[] getVirtualChildFolders(String fullVirtualName, ISystemOperationMonitor archiveOperationMonitor);
	
	/**
	 * @return the virtual File or Folder referred to by <code>fullVirtualName</code>.
	 * This method never returns null. In cases where the VirtualChild does not
	 * physically exist in the archive, this method returns a new VirtualChild object
	 * whose exists() method returns false.
	 * @param fullVirtualName full virtual path of the object to retrieve
	 * @param archiveOperationMonitor the operation progress monitor
	 */
	public VirtualChild getVirtualFile(String fullVirtualName, ISystemOperationMonitor archiveOperationMonitor);

	/**
	 * @return Whether or not the virtual file or folder named <code>fullVirtualName</code>
	 * exists in the archive (physically).
	 * @param fullVirtualName full virtual path of the object 
	 * @param archiveOperationMonitor the operation progress monitor
	 */
	public boolean exists(String fullVirtualName, ISystemOperationMonitor archiveOperationMonitor);

	/**
	 * @return Whether or not the handler exists. Usually false if the archive
	 * is corrupted or unreadable.
	 */
	public boolean exists();
	
	/**
	 * @return the archive that this handler deals with
	 */
	public File getArchive();
	
    /**
	 * @return the current timestamp (last modified) for the entry in the archive named
	 * <code>fullVirtualName</code>
	 */
	public long getTimeStampFor(String fullVirtualName);
	 
	/**
	 * @return the current size (uncompressed) for the entry in the archive named
	 * <code>fullVirtualName</code>
	 */
	public long getSizeFor(String fullVirtualName);
	 
	/**
	 * Extracts the virtual file named <code>fullVirtualName</code> from the archive,
	 * placing the results in <code>destination</code>.
	 * @param fullVirtualName The full path and name of the virtual file in the archive.
	 * @param destination The destination file for the extracted virtual file.
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true iff the extraction is successful
	 */
	public boolean extractVirtualFile(String fullVirtualName, File destination, ISystemOperationMonitor archiveOperationMonitor);
	
	/**
	 * Extracts the virtual file named <code>fullVirtualName</code> from the archive,
	 * placing the results in <code>destination</code>. Extracts to the native encoding, but assumes
	 * that the source was archived using <code>sourceEncoding</code> if <code>isText</code> is true.
	 * @param fullVirtualName The full path and name of the virtual file in the archive.
	 * @param destination The destination file for the extracted virtual file.
	 * @param sourceEncoding The encoding of the file in the archive.
	 * @param isText Whether or not the virtual file is a text file.
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true iff the extraction is successful
	 */
	public boolean extractVirtualFile(String fullVirtualName, File destination, String sourceEncoding, boolean isText, ISystemOperationMonitor archiveOperationMonitor);

	/**
	 * Extracts the directory <code>dir</code> (and its children) from 
	 * the archive and places the results in the directory <code>destinationParent</code>.
	 * @param dir The full name of the virtual directory to extract
	 * @param destinationParent A handle to the directory in which the extracted
	 * directory will be placed as a subdirectory.
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true iff the extraction is successful
	 */
	public boolean extractVirtualDirectory(String dir, File destinationParent, ISystemOperationMonitor archiveOperationMonitor);
	
	/**
	 * Extracts the directory <code>dir</code> (and its children) from 
	 * the archive and places the results in the directory <code>destinationParent</code>.
	 * Extracts to the native encoding (if <code>isText</code>), but assumes
	 * that the source was archived using <code>sourceEncoding</code>.
	 * @param dir The full name of the virtual directory to extract
	 * @param destinationParent A handle to the directory in which the extracted
	 * directory will be placed as a subdirectory.
	 * @param sourceEncoding The encoding of the files in the archive.
	 * @param isText Whether or not the files in the directory are text files
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true iff the extraction is successful
	 */
	public boolean extractVirtualDirectory(String dir, File destinationParent, String sourceEncoding, boolean isText, ISystemOperationMonitor archiveOperationMonitor);

	/**
	 * Extracts the directory <code>dir</code> (and its children) from 
	 * the archive and places the results in the directory <code>destinationParent</code>.
	 * The results will be named destination.getName() rather than <code>dir</code>'s name.
	 * @param dir The full name of the virtual directory to extract
	 * @param destinationParent A handle to the directory in which the extracted
	 * directory will be placed as a subdirectory.
	 * @param destination A handle to the directory that will be created. Whatever
	 * contents are in that directory will be replaced with what is extracted from
	 * the archive.
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true iff the extraction is successful
	 */
	public boolean extractVirtualDirectory(String dir, File destinationParent, File destination, ISystemOperationMonitor archiveOperationMonitor);

	/**
	 * Extracts the directory <code>dir</code> (and its children) from 
	 * the archive and places the results in the directory <code>destinationParent</code>.
	 * The results will be named destination.getName() rather than <code>dir</code>'s name.
	 * Extracts to the native encoding (if <code>isText</code>), but assumes
	 * that the source was archived using <code>sourceEncoding</code>.
	 * @param dir The full name of the virtual directory to extract
	 * @param destinationParent A handle to the directory in which the extracted
	 * directory will be placed as a subdirectory.
	 * @param destination A handle to the directory that will be created. Whatever
	 * contents are in that directory will be replaced with what is extracted from
	 * the archive.
	 * @param sourceEncoding The encoding of the files in the archive.
	 * @param isText Whether or not the files to be extracted in the directory are all text files
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true iff the extraction is successful
	 */
	public boolean extractVirtualDirectory(String dir, File destinationParent, File destination, String sourceEncoding, boolean isText, ISystemOperationMonitor archiveOperationMonitor);

	/**
	 * Compresses the file <code>file</code> and adds it to the archive,
	 * placing it in the virtual directory <code>virtualPath</code>. Pass the
	 * name as the parameter <code>name</code>. If the virtual path does not exist
	 * in the archive, create it. If <code>file</code> is a directory, copy it and
	 * its contents into the archive, maintaining the tree structure.
	 * @param file the file to be added to the archive
	 * @param virtualPath the destination of the file
	 * @param name the name of the result virtual file
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true if and only if the add was successful
	 */	 
	public boolean add(File file, String virtualPath, String name, ISystemOperationMonitor archiveOperationMonitor);

	/**
	 * Compresses the file <code>file</code> and adds it to the archive,
	 * saving it in the encoding specified by <code>encoding</code> if the isText is true.
	 * placing it in the virtual directory <code>virtualPath</code>. Pass the
	 * name as the parameter <code>name</code>. If the virtual path does not exist
	 * in the archive, create it. If <code>file</code> is a directory, copy it and
	 * its contents into the archive, maintaining the tree structure.
	 * @param file the file to be added to the archive
	 * @param virtualPath the destination of the file
	 * @param name the name of the result virtual file
	 * @param sourceEncoding the encoding of the source file
	 * @param targetEncoding the encoding of the result file
	 * @param isText is the file a text file
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true if and only if the add was successful
	 */	 
	public boolean add(File file, String virtualPath, String name, String sourceEncoding, String targetEncoding, boolean isText, ISystemOperationMonitor archiveOperationMonitor);
	
	/**
	 * Compresses the bytes in the InputStream <code>stream</code> and adds them as an entry to the archive,
	 * saving them in the encoding specified by <code>encoding</code> if <code>isText</code> is true, and
	 * placing it in the virtual directory <code>virtualPath</code>. Pass the
	 * name as the parameter <code>name</code>. If the virtual path does not exist
	 * in the archive, create it. 
	 * @param stream the InputStream to be added as an entry to the archive
	 * @param virtualPath the destination of the stream
	 * @param name the name of the result virtual file
	 * @param sourceEncoding the encoding of the source stream
	 * @param targetEncoding the encoding of the result file
	 * @param isText is the file a text file
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true if and only if the add was successful
	 */	 
	public boolean add(InputStream stream, String virtualPath, String name, String sourceEncoding, String targetEncoding, boolean isText, ISystemOperationMonitor archiveOperationMonitor);
	
	/**
	 * Compresses the file <code>file</code> and adds it to the archive,
	 * saving it in the encoding specified by <code>encoding</code> if the isText is true.
	 * placing it in the virtual directory <code>virtualPath</code>. Pass the
	 * name as the parameter <code>name</code>. If the virtual path does not exist
	 * in the archive, create it. If <code>file</code> is a directory, copy it and
	 * its contents into the archive, maintaining the tree structure.
	 * @param file the file to be added to the archive
	 * @param virtualPath the destination of the file
	 * @param name the name of the result virtual file
	 * @param sourceEncoding the encoding of the source file
	 * @param targetEncoding the encoding of the result file
	 * @param typeRegistery file transfer mode (binary or text) of this file
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true if and only if the add was successful
	 */	 
	public boolean add(File file, String virtualPath, String name, String sourceEncoding, String targetEncoding, ISystemFileTypes typeRegistery, ISystemOperationMonitor archiveOperationMonitor);
	
	/**
	 * A generalization of the add method.
	 * Compresses the array of files <code>files</code> and adds each of them to the archive, placing them 
	 * in the virtual directory <code>virtualPath</code>. Pass the names of the files
	 * as the parameter <code>names</code>, where <code>files[i]</code> has the name <code>names[i]</code>.
	 * If the virtual path does not exist in the archive, create it.
	 * @param files the list of files to be added to the archive
	 * @param virtualPath the destination of the file
	 * @param names the names of the result virtual files
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true if and only if the add was successful
	 */
	public boolean add(File[] files, String virtualPath, String[] names, ISystemOperationMonitor archiveOperationMonitor);

	/**
	 * A generalization of the add method.
	 * Compresses the array of files <code>files</code> and adds each of them to the archive, placing them 
	 * in the virtual directory <code>virtualPath</code>. Save the i'th file in the i'th encoding (if isText[i] is true)
	 * specified by <code>encodings</code>. Pass the names of the files
	 * as the parameter <code>names</code>, where <code>files[i]</code> has the name <code>names[i]</code>.
	 * If the virtual path does not exist in the archive, create it.
	 * @param files the list of files to be added to the archive
	 * @param virtualPath the destination of the files
	 * @param names the names of the result virtual files
	 * @param sourceEncodings the encoding of the source files
	 * @param targetEncodings the encoding of the result files
	 * @param isText file transfer mode (binary or text) of the files
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true if and only if the add was successful
	 */
	public boolean add(File[] files, String virtualPath, String[] names, String[] sourceEncodings, String[] targetEncodings, boolean[] isText, ISystemOperationMonitor archiveOperationMonitor);
	
	/**
	 * Compress the file <code>file</code> and replace the virtual file 
	 * referred to by <code>fullVirtualName</code> with the compressed file.
	 * Pass the name of the file as the parameter <code>name</code>.
	 * @param fullVirtualName the path of the file to be replaced
	 * @param file the file to be added to the archive
	 * @param name the name of the file
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true if and only if the replace was successful
	 */
	public boolean replace(String fullVirtualName, File file, String name, ISystemOperationMonitor archiveOperationMonitor);

	/**
	 * Compress the InputStream <code>stream</code> and replace the virtual file 
	 * referred to by <code>fullVirtualName</code> with the compressed stream.
	 * Pass the name of the new entry as the parameter <code>name</code>, the
	 * encoding of the entry as <code>encoding</code> and whether or not the entry
	 * <code>isText</code> or not.
	 * @param fullVirtualName the path of the file to be replaced
	 * @param stream the InputStream to be added as an entry to the archive
	 * @param name the name of the result virtual file
	 * @param sourceEncoding the encoding of the source stream
	 * @param targetEncoding the encoding of the result file
	 * @param isText is the file a text file
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true if and only if the replace was successful
	 */
	public boolean replace(String fullVirtualName, InputStream stream, String name, String sourceEncoding, String targetEncoding, boolean isText, ISystemOperationMonitor archiveOperationMonitor);
	
	/**
	 * Deletes the entry <code>fullVirtualName</code> from the archive, and returns
	 * whether or not the deletion was successful.
	 * @param fullVirtualName the path of the file to be deleted
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true if and only if the replace was successful
	 */
	public boolean delete(String fullVirtualName, ISystemOperationMonitor archiveOperationMonitor);
	
	/**
	 * Renames the entry <code>fullVirtualName</code> to the new name
	 * <code>newName</code> while still leaving the entry in the same virtual
	 * directory. Returns true if and only if the rename was successful.
	 * @param fullVirtualName the path of the file to be renamed
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true if and only if the replace was successful
	 */
	public boolean rename(String fullVirtualName, String newName, ISystemOperationMonitor archiveOperationMonitor);
	
	/**
	 * Moves the entry <code>fullVirtualName</code> to the location
	 * specified by <code>destinationVirtualPath</code>, while leaving the entry with
	 * the same name as before. 
	 * @param fullVirtualName the path of the file to be renamed
	 * @param destinationVirtualPath the destination of the file to move to
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true if and only if the replace was successful
	 */ 
	public boolean move(String fullVirtualName, String destinationVirtualPath, ISystemOperationMonitor archiveOperationMonitor);
	
	/**
	 * Replaces the full name and path of the entry <code>fullVirtualName</code>
	 * with the new full name and path <code>newFullVirtualName</code>.
	 * @param fullVirtualName the path of the file to be renamed
	 * @param newFullVirtualName the full path of the virtual file name
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return true if and only if the replace was successful
	 */
	public boolean fullRename(String fullVirtualName, String newFullVirtualName, ISystemOperationMonitor archiveOperationMonitor);

	/**
	 * Extracts and returns the specified list of virtual files from the archive.
	 * @param fullNames The list of files to return
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return An array of handles to the extracted files. If fullNames has length 0
	 * then this method returns an array of length 0.
	 */
	public File[] getFiles(String[] fullNames, ISystemOperationMonitor archiveOperationMonitor);
	
	/**
	 * Creates a new, empty folder in the archive. If parent folders do not exist either, creates them.
	 * @param fullVirtualName The full name and path of the new folder within the virtual file system.
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return Whether or not the creation was successful.
	 */
	public boolean createFolder(String fullVirtualName, ISystemOperationMonitor archiveOperationMonitor);
	
	/**
	 * Creates a new, empty file in the archive. If parent folders do not exist either, creates them.
	 * @param fullVirtualName The full name and path of the new file within the virtual file system.
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return Whether or not the creation was successful.
	 */
	public boolean createFile(String fullVirtualName, ISystemOperationMonitor archiveOperationMonitor);
	
	/**
	 * Gets the archive-type specific standard name for the VirtualChild
	 * <code>vc</code>. For example, for Zips, if vc is a directory, then
	 * the standard name must end with a "/".
	 */
	public String getStandardName(VirtualChild vc);

	/**
	 * Searches for text within a virtual file in this archive.
	 * A good implementation will not actually extract the file to disk.
	 * @param fullVirtualName the virtual file to search.
	 * @param matcher the pattern matcher to use.
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return an array of match objects corresponding to lines where matches were found.
	 * Returns an empty array if there are no results.
	 */	
	public SystemSearchLineMatch[] search(String fullVirtualName, SystemSearchStringMatcher matcher, ISystemOperationMonitor archiveOperationMonitor);

	/**
	 * Gets the user-defined comment for a specific entry in the archive.
	 * @param fullVirtualName The entry who's comment is desired
	 * @return the comment as a String or "" if there is none
	 */
	public String getCommentFor(String fullVirtualName);
	
	/**
	 * Gets the amount of space taken up by a specific entry in the archive
	 * when it is in compressed form. Compare with getSizeFor(String) which gets
	 * the size of the entry after it is decompressed.
	 * @param fullVirtualName The entry who's compressed size is desired
	 * @return the compressed size of the specified entry, or 0 if the entry is not
	 * found. If the archive is not a compression type (ie. tar), return the same as getSizeFor(String). 
	 */
	public long getCompressedSizeFor(String fullVirtualName);
	
	/**
	 * Gets the method used to compress a specific entry in the archive.
	 * @param fullVirtualName The entry who's compression method is desired
	 * @return The compression method of the specified entry, or "" if none.
	 */
	public String getCompressionMethodFor(String fullVirtualName);

	/**
	 * @return The comment associated with this archive, or "" if there is none.
	 */
	public String getArchiveComment();
	
	/**
	 * Returns the classification for the entry with the given path.
	 * @param fullVirtualName the virtual name.
	 * @return the classification.
	 */
	public String getClassification(String fullVirtualName);
}