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
 * Created on Dec 10, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;

/**
 * @author aniefer
 */
public class CPPBasicType implements ICPPBasicType {
	public static final int IS_LONG     = 1;
	public static final int IS_SHORT    = 1 << 1;
	public static final int IS_SIGNED   = 1 << 2;
	public static final int IS_UNSIGNED = 1 << 3;
	protected static final int LAST = IS_UNSIGNED;
	
	protected int qualifierBits = 0;
	protected int type;
	
	public CPPBasicType( int t, int bits ){
		type = t;
		qualifierBits = bits;
	}

	public boolean equals( Object object ) {
	    if( object instanceof CPPTypedef )
	        return object.equals( this );
	    
		if( !(object instanceof CPPBasicType) )
			return false;
		
		CPPBasicType t = (CPPBasicType) object;
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
}
