/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.core.dom.parser.c.ICInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.c.ICInternalFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalFunction;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * Access to methods on scopes and bindings internal to the parser.
 */
public class ASTInternal {
	public static IASTNode[] getDeclarationsOfBinding(IBinding binding) {
		if (binding instanceof ICPPInternalBinding) {
			return ((ICPPInternalBinding) binding).getDeclarations();
		}
		assert false;
		return IASTNode.EMPTY_NODE_ARRAY;
	}

	public static IASTNode getPhysicalNodeOfScope(IScope scope) {
		if (scope instanceof IASTInternalScope) {
			return ((IASTInternalScope) scope).getPhysicalNode();
		}
		return null;
	}

	public static void addBinding(IScope scope, IBinding binding) {
		if (scope instanceof IASTInternalScope) {
			((IASTInternalScope) scope).addBinding(binding);
		}		
	}

	public static void addName(IScope scope, IASTName name) {
		if (scope instanceof IASTInternalScope) {
			((IASTInternalScope) scope).addName(name);
		}		
	}		
	
	public static boolean isStatic(IFunction func, boolean resolveAll) {
		if (func instanceof ICPPInternalFunction) {
			return ((ICPPInternalFunction) func).isStatic(resolveAll);
		}
		if (func instanceof ICInternalFunction) {
			return ((ICInternalFunction) func).isStatic(resolveAll);
		}
		return func.isStatic();
	}

	public static void setFullyResolved(IBinding binding, boolean val) {
		if (binding instanceof ICInternalFunction) {
			((ICInternalFunction) binding).setFullyResolved(true);
		}
	}

	public static IASTNode getDeclaredInSourceFileOnly(IBinding binding, boolean requireDefinition, PDOMBinding nonLocal) {
		IASTNode[] decls;
		IASTNode def;
		if (binding instanceof ICPPInternalBinding) {
			ICPPInternalBinding ib= (ICPPInternalBinding) binding;
			decls= ib.getDeclarations();
			def= ib.getDefinition();
		} else if (binding instanceof ICInternalBinding) {
			ICInternalBinding ib= (ICInternalBinding) binding;
			decls= ib.getDeclarations();
			def= ib.getDefinition();
		} else {
			return null;
		}
		if (requireDefinition && def == null) {
			return null;
		}
		IASTNode result= null;
		if (def != null) {
			if (!isPartOfSource(def))
				return null;
			result= def;
		}
		if (decls != null) {
			for (final IASTNode node : decls) {
				if (node != null) {
					if (!isPartOfSource(node))
						return null;
					if ((result= resolveConflict(result, node)) == null) 
						return null;
				}
			}
		}
		if (requireDefinition && nonLocal != null) {
			try {
				if (nonLocal.hasDeclaration())
					return null;
			} catch (CoreException e) {
			}
		}
		return result;
	}

	private static boolean isPartOfSource(IASTNode decl) {
		return decl instanceof ASTNode && ((ASTNode) decl).isPartOfSourceFile();
	}

	private static IASTNode resolveConflict(IASTNode n1, IASTNode n2) {
		if (n1 == null)
			return n2;
		
		IASTFileLocation loc1= n1.getFileLocation();
		if (loc1 == null)
			return n2;
		
		IASTFileLocation loc2= n2.getFileLocation();
		if (loc2 != null && loc1.getContextInclusionStatement() != loc2.getContextInclusionStatement()) 
			return null;

		return n1;
	}

	public static IASTNode getDeclaredInOneFileOnly(IBinding binding) {
		IASTNode[] decls;
		IASTNode def;
		if (binding instanceof ICPPInternalBinding) {
			ICPPInternalBinding ib= (ICPPInternalBinding) binding;
			decls= ib.getDeclarations();
			def= ib.getDefinition();
		} else if (binding instanceof ICInternalBinding) {
			ICInternalBinding ib= (ICInternalBinding) binding;
			decls= ib.getDeclarations();
			def= ib.getDefinition();
		} else {
			return null;
		}
		IASTNode result= null;
		if (def != null) {
			result= def;
		}
		if (decls != null) {
			for (final IASTNode node : decls) {
				if (node != null) {
					if ((result= resolveConflict(result, node)) == null) 
						return null;
				}
			}
		}
		return result;
	}

	public static void addDeclaration(IBinding b, IASTNode declaration) {
		if (b instanceof ICPPInternalBinding && declaration.isActive()) {
			((ICPPInternalBinding) b).addDeclaration(declaration);
		}
	}

	public static void addDefinition(IBinding b, IASTNode declaration) {
		if (b instanceof ICPPInternalBinding && declaration.isActive()) {
			((ICPPInternalBinding) b).addDefinition(declaration);
		}
	}

	public static boolean hasDeclaration(IBinding binding) {
		if (binding instanceof ICPPInternalBinding) {
			ICPPInternalBinding internal= (ICPPInternalBinding) binding;
			if (internal.getDefinition() != null)
				return true;
			IASTNode[] decls= internal.getDeclarations();
			return decls != null && decls.length > 0 && decls[0] != null;
		} 
		if (binding instanceof IIndexBinding) {
			try {
				return IndexFilter.ALL_DECLARED.acceptBinding(binding);
			} catch (CoreException e) {
			}
			return false;
		} 
		return binding != null;
	}
}
