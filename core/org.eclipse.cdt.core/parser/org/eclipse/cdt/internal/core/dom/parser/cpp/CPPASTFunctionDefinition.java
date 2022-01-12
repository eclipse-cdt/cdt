/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

/**
 * Models a function definition without a try-block. If used for a constructor definition
 * it may contain member initializers.
 */
public class CPPASTFunctionDefinition extends CPPASTAttributeOwner
		implements ICPPASTFunctionDefinition, IASTImplicitNameOwner {
	private IASTDeclSpecifier declSpecifier;
	private IASTFunctionDeclarator declarator;
	private IASTStatement bodyStatement;
	private ICPPASTConstructorChainInitializer[] memInits;
	private IASTImplicitName[] implicitNames; // for constructors: base constructors called implicitly
	private int memInitPos = -1;
	private boolean fDeleted;
	private boolean fDefaulted;

	public CPPASTFunctionDefinition() {
	}

	public CPPASTFunctionDefinition(IASTDeclSpecifier declSpecifier, IASTFunctionDeclarator declarator,
			IASTStatement bodyStatement) {
		setDeclSpecifier(declSpecifier);
		setDeclarator(declarator);
		setBody(bodyStatement);
	}

	@Override
	public CPPASTFunctionDefinition copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTFunctionDefinition copy(CopyStyle style) {
		CPPASTFunctionDefinition copy = new CPPASTFunctionDefinition();
		copy.setDeclSpecifier(declSpecifier == null ? null : declSpecifier.copy(style));

		if (declarator != null) {
			IASTDeclarator outer = ASTQueries.findOutermostDeclarator(declarator);
			outer = outer.copy(style);
			copy.setDeclarator((IASTFunctionDeclarator) ASTQueries.findTypeRelevantDeclarator(outer));
		}

		copy.setBody(bodyStatement == null ? null : bodyStatement.copy(style));

		for (ICPPASTConstructorChainInitializer initializer : getMemberInitializers()) {
			copy.addMemberInitializer(initializer == null ? null : initializer.copy(style));
		}

		copy.fDefaulted = fDefaulted;
		copy.fDeleted = fDeleted;
		return copy(copy, style);
	}

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
		return declSpecifier;
	}

	@Override
	public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
		assertNotFrozen();
		declSpecifier = declSpec;
		if (declSpec != null) {
			declSpec.setParent(this);
			declSpec.setPropertyInParent(DECL_SPECIFIER);
		}
	}

	@Override
	public IASTFunctionDeclarator getDeclarator() {
		return declarator;
	}

	@Override
	public void setDeclarator(IASTFunctionDeclarator declarator) {
		assertNotFrozen();
		this.declarator = declarator;
		if (declarator != null) {
			IASTDeclarator outerDtor = ASTQueries.findOutermostDeclarator(declarator);
			outerDtor.setParent(this);
			outerDtor.setPropertyInParent(DECLARATOR);
		}
	}

	@Override
	public IASTStatement getBody() {
		return bodyStatement;
	}

	@Override
	public void setBody(IASTStatement statement) {
		assertNotFrozen();
		bodyStatement = statement;
		if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(FUNCTION_BODY);
		}
	}

	@Override
	public void addMemberInitializer(ICPPASTConstructorChainInitializer initializer) {
		assertNotFrozen();
		if (initializer != null) {
			memInits = ArrayUtil.appendAt(ICPPASTConstructorChainInitializer.class, memInits, ++memInitPos,
					initializer);
			initializer.setParent(this);
			initializer.setPropertyInParent(MEMBER_INITIALIZER);
		}
	}

	@Override
	public ICPPASTConstructorChainInitializer[] getMemberInitializers() {
		if (memInits == null)
			return ICPPASTConstructorChainInitializer.EMPTY_CONSTRUCTORCHAININITIALIZER_ARRAY;

		return memInits = ArrayUtil.trimAt(ICPPASTConstructorChainInitializer.class, memInits, memInitPos);
	}

	@Override
	public IScope getScope() {
		return ((ICPPASTFunctionDeclarator) declarator).getFunctionScope();
	}

	@Override
	public boolean isDefaulted() {
		return fDefaulted;
	}

	@Override
	public boolean isDeleted() {
		return fDeleted;
	}

	@Override
	public void setIsDefaulted(boolean isDefaulted) {
		assertNotFrozen();
		fDefaulted = isDefaulted;
	}

	@Override
	public void setIsDeleted(boolean isDeleted) {
		assertNotFrozen();
		fDeleted = isDeleted;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclarations) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (!acceptByAttributeSpecifiers(action))
			return false;

		if (declSpecifier != null && !declSpecifier.accept(action))
			return false;

		final IASTDeclarator outerDtor = ASTQueries.findOutermostDeclarator(declarator);
		if (outerDtor != null && !outerDtor.accept(action))
			return false;

		final ICPPASTConstructorChainInitializer[] chain = getMemberInitializers();
		for (ICPPASTConstructorChainInitializer memInit : chain) {
			if (!memInit.accept(action))
				return false;
		}

		if (action.shouldVisitImplicitNames) {
			for (IASTImplicitName implicitName : getImplicitNames()) {
				if (!implicitName.accept(action)) {
					return false;
				}
			}
		}

		if (bodyStatement != null && !bodyStatement.accept(action))
			return false;

		if (!acceptCatchHandlers(action))
			return false;

		if (action.shouldVisitDeclarations && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	/**
	 * Allows subclasses to visit catch handlers, returns whether the visit should continue.
	 */
	protected boolean acceptCatchHandlers(ASTVisitor action) {
		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (bodyStatement == child) {
			other.setPropertyInParent(bodyStatement.getPropertyInParent());
			other.setParent(bodyStatement.getParent());
			bodyStatement = (IASTStatement) other;
			return;
		}
		super.replace(child, other);
	}

	@Override
	public IASTImplicitName[] getImplicitNames() {
		if (implicitNames == null) {
			implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			IASTName functionName = ASTQueries.findInnermostDeclarator(declarator).getName();
			IBinding function = functionName.resolveBinding();
			if (function instanceof ICPPConstructor) {
				CPPSemantics.pushLookupPoint(this);
				try {
					ICPPClassType classOwner = ((ICPPConstructor) function).getClassOwner();

					// Determine the bases of 'classOwner' that need to be initialized by this constructor.
					Set<ICPPClassType> basesThatNeedInitialization = new HashSet<>();
					for (ICPPBase base : classOwner.getBases()) {
						IType baseType = base.getBaseClassType();
						if (baseType instanceof ICPPClassType) {
							basesThatNeedInitialization.add((ICPPClassType) baseType);
						}
					}
					for (ICPPClassType virtualBase : ClassTypeHelper.getVirtualBases(classOwner)) {
						basesThatNeedInitialization.add(virtualBase);
					}

					// Go through the bases determined above, and see which ones aren't initialized
					// explicitly in the mem-initializer list.
					for (ICPPClassType base : basesThatNeedInitialization) {
						if (!isInitializedExplicitly(base)) {
							// Try to find a default constructor to create an implicit name for.
							for (ICPPConstructor constructor : base.getConstructors()) {
								if (constructor.getRequiredArgumentCount() == 0) { // default constructor
									CPPASTImplicitName ctorName = new CPPASTImplicitName(constructor.getNameCharArray(),
											this);
									ctorName.setBinding(constructor);
									ctorName.setOffsetAndLength((ASTNode) functionName);
									implicitNames = ArrayUtil.append(implicitNames, ctorName);
									break;
								}
							}
						}
					}
				} finally {
					CPPSemantics.popLookupPoint();
				}
			}
			implicitNames = ArrayUtil.trim(implicitNames);
		}
		return implicitNames;
	}

	// Returns whether the base type 'base' is explicitly initialized by one of the mem-initializers
	// of this constructor.
	private boolean isInitializedExplicitly(ICPPClassType base) {
		for (ICPPASTConstructorChainInitializer memInitializer : getMemberInitializers()) {
			IBinding binding = memInitializer.getMemberInitializerId().resolveBinding();
			if (binding instanceof IType) {
				if (((IType) binding).isSameType(base)) {
					return true;
				}
			}
			if (binding instanceof ICPPConstructor) {
				if (((ICPPConstructor) binding).getClassOwner().isSameType(base)) {
					return true;
				}
			}
		}
		return false;
	}
}
