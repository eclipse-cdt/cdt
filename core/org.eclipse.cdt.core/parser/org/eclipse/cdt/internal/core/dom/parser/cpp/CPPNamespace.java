/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

public class CPPNamespace extends PlatformObject implements ICPPNamespace, ICPPInternalBinding {

	public static class CPPNamespaceProblem extends ProblemBinding implements ICPPNamespace, ICPPNamespaceScope {
		public CPPNamespaceProblem(IASTNode node, int id, char[] arg) {
			super(node, id, arg);
		}

		@Override
		public ICPPNamespaceScope getNamespaceScope() {
			return this;
		}

		@Override
		public IBinding[] getMemberBindings() {
			return IBinding.EMPTY_BINDING_ARRAY;
		}

		@Override
		public void addUsingDirective(ICPPUsingDirective usingDirective) {
		}

		@Override
		public ICPPUsingDirective[] getUsingDirectives() {
			return ICPPUsingDirective.EMPTY_ARRAY;
		}

		@Override
		public ICPPNamespaceScope[] getInlineNamespaces() {
			return ICPPNamespaceScope.EMPTY_NAMESPACE_SCOPE_ARRAY;
		}
	}

	IASTName[] namespaceDefinitions;
	ICPPNamespaceScope scope;
	ICPPASTTranslationUnit tu;

	public CPPNamespace(ICPPASTNamespaceDefinition nsDef) {
		findAllDefinitions(nsDef);
		if (namespaceDefinitions.length == 0) {
			namespaceDefinitions = new IASTName[] { nsDef.getName() };
		}
	}

	@Override
	public IASTNode[] getDeclarations() {
		return namespaceDefinitions;
	}

	@Override
	public IASTNode getDefinition() {
		return tu != null ? tu : (IASTNode) namespaceDefinitions[0];
	}

	static private class NamespaceCollector extends ASTVisitor {
		private ICPPASTNamespaceDefinition namespaceDef;
		private IASTName[] namespaces;

		public NamespaceCollector(ICPPASTNamespaceDefinition ns) {
			shouldVisitNamespaces = true;
			shouldVisitDeclarations = true;
			this.namespaceDef = ns;
		}

		@Override
		public int visit(ICPPASTNamespaceDefinition namespace) {
			ICPPASTNamespaceDefinition orig = namespaceDef, candidate = namespace;
			while (candidate != null) {
				if (!CharArrayUtils.equals(orig.getName().getLookupKey(), candidate.getName().getLookupKey()))
					return PROCESS_CONTINUE;
				if (orig.getParent() instanceof ICPPASTNamespaceDefinition) {
					if (!(candidate.getParent() instanceof ICPPASTNamespaceDefinition))
						return PROCESS_CONTINUE;
					orig = (ICPPASTNamespaceDefinition) orig.getParent();
					candidate = (ICPPASTNamespaceDefinition) candidate.getParent();
				} else if (candidate.getParent() instanceof ICPPASTNamespaceDefinition) {
					return PROCESS_CONTINUE;
				} else {
					break;
				}
			}

			namespaces = ArrayUtil.append(IASTName.class, namespaces, namespace.getName());
			return PROCESS_SKIP;
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			if (declaration instanceof ICPPASTLinkageSpecification)
				return PROCESS_CONTINUE;
			return PROCESS_SKIP;
		}

		public IASTName[] getNamespaces() {
			return ArrayUtil.trim(IASTName.class, namespaces);
		}
	}

	static private class NamespaceMemberCollector extends ASTVisitor {
		public ObjectSet<IBinding> members = new ObjectSet<>(8);

		public NamespaceMemberCollector() {
			shouldVisitNamespaces = true;
			shouldVisitDeclarators = true;
			shouldVisitDeclSpecifiers = true;
			shouldVisitDeclarations = true;
		}

		@Override
		public int visit(IASTDeclarator declarator) {
			while (declarator.getNestedDeclarator() != null)
				declarator = declarator.getNestedDeclarator();

			IBinding binding = declarator.getName().resolveBinding();
			if (binding != null && !(binding instanceof IProblemBinding))
				members.put(binding);

			return PROCESS_SKIP;
		}

		@Override
		public int visit(IASTDeclSpecifier declSpec) {
			if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
				IBinding binding = ((ICPPASTCompositeTypeSpecifier) declSpec).getName().resolveBinding();
				if (binding != null && !(binding instanceof IProblemBinding))
					members.put(binding);
				return PROCESS_SKIP;
			} else if (declSpec instanceof ICPPASTElaboratedTypeSpecifier) {
				IASTNode parent = declSpec.getParent();
				if (parent instanceof IASTSimpleDeclaration) {
					if (((IASTSimpleDeclaration) parent).getDeclarators().length > 0)
						return PROCESS_SKIP;

					IBinding binding = ((ICPPASTElaboratedTypeSpecifier) declSpec).getName().resolveBinding();
					if (binding != null && !(binding instanceof IProblemBinding))
						members.put(binding);
					return PROCESS_SKIP;
				}
			}
			return PROCESS_SKIP;
		}

		@Override
		public int visit(ICPPASTNamespaceDefinition namespace) {
			IBinding binding = namespace.getName().resolveBinding();
			if (binding != null && !(binding instanceof IProblemBinding))
				members.put(binding);
			return PROCESS_SKIP;
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			if (declaration instanceof ICPPASTUsingDeclaration) {
				IBinding binding = ((ICPPASTUsingDeclaration) declaration).getName().resolveBinding();
				if (binding != null && !(binding instanceof IProblemBinding))
					members.put(binding);
				return PROCESS_SKIP;
			} else if (declaration instanceof IASTFunctionDefinition) {
				return visit(((IASTFunctionDefinition) declaration).getDeclarator());
			}
			return PROCESS_CONTINUE;
		}
	}

	private void findAllDefinitions(ICPPASTNamespaceDefinition namespaceDef) {
		NamespaceCollector collector = new NamespaceCollector(namespaceDef);
		namespaceDef.getTranslationUnit().accept(collector);

		namespaceDefinitions = collector.getNamespaces();
		for (IASTName namespaceDefinition : namespaceDefinitions) {
			namespaceDefinition.setBinding(this);
		}
	}

	public IASTName[] getNamespaceDefinitions() {
		return namespaceDefinitions;
	}

	public CPPNamespace(CPPASTTranslationUnit unit) {
		tu = unit;
	}

	@Override
	public ICPPNamespaceScope getNamespaceScope() {
		if (scope == null) {
			if (tu != null) {
				scope = (ICPPNamespaceScope) tu.getScope();
			} else {
				scope = new CPPNamespaceScope(namespaceDefinitions[0].getParent());
			}
		}
		return scope;
	}

	@Override
	public String getName() {
		return new String(getNameCharArray());
	}

	@Override
	public char[] getNameCharArray() {
		return tu != null ? CharArrayUtils.EMPTY_CHAR_ARRAY : namespaceDefinitions[0].getSimpleID();
	}

	@Override
	public IScope getScope() {
		return tu != null ? null : CPPVisitor.getContainingScope(namespaceDefinitions[0]);
	}

	@Override
	public String[] getQualifiedName() {
		return CPPVisitor.getQualifiedName(this);
	}

	@Override
	public char[][] getQualifiedNameCharArray() {
		return CPPVisitor.getQualifiedNameCharArray(this);
	}

	@Override
	public boolean isGloballyQualified() {
		return true;
	}

	@Override
	public void addDefinition(IASTNode node) {
		if (!(node instanceof IASTName))
			return;

		IASTName name = (IASTName) node;
		if (namespaceDefinitions == null) {
			namespaceDefinitions = new IASTName[] { name };
			return;
		}

		if (namespaceDefinitions.length > 0
				&& ((ASTNode) name).getOffset() < ((ASTNode) namespaceDefinitions[0]).getOffset()) {
			namespaceDefinitions = ArrayUtil.prepend(IASTName.class, namespaceDefinitions, name);
		} else {
			namespaceDefinitions = ArrayUtil.append(IASTName.class, namespaceDefinitions, name);
		}
	}

	@Override
	public void addDeclaration(IASTNode node) {
		addDefinition(node);
	}

	@Override
	public IBinding[] getMemberBindings() {
		if (namespaceDefinitions != null) {
			NamespaceMemberCollector collector = new NamespaceMemberCollector();
			for (IASTName namespaceDefinition : namespaceDefinitions) {
				IASTNode parent = namespaceDefinition.getParent();
				if (parent instanceof ICPPASTNamespaceDefinition) {
					IASTDeclaration[] decls = ((ICPPASTNamespaceDefinition) parent).getDeclarations();
					for (IASTDeclaration decl : decls) {
						decl.accept(collector);
					}
				}
			}
			return collector.members.keyArray(IBinding.class);
		}
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public boolean isInline() {
		final ICPPNamespaceScope nsScope = getNamespaceScope();
		if (nsScope instanceof CPPNamespaceScope) {
			return ((CPPNamespaceScope) nsScope).isInlineNamepace();
		}
		return false;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public String toString() {
		String[] names = getQualifiedName();
		if (names.length == 0) {
			return "<global namespace>"; //$NON-NLS-1$
		}
		StringBuilder buf = new StringBuilder();
		for (String name : names) {
			if (buf.length() != 0)
				buf.append(Keywords.cpCOLONCOLON);
			buf.append(name.isEmpty() ? "<anonymous>" : name); //$NON-NLS-1$
		}
		return buf.toString();
	}

	@Override
	public IBinding getOwner() {
		if (namespaceDefinitions != null && namespaceDefinitions.length > 0) {
			return CPPVisitor.findDeclarationOwner(namespaceDefinitions[0], false);
		}
		return null;
	}
}
