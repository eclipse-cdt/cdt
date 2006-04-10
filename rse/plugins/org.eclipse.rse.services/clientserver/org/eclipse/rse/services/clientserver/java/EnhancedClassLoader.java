/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.services.clientserver.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class loader is used to load classes given a root path.
 */
public class EnhancedClassLoader extends ClassLoader {
	
	private String rootPath;

	/**
	 * Constructor.
	 * @param rootPath the root path.
	 */
	public EnhancedClassLoader(String rootPath) {
		super();
		setRootPath(rootPath);
	}
	
	/**
	 * Returns the root path.
	 * @return the root path.
	 */
	public String getRootPath() {
		return rootPath;
	}
	
	/**
	 * Sets the root path.
	 * @param rootPath the root path.
	 */
	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}
	
	/**
	 * Finds the class with the given name.
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	protected Class findClass(String name) throws ClassNotFoundException {
		
		try {
			byte[] b = loadClassData(name);
			return defineClass(name, b, 0, b.length);
		}
		catch (IOException e) {
			throw new ClassNotFoundException();
		}
	}
	
	/**
	 * Returns the class data.
	 * @param name the name of the class.
	 * @return the contents of the class.
	 */
	private byte[] loadClassData(String name) throws IOException {
		
		// parent path
		String parentPath = rootPath;
		
		// system file separator
		String fileSep = System.getProperty("file.separator");
    	
		// add file separator to the parent path if it does not end with it
    	if (!parentPath.endsWith(fileSep)) {
    		parentPath = parentPath + fileSep;
    	}
    	
    	StringBuffer buf = new StringBuffer(parentPath);
    	
    	// replace '.' in class name with file separator 
    	for (int i = 0; i < name.length(); i++) {
    		char c = name.charAt(i);
    		
    		if (c == '.') {
    			buf.append(fileSep);
    		}
    		else {
    			buf.append(c);
    		}
    	}
		
		String filePath = buf.append(".class").toString();
		
		File file = new File(filePath);
		
		FileInputStream fileStream = new FileInputStream(file);
		
		int length = (int)(file.length());
		byte[] classData = new byte[length];
		
		int bytesRead = 0;
		int offset = 0;
		
		int available = fileStream.available();
			
		while (available > 0) {
			int bytesToRead = Math.min(available, 256000);
			bytesRead = fileStream.read(classData, offset, bytesToRead);
			
			if (bytesRead == -1) {
				break;
			}
			
			offset += bytesRead;
			
			available = fileStream.available();
		}
		
		return classData;
	}
}