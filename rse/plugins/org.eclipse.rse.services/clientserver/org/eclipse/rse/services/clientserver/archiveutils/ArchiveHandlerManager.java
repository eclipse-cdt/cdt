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
 ********************************************************************************/

package org.eclipse.rse.services.clientserver.archiveutils;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * This class manages all the Archive Handlers that correspond to the archive file that the system
 * would like to deal with. It contains methods for registering handlers with file types, as well as
 * utilities for getting at the contents of archives that the Manager represents.
 * This class is designed to be a singleton class, so the best way to use it is
 * to use statements of the form "ArchiveHandlerManager.getInstance().method".
 * @author mjberger
 */
public class ArchiveHandlerManager 
{
	//	The string that separates the virtual part of an absolute path from the real part
	public static final String VIRTUAL_SEPARATOR = "#virtual#/";
	public static final String VIRTUAL_CANONICAL_SEPARATOR = "#virtual#";
	
	//	the singleton instance
	protected static ArchiveHandlerManager _instance = new ArchiveHandlerManager(); 

	// a mapping from Files to ISystemArchiveHandlers
	protected HashMap _handlers;
	
	// a mapping from Strings (file extensions) to Classes (the type of handler to use)
	protected HashMap _handlerTypes;
	
	/**
	 * @return The singleton instance of this class.
	 */
	public static ArchiveHandlerManager getInstance()
	{
		return _instance;
	}
	
	public ArchiveHandlerManager()
	{
		_handlers = new HashMap();
		_handlerTypes = new HashMap();
	}

	/**
	 * Returns the children of an object in the virtual file system.
	 * @param file The archive in whose virtual file system the children reside.
	 * @param virtualpath The parent virtual object whose children this method is to return. To
	 * get the top level virtualchildren in the archive, set virtualpath to "" or null.
	 * @return An array of VirtualChild objects representing the children of the virtual object
	 * in <code>file</code> referred to by <code>virtualpath</code>. If no class implementing 
	 * ISystemArchiveHandler can be found that corresponds to file, then this method returns null.
	 * If the virtual object has no children, this method also returns null.
	 * @throws IOException if there was a problem getting the registered handler for the
	 * file. This usually means the archive is corrupted.
	 */	
	public VirtualChild[] getContents(File file, String virtualpath) throws IOException
	{
		if (virtualpath == null) virtualpath = "";
		ISystemArchiveHandler handler = getRegisteredHandler(file);
		if (handler == null || !handler.exists()) throw new IOException();	
		return handler.getVirtualChildren(virtualpath);	
	}

	/**
	 * Returns the children of an object in the virtual file system that are folders.
	 * @param file The archive in whose virtual file system the children reside.
	 * @param virtualpath The parent virtual object whose children this method is to return. To
	 * get the top level virtualchildren in the archive, set virtualpath to "" or null.
	 * @return An array of VirtualChild objects representing the children of the virtual object
	 * in <code>file</code> referred to by <code>virtualpath</code> that are themselves folders. 
	 * If no class implementing ISystemArchiveHandler can be found that corresponds to file, then 
	 * this method returns null. If the virtual object has no children, this method also returns null.
	 */	
	public VirtualChild[] getFolderContents(File file, String virtualpath)
	{
		if (virtualpath == null) virtualpath = "";
		ISystemArchiveHandler handler = getRegisteredHandler(file);
		if (handler == null) return null;
		return handler.getVirtualChildFolders(virtualpath);	
	}
	
	/**
	 * Tests whether a file is an known type of archive.
	 * @param file the file to test.
	 * @return true if and only if the file is an archive whose
	 * type is registered with the ArchiveHandlerManager.
	 */
	public boolean isArchive(File file)
	{
		if (_handlers.containsKey(file))
		{
			return true;
		}
		else
		{
			if (_handlerTypes.containsKey(getExtension(file)))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		
	}
	
	/**
	 * Tests whether a file is an known type of archive, based on the file name.
	 * @param file the name of the file to test.
	 * @return true if and only if the file is an archive whose
	 * type is registered with the ArchiveHandlerManager.
	 */
	public boolean isRegisteredArchive(String filename)
	{
		if (_handlerTypes.containsKey(getExtension(filename)))
		{
			return true;
		}
		else
		{
			return false;
		}
	}	
	/** 
	 * @param file the file whose extension we are computing.
	 * @return the extension of <code>file</code>. "Extension" is
	 * defined as any letters in the filename after the last ".". 
	 * Returns "" if there is no extension.
	 */
	protected String getExtension(File file)
	{
		String filename = file.getName();
		int i = filename.lastIndexOf(".");
		if (i == -1) return "";
		return filename.substring(i+1).toLowerCase();
	}
	
	/** 
	 * @param filename the name of the file whose extension we are computing.
	 * @return the extension of <code>filename</code>. "Extension" is
	 * defined as any letters in the filename after the last ".". 
	 * Returns "" if there is no extension.
	 */
	protected String getExtension(String filename)
	{
		int i = filename.lastIndexOf(".");
		if (i == -1) return "";
		return filename.substring(i+1).toLowerCase();
	}
	
	/**
	 * Given the absolute path to a virtual object, returns that object
	 * as a VirtualChild.
	 * @param fullyQualifiedName The absolute path to the object. Usually consists
	 * of the fullyQualifiedName of the archive, followed by the virtual path separator
	 * (defined in ArchiveHandlerManager.VIRTUAL_SEPARATOR) followed by the virtual path to
	 * the object within the archive's virtual file system.
	 */
	public VirtualChild getVirtualObject(String fullyQualifiedName)
	{
		String cleanName = cleanUpVirtualPath(fullyQualifiedName);
		AbsoluteVirtualPath avp = new AbsoluteVirtualPath(cleanName);
		if (!avp.isVirtual()) return new VirtualChild("", new File(avp.getContainingArchiveString()));
		String zipfile = avp.getContainingArchiveString();
		File file = new File(zipfile);
		ISystemArchiveHandler handler = getRegisteredHandler(file);
		if (handler == null) return new VirtualChild(avp.getVirtualPart(), new File(avp.getContainingArchiveString()));
		VirtualChild vc = handler.getVirtualFile(avp.getVirtualPart());
		return vc;
	}

	/**
	 * Returns the registered handler for the File <code>file</code>. If
	 * no handler exists for that file yet, create it. If the extension of
	 * <code>file</code> is not registered, then returns null.
	 */	
	public ISystemArchiveHandler getRegisteredHandler(File file)
	{
		ISystemArchiveHandler handler = null;
		if (_handlers.containsKey(file))
		{
			handler = (ISystemArchiveHandler) _handlers.get(file);
		}
		
		if (handler != null && handler.exists())
		{
		    return handler;
		}
		else {
			// find registered handler based on file's extension
			String ext = getExtension(file);
			if (!_handlerTypes.containsKey(ext))
			{
				System.out.println("Unknown archive file type: " + ext);
				return null;
			}
			else
			{
				Class handlerType = (Class) _handlerTypes.get(ext);
				Constructor newHandlerType = getProperConstructor(handlerType);
				Object[] files = new Object[1];
				files[0] = file;
				try
				{
					handler = (ISystemArchiveHandler) newHandlerType.newInstance(files);
				}
				catch (InvocationTargetException e)
				{
					Throwable target = e.getCause();
					System.out.println(e.getMessage());
					e.printStackTrace();
					System.out.println("Could not instantiate handler for " + file.getName());
					return null;

				}
				catch (Exception e)
				{
					System.out.println(e.getMessage());
					System.out.println("Could not instantiate handler for " + file.getName());
					return null;
				}
				_handlers.put(file, handler);
				return handler;
			}
		}
	}

	public Constructor getProperConstructor(Class handlerType)
	{
		Constructor[] constructors = handlerType.getConstructors();
		for (int i = 0; i < constructors.length; i++)
		{
			if (constructors[i].getParameterTypes().length != 1)
			{
				continue;
			}
			else if (!constructors[i].getParameterTypes()[0].equals(File.class))
			{
				continue;
			}
			else return constructors[i];
		}
		return null; // should never get to this point
	}

	/**
	 * Registers an extension and a handler type.
	 * @param ext The extension to register with the ArchiveHandlerManager
	 * @param handlerType The class of handler to register with <code>ext</code>. 
	 * Note that any class passed in must implement ISystemArchiveHandler.
	 * @return Whether or not the registration was successful. 
	 */
	public boolean setRegisteredHandler(String ext, Class handlerType)
	{
		if (!handlerHasProperConstructor(handlerType))
		{
			System.out.println("Cannot register archive handler " + handlerType);
			System.out.println(handlerType + " does not contain a constructor whose signature is 'Constructor(File file)'");
			return false;
		}
		if (handlerImplementsISystemArchiveHandler(handlerType))
		{
			if (_handlerTypes.containsKey(ext)) _handlerTypes.remove(ext); 
			_handlerTypes.put(ext, handlerType);
			return true;
		}
		else
		{
			System.out.println("Cannot register archive handler " + handlerType);
			System.out.println("Neither " + handlerType + ", nor any of its superclasses implements ISystemArchiveHandler.");
			return false;
		}
	}

	/**
	 * Returns whether or not handlerType has a constructor that takes only one
	 * parameter, a java.io.File.
	 */
	protected boolean handlerHasProperConstructor(Class handlerType)
	{
		Constructor[] constructors = handlerType.getConstructors();
		boolean ok = false;
		for (int i = 0; i < constructors.length; i++)
		{
			if (constructors[i].getParameterTypes().length == 1)
			{
				if (constructors[i].getParameterTypes()[0].equals(File.class))
				{
					ok = true;
					break;
				}
			}
		}
		return ok;
	}
	
	/**
	 * Returns whether or not handlerType or one of its superclasses implements ISystemArchiveHandler.
	 */
	protected boolean handlerImplementsISystemArchiveHandler(Class handlerType)
	{
		Class[] interfaces = handlerType.getInterfaces();
		boolean okay = false;
		for (int i = 0; i < interfaces.length; i++)
		{
			if (interfaces[i].getName().equals(ISystemArchiveHandler.class.getName())) okay = true;
		}
		if (!okay) 
		{
			Class superclass = handlerType.getSuperclass();
			if (superclass.getName().equals(Object.class.getName())) return false;
			return handlerImplementsISystemArchiveHandler(superclass);
		}
		else return true; 
	}
	
	/**
	 * Removes the handler associated with <code>file</code>, freeing the file
	 * to be used by other processes.
	 */
	public void disposeOfRegisteredHandlerFor(File file)
	{
		_handlers.remove(file);	
	}

	/**
	 * Tests whether the absolute path given by <code>path</code>
	 * refers to a virtual object.
	 * @param path
	 * @return True if and only if the absolute path refers to a virtual object.
	 */
	public static boolean isVirtual(String path)
	{
		return path.indexOf(VIRTUAL_CANONICAL_SEPARATOR) != -1;
	}
	
	/**
	 * Converts the virtual path given by <code>fullVirtualName</code>
	 * to the standard virtual form ('/' as separator, no leading or trailing '/'s)
	 * @param fullVirtualName the path to convert
	 * @return the new path in standard form
	 */
	public static String cleanUpVirtualPath(String fullVirtualName)
	{
		int j = fullVirtualName.indexOf(VIRTUAL_CANONICAL_SEPARATOR);
		if (j == -1 && fullVirtualName.indexOf(":") != -1) return fullVirtualName;
		String realPart = "";
		String newPath = fullVirtualName;
		if (j != -1)
		{
			realPart = fullVirtualName.substring(0, j) + VIRTUAL_SEPARATOR;
			newPath = fullVirtualName.substring(j + VIRTUAL_SEPARATOR.length());
		}
		// use only forward slash separator
		newPath = newPath.replace('\\', '/');

		//get rid of any double slashes
		int i = newPath.indexOf("//");
		while (i != -1)
		{
			newPath = newPath.substring(0,i) + newPath.substring(i+1);
			i = newPath.indexOf("//");
		}
		
		// get rid of any leading or trailing slashes
		if (j != -1 && newPath.startsWith("/")) newPath = newPath.substring(1);
		if (newPath.endsWith("/")) newPath = newPath.substring(0, newPath.length() - 1);
		return realPart + newPath;
	}

	/**
	 * Disposes of all registered handlers.
	 */	
	public void dispose()
	{
		_handlers.clear();
	}
	
	public boolean createEmptyArchive(File newFile)
	{
		if (!isRegisteredArchive(newFile.getName())) 
		{
			System.out.println("Could not create new archive.");
			System.out.println(newFile + " is not a registered type of archive.");
			return false;
		}
		
		if (newFile.exists())
		{
			if (!newFile.isFile())
			{
				System.out.println("Could not create new archive.");
				System.out.println(newFile + " is not a file.");
				return false;
			}
			if (!newFile.delete())
			{
				System.out.println("Could not create new archive.");
				System.out.println(newFile + " could not be deleted.");
				return false;
			}
		}
		
		try
		{	
			if (!newFile.createNewFile())
			{
				System.out.println("Could not create new archive.");
				System.out.println(newFile + " could not be created.");
				return false;
			}
		}
		catch (IOException e)
		{
			System.out.println("Could not create new archive.");
			System.out.println(e.getMessage());
			return false;
		}
		
		ISystemArchiveHandler handler = getRegisteredHandler(newFile);
		return handler.create();
	}
	
	/**
	 * Returns the extensions for archive types that have been registered
	 * with the ArchiveHandlerManager.
	 */
	public String[] getRegisteredExtensions()
	{
		Object[] exts = _handlerTypes.keySet().toArray();
		String[] extensions = new String[exts.length];
		for (int i = 0; i < exts.length; i++)
		{
			extensions[i] = (String) exts[i];
		}
		return extensions;
	}
	
	public String getComment(File archive)
	{
		ISystemArchiveHandler handler = getRegisteredHandler(archive);
		if (handler == null || !handler.exists()) return "";	
		return handler.getArchiveComment();	
	}
	
	public long getExpandedSize(File archive)
	{
		ISystemArchiveHandler handler = getRegisteredHandler(archive);
		if (handler == null || !handler.exists()) return 0;
		VirtualChild[] allEntries = handler.getVirtualChildrenList();
		int total = 0;
		for (int i = 0; i < allEntries.length; i++)
		{
			total += allEntries[i].getSize();
		}
		return total;
	}
	
	/**
	 * Returns the classification for the entry in a archive with the given virtual path.
	 * @param file the archive file.
	 * @param virtualPath the virtual path.
	 * @return the classification for the virtual file.
	 */
	public String getClassification(File file, String virtualPath) {
		
		// if archive file is null, or if it does not exist, or if the virtual path
		// is null, then return null for the classification
		if (file == null || !file.exists()) {
			return null;
		}
		
		// get archive handler
		ISystemArchiveHandler handler = getRegisteredHandler(file);
		
		if (handler == null || !handler.exists()) {
			return null;	
		}
		
		return handler.getClassification(virtualPath);
	}
}