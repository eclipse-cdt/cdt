/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.parser.pst.ITypeInfo.PtrOp;
import org.eclipse.cdt.internal.core.parser.pst.ITypeInfo.eType;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable.LookupData;

/**
 * A symbol type representing a template parameter as a scope
 * example: <code>
 * template <typename T>
 * class A {
 *     typedef typename T::some_type some_type;
 * };
 * </code>
 * Actual template argument is expected to have type 'some_type' but
 * that cannot be confirmed until the templated class is instantiated.
 * A symbol that represents T requires some behaviour of IContainerSymbol.
 * 'some_type' symbol will be added to this container as an unknown type
 * so that at the time of instantiation it can be replaced with real type symbol.
 * 
 * @author vhirsl
 */
public class UndefinedTemplateSymbol extends BasicSymbol implements ITemplateSymbol {

	// ContainerSymbol
	private 	List 			   _contents         = Collections.EMPTY_LIST;				//ordered list of all contents of this symbol
	private		CharArrayObjectMap _containedSymbols = CharArrayObjectMap.EMPTY_MAP;		//declarations contained by us.
//	private		List _usingDirectives = Collections.EMPTY_LIST;		//collection of nominated namespaces
	// TemplateSymbol
	// these are actually arguments in case of
	// typedef typename T::template B<U, char>::some_type some_type;
	//                                ^  ^^^^
	private 	List      		   _argumentList 	 = Collections.EMPTY_LIST;	//list of template arguments
	private 	ObjectMap 		   _instantiations   = ObjectMap.EMPTY_MAP;
	
	/**
	 * @param table
	 * @param name
	 */
	public UndefinedTemplateSymbol(ParserSymbolTable table, char[] name) {
		super(table, name);
	}

	/**
	 * @param table
	 * @param name
	 * @param typeInfo
	 */
	public UndefinedTemplateSymbol(ParserSymbolTable table, char[] name, eType typeInfo) {
		super(table, name, typeInfo);
	}

	public Object clone() {
		UndefinedTemplateSymbol copy = (UndefinedTemplateSymbol) super.clone();
			
		copy._containedSymbols = (CharArrayObjectMap) ( ( _containedSymbols != CharArrayObjectMap.EMPTY_MAP )? _containedSymbols.clone() : _containedSymbols );
		copy._contents = (_contents != Collections.EMPTY_LIST) ? (List) ((ArrayList)_contents).clone() : _contents;
		
		copy._instantiations = ( _instantiations != ObjectMap.EMPTY_MAP ) ? (ObjectMap)_instantiations.clone() : _instantiations;
		copy._argumentList = ( _argumentList != Collections.EMPTY_LIST ) ? (List) ((ArrayList)_argumentList).clone() : _argumentList;	
			
		return copy;	
	}
	
	/*
	 * IContainerSymbol --------------------------------------------------------
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addSymbol(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addSymbol(ISymbol obj) throws ParserSymbolTableException {
		IContainerSymbol containing = this;
		
		// We are expecting another UTS
		if ( ! ( obj instanceof UndefinedTemplateSymbol) ) {
			throw new ParserSymbolTableError( ParserSymbolTableError.r_InternalError );
		}
		
		obj.setContainingSymbol( containing );
		((UndefinedTemplateSymbol)containing).putInContainedSymbols( obj.getName(), obj );
		obj.setIsTemplateMember( isTemplateMember() || getType() == ITypeInfo.t_template );
		
		addToContents( obj );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addTemplateId(org.eclipse.cdt.internal.core.parser.pst.ISymbol, java.util.List)
	 */
	public void addTemplateId(ISymbol symbol, List args) throws ParserSymbolTableException {
		throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#hasUsingDirectives()
	 */
	public boolean hasUsingDirectives() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#getUsingDirectives()
	 */
	public List getUsingDirectives() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDirective(org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public IUsingDirectiveSymbol addUsingDirective(IContainerSymbol namespace) throws ParserSymbolTableException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDeclaration(char[])
	 */
	public IUsingDeclarationSymbol addUsingDeclaration(char[] name)
			throws ParserSymbolTableException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDeclaration(char[], org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public IUsingDeclarationSymbol addUsingDeclaration(char[] name,
			IContainerSymbol declContext) throws ParserSymbolTableException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#getContainedSymbols()
	 */
	public CharArrayObjectMap getContainedSymbols() {
		return _containedSymbols;
	}

	protected void putInContainedSymbols( char[] key, Object obj ){
		if( _containedSymbols == CharArrayObjectMap.EMPTY_MAP ){
			_containedSymbols = new CharArrayObjectMap( 4 );
		}
		_containedSymbols.put( key, obj );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#prefixLookup(org.eclipse.cdt.internal.core.parser.pst.TypeFilter, char[], boolean, java.util.List)
	 */
	public List prefixLookup(TypeFilter filter, char[] prefix, boolean qualified, List paramList) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#elaboratedLookup(org.eclipse.cdt.internal.core.parser.pst.ITypeInfo.eType, char[])
	 */
	public ISymbol elaboratedLookup(final eType type, char[] name) throws ParserSymbolTableException {
		LookupData data = new LookupData( name ){
			public TypeFilter getFilter() {
				if( t == ITypeInfo.t_any ) return ANY_FILTER;
				if( filter == null ) filter = new TypeFilter( t );
				return filter;
			}
			private TypeFilter filter = null;
			private final ITypeInfo.eType t = type;
		};
	
		ParserSymbolTable.lookup( data, this );
	
		ISymbol found = getSymbolTable().resolveAmbiguities( data );
		
		if( isTemplateMember() && found instanceof ITemplateSymbol ) {
			boolean areWithinTemplate = false;
			IContainerSymbol container = getContainingSymbol();
			while( container != null ){
				if( container == found ){
					areWithinTemplate = true;
					break;
				}
				container = container.getContainingSymbol();
			}
			if( areWithinTemplate )
				return TemplateEngine.instantiateWithinTemplateScope( this, (ITemplateSymbol) found );
		}
		
		return found;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookup(char[])
	 */
	public ISymbol lookup(char[] name) throws ParserSymbolTableException {
		LookupData data = new LookupData( name );
		
		ParserSymbolTable.lookup( data, this );
	
		ISymbol found = getSymbolTable().resolveAmbiguities( data );
		
		if( isTemplateMember() && found instanceof ITemplateSymbol ) {
			return TemplateEngine.instantiateWithinTemplateScope( this, (ITemplateSymbol) found );
		}
		
		if (found == null && getTypeInfo() instanceof TemplateParameterTypeInfo) {
			// add a symbol as an expected type to a template parameter
			found = getSymbolTable().newUndefinedTemplateSymbol(name, ITypeInfo.t_undef);
			addSymbol(found);
		}

		return found;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupMemberForDefinition(char[])
	 */
	public ISymbol lookupMemberForDefinition(char[] name, ITypeInfo.eType type) throws ParserSymbolTableException {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupMemberForDefinition(char[])
	 */
	public ISymbol lookupMemberForDefinition(char[] name) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupMethodForDefinition(char[], java.util.List)
	 */
	public IParameterizedSymbol lookupMethodForDefinition(char[] name, List parameters) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * The name of a class or namespace member can be referred to after the ::
	 * scope resolution operator applied to a nested-name-specifier that
	 * nominates its class or namespace.  During the lookup for a name preceding
	 * the ::, object, function and enumerator names are ignored.  If the name
	 * is not a class-name or namespace-name, the program is ill-formed
	 */
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupNestedNameSpecifier(char[])
	 */
	public ISymbol lookupNestedNameSpecifier(char[] name) throws ParserSymbolTableException {
		return lookupNestedNameSpecifier( name, this );
	}

	private ISymbol lookupNestedNameSpecifier(char[] name, IContainerSymbol inSymbol ) throws ParserSymbolTableException{		
		ISymbol foundSymbol = null;
	
		final TypeFilter filter = new TypeFilter( ITypeInfo.t_namespace );
		filter.addAcceptedType( ITypeInfo.t_class );
		filter.addAcceptedType( ITypeInfo.t_struct );
		filter.addAcceptedType( ITypeInfo.t_union );
		filter.addAcceptedType( ITypeInfo.t_templateParameter );
		filter.addAcceptedType( IASTNode.LookupKind.TYPEDEFS );
		
		LookupData data = new LookupData( name ){
			public TypeFilter getFilter() { return typeFilter; }
			final private TypeFilter typeFilter = filter; 
		};
		data.qualified = true;
		ParserSymbolTable.lookup( data, inSymbol );
		
		if( data.foundItems != null ){
			foundSymbol = getSymbolTable().resolveAmbiguities( data );
		}
		
		// another undefined symbol i.e.:
		// template <typename T> class A {
		//     typedef typename T::R::some_type some_type;
		// };                      ^
		if (foundSymbol == null && getTypeInfo() instanceof TemplateParameterTypeInfo) {
			// add a symbol as an expected type to a template parameter
			foundSymbol = getSymbolTable().newUndefinedTemplateSymbol(name, ITypeInfo.t_undef);
			addSymbol(foundSymbol);
		}

		return foundSymbol;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#qualifiedLookup(char[])
	 */
	public ISymbol qualifiedLookup(char[] name) throws ParserSymbolTableException {
		LookupData data = new LookupData( name );
		data.qualified = true;
		ParserSymbolTable.lookup( data, this );
	
		ISymbol found = getSymbolTable().resolveAmbiguities( data );
		
		if (found == null) {
			// add a symbol as an expected type to a template parameter
			found = getSymbolTable().newUndefinedTemplateSymbol(name, ITypeInfo.t_undef);
			addSymbol(found);
		}
		return found;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#qualifiedLookup(char[], org.eclipse.cdt.internal.core.parser.pst.ITypeInfo.eType)
	 */
	public ISymbol qualifiedLookup(char[] name, final ITypeInfo.eType t) throws ParserSymbolTableException {
		LookupData data = new LookupData( name ){
			public TypeFilter getFilter() { 
				if( t == ITypeInfo.t_any ) return ANY_FILTER;
				
				if( filter == null )
					filter = new TypeFilter( t );
				return filter;
				
			}
			private TypeFilter filter = null;
		};
		data.qualified = true;
		ParserSymbolTable.lookup( data, this );
	
		ISymbol found = getSymbolTable().resolveAmbiguities( data );
		
		if (found == null) {
			// add a symbol as an expected type to a template parameter
			found = getSymbolTable().newUndefinedTemplateSymbol(name, ITypeInfo.t_undef);
			addSymbol(found);
		}
		return found;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#unqualifiedFunctionLookup(char[], java.util.List)
	 */
	public IParameterizedSymbol unqualifiedFunctionLookup(char[] name, List parameters) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#memberFunctionLookup(char[], java.util.List)
	 */
	public IParameterizedSymbol memberFunctionLookup(char[] name, List parameters) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#qualifiedFunctionLookup(char[], java.util.List)
	 */
	public IParameterizedSymbol qualifiedFunctionLookup(char[] name, List parameters) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupTemplateId(char[], java.util.List)
	 */
	public ISymbol lookupTemplateId(char[] name, List arguments) throws ParserSymbolTableException {
		LookupData data = new LookupData( name );
		
		ParserSymbolTable.lookup( data, this );
		ISymbol found = getSymbolTable().resolveAmbiguities( data );
		if (found == null) {
			// VMIR {
			// another undefined symbol i.e.:
			// template <typename T> class A {
			//     typedef typename T::template R<T>::some_type some_type;
			// };                               ^^^^
			// add a symbol as an expected type to a template parameter
			found = getSymbolTable().newUndefinedTemplateSymbol(name, ITypeInfo.t_template);
			addSymbol(found);
		}
		if( found != null ){
			if( (found.isType( ITypeInfo.t_templateParameter ) && found.getTypeInfo().getTemplateParameterType() == ITypeInfo.t_template) ||
				 found.isType( ITypeInfo.t_template ) )
			{
				found = ((ITemplateSymbol) found).instantiate( arguments );
			} else if( found.getContainingSymbol().isType( ITypeInfo.t_template ) ){
				found = ((ITemplateSymbol) found.getContainingSymbol()).instantiate( arguments );
			}	
		}
		
		return found;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupFunctionTemplateId(char[], java.util.List, java.util.List, boolean)
	 */
	public ISymbol lookupFunctionTemplateId(char[] name, final List parameters, final List arguments, boolean forDefinition)
			throws ParserSymbolTableException {
		LookupData data = new LookupData( name ){
			public List getParameters() { return params; }
			public List getTemplateParameters() { return templateParams; }
			public TypeFilter getFilter() { return FUNCTION_FILTER; }
			final private List params = ( parameters == null ) ? Collections.EMPTY_LIST : parameters;
			final private List templateParams = arguments;
		};
		data.exactFunctionsOnly = forDefinition;
		
		ParserSymbolTable.lookup( data, this );
		ISymbol found = getSymbolTable().resolveAmbiguities( data );

		return found;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupTemplateIdForDefinition(char[], java.util.List)
	 */
	public IContainerSymbol lookupTemplateIdForDefinition(char[] name, List arguments) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#isVisible(org.eclipse.cdt.internal.core.parser.pst.ISymbol, org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public boolean isVisible(ISymbol symbol, IContainerSymbol qualifyingSymbol) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#getContentsIterator()
	 */
	public Iterator getContentsIterator() {
		return getContents().iterator();
	}

	protected void addToContents( IExtensibleSymbol symbol ){
		if( _contents == Collections.EMPTY_LIST ){
			_contents = new ArrayList( 8 );
		}
		_contents.add( symbol );
	}

	protected List getContents(){
		return _contents;
	}

	
	/*
	 * IParameterSymbol --------------------------------------------------------
	 */
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addParameter(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addParameter(ISymbol param) {
		throw new ParserSymbolTableError( ParserSymbolTableError.r_OperationNotSupported );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addParameter(org.eclipse.cdt.internal.core.parser.pst.ITypeInfo.eType, int, org.eclipse.cdt.internal.core.parser.pst.ITypeInfo.PtrOp, boolean)
	 */
	public void addParameter(eType type, int info, PtrOp ptrOp, boolean hasDefault) {
		throw new ParserSymbolTableError( ParserSymbolTableError.r_OperationNotSupported );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addParameter(org.eclipse.cdt.internal.core.parser.pst.ISymbol, int, org.eclipse.cdt.internal.core.parser.pst.ITypeInfo.PtrOp, boolean)
	 */
	public void addParameter(ISymbol typeSymbol, int info, PtrOp ptrOp, boolean hasDefault) {
		throw new ParserSymbolTableError( ParserSymbolTableError.r_OperationNotSupported );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getParameterMap()
	 */
	public CharArrayObjectMap getParameterMap() {
		return CharArrayObjectMap.EMPTY_MAP;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getParameterList()
	 */
	public List getParameterList() {
		return Collections.EMPTY_LIST;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#hasSameParameters(org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol)
	 */
	public boolean hasSameParameters(IParameterizedSymbol newDecl) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#setReturnType(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void setReturnType(ISymbol type) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getReturnType()
	 */
	public ISymbol getReturnType() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#setHasVariableArgs(boolean)
	 */
	public void setHasVariableArgs(boolean var) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#hasVariableArgs()
	 */
	public boolean hasVariableArgs() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#prepareForParameters(int)
	 */
	public void prepareForParameters(int numParams) {
	}


	/*
	 * ITemplateSymbol --------------------------------------------------------
	 */
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#addTemplateParameter(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addTemplateParameter(ISymbol param) throws ParserSymbolTableException {
		throw new ParserSymbolTableError( ParserSymbolTableError.r_OperationNotSupported );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#hasSpecializations()
	 */
	public boolean hasSpecializations() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#addSpecialization(org.eclipse.cdt.internal.core.parser.pst.ISpecializedSymbol)
	 */
	public void addSpecialization(ISpecializedSymbol spec) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#getSpecializations()
	 */
	public List getSpecializations() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#getTemplatedSymbol()
	 */
	public IContainerSymbol getTemplatedSymbol() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#getDefinitionParameterMap()
	 */
	public ObjectMap getDefinitionParameterMap() {
		return ObjectMap.EMPTY_MAP;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#findInstantiation(java.util.List)
	 */
	public IContainerSymbol findInstantiation(List arguments) {
		if( _instantiations == ObjectMap.EMPTY_MAP ){
			return null;
		}
		
		//TODO: we could optimize this by doing something other than a linear search.
		int size = _instantiations.size();
		List args = null;
		for( int i = 0; i < size; i++ ){
			args = (List) _instantiations.keyAt(i);
			
			if( args.equals( arguments ) ){
				return (IContainerSymbol) _instantiations.get( args );
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#findArgumentsFor(org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public List findArgumentsFor(IContainerSymbol instance) {
		if( instance == null || !instance.isTemplateInstance() )
			return null;
		
//		ITemplateSymbol template = (ITemplateSymbol) instance.getInstantiatedSymbol().getContainingSymbol();
//		if( template != this )
//			return null;
		
		int size = _instantiations.size();
		for( int i = 0; i < size; i++){
			List args = (List) _instantiations.keyAt( i );
			if( _instantiations.get( args ) == instance ){
				return args;
			}
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#addInstantiation(org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol, java.util.List)
	 */
	public void addInstantiation(IContainerSymbol instance, List args) {
		List key = new ArrayList( args );
		if( _instantiations == ObjectMap.EMPTY_MAP ){
			_instantiations = new ObjectMap(2);
		}
		_instantiations.put( key, instance );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#removeInstantiation(org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public void removeInstantiation(IContainerSymbol symbol) {
		List args = findArgumentsFor( symbol );
		if( args != null ){
			_instantiations.remove( args );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#addExplicitSpecialization(org.eclipse.cdt.internal.core.parser.pst.ISymbol, java.util.List)
	 */
	public void addExplicitSpecialization(ISymbol symbol, List args) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#instantiate(java.util.List)
	 */
	public ISymbol instantiate(List arguments) throws ParserSymbolTableException {
		if( getType() != ITypeInfo.t_template &&
			( getType() != ITypeInfo.t_templateParameter || 
				getTypeInfo().getTemplateParameterType() != ITypeInfo.t_template ) )
		{
			return null;
		}
		
		UndefinedTemplateSymbol instance = (UndefinedTemplateSymbol) findInstantiation( arguments ); 
		if (instance == null) {
			// clone and store the arguments
			instance = (UndefinedTemplateSymbol) getSymbolTable().newUndefinedTemplateSymbol(getName(), getType());
			instance.setArgumentList(arguments);
			instance.setInstantiatedSymbol(this);

			addInstantiation(instance, arguments);
		}
		return instance;
	}

	/**
	 * @return Returns the _argumentList.
	 */
	public List getArgumentList() {
		return _argumentList;
	}
	/**
	 * @param list The _argumentList to set.
	 */
	protected void setArgumentList(List list) {
		_argumentList = new ArrayList(list);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#instantiate(org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol, org.eclipse.cdt.core.parser.util.ObjectMap)
	 */
	public ISymbol instantiate(ITemplateSymbol template, ObjectMap argMap) throws ParserSymbolTableException {
		if( !isTemplateMember() || template == null ) {
			return null;
		}
		if (getContainingSymbol() instanceof UndefinedTemplateSymbol) {
			// instantiate containing symbol
			ISymbol containingSymbol = ((UndefinedTemplateSymbol) getContainingSymbol()).instantiate( template, argMap );
			// now lookup for the symbol in containing symbol's list of contained symbols
			if (containingSymbol instanceof IContainerSymbol) {
				ISymbol symbol;
				if (isType(ITypeInfo.t_template)) {
					symbol = ((IContainerSymbol) containingSymbol).lookupTemplateId(getName(), getArgumentList());
				}
				else {
					symbol = ((IContainerSymbol) containingSymbol).lookup(getName());
				}
				if (symbol instanceof IDeferredTemplateInstance) {
					symbol = ((IDeferredTemplateInstance) symbol).getTemplate();
				}
				if (symbol instanceof ITemplateSymbol) {
					symbol = ((ITemplateSymbol) symbol).getTemplatedSymbol();
				}
				return symbol;
			}
			
			throw new ParserSymbolTableException(ParserSymbolTableException.r_BadTemplateArgument);
			
		}
		else if (isType(ITypeInfo.t_templateParameter) && argMap.containsKey(this)) {
			return ((ITypeInfo)argMap.get(this)).getTypeSymbol();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#deferredInstance(java.util.List)
	 */
	public IDeferredTemplateInstance deferredInstance(List args) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#getExplicitSpecializations()
	 */
	public ObjectMap getExplicitSpecializations() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#getNumberDeferredInstantiations()
	 */
	public int getNumberDeferredInstantiations() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol#registerDeferredInstatiation(java.lang.Object, java.lang.Object, org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol.DeferredKind, org.eclipse.cdt.core.parser.util.ObjectMap)
	 */
	public void registerDeferredInstatiation(Object obj0, Object obj1, DeferredKind kind, ObjectMap argMap) {
	}
}
