package org.eclipse.cdt.internal.core.newparser;

import java.util.Stack;
/**
 * @author aniefer
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ParserSymbolTable {

	/**
	 * Constructor for ParserSymbolTable.
	 */
	public ParserSymbolTable() {
		super();
		_compilationUnit = new Declaration();
		push( _compilationUnit );
	}

	public void push( Declaration obj ){
		if( _contextStack.empty() == false )
			obj.setContainingScope( (Declaration) _contextStack.peek() );
		_contextStack.push( obj );
	}
	
	public Declaration pop(){
		return (Declaration) _contextStack.pop();
	}
	
	public Declaration peek(){
		return (Declaration) _contextStack.peek();
	}
	
	public Declaration Lookup( String name ) throws ParserSymbolTableException {
		return ( (Declaration) _contextStack.peek() ).Lookup( -1, name );
	}
	
	public Declaration ElaboratedLookup( int type, String name ) throws ParserSymbolTableException{
		return ( (Declaration) _contextStack.peek() ).Lookup( type, name );
	}
	
	public void addDeclaration( Declaration obj ){
		((Declaration) _contextStack.peek() ).addDeclaration( obj );
	}
	
	public Declaration getCompilationUnit(){
		return _compilationUnit;
	}
	
	private Stack _contextStack = new Stack();
	private Declaration _compilationUnit;
}
