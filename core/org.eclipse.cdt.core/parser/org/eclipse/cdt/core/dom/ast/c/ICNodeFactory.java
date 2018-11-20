/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mike Kucera (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;

/**
 * Factory for AST nodes for the C programming language.
 *
 * @since 5.1
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICNodeFactory extends INodeFactory {

	public ICASTArrayDesignator newArrayDesignator(IASTExpression exp);

	@Override
	public ICASTArrayModifier newArrayModifier(IASTExpression expr);

	public IGCCASTArrayRangeDesignator newArrayRangeDesignatorGCC(IASTExpression floor, IASTExpression ceiling);

	@Override
	public ICASTCompositeTypeSpecifier newCompositeTypeSpecifier(int key, IASTName name);

	/**
	 * @since 5.2
	 */
	public ICASTDesignatedInitializer newDesignatedInitializer(IASTInitializerClause initializer);

	@Override
	public ICASTElaboratedTypeSpecifier newElaboratedTypeSpecifier(int kind, IASTName name);

	@Override
	public ICASTEnumerationSpecifier newEnumerationSpecifier(IASTName name);

	public ICASTFieldDesignator newFieldDesignator(IASTName name);

	public ICASTKnRFunctionDeclarator newKnRFunctionDeclarator(IASTName[] parameterNames,
			IASTDeclaration[] parameterDeclarations);

	@Override
	public ICASTPointer newPointer();

	@Override
	public ICASTSimpleDeclSpecifier newSimpleDeclSpecifier();

	@Override
	public ICASTTypedefNameSpecifier newTypedefNameSpecifier(IASTName name);

	@Override
	public ICASTTypeIdInitializerExpression newTypeIdInitializerExpression(IASTTypeId typeId,
			IASTInitializer initializer);

	/**
	 * @deprecated Replaced by {@link #newDesignatedInitializer(IASTInitializerClause)}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public ICASTDesignatedInitializer newDesignatedInitializer(IASTInitializer rhs);

	/**
	 * @deprecated Replaced by {@link #newSimpleDeclSpecifier()}
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTSimpleDeclSpecifier newSimpleDeclSpecifierGCC(
			IASTExpression typeofExpression);
}