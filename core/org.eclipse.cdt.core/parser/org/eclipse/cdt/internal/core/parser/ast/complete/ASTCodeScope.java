/*
 * Created on Sep 2, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.cdt.internal.core.parser.ast.complete;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;

/**
 * @author jcamelon
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ASTCodeScope extends ASTScope implements IASTCodeScope {

	/**
	 * @param newScope
	 */
	public ASTCodeScope(IContainerSymbol newScope) {
		super( newScope );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
	 */
	public void acceptElement(ISourceElementRequestor requestor) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
	 */
	public void enterScope(ISourceElementRequestor requestor) {
		requestor.enterCodeBlock( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
	 */
	public void exitScope(ISourceElementRequestor requestor) {
		requestor.exitCodeBlock( this );
	}

}
