/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
/*
 * Created on Dec 10, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.internal.core.index.IIndexType;

/**
 * @author aniefer
 */
public class CPPBasicType implements ICPPBasicType {
	
	protected int qualifierBits = 0;
	protected int type;
	protected IASTExpression value = null;
	
	public CPPBasicType( int t, int bits ){
		type = t;
		qualifierBits = bits;
		
		if( type == IBasicType.t_unspecified ){
			if( (qualifierBits & ( IS_LONG | IS_SHORT | IS_SIGNED | IS_UNSIGNED )) != 0 )
				type = IBasicType.t_int;
		}
	}

	public CPPBasicType( int t, int bits, IASTExpression val ){
		type = t;
		qualifierBits = bits;
		value = val;
	}
	
	public boolean isSameType( IType object ) {
		if( object == this )
			return true;
		
	    if( object instanceof ITypedef || object instanceof IIndexType)
	        return object.isSameType( this );
	    
		if( !(object instanceof CPPBasicType) )
			return false;
		
		if( type == -1 ) 
			return false;
		
		CPPBasicType t = (CPPBasicType) object;
		if( type != t.type )
			return false;
		
		if( type == IBasicType.t_int ){
			//signed int and int are equivalent
			return (qualifierBits & ~IS_SIGNED ) == (t.qualifierBits & ~IS_SIGNED );
		}
		return ( type == t.type && qualifierBits == t.qualifierBits );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#getType()
	 */
	public int getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#isSigned()
	 */
	public boolean isSigned() {
		return ( qualifierBits & IS_SIGNED ) != 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#isUnsigned()
	 */
	public boolean isUnsigned() {
		return ( qualifierBits & IS_UNSIGNED ) != 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#isShort()
	 */
	public boolean isShort() {
		return ( qualifierBits & IS_SHORT) != 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#isLong()
	 */
	public boolean isLong() {
		return ( qualifierBits & IS_LONG ) != 0;
	}
	
    public Object clone(){
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch ( CloneNotSupportedException e ) {
            //not going to happen
        }
        return t;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBasicType#getValue()
	 */
	public IASTExpression getValue() {
		return value;
	}
	
	public void setValue( IASTExpression val ){
		value = val;
	}

	public int getQualifierBits() {
		return qualifierBits;
	}
}
