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
	
	public Declaration( String name, Object obj ){
		_name   = name;
		_object = obj;
	}

	//Type information, only what we need for now...
	public static final int typeMask   = 0x0001f;
	public static final int isStatic   = 0x00020;

	// Types
	public static final int t_type        = 0; // Type Specifier
	public static final int t_class       = 1;
	public static final int t_struct      = 2;
	public static final int t_union       = 3;
	public static final int t_enum        = 4;

	public void setStatic( boolean b ) { setBit( b, isStatic ); }
	public boolean isStatic() { return checkBit( isStatic ); }

	public void setType(int t) throws ParserSymbolTableException {
		if( t > typeMask )
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTypeInfo );
		_typeInfo = _typeInfo & ~typeMask | t; 
	}
	public int getType(){ 
		return _typeInfo & typeMask; 
	}
	public boolean isType( int t ){
		return ( t == -1 || getType() == t );	
	}
		
	public Declaration getTypeDeclaration() {	return _typeDeclaration; }
	public void setTypeDeclaration( Declaration type ){
		try { setType( t_type ); }
		catch (ParserSymbolTableException e) { /*will never happen*/ }
		
		_typeDeclaration = type; 
	}
	
	public String getName() { return _name; }
	public void setName(String name) { _name = name; }
	
	public Object getObject() { return _object; }
	public void setObject( Object obj ) { _object = obj; }
	
	public Declaration	getContainingScope() { return _containingScope; }
	protected void setContainingScope( Declaration scope ) { _containingScope = scope; }
	
	public void addParent( Declaration parent ){
		addParent( parent, false );
	}
	public void addParent( Declaration parent, boolean virtual ){
		_parentScopes.add( new ParentWrapper( parent, virtual ) );
	}

	protected void addDeclaration( Declaration obj ){
		obj.setContainingScope( this );
		_containedDeclarations.put( obj.getName(), obj );
	}
	
	public Map getContainedDeclarations(){
		return _containedDeclarations;
	}
	
	/**
	 * Lookup the given name in this context.
	 * @param type: for elaborated lookups, only return declarations of this
	 * 				 type
	 * @param name: Name of the object to lookup
	 * @return Declaration 
	 * @throws ParserSymbolTableException
	 * @see ParserSymbolTable#Lookup
	 */
	protected Declaration Lookup( int type, String name ) throws ParserSymbolTableException{
		
		if( type != -1 && type < t_class && type > t_union )
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTypeInfo );
			
		Declaration decl = null;
		
		//if this name define in this scope?
		decl = (Declaration) _containedDeclarations.get( name );
		
		//if yes, it hides any others, we are done.
		if( decl != null && decl.isType( type ) ){
			return decl; 
		}

		//if no, we next check any parents we have	
		decl = LookupInParents( type, name, new HashSet() );	
					
		//if still not found, check our containing scope.			
		if( decl == null && _containingScope != null ) 
			decl = _containingScope.Lookup( type, name );

		return decl;
	}

	private Declaration LookupInParents( int type, String name, Set virtualsVisited ) throws ParserSymbolTableException{
		
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
				if( temp == null || !temp.isType( type ) )
					temp = wrapper.parent.LookupInParents( type, name, virtualsVisited );
			}	
				
			if( temp != null && temp.isType( type ) ){
				if( decl == null  )
					decl = temp;
				else if ( temp != null )
				{
					//it is not ambiguous if temp & decl are the same thing and it is static
					//or an enum
					if( decl == temp && ( temp.isStatic() || temp.getType() == t_enum) )
						temp = null;
					else
						throw( new ParserSymbolTableException( ParserSymbolTableException.r_AmbiguousName ) );
		
				}
			}
			else
				temp = null;
			
			try{
				wrapper = (ParentWrapper) iterator.next();
			}
			catch (NoSuchElementException e){
				wrapper = null;	
			}		
		}
		
		return decl;	
	}
	

	// Convenience methods
	private void setBit(boolean b, int mask) {
		if (b)	_typeInfo = _typeInfo | mask;
		else	_typeInfo = _typeInfo & ~mask;
	}
	
	private boolean checkBit(int mask) {
		return (_typeInfo & mask) != 0;
	}	
	
	
	//Other scopes to check if the name is not in currRegion
	//we might want another Vector to deal with namespaces & using...
	private Declaration _containingScope  = null;
	private Declaration _type             = null;
	private Declaration _typeDeclaration  = null;
	private int   _typeInfo               = 0;
	private Object _object                = null;
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
