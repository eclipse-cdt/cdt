/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.parser2.c;

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

}
