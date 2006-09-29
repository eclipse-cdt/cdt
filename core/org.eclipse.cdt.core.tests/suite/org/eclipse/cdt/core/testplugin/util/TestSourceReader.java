/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.core.testplugin.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import junit.framework.Assert;

import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class TestSourceReader {

	/**
	 * Searches for the offset of the first occurrence of a string in a workspace file.
	 * @param lookfor string to be searched for
	 * @param fullPath full path of the workspace file
	 * @return the offset or -1
	 * @throws CoreException
	 * @since 4.0
	 */
	public static int indexOfInFile(String lookfor, Path fullPath) throws CoreException {
		ITextFileBufferManager fbm= FileBuffers.getTextFileBufferManager();
		fbm.connect(fullPath, new NullProgressMonitor());
		try {
			ITextFileBuffer buf= FileBuffers.getTextFileBufferManager().getTextFileBuffer(fullPath);
			Assert.assertTrue("Could not find " + fullPath.toString(),  buf.getModificationStamp() > 0);
			String content= buf.getDocument().get();
			int result= content.indexOf(lookfor);
			Assert.assertTrue("Could not find '" + lookfor + "' in " + fullPath.toString(), result >= 0);
			return result;
		}
		finally {
			fbm.disconnect(fullPath, new NullProgressMonitor());
		}
	}

	/**
	 * Reads a section in comments form the source of the given class. The section
	 * is started with '// {tag}' and ends with the first line not started by '//' 
	 * @since 4.0
	 */
	public static String readTaggedComment(Class clazz, final String tag) throws IOException {
	    IPath filePath= new Path("ui/" + clazz.getName().replace('.', '/') + ".java");
	    
	    InputStream in= FileLocator.openStream(CTestPlugin.getDefault().getBundle(), filePath, false);
	    LineNumberReader reader= new LineNumberReader(new InputStreamReader(in));
	    boolean found= false;
	    final StringBuffer content= new StringBuffer();
	    try {
	        String line= reader.readLine();
	        while (line != null) {
	        	line= line.trim();
	            if (line.startsWith("//")) {
	                line= line.substring(2);
	                if (found) {
	                    content.append(line);
	                    content.append('\n');
	                }
	                else {
	                    line= line.trim();
	                    if (line.startsWith("{" + tag)) {
	                        if (line.length() == tag.length()+1 ||
	                                !Character.isJavaIdentifierPart(line.charAt(tag.length()+1))) {
	                            found= true;
	                        }
	                    }
	                }
	            }
	            else if (found) {
	                break;
	            }
	            line= reader.readLine();
	        }
	    }
	    finally {
	        reader.close();
	    }
	    Assert.assertTrue("Tag '" + tag + "' is not defined inside of '" + filePath + "'.", found);
	    return content.toString();
	}

	/**
	 * Creates a file with content at the given path inside the given container. 
	 * If the file exists its content is replaced.
	 * @param container a container to create the file in
	 * @param filePath the path relative to the container to create the file at
	 * @param contents the content for the file
	 * @return a file object.
	 * @throws Exception
	 * @since 4.0
	 */    
	public static IFile createFile(IContainer container, IPath filePath, String contents) throws Exception {
		//Obtain file handle
		IFile file = container.getFile(filePath);
	
		InputStream stream = new ByteArrayInputStream(contents.getBytes());
		//Create file input stream
		if (file.exists()) {
			file.setContents(stream, false, false, new NullProgressMonitor());
		} 
		else {
			file.create(stream, false, new NullProgressMonitor());
		}
		return file;
	}
}
