/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Represents a label statement.
 * 
 * @author Doug Schaefer
 */
public interface IASTLabelStatement extends IASTStatement, IASTNameOwner {

	public static final ASTNodeProperty NAME = new ASTNodeProperty("IASTLabelStatement.NAME - name for IASTLabelStatement"); //$NON-NLS-1$
    public static final ASTNodeProperty NESTED_STATEMENT = new ASTNodeProperty( "IASTLabelStatement.NESTED_STATEMENT - statement for IASTLabelStatement" ); //$NON-NLS-1$

	/**
	 * The name for the label. The name resolves to an ILabel binding.
	 * 
	 * @return the name for the label
	 */
	public IASTName getName();

	/**
	 * Set the name for a label.
	 * 
	 * @param name
	 */
	public void setName(IASTName name);

    /**
     * @return
     */
    public IASTStatement getNestedStatement();
    /**
     * @param s
     */
    public void setNestedStatement( IASTStatement s );

}
