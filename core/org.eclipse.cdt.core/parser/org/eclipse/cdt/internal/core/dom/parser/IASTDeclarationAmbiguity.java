/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;

/**
 * @author jcamelon
 * @deprecated there is no class that implements this interface. Use {@link IASTAmbiguousDeclaration}, instead.
 */
@Deprecated
public interface IASTDeclarationAmbiguity extends IASTDeclaration
{

    public void addDeclaration( IASTDeclaration decl );
    
    public IASTDeclaration [] getDeclarations();
    
}
