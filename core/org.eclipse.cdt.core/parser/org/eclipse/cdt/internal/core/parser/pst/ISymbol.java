/**********************************************************************
 * Copyright (c) 2002,2003, 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.List;
import java.util.Map;

/**
 * @author jcamelon
 *
 */
public interface ISymbol extends Cloneable,  IExtensibleSymbol {

	public Object clone();
	
	/**
	 * 
	 * @param args
	 * @return
	 * @throws ParserSymbolTableException
	 * Exceptions can be thrown when a IDeferredTemplateInstance must be instantiated
	 * Reason:
	 *     r_BadTemplateArgument if an argument does not match the corresponding parameter type
	 *     r_Ambiguous if more than one specialization can be used but none is more specialized than all the others
	 */
	public ISymbol instantiate( ITemplateSymbol template, Map argMap ) throws ParserSymbolTableException;

	public void setName(String name);
	public String getName();
	
	public IContainerSymbol getContainingSymbol();
	public void setContainingSymbol( IContainerSymbol containing );
	
	public boolean isType( ITypeInfo.eType type );
	public boolean isType( ITypeInfo.eType type, ITypeInfo.eType upperType );
	public ITypeInfo.eType getType();
	public void setType(ITypeInfo.eType t);
	public ITypeInfo getTypeInfo();
	public void setTypeInfo( ITypeInfo info );
	public ISymbol getTypeSymbol();
	public void setTypeSymbol( ISymbol type );

	public boolean isForwardDeclaration();
	public void setIsForwardDeclaration( boolean forward );
	public void setForwardSymbol( ISymbol forward );
	public ISymbol getForwardSymbol();
	
	public int compareCVQualifiersTo( ISymbol symbol );
	public List getPtrOperators();
	public void addPtrOperator( ITypeInfo.PtrOp ptrOp );
	
	public boolean isTemplateInstance();
	public ISymbol getInstantiatedSymbol();
	public void setInstantiatedSymbol( ISymbol symbol );
	public boolean isTemplateMember();
	public void setIsTemplateMember( boolean isMember );
	
	public int getDepth();
	public boolean getIsInvisible();
	public void setIsInvisible( boolean invisible );

	public void preparePtrOperatros(int numPtrOps);
}
