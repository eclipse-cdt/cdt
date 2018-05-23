/*******************************************************************************
 * Copyright (c) 2015, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lukas Wegmann (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStructuredBindingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Collects functionality used by CPPVariable, CPPVariableTemplate and their subclasses.
 */
public class VariableHelpers {
	public static boolean hasStorageClass(IASTName name, IASTNode[] declarations, int storage) {
		int i = -1;
		do {
			if (name != null) {
				IASTNode parent = name.getParent();
				while (!(parent instanceof IASTDeclaration))
					parent = parent.getParent();

				if (parent instanceof IASTSimpleDeclaration) {
					IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration) parent).getDeclSpecifier();
					if (declSpec.getStorageClass() == storage) {
						return true;
					}
				}
			}
			if (declarations != null && ++i < declarations.length) {
				name = (IASTName) declarations[i];
			} else {
				break;
			}
		} while (name != null);
		return false;
	}

	public static boolean isConstexpr(IASTName definition) {
		if (definition == null)
			return false;

		IASTNode parent = definition.getParent();
		while (!(parent instanceof IASTDeclaration)) {
			parent = parent.getParent();
		}

		if (parent instanceof IASTSimpleDeclaration) {
			ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration) parent).getDeclSpecifier();
			if (declSpec != null)
				return declSpec.isConstexpr();
		}
		return false;
	}

	@SuppressWarnings("null")
	public static IType createType(ICPPVariable variable, IASTName definition, IASTName[] declarations,
			boolean allDeclarationsResolved) {
		boolean doneWithDefinition = false;
		IArrayType firstCandidate = null;
		final int length = declarations == null ? 0 : declarations.length;
		for (int i = 0; i <= length; i++) {
			IASTName n;
			// Process the definition according to its relative position among
			// the declarations.
			// See http://bugs.eclipse.org/434150
			if (definition != null && !doneWithDefinition
					&& (i == length || ((ASTNode) definition).getOffset() < ((ASTNode) declarations[i]).getOffset())) {
				n = definition;
				doneWithDefinition = true;
				--i; // We still have to come back to the declaration at position i.
			} else if (i < length) {
				n = declarations[i];
			} else {
				break;
			}
			if (n != null) {
				while (n.getParent() instanceof IASTName) {
					n = (IASTName) n.getParent();
				}

				IASTNode node = n.getParent();
				if (node instanceof IASTDeclarator) {
					IType t = CPPVisitor.createType((IASTDeclarator) node);
					if (!(t instanceof IArrayType) || ((IArrayType) t).hasSize()) {
						return t;
					}
					if (firstCandidate == null) {
						firstCandidate = (IArrayType) t;
					}
				} else if (node instanceof ICPPASTStructuredBindingDeclaration) {
					ICPPASTStructuredBindingDeclaration parent = (ICPPASTStructuredBindingDeclaration) node;
					return CPPVisitor.createType(parent, n);
				}
			}
		}

		if (!allDeclarationsResolved) {
			resolveAllDeclarations(variable, definition, declarations);
			return variable.getType();
		}
		return firstCandidate;
	}

	private static void resolveAllDeclarations(ICPPVariable variable, IASTName definition, IASTName[] declarations) {
		final int length = declarations == null ? 0 : declarations.length;
		for (int i = -1; i < length; i++) {
			@SuppressWarnings("null")
			IASTName n = i == -1 ? definition : declarations[i];
			if (n != null) {
				IASTTranslationUnit tu = n.getTranslationUnit();
				if (tu != null) {
					CPPVisitor.getDeclarations(tu, variable);
					return;
				}
			}
		}
	}

	public static IValue getInitialValue(IASTName definition, IASTName[] declarations, IType type) {
		if (definition != null) {
			final IValue val = getInitialValue(definition, type);
			if (val != null)
				return val;
		}
		if (declarations != null) {
			for (IASTName decl : declarations) {
				if (decl == null)
					break;
				final IValue val = getInitialValue(decl, type);
				if (val != null)
					return val;
			}
		}
		return null;
	}

	private static IValue getInitialValue(IASTName name, IType type) {
		IASTDeclarator dtor = findDeclarator(name);
		if (dtor != null) {
			IASTInitializer init = dtor.getInitializer();
			if (init != null) {
				return SemanticUtil.getValueOfInitializer(init, type);
			}
		}
		return null;
	}

	public static IASTDeclarator findDeclarator(IASTName name) {
		IASTNode node = name.getParent();
		if (node instanceof ICPPASTQualifiedName)
			node = node.getParent();

		if (!(node instanceof IASTDeclarator))
			return null;

		IASTDeclarator dtor = (IASTDeclarator) node;
		while (dtor.getParent() instanceof IASTDeclarator) {
			dtor = (IASTDeclarator) dtor.getParent();
		}

		return dtor;
	}

	public static int getVisibility(ICPPInternalVariable field) {
		ICPPASTVisibilityLabel vis = null;
		IASTDeclaration decl = getPrimaryDeclaration(field);
		if (decl != null) {
			IASTCompositeTypeSpecifier cls = (IASTCompositeTypeSpecifier) decl.getParent();
			IASTDeclaration[] members = cls.getMembers();

			for (IASTDeclaration member : members) {
				if (member == decl)
					break;
				if (member instanceof ICPPASTVisibilityLabel)
					vis = (ICPPASTVisibilityLabel) member;
			}

			if (vis != null) {
				return vis.getVisibility();
			} else if (cls.getKey() == ICPPASTCompositeTypeSpecifier.k_class) {
				return ICPPASTVisibilityLabel.v_private;
			}
		}
		return ICPPASTVisibilityLabel.v_public;
	}

	private static IASTDeclaration getPrimaryDeclaration(ICPPInternalVariable field) {
		// First check if we already know it.
		IASTDeclaration decl = findDeclaration(field.getDefinition());
		if (decl != null) {
			return decl;
		}

		IASTName[] declarations = (IASTName[]) field.getDeclarations();
		if (declarations != null) {
			for (IASTName name : declarations) {
				decl = findDeclaration(name);
				if (decl != null) {
					return decl;
				}
			}
		}

		char[] myName = field.getNameCharArray();

		ICPPClassScope scope = findClassScope(field);
		ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) ASTInternal
				.getPhysicalNodeOfScope(scope);
		IASTDeclaration[] members = compSpec.getMembers();
		for (IASTDeclaration member : members) {
			if (member instanceof IASTSimpleDeclaration) {
				IASTDeclarator[] dtors = ((IASTSimpleDeclaration) member).getDeclarators();
				for (IASTDeclarator dtor : dtors) {
					IASTName name = dtor.getName();
					if (CharArrayUtils.equals(name.getLookupKey(), myName) && name.resolveBinding() == field) {
						return member;
					}
				}
			}
		}
		return null;
	}

	private static ICPPClassScope findClassScope(ICPPInternalVariable v) {
		IScope scope;
		try {
			scope = v.getScope();
		} catch (DOMException e) {
			scope = null;
		}
		while (scope != null) {
			if (scope instanceof ICPPClassScope) {
				return (ICPPClassScope) scope;
			}
			try {
				scope = scope.getParent();
			} catch (DOMException e) {
				return null;
			}
		}
		return null;
	}

	private static IASTDeclaration findDeclaration(IASTNode node) {
		while (node != null && !(node instanceof IASTDeclaration)) {
			node = node.getParent();
		}
		if (node != null && node.getParent() instanceof ICPPASTCompositeTypeSpecifier) {
			return (IASTDeclaration) node;
		}
		return null;
	}
}
