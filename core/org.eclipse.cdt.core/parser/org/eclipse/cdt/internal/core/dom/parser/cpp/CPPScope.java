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
 * Created on Nov 29, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
abstract public class CPPScope implements ICPPScope{
    public static class CPPScopeProblem extends ProblemBinding implements ICPPScope {
        public CPPScopeProblem( int id, char[] arg ) {
            super( id, arg );
        }
        public void addBinding( IBinding binding ) throws DOMException {
            throw new DOMException( this );
        }

        public IBinding getBinding( IASTName name ) throws DOMException {
            throw new DOMException( this );
        }

        public IScope getParent() throws DOMException {
            throw new DOMException( this );
        }

        public List find( String name ) throws DOMException {
            throw new DOMException( this );
        }
    }

    
	private IASTNode physicalNode;
	public CPPScope( IASTNode physicalNode ) {
		this.physicalNode = physicalNode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
	 */
	public IScope getParent() throws DOMException {
		return CPPVisitor.getContainingScope( physicalNode );
	}
	
	public IASTNode getPhysicalNode() throws DOMException{
		return physicalNode;
	}
}
