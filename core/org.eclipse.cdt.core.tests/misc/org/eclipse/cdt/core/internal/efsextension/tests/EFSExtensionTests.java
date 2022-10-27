/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.internal.efsextension.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.jupiter.api.Test;

/**
 * Tests the EFSExtensionManager and EFSExtensionProvider classes, as well as the EFSExtensionProvider extension point.
 *
 * @author crecoskie
 *
 */
public class EFSExtensionTests extends BaseTestCase5 {
	@Test
	public void testReplaceInRSEURI() {
		URI originalURI = null;
		URI expected = null;
		try {
			originalURI = new URI("rse", "dbgaix3.torolab.ibm.com", "/home/recoskie", null);
			expected = new URI("rse", "dbgaix3.torolab.ibm.com", "/home/recoskie/subdirectory", null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
		String workingDirPath = EFSExtensionManager.getDefault().getPathFromURI(originalURI);
		IPath path = new Path("subdirectory");
		IPath newPath = new Path(workingDirPath).append(path).makeAbsolute();
		URI uri = EFSExtensionManager.getDefault().createNewURIFromPath(originalURI, newPath.toString());

		assertEquals(expected, uri);
	}

	@Test
	public void testReplaceInUNIXURI() {
		URI originalURI = null;
		URI expected = null;
		try {
			originalURI = new URI("file", "/home/recoskie", null);
			expected = new URI("file", "/home/recoskie/subdirectory", null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
		String workingDirPath = EFSExtensionManager.getDefault().getPathFromURI(originalURI);
		IPath path = new Path("subdirectory");
		IPath newPath = new Path(workingDirPath).append(path).makeAbsolute();
		URI uri = EFSExtensionManager.getDefault().createNewURIFromPath(originalURI, newPath.toString());

		assertEquals(expected, uri);
	}

	@Test
	public void testReplaceInWindowsURI() {
		URI originalURI = null;
		URI expected = null;
		try {
			originalURI = new URI("file", "/c:/foo", null);
			expected = new URI("file", "/c:/foo/subdirectory", null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
		String workingDirPath = EFSExtensionManager.getDefault().getPathFromURI(originalURI);
		IPath path = new Path("subdirectory");
		IPath newPath = new Path(workingDirPath).append(path).makeAbsolute();
		URI uri = EFSExtensionManager.getDefault().createNewURIFromPath(originalURI, newPath.toString());

		assertEquals(expected, uri);
	}

	@Test
	public void testReplaceInMadeUpURI() {
		URI originalURI = null;
		URI expected = null;
		try {
			originalURI = new URI("myfile", "/c:/foo", null);
			expected = new URI("myfile", "/c:/foo/subdirectory", null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
		String workingDirPath = EFSExtensionManager.getDefault().getPathFromURI(originalURI);
		IPath path = new Path("subdirectory");
		IPath newPath = new Path(workingDirPath).append(path).makeAbsolute();
		URI uri = EFSExtensionManager.getDefault().createNewURIFromPath(originalURI, newPath.toString());

		assertEquals(expected, uri);
	}

	@Test
	public void testReplaceWithWindowsPathNoLeadingSlash() {
		URI originalURI = null;
		URI expected = null;
		try {
			originalURI = new URI("file", "/c:/foo", null);

			if (java.io.File.separatorChar == '\\') {
				expected = new URI("file", "/c:/foo/subdirectory", null);
			} else {
				// if we're not on Windows then backslash is not the path separator, and instead
				// is a valid filename character.  Using a backslash will result in it being escaped.
				expected = new URI("file", "/c:\\foo\\subdirectory", null);
			}
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}

		String newPath = "c:\\foo\\subdirectory";
		URI uri = EFSExtensionManager.getDefault().createNewURIFromPath(originalURI, newPath);

		assertEquals(expected, uri);
	}

	@Test
	public void testReplaceURIWithAuthority() {
		URI originalURI = null;
		URI expected = null;
		try {
			originalURI = new URI("rse", "recoskie@dbgaix3.torolab.ibm.com:1000", "/home/recoskie", null, null);
			expected = new URI("rse", "recoskie@dbgaix3.torolab.ibm.com:1000", "/home/recoskie/subdirectory", null,
					null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
		String workingDirPath = EFSExtensionManager.getDefault().getPathFromURI(originalURI);
		IPath path = new Path("subdirectory");
		IPath newPath = new Path(workingDirPath).append(path).makeAbsolute();
		URI uri = EFSExtensionManager.getDefault().createNewURIFromPath(originalURI, newPath.toString());

		assertEquals(expected, uri);
	}

	@Test
	public void testReplaceURIWithAuthority2() {
		URI originalURI = null;
		URI expected = null;
		try {
			originalURI = new URI("rse", "recoskie@dbgaix3.torolab.ibm.com:1000", "/home/recoskie", null, null);
			expected = new URI("rse", "recoskie", "dbgaix3.torolab.ibm.com", 1000, "/home/recoskie/subdirectory", null,
					null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
		String workingDirPath = EFSExtensionManager.getDefault().getPathFromURI(originalURI);
		IPath path = new Path("subdirectory");
		IPath newPath = new Path(workingDirPath).append(path).makeAbsolute();
		URI uri = EFSExtensionManager.getDefault().createNewURIFromPath(originalURI, newPath.toString());

		assertEquals(expected, uri);
	}

	@Test
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

	@Test
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

	@Test
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

	@Test
	public void testGetLinkedURI() {
		URI originalURI = null;
		try {
			originalURI = new URI("file", "/c:/foo", null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}

		URI uri = EFSExtensionManager.getDefault().getLinkedURI(originalURI);

		assertEquals(originalURI, uri);
	}

	@Test
	public void testGetMappedPath() {
		URI originalURI = null;
		try {
			originalURI = new URI("file", "/c:/foo", null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}

		String path = EFSExtensionManager.getDefault().getMappedPath(originalURI);

		if (Platform.getOS().equals(Platform.WS_WIN32)) {
			assertEquals(path, "c:/foo");
		} else {
			assertEquals(path, "/c:/foo");
		}
	}

	@Test
	public void testGetPathFromURI() {
		URI originalURI = null;
		try {
			originalURI = new URI("file", "/c:/foo", null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}

		String path = EFSExtensionManager.getDefault().getMappedPath(originalURI);

		if (Platform.getOS().equals(Platform.WS_WIN32)) {
			assertEquals(path, "c:/foo");
		} else {
			assertEquals(path, "/c:/foo");
		}
	}

	@Test
	public void testExtension() {
		URI originalURI = null;
		try {
			originalURI = new URI("EFSExtensionProviderTestsScheme", "/some/silly/path", null);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}

		assertTrue(EFSExtensionManager.getDefault().isVirtual(originalURI));
	}

}
