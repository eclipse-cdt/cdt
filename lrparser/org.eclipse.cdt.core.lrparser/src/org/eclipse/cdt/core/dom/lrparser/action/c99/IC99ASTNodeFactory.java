/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action.c99;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.lrparser.action.IASTNodeFactory;

/**
 * Factory for AST nodes that are just used by C and not by C++.
 * 
 * @author Mike Kucera
 */
public interface IC99ASTNodeFactory extends IASTNodeFactory {


	public IASTFieldReference newFieldReference(IASTName name, IASTExpression owner, boolean isPointerDereference);

	public ICASTTypeIdInitializerExpression newCTypeIdInitializerExpression(IASTTypeId typeId, IASTInitializerList list);

	public ICASTArrayModifier newModifiedArrayModifier();

	public ICASTKnRFunctionDeclarator newCKnRFunctionDeclarator();

	public ICASTPointer newCPointer();

	public ICASTDesignatedInitializer newCDesignatedInitializer(IASTInitializer rhs);

	public ICASTArrayDesignator newCArrayDesignator(IASTExpression exp);

	public ICASTFieldDesignator newCFieldDesignator(IASTName name);

	public ICASTSimpleDeclSpecifier newCSimpleDeclSpecifier();

	public ICASTTypedefNameSpecifier newCTypedefNameSpecifier();

	public IASTFieldDeclarator newFieldDeclarator(IASTName name, IASTExpression bitFieldSize);
	
	public ICASTCompositeTypeSpecifier newCCompositeTypeSpecifier(int key, IASTName name);

	
}
