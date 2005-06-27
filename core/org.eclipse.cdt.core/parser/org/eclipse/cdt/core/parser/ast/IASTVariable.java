/*******************************************************************************
 * Copyright (c) 2002, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.ast;


/**
 * @author jcamelon
 *
 */
public interface IASTVariable extends  IASTDeclaration, IASTOffsetableNamedElement, IASTQualifiedNameElement {

	public boolean isAuto(); 
	public boolean isRegister(); 
	public boolean isStatic(); 
	public boolean isExtern(); 
	public boolean isMutable(); 
	
	public IASTAbstractDeclaration getAbstractDeclaration(); 
	public String getName(); 
	public IASTInitializerClause getInitializerClause(); 
	
	public boolean isBitfield(); 
	public IASTExpression getBitfieldExpression(); 
	public IASTExpression getConstructorExpression();
}
