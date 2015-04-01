/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wainer S. Moschetta (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.JUnit4TestAdapter;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class UNCPathConverterTest {
	private URI testURI;
	private IPath expectedPath;

	public UNCPathConverterTest(URI uri, IPath expectedPath) {
		this.testURI = uri;
		this.expectedPath = expectedPath;
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(UNCPathConverterTest.class);
	}

	@Parameters(name = "{index}: {0}")
	public static Collection<Object []> testToPathCases() throws URISyntaxException {
		return Arrays.asList(new Object [][] {
						// Without scheme
						{new URI("/foo/bar"), Path.fromOSString("/foo/bar")},
						// With Scheme, no authority
						{new URI("file", "/foo/bar", null), Path.fromOSString("/foo/bar")},
						// With scheme and host
						{new URI("http", "example.com", "/foo/bar", null), Path.fromOSString("//example.com/foo/bar")},
						 // With server-based authority
						{new URI("ssh", "user:password", "example.com", 8080, "/foo/bar", null, null), Path.fromOSString("//example.com/foo/bar")},
						 // With Registry-based authority
						{new URI("remotetools", "My Connection", "/foo/bar", null, null), Path.fromOSString("//My Connection/foo/bar")}}
				);
	}

	@Test
	public void testToPath() {
		assertEquals("Failed to convert an URI to Path", expectedPath, UNCPathConverter.toPath(testURI));
	}
}
