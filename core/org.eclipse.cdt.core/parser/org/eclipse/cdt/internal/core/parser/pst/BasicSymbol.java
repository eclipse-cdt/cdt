/*
 * Created on Nov 4, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.Iterator;
import java.util.List;
import java.util.Map;



public class BasicSymbol implements Cloneable, ISymbol
{
	private final ParserSymbolTable _table;
	public BasicSymbol( ParserSymbolTable table, String name ){
		super();
		this._table = table;
		_name = name;
		_typeInfo = new TypeInfo();
	}
	
	public BasicSymbol( ParserSymbolTable table, String name, ISymbolASTExtension obj ){
		super();
		this._table = table;
		_name   = name;
		_object = obj;
		_typeInfo = new TypeInfo();
	}
	
	public BasicSymbol( ParserSymbolTable table, String name, TypeInfo.eType typeInfo )
	{
		super();
		this._table = table;
		_name = name;
		_typeInfo = new TypeInfo( typeInfo, 0, null );
	}
	
	public ParserSymbolTable getSymbolTable(){
		return _table;
	}
	
	public Object clone(){
		BasicSymbol copy = null;
		try{
			copy = (BasicSymbol)super.clone();
		} catch ( CloneNotSupportedException e ){
			//should not happen
			return null;
		}
		copy._object = null;
		return copy;	
	}
	
	public String getName() { return _name; }
	public void setName(String name) { _name = name; }

	public ISymbolASTExtension getASTExtension() { return _object; }
	public void setASTExtension( ISymbolASTExtension obj ) { _object = obj; }
		
	public IContainerSymbol getContainingSymbol() { return _containingScope; }
	public void setContainingSymbol( IContainerSymbol scope ){ 
		_containingScope = scope;
		_depth = scope.getDepth() + 1; 
	}

	public void setType(TypeInfo.eType t){
		getTypeInfo().setType( t );	 
	}

	public TypeInfo.eType getType(){ 
		return getTypeInfo().getType(); 
	}

	public boolean isType( TypeInfo.eType type ){
		return getTypeInfo().isType( type, TypeInfo.t_undef ); 
	}

	public boolean isType( TypeInfo.eType type, TypeInfo.eType upperType ){
		return getTypeInfo().isType( type, upperType );
	}
	
	public ISymbol getTypeSymbol(){
		ISymbol symbol = getTypeInfo().getTypeSymbol();
		
		if( symbol != null && symbol.getTypeInfo().isForwardDeclaration() && symbol.getTypeSymbol() != null ){
			return symbol.getTypeSymbol();
		}
		
		return symbol;
	}

	public void setTypeSymbol( ISymbol type ){
		getTypeInfo().setTypeSymbol( type ); 
	}

	public TypeInfo getTypeInfo(){
		return _typeInfo; 
	}
	
	public void setTypeInfo( TypeInfo info ) {
		_typeInfo = info;
	}
	
	public boolean isForwardDeclaration(){
		return getTypeInfo().isForwardDeclaration();
	}
	
	public void setIsForwardDeclaration( boolean forward ){
		getTypeInfo().setIsForwardDeclaration( forward );
	}
	
	/**
	 * returns 0 if same, non zero otherwise
	 */
	public int compareCVQualifiersTo( ISymbol symbol ){
		int size = symbol.getTypeInfo().hasPtrOperators() ? symbol.getTypeInfo().getPtrOperators().size() : 0;
		int size2 = getTypeInfo().hasPtrOperators() ? getTypeInfo().getPtrOperators().size() : 0;
			
		if( size != size2 ){
			return size2 - size;
		} else if( size == 0 ) 
			return 0; 
		else {
			
			Iterator iter1 = symbol.getTypeInfo().getPtrOperators().iterator();
			Iterator iter2 = getTypeInfo().getPtrOperators().iterator();

			TypeInfo.PtrOp op1 = null, op2 = null;

			for( int i = size; i > 0; i-- ){
				op1 = (TypeInfo.PtrOp)iter1.next();
				op2 = (TypeInfo.PtrOp)iter2.next();
	
				if( op1.compareCVTo( op2 ) != 0 ){
					return -1;
				}
			}
		}
		
		return 0;
	}
	
	public List getPtrOperators(){
		return getTypeInfo().getPtrOperators();
	}
	public void addPtrOperator( TypeInfo.PtrOp ptrOp ){
		getTypeInfo().addPtrOperator( ptrOp );
	}	
	
	public int getDepth(){
		return _depth;
	}
	
	public boolean isTemplateMember(){
		return _isTemplateMember;
	}
	public void setIsTemplateMember( boolean isMember ){
		_isTemplateMember = isMember;
	}
	public ISymbol getTemplateInstance(){
		return _templateInstance;
	}
	public void setTemplateInstance( TemplateInstance instance ){
		_templateInstance = instance;
	}
	public Map getArgumentMap(){
		return null;
	}
	
	public boolean getIsInvisible(){
		return _isInvisible;
	}
	public void setIsInvisible( boolean invisible ){
		_isInvisible = invisible ;
	}
	
	private 	String 				_name;					//our name
	private		ISymbolASTExtension	_object;				//the object associated with us
	private		TypeInfo			_typeInfo;				//our type info
	private		IContainerSymbol	_containingScope;		//the scope that contains us
	private		int 				_depth;					//how far down the scope stack we are
	
	private 	boolean				_isInvisible = false;	//used by friend declarations (11.4-9)	
	private		boolean				_isTemplateMember = false;		
	private		TemplateInstance	_templateInstance;		
}
