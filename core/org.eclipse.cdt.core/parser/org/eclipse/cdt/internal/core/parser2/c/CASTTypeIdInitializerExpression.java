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

import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;

/**
 * @author jcamelon
 */
public class CASTTypeIdInitializerExpression extends CASTNode implements
        ICASTTypeIdInitializerExpression {

    private IASTTypeId t;
    private IASTInitializer i;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression#getTypeId()
     */
    public IASTTypeId getTypeId() {
        return t;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression#setTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
     */
    public void setTypeId(IASTTypeId typeId) {
        t = typeId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression#getInitializer()
     */
    public IASTInitializer getInitializer() {
        return i;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression#setInitializer(org.eclipse.cdt.core.dom.ast.IASTInitializer)
     */
    public void setInitializer(IASTInitializer initializer) {
        i = initializer;
    }

}
