/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Jan 14, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * @author aniefer
 */
public class CPPLabel implements ILabel {
    private IASTStatement statement;
    /**
     * @param gotoStatement
     */
    public CPPLabel( IASTStatement statement ) {
        this.statement = statement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.ILabel#getLabelStatement()
     */
    public IASTLabelStatement getLabelStatement() {
        if( statement instanceof IASTLabelStatement )
            return (IASTLabelStatement) statement;
        
        // TODO find label statement
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
     */
    public String getName() {
        if( statement instanceof IASTLabelStatement )
            return ((IASTLabelStatement) statement).getName().toString();

        return ((IASTGotoStatement) statement).getName().toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    public char[] getNameCharArray() {
        if( statement instanceof IASTLabelStatement )
            return ((IASTLabelStatement) statement).getName().toCharArray();

        return ((IASTGotoStatement) statement).getName().toCharArray();    
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() {
        return CPPVisitor.getContainingScope( statement );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
     */
    public IASTNode getPhysicalNode() {
        return statement;
    }

    /**
     * @param labelStatement
     */
    public void setLabelStatement( IASTLabelStatement labelStatement ) {
        statement = labelStatement;
    }

}
