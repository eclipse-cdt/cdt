/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty() 
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.files;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;

public class FileServiceTest extends RSEBaseConnectionTestCase {

	private IFileServiceSubSystem fss;
	private IFileService fs;
	private File tempDir;
	private String tempDirPath;
	private IProgressMonitor mon = new NullProgressMonitor();
	
	public void setUp() throws Exception {
		super.setUp();
		IHost localHost = getLocalSystemConnection();
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry(); 
		ISubSystem[] ss = sr.getServiceSubSystems(localHost, IFileService.class);
		for (int i=0; i<ss.length; i++) {
			if (ss[i] instanceof IFileServiceSubSystem) {
				fss = (IFileServiceSubSystem)ss[i];
				fs = fss.getFileService();
			}
		}
		try {
			 tempDir = File.createTempFile("rsetest","dir"); //$NON-NLS-1$ //$NON-NLS-2$
			 assertTrue(tempDir.delete());
			 assertTrue(tempDir.mkdir());
			 tempDirPath = tempDir.getAbsolutePath();
		} catch(IOException ioe) {
			assertTrue("Exception creating temp dir", false); //$NON-NLS-1$
		}
	}
	
	public void tearDown() throws Exception {
		try {
			fs.delete(tempDir.getParent(), tempDir.getName(), mon);
		} catch(SystemMessageException msg) {
			//ensure that super.tearDown() can run
			System.err.println("Exception on tearDown: "+msg.getLocalizedMessage()); //$NON-NLS-1$
		}
		super.tearDown();
	}
	
	public boolean isWindows() {
		return fss.getHost().getSystemType().isWindows(); 
	}
	
	public String getTestFileName() {
		//Return a filename for testing that exposes all characters valid on the file system
		if (!isWindows()) {
			//UNIX TODO: test embedded newlines
			return "a !@#${a}\"\' fi\tle\b\\%^&*()?_ =[]~+-'`;:,.|<>"; //$NON-NLS-1$
		}
		//Fallback: Windows TODO: test unicode
		return "a !@#${a}'` file%^&()_ =[]~+-;,."; //$NON-NLS-1$
	}
	
	public void testCaseSensitive() {
		//-test-author-:MartinOberhuber
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCaseSensitive")) return; //$NON-NLS-1$
		
		if (isWindows()) {
			assertFalse(fss.getSubSystemConfiguration().isCaseSensitive());
			assertFalse(fss.isCaseSensitive());
			assertFalse(fs.isCaseSensitive()); //FAIL due to bug 168586
		} else {
			assertTrue(fss.getSubSystemConfiguration().isCaseSensitive());
			assertTrue(fss.isCaseSensitive()); //FAIL due to bug 168596
			assertTrue(fs.isCaseSensitive());
		}
	}
	
	public void testCreateFile() throws SystemMessageException {
		//-test-author-:MartinOberhuber
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		String testName = getTestFileName();
		IHostFile hf = fs.createFile(tempDirPath, testName, mon);
		assertTrue(hf.exists());
		assertTrue(hf.canRead());
		assertTrue(hf.canWrite());
		assertEquals(hf.getName(), testName);
		assertEquals(hf.getParentPath(), tempDirPath);
		assertEquals(hf.getSize(), 0);
		long modDate = hf.getModifiedDate();
		assertTrue(modDate > 0);
		
		File theFile = new File(tempDir, testName); 
		assertTrue(theFile.exists());
		assertTrue(modDate == theFile.lastModified());
	}
	
	public void testCreateCaseSensitive() throws SystemMessageException {
		//-test-author-:MartinOberhuber
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateCaseSensitive")) return; //$NON-NLS-1$
		
		String testName = getTestFileName();
		String testName2 = testName.toUpperCase();
		IHostFile hf = fs.createFile(tempDirPath, testName, mon);
		if (fss.isCaseSensitive()) {
			//UNIX: uppercase version must be distinct
			IHostFile hf2 = fs.getFile(tempDirPath, testName2, mon);
			assertFalse(hf2.exists());
			hf2 = fs.createFolder(tempDirPath, testName2, mon);
			assertTrue(hf2.exists());
			assertTrue(hf2.isDirectory());
			assertFalse(hf.equals(hf2));
		} else {
			//Windows: uppercase version must be the same
			IHostFile hf2 = fs.getFile(tempDirPath, testName2, mon);
			assertTrue(hf2.exists());
			try {
				hf2 = fs.createFolder(tempDirPath, testName2, mon);
			} catch(SystemMessageException e) {
				//Windows cannot create a folder when the file is already there
				assertNotNull(e);
			}
			assertTrue(hf2.exists());
			assertFalse(hf2.isDirectory());
			assertEquals(hf.getModifiedDate(), hf2.getModifiedDate());
			assertEquals(hf.getSize(), hf2.getSize());
			//Different abstract path names but denote the same file
			//Should be equal since java.io.File treats them as equal
			assertEquals(new File(tempDirPath, testName), new File(tempDirPath, testName2));
			assertEquals(hf, hf2); //bug 168591: should be equal
		}
	}

}
