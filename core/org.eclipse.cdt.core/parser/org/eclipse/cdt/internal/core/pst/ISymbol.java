/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.pst;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.cdt.internal.core.pst.ParserSymbolTable.TypeInfo;
import org.eclipse.cdt.internal.core.pst.ParserSymbolTable.TemplateInstance;
/**
 * @author jcamelon
 *
 */
public interface ISymbol {

	public ParserSymbolTable getSymbolTable();
	
	public Object clone();
		
	public Object getCallbackExtension(); 
	public void setCallbackExtension( Object obj );

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

	public int compareCVQualifiersTo( ISymbol symbol );
	public LinkedList getPtrOperators();
	public void addPtrOperator( TypeInfo.PtrOp ptrOp );
	
	public boolean isTemplateMember();
	public void setIsTemplateMember( boolean isMember );
	public ISymbol getTemplateInstance();
	public HashMap getArgumentMap();
	public void setTemplateInstance( TemplateInstance instance );
	
	/*public interface ITypeInfo {
		public boolean checkBit(int mask);
		public void setBit(boolean b, int mask);
		public boolean isType( int type );
		public boolean isType( int type, int upperType );	
		public int getType();
		public ISymbol getTypeSymbol();
		
		public int getCVQualifier();
		public void addCVQualifier( int cvQual ); 
		public String getPtrOperator();
		public void addPtrOperator( String ptrOp );
		public void setType(int i);
		public void setTypeSymbol(ISymbol typeSymbol);
	
		public int getTypeInfo();
		public void setTypeInfo( int typeInfo );
		public void setPtrOperator(String string);
		public boolean canHold(ITypeInfo src);
		public String getInvertedPtrOperator();
		public void setCVQualifier(int i);
		public boolean getHasDefault();
		public void setHasDefault(boolean hasDefault);				
	}
	*/
	public int getDepth();
}
