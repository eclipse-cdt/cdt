/*******************************************************************************
 * Copyright (c) 2008, 2010 Google, Inc and others.
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
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IndexCPPSignatureUtil;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMOverloader;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Sergey Prigogin
 */
class PDOMCPPUnknownClassInstance extends PDOMCPPUnknownClassType implements ICPPUnknownClassInstance, IPDOMOverloader {

	private static final int ARGUMENTS = PDOMCPPUnknownClassType.RECORD_SIZE + 0;
	private static final int SIGNATURE_HASH = ARGUMENTS + 4;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = SIGNATURE_HASH + 4;
	
	// Cached values.
	ICPPTemplateArgument[] arguments;

	public PDOMCPPUnknownClassInstance(PDOMLinkage linkage, PDOMNode parent, ICPPUnknownClassInstance classInstance)
			throws CoreException {
		super(linkage, parent, classInstance);
		
		final ICPPTemplateArgument[] args= SemanticUtil.getSimplifiedArguments(classInstance.getArguments());
		long rec= PDOMCPPArgumentList.putArguments(this, args);
		final Database db = getDB();
		db.putRecPtr(record + ARGUMENTS, rec);
		try {
			Integer sigHash = IndexCPPSignatureUtil.getSignatureHash(classInstance);
			db.putInt(record + SIGNATURE_HASH, sigHash != null ? sigHash.intValue() : 0);
		} catch (DOMException e) {
		}

	}

	public PDOMCPPUnknownClassInstance(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_UNKNOWN_CLASS_INSTANCE;
	}

	public int getSignatureHash() throws CoreException {
		return getDB().getInt(record + SIGNATURE_HASH);
	}

	public ICPPTemplateArgument[] getArguments() {
		if (arguments == null) {
			try {
				final long rec= getPDOM().getDB().getRecPtr(record+ARGUMENTS);
				arguments= PDOMCPPArgumentList.getArguments(this, rec);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				arguments=  ICPPTemplateArgument.EMPTY_ARGUMENTS;
			}
		}
		return arguments;
	}
	
	@Override
	public boolean isSameType(IType type) {
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}
		
		if (type instanceof PDOMNode) {
			PDOMNode node= (PDOMNode) type;
			// Different PDOM bindings may result in equal types if a parent
			// turns out to be a template parameter.
			if (node.getPDOM() == getPDOM() && node.getRecord() == getRecord()) {
				return true;
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
				final IBinding lhsContainer= getOwner();
				final IBinding rhsContainer= rhs.getOwner();
				if (lhsContainer instanceof IType && rhsContainer instanceof IType) {
					 return (((IType) lhsContainer).isSameType((IType) rhsContainer));
				}
			}
		}
		return false;
 	}
}
