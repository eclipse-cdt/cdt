/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.newparser;

import java.io.FileReader;
import java.io.Reader;

public class Main {

	public static void main(String[] args) {
		
		String fileName = null;
		
		// Find the file
		for (int i = 0; i < args.length; ++i) {
			if (!args[i].startsWith("-"))
				fileName = args[i];
		}
		
		if (fileName == null) {
			System.out.println("Error: no files.");
			return;
		}
		
		Reader reader;
		try {
			reader = new FileReader(fileName);
		} catch (Exception e) {
			System.err.println(e);
			return;
		}
		
		Scanner scanner = new Scanner( reader, fileName );
		
		// Now pass on the preprocessing options
		for (int i = 0; i < args.length; ++i) {
			if (args[i].startsWith("-I")) {
				String dir = args[i].substring(2);
				scanner.addIncludePath(dir);
			} else if (args[i].startsWith("-D")) {
				int pos = args[i].indexOf('=');
				String name;
				String value = "";
				if (pos < 0) {
					name = args[i].substring(2);
				} else {
					name = args[i].substring(2, pos);
					value = args[i].substring(pos + 1);
				}
				scanner.addDefinition(name, value);
			}
		}
		
		Parser parser = null;
		try {
			parser = new Parser(scanner);
		} catch (Exception e) {
			System.out.println(e);
			return;
		}
		
		long startTime = System.currentTimeMillis();		
		try {
			parser.parse();
		} catch (Exception e) {
			System.err.println(e);
		}
		
		long time = System.currentTimeMillis() - startTime;
		
		System.out.println("done " + scanner.getCount() + " tokens in " + time	 + "ms.");
	}
}
