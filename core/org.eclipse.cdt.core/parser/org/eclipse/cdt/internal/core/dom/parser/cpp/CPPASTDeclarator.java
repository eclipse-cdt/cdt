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
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecIncomplete;

/**
 * C++ specific declarator.
 */
public class CPPASTDeclarator extends CPPASTAttributeOwner
		implements ICPPASTDeclarator, IASTImplicitNameOwner, ICPPExecutionOwner {
	private IASTInitializer initializer;
	private IASTName name;
	private IASTImplicitName[] implicitNames;
	private IASTDeclarator nested;
	private IASTPointerOperator[] pointerOps;
	private boolean isPackExpansion;

	public CPPASTDeclarator() {
	}

	public CPPASTDeclarator(IASTName name) {
		setName(name);
	}

	public CPPASTDeclarator(IASTName name, IASTInitializer initializer) {
		this(name);
		setInitializer(initializer);
	}

	@Override
	public CPPASTDeclarator copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTDeclarator copy(CopyStyle style) {
		CPPASTDeclarator copy = new CPPASTDeclarator();
		return copy(copy, style);
	}

	protected <T extends CPPASTDeclarator> T copy(T copy, CopyStyle style) {
		copy.setName(name == null ? null : name.copy(style));
		copy.setInitializer(initializer == null ? null : initializer.copy(style));
		copy.setNestedDeclarator(nested == null ? null : nested.copy(style));
		((CPPASTDeclarator) copy).isPackExpansion = isPackExpansion;
		for (IASTPointerOperator pointer : getPointerOperators()) {
			copy.addPointerOperator(pointer.copy(style));
		}
		return super.copy(copy, style);
	}

	@Override
	public boolean declaresParameterPack() {
		return isPackExpansion;
	}

	@Override
	public IASTPointerOperator[] getPointerOperators() {
		if (pointerOps == null)
			return IASTPointerOperator.EMPTY_ARRAY;
		pointerOps = ArrayUtil.trim(IASTPointerOperator.class, pointerOps);
		return pointerOps;
	}

	@Override
	public IASTDeclarator getNestedDeclarator() {
		return nested;
	}

	@Override
	public IASTName getName() {
		return name;
	}

	@Override
	public IASTInitializer getInitializer() {
		return initializer;
	}

	@Override
	public void setInitializer(IASTInitializer initializer) {
		assertNotFrozen();
		this.initializer = initializer;
		if (initializer != null) {
			initializer.setParent(this);
			initializer.setPropertyInParent(INITIALIZER);
		}
	}

	@Override
	public void addPointerOperator(IASTPointerOperator operator) {
		assertNotFrozen();
		if (operator != null) {
			operator.setParent(this);
			operator.setPropertyInParent(POINTER_OPERATOR);
			pointerOps = ArrayUtil.append(IASTPointerOperator.class, pointerOps, operator);
		}
	}

	/**
	 * Remove a pointer operator from the pointer operators
	 * @param operator Pointer operator to be removed
	 */
	public void removePointerOperator(IASTPointerOperator operator) {
		assertNotFrozen();
		ArrayUtil.remove(pointerOps, operator);
	}

	@Override
	public void setNestedDeclarator(IASTDeclarator nested) {
		assertNotFrozen();
		this.nested = nested;
		if (nested != null) {
			nested.setParent(this);
			nested.setPropertyInParent(NESTED_DECLARATOR);
		}
	}

	@Override
	public void setName(IASTName name) {
		assertNotFrozen();
		this.name = name;
		if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(DECLARATOR_NAME);
		}
	}

	@Override
	public void setDeclaresParameterPack(boolean val) {
		assertNotFrozen();
		isPackExpansion = val;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclarators) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (pointerOps != null) {
			for (IASTPointerOperator op : pointerOps) {
				if (op == null)
					break;
				if (!op.accept(action))
					return false;
			}
		}

		if (!acceptByAttributeSpecifiers(action))
			return false;

		if (nested == null && name != null) {
			IASTDeclarator outermost = ASTQueries.findOutermostDeclarator(this);
			if (outermost.getPropertyInParent() != IASTTypeId.ABSTRACT_DECLARATOR) {
				if (!name.accept(action))
					return false;
				if (action.shouldVisitImplicitNames) {
					for (IASTImplicitName implicitName : getImplicitNames()) {
						if (!implicitName.accept(action))
							return false;
					}
				}
			}
		}

		if (nested != null && !nested.accept(action))
			return false;

		if (!postAccept(action))
			return false;

		if (action.shouldVisitDeclarators && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	protected boolean postAccept(ASTVisitor action) {
		return initializer == null || initializer.accept(action);
	}

	@Override
	public int getRoleForName(IASTName n) {
		// 3.1.2
		IASTNode parent = ASTQueries.findOutermostDeclarator(this).getParent();
		if (parent instanceof IASTDeclaration) {
			// a declaration is a definition unless ...
			if (parent instanceof IASTFunctionDefinition)
				return r_definition;

			if (parent instanceof IASTSimpleDeclaration) {
				final IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration) parent;

				// unless it declares a function without body
				if (this instanceof IASTFunctionDeclarator) {
					return r_declaration;
				}

				final int storage = sdecl.getDeclSpecifier().getStorageClass();
				// unless it contains the extern specifier or a linkage-specification and neither initializer nor function-body
				if (getInitializer() == null
						&& (storage == IASTDeclSpecifier.sc_extern || isSimpleLinkageSpec(sdecl))) {
					return r_declaration;
				}
				// unless it declares a static data member in a class declaration
				if (storage == IASTDeclSpecifier.sc_static
						&& CPPVisitor.getContainingScope(parent) instanceof ICPPClassScope) {
					return r_declaration;
				}
				// unless it is a class name declaration: no declarator in this case
				// unless it is a typedef declaration
				if (storage == IASTDeclSpecifier.sc_typedef)
					return r_definition; // should actually be a declaration

				// unless it is a using-declaration or using-directive: no declarator in this case
			}

			// all other cases
			return r_definition;
		}

		if (parent instanceof IASTTypeId)
			return r_reference;

		if (parent instanceof IASTParameterDeclaration)
			return (n.getLookupKey().length > 0) ? r_definition : r_declaration;

		return r_unclear;
	}

	private boolean isSimpleLinkageSpec(IASTSimpleDeclaration sdecl) {
		IASTNode parent = sdecl.getParent();
		if (parent instanceof ICPPASTLinkageSpecification) {
			ICPPASTLinkageSpecification spec = (ICPPASTLinkageSpecification) parent;
			// todo distinction between braced enclose and simple linkage specification
			if (spec.getDeclarations().length == 1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see IASTImplicitNameOwner#getImplicitNames()
	 */
	@Override
	public IASTImplicitName[] getImplicitNames() {
		if (implicitNames == null) {
			IBinding ctor = CPPSemantics.findImplicitlyCalledConstructor(this);
			if (ctor == null) {
				implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			} else {
				CPPASTImplicitName ctorName = new CPPASTImplicitName(ctor.getNameCharArray(), this);
				ctorName.setBinding(ctor);
				IASTName id = name;
				if (id instanceof ICPPASTQualifiedName) {
					id = id.getLastName();
				}
				ctorName.setOffsetAndLength((ASTNode) id);
				implicitNames = new IASTImplicitName[] { ctorName };
			}
		}

		return implicitNames;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == nested) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			nested = (IASTDeclarator) other;
			return;
		}
		super.replace(child, other);
	}

	@Override
	public ICPPExecution getExecution() {
		final IBinding binding = getName().resolveBinding();
		if (!(binding instanceof ICPPBinding)) // ProblemBinding
			return ExecIncomplete.INSTANCE;
		ICPPEvaluation initializerEval = null;
		if (binding instanceof CPPVariable) {
			CPPVariable variable = (CPPVariable) binding;
			initializerEval = variable.getInitializerEvaluation();
		}
		if (initializerEval == EvalFixed.INCOMPLETE) {
			return ExecIncomplete.INSTANCE;
		}
		return new ExecDeclarator((ICPPBinding) binding, initializerEval);
	}
}
