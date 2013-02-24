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

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Base class for evaluations that are dependent, or that have been instantiated
 * from a dependent evaluation. These evaluations keep track of the template
 * in which they are defined, so that certain name lookups can be performed
 * starting from their point of definition.
 */
public abstract class CPPDependentEvaluation extends CPPEvaluation {

	private IBinding fTemplateDefinition;
	private IScope fTemplateDefinitionScope;
	
	CPPDependentEvaluation(IBinding templateDefinition) {
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
	
	protected IScope getTemplateDefinitionScope() {
		if (fTemplateDefinitionScope == null) {
			IBinding templateDefinition = getTemplateDefinition();
			if (templateDefinition != null) {
				if (templateDefinition instanceof ICPPClassType) {
					fTemplateDefinitionScope = ((ICPPClassType) templateDefinition).getCompositeScope();
				}
				try {
					fTemplateDefinitionScope = templateDefinition.getScope();
				} catch (DOMException e) {
				}		
			}
		}
		return fTemplateDefinitionScope;
	}
	
	/**
	 * If the given node is contained in some template declaration,
	 * return the binding for that template. Otherwise return null. 
	 */
	protected static IBinding findEnclosingTemplate(IASTNode node) {
		while (node != null) {
			if (node instanceof ICPPASTTemplateDeclaration) {
				ICPPASTTemplateDeclaration templateDecl = (ICPPASTTemplateDeclaration) node;
				IASTName templateName = CPPTemplates.getTemplateName(templateDecl);
				if (templateName == null)
					return null;
				return new DeferredResolutionBinding(templateName);
			}
			node = node.getParent();
		}
		return null;
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
	
	/**
	 * Instantiate evaluations that represent subexpressions separated by commas.
	 * If a subexpression is a pack expansion expression, and the template parameter map
	 * contains a mapping for the parameter pack(s) that occur in its expansion pattern,
	 * the expansion pattern is instantiated once for each mapped template argument,
	 * and the resulting evaluations are returned in place of the pack expansion.  
	 * 
	 * This code is similar to CPPTemplates.instantiateArguments(), but applies to evaluations
	 * rather than template arguments. 
	 */
	protected static ICPPEvaluation[] instantiateCommaSeparatedSubexpressions(
			ICPPEvaluation[] subexpressions, ICPPTemplateParameterMap tpMap, int packOffset,
			ICPPClassSpecialization within, int maxdepth, IASTNode point) {
		ICPPEvaluation[] result = subexpressions;
		int resultShift = 0;
		for (int i = 0; i < subexpressions.length; i++) {
			ICPPEvaluation origEval = subexpressions[i];
			ICPPEvaluation newEval;
			if (origEval instanceof EvalParameterPack) {
				origEval = ((EvalParameterPack) origEval).getExpansionPattern();
				if (origEval == null) {
					newEval = EvalFixed.INCOMPLETE;
				} else {
					int packSize = origEval.determinePackSize(tpMap);
					if (packSize == CPPTemplates.PACK_SIZE_FAIL || packSize == CPPTemplates.PACK_SIZE_NOT_FOUND) {
						newEval = EvalFixed.INCOMPLETE;
					} else if (packSize == CPPTemplates.PACK_SIZE_DEFER) {
						newEval = origEval;
					} else {
						int shift = packSize - 1;
						ICPPEvaluation[] newResult = new ICPPEvaluation[subexpressions.length + resultShift + shift];
						System.arraycopy(result, 0, newResult, 0, i + resultShift);
						for (int j = 0; j < packSize; ++j) {
							newEval = origEval.instantiate(tpMap, j, within, maxdepth, point);
							newResult[i + resultShift + j] = newEval;
						}
						result = newResult;
						resultShift += shift;
						continue;
					}
				}
			} else {
				newEval = origEval.instantiate(tpMap, packOffset, within, maxdepth, point);
			}
			
			if (result != subexpressions)
				result[i + resultShift] = newEval;
			else if (newEval != origEval) {
				assert resultShift == 0;
				result = new ICPPEvaluation[subexpressions.length];
				System.arraycopy(subexpressions, 0, result, 0, i);
				result[i] = newEval;
			}
		}
		return result;
	}
	
	/**
	 * Used to defer the resolution of a template definition until it is needed, 
	 * to avoid recursion. The only valid operation on this binding is resolve().
	 */
	private static class DeferredResolutionBinding extends PlatformObject implements IBinding {
		private final IASTName fName;
		
		public DeferredResolutionBinding(IASTName name) {
			fName = name;
		}
		
		public IBinding resolve() {
			return fName.resolveBinding();
		}
		
		@Override
		public String getName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public char[] getNameCharArray() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ILinkage getLinkage() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IBinding getOwner() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IScope getScope() throws DOMException {
			throw new UnsupportedOperationException();
		}
	}
}