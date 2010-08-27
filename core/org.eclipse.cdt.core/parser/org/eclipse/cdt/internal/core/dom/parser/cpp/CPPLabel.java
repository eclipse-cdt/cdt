/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM Corporation) - Initial API and implementation 
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

public class CPPLabel extends PlatformObject implements ILabel, ICPPInternalBinding {
    private IASTName statement;

    public CPPLabel( IASTName statement ) {
        this.statement = statement;
        statement.setBinding( this );
    }

    public IASTNode[] getDeclarations() {
        return null;
    }

    public IASTNode getDefinition() {
        return statement;
    }

    public IASTLabelStatement getLabelStatement() {
        if( statement instanceof IASTLabelStatement )
            return (IASTLabelStatement) statement;
        
        // TODO find label statement
        return null;
    }

    public String getName() {
        return new String(getNameCharArray());
    }

    public char[] getNameCharArray() {
        return statement.getSimpleID();
    }

    public IScope getScope() {
        return CPPVisitor.getContainingScope( statement );
    }

    public IASTNode getPhysicalNode() {
        return statement;
    }


    public void setLabelStatement( IASTName labelStatement ) {
        statement = labelStatement;
    }

    public String[] getQualifiedName() {
        return new String[] { getName() };
    }

    public char[][] getQualifiedNameCharArray() {
        return new char [] [] { getNameCharArray() };
    }
    
    public boolean isGloballyQualified() {
        return false;
    }

	public void addDefinition(IASTNode node) {
	}

	public void addDeclaration(IASTNode node) {
	}

	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	public IBinding getOwner() {
		return CPPVisitor.findEnclosingFunction(statement);
	}
}
