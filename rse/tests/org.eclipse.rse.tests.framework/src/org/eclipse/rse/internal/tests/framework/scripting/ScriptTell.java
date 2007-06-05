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

/**
 * The Tell step shows an image on the current context when run.
 */
public class ScriptTell extends ScriptStep {
	
	private String text;

	public ScriptTell(String text, int lineNumber) {
		super(lineNumber);
		this.text = text;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.scripting.ScriptStep#run(org.eclipse.rse.tests.framework.scripting.ScriptContext)
	 */
	public void run(ScriptContext context) {
		context.tell(text);
	}
	
}
