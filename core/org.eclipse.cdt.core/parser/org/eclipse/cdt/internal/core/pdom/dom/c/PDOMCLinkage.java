/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexCBindingConstants;
import org.eclipse.cdt.internal.core.index.composite.CompositeIndexBinding;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.dom.FindBinding;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMASTAdapter;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
class PDOMCLinkage extends PDOMLinkage implements IIndexCBindingConstants {

	public PDOMCLinkage(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMCLinkage(PDOM pdom) throws CoreException {
		super(pdom, C_LINKAGE_NAME, C_LINKAGE_NAME.toCharArray()); 
	}

	@Override
	public int getNodeType() {
		return LINKAGE;
	}
	
	public String getLinkageName() {
		return C_LINKAGE_NAME;
	}

	public int getLinkageID() {
		return C_LINKAGE_ID;
	}

	private PDOMBinding addBinding(final IBinding inputBinding, IASTName fromName) throws CoreException {
		if (cannotAdapt(inputBinding)) {
			return null;
		}

		PDOMBinding pdomBinding= attemptFastAdaptBinding(inputBinding);
		
		if (pdomBinding == null) {
			// assign names to anonymous types.
			IBinding binding= PDOMASTAdapter.getAdapterForAnonymousASTBinding(inputBinding);
			if (binding == null) 
				return null;

			PDOMNode parent = getAdaptedParent(binding);
			if (parent == null)
				return null;
		
			pdomBinding = adaptBinding(parent, binding);
			if (pdomBinding == null) {
				pdomBinding = createBinding(parent, binding);
				if (pdomBinding != null) {
					pdom.putCachedResult(inputBinding, pdomBinding);
				}
				return pdomBinding;
			}

			pdom.putCachedResult(inputBinding, pdomBinding);
		}
		
		if (shouldUpdate(pdomBinding, fromName)) {
			pdomBinding.update(this, fromName.getBinding());
		}
		return pdomBinding;
	}
	
	private PDOMBinding createBinding(PDOMNode parent, IBinding binding) throws CoreException {
		PDOMBinding pdomBinding= null;
		PDOMNode inheritFileLocal= parent;

		if (binding instanceof IField) { // must be before IVariable
			if (parent instanceof IPDOMMemberOwner)
				pdomBinding = new PDOMCField(pdom, (IPDOMMemberOwner)parent, (IField) binding);
		} else if (binding instanceof IVariable) {
			IVariable var= (IVariable) binding;
			pdomBinding = new PDOMCVariable(pdom, parent, var);
		} else if (binding instanceof IFunction) {
			IFunction func= (IFunction) binding;
			pdomBinding = new PDOMCFunction(pdom, parent, func);
		} else if (binding instanceof ICompositeType) {
			pdomBinding = new PDOMCStructure(pdom, parent, (ICompositeType) binding);
		} else if (binding instanceof IEnumeration) {
			pdomBinding = new PDOMCEnumeration(pdom, parent, (IEnumeration) binding);
		} else if (binding instanceof IEnumerator) {
			try {
				IType enumeration= ((IEnumerator)binding).getType();
				if (enumeration instanceof IEnumeration) {
					PDOMBinding pdomEnumeration = adaptBinding((IEnumeration) enumeration);
					inheritFileLocal= pdomEnumeration;
					if (pdomEnumeration instanceof PDOMCEnumeration)
						pdomBinding = new PDOMCEnumerator(pdom, parent, (IEnumerator) binding, (PDOMCEnumeration)pdomEnumeration);
				}
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}
		} else if (binding instanceof ITypedef) {
			pdomBinding = new PDOMCTypedef(pdom, parent, (ITypedef)binding);
		}

		if (pdomBinding != null) {
			pdomBinding.setLocalToFileRec(getLocalToFileRec(inheritFileLocal, binding));
			parent.addChild(pdomBinding);
			afterAddBinding(pdomBinding);
		}
		return pdomBinding;
	}

	private boolean shouldUpdate(PDOMBinding pdomBinding, IASTName fromName) throws CoreException {
		if (fromName != null) {
			if (fromName.isDefinition()) {
				return true;
			}
			if (fromName.isReference()) {
				return false;
			}
			return !pdomBinding.hasDefinition();
		}
		return false;
	}

	@Override
	public PDOMBinding addBinding(IASTName name) throws CoreException {
		if (name == null)
			return null;

		char[] namechars = name.getSimpleID();
		if (namechars == null)
			return null;

		IBinding binding = name.resolveBinding();
		return addBinding(binding, name);
	}

	@Override
	public int getBindingType(IBinding binding) {
		if (binding instanceof IField)
			// This needs to be before variable
			return CFIELD;
		else if (binding instanceof IVariable)
			return CVARIABLE;
		else if (binding instanceof IFunction)
			return CFUNCTION;
		else if (binding instanceof ICompositeType)
			return CSTRUCTURE;
		else if (binding instanceof IEnumeration)
			return CENUMERATION;
		else if (binding instanceof IEnumerator)
			return CENUMERATOR;
		else if (binding instanceof ITypedef)
			return CTYPEDEF;
		else
			return 0;
	}

	/**
	 * Adapts the parent of the given binding to an object contained in this linkage. May return 
	 * <code>null</code> if the binding cannot be adapted or the binding does not exist and addParent
	 * is set to <code>false</code>.
	 * @param binding the binding to adapt
	 * @return <ul>
	 * <li> null - skip this binding (don't add to pdom)
	 * <li> this - for global scope
	 * <li> a PDOMBinding instance - parent adapted binding
	 * </ul>
	 * @throws CoreException
	 */
	final private PDOMNode getAdaptedParent(IBinding binding) throws CoreException {
		try {
			if (binding instanceof IIndexBinding) {
				IIndexBinding ib= (IIndexBinding) binding;
				if (ib.isFileLocal()) {
					return null;
				}
			} 
			
			IBinding owner= binding.getOwner();
			if (owner == null) {
				return this;
			}
			if (owner instanceof IFunction) {
				return null;
			}

			return adaptBinding(owner);
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	@Override
	public final PDOMBinding adaptBinding(final IBinding inputBinding) throws CoreException {
		return adaptBinding(null, inputBinding);
	}
	
	private final PDOMBinding adaptBinding(final PDOMNode parent, IBinding inputBinding) throws CoreException {
		if (inputBinding instanceof CompositeIndexBinding) {
			inputBinding= ((CompositeIndexBinding) inputBinding).getRawBinding();
		}
		
		if (cannotAdapt(inputBinding)) {
			return null;
		}
		PDOMBinding result= attemptFastAdaptBinding(inputBinding);
		if (result != null) {
			return result;
		}

		// assign names to anonymous types.
		IBinding binding= PDOMASTAdapter.getAdapterForAnonymousASTBinding(inputBinding);
		if (binding == null) {
			return null;
		}

		result= doAdaptBinding(parent, binding);
		if (result != null) {
			pdom.putCachedResult(inputBinding, result);
		}
		return result;
	}

	private final PDOMBinding doAdaptBinding(PDOMNode parent, final IBinding binding) throws CoreException {
		if (parent == null) {
			parent= getAdaptedParent(binding);
		}
		PDOMNode inheritFileLocal= parent;
		if (binding instanceof IEnumerator) {
			try {
				IType enumeration= ((IEnumerator)binding).getType();
				if (enumeration instanceof IEnumeration) {
					inheritFileLocal= adaptBinding((IEnumeration) enumeration);
				}
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}
		}

		if (parent == this) {
			int localToFileRec= getLocalToFileRec(inheritFileLocal, binding);
			return FindBinding.findBinding(getIndex(), getPDOM(), binding.getNameCharArray(), new int[] {getBindingType(binding)}, localToFileRec);
		} 
		if (parent instanceof IPDOMMemberOwner) {
			int localToFileRec= getLocalToFileRec(inheritFileLocal, binding);
			return FindBinding.findBinding(parent, getPDOM(), binding.getNameCharArray(), new int[] {getBindingType(binding)}, localToFileRec);
		}
		return null;
	}

	@Override
	public PDOMNode getNode(int record) throws CoreException {
		if (record == 0)
			return null;

		switch (PDOMNode.getNodeType(pdom, record)) {
		case CVARIABLE:
			return new PDOMCVariable(pdom, record);
		case CFUNCTION:
			return new PDOMCFunction(pdom, record);
		case CSTRUCTURE:
			return new PDOMCStructure(pdom, record);
		case CFIELD:
			return new PDOMCField(pdom, record);
		case CENUMERATION:
			return new PDOMCEnumeration(pdom, record);
		case CENUMERATOR:
			return new PDOMCEnumerator(pdom, record);
		case CTYPEDEF:
			return new PDOMCTypedef(pdom, record);
		case CPARAMETER:
			return new PDOMCParameter(pdom, record);
		case CBASICTYPE:
			return new PDOMCBasicType(pdom, record);
		case CFUNCTIONTYPE:
			return new PDOMCFunctionType(pdom, record);
		}

		return super.getNode(record);
	}

	@Override
	public PDOMNode addType(PDOMNode parent, IType type) throws CoreException {
		if(type instanceof IProblemBinding)
			return null;
		
		if (type instanceof ICBasicType) {
			return new PDOMCBasicType(pdom, parent, (ICBasicType)type);
		} else if(type instanceof IFunctionType) {
			return new PDOMCFunctionType(pdom, parent, (IFunctionType)type);
		} else if (type instanceof IBinding) {
			return addBinding((IBinding)type, null);
		}
		
		return super.addType(parent, type); 
	}
	
	@Override
	public IBTreeComparator getIndexComparator() {
		return new FindBinding.DefaultBindingBTreeComparator(getPDOM());
	}
}
