/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM Rational Software) - Initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Methods needed by CVisitor but not meant for public interface
 */
public interface ICInternalBinding {
    public IASTNode getPhysicalNode();

    /**
     * Returns the declarations for this binding.
     * @since 5.0
     */
	public IASTNode[] getDeclarations();

    /**
     * Returns the definitions for this binding.
     * @since 5.0
     */
	public IASTNode getDefinition();
}
