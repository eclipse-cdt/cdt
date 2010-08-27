/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IScope;
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
		if( binding instanceof ICPPInternalBinding ) {
			return ((ICPPInternalBinding)binding).getDeclarations();
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
			return ((ICPPInternalFunction)func).isStatic(resolveAll);
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

	public static String getDeclaredInSourceFileOnly(IBinding binding, boolean requireDefinition, PDOMBinding nonLocal) {
		IASTNode[] decls;
		IASTNode def;
		if (binding instanceof ICPPInternalBinding) {
			ICPPInternalBinding ib= (ICPPInternalBinding) binding;
			decls= ib.getDeclarations();
			def= ib.getDefinition();
		}
		else if (binding instanceof ICInternalBinding) {
			ICInternalBinding ib= (ICInternalBinding) binding;
			decls= ib.getDeclarations();
			def= ib.getDefinition();
		}
		else {
			return null;
		}
		if (requireDefinition && def == null) {
			return null;
		}
		String filePath= null;
		if (def != null) {
			if ( (filePath= isPartOfSource(filePath, def)) == null) {
				return null;
			}
		}
		if (decls != null) {
			for (final IASTNode node : decls) {
				if (node != null) {
					if ( (filePath= isPartOfSource(filePath, node)) == null) {
						return null;
					}
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
		return filePath;
	}

	private static String isPartOfSource(String filePath, IASTNode decl) {
		if (decl instanceof ASTNode) {
			if (((ASTNode) decl).isPartOfSourceFile()) {
				if (filePath == null)
					return decl.getContainingFilename();
				
				if (filePath.equals(decl.getContainingFilename()))
					return filePath;
			}
		}
		return null;
	}

	public static String getDeclaredInOneFileOnly(IBinding binding) {
		IASTNode[] decls;
		IASTNode def;
		if (binding instanceof ICPPInternalBinding) {
			ICPPInternalBinding ib= (ICPPInternalBinding) binding;
			decls= ib.getDeclarations();
			def= ib.getDefinition();
		}
		else if (binding instanceof ICInternalBinding) {
			ICInternalBinding ib= (ICInternalBinding) binding;
			decls= ib.getDeclarations();
			def= ib.getDefinition();
		}
		else {
			return null;
		}
		String filePath= null;
		if (def != null) {
			filePath= def.getContainingFilename();
		}
		if (decls != null) {
			for (final IASTNode node : decls) {
				if (node != null) {
					final String fn = node.getContainingFilename();
					if (filePath == null) {
						filePath= fn;
					} else if (!filePath.equals(fn)) {
						return null;
					}
				}
			}
		}
		return filePath;
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
}
