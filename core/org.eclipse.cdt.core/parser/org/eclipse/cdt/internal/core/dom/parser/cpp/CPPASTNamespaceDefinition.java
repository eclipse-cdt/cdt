/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTNamespaceDefinition extends ASTNode implements
        ICPPASTNamespaceDefinition, IASTAmbiguityParent {

    private IASTName name;
  
    public CPPASTNamespaceDefinition() {
	}

	public CPPASTNamespaceDefinition(IASTName name) {
		setName(name);
	}

	public CPPASTNamespaceDefinition copy() {
		CPPASTNamespaceDefinition copy = new CPPASTNamespaceDefinition(name == null ? null : name.copy());
		for(IASTDeclaration declaration : getDeclarations())
			copy.addDeclaration(declaration == null ? null : declaration.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public IASTName getName() {
        return name;
    }

    public void setName(IASTName name) {
        assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(NAMESPACE_NAME);
		}
    }

    public IASTDeclaration [] getDeclarations() {
        if( declarations == null ) return IASTDeclaration.EMPTY_DECLARATION_ARRAY;
        return (IASTDeclaration[]) ArrayUtil.trim( IASTDeclaration.class, declarations );
    }

    public void addDeclaration(IASTDeclaration declaration) {
        assertNotFrozen();
        declarations = (IASTDeclaration[]) ArrayUtil.append( IASTDeclaration.class, declarations, declaration );
        if(declaration != null) {
        	declaration.setParent(this);
			declaration.setPropertyInParent(OWNED_DECLARATION);
        }
    }

    private IASTDeclaration [] declarations = new IASTDeclaration[32];

    public IScope getScope() {
	    try {
            return ((ICPPNamespace) name.resolveBinding()).getNamespaceScope();
        } catch ( DOMException e ) {
            return e.getProblem();
        }
	}

    @Override
	public boolean accept( ASTVisitor action ){
    	if (action.shouldVisitNamespaces && action instanceof ICPPASTVisitor) {
		    switch( ((ICPPASTVisitor)action).visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( name != null ) if( !name.accept( action ) ) return false;
        IASTDeclaration [] decls = getDeclarations();
        for ( int i = 0; i < decls.length; i++ )
            if( !decls[i].accept( action ) ) return false;    

    	if (action.shouldVisitNamespaces && action instanceof ICPPASTVisitor) {
    		    switch( ((ICPPASTVisitor)action).leave( this ) ){
    	            case ASTVisitor.PROCESS_ABORT : return false;
    	            case ASTVisitor.PROCESS_SKIP  : return true;
    	            default : break;
    	        }
    		}

        return true;
    }

	public int getRoleForName(IASTName n) {
		if( name == n ) return r_definition;
		return r_unclear;
	}

    public void replace(IASTNode child, IASTNode other) {
        if( declarations == null ) return;
        for( int i = 0; i < declarations.length; ++i )
        {
           if( declarations[i] == null ) break;
           if( declarations[i] == child )
           {
               other.setParent( child.getParent() );
               other.setPropertyInParent( child.getPropertyInParent() );
               declarations[i] = (IASTDeclaration) other;
           }
        }
    }
}
