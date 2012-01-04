/*******************************************************************************
 * Copyright (c) 2006, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.c.CArrayType;
import org.eclipse.cdt.internal.core.dom.parser.c.CBasicType;
import org.eclipse.cdt.internal.core.dom.parser.c.CFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.c.CPointerType;
import org.eclipse.cdt.internal.core.dom.parser.c.CQualifierType;
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
 * Container for c bindings
 */
class PDOMCLinkage extends PDOMLinkage implements IIndexCBindingConstants {

	public PDOMCLinkage(PDOM pdom, long record) {
		super(pdom, record);
	}

	public PDOMCLinkage(PDOM pdom) throws CoreException {
		super(pdom, C_LINKAGE_NAME, C_LINKAGE_NAME.toCharArray()); 
	}

	@Override
	public int getNodeType() {
		return LINKAGE;
	}
	
	@Override
	public String getLinkageName() {
		return C_LINKAGE_NAME;
	}

	@Override
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
		
			long[] localToFileHolder= {0};
			pdomBinding = adaptBinding(parent, binding, localToFileHolder);
			if (pdomBinding == null) {
				pdomBinding = createBinding(parent, binding, localToFileHolder[0]);
				if (pdomBinding != null) {
					getPDOM().putCachedResult(inputBinding, pdomBinding);
				}
				return pdomBinding;
			}

			getPDOM().putCachedResult(inputBinding, pdomBinding);
		}
		
		if (shouldUpdate(pdomBinding, fromName)) {
			pdomBinding.update(this, fromName.getBinding());
		}
		return pdomBinding;
	}
	
	private PDOMBinding createBinding(PDOMNode parent, IBinding binding, long localToFile) throws CoreException {
		PDOMBinding pdomBinding= null;

		PDOMNode insertIntoIndex= null;
		if (binding instanceof IField) { // must be before IVariable
			if (parent instanceof IPDOMMemberOwner)
				pdomBinding = new PDOMCField(this, (IPDOMMemberOwner)parent, (IField) binding);
		} else if (binding instanceof IVariable) {
			IVariable var= (IVariable) binding;
			pdomBinding = new PDOMCVariable(this, parent, var);
		} else if (binding instanceof IFunction) {
			IFunction func= (IFunction) binding;
			pdomBinding = new PDOMCFunction(this, parent, func);
		} else if (binding instanceof ICompositeType) {
			pdomBinding = new PDOMCStructure(this, parent, (ICompositeType) binding);
		} else if (binding instanceof IEnumeration) {
			pdomBinding = new PDOMCEnumeration(this, parent, (IEnumeration) binding);
		} else if (binding instanceof IEnumerator) {
			assert parent instanceof IEnumeration;
			pdomBinding = new PDOMCEnumerator(this, parent, (IEnumerator) binding);
			insertIntoIndex= parent.getParentNode();
			if (insertIntoIndex == null) {
				insertIntoIndex= this;
			}
		} else if (binding instanceof ITypedef) {
			pdomBinding = new PDOMCTypedef(this, parent, (ITypedef)binding);
		}

		if (pdomBinding != null) {
			pdomBinding.setLocalToFileRec(localToFile);
			parent.addChild(pdomBinding);
			if (insertIntoIndex != null) {
				insertIntoIndex.addChild(pdomBinding);
			}
			if (parent != this && insertIntoIndex != this) {
				insertIntoNestedBindingsIndex(pdomBinding);
			}
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
	}

	@Override
	public final PDOMBinding adaptBinding(final IBinding inputBinding, boolean includeLocal) throws CoreException {
		return adaptBinding(null, inputBinding, includeLocal ? FILE_LOCAL_REC_DUMMY : null);
	}
	
	private final PDOMBinding adaptBinding(final PDOMNode parent, IBinding inputBinding, long[] localToFileHolder) throws CoreException {
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

		result= doAdaptBinding(parent, binding, localToFileHolder);
		if (result != null) {
			getPDOM().putCachedResult(inputBinding, result);
		}
		return result;
	}

	private final PDOMBinding doAdaptBinding(PDOMNode parent, final IBinding binding, long[] localToFileHolder) throws CoreException {
		if (parent == null) {
			parent= getAdaptedParent(binding);
		}
		if (parent == this) {
			final int[] bindingTypes = new int[] {getBindingType(binding)};
			final char[] nameChars = binding.getNameCharArray();
			PDOMBinding nonLocal= FindBinding.findBinding(getIndex(), this, nameChars, bindingTypes, 0);
			if (localToFileHolder == null)
				return nonLocal;
			
			long localToFileRec= getLocalToFileRec(parent, binding, nonLocal);
			if (localToFileRec == 0)
				return nonLocal;
			localToFileHolder[0]= localToFileRec;
			return FindBinding.findBinding(getIndex(), this, nameChars, bindingTypes, localToFileRec);
		} 
		if (parent instanceof IPDOMMemberOwner) {
			final int[] bindingTypes = new int[] {getBindingType(binding)};
			final char[] nameChars = binding.getNameCharArray();
			PDOMBinding nonLocal= FindBinding.findBinding(parent, this, nameChars, bindingTypes, 0);
			if (localToFileHolder == null)
				return nonLocal;

			long localToFileRec= getLocalToFileRec(parent, binding, nonLocal);
			if (localToFileRec == 0)
				return nonLocal;
			localToFileHolder[0]= localToFileRec;
			return FindBinding.findBinding(parent, this, nameChars, bindingTypes, localToFileRec);
		}
		return null;
	}

	@Override
	public PDOMNode getNode(long record, int nodeType) throws CoreException {
		switch (nodeType) {
		case CVARIABLE:
			return new PDOMCVariable(this, record);
		case CFUNCTION:
			return new PDOMCFunction(this, record);
		case CSTRUCTURE:
			return new PDOMCStructure(this, record);
		case CFIELD:
			return new PDOMCField(this, record);
		case CENUMERATION:
			return new PDOMCEnumeration(this, record);
		case CENUMERATOR:
			return new PDOMCEnumerator(this, record);
		case CTYPEDEF:
			return new PDOMCTypedef(this, record);
		}

		assert false;
		return null;
	}
	
	@Override
	public IBTreeComparator getIndexComparator() {
		return new FindBinding.DefaultBindingBTreeComparator(this);
	}

	@Override 
	public PDOMBinding addTypeBinding(IBinding type) throws CoreException {
		return addBinding(type, null);
	}

	
	@Override
	public IType unmarshalType(ITypeMarshalBuffer buffer) throws CoreException {
		int firstByte= buffer.getByte();
		switch((firstByte & ITypeMarshalBuffer.KIND_MASK)) {
		case ITypeMarshalBuffer.ARRAY:
			return CArrayType.unmarshal(firstByte, buffer);
		case ITypeMarshalBuffer.BASIC_TYPE:
			return CBasicType.unmarshal(firstByte, buffer);
		case ITypeMarshalBuffer.CVQUALIFIER:
			return CQualifierType.unmarshal(firstByte, buffer);
		case ITypeMarshalBuffer.FUNCTION_TYPE:
			return CFunctionType.unmarshal(firstByte, buffer);
		case ITypeMarshalBuffer.POINTER:
			return CPointerType.unmarshal(firstByte, buffer);
		case ITypeMarshalBuffer.PROBLEM_TYPE:
			return ProblemType.unmarshal(firstByte, buffer);
		}
		
		throw new CoreException(CCorePlugin.createStatus("Cannot unmarshal a type, first byte=" + firstByte)); //$NON-NLS-1$
	}
}
