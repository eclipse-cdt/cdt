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
		
	public void setName(String name);
	public String getName();
	
	public IContainerSymbol getContainingSymbol();
	public void setContainingSymbol( IContainerSymbol containing );
	
	public boolean isType( TypeInfo.eType type );
	public boolean isType( TypeInfo.eType type, TypeInfo.eType upperType );
	public TypeInfo.eType getType();
	public void setType(TypeInfo.eType t);
	public TypeInfo getTypeInfo();
	public void setTypeInfo( TypeInfo info );
	public ISymbol getTypeSymbol();
	public void setTypeSymbol( ISymbol type );

	public boolean isForwardDeclaration();
	public void setIsForwardDeclaration( boolean forward );
	
	public int compareCVQualifiersTo( ISymbol symbol );
	public List getPtrOperators();
	public void addPtrOperator( TypeInfo.PtrOp ptrOp );
	
	public boolean isTemplateMember();
	public void setIsTemplateMember( boolean isMember );
	public ISymbol getTemplateInstance();
	public Map getArgumentMap();
	public void setTemplateInstance( TemplateInstance instance );

	public int getDepth();
	public boolean getIsInvisible();
	public void setIsInvisible( boolean invisible );
}
