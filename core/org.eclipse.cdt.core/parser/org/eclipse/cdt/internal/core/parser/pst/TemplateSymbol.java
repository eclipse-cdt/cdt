/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.core.parser.pst.TypeInfo.PtrOp;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TemplateSymbol	extends ParameterizedSymbol	implements ITemplateSymbol {
	
	protected TemplateSymbol ( ParserSymbolTable table, String name ){
		super( table, name, TypeInfo.t_template );
	}
	
	protected TemplateSymbol( ParserSymbolTable table, String name, ISymbolASTExtension obj ){
		super( table, name, obj );
	}
	
	public Object clone(){
		TemplateSymbol copy = (TemplateSymbol)super.clone();
		
		//copy._specializations = ( _specializations != null ) ? (LinkedList) _specializations.clone() : null;
		
		copy._defnParameterMap = ( _defnParameterMap != null ) ? (HashMap) _defnParameterMap.clone() : null;
		copy._instantiations = ( _instantiations != null ) ? (HashMap) _instantiations.clone() : null;
		
		
		return copy;	
	}
	
	public IContainerSymbol getTemplatedSymbol(){
		Iterator iter = getContentsIterator();
		if( iter.hasNext() ){
			IContainerSymbol contained = (IContainerSymbol) iter.next();
			return contained;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#instantiate(java.util.List)
	 */
	public ISymbol instantiate( List arguments ) throws ParserSymbolTableException{
		if( getType() != TypeInfo.t_template &&
				( getType() != TypeInfo.t_templateParameter || 
						getTypeInfo().getTemplateParameterType() != TypeInfo.t_template ) )
		{
			return null;
		}
		
		ITemplateSymbol template = TemplateEngine.matchTemplatePartialSpecialization( this, arguments );
				
		if( template != null && template instanceof ISpecializedSymbol ){
			return template.instantiate( arguments );	
		}
		
		if( template == null ){
			template = this;
		}
		
		List paramList = template.getParameterList();
		int numParams = ( paramList != null ) ? paramList.size() : 0;
		
		if( numParams == 0 ){
			return null;				
		}

		HashMap map = new HashMap();
		Iterator paramIter = paramList.iterator();
		Iterator argIter = arguments.iterator();
		
		ISymbol param = null;
		TypeInfo arg = null;
		
		List actualArgs = new LinkedList();
		
		ISymbol templatedSymbol = template.getTemplatedSymbol();
		while( templatedSymbol != null && templatedSymbol.isTemplateInstance() ){
			templatedSymbol = templatedSymbol.getInstantiatedSymbol();
		}
		
		for( int i = 0; i < numParams; i++ ){
			param = (ISymbol) paramIter.next();
			
			param = TemplateEngine.translateParameterForDefinition ( templatedSymbol, param, getDefinitionParameterMap() );
			
			if( argIter.hasNext() ){
				arg = (TypeInfo) argIter.next();
				//If the argument is a template parameter, we can't instantiate yet, defer for later
				if( arg.isType( TypeInfo.t_type ) ){
					if( arg.getTypeSymbol() == null ) 
						throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplateArgument );
					else if( arg.getTypeSymbol().isType( TypeInfo.t_templateParameter ) )
						return deferredInstance( arguments );
				}
			} else {
				Object obj = param.getTypeInfo().getDefault();
				if( obj != null && obj instanceof TypeInfo ){
					arg = (TypeInfo) obj;
					if( arg.isType( TypeInfo.t_type ) && arg.getTypeSymbol().isType( TypeInfo.t_templateParameter ) ){
						if( map.containsKey( arg.getTypeSymbol() ) ){
							arg = (TypeInfo) map.get( arg.getTypeSymbol() );
						} else {
							throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplateArgument );
						}
					}
				} else {
					throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplateArgument );					
				}
			}
			
			if( TemplateEngine.matchTemplateParameterAndArgument( param, arg ) ){
				map.put( param, arg );
				actualArgs.add( arg );
			} else {
				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplateArgument );
			}
		}
		
		IContainerSymbol instance = findInstantiation( actualArgs );
		if( instance != null ){
			return instance;
		} else {
			if( template.isType( TypeInfo.t_templateParameter ) ){
				//template template parameter.  must defer instantiation
				return deferredInstance( arguments );
			} 
			
			IContainerSymbol symbol = template.getTemplatedSymbol(); 
			ISymbol temp = TemplateEngine.checkForTemplateExplicitSpecialization( template, symbol, actualArgs );
			symbol = (IContainerSymbol) ( temp != null ? temp : symbol);
				
			instance = (IContainerSymbol) symbol.instantiate( template, map );
			addInstantiation( instance, actualArgs );
			return instance;
		}
	}
	
	
	public void addParameter( ISymbol param ) {
		throw new ParserSymbolTableError( ParserSymbolTableError.r_OperationNotSupported );
	}
	
	public void addTemplateParameter( ISymbol param ) throws ParserSymbolTableException {
		if( isType( TypeInfo.t_template ) ){
			if( !isAllowableTemplateParameter( param ) ){
				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplateParameter );
			}
			modifyTemplateParameter( param );
		}
		
		super.addParameter( param );
	}
	
	private boolean isAllowableTemplateParameter( ISymbol param ) {
		if( !param.isType( TypeInfo.t_templateParameter ) )
			return false;
		
		if( param.getName().equals( getName() ) ){
			return false;
		}
		
		if( param.getTypeInfo().getTemplateParameterType() != TypeInfo.t_typeName &&
				param.getTypeInfo().getTemplateParameterType() != TypeInfo.t_template )
		{
			if( param.isType( TypeInfo.t_bool, TypeInfo.t_int ) ||
					param.isType( TypeInfo.t_enumerator ) )
			{
				return true;
			}
			
			//a non-tpye template parameter shall have one of the following:
			//integral or enumeration type
			//pointer to object or pointer to function
			//reference to object or reference to function
			//pointer to member

			//A non-type template-parameter shall not be declared to have floating point, class or void type
			if( param.isType( TypeInfo.t_float ) || 
					param.isType( TypeInfo.t_double )||
					param.isType( TypeInfo.t_class ) ||
					param.isType( TypeInfo.t_void ) )
			{
				return false;				
			}
		}
		return true;			
	}
	
	private void modifyTemplateParameter( ISymbol param ){
		List ptrs = param.getPtrOperators();
		if( ptrs.size() > 0 ){
			PtrOp op = (PtrOp) ptrs.get( 0 );
			if( op.getType() == PtrOp.t_array ){
				op.setType( PtrOp.t_pointer );
			}
		} else if ( param.isType( TypeInfo.t_type ) && param.getTypeSymbol().isType( TypeInfo.t_function ) ){
			param.addPtrOperator( new PtrOp( PtrOp.t_pointer ) );
		}
	}
	




	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#hasSpecializations()
	 */
	public boolean hasSpecializations(){
		return ( _specializations != null && !_specializations.isEmpty() );
	}
	
	public void addExplicitSpecialization( ISymbol symbol, List args ) throws ParserSymbolTableException{
		
		List actualArgs = TemplateEngine.verifyExplicitArguments( this, args, symbol );
		
		Map map = getExplicitSpecializations();
		
		Map specs = null;
		List key = null;
		
		Iterator iter = map.keySet().iterator();
		while( iter.hasNext() ){
			List list = (List) iter.next();
			if( list.equals( args ) ){
				key = list;
				break;
			}
		}
		
		if( key != null ){
			specs = (Map) map.get( key );
		} else {
			specs = new HashMap();
			map.put( new LinkedList( actualArgs ), specs );
		}
		
		ISymbol found = null;
		try{
			if( symbol.isType( TypeInfo.t_function ) || symbol.isType( TypeInfo.t_constructor ) ){
				List fnArgs = new LinkedList();
				iter = ((IParameterizedSymbol)symbol).getParameterList().iterator();
				while( iter.hasNext() ){
					fnArgs.add( ((ISymbol)iter.next()).getTypeInfo() );
				}
				found = getTemplatedSymbol().lookupMethodForDefinition( symbol.getName(), fnArgs );
			} else {
				found = getTemplatedSymbol().lookupMemberForDefinition( symbol.getName() );
			}
		} catch (ParserSymbolTableException e) {
		}
		if( found == null && getTemplatedSymbol().getName().equals( symbol.getName() ) ){
			found = getTemplatedSymbol();
		}
		
		if( found != null ){
			symbol.setIsTemplateMember( true );
			symbol.setContainingSymbol( found.getContainingSymbol() );
			specs.put( found, symbol );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addSpecialization(org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol)
	 */
	public void addSpecialization( ISpecializedSymbol spec ){
		List specializationList = getSpecializations();
		specializationList.add( spec );
		
		spec.setContainingSymbol( getContainingSymbol() );	
		spec.setPrimaryTemplate( this );
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
	
	public void addInstantiation( IContainerSymbol instance, List args ){
		List key = new LinkedList( args );
		if( _instantiations == null ){
			_instantiations = new HashMap();
		}
		_instantiations.put( key, instance );
	}
	
	public IContainerSymbol findInstantiation( List arguments ){
		if( _instantiations == null ){
			return null;
		}
		
		//TODO: we could optimize this by doing something other than a linear search.
		Iterator iter = _instantiations.keySet().iterator();
		List args = null;
		while( iter.hasNext() ){
			args = (List) iter.next();
			
			if( args.equals( arguments ) ){
				return (IContainerSymbol) _instantiations.get( args );
			}
		}
		return null;
	}
	
	public List findArgumentsFor( IContainerSymbol instance ){
		if( instance == null || !instance.isTemplateInstance() )
			return null;
		
		ITemplateSymbol template = (ITemplateSymbol) instance.getInstantiatedSymbol().getContainingSymbol();
		if( template != this )
			return null;
		
		Iterator iter = _instantiations.keySet().iterator();
		while( iter.hasNext() ){
			List args = (List) iter.next();
			if( _instantiations.get( args ) == instance ){
				return args;
			}
		}
		
		return null;
	}
	
	public Map getDefinitionParameterMap(){
		if( _defnParameterMap == null ){
			_defnParameterMap = new HashMap();
		}
		return _defnParameterMap;
	}
	
	public IDeferredTemplateInstance deferredInstance( List args ){
		return new DeferredTemplateInstance( getSymbolTable(), this, args );
	}

	public Map getExplicitSpecializations() {
		if( _explicitSpecializations == null ){
			_explicitSpecializations = new HashMap();
		}
		return _explicitSpecializations;
	}
	
	private		LinkedList	_specializations;		  //template specializations
	private     HashMap		_explicitSpecializations; //explicit specializations
	private		HashMap		_defnParameterMap;		  //members could be defined with different template parameter names
	private 	HashMap 	_instantiations;		
	
}