/*******************************************************************************
 * Copyright (c) 2005, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.LinkedList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPBasicType;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.composite.CompositeIndexBinding;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMASTAdapter;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Container for c++-entities.
 */
class PDOMCPPLinkage extends PDOMLinkage implements IIndexCPPBindingConstants {
	public final static int CACHE_MEMBERS= 0;
	public final static int CACHE_BASES= 1;
	public final static int CACHE_INSTANCES= 2;
	public final static int CACHE_INSTANCE_SCOPE= 3;

	private LinkedList<Runnable> postProcesses = new LinkedList<Runnable>();
	
	public PDOMCPPLinkage(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMCPPLinkage(PDOM pdom) throws CoreException {
		super(pdom, CPP_LINKAGE_NAME, CPP_LINKAGE_NAME.toCharArray());
	}

	public String getLinkageName() {
		return CPP_LINKAGE_NAME;
	}

	public int getLinkageID() {
		return CPP_LINKAGE_ID;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return LINKAGE;
	}

	// Binding types
	class ConfigureTemplateParameters implements Runnable {
		private final IPDOMCPPTemplateParameter[] fPersisted;
		private final ICPPTemplateParameter[] fOriginal;
		
		public ConfigureTemplateParameters(ICPPTemplateParameter[] original, IPDOMCPPTemplateParameter[] params) {
			fOriginal= original;
			fPersisted= params;
			postProcesses.add(this);
		}
		
		public void run() {
			for (int i = 0; i < fOriginal.length; i++) {
				final IPDOMCPPTemplateParameter tp = fPersisted[i];
				if (tp != null)
					tp.configure(fOriginal[i]);
			}
		}
	}
	
	class ConfigurePartialSpecialization implements Runnable {
		PDOMCPPClassTemplatePartialSpecialization partial;
		ICPPClassTemplatePartialSpecialization binding;
		
		public ConfigurePartialSpecialization(PDOMCPPClassTemplatePartialSpecialization partial,
				ICPPClassTemplatePartialSpecialization binding) {
			this.partial = partial;
			this.binding = binding;
			postProcesses.add(this);
		}
		
		public void run() {
			try {
				ICPPTemplateArgument[] args = binding.getTemplateArguments();
				partial.setArguments(args);
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
	
	class ConfigureFunctionTemplate implements Runnable {
		private final PDOMCPPFunctionTemplate fTemplate;
		private final IPDOMCPPTemplateParameter[] fTemplateParameters;
		private final ICPPTemplateParameter[] fOriginalTemplateParameters;
		private final ICPPFunctionType fOriginalFunctionType;
		private final IParameter[] fOriginalParameters;
		
		public ConfigureFunctionTemplate(ICPPFunctionTemplate original, PDOMCPPFunctionTemplate template) throws DOMException {
			fTemplate = template;
			fTemplateParameters= template.getTemplateParameters();
			fOriginalTemplateParameters= original.getTemplateParameters();
			fOriginalFunctionType= original.getType();
			fOriginalParameters= original.getParameters();
			postProcesses.add(this);
		}
		
		public void run() {
			for (int i = 0; i < fOriginalTemplateParameters.length; i++) {
				final IPDOMCPPTemplateParameter tp = fTemplateParameters[i];
				if (tp != null)
					tp.configure(fOriginalTemplateParameters[i]);
			}

			fTemplate.initData(fOriginalFunctionType, fOriginalParameters);
		}
	}
	
	/**
	 * Adds or returns existing binding for the given name.
	 */
	@Override
	public PDOMBinding addBinding(IASTName name) throws CoreException {
		if (name == null || name instanceof ICPPASTQualifiedName)
			return null;

		// Check for null name
		char[] namechars = name.toCharArray();
		if (namechars == null)
			return null;
		
		IBinding binding = name.resolveBinding();

		PDOMBinding pdomBinding = addBinding(binding, name);
		if (pdomBinding instanceof PDOMCPPClassType || pdomBinding instanceof PDOMCPPClassSpecialization) {
			if (binding instanceof ICPPClassType && name.isDefinition()) {
				addImplicitMethods(pdomBinding, (ICPPClassType) binding);
			}
		}
		
		handlePostProcesses();
		return pdomBinding;
	}

	@Override
	public PDOMBinding addUnknownValue(IBinding binding) throws CoreException {
		return addBinding(binding, null);
	}

	/**
	 * Adds or returns existing binding for the given one. If <code>fromName</code> is not <code>null</code>
	 * then an existing binding is updated with the properties of the name.
	 */
	private PDOMBinding addBinding(IBinding inputBinding, IASTName fromName) throws CoreException {
		if (inputBinding instanceof CompositeIndexBinding) {
			inputBinding= ((CompositeIndexBinding) inputBinding).getRawBinding();
		}

		if (cannotAdapt(inputBinding)) {
			return null;
		}
		
		PDOMBinding pdomBinding= attemptFastAdaptBinding(inputBinding);
		if (pdomBinding == null) {
			// assign names to anonymous types.
			IBinding binding= PDOMASTAdapter.getAdapterForAnonymousASTBinding(inputBinding);
			if (binding == null) 
				return null;

			final PDOMNode parent= adaptOrAddParent(true, binding);
			if (parent == null)
				return null;
		
			pdomBinding = adaptBinding(parent, binding);
			if (pdomBinding != null) {
				pdom.putCachedResult(inputBinding, pdomBinding);
			} else {
				try {
					pdomBinding = createBinding(parent, binding);
					if (pdomBinding != null) {
						pdom.putCachedResult(inputBinding, pdomBinding);
					}
				} catch (DOMException e) {
					throw new CoreException(Util.createStatus(e));
				}
				return pdomBinding;
			}
		}

		if (shouldUpdate(pdomBinding, fromName)) {
			pdomBinding.update(this, fromName.getBinding());
		}
		return pdomBinding;
	}

	private boolean shouldUpdate(PDOMBinding pdomBinding, IASTName fromName) throws CoreException {
		if (fromName != null) {
			if (pdomBinding instanceof IParameter || pdomBinding instanceof ICPPTemplateParameter)
				return false;
			if (fromName.isReference()) {
				return false;
			}
			if (pdomBinding instanceof ICPPMember) {
				IASTNode node= fromName.getParent();
				while (node != null) {
					if (node instanceof IASTCompositeTypeSpecifier) {
						return true;
					}
					node= node.getParent();
				}
				return false;
			}
			if (fromName.isDefinition()) {
				return true;
			}
			return !pdomBinding.hasDefinition();
		}
		return false;
	}

	PDOMBinding createBinding(PDOMNode parent, IBinding binding) throws CoreException, DOMException {
		PDOMBinding pdomBinding= null;

		// template parameters are created directly by their owners.
		if (binding instanceof ICPPTemplateParameter) 
			return null;
		
		if (binding instanceof ICPPSpecialization) {
			IBinding specialized = ((ICPPSpecialization)binding).getSpecializedBinding();
			PDOMBinding pdomSpecialized= addBinding(specialized, null);
			if (pdomSpecialized == null)
				return null;

			pdomBinding = createSpecialization(parent, pdomSpecialized, binding);
		} else if (binding instanceof ICPPField) {
			if (parent instanceof PDOMCPPClassType || parent instanceof PDOMCPPClassSpecialization) {
				pdomBinding = new PDOMCPPField(pdom, parent, (ICPPField) binding);
			}
		} else if (binding instanceof ICPPVariable) {
			ICPPVariable var= (ICPPVariable) binding;
			pdomBinding = new PDOMCPPVariable(pdom, parent, var);
		} else if (binding instanceof ICPPFunctionTemplate) {
			if (binding instanceof ICPPConstructor) {
				pdomBinding= new PDOMCPPConstructorTemplate(pdom, this, parent, (ICPPConstructor) binding);
			} else if (binding instanceof ICPPMethod) {
				pdomBinding= new PDOMCPPMethodTemplate(pdom, this, parent, (ICPPMethod) binding);
			} else if (binding instanceof ICPPFunction) {
				pdomBinding= new PDOMCPPFunctionTemplate(pdom, this, parent, (ICPPFunctionTemplate) binding);
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
			pdomBinding= new PDOMCPPClassTemplate(pdom, this, parent, (ICPPClassTemplate) binding);
		} else if (binding instanceof ICPPClassType) {
			if (binding instanceof ICPPUnknownClassInstance) {
				pdomBinding= new PDOMCPPUnknownClassInstance(pdom, parent, (ICPPUnknownClassInstance) binding);
			} else if (binding instanceof ICPPUnknownClassType) {
				pdomBinding= new PDOMCPPUnknownClassType(pdom, parent, (ICPPUnknownClassType) binding);
			} else {
				pdomBinding= new PDOMCPPClassType(pdom, parent, (ICPPClassType) binding);
			}
		} else if (binding instanceof ICPPNamespaceAlias) {
			pdomBinding = new PDOMCPPNamespaceAlias(pdom, parent, (ICPPNamespaceAlias) binding);
		} else if (binding instanceof ICPPNamespace) {
			pdomBinding = new PDOMCPPNamespace(pdom, parent, (ICPPNamespace) binding);
		} else if (binding instanceof ICPPUsingDeclaration) {
			pdomBinding = new PDOMCPPUsingDeclaration(pdom, parent, (ICPPUsingDeclaration) binding);
		} else if (binding instanceof IEnumeration) {
			pdomBinding = new PDOMCPPEnumeration(pdom, parent, (IEnumeration) binding);
		} else if (binding instanceof IEnumerator) {
			final IEnumerator etor = (IEnumerator) binding;
			IType enumeration= etor.getType();
			if (enumeration instanceof IEnumeration) {
				PDOMBinding pdomEnumeration = adaptBinding((IEnumeration) enumeration);
				if (pdomEnumeration instanceof PDOMCPPEnumeration) {
					pdomBinding = new PDOMCPPEnumerator(pdom, parent, etor,	(PDOMCPPEnumeration)pdomEnumeration);
				}
			}
		} else if (binding instanceof ITypedef) {
			pdomBinding = new PDOMCPPTypedef(pdom, parent, (ITypedef)binding);
		}

		if (pdomBinding != null) {
			pdomBinding.setLocalToFileRec(getLocalToFileRec(parent, binding));
			parent.addChild(pdomBinding);
			afterAddBinding(pdomBinding);
		}
		
		return pdomBinding;
	}

	private PDOMBinding createSpecialization(PDOMNode parent, PDOMBinding orig, IBinding special) 
			throws CoreException, DOMException {
		PDOMBinding result= null;
		if (special instanceof ICPPDeferredClassInstance) {
			if (orig instanceof ICPPClassTemplate) {
				result= new PDOMCPPDeferredClassInstance(pdom,	parent, (ICPPDeferredClassInstance) special, orig);
			}
		} else if (special instanceof ICPPTemplateInstance) {
			if (special instanceof ICPPConstructor && orig instanceof ICPPConstructor) {
				result= new PDOMCPPConstructorInstance(pdom, parent, (ICPPConstructor) special, orig);
			} else if (special instanceof ICPPMethod && orig instanceof ICPPMethod) {
				result= new PDOMCPPMethodInstance(pdom, parent, (ICPPMethod) special, orig);
			} else if (special instanceof ICPPFunction && orig instanceof ICPPFunction) {
				result= new PDOMCPPFunctionInstance(pdom, parent, (ICPPFunction) special, orig);
			} else if (special instanceof ICPPClassType && orig instanceof ICPPClassType) {
				result= new PDOMCPPClassInstance(pdom, parent, (ICPPClassType) special, orig);
			}
		} else if (special instanceof ICPPClassTemplatePartialSpecialization) {
			if (orig instanceof PDOMCPPClassTemplate) {
				result= new PDOMCPPClassTemplatePartialSpecialization(
						pdom, this, parent, (ICPPClassTemplatePartialSpecialization) special,
						(PDOMCPPClassTemplate) orig);
			}
		} else if (special instanceof ICPPField) {
			result= new PDOMCPPFieldSpecialization(pdom, parent, (ICPPField) special, orig);
		} else if (special instanceof ICPPFunctionTemplate) {
			if (special instanceof ICPPConstructor) {
				result= new PDOMCPPConstructorTemplateSpecialization( pdom, parent, (ICPPConstructor) special, orig); 
			} else if (special instanceof ICPPMethod) {
				result= new PDOMCPPMethodTemplateSpecialization( pdom, parent, (ICPPMethod) special, orig);
			} else if (special instanceof ICPPFunction) {
				result= new PDOMCPPFunctionTemplateSpecialization( pdom, parent, (ICPPFunctionTemplate) special, orig);
			}
		} else if (special instanceof ICPPConstructor) {
			result= new PDOMCPPConstructorSpecialization(pdom, parent, (ICPPConstructor) special, orig);
		} else if (special instanceof ICPPMethod) {
			result= new PDOMCPPMethodSpecialization(pdom, parent, (ICPPMethod) special, orig);
		} else if (special instanceof ICPPFunction) {
			result= new PDOMCPPFunctionSpecialization(pdom, parent, (ICPPFunction) special, orig);
		} else if (special instanceof ICPPClassTemplate) {
			result= new PDOMCPPClassTemplateSpecialization(pdom, parent, (ICPPClassTemplate) special, orig);
		} else if (special instanceof ICPPClassType) {
			result= new PDOMCPPClassSpecialization(pdom, parent, (ICPPClassType) special, orig);
		} else if (special instanceof ITypedef) {
			result= new PDOMCPPTypedefSpecialization(pdom, parent, (ITypedef) special, orig);
		}
		
		return result;
	}
	
	private void addImplicitMethods(PDOMBinding type, ICPPClassType binding) throws CoreException {
		try {
			IScope scope = binding.getCompositeScope();
			if (scope instanceof ICPPClassScope) {
				ICPPMethod[] implicit= ((ICPPClassScope) scope).getImplicitMethods();
				for (ICPPMethod method : implicit) {
					if (!(method instanceof IProblemBinding)) {
						PDOMBinding pdomBinding= adaptBinding(method);
						if (pdomBinding == null) {
							createBinding(type, method);
						} else if (!pdomBinding.hasDefinition()) {
							pdomBinding.update(this, method);
						}
					}
				}
			}
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public int getBindingType(IBinding binding) {
		if (binding instanceof ICPPSpecialization) {
			if (binding instanceof ICPPTemplateInstance) {
				if (binding instanceof ICPPDeferredClassInstance) {
					return CPP_DEFERRED_CLASS_INSTANCE;
				} else if (binding instanceof ICPPConstructor) {
					return CPP_CONSTRUCTOR_INSTANCE;
				} else if (binding instanceof ICPPMethod) {
					return CPP_METHOD_INSTANCE;
				} else if (binding instanceof ICPPFunction) {
					return CPP_FUNCTION_INSTANCE;
				} else if (binding instanceof ICPPClassType) {
					return CPP_CLASS_INSTANCE;		
				}
			} else if (binding instanceof ICPPClassTemplatePartialSpecialization) {
				return CPP_CLASS_TEMPLATE_PARTIAL_SPEC;
			} else if (binding instanceof ICPPField) {
				return CPP_FIELD_SPECIALIZATION;
		    } else if (binding instanceof ICPPFunctionTemplate) {
				if (binding instanceof ICPPConstructor) {
					return CPP_CONSTRUCTOR_TEMPLATE_SPECIALIZATION;
				} else if (binding instanceof ICPPMethod) {
					return CPP_METHOD_TEMPLATE_SPECIALIZATION;
				} else if (binding instanceof ICPPFunction) {
					return CPP_FUNCTION_TEMPLATE_SPECIALIZATION;
				}
			} else if (binding instanceof ICPPConstructor) {
				return CPP_CONSTRUCTOR_SPECIALIZATION;
			} else if (binding instanceof ICPPMethod) {
				return CPP_METHOD_SPECIALIZATION;
			} else if (binding instanceof ICPPFunction) {
				return CPP_FUNCTION_SPECIALIZATION;
			} else if (binding instanceof ICPPClassTemplate) {
				return CPP_CLASS_TEMPLATE_SPECIALIZATION;
			} else if (binding instanceof ICPPClassType) {
				return CPP_CLASS_SPECIALIZATION;
			} else if (binding instanceof ITypedef) {
				return CPP_TYPEDEF_SPECIALIZATION;
			}
		} else if (binding instanceof ICPPTemplateParameter) {
			if (binding instanceof ICPPTemplateTypeParameter) {
				return CPP_TEMPLATE_TYPE_PARAMETER;
			}
// TODO other template parameter types
//			else if (binding instanceof ICPPTemplateTemplateParameter)
//				return CPP_TEMPLATE_TEMPLATE_PARAMETER;
//			else if (binding instanceof ICPPTemplateNonTypeParameter)
//				return CPP_TEMPLATE_NON_TYPE_PARAMETER;
		} else if (binding instanceof ICPPField) {
			// this must be before variables
			return CPPFIELD;
		} else if (binding instanceof ICPPVariable) {
			return CPPVARIABLE;
		} else if (binding instanceof ICPPFunctionTemplate) {
			// this must be before functions
			if (binding instanceof ICPPConstructor) {
				return CPP_CONSTRUCTOR_TEMPLATE;
			} else if (binding instanceof ICPPMethod) {
				return CPP_METHOD_TEMPLATE;
			} else if (binding instanceof ICPPFunction) {
				return CPP_FUNCTION_TEMPLATE;
			}
		} else if (binding instanceof ICPPConstructor) {
			// before methods
			return CPP_CONSTRUCTOR;
		} else if (binding instanceof ICPPMethod) {
			// this must be before functions
			return CPPMETHOD;
		} else if (binding instanceof ICPPFunctionType) {
			return CPP_FUNCTION_TYPE;
		} else if (binding instanceof ICPPFunction) {
			return CPPFUNCTION;
		} else if (binding instanceof ICPPUnknownBinding) {
			if (binding instanceof ICPPUnknownClassInstance) {
				return CPP_UNKNOWN_CLASS_INSTANCE;
			} else if (binding instanceof ICPPUnknownClassType) {
				return CPP_UNKNOWN_CLASS_TYPE;
			}
		} else if (binding instanceof ICPPClassTemplate) {
			// this must be before class type
			return CPP_CLASS_TEMPLATE;
		} else if (binding instanceof ICPPClassType) {
			return CPPCLASSTYPE;
		} else if (binding instanceof ICPPNamespaceAlias) {
			return CPPNAMESPACEALIAS;
		} else if (binding instanceof ICPPNamespace) {
			return CPPNAMESPACE;
		} else if (binding instanceof ICPPUsingDeclaration) {
			return CPP_USING_DECLARATION;
		} else if (binding instanceof IEnumeration) {
			return CPPENUMERATION;
		} else if (binding instanceof IEnumerator) {
			return CPPENUMERATOR;
		} else if (binding instanceof ITypedef) {
			return CPPTYPEDEF;
		}
			
		return 0;
	}

	@Override
	public final PDOMBinding adaptBinding(final IBinding inputBinding) throws CoreException {
		return adaptBinding(null, inputBinding);
	}

	private final PDOMBinding adaptBinding(final PDOMNode parent, IBinding inputBinding) throws CoreException {
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

	/**
	 * Find the equivalent binding, or binding place holder within this PDOM
	 */
	private final PDOMBinding doAdaptBinding(PDOMNode parent, IBinding binding) throws CoreException {
		if (parent == null) {
			parent= adaptOrAddParent(false, binding);
		}
		if (parent == this) {
			int localToFileRec= getLocalToFileRec(null, binding);
			return CPPFindBinding.findBinding(getIndex(), this, binding, localToFileRec);
		}
		if (parent instanceof PDOMCPPNamespace) {
			int localToFileRec= getLocalToFileRec(parent, binding);
			return CPPFindBinding.findBinding(((PDOMCPPNamespace) parent).getIndex(), this, binding,
					localToFileRec);
		}
		if (binding instanceof ICPPTemplateParameter && parent instanceof IPDOMCPPTemplateParameterOwner) {
			return (PDOMBinding) ((IPDOMCPPTemplateParameterOwner) parent).adaptTemplateParameter(
					(ICPPTemplateParameter) binding);
		}
		if (parent instanceof IPDOMMemberOwner) {
			int localToFileRec= getLocalToFileRec(parent, binding);
			return CPPFindBinding.findBinding(parent, this, binding, localToFileRec);
		}
		return null;
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
 	private final PDOMNode adaptOrAddParent(boolean add, IBinding binding) throws CoreException {
 		try {
 			IBinding owner= binding.getOwner();
 			if (owner instanceof IFunction && !(binding instanceof ICPPTemplateParameter)) {
 				return null;
 			}

 			if (binding instanceof IIndexBinding) {
 				IIndexBinding ib= (IIndexBinding) binding;
 				// don't adapt file local bindings from other fragments to this one.
 				if (ib.isFileLocal()) {
 					return null;
 				}
 			} else {
 				// skip unnamed namespaces
 				while (owner instanceof ICPPNamespace) {
 					char[] name= owner.getNameCharArray();
 					if (name.length > 0) {
 						break;
 					}
 					owner= owner.getOwner();
 				}
 			}
 			
 			if (owner == null)
 				return this;

 			return adaptOrAddBinding(add, owner);
 		} catch (DOMException e) {
 			throw new CoreException(Util.createStatus(e));
 		}
 	}

	private PDOMBinding adaptOrAddBinding(boolean add, IBinding binding) throws CoreException {
		if (add) {
			return addBinding(binding, null);
		}
		return adaptBinding(binding);
	}

	@Override
	public PDOMNode addType(PDOMNode parent, IType type) throws CoreException {
		if (type instanceof IProblemBinding) {
			return null;
		}
		if (type instanceof IGPPBasicType) {
			IGPPBasicType gbt= (IGPPBasicType) type;
			IType typeof;
			try {
				typeof = gbt.getTypeofType();
				if (typeof != null) {
					return addType(parent, typeof);
				}
			} catch (DOMException e) {
			}
			return new PDOMGPPBasicType(pdom, parent, gbt);
		}
		if (type instanceof ICPPBasicType) {
			return new PDOMCPPBasicType(pdom, parent, (ICPPBasicType) type);
		}
		if (type instanceof ICPPFunctionType) {
			return new PDOMCPPFunctionType(pdom, parent, (ICPPFunctionType) type);
		}
		if (type instanceof ICPPClassType) {
			return addBinding((ICPPClassType) type, null);
		}
		if (type instanceof IEnumeration) {
			return addBinding((IEnumeration) type, null);
		}
		if (type instanceof ITypedef) {
			return addBinding((ITypedef) type, null);
		}
		if (type instanceof ICPPReferenceType) {
			return new PDOMCPPReferenceType(pdom, parent, (ICPPReferenceType) type);
		}
		if (type instanceof ICPPPointerToMemberType) {
			return new PDOMCPPPointerToMemberType(pdom, parent,	(ICPPPointerToMemberType) type);
		}
		if (type instanceof ICPPTemplateTypeParameter) {
			return addBinding((ICPPTemplateTypeParameter) type, null);
		}

		return super.addType(parent, type); 
	}

	private void handlePostProcesses() {
		while (!postProcesses.isEmpty()) {
			postProcesses.removeFirst().run();
		}
	}
	
	@Override
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
		case CPP_USING_DECLARATION:
			return new PDOMCPPUsingDeclaration(pdom, record);
		case GPPBASICTYPE:
			return new PDOMGPPBasicType(pdom, record);
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
		case CPP_CLASS_INSTANCE:
			return new PDOMCPPClassInstance(pdom, record);
		case CPP_DEFERRED_CLASS_INSTANCE:
			return new PDOMCPPDeferredClassInstance(pdom, record);
		case CPP_UNKNOWN_CLASS_TYPE:
			return new PDOMCPPUnknownClassType(pdom, record);
		case CPP_UNKNOWN_CLASS_INSTANCE:
			return new PDOMCPPUnknownClassInstance(pdom, record);
		case CPP_TEMPLATE_TYPE_PARAMETER:
			return new PDOMCPPTemplateTypeParameter(pdom, record);
		case CPP_TEMPLATE_TEMPLATE_PARAMETER:
			return new PDOMCPPTemplateTemplateParameter(pdom, record);
		case CPP_TEMPLATE_NON_TYPE_PARAMETER:
			return new PDOMCPPTemplateNonTypeParameter(pdom, record);
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
		case CPP_PARAMETER_SPECIALIZATION:
			return new PDOMCPPParameterSpecialization(pdom, record);
		default:
			return super.getNode(record);
		}
	}

	@Override
	public IBTreeComparator getIndexComparator() {
		return new CPPFindBinding.CPPBindingBTreeComparator(pdom);
	}

	@Override
	public void onCreateName(PDOMFile file, IASTName name, PDOMName pdomName) throws CoreException {
		super.onCreateName(file, name, pdomName);
		
		IASTNode parentNode= name.getParent();
		IASTNode nameNode = name;
		if (name.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_NAME &&
				parentNode.getParent() instanceof ICPPASTQualifiedName) {
			nameNode = parentNode;
			parentNode = parentNode.getParent();
		}
		if (parentNode instanceof ICPPASTQualifiedName) {
		    if (nameNode != ((ICPPASTQualifiedName) parentNode).getLastName()) {
		    	return;
		    }
	    	parentNode = parentNode.getParent();
		}
		if (parentNode instanceof ICPPASTBaseSpecifier) {
			PDOMName derivedClassName= (PDOMName) pdomName.getEnclosingDefinition();
			if (derivedClassName != null) {
				ICPPASTBaseSpecifier baseNode= (ICPPASTBaseSpecifier) parentNode;
				PDOMBinding derivedClassBinding= derivedClassName.getBinding();
				if (derivedClassBinding instanceof PDOMCPPClassType) {
					PDOMCPPClassType ownerClass = (PDOMCPPClassType) derivedClassBinding;
					PDOMCPPBase pdomBase = new PDOMCPPBase(pdom, pdomName, baseNode.isVirtual(),
							baseNode.getVisibility());
					ownerClass.addBase(pdomBase);
					pdomName.setIsBaseSpecifier(true);
				} else if (derivedClassBinding instanceof PDOMCPPClassSpecialization) {
					PDOMCPPClassSpecialization ownerClass = (PDOMCPPClassSpecialization) derivedClassBinding;
					PDOMCPPBase pdomBase = new PDOMCPPBase(pdom, pdomName, baseNode.isVirtual(),
							baseNode.getVisibility());
					ownerClass.addBase(pdomBase);
					pdomName.setIsBaseSpecifier(true);
				}
			}
		}
		else if (parentNode instanceof ICPPASTUsingDirective) {
			IScope container= CPPVisitor.getContainingScope(name);
			try {
				boolean doit= false;
				PDOMCPPNamespace containerNS= null;
				
				IASTNode node= ASTInternal.getPhysicalNodeOfScope(container);
				if (node instanceof IASTTranslationUnit) {
					doit= true;
				}
				else if (node instanceof ICPPASTNamespaceDefinition) {
					ICPPASTNamespaceDefinition nsDef= (ICPPASTNamespaceDefinition) node;
					IASTName nsContainerName= nsDef.getName();
					if (nsContainerName != null) {
						PDOMBinding binding= adaptBinding(nsContainerName.resolveBinding());
						if (binding instanceof PDOMCPPNamespace) {
							containerNS= (PDOMCPPNamespace) binding;
							doit= true;
						}
					}
				}
				if (doit) {
					int rec= file.getFirstUsingDirectiveRec();
					PDOMCPPUsingDirective ud= new PDOMCPPUsingDirective(this, rec, containerNS,
							pdomName.getBinding());
					file.setFirstUsingDirectiveRec(ud.getRecord());
				}
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}
		}
		else if (parentNode instanceof ICPPASTElaboratedTypeSpecifier) {
			ICPPASTElaboratedTypeSpecifier elaboratedSpecifier = (ICPPASTElaboratedTypeSpecifier)parentNode;
			if (elaboratedSpecifier.isFriend()) {
				pdomName.setIsFriendSpecifier(true);
				PDOMName enclClassName= (PDOMName) pdomName.getEnclosingDefinition();
				PDOMBinding enclClassBinding= enclClassName.getBinding();
				if (enclClassBinding instanceof PDOMCPPClassType) {
					((PDOMCPPClassType)enclClassBinding).addFriend(new PDOMCPPFriend(pdom, pdomName));
				}
			}
		}
		else if (parentNode instanceof ICPPASTFunctionDeclarator) {			
			if (parentNode.getParent() instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration grandparentNode = (IASTSimpleDeclaration) parentNode.getParent();
				if (grandparentNode.getDeclSpecifier() instanceof ICPPASTDeclSpecifier) {
					if (((ICPPASTDeclSpecifier)grandparentNode.getDeclSpecifier()).isFriend()) {
						pdomName.setIsFriendSpecifier(true);
						PDOMName enclClassName= (PDOMName) pdomName.getEnclosingDefinition();
						PDOMBinding enclClassBinding= enclClassName.getBinding();
						if (enclClassBinding instanceof PDOMCPPClassType) {
							((PDOMCPPClassType)enclClassBinding).addFriend(new PDOMCPPFriend(pdom, pdomName));
						}
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage#getUsingDirectives()
	 */
	@Override
	public ICPPUsingDirective[] getUsingDirectives(PDOMFile file) throws CoreException {
		int rec= file.getFirstUsingDirectiveRec();
		if (rec == 0) {
			return ICPPUsingDirective.EMPTY_ARRAY;
		}
		LinkedList<ICPPUsingDirective> uds= new LinkedList<ICPPUsingDirective>();
		do {
			PDOMCPPUsingDirective ud= new PDOMCPPUsingDirective(this, rec);
			uds.addFirst(ud);
			rec= ud.getPreviousRec();
		}
		while (rec != 0);
		return uds.toArray(new ICPPUsingDirective[uds.size()]);
	}

	@Override
	public void onDeleteName(PDOMName pdomName) throws CoreException {
		super.onDeleteName(pdomName);
		
		if (pdomName.isBaseSpecifier()) {
			PDOMName derivedClassName= (PDOMName) pdomName.getEnclosingDefinition();
			if (derivedClassName != null) {
				PDOMBinding derivedClassBinding= derivedClassName.getBinding();
				if (derivedClassBinding instanceof PDOMCPPClassType) {
					PDOMCPPClassType ownerClass = (PDOMCPPClassType)derivedClassBinding;
					ownerClass.removeBase(pdomName);
				} else if (derivedClassBinding instanceof PDOMCPPClassSpecialization) {
					PDOMCPPClassSpecialization ownerClass = (PDOMCPPClassSpecialization)derivedClassBinding;
					ownerClass.removeBase(pdomName);
				}
			}
		}
		if (pdomName.isFriendSpecifier()) {
			PDOMName enclClassName= (PDOMName) pdomName.getEnclosingDefinition();
			PDOMBinding enclClassBinding= enclClassName.getBinding();
			if (enclClassBinding instanceof PDOMCPPClassType) {
				PDOMCPPClassType ownerClass = (PDOMCPPClassType)enclClassBinding;
				ownerClass.removeFriend(pdomName);
			}
		}
	}

	@Override
	protected PDOMFile getLocalToFile(IBinding binding) throws CoreException {
		if (pdom instanceof WritablePDOM) {
			final WritablePDOM wpdom= (WritablePDOM) pdom;
			PDOMFile file= null;
			if (binding instanceof ICPPUsingDeclaration) {
				String path= ASTInternal.getDeclaredInOneFileOnly(binding);
				if (path != null) {
					file= wpdom.getFileForASTPath(getLinkageID(), path);
				}
			} else if (binding instanceof ICPPNamespaceAlias) {
				String path= ASTInternal.getDeclaredInSourceFileOnly(binding, false);
				if (path != null) {
					file= wpdom.getFileForASTPath(getLinkageID(), path);
				}
			}
			if (file == null && !(binding instanceof IIndexBinding)) {
				try {
					IBinding owner= binding.getOwner();
					if (owner instanceof ICPPNamespace) {
						if (owner.getNameCharArray().length == 0) {
							String path= ASTInternal.getDeclaredInSourceFileOnly(owner, false);
							if (path != null) {
								file= wpdom.getFileForASTPath(getLinkageID(), path);
							}
						}
					}
				} catch (DOMException e) {
				}
			}
			if (file != null) {
				return file;
			}
		} 
		if (binding instanceof ICPPMember) {
			return null;
		}
		return super.getLocalToFile(binding);
	}
}
