/*******************************************************************************
 * Copyright (c) 2003,2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Nov 4, 2003
 */
 
package org.eclipse.cdt.internal.core.parser.pst;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.IASTMember;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable.LookupData;
import org.eclipse.cdt.internal.core.parser.scanner2.CharArraySet;
import org.eclipse.cdt.internal.core.parser.scanner2.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectSet;
import org.eclipse.cdt.internal.core.parser.scanner2.CharArrayObjectMap;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ContainerSymbol extends BasicSymbol implements IContainerSymbol {

	protected ContainerSymbol( ParserSymbolTable table, char[] name ){
		super( table, name );
	}
	
	protected ContainerSymbol( ParserSymbolTable table, char[] name, ITypeInfo.eType typeInfo ){
		super( table, name, typeInfo );
	}
	
	public Object clone(){
		ContainerSymbol copy = (ContainerSymbol)super.clone();
			
		copy._usingDirectives  =  (_usingDirectives != Collections.EMPTY_LIST) ? (List) ((ArrayList)_usingDirectives).clone() : _usingDirectives;
		copy._containedSymbols = (CharArrayObjectMap) ( ( _containedSymbols != CharArrayObjectMap.EMPTY_MAP )? _containedSymbols.clone() : _containedSymbols );
		copy._contents = (_contents != Collections.EMPTY_LIST) ? (List) ((ArrayList)_contents).clone() : _contents;
		
		return copy;	
	}
	
	public ISymbol instantiate( ITemplateSymbol template, Map argMap ) throws ParserSymbolTableException{
		if( !isTemplateMember() || template == null ){
			return null;
		}
		
		ContainerSymbol newContainer = (ContainerSymbol) super.instantiate( template, argMap );

		Iterator iter = getContentsIterator();
	
		newContainer.getContainedSymbols().clear();
		if( !newContainer._contents.isEmpty()  ){
			newContainer._contents.clear();
			
			IExtensibleSymbol containedSymbol = null;
			ISymbol newSymbol = null;
			
			while( iter.hasNext() ){
				containedSymbol = (IExtensibleSymbol) iter.next();
				
				if( containedSymbol instanceof IUsingDirectiveSymbol ){
					newContainer._contents.add( containedSymbol );
				} else {
					ISymbol symbol = (ISymbol) containedSymbol;
					if( symbol.isForwardDeclaration() && symbol.getForwardSymbol() != null ){
						continue;
					}
					
					if( !template.getExplicitSpecializations().isEmpty() ){
						List params = template.getParameterList();
						int size = template.getParameterList().size();
						List argList = new ArrayList( size );
						boolean hasAllParams = true;
						for( int i = 0; i < size; i++ ){
							Object obj = argMap.get( params.get( i ) );
							if( obj == null ){
								hasAllParams = false;
								break;
							}
							argList.add( obj );
						}
						if( hasAllParams){
							ISymbol temp = TemplateEngine.checkForTemplateExplicitSpecialization( template, symbol, argList );
							if( temp != null )
								containedSymbol = temp;
						}
					}
					
					Map instanceMap = argMap;
					if( !template.getDefinitionParameterMap().isEmpty() && 
						template.getDefinitionParameterMap().containsKey( containedSymbol ) )
					{
						Map defMap = (Map) template.getDefinitionParameterMap().get( containedSymbol );
						instanceMap = new HashMap();
						Iterator i = defMap.keySet().iterator();
						while( i.hasNext() ){
							ISymbol p = (ISymbol) i.next();
							instanceMap.put( p, argMap.get( defMap.get( p ) ) );
						}
					}
					
					newSymbol = ((ISymbol)containedSymbol).instantiate( template, instanceMap );
					newSymbol.setContainingSymbol( newContainer );
					newContainer._contents.add( newSymbol );
					
					if( newSymbol instanceof IParameterizedSymbol && newSymbol.isType( ITypeInfo.t_constructor ) ){
						collectInstantiatedConstructor( (IParameterizedSymbol) containedSymbol );	
					} else {
						if( newContainer.getContainedSymbols().containsKey( newSymbol.getName() ) ){
							Object obj = newContainer.getContainedSymbols().get( newSymbol.getName() );
							if( obj instanceof List ){
								((List) obj).add( newSymbol );
							} else {
								List list = new ArrayList(4);
								list.add( obj );
								list.add( newSymbol );
								newContainer.putInContainedSymbols( newSymbol.getName(), list );
							}
						} else {
							newContainer.putInContainedSymbols( newSymbol.getName(), newSymbol );
						}
					}
				}
			}
		}
		
		return newContainer;	
	}
	
	protected void collectInstantiatedConstructor( IParameterizedSymbol constructor ){
		throw new ParserSymbolTableError();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addSymbol(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addSymbol( ISymbol obj ) throws ParserSymbolTableException{
		IContainerSymbol containing = this;
		
		//handle enumerators
		if( obj.getType() == ITypeInfo.t_enumerator ){
			//a using declaration of an enumerator will not be contained in a
			//enumeration.
			if( containing.getType() == ITypeInfo.t_enumeration ){
				//Following the closing brace of an enum-specifier, each enumerator has the type of its 
				//enumeration
				obj.setTypeSymbol( containing );
				//Each enumerator is declared in the scope that immediately contains the enum-specifier	
				containing = containing.getContainingSymbol();
			}
		}
	
		if( obj.isType( ITypeInfo.t_template ) ){
			if( ! TemplateEngine.canAddTemplate( containing, (ITemplateSymbol) obj ) ) {
				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
			}
		}
		
		//in C, structs, unions, enums don't nest
		if( getSymbolTable().getLanguage() == ParserLanguage.C ){
			if( obj.isType( ITypeInfo.t_struct, ITypeInfo.t_enumeration ) ){
				containing = getScopeForCTag( containing );
			}
		}
		
		//14.6.1-4 A Template parameter shall not be redeclared within its scope.
		if( isTemplateMember() || isType( ITypeInfo.t_template ) ){
			if( TemplateEngine.alreadyHasTemplateParameter( this, obj.getName() ) ){
				throw new ParserSymbolTableException( ParserSymbolTableException.r_RedeclaredTemplateParam );	
			}
		}
	
		boolean unnamed = CharArrayUtils.equals( obj.getName(), ParserSymbolTable.EMPTY_NAME_ARRAY );
	
		Object origObj = null;
	
		obj.setContainingSymbol( containing );

		//does this name exist already?
		origObj = containing.getContainedSymbols().get( obj.getName() );
	
		if( origObj != null )
		{
			ISymbol origDecl = null;
			ArrayList  origList = null;
	
			if( origObj instanceof ISymbol ){
				origDecl = (ISymbol)origObj;
			} else if( origObj.getClass() == ArrayList.class ){
				origList = (ArrayList)origObj;
			} else {
				throw new ParserSymbolTableError( ParserSymbolTableError.r_InternalError );
			}
		
			boolean validOverride =  ( !unnamed ? ( (origList == null) ? ParserSymbolTable.isValidOverload( origDecl, obj ) 
																	   : ParserSymbolTable.isValidOverload( origList, obj ) )
											    : true );
			
			if( unnamed || validOverride )
			{	
				if( origList == null ){
					origList = new ArrayList(4);
					origList.add( origDecl );
					origList.add( obj );
			
					((ContainerSymbol)containing).putInContainedSymbols( obj.getName(), origList );
				} else	{
					origList.add( obj );
					//origList is already in _containedDeclarations
				}
			} else {
				throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidOverload );
			}
		} else {
			((ContainerSymbol)containing).putInContainedSymbols( obj.getName(), obj );
		}
	
		obj.setIsTemplateMember( isTemplateMember() || getType() == ITypeInfo.t_template );
		
		addToContents( obj );
		
//		Command command = new AddSymbolCommand( obj, containing );
//		getSymbolTable().pushCommand( command );
	}

//	public boolean removeSymbol( ISymbol symbol ){
//		boolean removed = false;
//		
//		StringObjectMap contained = getContainedSymbols();
//		
//		if( symbol != null && contained.containsKey( symbol.getName() ) ){
//			Object obj = contained.get( symbol.getName() );
//			if( obj instanceof ISymbol ){
//				if( obj == symbol ){
//					contained.remove( symbol.getName() );
//					removed = true;
//				}
//			} else if ( obj instanceof List ){
//				List list = (List) obj;
//				if( list.remove( symbol ) ){
//					if( list.size() == 1 ){
//						contained.put( symbol.getName(), list.get( 0 ) );
//					}
//					removed = true;
//				}
//			}
//		}
//		
//		if( removed ){
//			ListIterator iter = getContents().listIterator( getContents().size() );
//			while( iter.hasPrevious() ){
//				if( iter.previous() == symbol ){
//					iter.remove();
//					break;
//				}
//			}
//		}
//		
//		return removed;
//	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#hasUsingDirectives()
	 */
	public boolean hasUsingDirectives(){
		return !_usingDirectives.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#getUsingDirectives()
	 */
	public List getUsingDirectives(){
		return _usingDirectives;
	}
	
	protected void addToUsingDirectives( IExtensibleSymbol symbol ){
		if( _usingDirectives == Collections.EMPTY_LIST )
			_usingDirectives = new ArrayList(4);
		_usingDirectives.add( symbol );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDirective(org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public IUsingDirectiveSymbol addUsingDirective( IContainerSymbol namespace ) throws ParserSymbolTableException{
		if( namespace.getType() != ITypeInfo.t_namespace ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidUsing );
		}
		//7.3.4 A using-directive shall not appear in class scope
		if( isType( ITypeInfo.t_class, ITypeInfo.t_union ) ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidUsing );
		}
		
		//handle namespace aliasing
		ISymbol alias = namespace.getForwardSymbol();
		if( alias != null && alias.isType( ITypeInfo.t_namespace ) ){
			namespace = (IContainerSymbol) alias;
		}
		
		IUsingDirectiveSymbol usingDirective = new UsingDirectiveSymbol( getSymbolTable(), namespace );
		
		addToUsingDirectives( usingDirective );
		addToContents( usingDirective );
		
//		Command command = new AddUsingDirectiveCommand( this, usingDirective );
//		getSymbolTable().pushCommand( command );
		
		return usingDirective;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDeclaration(java.lang.String)
	 */
	/**
	 * addUsingDeclaration
	 * @param obj
	 * @throws ParserSymbolTableException
	 * 
	 * 7.3.3-9  The entity declared by a using-declaration shall be known in the
	 * context using it according to its definition at the point of the using-
	 * declaration.  Definitions added to the namespace after the using-
	 * declaration are not considered when a use of the name is made.
	 * 
	 * 7.3.3-4 A using-declaration used as a member-declaration shall refer to a
	 * member of a base class of the class being defined, shall refer to a
	 * member of an anonymous union that is a member of a base class of the
	 * class being defined, or shall refer to an enumerator for an enumeration
	 * type that is a member of a base class of the class being defined.
	 */
	public IUsingDeclarationSymbol addUsingDeclaration( char[] name ) throws ParserSymbolTableException {
		return addUsingDeclaration( name, null );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDeclaration(java.lang.String, org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public IUsingDeclarationSymbol addUsingDeclaration( char[] name, IContainerSymbol declContext ) throws ParserSymbolTableException{
	    LookupData data = new LookupData( name );

		if( declContext != null ){				
			data.qualified = true;
			ParserSymbolTable.lookup( data, declContext );
		} else {
			ParserSymbolTable.lookup( data, this );
		}

		//figure out which declaration we are talking about, if it is a set of functions,
		//then they will be in data.foundItems (since we provided no parameter info);
		ISymbol symbol = null;
		ISymbol clone = null;
		int objListSize = 0;
		List objList = null;
		
		try{
			symbol = getSymbolTable().resolveAmbiguities( data );
		} catch ( ParserSymbolTableException e ) {
			if( e.reason != ParserSymbolTableException.r_UnableToResolveFunction ){
				throw e;
			}
		}

		if( symbol == null && (data.foundItems == null || data.foundItems.isEmpty()) ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidUsing );				
		}

		if( symbol == null ){
			Object object = data.foundItems.get( data.name );
			objList = ( object instanceof List ) ? (List) object : null;
			objListSize = ( objList != null ) ? objList.size() : 0;
			symbol = ( objListSize > 0 ) ? (ISymbol)objList.get(0) : null;
		}

		List usingDecs = new ArrayList( ( objListSize > 0 ) ? objListSize : 1 );
		List usingRefs = new ArrayList( ( objListSize > 0 ) ? objListSize : 1 );
		
		UsingDeclarationSymbol usingDeclaration = new UsingDeclarationSymbol( getSymbolTable(), usingRefs, usingDecs );
		boolean addedUsingToContained = false;
		int idx = 1;
		while( symbol != null ){
			if( ParserSymbolTable.okToAddUsingDeclaration( symbol, this ) ){
				if( ! addedUsingToContained ){
					addToContents( usingDeclaration );
					addedUsingToContained = true;
				}
				clone = (ISymbol) symbol.clone(); //7.3.3-9
				addSymbol( clone );
			} else {
				throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidUsing );
			}
			
			usingDecs.add( clone );
			usingRefs.add( symbol );
			
			if( objList != null && idx < objListSize ){
				symbol = (ISymbol) objList.get( idx++ );
			} else {
				symbol = null;
			}
		}
		
		return usingDeclaration;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#getContainedSymbols()
	 */
	public CharArrayObjectMap getContainedSymbols(){
		return _containedSymbols;
	}
	
	protected void putInContainedSymbols( char[] key, Object obj ){
		if( _containedSymbols == CharArrayObjectMap.EMPTY_MAP ){
			_containedSymbols = new CharArrayObjectMap( 4 );
		}
		_containedSymbols.put( key, obj );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#elaboratedLookup(org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType, java.lang.String)
	 */
	public ISymbol elaboratedLookup( final ITypeInfo.eType type, char[] name ) throws ParserSymbolTableException{
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
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookup(java.lang.String)
	 */
	public ISymbol lookup( char[] name ) throws ParserSymbolTableException {
		LookupData data = new LookupData( name );
	
		ParserSymbolTable.lookup( data, this );
	
		ISymbol found = getSymbolTable().resolveAmbiguities( data );
		
		if( isTemplateMember() && found instanceof ITemplateSymbol ) {
			return TemplateEngine.instantiateWithinTemplateScope( this, (ITemplateSymbol) found );
		}
		
		return found;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupMemberForDefinition(java.lang.String)
	 */
	/**
	 * LookupMemberForDefinition
	 * @param name
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 * In a definition for a namespace member in which the declarator-id is a
	 * qualified-id, given that the qualified-id for the namespace member has
	 * the form "nested-name-specifier unqualified-id", the unqualified-id shall
	 * name a member of the namespace designated by the nested-name-specifier.
	 * 
	 * ie:
	 * you have this:
	 * namespace A{    
	 *    namespace B{       
	 *       void  f1(int);    
	 *    }  
	 *    using  namespace B; 
	 * }
	 * 
	 * if you then do this 
	 * void A::f1(int) { ... } //ill-formed, f1 is not a member of A
	 * but, you can do this (Assuming f1 has been defined elsewhere)
	 * A::f1( 1 );  //ok, finds B::f1
	 * 
	 * ie, We need a seperate lookup function for looking up the member names
	 * for a definition.
	 */
	public ISymbol lookupMemberForDefinition( char[] name ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name );
		data.qualified = true;
		
		IContainerSymbol container = this;
		
		//handle namespace aliases
		if( container.isType( ITypeInfo.t_namespace ) ){
			ISymbol symbol = container.getForwardSymbol();
			if( symbol != null && symbol.isType( ITypeInfo.t_namespace ) ){
				container = (IContainerSymbol) symbol;
			}
		}
		
		data.foundItems = ParserSymbolTable.lookupInContained( data, container );
		if( data.foundItems != null )
			return getSymbolTable().resolveAmbiguities( data );
		return null;
	}

	public IParameterizedSymbol lookupMethodForDefinition( char[] name, final List parameters ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name ){
			public List getParameters() { return params; }
			final private List params = ( parameters == null ) ? Collections.EMPTY_LIST : parameters;
		};
		data.qualified = true;
		data.exactFunctionsOnly = true;
		
		IContainerSymbol container = this;
		
		//handle namespace aliases
		if( container.isType( ITypeInfo.t_namespace ) ){
			ISymbol symbol = container.getForwardSymbol();
			if( symbol != null && symbol.isType( ITypeInfo.t_namespace ) ){
				container = (IContainerSymbol) symbol;
			}
		}
		
		data.foundItems = ParserSymbolTable.lookupInContained( data, container );
		
		if( data.foundItems != null ){
			ISymbol symbol = getSymbolTable().resolveAmbiguities( data ); 
			return (IParameterizedSymbol) (( symbol instanceof IParameterizedSymbol ) ? symbol : null);
		} 
		
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupNestedNameSpecifier(java.lang.String)
	 */
	/**
	 * Method LookupNestedNameSpecifier.
	 * @param name
	 * @return Declaration
	 * The name of a class or namespace member can be referred to after the ::
	 * scope resolution operator applied to a nested-name-specifier that
	 * nominates its class or namespace.  During the lookup for a name preceding
	 * the ::, object, function and enumerator names are ignored.  If the name
	 * is not a class-name or namespace-name, the program is ill-formed
	 */
	public IContainerSymbol lookupNestedNameSpecifier( char[] name ) throws ParserSymbolTableException {
		return lookupNestedNameSpecifier( name, this );
	}
	private IContainerSymbol lookupNestedNameSpecifier(char[] name, IContainerSymbol inSymbol ) throws ParserSymbolTableException{		
		ISymbol foundSymbol = null;
	
		final TypeFilter filter = new TypeFilter( ITypeInfo.t_namespace );
		filter.addAcceptedType( ITypeInfo.t_class );
		filter.addAcceptedType( ITypeInfo.t_struct );
		filter.addAcceptedType( ITypeInfo.t_union );
		
		LookupData data = new LookupData( name ){
			public TypeFilter getFilter() { return typeFilter; }
			final private TypeFilter typeFilter = filter; 
		};
		
		data.foundItems = ParserSymbolTable.lookupInContained( data, inSymbol );
	
		if( data.foundItems != null ){
			foundSymbol = getSymbolTable().resolveAmbiguities( data );
		}
			
		if( foundSymbol == null && inSymbol.getContainingSymbol() != null ){
			foundSymbol = lookupNestedNameSpecifier( name, inSymbol.getContainingSymbol() );
		}
		
		if( foundSymbol instanceof IContainerSymbol )
			return (IContainerSymbol) foundSymbol;
 
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#qualifiedLookup(java.lang.String)
	 */
	public ISymbol qualifiedLookup( char[] name ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name );
		data.qualified = true;
		ParserSymbolTable.lookup( data, this );
	
		return getSymbolTable().resolveAmbiguities( data );	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#qualifiedLookup(java.lang.String, org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType)
	 */
	public ISymbol qualifiedLookup( char[] name, final ITypeInfo.eType t ) throws ParserSymbolTableException{
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
	
		return getSymbolTable().resolveAmbiguities( data ); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#unqualifiedFunctionLookup(java.lang.String, java.util.List)
	 */
	/**
	 * UnqualifiedFunctionLookup
	 * @param name
	 * @param parameters
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 * 3.4.2-1 When an unqualified name is used as the post-fix expression in a
	 * function call, other namespaces not consdiered during the usual
	 * unqualified lookup may be searched.
	 * 
	 * 3.4.2-2 For each argument type T in the function call, there is a set of
	 * zero or more associated namespaces and a set of zero or more associated
	 * classes to be considered.
	 * 
	 * If the ordinary unqualified lookup of the name find the declaration of a
	 * class member function, the associated namespaces and classes are not
	 * considered.  Otherwise, the set of declarations found by the lookup of
	 * the function name is the union of the set of declarations found using
	 * ordinary unqualified lookup and the set of declarations found in the
	 * namespaces and classes associated with the argument types.
	 */
	public IParameterizedSymbol unqualifiedFunctionLookup( char[] name, final List parameters ) throws ParserSymbolTableException{
		//figure out the set of associated scopes first, so we can remove those that are searched
		//during the normal lookup to avoid doing them twice
		final ObjectSet associated = new ObjectSet(0);
	
		//collect associated namespaces & classes.
		int size = ( parameters == null ) ? 0 : parameters.size();
			
		ITypeInfo param = null;
		ISymbol paramType = null;
		for( int i = 0; i < size; i++ ){
			param = (ITypeInfo) parameters.get(i);
			ITypeInfo info = ParserSymbolTable.getFlatTypeInfo( param, getSymbolTable().getTypeInfoProvider() );
			paramType = info.getTypeSymbol();
			getSymbolTable().getTypeInfoProvider().returnTypeInfo( info );
		
			if( paramType == null ){
				continue;
			}
				
			ParserSymbolTable.getAssociatedScopes( paramType, associated );
		
			//if T is a pointer to a data member of class X, its associated namespaces and classes
			//are those associated with the member type together with those associated with X
			if( param.hasPtrOperators() && param.getPtrOperators().size() == 1 ){
				ITypeInfo.PtrOp op = (ITypeInfo.PtrOp)param.getPtrOperators().get(0);
				if( op.getType() == ITypeInfo.PtrOp.t_pointer && 
					paramType.getContainingSymbol().isType( ITypeInfo.t_class, ITypeInfo.t_union ) )
				{
					ParserSymbolTable.getAssociatedScopes( paramType.getContainingSymbol(), associated );	
				}
			}
		}
	
		LookupData data = new LookupData( name ){
			public ObjectSet getAssociated() { return assoc; }
			public List      getParameters() { return params; }
			public TypeFilter getFilter()    { return FUNCTION_FILTER; }
			
			final private ObjectSet assoc = associated;
			final private List params = ( parameters == null ) ? Collections.EMPTY_LIST : parameters;
		};
		
		ParserSymbolTable.lookup( data, this );
	
		ISymbol found = getSymbolTable().resolveAmbiguities( data );
	
		//if we haven't found anything, or what we found is not a class member, consider the 
		//associated scopes
		if( found == null || found.getContainingSymbol().getType() != ITypeInfo.t_class ){
//			if( found != null ){
//				data.foundItems.add( found );
//			}
								
			IContainerSymbol associatedScope;
			//dump the hash to an array and iterate over the array because we
			//could be removing items from the collection as we go and we don't
			//want to get ConcurrentModificationExceptions			
			Object [] scopes = associated.keyArray();
		
			size = associated.size();

			for( int i = 0; i < size; i++ ){
				associatedScope  = (IContainerSymbol) scopes[ i ];
				if( associated.containsKey( associatedScope ) ){
					data.qualified = true;
					data.ignoreUsingDirectives = true;
					data.usingDirectivesOnly = false;
					ParserSymbolTable.lookup( data, associatedScope );
				}
			}
		
			found = getSymbolTable().resolveAmbiguities( data );
		}
	
		if( found instanceof IParameterizedSymbol )
			return (IParameterizedSymbol) found;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#memberFunctionLookup(java.lang.String, java.util.List)
	 */
	/**
	 * MemberFunctionLookup
	 * @param name
	 * @param parameters
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 * Member lookup really proceeds as an unqualified lookup, but doesn't
	 * include argument dependant scopes
	 */
	public IParameterizedSymbol memberFunctionLookup( char[] name, final List parameters ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name ){
			public List getParameters() { return params; }
			final private List params = ( parameters == null ) ? Collections.EMPTY_LIST : parameters;
			public TypeFilter getFilter() { return FUNCTION_FILTER; }
		};
		ParserSymbolTable.lookup( data, this );
		return (IParameterizedSymbol) getSymbolTable().resolveAmbiguities( data ); 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#qualifiedFunctionLookup(java.lang.String, java.util.List)
	 */
	public IParameterizedSymbol qualifiedFunctionLookup( char[] name, final List parameters ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name ){
			public List getParameters() { return params; }
			final private List params = ( parameters == null ) ? Collections.EMPTY_LIST : parameters;
			public TypeFilter getFilter() { return FUNCTION_FILTER; }
		};
		data.qualified = true;
	
		ParserSymbolTable.lookup( data, this );
	
		return (IParameterizedSymbol) getSymbolTable().resolveAmbiguities( data ); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#templateLookup(java.lang.String, java.util.List)
	 */
	public ISymbol lookupTemplateId( char[] name, List arguments ) throws ParserSymbolTableException
	{
		LookupData data = new LookupData( name );
		
		ParserSymbolTable.lookup( data, this );
		ISymbol found = getSymbolTable().resolveAmbiguities( data );
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
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupFunctionTemplateId(java.lang.String, java.util.List, java.util.List)
	 */
	public ISymbol lookupFunctionTemplateId(char[] name, final List parameters, final List arguments, boolean forDefinition) throws ParserSymbolTableException {
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
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupTemplateIdForDefinition(java.lang.String, java.util.List)
	 */
	public IContainerSymbol lookupTemplateIdForDefinition(char[] name, List arguments){
		// TODO Auto-generated method stub
		return null;
	}
	
	public List prefixLookup( final TypeFilter filter, char[] prefix, boolean qualified, final List paramList ) throws ParserSymbolTableException{
		LookupData data = new LookupData( prefix ){
			public List 	getParameters() { return params;      }
			public boolean 	isPrefixLookup(){ return true;        }
			public CharArraySet  getAmbiguities(){ return ambiguities; }
			public TypeFilter getFilter() { return typeFilter; }
			
			public void addAmbiguity( char[] n ){
				if( ambiguities == CharArraySet.EMPTY_SET ){
					ambiguities = new CharArraySet(2);
				}
				ambiguities.put( n );
			}
			
			final private List params = paramList;
			private CharArraySet ambiguities = CharArraySet.EMPTY_SET;	
			final private TypeFilter typeFilter = filter;
		};
		
		data.qualified = qualified;
		
		ParserSymbolTable.lookup( data, this );
		
		List constructors = null;
		if( filter != null && filter.willAccept( ITypeInfo.t_constructor ) && (this instanceof IDerivableContainerSymbol) ){
			if( CharArrayUtils.equals( getName(), 0, prefix.length, prefix, true ) ){
				List temp = ((IDerivableContainerSymbol)this).getConstructors();
				int size = temp.size();
				constructors = new ArrayList( size );
				for( int i = 0; i < size; i++ )
					constructors.add( temp.get( i ) );
			}
		}
		
		if( data.foundItems == null || data.foundItems.isEmpty() ){
			if( constructors != null ){
				if( paramList != null ){
					getSymbolTable().resolveFunction( data, constructors );
					return constructors;
				} 
				return constructors;
			} 
			return null;
		}
		
		List list = new ArrayList();
		
		Object obj = null;
		char[] key = null;
		List tempList = null;
		int size = data.foundItems.size();
		for( int i = 0; i < size; i++ ){
		    key = data.foundItems.keyAt( i );
		    
		    //skip ambiguous symbols
		    if( data.getAmbiguities() != null && data.getAmbiguities().containsKey( key ) )
		        continue;
		    
			obj = data.foundItems.get( key );
			
			if( obj instanceof List ){
				//a list must be all functions?
				if( paramList != null )
					getSymbolTable().resolveFunction( data, (List) obj );
				list.addAll( (List) obj );
			} else{
				if( paramList != null && ((ISymbol)obj).isType( ITypeInfo.t_function ) )
				{
					if( tempList == null )
						tempList = new ArrayList(1);
					else 
						tempList.clear();
					tempList.add( obj );
					getSymbolTable().resolveFunction( data, tempList );
					list.addAll( tempList );
				} else {
					list.add( obj );
				}
			}
		}

		if( constructors != null )
			list.addAll( constructors );
		
		return list;
		
	}
	
	public boolean isVisible( ISymbol symbol, IContainerSymbol qualifyingSymbol ){
		ISymbolASTExtension extension = symbol.getASTExtension();
		if(extension == null)
			return true;
		IASTNode node = extension.getPrimaryDeclaration();
		if(node == null)
			return true;
		
		if( node instanceof IASTMember ){
			ASTAccessVisibility visibility;
			
			visibility = ParserSymbolTable.getVisibility( symbol, qualifyingSymbol );
			if( visibility == null )
				return false;
			
			if( visibility == ASTAccessVisibility.PUBLIC ){
				return true;
			}
			
			IContainerSymbol container = getContainingSymbol();
			IContainerSymbol symbolContainer = symbol.getContainingSymbol();
			
			if( !symbolContainer.isType( ITypeInfo.t_class, ITypeInfo.t_union ) ||
				symbolContainer.equals( container ) )
			{
				return true;
			}

			//if this is a friend of the symbolContainer, then we are good
			if( isFriendOf( ( qualifyingSymbol != null ) ? qualifyingSymbol : symbolContainer ) ){
				return true;
			}
			
			if( visibility == ASTAccessVisibility.PROTECTED )
			{
				try {
					return ( ParserSymbolTable.hasBaseClass( container, symbolContainer ) >= 0 );
				} catch (ParserSymbolTableException e) {
					return false;
				}
			}
			return false; 
		}
		return true;
	}
	
	protected boolean isFriendOf( IContainerSymbol symbol ){
		if( symbol instanceof IDerivableContainerSymbol ){
			IContainerSymbol container = this.getContainingSymbol();
			
			while( container != null && container.isType( ITypeInfo.t_block ) ){
				container = container.getContainingSymbol();
			}
			if( container != null && !container.isType( ITypeInfo.t_class, ITypeInfo.t_union ) ){
				container = null;
			}
			
			IDerivableContainerSymbol derivable = (IDerivableContainerSymbol) symbol;
			
			List friends = derivable.getFriends();
			int size = friends.size();
			for( int i = 0; i < size; i++ ){
				ISymbol friend = (ISymbol) friends.get(i);
				ISymbol forwardSymbol = friend.getForwardSymbol();
				if( friend == this      || forwardSymbol == this ||
					friend == container || ( container != null && forwardSymbol == container ) )
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private IContainerSymbol getScopeForCTag( IContainerSymbol container ){
		while( !container.isType( ITypeInfo.t_namespace ) && 
			   !container.isType( ITypeInfo.t_function )  &&
			   !container.isType( ITypeInfo.t_block ) )
		{
			container = container.getContainingSymbol();
		} 
		return container;
	}
	
	protected void addToContents( IExtensibleSymbol symbol ){
		if( _contents == Collections.EMPTY_LIST ){
			if( isType( ITypeInfo.t_namespace ) )
				_contents = new ArrayList( 64 );
			else if( isType( ITypeInfo.t_class ) || isType( ITypeInfo.t_struct ) )
				_contents = new ArrayList( 32 );
			else if( isType( ITypeInfo.t_function ) )
				_contents = new ArrayList( 16 );
			else
				_contents = new ArrayList( 8 );
		}
		_contents.add( symbol );
	}
	
	protected List getContents(){
		return _contents;
	}
	
	public Iterator getContentsIterator(){
		return new ContentsIterator( getContents() );
	}
	
	protected class ContentsIterator implements Iterator {
		final List internalList;
		private int idx = 0;
		ObjectSet alreadyReturned = new ObjectSet( 2 );
		
		public ContentsIterator( List contents ){
			internalList = contents;
		}
		
		IExtensibleSymbol next = null;
		public boolean hasNext() {
			if( next != null ){
				return true;
			}
			if( internalList.size() <= idx )
				return false;
			for( ; idx < internalList.size(); ){
				IExtensibleSymbol extensible = (IExtensibleSymbol) internalList.get(idx++);
				if( !alreadyReturned.containsKey( extensible ) ){
					if( extensible instanceof ISymbol ){
						ISymbol symbol = (ISymbol) extensible;
						ISymbol forward = symbol.getForwardSymbol();
						if( symbol.isForwardDeclaration() && forward != null &&
							forward.getContainingSymbol() == ContainerSymbol.this )
						{
							alreadyReturned.put( forward );
							next = forward;
							return true;
						}
					} else if( extensible instanceof IUsingDeclarationSymbol ){
						IUsingDeclarationSymbol using = (IUsingDeclarationSymbol) extensible;
						alreadyReturned.addAll( using.getDeclaredSymbols() );
					}
					next = extensible;
					return true;
				}
			}
			return false;
		}

		public Object next() {
			IExtensibleSymbol extensible = next;
			if( next != null ){
				next = null;
				return extensible;
			}
			
			for( ; idx < internalList.size(); ){
				extensible = (IExtensibleSymbol) internalList.get(idx++);
				if( !alreadyReturned.containsKey( extensible ) ){
					if( extensible instanceof ISymbol ){
						ISymbol symbol = (ISymbol) extensible;
						if( symbol.isForwardDeclaration() && symbol.getForwardSymbol() != null &&
							symbol.getForwardSymbol().getContainingSymbol() == ContainerSymbol.this )
						{
							alreadyReturned.put( symbol.getForwardSymbol() );
							return symbol.getForwardSymbol();
						}
					} else if( extensible instanceof IUsingDeclarationSymbol ){
						IUsingDeclarationSymbol using = (IUsingDeclarationSymbol) extensible;
						alreadyReturned.addAll( using.getDeclaredSymbols() );
					}
					return extensible;
				}
			}
			throw new NoSuchElementException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
//	static private class AddSymbolCommand extends Command{
//		AddSymbolCommand( ISymbol newDecl, IContainerSymbol context ){
//			_symbol = newDecl;
//			_context = context;
//		}
//		
//		public void undoIt(){
//			Object obj = _context.getContainedSymbols().get( _symbol.getName() );
//			
//			if( obj instanceof ArrayList ){
//				ArrayList list = (ArrayList)obj;
//				ListIterator iter = list.listIterator();
//				int size = list.size();
//				ISymbol item = null;
//				for( int i = 0; i < size; i++ ){
//					item = (ISymbol)iter.next();
//					if( item == _symbol ){
//						iter.remove();
//						break;
//					}
//				}
//				if( list.size() == 1 ){
//					_context.getContainedSymbols().put( _symbol.getName(), list.get(0) );
//				}
//			} else if( obj instanceof BasicSymbol ){
//				_context.getContainedSymbols().remove( _symbol.getName() );
//			}
//			
//			//this is an inefficient way of doing this, we can modify the interfaces if the undo starts
//			//being used often.
//			ContentsIterator iter = (ContentsIterator) _context.getContentsIterator();
//			while( iter.hasNext() ){
//				IExtensibleSymbol ext = (IExtensibleSymbol) iter.next();
//				if( ext == _symbol ){
//					iter.removeSymbol();
//					break;
//				}
//			}
//		}
//		
//		private final ISymbol          _symbol;
//		private final IContainerSymbol _context; 
//	}
	
//	static private class AddUsingDirectiveCommand extends Command{
//		public AddUsingDirectiveCommand( IContainerSymbol container, IUsingDirectiveSymbol directive ){
//			_decl = container;
//			_directive = directive;
//		}
//		public void undoIt(){
//			_decl.getUsingDirectives().remove( _directive );
//			
//			//this is an inefficient way of doing this, we can modify the interfaces if the undo starts
//			//being used often.
//			ContentsIterator iter = (ContentsIterator) _decl.getContentsIterator();
//			while( iter.hasNext() ){
//				IExtensibleSymbol ext = (IExtensibleSymbol) iter.next();
//				if( ext == _directive ){
//					iter.removeSymbol();
//					break;
//				}
//			}
//		}
//		private final IContainerSymbol _decl;
//		private final IUsingDirectiveSymbol _directive;
//	}

	static public final SymbolTableComparator comparator = new SymbolTableComparator();
	static protected class SymbolTableComparator implements Comparator{
		static final private Collator collator = Collator.getInstance();
		static { collator.setStrength( Collator.PRIMARY ); }
		public int compare( Object o1, Object o2 ){
		    String s1 = String.valueOf( (char[])o1 );
		    String s2 = String.valueOf( (char[])o2 );
			int result = collator.compare( s1, s2 );
			if( result == 0 ){
				collator.setStrength( Collator.IDENTICAL );
				result = collator.compare( s1, s2 );
				collator.setStrength( Collator.PRIMARY );
			}
			return result;
		}
		
		public boolean equals( Object obj ){
			return ( obj instanceof SymbolTableComparator );
		}
	}

	private 	List _contents = Collections.EMPTY_LIST;				//ordered list of all contents of this symbol
	private		List _usingDirectives = Collections.EMPTY_LIST;		//collection of nominated namespaces
	private		CharArrayObjectMap _containedSymbols = CharArrayObjectMap.EMPTY_MAP;		//declarations contained by us.
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addTemplateId(org.eclipse.cdt.internal.core.parser.pst.ISymbol, java.util.List)
	 */
	public void addTemplateId(ISymbol symbol, List args) throws ParserSymbolTableException {
		throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
	}
}
