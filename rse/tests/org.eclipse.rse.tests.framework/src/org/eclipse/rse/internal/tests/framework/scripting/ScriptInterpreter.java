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

/**
 * An interpreter runs a script in a particular context.
 */
public class ScriptInterpreter extends SyntaxTreeVisitor {
	
	private ScriptContext context;
	private ScriptParserMessageList messageList = null;
	
	public ScriptInterpreter(ScriptContext context) {
		this.context = context;
	}
	
	/**
	 * @param in the InputStream to be interpreted
	 * @return true if the parsing and interpretation concluded successfully.
	 * @throws IOException
	 */
	public boolean run(InputStream in) throws IOException {
		messageList = new ScriptParserMessageList();
		ScriptParser p = new ScriptParser(messageList);
		Script script = p.parse(in);
		boolean success = false;
		if (script != null) {
			script.accept(this);
			success = !script.hasFailed();
		}
		return success;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.scripting.SyntaxTreeVisitor#enter(org.eclipse.rse.tests.framework.scripting.SyntaxNode)
	 */
	public void enter(SyntaxNode node) {
		node.enter(context);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.scripting.SyntaxTreeVisitor#leave(org.eclipse.rse.tests.framework.scripting.SyntaxNode)
	 */
	public void leave(SyntaxNode node) {
		node.leave(context);
	}
	
}
