/*******************************************************************************
 * Copyright (c) 2010 Tomasz Wesolowski
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.dom.rewrite;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.rewrite.DeclarationGeneratorImpl;

/**
 * 
 * This class handles the creation of {@link IASTDeclarator}s and {@link IASTDeclSpecifier}s basing on given type.
 * 
 * @author Tomasz Wesolowski
 * @noextend This interface is not intended to be extended by clients.
 * @since 5.3
 */
public abstract class DeclarationGenerator {

	public static DeclarationGenerator create(INodeFactory factory) {
		return new DeclarationGeneratorImpl(factory);
	}
	
	/**
	 * Creates a new {@link IASTDeclSpecifier}DeclSpecifier for a given {@link IType}.
	 * @param type the type to describe
	 * @return the generated declaration specifier
	 */
	public abstract IASTDeclSpecifier createDeclSpecFromType(IType type);

	/**
	 * Creates a new {@link IASTDeclarator}Declarator for a given  {@link IType}.
	 * @param type the type to describe
	 * @param name the name for the declarator
	 * @return the generated declarator
	 */
	public abstract IASTDeclarator createDeclaratorFromType(IType type, char[] name);

}