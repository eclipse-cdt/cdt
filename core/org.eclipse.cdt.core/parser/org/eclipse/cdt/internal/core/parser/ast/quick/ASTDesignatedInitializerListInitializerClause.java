/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.quick;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;

/**
 * @author jcamelon
 */
public class ASTDesignatedInitializerListInitializerClause
		extends
			ASTInitializerListInitializerClause
		implements
			IASTInitializerClause {

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
	 * @param initializerClauses
	 * @param designators
	 */
	public ASTDesignatedInitializerListInitializerClause(Kind kind, List initializerClauses, List designators) {
		super( kind, initializerClauses );
		this.designators = designators;
	}
}
