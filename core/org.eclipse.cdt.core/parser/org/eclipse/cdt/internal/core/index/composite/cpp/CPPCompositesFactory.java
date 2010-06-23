/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecializationSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexMacroContainer;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArrayType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameterPackType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPClassSpecializationScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassType;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.composite.AbstractCompositeFactory;
import org.eclipse.cdt.internal.core.index.composite.CompositeMacroContainer;
import org.eclipse.cdt.internal.core.index.composite.CompositingNotImplementedError;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;
import org.eclipse.core.runtime.CoreException;

public class CPPCompositesFactory extends AbstractCompositeFactory {

	public CPPCompositesFactory(IIndex index) {
		super(index);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.composite.cpp.ICompositesFactory#getCompositeScope(org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.core.dom.ast.IScope)
	 */
	public IIndexScope getCompositeScope(IIndexScope rscope) {
		try {
			if (rscope == null) {
				return null;
			} 
			if (rscope instanceof ICPPClassScope) {
				if (rscope instanceof ICPPClassSpecializationScope) {
					return new CompositeCPPClassSpecializationScope(this, (IIndexFragmentBinding) rscope.getScopeBinding());
				}
				ICPPClassScope classScope = (ICPPClassScope) rscope;
				return new CompositeCPPClassScope(this,	findOneBinding(classScope.getClassType()));
			} 
			if (rscope instanceof ICPPNamespaceScope) {
				ICPPNamespace[] namespaces;
				if (rscope instanceof CompositeCPPNamespace) {
					// avoid duplicating the search
					namespaces = ((CompositeCPPNamespace)rscope).namespaces;
				} else {
					namespaces = getNamespaces(rscope.getScopeBinding());
				}
				return new CompositeCPPNamespaceScope(this, namespaces);
			} 
			if (rscope instanceof ICPPInternalUnknownScope) {
				ICPPInternalUnknownScope uscope= (ICPPInternalUnknownScope) rscope;
				final ICPPBinding binding = uscope.getScopeBinding();
				return new CompositeCPPUnknownScope((CompositeCPPBinding) getCompositeBinding((IIndexFragmentBinding) binding), (IASTName) uscope.getPhysicalNode());
			}
			throw new CompositingNotImplementedError(rscope.getClass().getName());
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
			throw new CompositingNotImplementedError(ce.getMessage());		
		} 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.composite.cpp.ICompositesFactory#getCompositeType(org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.core.dom.ast.IType)
	 */
	public IType getCompositeType(IType rtype) {
		if (rtype instanceof IIndexFragmentBinding) {
			return (IType) getCompositeBinding((IIndexFragmentBinding) rtype);
		} 
		if (rtype instanceof ICPPFunctionType) {
			ICPPFunctionType ft= (ICPPFunctionType) rtype;
			IType r= ft.getReturnType();
			IType r2= getCompositeType(r);
			IType[] p= ft.getParameterTypes();
			IType[] p2= getCompositeTypes(p);
			if (r != r2 || p != p2) {
				return new CPPFunctionType(r2, p2, ft.isConst(), ft.isVolatile(), ft.takesVarArgs());
			}
			return ft;
		} 
		if (rtype instanceof ICPPPointerToMemberType) {
			ICPPPointerToMemberType pmt= (ICPPPointerToMemberType) rtype;
			IType ct= pmt.getMemberOfClass();
			IType ct2= getCompositeType(ct);
			IType t= pmt.getType();
			IType t2= getCompositeType(t);
			if (ct != ct2 || t != t2) {
				return new CPPPointerToMemberType(t2, ct2, pmt.isConst(), pmt.isVolatile());
			}
			return pmt;
		} 
		if (rtype instanceof IPointerType) {
			IPointerType pt= (IPointerType) rtype;
			IType r= pt.getType();
			IType r2= getCompositeType(r);
			if (r != r2) {
				return new CPPPointerType(r2, pt.isConst(), pt.isVolatile());
			}
			return pt;
		}
		if (rtype instanceof ICPPReferenceType) {
			ICPPReferenceType rt= (ICPPReferenceType) rtype;
			IType r= rt.getType();
			IType r2= getCompositeType(r);
			if (r != r2) {
				return new CPPReferenceType(r2, rt.isRValueReference());
			}
			return rt;
		}
		if (rtype instanceof ICPPParameterPackType) {
			ICPPParameterPackType rt= (ICPPParameterPackType) rtype;
			IType r= rt.getType();
			IType r2= getCompositeType(r);
			if (r != r2 && r2 != null) {
				return new CPPParameterPackType(r2);
			}
			return rt;
		}
		if (rtype instanceof IQualifierType) {
			IQualifierType qt= (IQualifierType) rtype;
			IType r= qt.getType();
			IType r2= getCompositeType(r);
			if (r != r2) {
				return new CPPQualifierType(r2, qt.isConst(), qt.isVolatile());
			}
			return qt;
		} 
		if (rtype instanceof IArrayType) {
			IArrayType at= (IArrayType) rtype;
			IType r= at.getType();
			IType r2= getCompositeType(r);
			IValue v= at.getSize();
			IValue v2= getCompositeValue(v);
			if (r != r2 || v != v2) {
				return new CPPArrayType(r2, v2);
			}
			return at;
		} 
		if (rtype instanceof IBasicType || rtype == null || rtype instanceof IProblemBinding) {
			return rtype;
		} 
		
		throw new CompositingNotImplementedError();
	}
	
	public IValue getCompositeValue(IValue v) {
		if (v == null)
			return null;
		
		IBinding[] b= v.getUnknownBindings();
		if (b.length == 0)
			return v;
		
		ICPPUnknownBinding[] b2= new ICPPUnknownBinding[b.length];
		for (int i = 0; i < b2.length; i++) {
			b2[i]= (ICPPUnknownBinding) getCompositeBinding((IIndexFragmentBinding) b[i]);
		}
		return Value.fromInternalRepresentation(v.getInternalExpression(), b2);
	}

	private ICPPNamespace[] getNamespaces(IBinding rbinding) throws CoreException {
		CIndex cindex = (CIndex) index;
		IIndexBinding[] ibs = cindex.findEquivalentBindings(rbinding);
		ICPPNamespace[] namespaces = new ICPPNamespace[ibs.length];
		for (int i = 0; i < namespaces.length; i++)
			namespaces[i] = (ICPPNamespace) ibs[i];
		return namespaces;
	}
	
	IIndex getContext() {
		return index;
	}
	
	protected IIndexFragmentBinding findOneBinding(IBinding binding) {
		return super.findOneBinding(binding, false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.composite.cpp.ICompositesFactory#getCompositeBinding(org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public IIndexBinding getCompositeBinding(IIndexFragmentBinding binding) {
		IIndexBinding result;

		try {
			if (binding == null) {
				result = null;
			} else if (binding instanceof ICPPSpecialization) {
				if (binding instanceof ICPPTemplateInstance) {
					if (binding instanceof ICPPDeferredClassInstance) {
						return new CompositeCPPDeferredClassInstance(this, (ICPPDeferredClassInstance) findOneBinding(binding));
					} else {
						if (binding instanceof ICPPClassType) {
							return new CompositeCPPClassInstance(this, (ICPPClassType) findOneBinding(binding));
						} else if (binding instanceof ICPPConstructor) {
							return new CompositeCPPConstructorInstance(this, (ICPPConstructor) binding);
						} else if (binding instanceof ICPPMethod) {
							return new CompositeCPPMethodInstance(this, (ICPPMethod) binding);
						} else if (binding instanceof ICPPFunction) {
							return new CompositeCPPFunctionInstance(this, (ICPPFunction) binding);
						} else {
							throw new CompositingNotImplementedError("composite binding unavailable for "+binding+" "+binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				} else if (binding instanceof ICPPTemplateDefinition) {
					if (binding instanceof ICPPClassTemplatePartialSpecialization) {
						if (binding instanceof ICPPClassTemplatePartialSpecializationSpecialization)
							return new CompositeCPPClassTemplatePartialSpecializationSpecialization(this, (ICPPClassTemplatePartialSpecializationSpecialization) binding);
						return new CompositeCPPClassTemplatePartialSpecialization(this, (ICPPClassTemplatePartialSpecialization) findOneBinding(binding));
					} else if (binding instanceof ICPPClassType) {
						return new CompositeCPPClassTemplateSpecialization(this, (ICPPClassType) binding);
					} else if (binding instanceof ICPPConstructor) {
						return new CompositeCPPConstructorTemplateSpecialization(this, (ICPPConstructor) binding);
					} else if (binding instanceof ICPPMethod) {
						return new CompositeCPPMethodTemplateSpecialization(this, (ICPPMethod) binding);
					} else if (binding instanceof ICPPFunctionType) {
						return new CompositeCPPFunctionTemplateSpecialization(this, (ICPPFunction) binding);
					} else {
						throw new CompositingNotImplementedError("composite binding unavailable for "+binding+" "+binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else {
					if (binding instanceof ICPPClassType) {
						return new CompositeCPPClassSpecialization(this, (ICPPClassType) findOneBinding(binding));
					} else if (binding instanceof ICPPConstructor) {
						return new CompositeCPPConstructorSpecialization(this, (ICPPConstructor) findOneBinding(binding, true));						
					} else if (binding instanceof ICPPMethod) {
						return new CompositeCPPMethodSpecialization(this, (ICPPMethod) findOneBinding(binding, true));
					} else if (binding instanceof ICPPFunction) {
						return new CompositeCPPFunctionSpecialization(this, (ICPPFunction) findOneBinding(binding, true));
					} else if (binding instanceof ICPPField) {
						return new CompositeCPPField(this, (ICPPField) binding);
					} else if (binding instanceof ICPPParameter) {
						return new CompositeCPPParameterSpecialization(this, (ICPPParameter) binding);
					} else if (binding instanceof ITypedef) {
						return new CompositeCPPTypedefSpecialization(this, (ICPPBinding) binding);
					} else {
						throw new CompositingNotImplementedError("composite binding unavailable for "+binding+" "+binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			} else if(binding instanceof ICPPTemplateParameter) {
				if (binding instanceof ICPPTemplateTypeParameter) {
					result = new CompositeCPPTemplateTypeParameter(this, (ICPPTemplateTypeParameter) binding);
				} else if (binding instanceof ICPPTemplateNonTypeParameter) {
					result = new CompositeCPPTemplateNonTypeParameter(this, (ICPPTemplateNonTypeParameter) binding);
				} else if (binding instanceof ICPPTemplateTemplateParameter) {
					result = new CompositeCPPTemplateTemplateParameter(this, (ICPPTemplateTemplateParameter) binding);
				} else {
					throw new CompositingNotImplementedError("composite binding unavailable for "+binding+" "+binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else if (binding instanceof ICPPTemplateDefinition) {
				if (binding instanceof ICPPClassTemplate) {
					ICPPClassType def= (ICPPClassType) findOneBinding(binding);
					return new CompositeCPPClassTemplate(this, def);
				} else if (binding instanceof ICPPConstructor) {
					return new CompositeCPPConstructorTemplate(this, (ICPPConstructor) binding);
				} else if (binding instanceof ICPPMethod) {
					return new CompositeCPPMethodTemplate(this, (ICPPMethod) binding);
				} else if (binding instanceof ICPPFunctionTemplate) {
					return new CompositeCPPFunctionTemplate(this, (ICPPFunction) binding);
				} else {
					throw new CompositingNotImplementedError("composite binding unavailable for "+binding+" "+binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else if (binding instanceof ICPPParameter) {
				result = new CompositeCPPParameter(this, (ICPPParameter) binding);
			} else if (binding instanceof ICPPField) {
				result = new CompositeCPPField(this, (ICPPField) binding);
			} else if (binding instanceof ICPPVariable) {
				result = new CompositeCPPVariable(this, (ICPPVariable) binding);
			} else if (binding instanceof ICPPUnknownBinding) {
				if (binding instanceof ICPPUnknownClassInstance) {
					result = new CompositeCPPUnknownClassInstance(this, (ICPPUnknownClassInstance) binding);
				} else if (binding instanceof ICPPUnknownClassType) {
					result = new CompositeCPPUnknownClassType(this, (ICPPUnknownClassType) binding);
				} else {
					result= new CompositeCPPUnknownBinding(this, (ICPPUnknownBinding) binding);
				}
			} else if (binding instanceof ICPPClassType) {
				ICPPClassType def = (ICPPClassType) findOneBinding(binding);
				result = def == null ? null : new CompositeCPPClassType(this, def);
			} else if (binding instanceof ICPPConstructor) {
				result = new CompositeCPPConstructor(this, (ICPPConstructor) binding);
			} else if (binding instanceof ICPPMethod) {
				result = new CompositeCPPMethod(this, (ICPPMethod) binding);
			} else if (binding instanceof ICPPNamespaceAlias) {
				result = new CompositeCPPNamespaceAlias(this, (ICPPNamespaceAlias) binding);
			} else if (binding instanceof ICPPNamespace) {
				ICPPNamespace[] ns = getNamespaces(binding);
				result = ns.length == 0 ? null : new CompositeCPPNamespace(this, ns);
			} else if (binding instanceof ICPPUsingDeclaration) {
				result = new CompositeCPPUsingDeclaration(this, (ICPPUsingDeclaration) binding);
			} else if (binding instanceof ICPPEnumeration) {
				ICPPEnumeration def = (ICPPEnumeration) findOneBinding(binding);
				result = def == null ? null : new CompositeCPPEnumeration(this, def);
			} else if (binding instanceof ICPPFunction) {
				result = new CompositeCPPFunction(this, (ICPPFunction) binding);				
			} else if (binding instanceof IEnumerator) {
				result = new CompositeCPPEnumerator(this, (IEnumerator) binding);
			} else if (binding instanceof ITypedef) {
				result = new CompositeCPPTypedef(this, (ICPPBinding) binding);
			} else if (binding instanceof IIndexMacroContainer) {
				result= new CompositeMacroContainer(this, binding);
			} else {
				throw new CompositingNotImplementedError("composite binding unavailable for "+binding+" "+binding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
			throw new CompositingNotImplementedError(ce.getMessage());			
		}

		return result;
	}

	private static class Key {
		final long i;
		final int j;
		final long k;
		public Key(long id1, int id2, long id3) {
			i= id1;
			j= id2;
			k= id3;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (i ^ (i >>> 32));
			result = prime * result + j;
			result = prime * result + (int)k;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Key) {
				Key other = (Key) obj;
				return i == other.i && j == other.j && k == other.k;
			}
			return false;
		}
	}
	
	public static Object createInstanceCacheKey(ICompositesFactory cf,IIndexFragmentBinding rbinding) {
		return new Key(Thread.currentThread().getId(), cf.hashCode(), rbinding.getBindingID());
	}
	public static Object createSpecializationKey(ICompositesFactory cf,IIndexFragmentBinding rbinding) {
		return new Key(Thread.currentThread().getId(), cf.hashCode(), rbinding.getBindingID()+1);
	}
}
