/**********************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 19, 2003
 */
package org.eclipse.cdt.core.indexer.tests;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorOperation;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.index.IIndexDelta;
import org.eclipse.cdt.core.index.IndexChangeEvent;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.internal.core.index.domsourceindexer.DOMSourceIndexer;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author bgheorgh
 */
public class DOMSourceIndexerPerfTests extends TestCase {
	IProject 				testProject;
	DOMSourceIndexer		sourceIndexer;
	IndexManager 			indexManager; 
	static long				timeC = 0;
	static long				timeCPP = 0;
	static int				countC = 0;
	static int				countCPP = 0;
	static final int 		MAXCOUNT = 10;
	
	/**
	 * @param name
	 */
	public DOMSourceIndexerPerfTests(String name) {
		super(name);
	}

	public void resetIndexer(final String indexerId){
		if ( testProject  != null) {
			ICDescriptorOperation op = new ICDescriptorOperation() {

				public void execute(ICDescriptor descriptor, IProgressMonitor monitor) throws CoreException {
						descriptor.remove(CCorePlugin.INDEXER_UNIQ_ID);
						descriptor.create(CCorePlugin.INDEXER_UNIQ_ID,indexerId);
				}
			};
			try {
				CCorePlugin.getDefault().getCDescriptorManager().runDescriptorOperation(testProject, op, new NullProgressMonitor());
				CCorePlugin.getDefault().getCoreModel().getIndexManager().indexerChangeNotification(testProject);
			} catch (CoreException e) {}
		}
	}
	 
	public static void main(String[] args) {}
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		testProject = CProjectHelper.createCCProject("IndexerTestProject", "bin").getProject(); //$NON-NLS-1$
		assertNotNull("Unable to create project", testProject);	 //$NON-NLS-1$
		
		resetIndexer(CCorePlugin.DEFAULT_INDEXER_UNIQ_ID);
		indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
		sourceIndexer = (DOMSourceIndexer) indexManager.getIndexerForProject(testProject);
	}
	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() {
		try {
			super.tearDown();
		} catch (Exception e1) {}
		//Delete project
		if (testProject.exists()) {
			try {
				System.gc();
				System.runFinalization();
				testProject.delete(true, null);
			} catch (CoreException e) {
				fail(getMessage(e.getStatus()));
			}
		}
	}

	/**
	 * Makes error message from multistatus
	 * 
	 * @param status
	 * @return
	 */
	private String getMessage(IStatus status) {
		StringBuffer message = new StringBuffer("["); //$NON-NLS-1$
		message.append(status.getMessage());
		if (status.isMultiStatus()) {
			IStatus children[] = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				message.append(getMessage(children[i]));
			}
		}
		message.append("]"); //$NON-NLS-1$
		return message.toString();
	}

	public static Test suite() {
		timeC = 0;
		timeCPP = 0;
		countC = 0;
		countCPP = 0;
		TestSuite suite = new TestSuite(DOMSourceIndexerPerfTests.class.getName());
		Test t1 = new DOMSourceIndexerPerfTests("testC");  //$NON-NLS-1$
		Test t2 = new DOMSourceIndexerPerfTests("testCPP");  //$NON-NLS-1$
		
		for (int i = 0; i < MAXCOUNT; i++) {
			suite.addTest(t1);
			suite.addTest(t2);
		}
		return suite;
	
	}
	/**
	 * Unpack archive contents to project directory
	 * 
	 * @param name - name of archive file w/o extension
	 * @param mask - pattern to check file extensions 
	 * @return - list of files which fit to pattern
	 */
	
  private ArrayList unzip(String name, String mask) {
		ArrayList lst = new ArrayList(20);  
		
	    byte[] buffer = new byte[512];
		String archname = "resources/zips/" + name + ".zip";
		ZipFile zipFile = null;
		
	    try {
			zipFile = new ZipFile(CTestPlugin.getDefault().getFileInPlugin(new Path(archname)));
			IPath projPath = testProject.getLocation();
			Enumeration entries = zipFile.entries();
		
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry)entries.nextElement();
				if (!entry.isDirectory()) {
					IPath entryPath = projPath.append(entry.getName());
					InputStream in = zipFile.getInputStream(entry);
					OutputStream out = new FileOutputStream(entryPath.toFile());
					for (int n = in.read(buffer); n >= 0; n = in.read(buffer))
						out.write(buffer, 0, n);
					in.close();
					out.close();
					if (entryPath.lastSegment().endsWith(mask)) {
						lst.add(entryPath.lastSegment());
					}				
				}
			}
			zipFile.close();
	    } catch (IOException e) { 
	    	fail("Unzip error with " + archname + " : " + e.getLocalizedMessage()); 
	    }
		return lst;
  }

  /**
   * Adds files from list to index and counts indexing time
   * 
   * @param lst - list of file names to be added
   * @return 	- time (in ms) spent for indexing
   */
  private long indexFiles(ArrayList lst) {
	    // wait for finish of other indexing processes, if any 
		while(indexManager.awaitingJobsCount()>0) {
			try { Thread.sleep(20);
			} catch (InterruptedException e) {}
		}
		System.gc();
		long t0 = System.currentTimeMillis();
		for (int i=0; i<lst.size(); i++) {
			IFile f = testProject.getProject().getFile((String)(lst.get(i)));
			sourceIndexer.addResource(testProject, f);
		}
		while(indexManager.awaitingJobsCount()>0) {
			try { Thread.sleep(20);
			} catch (InterruptedException e) {}
		}
		return (System.currentTimeMillis() - t0);
  }
  
  /**
   * Test C source indexation
   * @throws Exception
   */
  public void testC() throws Exception {
	  	long t0 = indexFiles(unzip("perf1", ".c"));
	  	timeC += t0; countC++; 
		System.out.println("Index performance: " + t0 + " ms on C source[" + countC + "]");
		if (countC == MAXCOUNT) {
			System.out.println("Average performance on " + MAXCOUNT + " C passes: " + (timeC/MAXCOUNT));			
		}
  }

  /**
   * Test C++ source indexation
   * @throws Exception
   */
  public void testCPP() throws Exception {
	  	long t0 = indexFiles(unzip("perf2", ".cpp"));
	  	timeCPP += t0; countCPP++; 
		System.out.println("Index performance: " + t0 + " ms on C++ source[" + countCPP + "]");
		if (countCPP == MAXCOUNT) {
			System.out.println("Average performance on " + MAXCOUNT + " C++ passes: " + (timeCPP/MAXCOUNT));			
		}
  }
}
