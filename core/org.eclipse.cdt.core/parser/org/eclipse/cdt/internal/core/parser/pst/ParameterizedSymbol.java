/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Nov 6, 2003
 */
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable.Command;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ParameterizedSymbol extends ContainerSymbol implements IParameterizedSymbol {

	protected ParameterizedSymbol( ParserSymbolTable table, String name ){
		super( table, name );
	}
	
	protected ParameterizedSymbol( ParserSymbolTable table, String name, ISymbolASTExtension obj ){
		super( table, name, obj );
	}
	
	protected ParameterizedSymbol( ParserSymbolTable table, String name, TypeInfo.eType typeInfo ){
		super( table, name, typeInfo );
	}
	
	public Object clone(){
		ParameterizedSymbol copy = (ParameterizedSymbol)super.clone();
			
		copy._parameterList = ( _parameterList != null ) ? (LinkedList) _parameterList.clone() : null;
		
		if( getSymbolTable().getParserMode() == ParserMode.COMPLETION_PARSE )
			copy._parameterMap	= ( _parameterMap  != null ) ? (Map) ((TreeMap) _parameterMap).clone() : null;
		else 
			copy._parameterMap	= ( _parameterMap  != null ) ? (Map) ((HashMap) _parameterMap).clone() : null;
			
		copy._argumentList	  = ( _argumentList != null ) ? (LinkedList) _argumentList.clone() : null;
		copy._specializations = ( _specializations != null ) ? (LinkedList) _specializations.clone() : null;
		
		return copy;	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addParameter(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addParameter( ISymbol param ){
		List paramList = getParameterList();

		paramList.add( param );
		
		String name = param.getName();
		if( name != null && !name.equals(ParserSymbolTable.EMPTY_NAME) )
		{
			Map paramMap = getParameterMap();

			if( !paramMap.containsKey( name ) )
				paramMap.put( name, param );
		}
		
		param.setContainingSymbol( this );
		param.setIsTemplateMember( isTemplateMember() || getType() == TypeInfo.t_template );
		
		Command command = new AddParameterCommand( this, param );
		getSymbolTable().pushCommand( command );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addParameter(org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType, int, org.eclipse.cdt.internal.core.parser.pst.TypeInfo.PtrOp, boolean)
	 */
	public void addParameter( TypeInfo.eType type, int info, TypeInfo.PtrOp ptrOp, boolean hasDefault ){
		BasicSymbol param = new BasicSymbol(getSymbolTable(), ParserSymbolTable.EMPTY_NAME);
				
		TypeInfo t = param.getTypeInfo();
		t.setTypeInfo( info );
		t.setType( type );
		t.addPtrOperator( ptrOp );
		t.setHasDefault( hasDefault );
			
		addParameter( param );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addParameter(org.eclipse.cdt.internal.core.parser.pst.ISymbol, org.eclipse.cdt.internal.core.parser.pst.TypeInfo.PtrOp, boolean)
	 */
	public void addParameter( ISymbol typeSymbol, TypeInfo.PtrOp ptrOp, boolean hasDefault ){
		BasicSymbol param = new BasicSymbol(getSymbolTable(), ParserSymbolTable.EMPTY_NAME);
		
		TypeInfo info = param.getTypeInfo();
		info.setType( TypeInfo.t_type );
		info.setTypeSymbol( typeSymbol );
		info.addPtrOperator( ptrOp );
		info.setHasDefault( hasDefault );
			
		addParameter( param );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addArgument(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addArgument( ISymbol arg ){
		List argumentList = getArgumentList();
		argumentList.add( arg );
		
		arg.setIsTemplateMember( isTemplateMember() || getType() == TypeInfo.t_template );
		
		Command command = new AddArgumentCommand( this, arg );
		getSymbolTable().pushCommand( command );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getArgumentList()
	 */
	public List getArgumentList(){
		if( _argumentList == null ){
			_argumentList = new LinkedList();
		}
		return _argumentList;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#setArgumentList(java.util.List)
	 */
//	public void setArgumentList( List list ){
//		_argumentList = new LinkedList( list );
//	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getParameterMap()
	 */
	public Map getParameterMap(){
		if( _parameterMap == null ){
			if( getSymbolTable().getParserMode() == ParserMode.COMPLETION_PARSE )
				_parameterMap = new TreeMap( new SymbolTableComparator() );
			else 
				_parameterMap = new HashMap( );
		}
		return _parameterMap;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getParameterList()
	 */
	public List getParameterList(){
		if( _parameterList == null ){
			_parameterList = new LinkedList();
		}
		return _parameterList;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#setParameterList(java.util.List)
	 */
//	public void setParameterList( List list ){
//		_parameterList = new LinkedList( list );	
//	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#hasSameParameters(org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol)
	 */
	public boolean hasSameParameters( IParameterizedSymbol function ){
		if( function.getType() != getType() ){
			return false;	
		}
	
		int size = ( getParameterList() == null ) ? 0 : getParameterList().size();
		int fsize = ( function.getParameterList() == null ) ? 0 : function.getParameterList().size();
		if( fsize != size ){
			return false;
		}
		if( fsize == 0 )
			return true;
	
		Iterator iter = getParameterList().iterator();
		Iterator fIter = function.getParameterList().iterator();
	
		TypeInfo info = null;
		TypeInfo fInfo = null;
	
		for( int i = size; i > 0; i-- ){
			info = ((BasicSymbol)iter.next()).getTypeInfo();
			fInfo = ((BasicSymbol) fIter.next()).getTypeInfo();
		
			if( !info.equals( fInfo ) ){
				return false;
			}
		}
	
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#setReturnType(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void setReturnType( ISymbol type ){
		_returnType = type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getReturnType()
	 */
	public ISymbol getReturnType(){
		return _returnType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#hasSpecializations()
	 */
	public boolean hasSpecializations(){
		return ( _specializations != null && !_specializations.isEmpty() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addSpecialization(org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol)
	 */
	public void addSpecialization( IParameterizedSymbol spec ){
		List specializationList = getSpecializations();
		specializationList.add( spec );
		
		spec.setContainingSymbol( getContainingSymbol() );	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getSpecializations()
	 */
	public List getSpecializations() {
		if( _specializations == null ){
			_specializations = new LinkedList();
		}
		return _specializations;
	}

	public void setHasVariableArgs( boolean var ){
		_hasVarArgs = var;
	}
	
	public boolean hasVariableArgs( ){
		return _hasVarArgs;
	}

	static private class AddParameterCommand extends Command{
		public AddParameterCommand( IParameterizedSymbol container, ISymbol parameter ){
			_decl = container;
			_param = parameter;
		}
		
		public void undoIt(){
			_decl.getParameterList().remove( _param );
			
			String name = _param.getName();
			if( name != null && !name.equals( ParserSymbolTable.EMPTY_NAME) )
			{	
				_decl.getParameterMap().remove( name );
			}
		}
		
		private IParameterizedSymbol _decl;
		private ISymbol _param;
	}
	
	static private class AddArgumentCommand extends Command{
		public AddArgumentCommand( IParameterizedSymbol container, ISymbol arg ){
			_decl = container;
			_arg = arg;
		}
		public void undoIt(){
			_decl.getArgumentList().remove( _arg );
		}

		private IParameterizedSymbol _decl;
		private ISymbol _arg;
	}
	
	private 	LinkedList	_parameterList;			//have my cake
	private 	Map			_parameterMap;			//and eat it too
	private		LinkedList	_specializations;		//template specializations
	private		LinkedList	_argumentList;			//template specialization arguments
	private 	ISymbol		_returnType;
	private 	boolean		_hasVarArgs = false;	//whether or not this function has variable arguments
}
