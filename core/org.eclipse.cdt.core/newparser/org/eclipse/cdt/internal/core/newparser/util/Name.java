package org.eclipse.cdt.internal.core.newparser.util;

import org.eclipse.cdt.internal.core.newparser.Token;


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
		String name = nameStart.getImage();
		
		for (Token t = nameStart; nameStart != nameEnd;) {
			t = nameStart.getNext();
			name += t.getImage();
		}
		
		return name;
	}
}
