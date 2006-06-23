/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.quick;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;

/**
 * @author jcamelon
 */
public class ASTNewDescriptor implements IASTNewExpressionDescriptor {


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor#getNewInitializerExpressions()
	 */
	public Iterator getNewInitializerExpressions() {
		return EmptyIterator.EMPTY_ITERATOR;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor#getNewPlacementExpressions()
	 */
	public Iterator getNewPlacementExpressions() {
		return EmptyIterator.EMPTY_ITERATOR;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor#getTypeIdExpressions()
	 */
	public Iterator getNewTypeIdExpressions() {
		return EmptyIterator.EMPTY_ITERATOR;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor#freeReferences(org.eclipse.cdt.core.parser.ast.IReferenceManager)
	 */
	public void freeReferences() {
	}

}
