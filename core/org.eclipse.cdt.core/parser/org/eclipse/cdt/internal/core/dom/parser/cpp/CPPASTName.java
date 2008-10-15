/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Bryan Wilkinson (QNX)
 *    Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalNameOwner;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.Assert;

/**
 * @author jcamelon
 */
public class CPPASTName extends ASTNode implements IASTName, IASTCompletionContext {
	/**
	 * For test-purposes, only.
	 */
	public static boolean fAllowRecursionBindings= true;
	
	final static class RecursionResolvingBinding extends ProblemBinding {
		public RecursionResolvingBinding(IASTName node) {
			super(node, IProblemBinding.SEMANTIC_RECURSION_IN_LOOKUP, node.toCharArray());
			Assert.isTrue(fAllowRecursionBindings, getMessage());
		}
	}

    private static final char[] EMPTY_CHAR_ARRAY = {};
	private static final String EMPTY_STRING = "";  //$NON-NLS-1$

	static final int MAX_RESOLUTION_DEPTH = 5;
	
	private char[] name;
    private IBinding binding = null;
    private int fResolutionDepth= 0;

    public CPPASTName(char[] name) {
        this.name = name;
    }

    public CPPASTName() {
        name = EMPTY_CHAR_ARRAY;
    }

    public IBinding resolveBinding() {
        if (binding == null) {
        	if (++fResolutionDepth > MAX_RESOLUTION_DEPTH) {
        		binding = new RecursionResolvingBinding(this);
        	} else {
        		binding = CPPVisitor.createBinding(this);
        	}
        }
        return binding;
    }

	public void incResolutionDepth() {
		if (binding == null && ++fResolutionDepth > MAX_RESOLUTION_DEPTH) {
    		binding = new RecursionResolvingBinding(this);
		}
	}

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

	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		IASTNode parent = getParent();
		if (parent instanceof ICPPASTElaboratedTypeSpecifier) {
			ICPPASTElaboratedTypeSpecifier specifier = (ICPPASTElaboratedTypeSpecifier) parent;
			int kind = specifier.getKind();
			switch (kind) {
			case ICompositeType.k_struct:
			case ICompositeType.k_union:
			case ICPPASTElaboratedTypeSpecifier.k_class:
				break;
			default:
				return null;
			}
			IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix);
			return filterByElaboratedTypeSpecifier(kind, bindings);
		}
		else if (parent instanceof IASTDeclarator) {
			IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix);
			for (int i = 0; i < bindings.length; i++) {
				if (bindings[i] instanceof ICPPNamespace || bindings[i] instanceof ICPPClassType) {
				} else {
					bindings[i] = null;
				}
			}
			return (IBinding[])ArrayUtil.removeNulls(IBinding.class, bindings);
		}
		return null;
	}

    private IBinding[] filterByElaboratedTypeSpecifier(int kind, IBinding[] bindings) {
		for (int i = 0; i < bindings.length; i++) {
			if (bindings[i] instanceof ICPPNamespace) {
			} else if (bindings[i] instanceof ICPPClassType) {
				ICPPClassType type = (ICPPClassType) bindings[i];
				try {
					switch (type.getKey()) {
					case ICompositeType.k_struct:
						if (kind != ICompositeType.k_struct) {
							bindings[i] = null;
						}
						break;
					case ICompositeType.k_union:
						if (kind != ICompositeType.k_union) {
							bindings[i] = null;
						}
						break;
					case ICPPClassType.k_class:
						if (kind != ICPPASTElaboratedTypeSpecifier.k_class) {
							bindings[i] = null;
						}
						break;
					}
				} catch (DOMException e) {
					bindings[i] = null;
					CCorePlugin.log(e);
				}
			} else {
				bindings[i]= null;
			}
		}
		return (IBinding[])ArrayUtil.removeNulls(IBinding.class, bindings);
	}

	public void setBinding(IBinding binding) {
        this.binding = binding;
        fResolutionDepth= 0;
    }

    public IBinding getBinding() {
        return binding;
    }

    @Override
	public String toString() {
        if (name == EMPTY_CHAR_ARRAY)
            return EMPTY_STRING;
        return new String(name);
    }

    public char[] toCharArray() {
        return name;
    }

    public void setName(char[] name) {
        this.name = name;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTName#getLinkage()
	 */
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	public IASTName getLastName() {
		return this;
	}
}
