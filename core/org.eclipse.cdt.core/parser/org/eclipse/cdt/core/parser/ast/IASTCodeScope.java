/*
 * Created on Sep 2, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.cdt.core.parser.ast;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;

/**
 * @author jcamelon
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface IASTCodeScope extends IASTScope, ISourceElementCallbackDelegate{

	public IASTCodeScope getOwnerCodeScope(); 

}
