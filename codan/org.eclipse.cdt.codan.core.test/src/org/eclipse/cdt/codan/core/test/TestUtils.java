/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
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

/**
 * TODO: add description
 */
public class TestUtils {
	public static File saveFile(InputStream st, File testFile)
			throws FileNotFoundException, IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(st));
		String line;
		PrintStream wr = new PrintStream(testFile);
		try {
			while ((line = r.readLine()) != null) {
				wr.println(line);
			}
		} finally {
			wr.close();
		}
		return testFile;
	}

	public static String loadFile(InputStream st) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(st));
		String buffer;
		StringBuffer result = new StringBuffer();
		while ((buffer = br.readLine()) != null) {
			result.append(buffer);
		}
		st.close();
		return result.toString();
	}

	/**
	 * @param clazz
	 * @return
	 * @throws IOException
	 */
	public static InputStream getJavaFileText(String srcRoot, Class clazz)
			throws IOException {
		CodanCoreTestActivator plugin = CodanCoreTestActivator.getDefault();
		String classFile = clazz.getName().replaceAll("\\.", "/");
		classFile += ".java";
		InputStream st = null;
		if (plugin != null) {
			URL resource = plugin.getBundle().getResource(
					srcRoot + "/" + classFile);
			st = resource.openStream();
		} else {
			st = clazz.getResourceAsStream("/" + classFile);
		}
		return st;
	}
}
