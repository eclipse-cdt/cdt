/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;

/**
 * @author jcamelon
 */
public class ASTCompletionNode implements IASTCompletionNode {

	private final String prefix;
	private final IASTNode context;
	private final IASTScope scope;
	private final CompletionKind kind;
	private final Set keywordSet;

	public ASTCompletionNode( CompletionKind kind, IASTScope scope, IASTNode context, String prefix, Set keywords )
	{
		this.kind = kind;
		this.context = context;
		this.scope = scope; 
		this.prefix = prefix;
		this.keywordSet = keywords;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTCompletionNode#getKeywords()
	 */
	public Iterator getKeywords() {
		if( keywordSet == null )
			return new EmptyIterator(); 
		return keywordSet.iterator();
	}

}
