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
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * This interface represents a C++ conversion member function.
 *  
 * @author dsteffle
 */
public interface ICPPASTConversionName extends IASTName {
	public static final ASTNodeProperty TYPE_ID=new ASTNodeProperty(
	"IASTArrayDeclarator.TYPE_ID - IASTTypeId for ICPPASTConversionName"); //$NON-NLS-1$
	
	/**
	 * Returns the IASTTypeId for the ICPPASTConversionName.
	 * 
	 * i.e. getTypeId() on operator int(); would return the IASTTypeId for "int" 
	 * 
	 * @return
	 */
	public IASTTypeId getTypeId();
	
	/**
	 * Sets the IASTTypeId for the ICPPASTConversionName.
	 * 
	 * @param typeId
	 */
	public void setTypeId(IASTTypeId typeId);
}
