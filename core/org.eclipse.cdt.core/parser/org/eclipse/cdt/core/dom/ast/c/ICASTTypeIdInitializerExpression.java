/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * C Expression of the format type-id { initializer }
 * 
 * @author jcamelon
 */
public interface ICASTTypeIdInitializerExpression extends IASTExpression {

	/**
	 * <code>TYPE_ID</code> represents the relationship between an
	 * <code>ICASTTypeIdInitializerExpression</code> and
	 * <code>IASTTypeId</code>.
	 */
	public static final ASTNodeProperty TYPE_ID = new ASTNodeProperty("ICASTTypeIdInitializerExpression.TYPE_ID - IASTTypeId for ICASTTypeIdInitializerExpression"); //$NON-NLS-1$

	/**
	 * <code>INITIALIZER</code> represents the relationship between an
	 * <code>ICASTTypeIdInitializerExpression</code> and
	 * <code>IASTInitializer</code>.
	 */
	public static final ASTNodeProperty INITIALIZER = new ASTNodeProperty(
			"ICASTTypeIdInitializerExpression.INITIALIZER - IASTInitializer for ICASTTypeIdInitializerExpression"); //$NON-NLS-1$

	/**
	 * Get the type-id.
	 * 
	 * @return <code>IASTTypeId</code>
	 */
	public IASTTypeId getTypeId();

	/**
	 * Set the typeId.
	 * 
	 * @param typeId
	 *            <code>IASTTypeId</code>
	 */
	public void setTypeId(IASTTypeId typeId);

	/**
	 * Get the initializer.
	 * 
	 * @return <code>IASTInitializer</code>
	 */
	public IASTInitializer getInitializer();

	/**
	 * Set the initializer.
	 * 
	 * @param initializer
	 *            <code>IASTInitializer</code>
	 */
	public void setInitializer(IASTInitializer initializer);

}
