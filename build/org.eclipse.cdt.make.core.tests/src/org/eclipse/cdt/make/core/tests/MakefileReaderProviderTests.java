/*******************************************************************************
 * Copyright (c) 2008, 2011 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia (Ed Swartz) - initial API and implementation
 *     Wind River Systems - Bug 338936
 *******************************************************************************/
package org.eclipse.cdt.make.core.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.makefile.IMacroDefinition;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.makefile.IMakefileReaderProvider;
import org.eclipse.cdt.make.core.makefile.IRule;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class MakefileReaderProviderTests extends TestCase {
	private String[] inclDirs;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		String basePath = "";
		File baseDir = getPluginRelativeFile(new Path("data"));
		if (baseDir != null)
			basePath = baseDir.getAbsolutePath() + File.separator;
		inclDirs = new String[] {
			basePath + "bogus",
			basePath + "incl"
		};
	}

	public void testNoReaderProvider() throws Exception {
		IPath path = new Path("data/Makefile.main");
		File file = getPluginRelativeFile(path);
		// doesn't work in packaged plugin, which is fine
		if (file != null) {
			IMakefile makefile = MakeCorePlugin.createMakefile(
					URIUtil.toURI(file.getAbsolutePath()),
					true, inclDirs);
			assertMakefileContents(makefile);
		}
	}

	public void testNullReaderProvider() throws Exception {
		IPath path = new Path("data/Makefile.main");
		File file = getPluginRelativeFile(path);
		// doesn't work in packaged plugin, which is fine
		if (file != null) {
			IMakefile makefile = MakeCorePlugin.createMakefile(
					URIUtil.toURI(file.getAbsolutePath()), true, inclDirs, null);
			assertMakefileContents(makefile);
		}
	}

	public void testInputStreamReaderProvider() throws Exception {
		IPath path = new Path("Makefile.main");

		// get base directory for searches
		final URL url = getPluginRelativeURL(new Path("data").addTrailingSeparator());
		IMakefile makefile = MakeCorePlugin.createMakefile(
				URIUtil.toURI(path), true, inclDirs,
				new IMakefileReaderProvider() {

					@Override
					public Reader getReader(URI fileURI) throws IOException {
						URL fileUrl;
						try {
							fileUrl = new URL(url, fileURI.getPath());
						} catch (MalformedURLException e) {
							fileUrl = new URL("file", null, fileURI.getPath());
						}
						InputStream is = fileUrl.openStream();
						return new InputStreamReader(is);
					}

				});

		assertMakefileContents(makefile);
	}

	public void testInMemoryReaderProvider() throws Exception {
		IMakefile makefile = MakeCorePlugin.createMakefile(
				URIUtil.toURI("/memory/Makefile.main"), true, inclDirs,
				new IMakefileReaderProvider() {

					@Override
					public Reader getReader(URI fileURI) throws IOException {
						String name = new File(fileURI).getName();
						if (name.equals("Makefile.main"))
							return new StringReader(
									"VAR = foo\r\n" +
									"\r\n" +
									"include Makefile.incl\r\n" +
									"\r\n" +
									"main: $(VAR)\r\n" +
									"	nothing\r\n");
						if (name.equals("Makefile.incl"))
							return new StringReader(
									"INCLVAR = bar\r\n" +
									"\r\n" +
									"foo.o: .PHONY\r\n"
									);

						throw new FileNotFoundException(fileURI.getPath());
					}

				});

		assertMakefileContents(makefile);
	}

	public void testReaderIsClosed_Bug338936() throws Exception {
		final boolean[] streamIsClosed = { false };
		MakeCorePlugin.createMakefile(
				URIUtil.toURI("Makefile.main"), true, inclDirs,
				new IMakefileReaderProvider() {
					@Override
					public Reader getReader(URI fileURI) throws IOException {
						return new StringReader("") {
							@Override
							public void close() {
								super.close();
								streamIsClosed[0] = true;
							}
						};
					}

				});
		assertTrue("Stream is not closed", streamIsClosed[0]);
	}

	/**
	 * @param makefile
	 */
	private void assertMakefileContents(IMakefile makefile) {
		assertNotNull(makefile);
		IMacroDefinition[] macroDefinitions = makefile.getMacroDefinitions();
		assertNotNull(macroDefinitions);
		assertEquals(2, macroDefinitions.length);
		assertEquals("VAR", macroDefinitions[0].getName());
		assertEquals("INCLVAR", macroDefinitions[1].getName());

		IRule[] rules = makefile.getRules();
		assertEquals(2, rules.length);
		assertEquals("main", rules[0].getTarget().toString());
		assertEquals("foo.o", rules[1].getTarget().toString());
	}

	/**
	 * Try to get a file in the development version of a plugin --
	 * will return <code>null</code> for a jar-packaged plugin.
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private File getPluginRelativeFile(IPath path) throws Exception {
		URL url = getPluginRelativeURL(path);
		assertNotNull(url);
		if (url.getProtocol().equals("file"))
			return new File(url.getPath());
		return null;
	}

	private URL getPluginRelativeURL(IPath path) throws Exception {
		if (MakeTestsPlugin.getDefault() != null) {
			URL url = FileLocator.find(
					MakeTestsPlugin.getDefault().getBundle(),
					path, null);
			return url != null ? FileLocator.toFileURL(url) : null;
		}
		else {
			return new URL("file", null, path.toFile().getAbsolutePath());
		}
	}

	public static Test suite() {
		return new TestSuite(MakefileReaderProviderTests.class);
	}
}
