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

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTGCCAttribute;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Base class for C and C++ attributes.
 */
public abstract class ASTGCCAttribute extends ASTNode implements IASTGCCAttribute {
	// Placeholder binding used to indicate that that no name lookup is performed on a IASTName.
	private static class NoNameLookup extends PlatformObject implements IBinding {
		private static final char[] EMPTY_CHAR_ARRAY = {};

		@Override
		public String getName() {
			return ""; //$NON-NLS-1$
		}

		@Override
		public char[] getNameCharArray() {
			return EMPTY_CHAR_ARRAY;
		}

		@Override
		public ILinkage getLinkage() {
			return Linkage.NO_LINKAGE;
		}

		@Override
		public IBinding getOwner() {
			return null;
		}

		@Override
		public IScope getScope() throws DOMException {
			return null;
		}
	}

	private static final NoNameLookup NO_NAME_LOOKUP = new NoNameLookup();
    private IASTName name;
    private IASTExpression[] arguments = IASTExpression.EMPTY_EXPRESSION_ARRAY;

    public ASTGCCAttribute() {
	}

	public ASTGCCAttribute(IASTName name) {
		setName(name);
	}

	protected <T extends ASTGCCAttribute> T copy(T copy, CopyStyle style) {
		super.copy(copy, style);
		copy.setName(name == null ? null : name.copy(style));
		for (IASTExpression argument : arguments) {
			if (argument == null)
				break;
			copy.addArgument(argument.copy(style));
		}
		return copy;
	}

    @Override
	public IASTName getName() {
        return name;
    }

	@Override
	public void setName(IASTName name) {
		assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(ATTRIBUTE_NAME);
		}
    }

	@Override
	public IASTExpression[] getArguments() {
		arguments = ArrayUtil.trim(arguments);
		return arguments;
	}

	@Override
	public void addArgument(IASTExpression argument) {
		assertNotFrozen();
        if (argument != null) {
        	arguments = ArrayUtil.append(arguments, argument);
        	argument.setParent(this);
        	argument.setPropertyInParent(ATTRIBUTE_ARGUMENT);
        	// Disable name lookup for names contained in the argument.
        	argument.accept(new ASTVisitor() {
        		{
        			shouldVisitNames = true;
        		}

        		@Override
				public int visit(IASTName name) {
        			name.setBinding(NO_NAME_LOOKUP);
					return PROCESS_CONTINUE;
        		}
        	});
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
        if (name != null && !name.accept(action)) return false;
        for (IASTExpression argument : arguments) {
        	if (argument == null)
        		break;
            if (!argument.accept(action))
            	return false;
        }
        if (action.shouldVisitAttributes) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }

	@Override
	public int getRoleForName(IASTName n) {
		return r_reference;
	}
}
