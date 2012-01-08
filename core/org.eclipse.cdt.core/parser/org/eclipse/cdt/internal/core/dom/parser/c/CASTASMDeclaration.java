/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Yuan Zhang / Beth Tibbitts (IBM Research)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author jcamelon
 */
public class CASTASMDeclaration extends ASTNode implements IASTASMDeclaration {

    char [] assembly = null;

    public CASTASMDeclaration() {
	}

	public CASTASMDeclaration(String assembly) {
		setAssembly(assembly);
	}

	@Override
	public CASTASMDeclaration copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CASTASMDeclaration copy(CopyStyle style) {
		CASTASMDeclaration copy = new CASTASMDeclaration();
		copy.assembly = assembly == null ? null : assembly.clone();
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public String getAssembly() {
        if( assembly == null ) return ""; //$NON-NLS-1$
        return new String( assembly );
    }

    @Override
	public void setAssembly(String assembly) {
        assertNotFrozen();
        this.assembly = assembly == null ? null : assembly.toCharArray();
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclarations ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}

        if( action.shouldVisitDeclarations ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }
}
