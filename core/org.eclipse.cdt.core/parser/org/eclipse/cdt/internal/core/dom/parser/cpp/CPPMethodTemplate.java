/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;

/**
 * A template for a method.
 */
public class CPPMethodTemplate extends CPPFunctionTemplate implements ICPPMethod {

	public CPPMethodTemplate(IASTName name) {
		super(name);
	}

	public IASTDeclaration getPrimaryDeclaration() {
		// First check if we already know it.
		if (declarations != null) {
			for (IASTName declaration : declarations) {
				IASTNode parent = declaration.getParent();
				while (!(parent instanceof IASTDeclaration) && parent != null)
					parent = parent.getParent();

				IASTDeclaration decl = (IASTDeclaration) parent;
				if (decl != null && decl.getParent() instanceof ICPPASTCompositeTypeSpecifier)
					return decl;
			}
		}
		if (definition != null) {
			IASTNode parent = definition.getParent();
			while (!(parent instanceof IASTDeclaration) && parent != null)
				parent = parent.getParent();

			IASTDeclaration decl = (IASTDeclaration) parent;
			if (decl != null && decl.getParent() instanceof ICPPASTCompositeTypeSpecifier)
				return decl;
		}

		final char[] myName = getTemplateName().getLookupKey();
		IScope scope = getScope();
		if (scope instanceof ICPPTemplateScope) {
			try {
				scope = scope.getParent();
			} catch (DOMException e) {
				return null;
			}
		}
		ICPPClassScope clsScope = (ICPPClassScope) scope;
		ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) ASTInternal
				.getPhysicalNodeOfScope(clsScope);
		IASTDeclaration[] members = compSpec.getMembers();
		for (IASTDeclaration member : members) {
			if (member instanceof ICPPASTTemplateDeclaration) {
				IASTDeclaration decl = ((ICPPASTTemplateDeclaration) member).getDeclaration();
				if (decl instanceof IASTSimpleDeclaration) {
					IASTDeclarator[] dtors = ((IASTSimpleDeclaration) decl).getDeclarators();
					for (IASTDeclarator dtor : dtors) {
						IASTName name = ASTQueries.findInnermostDeclarator(dtor).getName();
						if (CharArrayUtils.equals(name.getLookupKey(), myName) && name.resolveBinding() == this) {
							return member;
						}
					}
				} else if (decl instanceof IASTFunctionDefinition) {
					IASTName name = ASTQueries.findInnermostDeclarator(((IASTFunctionDefinition) decl).getDeclarator())
							.getName();
					if (CharArrayUtils.equals(name.getLookupKey(), myName) && name.resolveBinding() == this) {
						return member;
					}
				}
			}
		}
		return null;
	}

	@Override
	public int getVisibility() {
		IASTDeclaration decl = getPrimaryDeclaration();
		if (decl == null) {
			ICPPClassType cls = getClassOwner();
			if (cls != null) {
				return cls.getKey() == ICPPClassType.k_class ? ICPPASTVisibilityLabel.v_private
						: ICPPASTVisibilityLabel.v_public;
			}
			return ICPPASTVisibilityLabel.v_private;
		}
		IASTCompositeTypeSpecifier cls = (IASTCompositeTypeSpecifier) decl.getParent();
		IASTDeclaration[] members = cls.getMembers();
		ICPPASTVisibilityLabel vis = null;
		for (IASTDeclaration member : members) {
			if (member instanceof ICPPASTVisibilityLabel) {
				vis = (ICPPASTVisibilityLabel) member;
			} else if (member == decl) {
				break;
			}
		}
		if (vis != null) {
			return vis.getVisibility();
		} else if (cls.getKey() == ICPPASTCompositeTypeSpecifier.k_class) {
			return ICPPASTVisibilityLabel.v_private;
		}
		return ICPPASTVisibilityLabel.v_public;
	}

	@Override
	public ICPPClassType getClassOwner() {
		IScope scope = getScope();
		if (scope instanceof ICPPTemplateScope) {
			try {
				scope = scope.getParent();
			} catch (DOMException e) {
				return null;
			}
		}
		if (scope instanceof ICPPClassScope) {
			return ((ICPPClassScope) scope).getClassType();
		}
		return null;
	}

	@Override
	public boolean isVirtual() {
		IASTDeclaration decl = getPrimaryDeclaration();
		if (decl instanceof ICPPASTTemplateDeclaration) {
			ICPPASTDeclSpecifier declSpec = getDeclSpecifier(((ICPPASTTemplateDeclaration) decl).getDeclaration());
			if (declSpec != null) {
				return declSpec.isVirtual();
			}
		}
		return false;
	}

	@Override
	public boolean isStatic(boolean resolveAll) {
		IASTDeclaration decl = getPrimaryDeclaration();
		if (decl instanceof ICPPASTTemplateDeclaration) {
			ICPPASTDeclSpecifier declSpec = getDeclSpecifier(((ICPPASTTemplateDeclaration) decl).getDeclaration());
			if (declSpec != null) {
				return declSpec.getStorageClass() == IASTDeclSpecifier.sc_static;
			}
		}
		return false;
	}

	@Override
	public boolean isInline() {
		IASTDeclaration decl = getPrimaryDeclaration();
		if (decl instanceof ICPPASTTemplateDeclaration
				&& ((ICPPASTTemplateDeclaration) decl).getDeclaration() instanceof IASTFunctionDefinition)
			return true;

		return super.isInline();
	}

	@Override
	public boolean isDestructor() {
		char[] name = getNameCharArray();
		if (name.length > 1 && name[0] == '~')
			return true;

		return false;
	}

	@Override
	public boolean isImplicit() {
		return false;
	}

	@Override
	public boolean isExplicit() {
		IASTDeclaration decl = getPrimaryDeclaration();
		if (decl instanceof ICPPASTTemplateDeclaration) {
			ICPPASTDeclSpecifier declSpec = getDeclSpecifier(((ICPPASTTemplateDeclaration) decl).getDeclaration());
			if (declSpec != null) {
				return declSpec.isExplicit();
			}
		}
		return false;
	}

	@Override
	public boolean isPureVirtual() {
		ICPPASTFunctionDeclarator functionDeclarator = findFunctionDeclarator();
		if (functionDeclarator != null) {
			return functionDeclarator.isPureVirtual();
		}
		return false;
	}

	@Override
	public boolean isOverride() {
		ICPPASTFunctionDeclarator functionDeclarator = findFunctionDeclarator();
		if (functionDeclarator != null) {
			return functionDeclarator.isOverride();
		}
		return false;
	}

	@Override
	public boolean isFinal() {
		ICPPASTFunctionDeclarator functionDeclarator = findFunctionDeclarator();
		if (functionDeclarator != null) {
			return functionDeclarator.isFinal();
		}
		return false;
	}

	private ICPPASTFunctionDeclarator findFunctionDeclarator() {
		IASTName target = null;
		if (declarations != null && declarations.length > 0) {
			target = declarations[0];
		} else {
			target = definition;
		}
		if (target != null) {
			IASTNode parent = target.getParent();
			while (!(parent instanceof IASTDeclarator) && parent != null)
				parent = parent.getParent();

			if (parent instanceof IASTDeclarator) {
				IASTDeclarator dtor = ASTQueries.findTypeRelevantDeclarator((IASTDeclarator) parent);
				if (dtor instanceof ICPPASTFunctionDeclarator) {
					return (ICPPASTFunctionDeclarator) dtor;
				}
			}
		}
		return null;
	}
}
