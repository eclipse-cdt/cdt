/*
 * Created on Feb 18, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.List;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface IUsingDeclarationSymbol extends IExtensibleSymbol {
	
	List getReferencedSymbols();
	
	List getDeclaredSymbols();
	
}
