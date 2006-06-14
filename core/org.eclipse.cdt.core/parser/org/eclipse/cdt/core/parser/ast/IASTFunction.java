/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.ast;

import java.util.Iterator;

/**
 * @author jcamelon
 *
 */
public interface IASTFunction extends IASTCodeScope, IASTOffsetableNamedElement, IASTTemplatedDeclaration, IASTDeclaration, IASTQualifiedNameElement {
	
	public boolean isInline(); 
	public boolean isFriend();
	public boolean isStatic();

	public boolean takesVarArgs(); 
	
	public String getName();

	public IASTAbstractDeclaration getReturnType();   
	public Iterator getParameters();
	public IASTExceptionSpecification getExceptionSpec();
	/**
	 * @param b
	 */
	public void setHasFunctionBody(boolean b);
	public boolean hasFunctionBody();
	
	public void setHasFunctionTryBlock( boolean b );
	public boolean hasFunctionTryBlock();
	
	public boolean previouslyDeclared();

}
