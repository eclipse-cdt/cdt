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

/**
 * @author jcamelon
 *
 */
public interface ISymbol {

	public Object getCallbackExtension(); 
	public void setCallbackExtension( Object obj );

	public String getName();
	
	public IContainerSymbol getContainingSymbol();
	public void setContainingSymbol( IContainerSymbol containing );
	
	public boolean isType( int type );
	public boolean isType( int type, int upperType );
	public int getType();
	public void setType(int t);
	public ITypeInfo getTypeInfo();
	public ISymbol getTypeSymbol();
	public void setTypeSymbol( ISymbol type );
	public int getCVQualifier();
	public void setCVQualifier( int cv );
	public String getPtrOperator();
	public void setPtrOperator( String ptrOp );
	
	public interface ITypeInfo {
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
	
	public int getDepth();
}
