/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.DeferredResolutionBinding;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

/**
 * Base class for evaluations that are dependent, or that have been instantiated
 * from a dependent evaluation. These evaluations keep track of the template
 * in which they are defined, so that certain name lookups can be performed
 * starting from their point of definition.
 */
public abstract class CPPDependentEvaluation extends CPPEvaluation {

	private IBinding fTemplateDefinition;
	
	CPPDependentEvaluation(IBinding templateDefinition) {
		fTemplateDefinition = templateDefinition;
	}
	
	public CPPDependentEvaluation(IASTNode pointOfDefinition) {
		this(SemanticUtil.findEnclosingTemplate(pointOfDefinition));
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
}