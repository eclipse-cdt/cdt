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

package org.eclipse.rse.services.clientserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.java.BasicClassFileParser;


/**
 * This singleton class classifies a file on the remote system.
 */
public class SystemFileClassifier {
	
	private static SystemFileClassifier instance;

	/**
	 * Constructor.
	 */
	private SystemFileClassifier() {
		super();
	}
	
	/**
	 * Returns the singleton instance.
	 * @return the singleton instance.
	 */
	public static SystemFileClassifier getInstance() {
		
		if (instance == null) {
			instance = new SystemFileClassifier();
		}
		
		return instance;
	}
	
    /**
     * Classifies the file with the given absolute path. The absolute path can represent a virtual file.
     * By default, returns <code>"file"</code>.
     * @param absolutePath the absolute path.
     * @return the classification.
     */
    public String classifyFile(String absolutePath) {
    	
    	// first check if the absolute path is virtual
    	boolean isVirtual = ArchiveHandlerManager.isVirtual(absolutePath);
    	
    	// if virtual, classify using archive handler manager
    	if (isVirtual) {
    		return classifyVirtual(absolutePath);
    	}
    	// otherwise, we classify using our way
    	else {
    		return classifyNonVirtual(absolutePath);
    	}
    }
    
    /**
     * Classifies a virtual file with the given path.
     * @param absolutePath the absolute path of the virtual file.
     * @return the classification.
     */
    protected String classifyVirtual(String absolutePath) {
    	
    	// create an abolute virtual path object
    	AbsoluteVirtualPath avp = new AbsoluteVirtualPath(absolutePath);
		
		// get the path of the containing archive
		String archivePath = avp.getContainingArchiveString();
		
		// get the virtual part of the file path
		String virtualPath = avp.getVirtualPart();
		
		// get archive file
		File archiveFile = new File(archivePath);
		
		// get classification of virtual file
		return ArchiveHandlerManager.getInstance().getClassification(archiveFile, virtualPath);
    }
    
    /**
     * Classifies a non-virtual file with the given path.
     * @param absolutePath the absolute path of the file.
     * @return the classification.
     */
    protected String classifyNonVirtual(String absolutePath) {
    	
    	// default type
    	String type = "file";
    	
    	File file = new File(absolutePath);
    	
    	// check if file exists
    	if (!file.exists()) {
    		return type;
    	}
    	
    	// find out if we are on Windows
    	boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("win");
    	
    	// for Windows, we only detect *.exe and *.dll files
    	if (isWindows) 
    	{
    	    absolutePath = absolutePath.toLowerCase();
    		// classify *.class file
    		if (absolutePath.endsWith(".class")) {
    			type = classifyClassFile(absolutePath);
    		}
    		// *.exe files are binary executables
    		else if (absolutePath.endsWith(".exe")) {
    			type = "executable(binary)";
    		}
    		// *.dll files are of type "module"
    		else if (absolutePath.endsWith(".dll")) {
    			type = "module";
    		}
    		
    		return type;
    	}
    	
    	// get specified encoding if any
    	String encoding = System.getProperty("dstore.stdin.encoding");
    	
    	// otherwise, default to system encoding
    	if (encoding == null || encoding.equals("")) {
    		encoding = System.getProperty("file.encoding");
    	}
    	
    	// create command "sh -c file <absolutePath>"
    	String args[] = new String[3];
    	args[0] = "sh";
    	args[1] = "-c";
    	args[2] = "file " + absolutePath;
    		
    	BufferedReader poutReader = null;
    	
    	try {
        	Process childProcess = Runtime.getRuntime().exec(args);
        	InputStreamReader reader = new InputStreamReader(childProcess.getInputStream(), encoding);
        	poutReader = new BufferedReader(reader);
        	
        	// get line of output
        	String line = poutReader.readLine();
        	
        	if (line != null) {
        		line = line.trim();
        		
        		// classify from line of output
        		type = getClassification(absolutePath, line);
        		
        		// close stream
            	poutReader.close();
        		
            	// if it a symbolic link, then get the canonical path and classify it as well
        		if (type.equals("link")) {
        			String canonicalPath = file.getCanonicalPath();
        			return type + "(" + classifyNonVirtual(canonicalPath) + ")" + ":" + canonicalPath;
        		}
        		else {
        			return type;
        		}
        	}
    	}
    	catch (UnsupportedEncodingException e) {
    		// TODO: log it
    		return type;
    	}
    	catch (IOException e) {
    		// TODO: log it
    		return type;
    	}
    	
    	return type;
	}
    
    /**
     * Classifies from the given line of classification output.
     * @param absolutePath the absolute path of the file that was classified.
     * @param line the line of output to parse. 
     * @return the classification.
     */
    protected String getClassification(String absolutePath, String line) {
    	
    	// default type
    	String type = "file";
    	
    	// look for colon
    	int colon = line.indexOf(':');
    	
    	// name appears before colon
    	String name = line.substring(0, colon);
    	
    	// the full type appears after the colon
    	String fulltype = line.substring(colon + 1, line.length()).trim();
    	
    	// if it is a *.class file, then we look for main method and qulaified class name
    	// as part of the classification
    	if (name.endsWith(".class")) {
    		type = classifyClassFile(absolutePath);
    	}
    	
    	// check if it is a shared library
    	boolean matchesLib =	(fulltype.indexOf("shared object") > -1) ||
								(fulltype.indexOf("object module") > -1) ||
								(fulltype.indexOf("archive") > -1);

    	// check if it is an executable
    	boolean matchesExe = (fulltype.indexOf("executable") > -1);
    	
    	// check if it is a script
    	boolean matchesScript =  (fulltype.indexOf("script") > -1);
    	
       	// shared library
    	if (matchesLib) {
    		
    		// all *.a, *.so and *.so.* files are of type "module"
    		if (name.endsWith(".a") || name.endsWith(".so") || (name.indexOf(".so.") > 0)) {
    			type = "module";
    		}
    	}
    	
    	// a script file
    	else if (matchesScript) {
    		
    		// an executable script file
    		if (matchesExe) {
    			type = "executable(script)";
    		}
    		// non-executable script file
    		else {
    			type = "script";
    		}
    	}
    	
    	// binary executable
    	else if (matchesExe) {
    		type = "executable(binary)";
    	}
    	
    	// on iSeries we look for "OS/400 object" as a type
    	else if (fulltype.indexOf("OS/400 object") > -1) {
    		type = "OS/400 object";
    	}
    	
    	// finally, if the full type contains the symbolic link string, then type is simply "link"
    	else if (fulltype.startsWith("symbolic link to")) {
    		type = "link";
    	}
    	
    	return type;      
    }
    
    /**
     * Classifies a class file.
     * @param absolutePath the absolute path of the class file.
     * @return the classification.
     */
    protected String classifyClassFile(String absolutePath) {
    	
    	// default type
    	String type = "file";
    	
    	// input stream to file
		FileInputStream stream = null;
		
		// class file parser
		BasicClassFileParser parser = null;
		
		boolean isExecutable = false;
		
		try {
			stream = new FileInputStream(absolutePath);
			
			// use class file parser to parse the class file
			parser = new BasicClassFileParser(stream);
			parser.parse();
			
			// query if it is executable, i.e. whether it has main method
			isExecutable = parser.isExecutable();
		}
		catch (IOException e) {
			// TODO: log it
			
			// we assume not executable
			isExecutable = false;
		}
		
		// if it is executable, then also get qualified class name
		if (isExecutable) {
			type = "executable(java";
			
			String qualifiedClassName = parser.getQualifiedClassName();
			
			if (qualifiedClassName != null) {
    			type = type + ":" + qualifiedClassName;
			}
			
			type = type + ")";
		}
		
		return type;
    }
}