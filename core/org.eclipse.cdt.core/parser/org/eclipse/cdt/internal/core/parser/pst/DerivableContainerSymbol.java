/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Nov 6, 2003
 */
 
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable.LookupData;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DerivableContainerSymbol extends ContainerSymbol implements IDerivableContainerSymbol {

	protected DerivableContainerSymbol( ParserSymbolTable table, char[] name ){
		super( table, name );
	}

	protected DerivableContainerSymbol( ParserSymbolTable table, char[] name, ITypeInfo.eType typeInfo ){
		super( table, name, typeInfo );
	}
	
	
	public Object clone(){
		DerivableContainerSymbol copy = (DerivableContainerSymbol)super.clone();
			
		copy._parentScopes = ( _parentScopes != Collections.EMPTY_LIST ) ? (List)((ArrayList)_parentScopes).clone() : _parentScopes;
		copy._constructors = ( _constructors != Collections.EMPTY_LIST ) ? (List)((ArrayList) _constructors).clone() : _constructors;
		copy._friends      = ( _friends != Collections.EMPTY_LIST ) ? (List)((ArrayList) _friends).clone() : _friends;
			
		return copy;	
	}
	
	public ISymbol instantiate( ITemplateSymbol template, ObjectMap argMap ) throws ParserSymbolTableException{
		if( !isTemplateMember() ){
			return null;
		}
		
		DerivableContainerSymbol newSymbol = (DerivableContainerSymbol) super.instantiate( template, argMap );
		
		List parents = getParents();
		int size = parents.size();
		newSymbol.getParents().clear();
		ParentWrapper wrapper = null;
		for( int i = 0; i < size; i++ ){
			wrapper = (ParentWrapper) parents.get(i);
			ISymbol parent = wrapper.getParent();
			if( parent == null )
				continue; 
			
			if( parent instanceof IDeferredTemplateInstance ){
				template.registerDeferredInstatiation( newSymbol, parent, ITemplateSymbol.DeferredKind.PARENT, argMap );
			} else 	if( parent.isType( ITypeInfo.t_templateParameter ) && argMap.containsKey( parent ) ){
				ITypeInfo info = (ITypeInfo) argMap.get( parent );
				parent = info.getTypeSymbol();
			}
			newSymbol.addParent( parent, wrapper.isVirtual(), wrapper.getAccess(), wrapper.getOffset(), wrapper.getReferences() );
		}
		
		//TODO: friends
		
		return newSymbol;	
	}
	
	public void instantiateDeferredParent( ISymbol parent, ITemplateSymbol template, ObjectMap argMap ) throws ParserSymbolTableException{
		List parents = getParents();
		int size = parents.size();
		ParentWrapper w = null;
		for( int i = 0; i < size; i++ ){
			w = (ParentWrapper) parents.get(i);
			if( w.getParent() == parent ){
				w.setParent( parent.instantiate( template, argMap ) );
			}
		}
	}
	
	/**
	 * @param symbol
	 * @param symbol2
	 * @param map
	 */
	public void discardDeferredParent(IDeferredTemplateInstance parent, ITemplateSymbol template, ObjectMap map) {
		List parents = getParents();
		int size = parents.size();
		ParentWrapper w = null;
		ISymbol originalParent = parent.getTemplate().getTemplatedSymbol();
		for( int i = 0; i < size; i++ ){
			w = (ParentWrapper) parents.get(i);
			ISymbol instance = w.getParent();
			if( instance.getInstantiatedSymbol() == originalParent ){
				parents.remove( i );
				template.removeInstantiation( (IContainerSymbol) instance );
				break;
			}
		}
	}
	
	protected void collectInstantiatedConstructor( IParameterizedSymbol constructor ){
		if( constructor.isType( ITypeInfo.t_constructor ) )
			addToConstructors( constructor );
	}
	
	public void addSymbol(ISymbol symbol) throws ParserSymbolTableException {
		super.addSymbol( symbol );
					
		//take care of the this pointer
		if( symbol instanceof IParameterizedSymbol ){
			addThis( (IParameterizedSymbol) symbol );
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#addParent(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addParent( ISymbol parent ){
		addParent( parent, false, ASTAccessVisibility.PUBLIC, -1, null );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#addParent(org.eclipse.cdt.internal.core.parser.pst.ISymbol, boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility, int, java.util.List)
	 */
	public void addParent( ISymbol parent, boolean virtual, ASTAccessVisibility visibility, int offset, List references ){
		if( _parentScopes == Collections.EMPTY_LIST ){
			_parentScopes = new ArrayList(4);
		}
		
		ParentWrapper wrapper = new ParentWrapper( parent, virtual, visibility, offset, references );
		_parentScopes.add( wrapper );
		
//		Command command = new AddParentCommand( this, wrapper );
//		getSymbolTable().pushCommand( command );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#getParents()
	 */
	public List getParents(){
		return _parentScopes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#hasParents()
	 */
	public boolean hasParents(){
		return !_parentScopes.isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#addConstructor(org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol)
	 */
	public void addConstructor(IParameterizedSymbol constructor) throws ParserSymbolTableException {
		if( !constructor.isType( ITypeInfo.t_constructor ) )
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTypeInfo );
			
		List constructors = getConstructors();

		if( constructors.size() == 0 || ParserSymbolTable.isValidOverload( constructors, constructor ) ){
			addToConstructors( constructor );
		} else {
			throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidOverload );
		}
		
		constructor.setContainingSymbol( this );
		constructor.setIsTemplateMember( isTemplateMember() || getType() == ITypeInfo.t_template );
		
		addThis( constructor );

		addToContents( constructor );
		
//		Command command = new AddConstructorCommand( constructor, this );
//		getSymbolTable().pushCommand( command );			
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#addCopyConstructor()
	 */
	public void addCopyConstructor() throws ParserSymbolTableException{
		List parameters = new ArrayList(1);
		
		ISymbol paramType = this;
		if( getContainingSymbol() instanceof ITemplateSymbol ){
			paramType = TemplateEngine.instantiateWithinTemplateScope( this, (ITemplateSymbol) getContainingSymbol() );
		}
		
		ITypeInfo param = getSymbolTable().getTypeInfoProvider().getTypeInfo( ITypeInfo.t_type );
		param.setType( ITypeInfo.t_type );
		param.setBit( true, ITypeInfo.isConst );
		param.setTypeSymbol( paramType );
		param.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference, false, false ) );
		parameters.add( param );
		
		IParameterizedSymbol constructor = null;
		try{
			constructor = lookupConstructor( parameters );
		} catch ( ParserSymbolTableException e ){
		    /* nothing */
		} finally {
			getSymbolTable().getTypeInfoProvider().returnTypeInfo( param );
		}
		
		if( constructor == null ){
			constructor = getSymbolTable().newParameterizedSymbol( getName(), ITypeInfo.t_constructor );
			constructor.addParameter( this, ITypeInfo.isConst, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference, false, false ), false );

			addConstructor( constructor );	
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#lookupConstructor(java.util.List)
	 */
	public IParameterizedSymbol lookupConstructor( final List parameters ) throws ParserSymbolTableException
	{
		LookupData data = new LookupData( ParserSymbolTable.EMPTY_NAME_ARRAY ){
			public List getParameters() { return params; }
			public TypeFilter getFilter() { return CONSTRUCTOR_FILTER; }
			final private List params = parameters;
		};
		
		List constructors = null;
		if( !getConstructors().isEmpty() ){
			constructors = new ArrayList( getConstructors() );
		}
		if( constructors != null )	
			return getSymbolTable().resolveFunction( data, constructors );
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#getConstructors()
	 */
	public List getConstructors(){
		return _constructors;
	}

	private void addToConstructors( IParameterizedSymbol constructor ){
		if( _constructors == Collections.EMPTY_LIST )
			_constructors = new ArrayList(4);
		_constructors.add( constructor );
	}
	/**
	 * 
	 * @param obj
	 * @throws ParserSymbolTableException
	 * 9.3.2-1 In the body of a nonstatic member function... the type of this of
	 * a class X is X*.  If the member function is declared const, the type of
	 * this is const X*, if the member function is declared volatile, the type
	 * of this is volatile X*....
	 */
	private boolean addThis( IParameterizedSymbol obj ){
		if( getSymbolTable().getLanguage() != ParserLanguage.CPP ){
			return false; 
		}
			
		if( obj instanceof ITemplateSymbol ){
		    ISymbol templated = ((ITemplateSymbol)obj).getTemplatedSymbol(); 
		    if( templated instanceof IParameterizedSymbol ){
		        obj = (IParameterizedSymbol) templated;
		    } else {
		        return false;
		    }
		}
		
		IContainerSymbol containing = obj.getContainingSymbol();
		if( containing instanceof ITemplateSymbol ){
		    containing = containing.getContainingSymbol();
		}
		
		ITypeInfo type = obj.getTypeInfo();
		if( ( !type.isType( ITypeInfo.t_function ) && !type.isType( ITypeInfo.t_constructor) ) ||
			type.checkBit( ITypeInfo.isStatic ) ){
			return false;
		}

		if( containing.isType( ITypeInfo.t_class, ITypeInfo.t_union ) ){
			//check to see if there is already a this object, since using declarations
			//of function will have them from the original declaration
			boolean foundThis = false;
			
			LookupData data = new LookupData( ParserSymbolTable.THIS );
			try {
			    CharArrayObjectMap map = ParserSymbolTable.lookupInContained( data, obj );
				foundThis = ( map != null ) ? map.containsKey( data.name ) : false;
			} catch (ParserSymbolTableException e) {
				return false;
			}
			
			//if we didn't find "this" then foundItems will still be null, no need to actually
			//check its contents 
			if( !foundThis ){
				ISymbol thisObj = getSymbolTable().newSymbol( ParserSymbolTable.THIS, ITypeInfo.t_type );
				thisObj.setTypeSymbol( obj.getContainingSymbol() );
				ITypeInfo.PtrOp ptr = new ITypeInfo.PtrOp();
				ptr.setType( ITypeInfo.PtrOp.t_pointer );
				thisObj.getTypeInfo().setBit( obj.getTypeInfo().checkBit( ITypeInfo.isConst ), ITypeInfo.isConst );
				thisObj.getTypeInfo().setBit( obj.getTypeInfo().checkBit( ITypeInfo.isVolatile ), ITypeInfo.isVolatile );
				
				thisObj.addPtrOperator(ptr);
				
				try{
					obj.addSymbol( thisObj );
				} catch ( ParserSymbolTableException e ) {
					//shouldn't happen because we checked that "this" didn't exist already
					return false;
				}
				
			}
		}	
		return true;	
	}
	
	
	/**
	 * 
	 * @param name
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 * 7.3.1.2-3 If a friend declaration in a non-local class first declares a
	 * class or function, the friend class or function is a member of the
	 * innermost enclosing namespace.
	 */
	public void addFriend( ISymbol friend ){
		//is this symbol already in the table?
		IContainerSymbol containing = friend.getContainingSymbol();
		if( containing == null ){
			//its not, it goes in the innermost enclosing namespace
			IContainerSymbol enclosing = getContainingSymbol();
			
			boolean local = enclosing.isType( ITypeInfo.t_constructor ) ||
							enclosing.isType( ITypeInfo.t_function )    ||
							enclosing.isType( ITypeInfo.t_block );
			
			while( enclosing != null && !enclosing.isType( ITypeInfo.t_namespace ) ){
				enclosing = enclosing.getContainingSymbol();
			}
			
			friend.setIsInvisible( local );
			friend.setIsForwardDeclaration( true );
			try {
				enclosing.addSymbol( friend );
			} catch (ParserSymbolTableException e) {
			    /* nothing */
			}
		}
		
		addToFriends( friend );
	}
	
	/**
	 * LookupForFriendship
	 * @param name
	 * @return Declaration
	 * 7.3.1.2-3 When looking for a prior declaration of a class or a function
	 * declared as a friend, scopes outside the innermost enclosing namespace
	 * scope are not considered.
	 * 11.4-9 If a friend declaration appears in a local class and the name
	 * specified is an unqualified name, a prior declaration is looked up
	 * without considering scopes that are outside the innermost enclosing non-
	 * class scope.
	 */
	public ISymbol lookupForFriendship( char[] name ) throws ParserSymbolTableException{
		IContainerSymbol enclosing = getContainingSymbol();
		if( enclosing != null && enclosing.isType( ITypeInfo.t_namespace, ITypeInfo.t_union ) ){
			while( enclosing != null && ( enclosing.getType() != ITypeInfo.t_namespace) )
			{                                        		
				enclosing = enclosing.getContainingSymbol();
			}
		}
		
		final ISymbol finalEnc = enclosing;
		LookupData data = new LookupData( name ){
			public ISymbol getStopAt() { return stopAt; }
			final private ISymbol stopAt = finalEnc;
		};

		ParserSymbolTable.lookup( data, this );
		return getSymbolTable().resolveAmbiguities( data ); 
	}
	
	public IParameterizedSymbol lookupFunctionForFriendship( char[] name, final List parameters ) throws ParserSymbolTableException{

		
		IContainerSymbol enclosing = getContainingSymbol();
		if( enclosing != null && enclosing.isType( ITypeInfo.t_namespace, ITypeInfo.t_union ) ){
			while( enclosing != null && ( enclosing.getType() != ITypeInfo.t_namespace) )
			{                                        		
				enclosing = enclosing.getContainingSymbol();
			}
		}
		final ISymbol finalEnc = enclosing;
		LookupData data = new LookupData( name ){
			public List getParameters() { return params; }
			public ISymbol getStopAt()  { return stopAt; }
			final private List params = parameters;
			final ISymbol stopAt = finalEnc;
		};
		
		ParserSymbolTable.lookup( data, this );
		return (IParameterizedSymbol) getSymbolTable().resolveAmbiguities( data );
	}
	
	
	public List getFriends(){ 
		return _friends;
	}
	private void addToFriends( ISymbol friend ){
		if( _friends == Collections.EMPTY_LIST ){
			_friends = new ArrayList(4);
		}
		_friends.add( friend );
	}
	
//	static private class AddParentCommand extends Command{
//		public AddParentCommand( IDerivableContainerSymbol container, ParentWrapper wrapper ){
//			_decl = container;
//			_wrapper = wrapper;
//		}
//		
//		public void undoIt(){
//			List parents = _decl.getParents();
//			parents.remove( _wrapper );
//		}
//		
//		private IDerivableContainerSymbol _decl;
//		private ParentWrapper _wrapper;
//	}
//	
//	static private class AddConstructorCommand extends Command{
//		AddConstructorCommand( IParameterizedSymbol newConstr, IDerivableContainerSymbol context ){
//			_constructor = newConstr;
//			_context = context;
//		}
//		public void undoIt(){
//			List constructors = _context.getConstructors();
//			Iterator iter = constructors.listIterator();
//			
//			int size = constructors.size();
//			IParameterizedSymbol item = null;
//			for( int i = 0; i < size; i++ ){
//				item = (IParameterizedSymbol)iter.next();
//				if( item == _constructor ){
//					iter.remove();
//					break;
//				}
//			}
//			
//			ContentsIterator contents = (ContentsIterator) _context.getContentsIterator();
//			while( iter.hasNext() ){
//				IExtensibleSymbol ext = (IExtensibleSymbol) iter.next();
//				if( ext == _constructor ){
//					contents.removeSymbol();
//					break;
//				}
//			}
//		}
//	
//		private final IParameterizedSymbol _constructor;
//		private final IDerivableContainerSymbol _context; 
//	}
	
	public class ParentWrapper implements IDerivableContainerSymbol.IParentSymbol
	{
		public ParentWrapper( ISymbol p, boolean v, ASTAccessVisibility s, int offset, List r ){
			parent    = p;
			isVirtual = v;
			access = s;
			this.offset = offset;
			this.references = r;
		}
	
		public Object clone(){
			try{
				return super.clone();
			} catch ( CloneNotSupportedException e ){
				//should not happen
				return null;
			}
		}
		
		public void setParent( ISymbol parent ){	this.parent = parent;	}
		
		public ISymbol getParent() {	return parent;		}
		public boolean isVirtual() {	return isVirtual;	}
		
		public void setVirtual( boolean virtual	){	isVirtual = virtual;	}
		
		public ASTAccessVisibility getAccess() 	  {	return access;	}
		
		public int getOffset()		{	return offset;		}
		public List getReferences()	{	return references;	}
		
		private boolean isVirtual = false;
		protected ISymbol parent = null;
		private final ASTAccessVisibility access;
		private final int offset; 
		private final List references; 
	}
	
	private List _constructors = Collections.EMPTY_LIST;	//constructor list
	private	List _parentScopes = Collections.EMPTY_LIST;	//inherited scopes (is base classes)
	private	List _friends      = Collections.EMPTY_LIST;
}
