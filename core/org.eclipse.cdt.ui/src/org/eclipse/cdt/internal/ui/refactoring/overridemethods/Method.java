/*******************************************************************************
 * Copyright (c) 2017 Pavel Marek
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Pavel Marek - initial API and implementation
 *      Marco Stornelli - Improvements
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVirtSpecifier.SpecifierKind;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.rewrite.DeclarationGenerator;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.utils.DefinitionFinder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Wrapper for ICPPMethod
 */
public class Method {
	private IASTDeclSpecifier fDeclSpecifier;
	private ICPPMethod fMethod;
	private OverrideOptions fOptions;

	/**
	 * Accepts only methods declared as virtual.
	 * @param method The ICPPMethod to be wrapped
	 * @param declSpecifier The class declaration specifier
	 * @param options Override options
	 */
	public Method(ICPPMethod method, IASTDeclSpecifier declSpecifier, OverrideOptions options) {
		fMethod = method;
		fOptions = options;
		fDeclSpecifier = declSpecifier;
	}

	/**
	 * Accepts only methods declared as virtual.
	 * @param method The ICPPMethod to be wrapped
	 * @param options Override options
	 */
	public Method(ICPPMethod method, OverrideOptions options) {
		fMethod = method;
		fOptions = options;
		fDeclSpecifier = null;
	}

	/**
	 * Two methods are considered equal if they have same signature ie. name
	 * and types of parameters in same order.
	 */
	@Override
	public int hashCode() {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(fMethod.getName());
		for (ICPPParameter parameter : fMethod.getParameters()) {
			stringBuilder.append(parameter.getType());

		}
		return stringBuilder.toString().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return this.hashCode() == o.hashCode();
	}

	/**
	 * Get the wrapped method
	 * @return The method
	 */
	public ICPPMethod getMethod() {
		return fMethod;
	}

	/**
	 * Accepts only methods declared as virtual.
	 * @param fMethod
	 */
	public void setMethod(ICPPMethod fMethod) {
		this.fMethod = fMethod;
	}

	@Override
	public String toString() {
		return fMethod.toString();
	}

	/**
	 * Get the class declaration specifier
	 * @return The class declaration specifier
	 */
	public IASTDeclSpecifier getDeclSpecifier() {
		return fDeclSpecifier;
	}

	/**
	 * Create a IASTNode for this method
	 * @param context The refactoring context
	 * @return The IASTNode for the method declaration
	 * @throws OperationCanceledException
	 * @throws CoreException
	 */
	public IASTNode createNode(CRefactoringContext context) throws OperationCanceledException, CoreException {
		ICPPFunctionType functionType = fMethod.getDeclaredType();
		ICPPParameter[] parameters = fMethod.getParameters();
		IASTName declaration = DefinitionFinder.getMemberDeclaration(fMethod, getDeclSpecifier().getTranslationUnit(),
				context, null);
		INodeFactory factory = getDeclSpecifier().getTranslationUnit().getASTNodeFactory();
		DeclarationGenerator declGen = DeclarationGenerator.create(factory);
		if (declaration == null)
			return null;

		IASTDeclarator declarator = (IASTDeclarator) declaration.getParent();

		/**
		 * We can't just copy the original nodes here but we need to create a new node. We can't do it
		 * because the original node could lack some information needed in the clone. Example: the node
		 * in parent has a parameter to an object inside a namespace but namespace miss because the interface
		 * class is declared in the same namespace too, but in this case the new method of child class may needs
		 * of fully qualified name for the parameter, so a plain copy doesn't work.
		 */
		IASTStandardFunctionDeclarator newDeclarator = factory.newFunctionDeclarator(declarator.getName().copy());
		IASTDeclSpecifier newDeclSpec = declGen.createDeclSpecFromType(functionType);
		if (newDeclSpec instanceof ICPPASTDeclSpecifier && fOptions.preserveVirtual()) {
			((ICPPASTDeclSpecifier) newDeclSpec).setVirtual(true);
		}
		IASTSimpleDeclaration simple = factory.newSimpleDeclaration(newDeclSpec);
		if (newDeclarator instanceof ICPPASTFunctionDeclarator) {
			ICPPASTFunctionDeclarator funcDeclarator = (ICPPASTFunctionDeclarator) newDeclarator;
			funcDeclarator.setPureVirtual(false);
			funcDeclarator.setConst(functionType.isConst());
			if (fOptions.addOverride()) {
				funcDeclarator.addVirtSpecifier(((ICPPNodeFactory) factory).newVirtSpecifier(SpecifierKind.Override));
			}
			for (ICPPParameter par : parameters) {
				IASTDeclarator parDeclarator = declGen.createDeclaratorFromType(par.getType(),
						par.getName().toCharArray());
				IASTDeclSpecifier parSpecifier = declGen.createDeclSpecFromType(par.getType());
				IASTParameterDeclaration parameter = factory.newParameterDeclaration(parSpecifier, parDeclarator);
				funcDeclarator.addParameterDeclaration(parameter);
			}
			for (IASTPointerOperator op : declarator.getPointerOperators())
				funcDeclarator.addPointerOperator(op.copy());
			if (declarator instanceof ICPPASTFunctionDeclarator) {
				ICPPASTFunctionDeclarator orig = (ICPPASTFunctionDeclarator) declarator;
				funcDeclarator.setRefQualifier(orig.getRefQualifier());
				IASTTypeId[] typesId = orig.getExceptionSpecification();
				if (typesId == IASTTypeId.EMPTY_TYPEID_ARRAY)
					funcDeclarator.setEmptyExceptionSpecification();
				else {
					for (IASTTypeId typeId : typesId) {
						funcDeclarator.addExceptionSpecificationTypeId(typeId == null ? null : typeId.copy());
					}
				}
				ICPPASTExpression noexceptExpression = orig.getNoexceptExpression();
				if (noexceptExpression != null) {
					funcDeclarator.setNoexceptExpression(
							noexceptExpression == ICPPASTFunctionDeclarator.NOEXCEPT_DEFAULT ? noexceptExpression
									: (ICPPASTExpression) noexceptExpression.copy());
				}
			}
		}
		simple.addDeclarator(newDeclarator);
		simple.setDeclSpecifier(newDeclSpec);
		simple.setParent(getDeclSpecifier());
		return simple;
	}
}
