/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mike Kucera (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
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

	public ICASTEnumerationSpecifier newEnumerationSpecifier(IASTName name);

	public ICASTElaboratedTypeSpecifier newElaboratedTypeSpecifier(int kind, IASTName name);
	
	public ICASTSimpleDeclSpecifier newSimpleDeclSpecifier();
	
	public ICASTPointer newPointer();
	
	public ICASTTypedefNameSpecifier newTypedefNameSpecifier(IASTName name);

	public ICASTCompositeTypeSpecifier newCompositeTypeSpecifier(int key, IASTName name);

	public ICASTArrayModifier newArrayModifier(IASTExpression expr);
	
	public ICASTTypeIdInitializerExpression newTypeIdInitializerExpression(IASTTypeId typeId, IASTInitializer initializer);

	public ICASTKnRFunctionDeclarator newKnRFunctionDeclarator(IASTName[] parameterNames, IASTDeclaration[] parameterDeclarations);

	public ICASTDesignatedInitializer newDesignatedInitializer(IASTInitializer rhs);

	public ICASTArrayDesignator newArrayDesignator(IASTExpression exp);

	public ICASTFieldDesignator newFieldDesignator(IASTName name);

	public IGCCASTArrayRangeDesignator newArrayRangeDesignatorGCC(IASTExpression floor, IASTExpression ceiling);

	/**
	 * @deprecated Replaced by {@link #newSimpleDeclSpecifier()}
	 */
	@Deprecated
	public org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTSimpleDeclSpecifier newSimpleDeclSpecifierGCC(IASTExpression typeofExpression);	
}