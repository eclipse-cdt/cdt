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

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable.Command;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable.LookupData;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DerivableContainerSymbol extends ContainerSymbol implements IDerivableContainerSymbol {

	protected DerivableContainerSymbol( ParserSymbolTable table, String name ){
		super( table, name );
	}
	
	protected DerivableContainerSymbol( ParserSymbolTable table, String name, ISymbolASTExtension obj ){
		super( table, name, obj );
	}
	
	protected DerivableContainerSymbol( ParserSymbolTable table, String name, TypeInfo.eType typeInfo ){
		super( table, name, typeInfo );
	}
	
	
	public Object clone(){
		DerivableContainerSymbol copy = (DerivableContainerSymbol)super.clone();
			
		copy._parentScopes = ( _parentScopes != null ) ? (LinkedList) _parentScopes.clone() : null;
		copy._constructors = ( _constructors != null ) ? (LinkedList) _constructors.clone() : null;		
			
		return copy;	
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
		if( _parentScopes == null ){
			_parentScopes = new LinkedList();
		}
		
		ParentWrapper wrapper = new ParentWrapper( parent, virtual, visibility, offset, references );
		_parentScopes.add( wrapper );
		
		Command command = new AddParentCommand( this, wrapper );
		getSymbolTable().pushCommand( command );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#getParents()
	 */
	public List getParents(){
		if( _parentScopes == null ){
			_parentScopes = new LinkedList();
		}
		return _parentScopes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#hasParents()
	 */
	public boolean hasParents(){
		return ( _parentScopes != null && !_parentScopes.isEmpty() );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#addConstructor(org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol)
	 */
	public void addConstructor(IParameterizedSymbol constructor) throws ParserSymbolTableException {
		if( !constructor.isType( TypeInfo.t_constructor ) )
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTypeInfo );
			
		List constructors = getConstructors();

		if( constructors.size() == 0 || ParserSymbolTable.isValidOverload( constructors, constructor ) ){
			constructors.add( constructor );
		} else {
			throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidOverload );
		}
		
		constructor.setContainingSymbol( this );
		addThis( constructor );

		Command command = new AddConstructorCommand( constructor, this );
		getSymbolTable().pushCommand( command );			
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#addCopyConstructor()
	 */
	public void addCopyConstructor() throws ParserSymbolTableException{
		List parameters = new LinkedList();
		
		TypeInfo param = new TypeInfo( TypeInfo.t_type, 0, this, new TypeInfo.PtrOp( TypeInfo.PtrOp.t_reference, true, false ), false ); 
		parameters.add( param );
		
		IParameterizedSymbol constructor = lookupConstructor( parameters );
		
		if( constructor == null ){
			constructor = getSymbolTable().newParameterizedSymbol( getName(), TypeInfo.t_constructor );
			constructor.addParameter( this, new TypeInfo.PtrOp( TypeInfo.PtrOp.t_reference, true, false ), false );
			addConstructor( constructor );	
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#lookupConstructor(java.util.List)
	 */
	public IParameterizedSymbol lookupConstructor( List parameters ) throws ParserSymbolTableException
	{
		LookupData data = new LookupData( ParserSymbolTable.EMPTY_NAME, TypeInfo.t_constructor, null );
		data.parameters = parameters;
		
		List constructors = new LinkedList();
		if( !getConstructors().isEmpty() )
			constructors.addAll( getConstructors() );
			
		return ParserSymbolTable.resolveFunction( data, constructors );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#getConstructors()
	 */
	public List getConstructors(){
		if( _constructors == null ){
			_constructors = new LinkedList();
		}
		return _constructors;
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
			
		TypeInfo type = obj.getTypeInfo();
		if( ( !type.isType( TypeInfo.t_function ) && !type.isType( TypeInfo.t_constructor) ) ||
			type.checkBit( TypeInfo.isStatic ) ){
			return false;
		}

		if( obj.getContainingSymbol().isType( TypeInfo.t_class, TypeInfo.t_union ) ){
			//check to see if there is already a this object, since using declarations
			//of function will have them from the original declaration
			boolean foundThis = false;
			
			LookupData data = new LookupData( ParserSymbolTable.THIS, TypeInfo.t_any, null );
			try {
				Map map = ParserSymbolTable.lookupInContained( data, obj );
				foundThis = map.containsKey( data.name );
			} catch (ParserSymbolTableException e) {
				return false;
			}
			
			//if we didn't find "this" then foundItems will still be null, no need to actually
			//check its contents 
			if( !foundThis ){
				ISymbol thisObj = getSymbolTable().newSymbol( ParserSymbolTable.THIS, TypeInfo.t_type );
				thisObj.setTypeSymbol( obj.getContainingSymbol() );
				//thisObj.setCVQualifier( obj.getCVQualifier() );
				TypeInfo.PtrOp ptr = new TypeInfo.PtrOp();
				ptr.setType( TypeInfo.PtrOp.t_pointer );
				if( obj.getTypeInfo().hasPtrOperators() ){
					ptr.setConst( ((TypeInfo.PtrOp) obj.getPtrOperators().iterator().next()).isConst() );
					ptr.setVolatile( ((TypeInfo.PtrOp) obj.getPtrOperators().iterator().next()).isVolatile() );
				}
				
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
	 * 
	 * TODO: if/when the parser symbol table starts caring about visibility
	 * (public/protected/private) we will need to do more to record friendship.
	 */
	private ISymbol addFriend( String name ) throws ParserSymbolTableException{
		ISymbol friend = lookupForFriendship( name  );
	
		if( friend == null ){
			friend = getSymbolTable().newSymbol( name );
			friend.getTypeInfo().setIsForwardDeclaration( true );
		
			IContainerSymbol containing = getContainingSymbol();
			//find innermost enclosing namespace
			while( containing != null && containing.getType() != TypeInfo.t_namespace ){
				containing = containing.getContainingSymbol();
			}
		
			IContainerSymbol namespace = ( containing == null ) ?  getSymbolTable().getCompilationUnit() : containing;
			namespace.addSymbol( friend );
		}
		
		return friend;
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
	private ISymbol lookupForFriendship( String name ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, TypeInfo.t_any, getTemplateInstance() );
	
		boolean inClass = ( getType() == TypeInfo.t_class);
	
		IContainerSymbol enclosing = getContainingSymbol();
		while( enclosing != null && (inClass ? enclosing.getType() != TypeInfo.t_class
											  :	enclosing.getType() == TypeInfo.t_namespace) )
		{                                        		
			enclosing = enclosing.getContainingSymbol();
		}

		data.stopAt = enclosing;
	
		ParserSymbolTable.lookup( data, this );
		return ParserSymbolTable.resolveAmbiguities( data ); 
	}
	
	
	static private class AddParentCommand extends Command{
		public AddParentCommand( IDerivableContainerSymbol container, ParentWrapper wrapper ){
			_decl = container;
			_wrapper = wrapper;
		}
		
		public void undoIt(){
			List parents = _decl.getParents();
			parents.remove( _wrapper );
		}
		
		private IDerivableContainerSymbol _decl;
		private ParentWrapper _wrapper;
	}
	
	static private class AddConstructorCommand extends Command{
		AddConstructorCommand( IParameterizedSymbol newConstr, IDerivableContainerSymbol context ){
			_constructor = newConstr;
			_context = context;
		}
		public void undoIt(){
			List constructors = _context.getConstructors();
			ListIterator iter = constructors.listIterator();
			
			int size = constructors.size();
			IParameterizedSymbol item = null;
			for( int i = 0; i < size; i++ ){
				item = (IParameterizedSymbol)iter.next();
				if( item == _constructor ){
					iter.remove();
					break;
				}
			}
		}
	
		private IParameterizedSymbol _constructor;
		private IDerivableContainerSymbol _context; 
	}
	
	public class ParentWrapper implements IDerivableContainerSymbol.IParentSymbol
	{
		public ParentWrapper( ISymbol p, boolean v, ASTAccessVisibility s, int offset, List r ){
			parent    = p;
			isVirtual = v;
			access = s;
			this.offset = offset;
			this.references = r;
		}
	
		public void setParent( ISymbol parent ){	this.parent = parent;	}
		
		public ISymbol getParent() {	return parent;		}
		public boolean isVirtual() {	return isVirtual;	}
		
		public void setVirtual( boolean virtual	){	isVirtual = virtual;	}
		
		public ASTAccessVisibility getVisibility(){	return access;	}
		public ASTAccessVisibility getAccess() 	  {	return access;	}
		
		public int getOffset()		{	return offset;		}
		public List getReferences()	{	return references;	}
		
		private boolean isVirtual = false;
		protected ISymbol parent = null;
		private final ASTAccessVisibility access;
		private final int offset; 
		private final List references; 
	}
	
	private 	LinkedList 	_constructors;			//constructor list
	private		LinkedList	_parentScopes;			//inherited scopes (is base classes)
}
