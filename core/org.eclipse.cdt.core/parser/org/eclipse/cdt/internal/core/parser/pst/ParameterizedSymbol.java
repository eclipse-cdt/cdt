/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.core.parser.scanner2.CharArrayObjectMap;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ParameterizedSymbol extends ContainerSymbol implements IParameterizedSymbol { 

	protected ParameterizedSymbol( ParserSymbolTable table, char[] name ){
		super( table, name );
	}
	
	protected ParameterizedSymbol( ParserSymbolTable table, char[] name, ITypeInfo.eType typeInfo ){
		super( table, name, typeInfo );
	}
	
	public Object clone(){
		ParameterizedSymbol copy = (ParameterizedSymbol)super.clone();
			
		copy._parameterList = ( _parameterList != Collections.EMPTY_LIST ) ? (List) ((ArrayList)_parameterList).clone() : _parameterList;
		copy._parameterMap	= ( _parameterMap != CharArrayObjectMap.EMPTY_MAP ) ? (CharArrayObjectMap) _parameterMap.clone() : _parameterMap;
			
		return copy;	
	}
	
	public ISymbol instantiate( ITemplateSymbol template, Map argMap ) throws ParserSymbolTableException{
		if( !isTemplateMember() ){
			return null;
		}
		
		ParameterizedSymbol newParameterized = (ParameterizedSymbol) super.instantiate( template, argMap );

		if( _returnType != null ){
			if( _returnType.isType( ITypeInfo.t_templateParameter ) ){
				if( argMap.containsKey( _returnType ) ){
					newParameterized.setReturnType( getSymbolTable().newSymbol( ParserSymbolTable.EMPTY_NAME_ARRAY ) );
					newParameterized.getReturnType().setTypeInfo( (ITypeInfo) argMap.get( _returnType ) );
					newParameterized.getReturnType().setInstantiatedSymbol( _returnType );
				}
			} else {
				if( _returnType instanceof IDeferredTemplateInstance )
					template.registerDeferredInstatiation( newParameterized, _returnType, ITemplateSymbol.DeferredKind.RETURN_TYPE, argMap );
				else
					newParameterized.setReturnType( _returnType.instantiate( template, argMap ) );
			}
		}
		
		//handle template parameter lists in TemplateSymbol, only do function parameter lists here.
		if( !isType( ITypeInfo.t_template ) ){
			List params = getParameterList();
			int size = params.size();
			
			newParameterized.getParameterList().clear();
			newParameterized.getParameterMap().clear();
			
			ISymbol param = null, newParam = null;
			
			for( int i = 0; i < size; i++ ){
				param = (ISymbol) params.get(i);
				newParam = param.instantiate( template, argMap );
				
				newParameterized.addParameter( newParam );
			}	
		}
		
		return newParameterized;	
	}
	
	public void instantiateDeferredReturnType( ISymbol returnType, ITemplateSymbol template, Map argMap ) throws ParserSymbolTableException{
		setReturnType( returnType.instantiate( template, argMap ) );
	}
	
	/**
	 * @param symbol
	 * @param symbol2
	 * @param map
	 */
	public void discardDeferredReturnType(ISymbol oldReturnType, TemplateSymbol template, Map map) {
		ISymbol returnType = getReturnType();
		setReturnType( null );
		template.removeInstantiation( (IContainerSymbol) returnType );
	}
	
	public void prepareForParameters( int numParams ){
		if( _parameterList == Collections.EMPTY_LIST ){
			_parameterList = new ArrayList( numParams );
		} else {
			((ArrayList)_parameterList).ensureCapacity( numParams );
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addParameter(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addParameter( ISymbol param ){
		if( _parameterList == Collections.EMPTY_LIST)
			_parameterList = new ArrayList(8);

		_parameterList.add( param );
		
		char[] name = param.getName();
		if( name != null && !name.equals(ParserSymbolTable.EMPTY_NAME_ARRAY) )
		{
			if( _parameterMap == CharArrayObjectMap.EMPTY_MAP ){
				_parameterMap = new CharArrayObjectMap( 2 );
			}
			
			if( !_parameterMap.containsKey( name ) )
				_parameterMap.put( name, param );
		}
		
		param.setContainingSymbol( this );
		param.setIsTemplateMember( isTemplateMember() || getType() == ITypeInfo.t_template );
		
//		Command command = new AddParameterCommand( this, param );
//		getSymbolTable().pushCommand( command );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addParameter(org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType, int, org.eclipse.cdt.internal.core.parser.pst.TypeInfo.PtrOp, boolean)
	 */
	public void addParameter( ITypeInfo.eType type, int info, ITypeInfo.PtrOp ptrOp, boolean hasDefault ){
		BasicSymbol param = new BasicSymbol(getSymbolTable(), ParserSymbolTable.EMPTY_NAME_ARRAY);
				
		ITypeInfo t = TypeInfoProvider.newTypeInfo( type, info, ptrOp, hasDefault );
		param.setTypeInfo( t );
			
		addParameter( param );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addParameter(org.eclipse.cdt.internal.core.parser.pst.ISymbol, org.eclipse.cdt.internal.core.parser.pst.TypeInfo.PtrOp, boolean)
	 */
	public void addParameter( ISymbol typeSymbol, int info, ITypeInfo.PtrOp ptrOp, boolean hasDefault ){
		BasicSymbol param = new BasicSymbol(getSymbolTable(), ParserSymbolTable.EMPTY_NAME_ARRAY);
		
		ITypeInfo nfo = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, info, typeSymbol, ptrOp, hasDefault );
		param.setTypeInfo( nfo );
			
		addParameter( param );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getParameterMap()
	 */
	public CharArrayObjectMap getParameterMap(){
		return _parameterMap;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getParameterList()
	 */
	public List getParameterList(){
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
	
		List params = getParameterList();
		List functionParams = function.getParameterList();
	
		ITypeInfo info = null;
		ITypeInfo fInfo = null;
	
		TypeInfoProvider provider = getSymbolTable().getTypeInfoProvider();
		
		for( int i = 0; i < size; i++ ){
			ISymbol p = (ISymbol) params.get(i);
			ISymbol pf = (ISymbol) functionParams.get(i);
			
			info = p.getTypeInfo();
			fInfo = pf.getTypeInfo();
			
			//parameters that differ only in the use of equivalent typedef types are equivalent.
			info = ParserSymbolTable.getFlatTypeInfo( info, provider );
			fInfo = ParserSymbolTable.getFlatTypeInfo( fInfo, provider );
			
			for( ITypeInfo nfo = info; nfo != null; nfo = fInfo ){
				//an array declaration is adjusted to become a pointer declaration
				//only the second and subsequent array dimensions are significant in parameter types
				List ptrs = nfo.getPtrOperators(); 
				if( ptrs.size() > 0 ){
					ITypeInfo.PtrOp op = (ITypeInfo.PtrOp) ptrs.get(0);
					if( op.getType() == ITypeInfo.PtrOp.t_array ){
						ptrs.set( 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer, op.isConst(), op.isVolatile() ) );
					}
				}
				
				//a function type is adjusted to become a pointer to function type
				if( nfo.isType( ITypeInfo.t_type ) && nfo.getTypeSymbol() != null && 
					nfo.getTypeSymbol().isType( ITypeInfo.t_function ) )
				{
					if( nfo.getPtrOperators().size() == 0 ){
						nfo.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ) );
					}
				}

				//const and volatile type-specifiers are ignored (only the outermost level)
				if( nfo.getPtrOperators().size() == 0 ){
					nfo.setBit( false, ITypeInfo.isConst );
					nfo.setBit( false, ITypeInfo.isVolatile );
				} else {
					ITypeInfo.PtrOp op = (ITypeInfo.PtrOp) nfo.getPtrOperators().get( nfo.getPtrOperators().size() - 1 );
					op.setConst( false );
					op.setVolatile( false );
				}
				
				if( nfo == fInfo ) 
					break;
			}
			
			boolean equals = info.equals( fInfo );

			provider.returnTypeInfo( info );
			provider.returnTypeInfo( fInfo );
			
			if( ! equals )
				return false;
		}
	
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#setReturnType(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void setReturnType( ISymbol type ){
		_returnType = type;
		_returnType.setContainingSymbol( this );
		_returnType.setIsTemplateMember( isTemplateMember() || getType() == ITypeInfo.t_template );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getReturnType()
	 */
	public ISymbol getReturnType(){
		return _returnType;
	}

	public void setHasVariableArgs( boolean var ){
		_hasVarArgs = var;
	}
	
	public boolean hasVariableArgs( ){
		return _hasVarArgs;
	}

//	static private class AddParameterCommand extends Command{
//		public AddParameterCommand( IParameterizedSymbol container, ISymbol parameter ){
//			_decl = container;
//			_param = parameter;
//		}
//		
//		public void undoIt(){
//			_decl.getParameterList().remove( _param );
//			
//			String name = _param.getName();
//			if( name != null && !name.equals( ParserSymbolTable.EMPTY_NAME) )
//			{	
//				_decl.getParameterMap().remove( name );
//			}
//		}
//		
//		private IParameterizedSymbol _decl;
//		private ISymbol _param;
//	}
	
	
	private 	List	_parameterList = Collections.EMPTY_LIST;	//have my cake
	private 	CharArrayObjectMap 	_parameterMap  = CharArrayObjectMap.EMPTY_MAP;	//and eat it too
	private 	ISymbol		_returnType;
	private 	boolean		_hasVarArgs = false;	//whether or not this function has variable arguments
}
