/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Fix 154874 - handle files with space or $ in the name
 * Martin Oberhuber (Wind River) - [199854][api] Improve error reporting for archive handlers
 * Martin Oberhuber (Wind River) - [141823] Local connection does not classify symbolic links
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
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;


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

    	// create an absolute virtual path object
    	AbsoluteVirtualPath avp = new AbsoluteVirtualPath(absolutePath);

		// get the path of the containing archive
		String archivePath = avp.getContainingArchiveString();

		// get the virtual part of the file path
		String virtualPath = avp.getVirtualPart();

		// get archive file
		File archiveFile = new File(archivePath);

		// get classification of virtual file
		try {
			return ArchiveHandlerManager.getInstance().getClassification(archiveFile, virtualPath);
		} catch (SystemMessageException e) {
			return "file"; //FIXME check fallback //$NON-NLS-1$
		}
    }

    /**
     * Classifies a non-virtual file with the given path.
     * @param absolutePath the absolute path of the file.
     * @return the classification.
     */
    protected String classifyNonVirtual(String absolutePath) {

    	// default type
    	String type = "file"; //$NON-NLS-1$

    	File file = new File(absolutePath);

    	// check if file exists
    	if (!file.exists()) {
    		return type;
    	}

    	// find out if we are on Windows
    	boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("win"); //$NON-NLS-1$ //$NON-NLS-2$

    	// for Windows, we only detect *.exe and *.dll files
    	if (isWindows)
    	{
    	    absolutePath = absolutePath.toLowerCase();
    		// classify *.class file
    		if (absolutePath.endsWith(".class")) { //$NON-NLS-1$
    			type = classifyClassFile(absolutePath);
    		}
    		// *.exe files are binary executables
    		else if (absolutePath.endsWith(".exe")) { //$NON-NLS-1$
    			type = "executable(binary)"; //$NON-NLS-1$
    		}
    		// *.dll files are of type "module"
    		else if (absolutePath.endsWith(".dll")) { //$NON-NLS-1$
    			type = "module"; //$NON-NLS-1$
    		}

    		return type;
    	}

    	// get specified encoding if any
    	String encoding = System.getProperty("dstore.stdin.encoding"); //$NON-NLS-1$

    	// otherwise, default to system encoding
    	if (encoding == null || encoding.equals("")) { //$NON-NLS-1$
    		encoding = System.getProperty("file.encoding"); //$NON-NLS-1$
    	}

    	// create command "sh -c file <absolutePath>"
    	String args[] = new String[3];
    	args[0] = "sh"; //$NON-NLS-1$
    	args[1] = "-c"; //$NON-NLS-1$
    	args[2] = "file " + PathUtility.enQuoteUnix(absolutePath); //$NON-NLS-1$

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
				if (type.equals("symbolic link")) { //$NON-NLS-1$
        			String canonicalPath = file.getCanonicalPath();
        			return type + "(" + classifyNonVirtual(canonicalPath) + ")" + ":" + canonicalPath; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
    	String type = "file"; //$NON-NLS-1$

    	String name = line;
    	String fulltype = ""; //$NON-NLS-1$

    	// Look for colon. Name appears before colon. Full type appears after the colon
    	int colon = line.indexOf(':');
    	if (colon >= 0) {
        	name = line.substring(0, colon);
        	fulltype = line.substring(colon + 1, line.length()).trim();
    	}

    	// if it is a *.class file, then we look for main method and qulaified class name
    	// as part of the classification
    	if (name.endsWith(".class")) { //$NON-NLS-1$
    		type = classifyClassFile(absolutePath);
    	}

    	// check if it is a shared library
    	boolean matchesLib =	(fulltype.indexOf("shared object") > -1) || //$NON-NLS-1$
								(fulltype.indexOf("object module") > -1) || //$NON-NLS-1$
								(fulltype.indexOf("archive") > -1); //$NON-NLS-1$

    	// check if it is an executable
    	boolean matchesExe = (fulltype.indexOf("executable") > -1); //$NON-NLS-1$

    	// check if it is a script
    	boolean matchesScript =  (fulltype.indexOf("script") > -1); //$NON-NLS-1$

       	// shared library
    	if (matchesLib) {

    		// all *.a, *.so and *.so.* files are of type "module"
    		if (name.endsWith(".a") || name.endsWith(".so") || (name.indexOf(".so.") > 0)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    			type = "module"; //$NON-NLS-1$
    		}
    	}

    	// a script file
    	else if (matchesScript) {

    		// an executable script file
    		if (matchesExe) {
    			type = "executable(script)"; //$NON-NLS-1$
    		}
    		// non-executable script file
    		else {
    			type = "script"; //$NON-NLS-1$
    		}
    	}

    	// binary executable
    	else if (matchesExe) {
    		type = "executable(binary)"; //$NON-NLS-1$
    	}

    	// on iSeries we look for "OS/400 object" as a type
    	else if (fulltype.indexOf("OS/400 object") > -1) { //$NON-NLS-1$
    		type = "OS/400 object"; //$NON-NLS-1$
    	}

    	// finally, if the full type contains the symbolic link string, then
		// type is simply "symbolic link"
    	else if (fulltype.startsWith("symbolic link to")) { //$NON-NLS-1$
    		type = "symbolic link"; //$NON-NLS-1$
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
    	String type = "file"; //$NON-NLS-1$

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
			return type;
		}

		// if it is executable, then also get qualified class name
		if (isExecutable) {
			type = "executable(java"; //$NON-NLS-1$

			String qualifiedClassName = parser.getQualifiedClassName();

			if (qualifiedClassName != null) {
    			type = type + ":" + qualifiedClassName; //$NON-NLS-1$
			}

			type = type + ")"; //$NON-NLS-1$
		}

		return type;
    }
}