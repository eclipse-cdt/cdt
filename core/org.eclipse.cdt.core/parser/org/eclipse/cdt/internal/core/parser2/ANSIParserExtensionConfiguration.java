/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.parser2;

/**
 * @author jcamelon
 */
public class ANSIParserExtensionConfiguration implements
        IParserExtensionConfiguration {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.IParserExtensionConfiguration#allowRestrictPointerOperatorsCPP()
     */
    public boolean allowRestrictPointerOperatorsCPP() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.IParserExtensionConfiguration#supportTypeofUnaryExpressionsCPP()
     */
    public boolean supportTypeofUnaryExpressionsCPP() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.IParserExtensionConfiguration#supportAlignOfUnaryExpressionCPP()
     */
    public boolean supportAlignOfUnaryExpressionCPP() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.IParserExtensionConfiguration#supportExtendedTemplateSyntaxCPP()
     */
    public boolean supportExtendedTemplateSyntaxCPP() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.IParserExtensionConfiguration#supportMinAndMaxOperatorsCPP()
     */
    public boolean supportMinAndMaxOperatorsCPP() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.IParserExtensionConfiguration#supportStatementsInExpressions()
     */
    public boolean supportStatementsInExpressions() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.IParserExtensionConfiguration#supportGCCStyleDesignatorsC()
     */
    public boolean supportGCCStyleDesignatorsC() {
        return false;
    }

}
