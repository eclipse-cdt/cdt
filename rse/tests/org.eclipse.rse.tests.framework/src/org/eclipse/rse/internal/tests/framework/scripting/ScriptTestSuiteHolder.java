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

import java.net.URL;
import java.text.MessageFormat;

import junit.framework.TestSuite;

import org.eclipse.rse.tests.framework.DelegatingTestSuiteHolder;
import org.osgi.framework.Bundle;

/**
 * A script test suite holder is holds a scripted test case which is present in a file 
 * referenced by the extension point.
 */
public class ScriptTestSuiteHolder extends DelegatingTestSuiteHolder {
		
	private TestSuite suite;
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.AbstractTestSuiteHolder#getTestSuite()
	 */
	public TestSuite getTestSuite() {
		if (suite == null) {
			String folderName = getStringValue("folder"); //$NON-NLS-1$
			if (folderName != null) {
				if (!folderName.endsWith("/")) { //$NON-NLS-1$
					folderName += "/"; //$NON-NLS-1$
				}
				String scriptName = getStringValue("script"); //$NON-NLS-1$
				if (scriptName == null) {
					scriptName = "script.txt"; //$NON-NLS-1$
				}
				Bundle bundle = getBundle();
				URL resourceLocation = bundle.getEntry(folderName);
				ScriptContext context = new ConsoleContext(System.out, resourceLocation);
				URL scriptLocation = context.getResourceURL(scriptName);
				ScriptTestCase test = new ScriptTestCase(context, scriptLocation);
				String title = MessageFormat.format("Script from folder {0}", new String[] {folderName}); //$NON-NLS-1$
				suite = new TestSuite(title);
				suite.addTest(test);
			} else {
				suite = new TestSuite("ERROR: Missing folder argument"); //$NON-NLS-1$
			}
		}
		return suite;
	}
}
