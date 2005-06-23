/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.quick;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;

/**
 * @author jcamelon
 */
public class ASTDesignatedExpressionInitializerClause
		extends
			ASTExpressionInitializerClause implements IASTInitializerClause {

	private final List designators;
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTInitializerClause#getDesignators()
	 */
	public Iterator getDesignators() {
		if( designators == null ) return EmptyIterator.EMPTY_ITERATOR;
		return designators.iterator();
	}
	/**
	 * @param kind
	 * @param assignmentExpression
	 * @param designators
	 */
	public ASTDesignatedExpressionInitializerClause(Kind kind, IASTExpression assignmentExpression, List designators) {
		super(kind, assignmentExpression);
		this.designators = designators;
	}
}
