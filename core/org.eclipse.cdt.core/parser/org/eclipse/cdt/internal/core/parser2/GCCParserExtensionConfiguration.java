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
public class GCCParserExtensionConfiguration implements
        IParserExtensionConfiguration {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.IParserExtensionConfiguration#allowRestrictPointerOperatorsCPP()
     */
    public boolean allowRestrictPointerOperatorsCPP() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.IParserExtensionConfiguration#supportTypeofUnaryExpressionsCPP()
     */
    public boolean supportTypeofUnaryExpressionsCPP() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.IParserExtensionConfiguration#supportAlignOfUnaryExpressionCPP()
     */
    public boolean supportAlignOfUnaryExpressionCPP() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.IParserExtensionConfiguration#supportExtendedTemplateSyntaxCPP()
     */
    public boolean supportExtendedTemplateSyntaxCPP() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.IParserExtensionConfiguration#supportMinAndMaxOperatorsCPP()
     */
    public boolean supportMinAndMaxOperatorsCPP() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.IParserExtensionConfiguration#supportStatementsInExpressions()
     */
    public boolean supportStatementsInExpressions() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.IParserExtensionConfiguration#supportGCCStyleDesignatorsC()
     */
    public boolean supportGCCStyleDesignatorsC() {
        return true;
    }

}
