/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalNameOwner;

/**
 * Implementation for names in C translation units.
 */
public class CASTName extends ASTNode implements IASTName, IASTCompletionContext {
    private final char[] name;

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private IBinding binding;
    
    public CASTName(char[] name) {
        this.name = name;
    }

    public CASTName() {
        name = CharArrayUtils.EMPTY_CHAR_ARRAY;
    }

    @Override
	public CASTName copy() {
		return copy(CopyStyle.withoutLocations);
	}
    
	@Override
	public CASTName copy(CopyStyle style) {
		CASTName copy = new CASTName(name == null ? null : name.clone());
		return copy(copy, style);
	}

    @Override
	public IBinding resolveBinding() {
        if (binding == null) {
       		CVisitor.createBinding(this);
        }

        return binding;
    }
    
    @Override
	public IBinding resolvePreBinding() {
    	return resolveBinding();
    }

    @Override
	public IBinding getBinding() {
        return binding;
    }
    
    @Override
	public IBinding getPreBinding() {
        return binding;
    }

    @Override
	public IASTCompletionContext getCompletionContext() {
        IASTNode node = getParent();
    	while (node != null) {
    		if (node instanceof IASTCompletionContext) {
    			return (IASTCompletionContext) node;
    		}
    		node = node.getParent();
    	}
    	if (getLength() > 0) {
    		return this;
    	}
    	return null;
    }

    @Override
	public void setBinding(IBinding binding) {
        this.binding = binding;
    }

    @Override
	public String toString() {
        if (name == CharArrayUtils.EMPTY_CHAR_ARRAY)
            return EMPTY_STRING;
        return new String(name);
    }

    @Override
	public char[] toCharArray() {
        return name;
    }

	@Override
	public char[] getSimpleID() {
		return name;
	}
	
	@Override
	public char[] getLookupKey() {
		return name;
	}

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitNames) {
            switch (action.visit(this)) {
            case ASTVisitor.PROCESS_ABORT:
                return false;
            case ASTVisitor.PROCESS_SKIP:
                return true;
            default:
                break;
            }
        }
        
        if (action.shouldVisitNames) {
            switch (action.leave(this)) {
            case ASTVisitor.PROCESS_ABORT:
                return false;
            case ASTVisitor.PROCESS_SKIP:
                return true;
            default:
                break;
            }
        }
        return true;
    }

	@Override
	public int getRoleOfName(boolean allowResolution) {
        IASTNode parent = getParent();
        if (parent instanceof IASTInternalNameOwner) {
        	return ((IASTInternalNameOwner) parent).getRoleForName(this, allowResolution);
        }
        if (parent instanceof IASTNameOwner) {
            return ((IASTNameOwner) parent).getRoleForName(this);
        }
        return IASTNameOwner.r_unclear;
	}

    @Override
	public boolean isDeclaration() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            switch (role) {
            case IASTNameOwner.r_reference:
            case IASTNameOwner.r_unclear:
                return false;
            default:
                return true;
            }
        }
        return false;
    }

    @Override
	public boolean isReference() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            switch (role) {
            case IASTNameOwner.r_reference:
                return true;
            default:
                return false;
            }
        }
        return false;
    }

    @Override
	public boolean isDefinition() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            switch (role) {
            case IASTNameOwner.r_definition:
                return true;
            default:
                return false;
            }
        }
        return false;
    }

	@Override
	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		IASTNode parent = getParent();
		if (parent instanceof IASTElaboratedTypeSpecifier) {
			IASTElaboratedTypeSpecifier specifier = (IASTElaboratedTypeSpecifier) parent;
			int kind = specifier.getKind();
			switch (kind) {
			case IASTElaboratedTypeSpecifier.k_struct:
			case IASTElaboratedTypeSpecifier.k_union:
				break;
			default:
				return null;
			}
			IBinding[] bindings = CVisitor.findBindingsForContentAssist(n, isPrefix);
			return filterByElaboratedTypeSpecifier(kind, bindings);
		}
		return null;
	}

	private IBinding[] filterByElaboratedTypeSpecifier(int kind, IBinding[] bindings) {
		for (int i = 0; i < bindings.length; i++) {
			if (bindings[i] instanceof ICompositeType) {
				ICompositeType type = (ICompositeType) bindings[i];
				
				switch (type.getKey()) { 
				case ICompositeType.k_struct:
					if (kind != IASTElaboratedTypeSpecifier.k_struct) {
						bindings[i] = null;
					}
					break;

				case ICompositeType.k_union:
					if (kind != IASTElaboratedTypeSpecifier.k_union) {
						bindings[i] = null;
					}
					break;
				}
			} else {
				bindings[i]= null;
			}
		}
		return ArrayUtil.removeNulls(bindings);
	}

	@Override
	public IASTName getLastName() {
		return this;
	}

	@Override
	public boolean isQualified() {
		return false;
	}
}
