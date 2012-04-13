/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTAttributeOwner;
import org.eclipse.cdt.core.dom.ast.IASTToken;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * Base class for C and C++ attributes.
 */
public abstract class ASTAttribute extends ASTNode implements IASTAttribute {
	private static final String[] NORETURN_ATTRIBUTES = new String[] { "__noreturn__", "noreturn" };  //$NON-NLS-1$//$NON-NLS-2$

    private final char[] name;
    private final IASTToken argumentClause;

	public ASTAttribute(char[] name, IASTToken arguments) {
		this.name = name;
		this.argumentClause = arguments;
	}

    @Override
	public char[] getName() {
        return name;
    }

	@Override
	public IASTToken getArgumentClause() {
		return argumentClause;
	}

	@Override
	public void setArgumentClause(IASTToken argumentClause) {
		assertNotFrozen();
		if (argumentClause != null) {
			argumentClause.setParent(this);
			argumentClause.setPropertyInParent(ARGUMENT_CLAUSE);
		}
	}

	@Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitAttributes) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}

        if (argumentClause != null && !argumentClause.accept(action)) return false;

        if (action.shouldVisitAttributes && action.leave(this) == ASTVisitor.PROCESS_ABORT)
            return false;

        return true;
    }

	/**
	 * Returns {@code true} if a declarator has an attribute with one of the given names.
	 * The {@code names} array is assumed to be small. 
	 */
	public static boolean hasAttribute(IASTAttributeOwner node, String[] names) {
	    IASTAttribute[] attributes = node.getAttributes();
		for (IASTAttribute attribute : attributes) {
			char[] name = attribute.getName();
			for (int i = 0; i < names.length; i++) {
				if (CharArrayUtils.equals(name, names[i]))
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns {@code true} if the node has a "noreturn" or "__noreturn__" attribute.
	 */
	public static boolean hasNoreturnAttribute(IASTAttributeOwner node) {
		return hasAttribute(node, NORETURN_ATTRIBUTES);
	}

	/**
	 * Returns character representation of the attribute argument, or {@code null} if the attribute
	 * has zero or more than one argument.
	 */
	public static char[] getSimpleArgument(IASTAttribute attribute) {
		IASTToken argumentClause = attribute.getArgumentClause();
		if (argumentClause == null)
			return null;
		IToken token = argumentClause.getToken();
		return token != null ? token.getCharImage() : null;
	}
}
