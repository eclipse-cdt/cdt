package org.eclipse.cdt.internal.core.parser;

/**
 * @author aniefer
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ParserSymbolTableException extends Exception {

	/**
	 * Constructor for ParserSymbolTableException.
	 */
	public ParserSymbolTableException() {
		super();
	}

	/**
	 * Constructor for ParserSymbolTableException.
	 * @param int r: reason
	 */
	public ParserSymbolTableException( int r ) {
		reason = r;
	}

	public static final int r_Unspecified   = -1;
	public static final int r_AmbiguousName =  0;
	public static final int r_BadTypeInfo   =  1;
	
	public int reason = -1;
}