package org.eclipse.cdt.internal.core.parser.util;

import org.eclipse.cdt.internal.core.parser.Token;


/**
 * @author dschaefe
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Name {

	private Token nameStart, nameEnd;
	
	public Name(Token nameStart) {
		this.nameStart = nameStart;
	}
	
	public void setEnd(Token nameEnd) {
		this.nameEnd = nameEnd;
	}
	
	public int getStartOffset()
	{
		return nameStart.offset;
	}
	
	public int getEndOffset()
	{
		return nameEnd.offset;
	}

	public String toString() {
		Token t = nameStart;
		String name = t.getImage();
		
		while (t != nameEnd) {
			t = t.getNext();
			name += t.getImage();
		}
		
		return name;
	}
}
