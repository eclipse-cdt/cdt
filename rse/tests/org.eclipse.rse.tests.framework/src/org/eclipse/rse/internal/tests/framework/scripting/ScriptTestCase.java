/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 *******************************************************************************/
package org.eclipse.rse.internal.tests.framework.scripting;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.rse.tests.framework.AnnotatingTestCase;

/**
 * All test scripts are instances of ScriptedTestCase.
 */
public class ScriptTestCase extends AnnotatingTestCase {
	
	private URL scriptLocation;
	private ScriptContext context;
	
	public ScriptTestCase(ScriptContext context, URL scriptLocation) {
		super("runScript"); //$NON-NLS-1$
		this.context = context;
		this.scriptLocation = scriptLocation;
	}
	
	public void runScript() {
		ScriptInterpreter interpreter = new ScriptInterpreter(context);
		try {
			InputStream in = scriptLocation.openStream();
			boolean success = interpreter.run(in);
			in.close();
			assertTrue(success);
		} catch (IOException e) {
			fail("error reading " + scriptLocation.getPath()); //$NON-NLS-1$
		}
	}
}
