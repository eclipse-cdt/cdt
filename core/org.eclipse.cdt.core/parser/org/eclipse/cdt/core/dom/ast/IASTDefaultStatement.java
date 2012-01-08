/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is the default clause in the switch statement. Note that in the grammar,
 * a statement is part of the clause. For the AST, just go on to the next
 * statement to find it. It's really only there to ensure that there is at least
 * one statement following this clause.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTDefaultStatement extends IASTStatement {

	/**
	 * @since 5.1
	 */
	@Override
	public IASTDefaultStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTDefaultStatement copy(CopyStyle style);
}
