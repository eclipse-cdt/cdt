/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.internal.efsextension.tests;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the EFSExtensionManager and EFSExtensionProvider classes, as well as the EFSExtensionProvider extension point.
 * 
 * @author crecoskie
 *
 */
public class EFSExtensionTests extends TestCase {
	public void testReplaceInRSEURI() {
		URI originalURI = null;
		URI expected = null;
		try {
			originalURI = new URI("rse", "dbgaix3.torolab.ibm.com", "/home/recoskie", null);
			expected = new URI("rse", "dbgaix3.torolab.ibm.com", "/home/recoskie/subdirectory", null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
		String workingDirPath = EFSExtensionManager.getDefault()
				.getPathFromURI(originalURI);
		IPath path = new Path("subdirectory");
		IPath newPath = new Path(workingDirPath).append(path).makeAbsolute();
		URI uri = EFSExtensionManager.getDefault()
				.createNewURIFromPath(originalURI, newPath.toString());
		
		assertEquals(expected, uri);
	}
	
	public void testReplaceInUNIXURI() {
		URI originalURI = null;
		URI expected = null;
		try {
			originalURI = new URI("file", "/home/recoskie", null);
			expected = new URI("file", "/home/recoskie/subdirectory", null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
		String workingDirPath = EFSExtensionManager.getDefault()
				.getPathFromURI(originalURI);
		IPath path = new Path("subdirectory");
		IPath newPath = new Path(workingDirPath).append(path).makeAbsolute();
		URI uri = EFSExtensionManager.getDefault()
				.createNewURIFromPath(originalURI, newPath.toString());
		
		assertEquals(expected, uri);
	}
	
	public void testReplaceInWindowsURI() {
		URI originalURI = null;
		URI expected = null;
		try {
			originalURI = new URI("file", "/c:/foo", null);
			expected = new URI("file", "/c:/foo/subdirectory", null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
		String workingDirPath = EFSExtensionManager.getDefault()
				.getPathFromURI(originalURI);
		IPath path = new Path("subdirectory");
		IPath newPath = new Path(workingDirPath).append(path).makeAbsolute();
		URI uri = EFSExtensionManager.getDefault()
				.createNewURIFromPath(originalURI, newPath.toString());
		
		assertEquals(expected, uri);
	}
	
	public void testReplaceInMadeUpURI() {
		URI originalURI = null;
		URI expected = null;
		try {
			originalURI = new URI("myfile", "/c:/foo", null);
			expected = new URI("myfile", "/c:/foo/subdirectory", null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
		String workingDirPath = EFSExtensionManager.getDefault()
				.getPathFromURI(originalURI);
		IPath path = new Path("subdirectory");
		IPath newPath = new Path(workingDirPath).append(path).makeAbsolute();
		URI uri = EFSExtensionManager.getDefault()
				.createNewURIFromPath(originalURI, newPath.toString());
		
		assertEquals(expected, uri);
	}
	
	public void testReplaceWithWindowsPathNoLeadingSlash() {
		URI originalURI = null;
		URI expected = null;
		try {
			originalURI = new URI("file", "/c:/foo", null);
			
			if(java.io.File.separatorChar == '\\') {
				expected = new URI("file", "/c:/foo/subdirectory", null);
			}
			else {
				// if we're not on Windows then backslash is not the path separator, and instead
				// is a valid filename character.  Using a backslash will result in it being escaped.
				expected = new URI("file", "/c:%5Cfoo%5Csubdirectory", null);
			}
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
		
		String newPath = "c:\\foo\\subdirectory";
		URI uri = EFSExtensionManager.getDefault()
				.createNewURIFromPath(originalURI, newPath);
		
		assertEquals(expected, uri);
	}
	
	public void testAppendinRSEURI() {
		URI originalURI = null;
		URI expected = null;
		try {
			originalURI = new URI("rse", "dbgaix3.torolab.ibm.com", "/home/recoskie", null);
			expected = new URI("rse", "dbgaix3.torolab.ibm.com", "/home/recoskie/subdirectory", null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
		
		URI uri = EFSExtensionManager.getDefault().append(originalURI, "subdirectory");
		
		assertEquals(expected, uri);
	}
	
	public void testAppendToUNIXURI() {
		URI originalURI = null;
		URI expected = null;
		try {
			originalURI = new URI("file", "/home/recoskie", null);
			expected = new URI("file", "/home/recoskie/subdirectory", null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
		
		URI uri = EFSExtensionManager.getDefault().append(originalURI, "subdirectory");
		
		assertEquals(expected, uri);
	}
	
	public void testAppendToWindowsURI() {
		URI originalURI = null;
		URI expected = null;
		try {
			originalURI = new URI("file", "/c:/foo", null);
			expected = new URI("file", "/c:/foo/subdirectory", null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
		
		URI uri = EFSExtensionManager.getDefault().append(originalURI, "subdirectory");
		
		assertEquals(expected, uri);
	}
	
	public void testGetLinkedURI() {
		URI originalURI = null;
		try {
			originalURI = new URI("file", "/c:/foo", null);
		} catch(URISyntaxException e) {
			fail(e.getMessage());
		}
		
		URI uri = EFSExtensionManager.getDefault().getLinkedURI(originalURI);
		
		assertEquals(originalURI, uri);
	}
	
	public void testGetMappedPath() {
		URI originalURI = null;
		try {
			originalURI = new URI("file", "/c:/foo", null);
		} catch(URISyntaxException e) {
			fail(e.getMessage());
		}
		
		String path = EFSExtensionManager.getDefault().getMappedPath(originalURI);
		
		assertEquals(path, "/c:/foo");
	}
	
	public void testGetPathFromURI() {
		URI originalURI = null;
		try {
			originalURI = new URI("file", "/c:/foo", null);
		} catch(URISyntaxException e) {
			fail(e.getMessage());
		}
		
		String path = EFSExtensionManager.getDefault().getMappedPath(originalURI);
		
		assertEquals(path, "/c:/foo");
	}
	
	public void testExtension() {
		URI originalURI = null;
		try {
			originalURI = new URI("EFSExtensionProviderTestsScheme", "/some/silly/path", null);
		} catch(URISyntaxException e) {
			fail(e.getMessage());
		}
		
		assertTrue(EFSExtensionManager.getDefault().isVirtual(originalURI));
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(EFSExtensionTests.class);
		return suite;
	}
}
