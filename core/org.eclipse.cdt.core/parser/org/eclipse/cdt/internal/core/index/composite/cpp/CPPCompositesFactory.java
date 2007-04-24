/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.AbstractCompositeFactory;
import org.eclipse.cdt.internal.core.index.composite.CompositeArrayType;
import org.eclipse.cdt.internal.core.index.composite.CompositePointerType;
import org.eclipse.cdt.internal.core.index.composite.CompositeQualifierType;
import org.eclipse.cdt.internal.core.index.composite.CompositingNotImplementedError;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;
import org.eclipse.core.runtime.CoreException;

public class CPPCompositesFactory extends AbstractCompositeFactory implements ICompositesFactory {

	public CPPCompositesFactory(IIndex index) {
		super(index);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.composite.cpp.ICompositesFactory#getCompositeScope(org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.core.dom.ast.IScope)
	 */
	public IScope getCompositeScope(IIndexScope rscope) throws DOMException {
		IScope result;

		try {
			if(rscope == null) {
				return null;
			} else if(rscope instanceof ICPPClassScope) {
				ICPPClassScope classScope = (ICPPClassScope) rscope;
				result = new CompositeCPPClassScope(this,
						findOneDefinition(classScope.getClassType()));
			} else if(rscope instanceof ICPPNamespaceScope) {
				ICPPNamespace[] namespaces;
				if(rscope instanceof CompositeCPPNamespace) {
					// avoid duplicating the search
					namespaces = ((CompositeCPPNamespace)rscope).namespaces;
				} else {
					namespaces = getNamespaces(rscope.getScopeBinding());
				}
				return new CompositeCPPNamespaceScope(this, namespaces);
			} else if(rscope instanceof ICPPTemplateScope) {
				return new CompositeCPPTemplateScope(this, (ICPPTemplateScope) rscope);
			} else {
				throw new CompositingNotImplementedError(rscope.getClass().getName());
			}
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
			throw new CompositingNotImplementedError(ce.getMessage());		
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.composite.cpp.ICompositesFactory#getCompositeType(org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.core.dom.ast.IType)
	 */
	public IType getCompositeType(IIndexType rtype) throws DOMException {
		IType result;

		if(rtype instanceof ICPPSpecialization) {
			result = (IIndexType) getCompositeBinding((IIndexFragmentBinding) rtype);
		} else if(rtype instanceof ICPPClassType) {
			result = (ICPPClassType) getCompositeBinding((IIndexFragmentBinding) rtype);
		} else if(rtype instanceof ITypedef) {
			result = new CompositeCPPTypedef(this, (ICPPBinding) rtype);
		} else if (rtype instanceof IEnumeration) {
			result = (IEnumeration) getCompositeBinding((IIndexFragmentBinding) rtype);
		} else if(rtype instanceof ICPPFunctionType) {
			result = new CompositeCPPFunctionType((ICPPFunctionType) rtype, this); 
		} else if(rtype instanceof ICPPPointerToMemberType) {
			result = new CompositeCPPPointerToMemberType(this, (ICPPPointerToMemberType)rtype);
		} else if(rtype instanceof IPointerType) {
			result = new CompositePointerType((IPointerType)rtype, this);
		} else if(rtype instanceof ICPPReferenceType) {
			result = new CompositeCPPReferenceType((ICPPReferenceType)rtype, this);			
		} else if(rtype instanceof IQualifierType) {
			result = new CompositeQualifierType((IQualifierType) rtype, this);
		} else if(rtype instanceof IArrayType) {
			result = new CompositeArrayType((IArrayType) rtype, this);
		} else if(rtype == null) {
			result = null;
		} else if(rtype instanceof ICPPTemplateTypeParameter) {
			result = (IIndexType) getCompositeBinding((IIndexFragmentBinding) rtype);
		} else if(rtype instanceof IBasicType) {
			result = rtype; // no context required its a leaf with no way to traverse upward
		} else {
			throw new CompositingNotImplementedError();
		}

		return result;
	}


	private ICPPNamespace[] getNamespaces(IBinding rbinding) throws CoreException {
		CIndex cindex = (CIndex) index;
		IIndexBinding[] ibs = cindex.findEquivalentBindings(rbinding);
		ICPPNamespace[] namespaces = new ICPPNamespace[ibs.length];
		for(int i=0; i<namespaces.length; i++)
			namespaces[i] = (ICPPNamespace) ibs[i];
		return namespaces;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.composite.cpp.ICompositesFactory#getCompositeBinding(org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public IIndexBinding getCompositeBinding(IIndexFragmentBinding binding) {
		IIndexBinding result;

		try {
			if(binding==null) {
				result = null;
			} else if (binding instanceof ICPPTemplateDefinition) {
				if(binding instanceof ICPPClassTemplate) {
					return new CompositeCPPClassTemplate(this, (ICPPClassType) findOneDefinition(binding));
				} else if (binding instanceof ICPPFunctionTemplate) {
					return new CompositeCPPFunctionTemplate(this, (ICPPFunction) binding);
				} else {
					throw new CompositingNotImplementedError("composite binding unavailable for "+binding+" "+binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else if(binding instanceof ICPPSpecialization) {
				if(binding instanceof ICPPTemplateInstance) {
					if(binding instanceof ICPPDeferredTemplateInstance) {
						if(binding instanceof ICPPClassType) {
							return new CompositeCPPDeferredClassInstance(this, (ICPPClassType) findOneDefinition(binding));
						} else if(binding instanceof ICPPFunction) {
							return new CompositeCPPDeferredFunctionInstance(this, (ICPPFunction) binding);
						} else {
							throw new CompositingNotImplementedError("composite binding unavailable for "+binding+" "+binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
						}
					} else {
						if(binding instanceof ICPPClassType) {
							return new CompositeCPPClassInstance(this, (ICPPClassType) findOneDefinition(binding));
						} if(binding instanceof ICPPFunction) {
							return new CompositeCPPFunctionInstance(this, (ICPPFunction) binding);
						} else {
							throw new CompositingNotImplementedError("composite binding unavailable for "+binding+" "+binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				} else {
					if(binding instanceof ICPPClassType) {
						if(binding instanceof ICPPClassTemplatePartialSpecialization) {
							return new CompositeCPPClassTemplatePartialSpecialization(this, (ICPPClassTemplatePartialSpecialization) binding);
						} else {
							return new CompositeCPPClassSpecialization(this, (ICPPClassType) findOneDefinition(binding));
						}
					} if(binding instanceof ICPPConstructor) {
						return new CompositeCPPConstructorSpecialization(this, (ICPPConstructor) binding);						
					} if(binding instanceof ICPPMethod) {
						return new CompositeCPPMethodSpecialization(this, (ICPPMethod) binding);
					} if(binding instanceof ICPPFunction) {
						return new CompositeCPPFunctionSpecialization(this, (ICPPFunction) binding);
					} if(binding instanceof ICPPField) {
						return new CompositeCPPField(this, (ICPPField) binding);
					} if(binding instanceof ICPPParameter) {
						return new CompositeCPPParameterSpecialization(this, (ICPPParameter) binding);
					} else {
						throw new CompositingNotImplementedError("composite binding unavailable for "+binding+" "+binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			} else if(binding instanceof ICPPParameter) {
				result = new CompositeCPPParameter(this, (ICPPParameter) binding);
			} else if(binding instanceof ICPPField) {
				result = new CompositeCPPField(this, (ICPPField) binding);
			} else if(binding instanceof ICPPVariable) {
				result = new CompositeCPPVariable(this, (ICPPVariable) binding);
			} else if(binding instanceof ICPPClassType) {
				ICPPClassType def = (ICPPClassType) findOneDefinition(binding);
				result = def == null ? null : new CompositeCPPClassType(this, def);
			} else if(binding instanceof ICPPConstructor) {
				result = new CompositeCPPConstructor(this, (ICPPConstructor) binding);
			} else if(binding instanceof ICPPMethod) {
				result = new CompositeCPPMethod(this, (ICPPMethod) binding);
			} else if(binding instanceof ICPPNamespaceAlias) {
				result = new CompositeCPPNamespaceAlias(this, (ICPPNamespaceAlias) binding);
			} else if(binding instanceof ICPPNamespace) {
				ICPPNamespace[] ns = getNamespaces(binding);
				result = ns.length == 0 ? null : new CompositeCPPNamespace(this, ns);
			} else if(binding instanceof IEnumeration) {
				IEnumeration def = (IEnumeration) findOneDefinition(binding);
				result = def == null ? null : new CompositeCPPEnumeration(this, def);
			} else if(binding instanceof ICPPFunction) {
				result = new CompositeCPPFunction(this, (ICPPFunction) binding);				
			} else if(binding instanceof IEnumerator) {
				result = new CompositeCPPEnumerator(this, (IEnumerator) binding);
			} else if(binding instanceof ITypedef) {
				result = new CompositeCPPTypedef(this, (ICPPBinding) binding);
			} else if(binding instanceof ICPPTemplateTypeParameter) {
				result = new CompositeCPPTemplateTypeParameter(this, (ICPPTemplateTypeParameter) binding);
			} else {
				throw new CompositingNotImplementedError("composite binding unavailable for "+binding+" "+binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
			throw new CompositingNotImplementedError(ce.getMessage());			
		}

		return result;
	}
}
