/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;

public interface IASTAmbiguousDeclaration extends IASTDeclaration {

    public static final ASTNodeProperty SUBDECLARATION = new ASTNodeProperty( "IASTAmbiguousDeclaration.SUBDECLARATION"); //$NON-NLS-1$
    public void addDeclaration( IASTDeclaration d );
    public IASTDeclaration [] getDeclarations();

}
