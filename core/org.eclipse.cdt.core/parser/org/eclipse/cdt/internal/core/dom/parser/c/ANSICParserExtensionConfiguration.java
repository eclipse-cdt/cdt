/**********************************************************************
 * Copyright (c) 2002, 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser.c;

/**
 * @author jcamelon
 */
public class ANSICParserExtensionConfiguration implements
        ICParserExtensionConfiguration {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.c.ICParserExtensionConfiguration#supportStatementsInExpressions()
     */
    public boolean supportStatementsInExpressions() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.c.ICParserExtensionConfiguration#supportGCCStyleDesignators()
     */
    public boolean supportGCCStyleDesignators() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.c.ICParserExtensionConfiguration#supportTypeofUnaryExpressions()
     */
    public boolean supportTypeofUnaryExpressions() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.c.ICParserExtensionConfiguration#supportAlignOfUnaryExpression()
     */
    public boolean supportAlignOfUnaryExpression() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.c.ICParserExtensionConfiguration#supportKnRC()
     */
	public boolean supportKnRC() {
		return false;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.c.ICParserExtensionConfiguration#supportGCCBuiltinSymbols()
     */
	public boolean supportGCCOtherBuiltinSymbols() {
		return false;
	}

}
