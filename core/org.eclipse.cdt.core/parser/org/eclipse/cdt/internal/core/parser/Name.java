package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.parser.IToken;



/**
 * @author dschaefe
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Name {

	private IToken nameStart, nameEnd;
	
	public Name(IToken nameStart) {
		this.nameStart = nameStart;
	}

	public Name(IToken nameStart, IToken nameEnd) {
		this( nameStart ); 
		setEnd( nameEnd );
	}

	
	public void setEnd(IToken nameEnd) {
		this.nameEnd = nameEnd;
	}
	
	public int getStartOffset()
	{
		return nameStart.getOffset();
	}
	
	public int getEndOffset()
	{
		return nameEnd.getOffset();
	}

	public String toString() {
		IToken t = nameStart;
		StringBuffer buffer = new StringBuffer(); 
		buffer.append( t.getImage() ); 
		if( t.getType() == IToken.t_operator )
			buffer.append( " " );

		while (t != nameEnd) {
			t = t.getNext();
				
			buffer.append( t.getImage() );	
			if (t.getType() == IToken.t_operator) buffer.append( " " );					
		}
		
		return buffer.toString();
	}
	
	public int length()
	{
		return getEndOffset() - getStartOffset() + nameEnd.getLength();
	}
	/**
	 * @return
	 */
	public IToken getNameStart() {
		return nameStart;
	}
	
	public static String tokensToString( Token first, Token last )
	{
		Name n = new Name( first, last ); 
		return n.toString(); 
	}

}
