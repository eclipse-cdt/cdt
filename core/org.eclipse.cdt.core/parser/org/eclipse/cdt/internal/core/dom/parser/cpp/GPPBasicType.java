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

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPBasicType;

/**
 * @author aniefer
 */
public class GPPBasicType extends CPPBasicType implements IGPPBasicType {
	public static final int IS_LONGLONG = LAST << 1;
	
	private IType typeOf;
	
	public GPPBasicType( int type, int bits, IType typeOf ){
		super( type, bits );
		this.typeOf = typeOf;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPBasicType#isLongLong()
	 */
	public boolean isLongLong() {
		return ( qualifierBits & IS_LONGLONG ) != 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPBasicType#getTypeofType()
	 */
	public IType getTypeofType() {
		if( type != IGPPASTSimpleDeclSpecifier.t_typeof )
			return null;
		return typeOf;
	}

}
