/**********************************************************************
 * Copyright (c) 2002, 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

/**
 * @author jcamelon
 */
public class GPPParserExtensionConfiguration implements
        ICPPParserExtensionConfiguration {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#allowRestrictPointerOperators()
     */
    public boolean allowRestrictPointerOperators() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportTypeofUnaryExpressions()
     */
    public boolean supportTypeofUnaryExpressions() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportAlignOfUnaryExpression()
     */
    public boolean supportAlignOfUnaryExpression() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportExtendedTemplateSyntax()
     */
    public boolean supportExtendedTemplateSyntax() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportMinAndMaxOperators()
     */
    public boolean supportMinAndMaxOperators() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportStatementsInExpressions()
     */
    public boolean supportStatementsInExpressions() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportComplexNumbers()
     */
    public boolean supportComplexNumbers() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportRestrictKeyword()
     */
    public boolean supportRestrictKeyword() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportLongLongs()
     */
    public boolean supportLongLongs() {
        return true;
    }

	/* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportKnRC()
     */
	public boolean supportKnRC() {
		return false;
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportGCCOtherBuiltinSymbols()
     */
	public boolean supportGCCOtherBuiltinSymbols() {
		return true;
	}

}
