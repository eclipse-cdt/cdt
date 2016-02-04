/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPDeferredClassInstance;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents a instantiation that cannot be performed because of dependent arguments or an unknown template.
 */
public class CPPDeferredAliasTemplateInstance extends CPPUnknownBinding
		implements ICPPDeferredAliasTemplateInstance, ISerializableType {
	private final ICPPTemplateArgument[] fArguments;
	private final ICPPAliasTemplate fAliasTemplate;

	public CPPDeferredAliasTemplateInstance(ICPPAliasTemplate template, ICPPTemplateArgument[] arguments) {
		// With template template parameters the owner must not be calculated, it'd lead to an infinite loop.
		// Rather than that we override getOwner().
		super(template.getNameCharArray());
		fArguments= arguments;
		fAliasTemplate= template;
	}

	@Override
	public IBinding getOwner() {
		return fAliasTemplate.getOwner();
	}

	@Override
	public CPPDeferredAliasTemplateInstance clone() {
		 return (CPPDeferredAliasTemplateInstance) super.clone();
    }

	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;

		if (type instanceof CPPDeferredAliasTemplateInstance) {
			final CPPDeferredAliasTemplateInstance rhs = (CPPDeferredAliasTemplateInstance) type;
			if (!fAliasTemplate.isSameType(rhs.getTemplateDefinition())) 
				return false;
			
			return CPPTemplates.areSameTemplateArguments(fArguments, rhs.getTemplateArguments());
		}
		return false;
	}

	@Override
	public ICPPAliasTemplate getTemplateDefinition() {
		return fAliasTemplate;
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return fArguments;
	}

	@Override
	public CPPTemplateParameterMap getTemplateParameterMap() {
		ICPPTemplateParameter[] params = fAliasTemplate.getTemplateParameters();
		int size = Math.min(fArguments.length, params.length);
		CPPTemplateParameterMap map = new CPPTemplateParameterMap(size);
		for (int i = 0; i < size; i++) {
			map.put(params[i], fArguments[i]);
		}
		return map;
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this, true);
	}
	
	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		short firstBytes= ITypeMarshalBuffer.DEFERRED_ALIAS_TEMPLATE_INSTANCE;
		buffer.putShort(firstBytes);
		buffer.marshalBinding(fAliasTemplate);
		buffer.putInt(fArguments.length);
		for (ICPPTemplateArgument arg : fArguments) {
			buffer.marshalTemplateArgument(arg);
		}
	}
	
	public static ICPPDeferredClassInstance unmarshal(IIndexFragment fragment, short firstBytes,
			ITypeMarshalBuffer buffer) throws CoreException {
		IBinding template= buffer.unmarshalBinding();
		int argcount= buffer.getInt();
		ICPPTemplateArgument[] args = new ICPPTemplateArgument[argcount];
		for (int i = 0; i < argcount; i++) {
			args[i]= buffer.unmarshalTemplateArgument();
		}
		return new PDOMCPPDeferredClassInstance(fragment, (ICPPClassTemplate) template, args);
	}

	@Override
	public boolean isExplicitSpecialization() {
		return false;
	}

	@Override
	public IBinding getSpecializedBinding() {
		return fAliasTemplate;
	}

	@Override
	@Deprecated
	public IType[] getArguments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public ObjectMap getArgumentMap() {
		// TODO Auto-generated method stub
		return null;
	}
}
