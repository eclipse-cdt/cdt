/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.codan.core.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO: add description
 */
public class TestUtils {
	static final Pattern filePattern = Pattern.compile("file=\"(.*)\"");
	/**
	 * @param st
	 * @param testFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static File saveFile(InputStream st, File testFile)
			throws FileNotFoundException, IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(st));
		String line;
		PrintStream wr = new PrintStream(testFile);
		try {
			boolean print = false;
			while ((line = r.readLine()) != null) {
				if (line.contains("<code ")) {
					Matcher m = filePattern.matcher(line);
					if (m.find()) {
						String userFile = m.group(1);
						if (userFile.equals(testFile.getName())) {
							print = true;
						}
					}
				} else if (line.contains("</code>")) {
					print = false;
				} else if (print) {
					wr.println(line);
				}
			}
		} finally {
			wr.close();
		}
		return testFile;
	}

	/**
	 * @param clazz
	 * @return
	 * @throws IOException
	 */
	public static InputStream getJavaFileText(Class clazz) throws IOException {
		CodanCoreTestActivator plugin = CodanCoreTestActivator.getDefault();
		String classFile = clazz.getName().replaceAll("\\.", "/");
		classFile += ".java";
		InputStream st = null;

			if (plugin != null) {
				URL resource = plugin.getBundle().getResource(
						"src/" + classFile);
				st = resource.openStream();
			} else {
				st = clazz.getResourceAsStream(classFile);
			}

		return st;
	}
}
