/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Bryan Wilkinson (QNX)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalNameOwner;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.core.runtime.Assert;

/**
 * Qualified name, which can contain any other name (unqualified, operator-name, conversion name, 
 * template id).
 */
public class CPPASTQualifiedName extends CPPASTNameBase 
		implements ICPPASTQualifiedName, IASTCompletionContext {

	private IASTName[] names = null;
	private int namesPos= -1;
	private boolean isFullyQualified;
	private String signature;

	public CPPASTQualifiedName() {
	}

	public CPPASTQualifiedName copy() {
		CPPASTQualifiedName copy = new CPPASTQualifiedName();
		for(IASTName name : getNames())
			copy.addName(name == null ? null : name.copy());
		copy.setFullyQualified(isFullyQualified);
		copy.setSignature(signature);
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	@Override
	public final IBinding resolvePreBinding() {
		// The full qualified name resolves to the same thing as the last name
		return resolvePreBinding(getLastName());
	}

	@Override
	public IBinding resolveBinding() {
		// The full qualified name resolves to the same thing as the last name
		IASTName lastName= getLastName();
		return lastName == null ? null : lastName.resolveBinding();
	}

    @Override
	public final IBinding getPreBinding() {
		// The full qualified name resolves to the same thing as the last name
		return getPreBinding(getLastName());
    }
    
	@Override
	public IBinding getBinding() {
		return getLastName().getBinding();
	}

	@Override
	public void setBinding(IBinding binding) {
		getLastName().setBinding(binding);
	}

	public IASTCompletionContext getCompletionContext() {
        IASTNode node = getParent();
    	while (node != null) {
    		if (node instanceof IASTCompletionContext) {
    			return (IASTCompletionContext) node;
    		}
    		node = node.getParent();
    	}
    	
    	return null;
	}

	@Override
	public String toString() {
		return (signature == null) ? "" : signature; //$NON-NLS-1$
	}

	public void addName(IASTName name) {
        assertNotFrozen();
		assert !(name instanceof ICPPASTQualifiedName);
		if (name != null) {
			names = (IASTName[]) ArrayUtil.append(IASTName.class, names, ++namesPos, name);
			name.setParent(this);
			name.setPropertyInParent(SEGMENT_NAME);
		}
	}

	public IASTName[] getNames() {
		if (namesPos < 0)
			return IASTName.EMPTY_NAME_ARRAY;
        
		names = (IASTName[]) ArrayUtil.removeNullsAfter(IASTName.class, names, namesPos);
		return names;
	}

	@Override
	public IASTName getLastName() {
		if (namesPos < 0)
			return null;
		
		return names[namesPos];
	}
	
	public char[] toCharArray() {
		if (namesPos < 0)
			return new char[0];

		// count first
		int len = -2;
		for (int i = 0; i <= namesPos; ++i) {
			char[] n = names[i].toCharArray();
			if (n == null)
				return null;
			len+= 2;
			len+= n.length;
		}
		
		char[] nameArray = new char[len];
		int pos = 0;
		for (int i = 0; i <= namesPos; i++) {
			if (i != 0) {
				nameArray[pos++] = ':';
				nameArray[pos++] = ':';
			}
			final char[] n = names[i].toCharArray();
			System.arraycopy(n, 0, nameArray, pos, n.length);
			pos += n.length;
		}
		return nameArray;
	}

	public boolean isFullyQualified() {
		return isFullyQualified;
	}

	public void setFullyQualified(boolean isFullyQualified) {
        assertNotFrozen();
		this.isFullyQualified = isFullyQualified;
	}


	public void setSignature(String signature) {
        assertNotFrozen();
		this.signature = signature;
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
		for (int i = 0; i <= namesPos; i++) {
			if (i == namesPos) {
				// pointer-to-member qualified names have a dummy name as the last part of the name, don't visit it
				if (names[i].toCharArray().length > 0 && !names[i].accept(action))
					return false;
			} else if (!names[i].accept(action))
				return false;
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
			if (role == IASTNameOwner.r_reference) return false;
			return true;
		}
		return false;
	}

	public boolean isReference() {
		IASTNode parent = getParent();
		if (parent instanceof IASTNameOwner) {
			int role = ((IASTNameOwner) parent).getRoleForName(this);
			if (role == IASTNameOwner.r_reference) return true;
			return false;
		}
		return false;
	}

	public int getRoleForName(IASTName n) {
		for (int i=0; i < namesPos; ++i) {
			if (names[i] == n) 
				return r_reference;
		}
		if (getLastName() == n) {
			IASTNode p = getParent();
			if (p instanceof IASTNameOwner) {
				return ((IASTNameOwner)p).getRoleForName(this);
			}
		}
		return r_unclear;
	}
	
	public boolean isConversionOrOperator() {
		final IASTName lastName= getLastName();
		if (lastName instanceof ICPPASTConversionName || lastName instanceof ICPPASTOperatorName) {
			return true;
		}
		
		// check templateId's name
		if (lastName instanceof ICPPASTTemplateId) {
			IASTName tempName = ((ICPPASTTemplateId)lastName).getTemplateName();
			if (tempName instanceof ICPPASTConversionName || tempName instanceof ICPPASTOperatorName) {
				return true;
			}
		}
		
		return false;
	}
    
    public boolean isDefinition() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            if (role == IASTNameOwner.r_definition) return true;
            return false;
        }
        return false;
    }

	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix);
		
		if (namesPos > 0) {
			IBinding binding = names[namesPos-1].resolveBinding();
			if (binding instanceof ICPPClassType) {
				ICPPClassType classType = (ICPPClassType) binding;
				final boolean isDeclaration = getParent().getParent() instanceof IASTSimpleDeclaration;
				List<IBinding> filtered = filterClassScopeBindings(classType, bindings, isDeclaration);
			
				if (isDeclaration && nameMatches(classType.getNameCharArray(),
						n.toCharArray(), isPrefix)) {
					try {
						ICPPConstructor[] constructors = classType.getConstructors();
						for (int i = 0; i < constructors.length; i++) {
							if (!constructors[i].isImplicit()) {
								filtered.add(constructors[i]);
							}
						}
					} catch (DOMException e) {
					}
				}
				
				return filtered.toArray(new IBinding[filtered.size()]);
			}
		}

		return bindings;
	}
	
	private List<IBinding> filterClassScopeBindings(ICPPClassType classType,
			IBinding[] bindings, final boolean isDeclaration) {
		List<IBinding> filtered = new ArrayList<IBinding>();
		
		try {
			for (int i = 0; i < bindings.length; i++) {
				final IBinding binding = bindings[i];
				if (binding instanceof IField) {
					IField field = (IField) binding;
					if (!field.isStatic()) 
						continue;
				} else if (binding instanceof ICPPMethod) {
					ICPPMethod method = (ICPPMethod) binding;
					if (method.isImplicit()) 
						continue;
					if (!isDeclaration) {
						if (method.isDestructor() || method instanceof ICPPConstructor || !method.isStatic())
							continue;
					}
				} else if (binding instanceof IEnumerator || binding instanceof IEnumerator) {
					if (isDeclaration)
						continue;
				} else if (binding instanceof IType) {
					IType type = (IType) binding;
					if (type.isSameType(classType)) 
						continue;
				} 
				filtered.add(binding);
			}
		} catch (DOMException e) {
		}
		
		return filtered;
	}
	
	private boolean nameMatches(char[] potential, char[] name, boolean isPrefix) {
		if (isPrefix)
			return CharArrayUtils.equals(potential, 0, name.length, name, true);
		return CharArrayUtils.equals(potential, name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTName#getLinkage()
	 */
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	protected IBinding createIntermediateBinding() {
		Assert.isLegal(false);
		return null;
	}
}
