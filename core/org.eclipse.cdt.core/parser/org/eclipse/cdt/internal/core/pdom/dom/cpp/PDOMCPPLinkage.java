/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBlockScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalFunction;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMASTAdapter;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCPPLinkage extends PDOMLinkage implements IIndexCPPBindingConstants {
	public PDOMCPPLinkage(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMCPPLinkage(PDOM pdom) throws CoreException {
		super(pdom, CPP_LINKAGE_ID, CPP_LINKAGE_ID.toCharArray());
	}

	public String getID() {
		return CPP_LINKAGE_ID;
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return LINKAGE;
	}

	// Binding types

	
	private class ConfigureTemplate implements Runnable {
		ICPPTemplateDefinition template;
		
		public ConfigureTemplate(ICPPTemplateDefinition template) {
			this.template = template;
		}
		
		public void run() {
			try {
				ICPPTemplateParameter[] params = template.getTemplateParameters();
				for (int i = 0; i < params.length; i++) {
					if (params[i] != null && !(params[i] instanceof ProblemBinding)) {
						addBinding(params[i]);
					}
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			} catch (DOMException e) {
				CCorePlugin.log(e);
			} finally {
				template = null;
			}
		}
	}
	
	private class ConfigurePartialSpecialization implements Runnable {
		PDOMCPPClassTemplatePartialSpecialization partial;
		ICPPClassTemplatePartialSpecialization binding;
		
		public ConfigurePartialSpecialization(PDOMCPPClassTemplatePartialSpecialization partial, ICPPClassTemplatePartialSpecialization binding) {
			this.partial = partial;
			this.binding = binding;
		}
		
		public void run() {
			try {
				IType[] args = binding.getArguments();
				for (int i = 0; i < args.length; i++) {
					partial.addArgument(args[i]);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			} catch (DOMException e) {
				CCorePlugin.log(e);
			} finally {
				partial = null;
				binding = null;
			}
		}
	}
	
	private class ConfigureFunctionTemplate implements Runnable {
		PDOMCPPFunctionTemplate template;
		ICPPFunction function;
		
		public ConfigureFunctionTemplate(PDOMCPPFunctionTemplate template, ICPPFunction binding) {
			this.template = template;
			this.function = binding;
		}
		
		public void run() {
			try {
				template.initData(function);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			} catch (DOMException e) {
				CCorePlugin.log(e);
			} finally {
				template = null;
				function = null;
			}
		}
	}
	
	List postProcesses = new ArrayList();
	
	public PDOMBinding addBinding(IASTName name) throws CoreException {
		if (name == null || name instanceof ICPPASTQualifiedName)
			return null;

		// Check for null name
		char[] namechars = name.toCharArray();
		if (namechars == null)
			return null;
		
		IBinding binding = name.resolveBinding();

		if (binding == null || binding instanceof IProblemBinding) {
			// Can't tell what it is
			return null;
		}

		if (binding instanceof IParameter)
			// Skip parameters (TODO and others I'm sure)
			return null;

		PDOMBinding pdomBinding = addBinding(binding);
		if (pdomBinding instanceof PDOMCPPClassType || pdomBinding instanceof PDOMCPPClassSpecialization) {
			if (binding instanceof ICPPClassType && name.isDefinition()) {
				addImplicitMethods(pdomBinding, (ICPPClassType) binding);
			}
		}
		
		handlePostProcesses();
		
		return pdomBinding;
	}

	public PDOMBinding addBinding(IBinding binding) throws CoreException {
		// assign names to anonymous types.
		binding= PDOMASTAdapter.getAdapterIfAnonymous(binding);
		if (binding == null) {
			return null;
		}

		PDOMBinding pdomBinding = adaptBinding(binding);
		try {
			if (pdomBinding == null) {
				boolean addParent = shouldAddParent(binding);
				PDOMNode parent = getAdaptedParent(binding, true, addParent);
				if (parent == null)
					return null;
				pdomBinding = addBinding(parent, binding);
			}
		} catch(DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}

		return pdomBinding;
	}

	private boolean shouldAddParent(IBinding binding) throws CoreException {
		if (binding instanceof ICPPTemplateParameter) {
			return true;
		} else if (binding instanceof ICPPSpecialization) {
			return true;
		}
		return false;
	}
	
	private PDOMBinding addBinding(PDOMNode parent, IBinding binding) throws CoreException, DOMException {
		PDOMBinding pdomBinding= null;
		
		if (binding instanceof ICPPSpecialization) {
			IBinding specialized = ((ICPPSpecialization)binding).getSpecializedBinding();
			if (specialized == null || specialized instanceof ProblemBinding) return null;
			PDOMBinding pdomSpecialized = addBinding(specialized);
			if (pdomSpecialized == null) return null;
			
			if (binding instanceof ICPPDeferredTemplateInstance) {
				if (binding instanceof ICPPFunction && pdomSpecialized instanceof ICPPFunctionTemplate) {
					pdomBinding = new PDOMCPPDeferredFunctionInstance(pdom,
							parent, (ICPPFunction) binding, pdomSpecialized);	
				} else if (binding instanceof ICPPClassType && pdomSpecialized instanceof ICPPClassTemplate) {
					pdomBinding = new PDOMCPPDeferredClassInstance(pdom,
							parent, (ICPPClassType) binding, pdomSpecialized);
				}
			} else if (binding instanceof ICPPTemplateInstance) {
				if (binding instanceof ICPPConstructor && pdomSpecialized instanceof ICPPConstructor) {
					pdomBinding = new PDOMCPPConstructorInstance(pdom, parent,
							(ICPPConstructor) binding, pdomSpecialized);
				} else if (binding instanceof ICPPMethod && pdomSpecialized instanceof ICPPMethod) {
					pdomBinding = new PDOMCPPMethodInstance(pdom, parent,
							(ICPPMethod) binding, pdomSpecialized);
				} else if (binding instanceof ICPPFunction && pdomSpecialized instanceof ICPPFunction) {
					pdomBinding = new PDOMCPPFunctionInstance(pdom, parent,
							(ICPPFunction) binding, pdomSpecialized);
				} else if (binding instanceof ICPPClassType && pdomSpecialized instanceof ICPPClassType) {
					pdomBinding = new PDOMCPPClassInstance(pdom, parent,
							(ICPPClassType) binding, pdomSpecialized);
				}
			} else if (binding instanceof ICPPClassTemplatePartialSpecialization && pdomSpecialized instanceof PDOMCPPClassTemplate) {
				pdomBinding = new PDOMCPPClassTemplatePartialSpecialization(
						pdom, parent, (ICPPClassTemplatePartialSpecialization) binding,
						(PDOMCPPClassTemplate) pdomSpecialized);
			} else if (binding instanceof ICPPField) {
				pdomBinding = new PDOMCPPFieldSpecialization(pdom, parent,
						(ICPPField) binding, pdomSpecialized);
			} else if (binding instanceof ICPPFunctionTemplate) {
				if (binding instanceof ICPPConstructor) {
					pdomBinding = new PDOMCPPConstructorTemplateSpecialization(
							pdom, parent, (ICPPConstructor) binding, pdomSpecialized);
				} else if (binding instanceof ICPPMethod) {
					pdomBinding = new PDOMCPPMethodTemplateSpecialization(
							pdom, parent, (ICPPMethod) binding, pdomSpecialized);
				} else if (binding instanceof ICPPFunction) {
					pdomBinding = new PDOMCPPFunctionTemplateSpecialization(
							pdom, parent, (ICPPFunctionTemplate) binding, pdomSpecialized);
				}
			} else if (binding instanceof ICPPConstructor) {
				pdomBinding = new PDOMCPPConstructorSpecialization(pdom, parent,
						(ICPPConstructor) binding, pdomSpecialized);
			} else if (binding instanceof ICPPMethod) {
				pdomBinding = new PDOMCPPMethodSpecialization(pdom, parent,
						(ICPPMethod) binding, pdomSpecialized);
			} else if (binding instanceof ICPPFunction) {
				pdomBinding = new PDOMCPPFunctionSpecialization(pdom, parent,
						(ICPPFunction) binding, pdomSpecialized);
			} else if (binding instanceof ICPPClassTemplate) {
				pdomBinding = new PDOMCPPClassTemplateSpecialization(pdom, parent,
						(ICPPClassTemplate) binding, pdomSpecialized);
			} else if (binding instanceof ICPPClassType) {
				pdomBinding = new PDOMCPPClassSpecialization(pdom, parent,
						(ICPPClassType) binding, pdomSpecialized);
			} else if (binding instanceof ITypedef) {
				pdomBinding = new PDOMCPPTypedefSpecialization(pdom, parent,
						(ITypedef) binding, pdomSpecialized);
			}
		} else if (binding instanceof ICPPTemplateParameter) {
			if (binding instanceof ICPPTemplateTypeParameter) {
				pdomBinding = new PDOMCPPTemplateTypeParameter(pdom, parent, (ICPPTemplateTypeParameter)binding);
			} 
// TODO other template parameter types
//			else if (binding instanceof ICPPTemplateTemplateParameter) {
//				pdomBinding = new PDOMCPPTemplateTemplateParameter(pdom, parent, (ICPPTemplateTemplateParameter)binding);
//			} else if (binding instanceof ICPPTemplateNonTypeParameter) {
//				pdomBinding = new PDOMCPPTemplateNonTypeParameter(pdom, parent, (ICPPTemplateNonTypeParameter)binding);
//			}
		} else if (binding instanceof ICPPField ) {
			if (parent instanceof PDOMCPPClassType || parent instanceof PDOMCPPClassSpecialization) {
				pdomBinding = new PDOMCPPField(pdom, parent, (ICPPField) binding);
			}
		} else if (binding instanceof ICPPVariable) {
			if (!(binding.getScope() instanceof CPPBlockScope)) {
				ICPPVariable var= (ICPPVariable) binding;
				pdomBinding = new PDOMCPPVariable(pdom, parent, var);
			}
		} else if (binding instanceof ICPPFunctionTemplate) {
			if (binding instanceof ICPPConstructor) {
				pdomBinding= new PDOMCPPConstructorTemplate(pdom, parent, (ICPPConstructor) binding);
			} else if (binding instanceof ICPPMethod) {
				pdomBinding= new PDOMCPPMethodTemplate(pdom, parent, (ICPPMethod) binding);
			} else if (binding instanceof ICPPFunction) {
				pdomBinding= new PDOMCPPFunctionTemplate(pdom, parent, (ICPPFunctionTemplate) binding);
			}
		} else if (binding instanceof ICPPConstructor) {
			if (parent instanceof PDOMCPPClassType || parent instanceof PDOMCPPClassSpecialization) {
				pdomBinding = new PDOMCPPConstructor(pdom, parent, (ICPPConstructor)binding);
			}
		} else if (binding instanceof ICPPMethod) {
			if (parent instanceof PDOMCPPClassType || parent instanceof PDOMCPPClassSpecialization) {
				pdomBinding = new PDOMCPPMethod(pdom, parent, (ICPPMethod)binding);
			}
		} else if (binding instanceof ICPPFunction) {
			pdomBinding = new PDOMCPPFunction(pdom, parent, (ICPPFunction) binding, true);
		} else if (binding instanceof ICPPClassTemplate) {
			pdomBinding= new PDOMCPPClassTemplate(pdom, parent, (ICPPClassTemplate) binding);
		} else if (binding instanceof ICPPClassType) {
			pdomBinding= new PDOMCPPClassType(pdom, parent, (ICPPClassType) binding);
		} else if (binding instanceof ICPPNamespaceAlias) {
			pdomBinding = new PDOMCPPNamespaceAlias(pdom, parent, (ICPPNamespaceAlias) binding);
		} else if (binding instanceof ICPPNamespace) {
			pdomBinding = new PDOMCPPNamespace(pdom, parent, (ICPPNamespace) binding);
		} else if (binding instanceof IEnumeration) {
			pdomBinding = new PDOMCPPEnumeration(pdom, parent, (IEnumeration) binding);
		} else if (binding instanceof IEnumerator) {
			IEnumeration enumeration = (IEnumeration)((IEnumerator)binding).getType();
			PDOMBinding pdomEnumeration = adaptBinding(enumeration);
			if (pdomEnumeration instanceof PDOMCPPEnumeration)
				pdomBinding = new PDOMCPPEnumerator(pdom, parent, (IEnumerator) binding,
						(PDOMCPPEnumeration)pdomEnumeration);
		} else if (binding instanceof ITypedef) {
			pdomBinding = new PDOMCPPTypedef(pdom, parent, (ITypedef)binding);
		}

		if(pdomBinding!=null) {
			parent.addChild(pdomBinding);
		}
		
		pushPostProcesses(pdomBinding, binding);
		
		return pdomBinding;
	}
	
	private void pushPostProcesses(PDOMBinding pdomBinding, IBinding binding) throws CoreException, DOMException {
		if (pdomBinding instanceof PDOMCPPClassTemplatePartialSpecialization &&
				binding instanceof ICPPClassTemplatePartialSpecialization) {
			PDOMCPPClassTemplatePartialSpecialization pdomSpec = (PDOMCPPClassTemplatePartialSpecialization) pdomBinding;
			ICPPClassTemplatePartialSpecialization spec = (ICPPClassTemplatePartialSpecialization) binding;
			pushPostProcess(new ConfigurePartialSpecialization(pdomSpec, spec));
		} 
		if (pdomBinding instanceof PDOMCPPFunctionTemplate && binding instanceof ICPPFunction) {
			PDOMCPPFunctionTemplate pdomTemplate = (PDOMCPPFunctionTemplate) pdomBinding;
			ICPPFunction function = (ICPPFunction) binding;
			pushPostProcess(new ConfigureFunctionTemplate(pdomTemplate, function));
		}
		if (pdomBinding instanceof ICPPTemplateDefinition && binding instanceof ICPPTemplateDefinition) {
			ICPPTemplateDefinition template = (ICPPTemplateDefinition) binding;
			pushPostProcess(new ConfigureTemplate(template));
		}
	}
	
	private void addImplicitMethods(PDOMBinding type, ICPPClassType binding) throws CoreException {
		try {
			IScope scope = binding.getCompositeScope();
			if (scope instanceof ICPPClassScope) {
				ICPPMethod[] implicit= ((ICPPClassScope) scope).getImplicitMethods();
				for (int i = 0; i < implicit.length; i++) {
					ICPPMethod method = implicit[i];
					if (adaptBinding(method) == null) {
						addBinding(type, method);
					}
				}
			}
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
	}

	public int getBindingType(IBinding binding) {
		if (binding instanceof ICPPSpecialization) {
			if (binding instanceof ICPPDeferredTemplateInstance) {
				if (binding instanceof ICPPFunction)
					return CPP_DEFERRED_FUNCTION_INSTANCE;
				if (binding instanceof ICPPClassType)
					return CPP_DEFERRED_CLASS_INSTANCE;	
			} else if (binding instanceof ICPPTemplateInstance) {
				if (binding instanceof ICPPConstructor)
					return CPP_CONSTRUCTOR_INSTANCE;
				if (binding instanceof ICPPMethod)
					return CPP_METHOD_INSTANCE;
				else if (binding instanceof ICPPFunction)
					return CPP_FUNCTION_INSTANCE;
				else if (binding instanceof ICPPClassType)
					return CPP_CLASS_INSTANCE;		
			} else if (binding instanceof ICPPClassTemplatePartialSpecialization)
				return CPP_CLASS_TEMPLATE_PARTIAL_SPEC;
			else if (binding instanceof ICPPField)
				return CPP_FIELD_SPECIALIZATION;
			else if (binding instanceof ICPPFunctionTemplate) {
				if (binding instanceof ICPPConstructor)
					return CPP_CONSTRUCTOR_TEMPLATE_SPECIALIZATION;
				else if (binding instanceof ICPPMethod)
					return CPP_METHOD_TEMPLATE_SPECIALIZATION;
				else if (binding instanceof ICPPFunction)
					return CPP_FUNCTION_TEMPLATE_SPECIALIZATION;
			} else if (binding instanceof ICPPConstructor)
				return CPP_CONSTRUCTOR_SPECIALIZATION;
			else if (binding instanceof ICPPMethod)
				return CPP_METHOD_SPECIALIZATION;
			else if (binding instanceof ICPPFunction)
				return CPP_FUNCTION_SPECIALIZATION;
			else if (binding instanceof ICPPClassTemplate)
				return CPP_CLASS_TEMPLATE_SPECIALIZATION;
			else if (binding instanceof ICPPClassType)
				return CPP_CLASS_SPECIALIZATION;
			else if (binding instanceof ITypedef)
				return CPP_TYPEDEF_SPECIALIZATION;
		} else if (binding instanceof ICPPTemplateParameter) {
			if (binding instanceof ICPPTemplateTypeParameter)
				return CPP_TEMPLATE_TYPE_PARAMETER;
// TODO other template parameter types
//			else if (binding instanceof ICPPTemplateTemplateParameter)
//				return CPP_TEMPLATE_TEMPLATE_PARAMETER;
//			else if (binding instanceof ICPPTemplateNonTypeParameter)
//				return CPP_TEMPLATE_NON_TYPE_PARAMETER;
		} else if (binding instanceof ICPPField)
			// this must be before variables
			return CPPFIELD;
		else if (binding instanceof ICPPVariable)
			return CPPVARIABLE;
		else if (binding instanceof ICPPFunctionTemplate) {
			// this must be before functions
			if (binding instanceof ICPPConstructor)
				return CPP_CONSTRUCTOR_TEMPLATE;
			else if (binding instanceof ICPPMethod)
				return CPP_METHOD_TEMPLATE;
			else if (binding instanceof ICPPFunction)
				return CPP_FUNCTION_TEMPLATE;
		} else if (binding instanceof ICPPConstructor)
			// before methods
			return CPP_CONSTRUCTOR;
		else if (binding instanceof ICPPMethod)
			// this must be before functions
			return CPPMETHOD;
		else if (binding instanceof ICPPFunctionType)
			return CPP_FUNCTION_TYPE;
		else if (binding instanceof ICPPFunction)
			return CPPFUNCTION;
		else if (binding instanceof ICPPClassTemplate)
			// this must be before class type
			return CPP_CLASS_TEMPLATE;
		else if (binding instanceof ICPPClassType)
			return CPPCLASSTYPE;
		else if (binding instanceof ICPPNamespaceAlias)
			return CPPNAMESPACEALIAS;
		else if (binding instanceof ICPPNamespace)
			return CPPNAMESPACE;
		else if (binding instanceof IEnumeration)
			return CPPENUMERATION;
		else if (binding instanceof IEnumerator)
			return CPPENUMERATOR;
		else if (binding instanceof ITypedef)
			return CPPTYPEDEF;
			
		return 0;
	}

	/**
	 * Find the equivalent binding, or binding placeholder within this PDOM
	 */
	public PDOMBinding adaptBinding(IBinding binding) throws CoreException {
		if (binding == null || binding instanceof IProblemBinding)
			return null;

		if (binding instanceof PDOMBinding) {
			// there is no guarantee, that the binding is from the same PDOM object.
			PDOMBinding pdomBinding = (PDOMBinding) binding;
			if (pdomBinding.getPDOM() == getPDOM()) {
				return pdomBinding;
			}
			// so if the binding is from another pdom it has to be adapted. 
		}
		else {
			// assign names to anonymous types.
			binding= PDOMASTAdapter.getAdapterIfAnonymous(binding);
			if (binding == null) {
				return null;
			}
		}

		PDOMNode parent = getAdaptedParent(binding, false, false);

		if (parent == this) {
			return CPPFindBinding.findBinding(getIndex(), this, binding);
		} else if (parent instanceof IPDOMMemberOwner) {
			return CPPFindBinding.findBinding(parent, this, binding);
		} else if (parent instanceof PDOMCPPNamespace) {
			return CPPFindBinding.findBinding(((PDOMCPPNamespace)parent).getIndex(), this, binding);
		}

		return null;
	}

	public PDOMNode addType(PDOMNode parent, IType type) throws CoreException {
		if (type instanceof IProblemBinding) {
			return null;
		}
		if (type instanceof ICPPBasicType) {
			return new PDOMCPPBasicType(pdom, parent, (ICPPBasicType) type);
		}
		if (type instanceof ICPPFunctionType) {
			return new PDOMCPPFunctionType(pdom, parent, (ICPPFunctionType) type);
		}
		if (type instanceof ICPPClassType) {
			return addBinding((ICPPClassType) type);
		}
		if (type instanceof IEnumeration) {
			return addBinding((IEnumeration) type);
		}
		if (type instanceof ITypedef) {
			return addBinding((ITypedef) type);
		}
		if (type instanceof ICPPReferenceType) {
			return new PDOMCPPReferenceType(pdom, parent,
					(ICPPReferenceType) type);
		}
		if (type instanceof ICPPPointerToMemberType) {
			return new PDOMCPPPointerToMemberType(pdom, parent,
					(ICPPPointerToMemberType) type);
		}
		if (type instanceof ICPPTemplateTypeParameter) {
			return addBinding((ICPPTemplateTypeParameter) type);
		}

		return super.addType(parent, type); 
	}

	private void handlePostProcesses() {
		while (!postProcesses.isEmpty()) {
			popPostProcess().run();
		}
	}
	
	private void pushPostProcess(Runnable process) {
		postProcesses.add(postProcesses.size(), process);
	}
	
	private Runnable popPostProcess() {
		return (Runnable) postProcesses.remove(postProcesses.size() - 1);
	}
	
	public PDOMNode getNode(int record) throws CoreException {
		if (record == 0)
			return null;

		switch (PDOMNode.getNodeType(pdom, record)) {
		case CPPVARIABLE:
			return new PDOMCPPVariable(pdom, record);
		case CPPFUNCTION:
			return new PDOMCPPFunction(pdom, record);
		case CPPCLASSTYPE:
			return new PDOMCPPClassType(pdom, record);
		case CPPFIELD:
			return new PDOMCPPField(pdom, record);
		case CPP_CONSTRUCTOR:
			return new PDOMCPPConstructor(pdom, record);
		case CPPMETHOD:
			return new PDOMCPPMethod(pdom, record);
		case CPPNAMESPACE:
			return new PDOMCPPNamespace(pdom, record);
		case CPPNAMESPACEALIAS:
			return new PDOMCPPNamespaceAlias(pdom, record);
		case CPPBASICTYPE:
			return new PDOMCPPBasicType(pdom, record);
		case CPPPARAMETER:
			return new PDOMCPPParameter(pdom, record);
		case CPPENUMERATION:
			return new PDOMCPPEnumeration(pdom, record);
		case CPPENUMERATOR:
			return new PDOMCPPEnumerator(pdom, record);
		case CPPTYPEDEF:
			return new PDOMCPPTypedef(pdom, record);
		case CPP_POINTER_TO_MEMBER_TYPE:
			return new PDOMCPPPointerToMemberType(pdom, record);
		case CPP_REFERENCE_TYPE:
			return new PDOMCPPReferenceType(pdom, record);
		case CPP_FUNCTION_TEMPLATE:
			return new PDOMCPPFunctionTemplate(pdom, record);
		case CPP_METHOD_TEMPLATE:
			return new PDOMCPPMethodTemplate(pdom, record);
		case CPP_CONSTRUCTOR_TEMPLATE:
			return new PDOMCPPConstructorTemplate(pdom, record);
		case CPP_CLASS_TEMPLATE:
			return new PDOMCPPClassTemplate(pdom, record);
		case CPP_CLASS_TEMPLATE_PARTIAL_SPEC:
			return new PDOMCPPClassTemplatePartialSpecialization(pdom, record);
		case CPP_FUNCTION_INSTANCE:
			return new PDOMCPPFunctionInstance(pdom, record);
		case CPP_METHOD_INSTANCE:
			return new PDOMCPPMethodInstance(pdom, record);
		case CPP_CONSTRUCTOR_INSTANCE:
			return new PDOMCPPConstructorInstance(pdom, record);
		case CPP_DEFERRED_FUNCTION_INSTANCE:
			return new PDOMCPPDeferredFunctionInstance(pdom, record);
		case CPP_CLASS_INSTANCE:
			return new PDOMCPPClassInstance(pdom, record);
		case CPP_DEFERRED_CLASS_INSTANCE:
			return new PDOMCPPDeferredClassInstance(pdom, record);
		case CPP_TEMPLATE_TYPE_PARAMETER:
			return new PDOMCPPTemplateTypeParameter(pdom, record);
// TODO other template parameter types
//		case CPP_TEMPLATE_TEMPLATE_PARAMETER:
//			return new PDOMCPPTemplateTemplateParameter(pdom, record);
//		case CPP_TEMPLATE_NON_TYPE_PARAMETER:
//			return new PDOMCPPTemplateNonTypeParameter(pdom, record);
		case CPP_FIELD_SPECIALIZATION:
			return new PDOMCPPFieldSpecialization(pdom, record);
		case CPP_FUNCTION_SPECIALIZATION:
			return new PDOMCPPFunctionSpecialization(pdom, record);
		case CPP_METHOD_SPECIALIZATION:
			return new PDOMCPPMethodSpecialization(pdom, record);
		case CPP_CONSTRUCTOR_SPECIALIZATION:
			return new PDOMCPPConstructorSpecialization(pdom, record);
		case CPP_CLASS_SPECIALIZATION:
			return new PDOMCPPClassSpecialization(pdom, record);
		case CPP_FUNCTION_TEMPLATE_SPECIALIZATION:
			return new PDOMCPPFunctionTemplateSpecialization(pdom, record);
		case CPP_METHOD_TEMPLATE_SPECIALIZATION:
			return new PDOMCPPMethodTemplateSpecialization(pdom, record);
		case CPP_CONSTRUCTOR_TEMPLATE_SPECIALIZATION:
			return new PDOMCPPConstructorTemplateSpecialization(pdom, record);
		case CPP_CLASS_TEMPLATE_SPECIALIZATION:
			return new PDOMCPPClassTemplateSpecialization(pdom, record);
		case CPP_TYPEDEF_SPECIALIZATION:
			return new PDOMCPPTypedefSpecialization(pdom, record);
		case CPP_FUNCTION_TYPE:
			return new PDOMCPPFunctionType(pdom, record);
		default:
			return super.getNode(record);
		}
	}

	public IBTreeComparator getIndexComparator() {
		return new CPPFindBinding.CPPBindingBTreeComparator(pdom);
	}

	public void onCreateName(PDOMName pdomName, IASTName name) throws CoreException {
		super.onCreateName(pdomName, name);
		
		IASTNode parentNode= name.getParent();
		if (parentNode instanceof ICPPASTQualifiedName) {
		    IASTName [] ns = ((ICPPASTQualifiedName)parentNode).getNames();
		    if ( name != ns[ ns.length -1 ] ) {
		    	return;
		    } else {
		    	parentNode = parentNode.getParent();
		    }
		}
		if (parentNode instanceof ICPPASTBaseSpecifier) {
			PDOMName derivedClassName= (PDOMName) pdomName.getEnclosingDefinition();
			if (derivedClassName != null) {
				ICPPASTBaseSpecifier baseNode= (ICPPASTBaseSpecifier) parentNode;
				PDOMBinding derivedClassBinding= derivedClassName.getPDOMBinding();
				if (derivedClassBinding instanceof PDOMCPPClassType) {
					PDOMCPPClassType ownerClass = (PDOMCPPClassType)derivedClassBinding;
					PDOMCPPBase pdomBase = new PDOMCPPBase(pdom, pdomName, baseNode.isVirtual(), baseNode.getVisibility());
					ownerClass.addBase(pdomBase);
					pdomName.setIsBaseSpecifier(true);
				} else if (derivedClassBinding instanceof PDOMCPPClassSpecialization) {
					PDOMCPPClassSpecialization ownerClass = (PDOMCPPClassSpecialization)derivedClassBinding;
					PDOMCPPBase pdomBase = new PDOMCPPBase(pdom, pdomName, baseNode.isVirtual(), baseNode.getVisibility());
					ownerClass.addBase(pdomBase);
					pdomName.setIsBaseSpecifier(true);
				}
			}
		}
	}
	
	public void onDeleteName(PDOMName pdomName) throws CoreException {
		super.onDeleteName(pdomName);
		
		if (pdomName.isBaseSpecifier()) {
			PDOMName derivedClassName= (PDOMName) pdomName.getEnclosingDefinition();
			if (derivedClassName != null) {
				PDOMBinding derivedClassBinding= derivedClassName.getPDOMBinding();
				if (derivedClassBinding instanceof PDOMCPPClassType) {
					PDOMCPPClassType ownerClass = (PDOMCPPClassType)derivedClassBinding;
					ownerClass.removeBase(pdomName);
				} else if (derivedClassBinding instanceof PDOMCPPClassSpecialization) {
					PDOMCPPClassSpecialization ownerClass = (PDOMCPPClassSpecialization)derivedClassBinding;
					ownerClass.removeBase(pdomName);
				}
			}
		}
	}

	protected boolean isFileLocalBinding(IBinding binding) throws DOMException {
		if (binding instanceof ICPPField) {
			return false;
		}
		if (binding instanceof ICPPVariable) {
			if (!(binding.getScope() instanceof CPPBlockScope)) {
				ICPPVariable var= (ICPPVariable) binding;
				return var.isStatic();
			}
			return false;
		}
		if (binding instanceof ICPPMethod) {
			return false;
		}
		if (binding instanceof ICPPInternalFunction) {
			ICPPInternalFunction func = (ICPPInternalFunction)binding;
			return func.isStatic(false);
		}
		return false;
	}
}
