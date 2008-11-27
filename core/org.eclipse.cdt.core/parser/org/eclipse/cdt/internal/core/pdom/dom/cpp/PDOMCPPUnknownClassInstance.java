/*******************************************************************************
 * Copyright (c) 2008 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergey Prigogin (Google) - initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassInstance;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Sergey Prigogin
 */
class PDOMCPPUnknownClassInstance extends PDOMCPPUnknownClassType implements ICPPUnknownClassInstance {

	private static final int ARGUMENTS = PDOMCPPUnknownClassType.RECORD_SIZE + 0;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPUnknownClassType.RECORD_SIZE + 4;
	
	// Cached values.
	ICPPTemplateArgument[] arguments;

	public PDOMCPPUnknownClassInstance(PDOM pdom, PDOMNode parent, ICPPUnknownClassInstance classInstance)	throws CoreException {
		super(pdom, parent, classInstance);
		
		int rec= PDOMCPPArgumentList.putArguments(this, classInstance.getArguments());
		pdom.getDB().putInt(record + ARGUMENTS, rec);
	}

	public PDOMCPPUnknownClassInstance(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_UNKNOWN_CLASS_INSTANCE;
	}

	public ICPPTemplateArgument[] getArguments() {
		if (arguments == null) {
			try {
				final int rec= getPDOM().getDB().getInt(record+ARGUMENTS);
				arguments= PDOMCPPArgumentList.getArguments(this, rec);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				arguments=  ICPPTemplateArgument.EMPTY_ARGUMENTS;
			}
		}
		return arguments;
	}

	@Override
	public String toString() {
		return getName() + " " + ASTTypeUtil.getArgumentListString(getArguments(), true); //$NON-NLS-1$ 
	}
	
	@Override
	public boolean isSameType(IType type) {
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}
		
		if (type instanceof PDOMNode) {
			PDOMNode node= (PDOMNode) type;
			if (node.getPDOM() == getPDOM()) {
				return node.getRecord() == getRecord();
			}
		}
		
		if (type instanceof ICPPUnknownClassInstance) { 
			ICPPUnknownClassInstance rhs= (ICPPUnknownClassInstance) type;
			if (CharArrayUtils.equals(getNameCharArray(), rhs.getNameCharArray())) {
				ICPPTemplateArgument[] lhsArgs= getArguments();
				ICPPTemplateArgument[] rhsArgs= rhs.getArguments();
				if (lhsArgs != rhsArgs) {
					if (lhsArgs == null || rhsArgs == null)
						return false;
				
					if (lhsArgs.length != rhsArgs.length)
						return false;
				
					for (int i= 0; i < lhsArgs.length; i++) {
						if (!lhsArgs[i].isSameValue(rhsArgs[i])) 
							return false;
					}
				}
				try {
					final IBinding lhsContainer= getOwner();
					final IBinding rhsContainer= rhs.getOwner();
					if (lhsContainer instanceof IType && rhsContainer instanceof IType) {
						 return (((IType)lhsContainer).isSameType((IType) rhsContainer));
					}
				} catch (DOMException e) {
				}
			}
		}
		return false;
 	}
}
