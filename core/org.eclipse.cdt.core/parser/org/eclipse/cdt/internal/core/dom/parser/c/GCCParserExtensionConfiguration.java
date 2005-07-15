/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

/**
 * @author jcamelon
 */
public class GCCParserExtensionConfiguration implements
        ICParserExtensionConfiguration {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.c.ICParserExtensionConfiguration#supportStatementsInExpressions()
     */
    public boolean supportStatementsInExpressions() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.c.ICParserExtensionConfiguration#supportGCCStyleDesignators()
     */
    public boolean supportGCCStyleDesignators() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.c.ICParserExtensionConfiguration#supportTypeofUnaryExpressions()
     */
    public boolean supportTypeofUnaryExpressions() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.c.ICParserExtensionConfiguration#supportAlignOfUnaryExpression()
     */
    public boolean supportAlignOfUnaryExpression() {
        return true;
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.ICParserExtensionConfiguration#supportKRCSyntax()
	 */
	public boolean supportKnRC() {
		return true;
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.c.ICParserExtensionConfiguration#supportGCCOtherBuiltinSymbols()
     */
	public boolean supportGCCOtherBuiltinSymbols() {
		return true;
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.c.ICParserExtensionConfiguration#supportAttributeSpecifiers()
     */
	public boolean supportAttributeSpecifiers() {
		return true;
	}
	
}
