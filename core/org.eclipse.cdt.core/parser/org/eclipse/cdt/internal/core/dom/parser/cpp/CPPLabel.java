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

import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * @author aniefer
 */
public class CPPLabel implements ILabel, ICPPBinding {
    private IASTName statement;
    /**
     * @param gotoStatement
     */
    public CPPLabel( IASTName statement ) {
        this.statement = statement;
        ((CPPASTName)statement).setBinding( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDeclarations()
     */
    public IASTNode[] getDeclarations() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDefinition()
     */
    public IASTNode getDefinition() {
        return statement;
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
        return statement.toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return statement.toCharArray();
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
    public void setLabelStatement( IASTName labelStatement ) {
        statement = labelStatement;
    }

}
