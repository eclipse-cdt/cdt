/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;

/**
 * The implemented ICPPASTConversionName.
 *  
 * @author dsteffle
 */
public class CPPASTConversionName extends CPPASTName implements ICPPASTConversionName {
	private IASTTypeId typeId=null;
	
	public CPPASTConversionName() {
		super();
	}
	
	public CPPASTConversionName(char[] name) {
		super(name);
	}
	
	public IASTTypeId getTypeId() {
		return typeId;
	}

	public void setTypeId(IASTTypeId typeId) {
		this.typeId=typeId;
	}
	
	public boolean accept(ASTVisitor action) {
		boolean why = super.accept(action);
		if( why && typeId != null )
			return typeId.accept( action );
		return why;
	}
}
