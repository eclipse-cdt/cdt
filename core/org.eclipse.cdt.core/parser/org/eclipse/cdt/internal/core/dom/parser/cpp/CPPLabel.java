/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;

/**
 * @author aniefer
 */
public class CPPLabel implements ILabel, ICPPInternalBinding {
    private IASTName statement;
    /**
     * @param gotoStatement
     */
    public CPPLabel( IASTName statement ) {
        this.statement = statement;
        statement.setBinding( this );
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

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedName()
     */
    public String[] getQualifiedName() {
        return new String[] { getName() };
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedNameCharArray()
     */
    public char[][] getQualifiedNameCharArray() {
        return new char [] [] { getNameCharArray() };
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
     */
    public boolean isGloballyQualified() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public ICPPDelegate createDelegate( IASTName name ) {
        // TODO Auto-generated method stub
        return null;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDefinition(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDefinition(IASTNode node) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDeclaration(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDeclaration(IASTNode node) {
		// TODO Auto-generated method stub
		
	}
	public void removeDeclaration(IASTNode node) {
		
	}


}
