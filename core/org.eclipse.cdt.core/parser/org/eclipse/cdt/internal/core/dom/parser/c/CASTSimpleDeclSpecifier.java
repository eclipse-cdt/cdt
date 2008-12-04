/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;

/**
 * @author jcamelon
 */
public class CASTSimpleDeclSpecifier extends CASTBaseDeclSpecifier implements ICASTSimpleDeclSpecifier {
    
    private int simpleType;
    private boolean isSigned;
    private boolean isUnsigned;
    private boolean isShort;
    private boolean isLong;
    private boolean longlong;
    private boolean complex=false;
    private boolean imaginary=false;

    public int getType() {
        return simpleType;
    }

    public boolean isSigned() {
        return isSigned;
    }

    public boolean isUnsigned() {
        return isUnsigned;
    }

    public boolean isShort() {
        return isShort;
    }

    public boolean isLong() {
        return isLong;
    }
    
    public void setType(int type) {
        assertNotFrozen();
        simpleType = type;
    }
    
    public void setShort(boolean value) {
        assertNotFrozen();
        isShort = value;
    }
    
    public void setLong(boolean value) {
        assertNotFrozen();
        isLong = value;
    }
    
    public void setUnsigned(boolean value) {
        assertNotFrozen();
        isUnsigned = value;
    }
    
    public void setSigned(boolean value) {
        assertNotFrozen();
        isSigned = value;
    }

    public boolean isLongLong() {
        return longlong;
    }

    public void setLongLong(boolean value) {
        assertNotFrozen();
        longlong = value;
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

	public boolean isComplex() {
		return complex;
	}

	public void setComplex(boolean value) {
        assertNotFrozen();
		this.complex = value;
	}

	public boolean isImaginary() {
		return imaginary;
	}

	public void setImaginary(boolean value) {
        assertNotFrozen();
		this.imaginary = value;		
	}
}
