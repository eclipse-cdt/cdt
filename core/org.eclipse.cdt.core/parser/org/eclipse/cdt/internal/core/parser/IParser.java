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
	public abstract boolean parse() throws Backtrack;
	public abstract void expression(Object expression) throws Backtrack;
	/**
	 * @return
	 */
	public abstract boolean isCppNature();
	/**
	 * @param b
	 */
	public abstract void setCppNature(boolean b);
	public abstract int getLineNumberForOffset(int offset);
}