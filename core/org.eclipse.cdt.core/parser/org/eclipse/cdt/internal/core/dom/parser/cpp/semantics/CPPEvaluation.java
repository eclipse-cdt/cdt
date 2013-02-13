/*******************************************************************************
 * Copyright (c) 2012, 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.DeferredResolutionBinding;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.core.runtime.CoreException;

public abstract class CPPEvaluation implements ICPPEvaluation {

	private IBinding fTemplateDefinition;
	
	CPPEvaluation(IBinding templateDefinition) {
		fTemplateDefinition = templateDefinition;
	}
	
	@Override
	public IBinding getTemplateDefinition() {	
		if (fTemplateDefinition instanceof DeferredResolutionBinding) {
			IBinding toResolve = fTemplateDefinition;
			// While resolve() is called, set fTemplateDefinition to null to avoid
			// infinite recursion in some cases where the resolution process ends
			// up (indirectly) calling getTemplateDefinition() on this evaluation.
			fTemplateDefinition = null;
			fTemplateDefinition = ((DeferredResolutionBinding) toResolve).resolve();
		}
		return fTemplateDefinition;
	}
	
	protected LookupContext getLookupContext(IASTNode point) {
		return new LookupContext(point, getTemplateDefinition());
	}
	
	protected void marshalTemplateDefinition(ITypeMarshalBuffer buffer) throws CoreException {
		// Don't marshal the template definition when building a signature.
		// While the template definition needs to be stored in the index, it does not
		// need to be part of the signature, and trying to resolve it at the time a
		// signature is built sometimes causes recursion (as the call to resolve()
		// may end up needing the signature).
		if (!(buffer instanceof SignatureBuilder))
			buffer.marshalBinding(getTemplateDefinition());
	}
	
	@Override
	public char[] getSignature() {
		SignatureBuilder buf = new SignatureBuilder();
		try {
			marshal(buf, true);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new char[] { '?' };
		}
		return buf.getSignature();
	}

	protected static IBinding resolveUnknown(ICPPUnknownBinding unknown, ICPPTemplateParameterMap tpMap,
			int packOffset, ICPPClassSpecialization within, IASTNode point) {
		try {
			return CPPTemplates.resolveUnknown(unknown, tpMap, packOffset, within, point);
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return unknown;
	}

	protected static ICPPTemplateArgument[] instantiateArguments(ICPPTemplateArgument[] args,
			ICPPTemplateParameterMap tpMap, int packOffset, ICPPClassSpecialization within, IASTNode point) {
		try {
			return CPPTemplates.instantiateArguments(args, tpMap, packOffset, within, point, false);
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return args;
	}

	protected static IBinding instantiateBinding(IBinding binding, ICPPTemplateParameterMap tpMap, int packOffset,
			ICPPClassSpecialization within, int maxdepth, IASTNode point) {
		try {
			return CPPTemplates.instantiateBinding(binding, tpMap, packOffset, within, maxdepth, point);
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return binding;
	}
}