/*
 * Created on Dec 8, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.core.parser;

import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;

/**
 * @author jcamelon
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CompletionNode implements IASTCompletionNode {

	private final String prefix;
	private final IASTNode context;
	private final IASTScope scope;
	private final CompletionKind kind;

	public CompletionNode( CompletionKind kind, IASTScope scope, IASTNode context, String prefix )
	{
		this.kind = kind;
		this.context = context;
		this.scope = scope; 
		this.prefix = prefix;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTCompletionNode#getCompletionKind()
	 */
	public CompletionKind getCompletionKind() {
		return kind;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTCompletionNode#getCompletionScope()
	 */
	public IASTScope getCompletionScope() {
		return scope;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTCompletionNode#getCompletionContext()
	 */
	public IASTNode getCompletionContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTCompletionNode#getCompletionPrefix()
	 */
	public String getCompletionPrefix() {
		return prefix;
	}

}
