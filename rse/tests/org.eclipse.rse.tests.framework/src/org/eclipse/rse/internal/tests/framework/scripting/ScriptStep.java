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
 * A Step accomplishes a single action.  If the context is in fail mode then steps
 * are not executed.
 * show anImageName
 * tell aTextString
 * pause
 */
abstract class ScriptStep extends SyntaxNode {
	
	/**
	 * @param lineNumber The line number of this step. Used for diagnostics.
	 */
	public ScriptStep(int lineNumber) {
		super(lineNumber);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.scripting.SyntaxNode#enter(org.eclipse.rse.tests.framework.scripting.ScriptContext)
	 */
	public void enter(ScriptContext context) {
		if (context.getFailed()) return;
		run(context);
	}
	
	/**
	 * Perform the action to be done by this step.
	 * @param context the Context on which to perform the action.
	 */
	public abstract void run(ScriptContext context);
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.scripting.SyntaxNode#leave(org.eclipse.rse.tests.framework.scripting.ScriptContext)
	 */
	public final void leave(ScriptContext context) {
	}
	
}


