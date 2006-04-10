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
import java.io.InputStream;

/**
 * Singleton class for obtaining various information about class files.
 */
public class ClassFileUtil {
	
	private static ClassFileUtil instance;

	/**
	 * Constructor.
	 */
	private ClassFileUtil() {
		super();
	}
	
	/**
	 * Returns the singleton instance.
	 * @return the singleton instance.
	 */
	public static final ClassFileUtil getInstance() {
		
		if (instance == null) {
			instance = new ClassFileUtil();
		}
		
		return instance;
	}
	
	/**
	 * Returns whether the class with the given path is runnable, i.e. whether it contains <code>public
	 * static void main (String[])</code> method.
	 * @param classFilePath the class file path.
	 * @return <code>true</code> if the class file is runnable, <code>false</code> otherwise.
	 * @throws IOException if an I/O error occurs.
	 */
	public boolean isRunnable(String classFilePath) throws IOException {
		File classFile = new File(classFilePath);
		return isRunnable(classFile);
	}
	
	/**
	 * Returns whether the class is runnable, i.e. whether it contains <code>public
	 * static void main (String[])</code> method.
	 * @param classFile the class file.
	 * @return <code>true</code> if the class file is runnable, <code>false</code> otherwise.
	 * @throws IOException if an I/O error occurs.
	 */
	public boolean isRunnable(File classFile) throws IOException {
		FileInputStream stream = new FileInputStream(classFile);
		return isRunnable(stream);
 	}
	
	/**
	 * Returns whether the class represented by the given input stream is runnable,
	 * i.e. whether it contains <code>public static void main (String[])</code> method.
	 * @param stream the input stream.
	 * @return <code>true</code> if the class file is runnable, <code>false</code> otherwise.
	 * @throws IOException if an I/O error occurs.
	 */
	public boolean isRunnable(InputStream stream) throws IOException {
		BasicClassFileParser parser = new BasicClassFileParser(stream);
		parser.parse();
		return parser.isExecutable();
	}
	
	/**
	 * Gets the qualified class name for the file with the given path.
	 * @param classFilePath the class file path.
	 * @throws IOException if an I/O error occurs.
	 */
	public String getQualifiedClassName(String classFilePath) throws IOException {
		File classFile = new File(classFilePath);
		return getQualifiedClassName(classFile);
	}
	
	/**
	 * Gets the qualified class name.
	 * @param classFile the class file.
	 * @throws IOException if an I/O error occurs.
	 */
	public String getQualifiedClassName(File classFile) throws IOException {
		FileInputStream stream = new FileInputStream(classFile);
		return getQualifiedClassName(stream);
	}
	
	/**
	 * Gets the qualified class name for the class represented by the given input stream.
	 * @param classFilePath the class file path.
	 * @throws IOException if an I/O error occurs.
	 */
	public String getQualifiedClassName(InputStream stream) throws IOException {
		BasicClassFileParser parser = new BasicClassFileParser(stream);
		parser.parse();
		return parser.getQualifiedClassName();
	}
}