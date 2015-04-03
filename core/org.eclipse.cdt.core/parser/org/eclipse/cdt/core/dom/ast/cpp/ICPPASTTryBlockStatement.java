/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

/**
 * This interface represents the try block statement. try { //body } catch (Exc e )
 * { // handler } catch ( ... ) { }
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTTryBlockStatement extends IASTStatement {
	/**
	 * <code>BODY</code> is the body of the try block.
	 */
	public static final ASTNodeProperty BODY = new ASTNodeProperty("ICPPASTTryBlockStatement.BODY - Body of try block"); //$NON-NLS-1$

	/**
	 * Set try body.
	 * 
	 * @param tryBlock
	 *            <code>IASTStatement</code>
	 */
	public void setTryBody(IASTStatement tryBlock);

	/**
	 * Get try body.
	 * 
	 * @return <code>IASTStatement</code>
	 */
	public IASTStatement getTryBody();

	/**
	 * <code>CATCH_HANDLER</code> are the exception catching handlers.
	 */
	public static final ASTNodeProperty CATCH_HANDLER = new ASTNodeProperty(
			"ICPPASTTryBlockStatement.CATCH_HANDLER - Exception catching handlers"); //$NON-NLS-1$

	/**
	 * Add catch handler.
	 * 
	 * @param handler
	 *            <code>ICPPASTCatchHandler</code>
	 */
	public void addCatchHandler(ICPPASTCatchHandler handler);

	/**
	 * Get the catch handlers.
	 * 
	 * @return <code>ICPPASTCatchHandler []</code>
	 */
	public ICPPASTCatchHandler[] getCatchHandlers();
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTTryBlockStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTTryBlockStatement copy(CopyStyle style);
}
