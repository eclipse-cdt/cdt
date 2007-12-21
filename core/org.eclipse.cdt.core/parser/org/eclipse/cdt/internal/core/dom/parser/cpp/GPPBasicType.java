/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPBasicType;

/**
 * @author aniefer
 */
public class GPPBasicType extends CPPBasicType implements IGPPBasicType {
	private IType typeOf;
	
	public GPPBasicType( int type, int bits, IType typeOf ){
		super( type, bits );
		this.typeOf = typeOf;
		if( type == IBasicType.t_unspecified ){
			if((qualifierBits & ( IS_COMPLEX | IS_IMAGINARY )) != 0 )
				type = IBasicType.t_float;
			else if( (qualifierBits & IS_LONG_LONG) != 0 )
				type = IBasicType.t_int;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPBasicType#isLongLong()
	 */
	public boolean isLongLong() {
		return ( qualifierBits & IS_LONG_LONG ) != 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPBasicType#getTypeofType()
	 */
	public IType getTypeofType() {
		if( type != IGPPASTSimpleDeclSpecifier.t_typeof )
			return null;
		return typeOf;
	}

	public boolean isComplex() {
		return ( qualifierBits & IS_COMPLEX ) != 0;
	}

	public boolean isImaginary() {
		return ( qualifierBits & IS_IMAGINARY ) != 0;
	}

}
