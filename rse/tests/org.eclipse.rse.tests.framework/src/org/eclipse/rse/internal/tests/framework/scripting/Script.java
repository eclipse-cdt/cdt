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
 * A Script is a collection of steps.
 */
public class Script extends SyntaxNode {
	
	private boolean failed = false;
	
	/**
	 * Creates a new script node. This is the base of the syntax tree for 
	 * scripts. These always have a line number of zero.
	 */
	public Script() {
		super(0);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.scripting.SyntaxNode#enter(org.eclipse.rse.tests.framework.scripting.ScriptContext)
	 */
	public void enter(ScriptContext context) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.scripting.SyntaxNode#leave(org.eclipse.rse.tests.framework.scripting.ScriptContext)
	 */
	public void leave(ScriptContext context) {
		failed = context.getFailed();
	}
	
	/**
	 * @return the failure state of the script.
	 */
	public boolean hasFailed() {
		return failed;
	}
	
}


