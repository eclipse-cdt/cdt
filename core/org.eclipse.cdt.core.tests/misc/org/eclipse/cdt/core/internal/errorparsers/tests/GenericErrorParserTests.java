/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.internal.errorparsers.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import junit.framework.TestCase;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * This test is designed to exercise the error parser capabilities.
 */
public class GenericErrorParserTests extends TestCase {
	protected IProject fTempProject;
	
	/**
	 * Constructor for IndexManagerTest.
	 * @param name
	 */
	public GenericErrorParserTests(String name) {
		super(name);
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();		
		fTempProject = ResourcesPlugin.getWorkspace().getRoot().getProject("temp-" + System.currentTimeMillis());
		if(!fTempProject.exists()) {
			fTempProject.create(new NullProgressMonitor());
		}
	}
	
	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() {
		try {
			super.tearDown();
		} catch (Exception ex) {
		}
		try {
			fTempProject.delete(true, true, new NullProgressMonitor());
		} catch (Exception ex) {
		}
	}
	
	protected IProject getTempProject() {
		return fTempProject;
	}

	protected String makeStringFromArray(String [] pieces, String sep) {
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < pieces.length; i++) {
			if(i != 0) {
				buf.append(sep);
			}
			buf.append(pieces[i]);
		}
		return buf.toString();
	}

	protected void transferInputStreamToOutputStream(InputStream input, OutputStream output, int byteBlockSize) throws IOException {
		byte [] buffer = new byte[byteBlockSize];	
		int     bytesRead;
		
		while((bytesRead = input.read(buffer)) >= 0) {
			output.write(buffer, 0, bytesRead);
		}
		
		buffer = null;
	}
	
	class FileNameComparator implements Comparator {
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object arg0, Object arg1) {
			try {
				IFile f0 = (IFile)arg0;
				IFile f1 = (IFile)arg1;
				return f0.getName().compareToIgnoreCase(f1.getName());
			} catch(Exception ex) {
				/* Ignore */
			}
			return 1;
		}
	}
	
	/**
	 * Expand and grow this class to make it more usefull.
	 */
	class CountingMarkerGenerator implements IMarkerGenerator {
		public int numErrors;
		public int numWarnings;
		public int numMarkers;
		public ArrayList uniqFiles;
		public String    lastDescription;
		private Comparator fFileNameComparator; 
		
		public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
			int index = Collections.binarySearch(uniqFiles, file, fFileNameComparator);
			if(index < 0) {
				uniqFiles.add(-1*(index + 1), file);	
			}
			
			if(severity == SEVERITY_WARNING) {
				numWarnings++;
			} else if(severity == SEVERITY_ERROR_BUILD || severity == SEVERITY_ERROR_RESOURCE) {
				numErrors++;
			}
			
			lastDescription = errorDesc;
			numMarkers++;
		}
		
		public CountingMarkerGenerator() {
			numErrors = 0;
			numWarnings = 0;
			uniqFiles = new ArrayList(0);
			fFileNameComparator = new FileNameComparator();
		}
	}

	/**
	 * This class allows us to run error parsers for files which don't
	 * really exist by just using the strings that come out as error codes.
	 */	
	class ImaginaryFilesErrorParserManager extends ErrorParserManager {
		IProject fProject;
		
		public ImaginaryFilesErrorParserManager(IProject project, IMarkerGenerator generator, String [] ids) {
			super(project, generator, ids);
			fProject = project;
		}
		
		public IFile findFileName(String fileName) {
			if(fileName.lastIndexOf('/') != -1) {
				fileName = fileName.substring(fileName.lastIndexOf('/') + 1);		
			}
			IFile file = fProject.getFile(fileName);
			if(!file.exists()) {
				try {
					InputStream stream = new StringBufferInputStream("TestFile");
					file.create(stream, true, new NullProgressMonitor());
					stream.close();
				} catch(Exception ex) {
					/* Ignore */
				}
			}
			return file;
		}
		
		protected IFile findFileInWorkspace(IPath path) {
			IFile file = fProject.getFile(path.lastSegment());
			if(!file.exists()) {
				try {
					InputStream stream = new StringBufferInputStream("TestFile");
					file.create(stream, true, new NullProgressMonitor());
					stream.close();
				} catch(Exception ex) {
					/* Ignore */
				}
			}
			return file;			
		}
	}
}
