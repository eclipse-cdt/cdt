package org.eclipse.cdt.internal.core.newparser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author aniefer
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Declaration {

	/**
	 * Constructor for Declaration.
	 */
	public Declaration() {
		super();
	}

	public Declaration( String name ){
		_name = name;
	}

	public String getName() {
		return _name;
	}
	
	public void setName(String name) {
		_name = name;
	}
	
	public void addParent( Declaration parent ){
		addParent( parent, false );
	}
	public void addParent( Declaration parent, boolean virtual ){
		_parentScopes.add( new ParentWrapper( parent, virtual ) );
	}
	
	public Declaration getContainingScope(){
		return _containingScope;
	}
	
	protected void setContainingScope( Declaration scope ){
		_containingScope = scope;
	}
	
	public Map getContainedDeclarations(){
		return _containedDeclarations;
	}
	
	/**
	 * Lookup the given name in this context.
	 * @param name: Name of the object to lookup
	 * @return Declaration 
	 * @throws ParserSymbolTableException
	 * @see ParserSymbolTable#Lookup
	 */
	protected Declaration Lookup( String name ) throws ParserSymbolTableException{
		Declaration decl = null;
		
		//if this name define in this scope?
		decl = (Declaration) _containedDeclarations.get( name );
		
		//if yes, it hides any others, we are done.
		if( decl != null ) return decl;

		//if no, we next check any parents we have	
		decl = LookupInParents( name, new HashSet() );	
					
		//if still not found, check our containing scope.			
		if( decl == null && _containingScope != null ) 
			decl = _containingScope.Lookup( name );

		return decl;
	}
	
	private Declaration LookupInParents( String name, Set virtualsVisited ) throws ParserSymbolTableException{
		
		Declaration decl = null, temp = null;
				
		Iterator iterator = _parentScopes.iterator();
		
		ParentWrapper wrapper = null;
		try{
			wrapper = (ParentWrapper) iterator.next();
		}
		catch ( NoSuchElementException e ){
			wrapper = null;
		}
		
		while( wrapper != null )
		{
			if( !wrapper.isVirtual || !virtualsVisited.contains( wrapper.parent ) ){
				if( wrapper.isVirtual )
					virtualsVisited.add( wrapper.parent );
					
				//is this name define in this scope?
				temp = (Declaration) wrapper.parent._containedDeclarations.get( name );
				if( temp == null )
					temp = wrapper.parent.LookupInParents( name, virtualsVisited );
			}	
				
			if( decl == null )
				decl = temp;
			else if ( temp != null )
				throw( new ParserSymbolTableException() );
				
			try{
				wrapper = (ParentWrapper) iterator.next();
			}
			catch (NoSuchElementException e){
				wrapper = null;	
			}
		}
		
		return decl;	
	}
	
	protected void addDeclaration( Declaration obj ){
		_containedDeclarations.put( obj.getName(), obj );
	}	
	
	
	//Other scopes to check if the name is not in currRegion
	//we might want another Vector to deal with namespaces & using...
	private Declaration _containingScope  = null;
	private Declaration _type             = null;
	
	private List   _parentScopes          = new LinkedList();
	private Map    _containedDeclarations = new HashMap();
	private String _name;
	
	private class ParentWrapper{
		public ParentWrapper( Declaration p, boolean v ){
			parent    = p;
			isVirtual = v;
		}
		
		public boolean isVirtual = false;
		public Declaration parent = null;
	}
	
}
