/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Interface to model using directives. Needed to bridge between directives found in the 
 * AST and the ones found in the index.
 * @since 5.0
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPUsingDirective {

	ICPPUsingDirective[] EMPTY_ARRAY = new ICPPUsingDirective[0];

	/**
	 * Returns the scope of the namespace that is nominated by this
	 * directive, or <code>null</code> if it cannot be determined.
	 */
	ICPPNamespaceScope getNominatedScope() throws DOMException;

	/**
	 * Returns the point of declaration as global offset ({@link ASTNode#getOffset()}).
	 */
	int getPointOfDeclaration();

	/**
	 * Returns the scope containing this directive.
	 */
	IScope getContainingScope();
}
