/*
 * Created on Apr 14, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.internal.core.parser.Parser.Backtrack;

/**
 * @author jcamelon
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IParser {
	public boolean parse() throws Backtrack;
	public void expression(Object expression) throws Backtrack;
	/**
	 * @return
	 */
	public boolean isCppNature();
	/**
	 * @param b
	 */
	public void setCppNature(boolean b);
	public void mapLineNumbers( boolean value );
	public int getLineNumberForOffset(int offset) throws NoSuchMethodException;
	public int getLastErrorOffset(); 
	
}