/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.parser2.cpp;

/**
 * @author jcamelon
 */
public class ANSICPPParserExtensionConfiguration implements
        ICPPParserExtensionConfiguration {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#allowRestrictPointerOperators()
     */
    public boolean allowRestrictPointerOperators() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportTypeofUnaryExpressions()
     */
    public boolean supportTypeofUnaryExpressions() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportAlignOfUnaryExpression()
     */
    public boolean supportAlignOfUnaryExpression() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportExtendedTemplateSyntax()
     */
    public boolean supportExtendedTemplateSyntax() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportMinAndMaxOperators()
     */
    public boolean supportMinAndMaxOperators() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportStatementsInExpressions()
     */
    public boolean supportStatementsInExpressions() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportComplexNumbers()
     */
    public boolean supportComplexNumbers() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration#supportRestrictKeyword()
     */
    public boolean supportRestrictKeyword() {
        return false;
    }


}
