/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/ 

package org.eclipse.cdt.core.testplugin.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

/**
 * Utilities for reading test source code from plug-in .java sources
 */
public class TestSourceReader {

	/**
	 * Returns an array of StringBuffer objects for each comment section found preceding the named
	 * test in the source code. 
	 * @param bundle the bundle containing the source, if null can try to load using classpath (source folder has to be in the classpath for this to work)
	 * @param srcRoot the directory inside the bundle containing the packages
	 * @param clazz the name of the class containing the test
	 * @param testName the name of the test
	 * @param sections the number of comment sections preceding the named test to return
	 * @return an array of StringBuffer objects for each comment section found preceding the named
	 * test in the source code. 
	 * @throws IOException
	 */
	public static StringBuffer[] getContentsForTest(Bundle bundle, String srcRoot, Class clazz, final String testName, int sections) throws IOException {
		String fqn = clazz.getName().replace('.', '/');
		fqn = fqn.indexOf("$")==-1 ? fqn : fqn.substring(0,fqn.indexOf("$"));
		String classFile = fqn + ".java";
		IPath filePath= new Path(srcRoot + '/' + classFile);
	
		InputStream in;
		try {
			if (bundle != null)
				in = FileLocator.openStream(bundle, filePath, false);
			else {
				in = clazz.getResourceAsStream('/'+classFile);
			}
		} catch(IOException e) {
			if(clazz.getSuperclass()!=null && !clazz.equals(TestCase.class)) {
		    	return getContentsForTest(bundle, srcRoot, clazz.getSuperclass(), testName, sections);
		    }
			throw e;
		}
	    
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    
	    List contents = new ArrayList();
	    StringBuffer content = new StringBuffer();
	    for(String line = br.readLine(); line!=null; line = br.readLine()) {
	    	line = line.replaceFirst("^\\s*", ""); // replace leading whitespace, preserve trailing
	    	if(line.startsWith("//")) {
	    		content.append(line.substring(2)+"\n");
	    	} else {
	    		if(content.length()>0) {
	    			contents.add(content);
	    			if(contents.size()==sections+1)
	    				contents.remove(0);
	    			content = new StringBuffer();
	    		}
	    		int idx= line.indexOf(testName);
	    		if( idx != -1 && !Character.isJavaIdentifierPart(line.charAt(idx+testName.length()))) {
	    			return (StringBuffer[]) contents.toArray(new StringBuffer[contents.size()]);
	    		}
	    	}
	    }
	    
	    if(clazz.getSuperclass()!=null && !clazz.equals(TestCase.class)) {
	    	return getContentsForTest(bundle, srcRoot, clazz.getSuperclass(), testName, sections);
	    }
	    throw new IOException("Test data not found for "+clazz+" "+testName);	
	}
	
	/**
	 * Searches for the offset of the first occurrence of a string in a workspace file.
	 * @param lookfor string to be searched for
	 * @param fullPath full path of the workspace file
	 * @return the offset or -1
	 * @throws Exception 
	 * @throws UnsupportedEncodingException 
	 * @since 4.0
	 */
	public static int indexOfInFile(String lookfor, Path fullPath) throws Exception {
		IFile file= ResourcesPlugin.getWorkspace().getRoot().getFile(fullPath);
		Reader reader= new BufferedReader(new InputStreamReader(file.getContents(), file.getCharset()));
		Assert.assertTrue(lookfor.indexOf('\n') == -1);
		try {
			int c= 0;
			int offset= 0;
			StringBuffer buf= new StringBuffer();
			while ((c = reader.read()) >= 0) {
				buf.append((char) c);
				if (c == '\n') {
					int idx= buf.indexOf(lookfor);
					if (idx >= 0) {
						return idx+offset;
					}
					offset+=buf.length();
					buf.setLength(0);
				}
			}
			int idx= buf.indexOf(lookfor);
			if (idx >= 0) {
				return idx+offset;
			}
			return -1;
		} finally {
			reader.close();
		}
	}
	
	public static int getLineNumber(int offset, Path fullPath) throws Exception {
		IFile file= ResourcesPlugin.getWorkspace().getRoot().getFile(fullPath);
		Reader reader= new BufferedReader(new InputStreamReader(file.getContents(), file.getCharset()));
		try {
			int line = 1;
			for (int i = 0; i < offset; i++) {
				int c= reader.read();
				Assert.assertTrue(c >= 0);
				if (c == '\n')
					line++;
			}
			return line;
		} finally {
			reader.close();
		}
	}

	/**
	 * Reads a section in comments form the source of the given class. The section
	 * is started with '// {tag}' and ends with the first line not started by '//' 
	 * @since 4.0
	 */
	public static String readTaggedComment(Bundle bundle, String srcRoot, Class clazz, final String tag) throws IOException {
	    IPath filePath= new Path(srcRoot + '/' + clazz.getName().replace('.', '/') + ".java");
	    
	    InputStream in= FileLocator.openStream(bundle, filePath, false);
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
	                } else {
	                    line= line.trim();
	                    if (line.startsWith("{" + tag)) {
	                        if (line.length() == tag.length()+1 ||
	                                !Character.isJavaIdentifierPart(line.charAt(tag.length()+1))) {
	                            found= true;
	                        }
	                    }
	                }
	            } else if (found) {
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
	 * @throws CoreException 
	 * @throws Exception
	 * @since 4.0
	 */    
	public static IFile createFile(final IContainer container, final IPath filePath, final String contents) throws CoreException {
		final IWorkspace ws = ResourcesPlugin.getWorkspace();
		final IFile result[] = new IFile[1];
		ws.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				//Obtain file handle
				IFile file = container.getFile(filePath);

				InputStream stream = new ByteArrayInputStream(contents.getBytes());
				//Create file input stream
				if (file.exists()) {
					long timestamp= file.getLocalTimeStamp();
					file.setContents(stream, false, false, new NullProgressMonitor());
					if (file.getLocalTimeStamp() == timestamp) {
						file.setLocalTimeStamp(timestamp+1000);
					}
				} else {
					createFolders(file);
					file.create(stream, true, new NullProgressMonitor());
				}
				result[0]= file;
			}
			private void createFolders(IResource res) throws CoreException {
				IContainer container= res.getParent();
				if (!container.exists() && container instanceof IFolder) {
					createFolders(container);
					((IFolder) container).create(true, true, new NullProgressMonitor());
				}
			}
		}, null);
		return result[0];
	}

	/**
	 * Creates a file with content at the given path inside the given container. 
	 * If the file exists its content is replaced.
	 * @param container a container to create the file in
	 * @param filePath the path relative to the container to create the file at
	 * @param contents the content for the file
	 * @return 
	 * @return a file object.
	 * @throws Exception 
	 * @throws Exception
	 * @since 4.0
	 */    
	public static IFile createFile(IContainer container, String filePath, String contents) throws CoreException {
		return createFile(container, new Path(filePath), contents);
	}
	
	/**
	 * Waits until the given file is indexed. Fails if this does not happen within the
	 * given time. 
	 * @param file
	 * @param maxmillis
	 * @throws Exception
	 * @since 4.0
	 */
	public static void waitUntilFileIsIndexed(IIndex index, IFile file, int maxmillis) throws Exception {
		long endTime= System.currentTimeMillis() + maxmillis;
		int timeLeft= maxmillis;
		while (timeLeft >= 0) {
			Assert.assertTrue(CCorePlugin.getIndexManager().joinIndexer(timeLeft, new NullProgressMonitor()));
			index.acquireReadLock();
			try {
				IIndexFile pfile= index.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
				if (pfile != null && pfile.getTimestamp() >= file.getLocalTimeStamp()) {
					return;
				}
				pfile= index.getFile(ILinkage.C_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
				if (pfile != null && pfile.getTimestamp() >= file.getLocalTimeStamp()) {
					return;
				}
			} finally {
				index.releaseReadLock();
			}
			
			Thread.sleep(50);
			timeLeft= (int) (endTime - System.currentTimeMillis());
		}
		Assert.fail("Indexing " + file.getFullPath() + " did not complete in time!");
	}

    public static IASTTranslationUnit createIndexBasedAST(IIndex index, ICProject project, IFile file) throws CModelException, CoreException {
    	ICElement elem= project.findElement(file.getFullPath());
    	if (elem instanceof ITranslationUnit) {
    		ITranslationUnit tu= (ITranslationUnit) elem;
   			return tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
    	}
    	Assert.fail("Could not create ast for " + file.getFullPath());
    	return null;
    }
}
