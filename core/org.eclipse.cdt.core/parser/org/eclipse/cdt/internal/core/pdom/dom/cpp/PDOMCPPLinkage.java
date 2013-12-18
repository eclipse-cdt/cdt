/*******************************************************************************
 * Copyright (c) 2005, 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.ast.tag.TagManager;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPAliasTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArrayType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClosureType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameterPackType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnaryTypeTransformation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownMember;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinary;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinaryTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalComma;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalCompound;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalConditional;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFunctionCall;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFunctionSet;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalID;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalInitList;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalMemberAccess;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalParameterPack;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUnary;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUnaryTypeID;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeOfDependentExpression;
import org.eclipse.cdt.internal.core.index.IIndexBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.composite.CompositeIndexBinding;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.TypeMarshalBuffer;
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

	private final static int FIRST_NAMESPACE_CHILD_OFFSET= PDOMLinkage.RECORD_SIZE;

	@SuppressWarnings("hiding")
	private final static int RECORD_SIZE= FIRST_NAMESPACE_CHILD_OFFSET + Database.PTR_SIZE;

	// Only used when writing to database, which is single-threaded
	private final LinkedList<Runnable> postProcesses = new LinkedList<Runnable>();

	public PDOMCPPLinkage(PDOM pdom, long record) {
		super(pdom, record);
	}

	public PDOMCPPLinkage(PDOM pdom) throws CoreException {
		super(pdom, CPP_LINKAGE_NAME, CPP_LINKAGE_NAME.toCharArray());
	}

	@Override
	public String getLinkageName() {
		return CPP_LINKAGE_NAME;
	}

	@Override
	public int getLinkageID() {
		return CPP_LINKAGE_ID;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexBindingConstants.LINKAGE;
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

		@Override
		public void run() {
			for (int i = 0; i < fOriginal.length; i++) {
				final IPDOMCPPTemplateParameter tp = fPersisted[i];
				if (tp != null)
					tp.configure(fOriginal[i]);
			}
		}
	}

	class ConfigurePartialSpecialization implements Runnable {
		IPDOMPartialSpecialization partial;
		ICPPClassTemplatePartialSpecialization binding;

		public ConfigurePartialSpecialization(IPDOMPartialSpecialization partial,
				ICPPClassTemplatePartialSpecialization binding) {
			this.partial = partial;
			this.binding = binding;
			postProcesses.add(this);
		}

		@Override
		public void run() {
			try {
				ICPPTemplateArgument[] args = binding.getTemplateArguments();
				partial.setArguments(args);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			} finally {
				partial = null;
				binding = null;
			}
		}
	}

	class ConfigureFunction implements Runnable {
		private final PDOMCPPFunction fFunction;
		private final ICPPFunctionType fOriginalFunctionType;
		private final ICPPParameter[] fOriginalParameters;
		private final IType[] fOriginalExceptionSpec;
		private final ICPPEvaluation fReturnExpression;

		public ConfigureFunction(ICPPFunction original, PDOMCPPFunction function) throws DOMException {
			fFunction = function;
			fOriginalFunctionType= original.getType();
			fOriginalParameters= original.getParameters();
			fOriginalExceptionSpec= function.extractExceptionSpec(original);
			fReturnExpression= CPPFunction.getReturnExpression(original);
			postProcesses.add(this);
		}

		@Override
		public void run() {
			fFunction.initData(fOriginalFunctionType, fOriginalParameters, fOriginalExceptionSpec,
					fReturnExpression);
		}
	}
	
	class ConfigureFunctionSpecialization implements Runnable {
		private final PDOMCPPFunctionSpecialization fSpec; 
		private final ICPPEvaluation fReturnExpression;
		
		public ConfigureFunctionSpecialization(ICPPFunction original, PDOMCPPFunctionSpecialization spec) {
			fSpec = spec;
			fReturnExpression = CPPFunction.getReturnExpression(original);
			postProcesses.add(this);
		}
		
		@Override
		public void run() {
			fSpec.initData(fReturnExpression);
		}
	}

	class ConfigureFunctionTemplate implements Runnable {
		private final PDOMCPPFunctionTemplate fTemplate;
		private final IPDOMCPPTemplateParameter[] fTemplateParameters;
		private final ICPPTemplateParameter[] fOriginalTemplateParameters;
		private final ICPPFunctionType fOriginalFunctionType;
		private final ICPPParameter[] fOriginalParameters;
		private final IType[] fOriginalExceptionSpec;
		private final ICPPEvaluation fReturnExpression;

		public ConfigureFunctionTemplate(ICPPFunctionTemplate original, PDOMCPPFunctionTemplate template) throws DOMException {
			fTemplate = template;
			fTemplateParameters= template.getTemplateParameters();
			fOriginalTemplateParameters= original.getTemplateParameters();
			fOriginalFunctionType= original.getType();
			fOriginalParameters= original.getParameters();
			fOriginalExceptionSpec= template.extractExceptionSpec(original);
			fReturnExpression= CPPFunction.getReturnExpression(original);
			postProcesses.add(this);
		}

		@Override
		public void run() {
			for (int i = 0; i < fOriginalTemplateParameters.length; i++) {
				final IPDOMCPPTemplateParameter tp = fTemplateParameters[i];
				if (tp != null)
					tp.configure(fOriginalTemplateParameters[i]);
			}
			fTemplate.initData(fOriginalFunctionType, fOriginalParameters, fOriginalExceptionSpec,
					fReturnExpression);
		}
	}

	class ConfigureAliasTemplate implements Runnable {
		private final PDOMCPPAliasTemplate fTemplate;
		private final IPDOMCPPTemplateParameter[] fTemplateParameters;
		private final ICPPTemplateParameter[] fOriginalTemplateParameters;
		private final IType fOriginalAliasedType;

		public ConfigureAliasTemplate(ICPPAliasTemplate original, PDOMCPPAliasTemplate template) throws DOMException {
			fTemplate = template;
			fTemplateParameters= template.getTemplateParameters();
			fOriginalTemplateParameters= original.getTemplateParameters();
			fOriginalAliasedType= original.getType();
			postProcesses.add(this);
		}

		@Override
		public void run() {
			for (int i = 0; i < fOriginalTemplateParameters.length; i++) {
				final IPDOMCPPTemplateParameter tp = fTemplateParameters[i];
				if (tp != null)
					tp.configure(fOriginalTemplateParameters[i]);
			}
			fTemplate.initData(fOriginalAliasedType);
		}
	}

	/**
	 * Adds or returns existing binding for the given name.
	 */
	@Override
	public PDOMBinding addBinding(IASTName name) throws CoreException {
		if (name == null || name instanceof ICPPASTQualifiedName)
			return null;

		IBinding binding = name.resolveBinding();

		PDOMBinding pdomBinding = addBinding(binding, name);
		if (pdomBinding instanceof PDOMCPPClassType || pdomBinding instanceof PDOMCPPClassSpecialization) {
			if (binding instanceof ICPPClassType && name.isDefinition()) {
				addImplicitMethods(pdomBinding, (ICPPClassType) binding, name);
			}
		}

		handlePostProcesses();
		return pdomBinding;
	}

	/**
	 * Adds or returns existing binding for the given one. If {@code fromName} is not {@code null},
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
			// Assign names to anonymous types.
			IBinding binding= PDOMASTAdapter.getAdapterForAnonymousASTBinding(inputBinding);
			if (binding == null)
				return null;

			final PDOMNode parent= adaptOrAddParent(true, binding);
			if (parent == null)
				return null;

			long fileLocalRec[]= {0};
			pdomBinding = adaptBinding(parent, binding, fileLocalRec);
			if (pdomBinding != null) {
				getPDOM().putCachedResult(inputBinding, pdomBinding);
			} else {
				try {
					pdomBinding = createBinding(parent, binding, fileLocalRec[0]);
					if (pdomBinding != null) {
						getPDOM().putCachedResult(inputBinding, pdomBinding);
						if (inputBinding instanceof CPPClosureType) {
							addImplicitMethods(pdomBinding, (ICPPClassType) binding, fromName);
						}

						// Synchronize the tags associated with the persistent binding to match
						// the set that is associated with the input binding.
						TagManager.getInstance().syncTags(pdomBinding, inputBinding);
					}
				} catch (DOMException e) {
					throw new CoreException(Util.createStatus(e));
				}
				return pdomBinding;
			}
		}

		if (shouldUpdate(pdomBinding, fromName)) {
			IBinding fromBinding = fromName.getBinding();

			pdomBinding.update(this, fromBinding);

			// Update the tags based on the tags from the new binding.  This cannot be done in
			// PDOMBinding.update, because not all subclasses (e.g., PDOMCPPFunction) call
			// the superclass implementation.
			TagManager.getInstance().syncTags(pdomBinding, fromBinding);
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
			// Update opaque enums.
			if (pdomBinding instanceof ICPPEnumeration && fromName.isDeclaration()) {
				return true;
			}
			return !getPDOM().hasLastingDefinition(pdomBinding);
		}
		return false;
	}

	PDOMBinding createBinding(PDOMNode parent, IBinding binding, long fileLocalRec) throws CoreException, DOMException {
		PDOMBinding pdomBinding= null;
		PDOMNode parent2= null;

		// Template parameters are created directly by their owners.
		if (binding instanceof ICPPTemplateParameter)
			return null;
		if (binding instanceof ICPPUnknownBinding)
			return null;

		if (binding instanceof ICPPSpecialization) {
			IBinding specialized = ((ICPPSpecialization) binding).getSpecializedBinding();
			PDOMBinding pdomSpecialized= addBinding(specialized, null);
			if (pdomSpecialized == null)
				return null;

			pdomBinding = createSpecialization(parent, pdomSpecialized, binding);
		} else if (binding instanceof ICPPClassTemplatePartialSpecialization) {
			ICPPClassTemplate primary = ((ICPPClassTemplatePartialSpecialization) binding).getPrimaryClassTemplate();
			PDOMBinding pdomPrimary = addBinding(primary, null);
			if (pdomPrimary instanceof PDOMCPPClassTemplate) {
				pdomBinding = new PDOMCPPClassTemplatePartialSpecialization(
						this, parent, (ICPPClassTemplatePartialSpecialization) binding, (PDOMCPPClassTemplate) pdomPrimary);
			}
		} else if (binding instanceof ICPPField) {
			if (parent instanceof PDOMCPPClassType || parent instanceof PDOMCPPClassSpecialization) {
				pdomBinding = new PDOMCPPField(this, parent, (ICPPField) binding);
			}
		} else if (binding instanceof ICPPClassTemplate) {
			pdomBinding= new PDOMCPPClassTemplate(this, parent, (ICPPClassTemplate) binding);
		} else if (binding instanceof ICPPClassType) {
			pdomBinding= new PDOMCPPClassType(this, parent, (ICPPClassType) binding);
		} else if (binding instanceof ICPPVariable) {
			ICPPVariable var= (ICPPVariable) binding;
			pdomBinding = new PDOMCPPVariable(this, parent, var);
		} else if (binding instanceof ICPPFunctionTemplate) {
			if (binding instanceof ICPPConstructor) {
				pdomBinding= new PDOMCPPConstructorTemplate(this, parent, (ICPPConstructor) binding);
			} else if (binding instanceof ICPPMethod) {
				pdomBinding= new PDOMCPPMethodTemplate(this, parent, (ICPPMethod) binding);
			} else if (binding instanceof ICPPFunction) {
				pdomBinding= new PDOMCPPFunctionTemplate(this, parent, (ICPPFunctionTemplate) binding);
			}
		} else if (binding instanceof ICPPConstructor) {
			if (parent instanceof PDOMCPPClassType || parent instanceof PDOMCPPClassSpecialization) {
				pdomBinding = new PDOMCPPConstructor(this, parent, (ICPPConstructor) binding);
			}
		} else if (binding instanceof ICPPMethod) {
			if (parent instanceof PDOMCPPClassType || parent instanceof PDOMCPPClassSpecialization) {
				pdomBinding = new PDOMCPPMethod(this, parent, (ICPPMethod) binding);
			}
		} else if (binding instanceof ICPPFunction) {
			pdomBinding = new PDOMCPPFunction(this, parent, (ICPPFunction) binding, true);
		} else if (binding instanceof ICPPNamespaceAlias) {
			pdomBinding = new PDOMCPPNamespaceAlias(this, parent, (ICPPNamespaceAlias) binding);
		} else if (binding instanceof ICPPNamespace) {
			pdomBinding = new PDOMCPPNamespace(this, parent, (ICPPNamespace) binding);
		} else if (binding instanceof ICPPUsingDeclaration) {
			pdomBinding = new PDOMCPPUsingDeclaration(this, parent, (ICPPUsingDeclaration) binding);
		} else if (binding instanceof ICPPEnumeration) {
			pdomBinding = new PDOMCPPEnumeration(this, parent, (ICPPEnumeration) binding);
		} else if (binding instanceof IEnumerator) {
			assert parent instanceof ICPPEnumeration;
			pdomBinding = new PDOMCPPEnumerator(this, parent, (IEnumerator) binding);
			if (parent instanceof ICPPEnumeration && !((ICPPEnumeration) parent).isScoped()) {
				parent2= parent.getParentNode();
				if (parent2 == null) {
					parent2= this;
				}
			}
		} else if (binding instanceof ITypedef) {
			pdomBinding = new PDOMCPPTypedef(this, parent, (ITypedef) binding);
		} else if (binding instanceof ICPPAliasTemplate) {
			pdomBinding = new PDOMCPPAliasTemplate(this, parent, (ICPPAliasTemplate) binding);
		}

		if (pdomBinding != null) {
			pdomBinding.setLocalToFileRec(fileLocalRec);
			addChild(parent, pdomBinding, binding);
			if (parent2 != null) {
				addChild(parent2, pdomBinding, binding);
			}
			if (parent != this && parent2 != this) {
				insertIntoNestedBindingsIndex(pdomBinding);
			}
		}

		return pdomBinding;
	}

	/**
	 * Returns visibility of the member binding in its containing class, or -1 if the binding is
	 * not a class member.
	 */
	private static int getVisibility(IBinding binding) {
        while (binding instanceof ICPPSpecialization) {
            binding = ((ICPPSpecialization) binding).getSpecializedBinding();
        }
        if (binding instanceof ICPPClassTemplatePartialSpecialization) {
        	// A class template partial specialization inherits the visibility of its primary class template. 
        	binding = ((ICPPClassTemplatePartialSpecialization) binding).getPrimaryClassTemplate();
        }
        if (binding instanceof ICPPAliasTemplateInstance) {
        	binding = ((ICPPAliasTemplateInstance) binding).getTemplateDefinition();
        }
		if (binding instanceof CPPImplicitMethod)
			return ICPPClassType.v_public;

		int visibility = -1;

		IBinding bindingOwner = binding.getOwner();

		if (bindingOwner instanceof ICPPClassType) {
			if (bindingOwner instanceof CPPClosureType)
				return ICPPClassType.v_public;
			binding = PDOMASTAdapter.getOriginalForAdaptedBinding(binding);
			visibility = ((ICPPClassType) bindingOwner).getVisibility(binding);
		}
		return visibility;
	}

	private static void addChild(PDOMNode parent, PDOMBinding binding, IBinding originalBinding)
			throws CoreException {
		if (parent instanceof IPDOMCPPClassType) {
			if (originalBinding instanceof IEnumerator)
				originalBinding = originalBinding.getOwner();
			try {
				int visibility = getVisibility(originalBinding);
				if (visibility >= 0) {
					((IPDOMCPPClassType) parent).addMember(binding, visibility);
					return;
				}
			} catch (IllegalArgumentException e) {
				CCorePlugin.log(e);
				return;
			}
		}
		parent.addChild(binding);
	}

	@Override
	public void addChild(PDOMNode node) throws CoreException {
		super.addChild(node);
		if (node instanceof PDOMCPPNamespace) {
			((PDOMCPPNamespace) node).addToList(record + FIRST_NAMESPACE_CHILD_OFFSET);
		}
	}

	private PDOMBinding createSpecialization(PDOMNode parent, PDOMBinding orig, IBinding special)
			throws CoreException, DOMException {
		PDOMBinding result= null;
		if (special instanceof ICPPTemplateInstance) {
			if (special instanceof ICPPConstructor && orig instanceof ICPPConstructor) {
				result= new PDOMCPPConstructorInstance(this, parent, (ICPPConstructor) special, orig);
			} else if (special instanceof ICPPMethod && orig instanceof ICPPMethod) {
				result= new PDOMCPPMethodInstance(this, parent, (ICPPMethod) special, orig);
			} else if (special instanceof ICPPFunction && orig instanceof ICPPFunction) {
				result= new PDOMCPPFunctionInstance(this, parent, (ICPPFunction) special, orig);
			} else if (special instanceof ICPPClassType && orig instanceof ICPPClassType) {
				result= new PDOMCPPClassInstance(this, parent, (ICPPClassType) special, orig);
			}
		} else if (special instanceof ICPPField) {
			result= new PDOMCPPFieldSpecialization(this, parent, (ICPPField) special, orig);
		} else if (special instanceof ICPPFunctionTemplate) {
			if (special instanceof ICPPConstructor) {
				result= new PDOMCPPConstructorTemplateSpecialization(this, parent, (ICPPConstructor) special, orig);
			} else if (special instanceof ICPPMethod) {
				result= new PDOMCPPMethodTemplateSpecialization(this, parent, (ICPPMethod) special, orig);
			} else if (special instanceof ICPPFunction) {
				result= new PDOMCPPFunctionTemplateSpecialization(this, parent, (ICPPFunctionTemplate) special, orig);
			}
		} else if (special instanceof ICPPClassTemplatePartialSpecialization) {
			ICPPClassTemplatePartialSpecialization partialSpecSpec = (ICPPClassTemplatePartialSpecialization) special;
			ICPPClassTemplate primarySpec = partialSpecSpec.getPrimaryClassTemplate();
			PDOMBinding pdomPrimarySpec = addBinding(primarySpec, null);
			if (pdomPrimarySpec instanceof PDOMCPPClassTemplateSpecialization) {
				result= new PDOMCPPClassTemplatePartialSpecializationSpecialization(this, parent, orig, 
						partialSpecSpec, (PDOMCPPClassTemplateSpecialization) pdomPrimarySpec);
			}
		} else if (special instanceof ICPPConstructor) {
			result= new PDOMCPPConstructorSpecialization(this, parent, (ICPPConstructor) special, orig);
		} else if (special instanceof ICPPMethod) {
			result= new PDOMCPPMethodSpecialization(this, parent, (ICPPMethod) special, orig);
		} else if (special instanceof ICPPFunction) {
			result= new PDOMCPPFunctionSpecialization(this, parent, (ICPPFunction) special, orig);
		} else if (special instanceof ICPPClassTemplate) {
			result= new PDOMCPPClassTemplateSpecialization(this, parent, (ICPPClassTemplate) special, orig);
		} else if (special instanceof ICPPClassType) {
			result= new PDOMCPPClassSpecialization(this, parent, (ICPPClassType) special, orig);
		} else if (special instanceof ITypedef) {
			result= new PDOMCPPTypedefSpecialization(this, parent, (ITypedef) special, orig);
		} else if (special instanceof ICPPUsingDeclaration) {
			result= new PDOMCPPUsingDeclarationSpecialization(this, parent, (ICPPUsingDeclaration) special, orig);
		} else if (special instanceof ICPPEnumeration) {
			result= new PDOMCPPEnumerationSpecialization(this, parent, (ICPPEnumeration) special, orig);
		} else if (special instanceof IEnumerator) {
			result= new PDOMCPPEnumeratorSpecialization(this, parent, (IEnumerator) special, orig);
		}

		return result;
	}

	private void addImplicitMethods(PDOMBinding type, ICPPClassType binding, IASTNode point) throws CoreException {
		try {
			final long fileLocalRec= type.getLocalToFileRec();
			IScope scope = binding.getCompositeScope();
			if (scope instanceof ICPPClassScope) {
				List<ICPPMethod> old= new ArrayList<ICPPMethod>();
				if (type instanceof ICPPClassType) {
					ArrayUtil.addAll(old, ClassTypeHelper.getImplicitMethods((ICPPClassType) type, point));
				}
				ICPPMethod[] implicit= ((ICPPClassScope) scope).getImplicitMethods();
				for (ICPPMethod method : implicit) {
					if (!(method instanceof IProblemBinding)) {
						PDOMBinding pdomBinding= adaptBinding(method);
						if (pdomBinding == null) {
							pdomBinding = createBinding(type, method, fileLocalRec);
						} else if (!getPDOM().hasLastingDefinition(pdomBinding)) {
							pdomBinding.update(this, method);
							old.remove(pdomBinding);

							// Update the tags based on the tags from the new binding.  This was in
							// PDOMBinding.update, but not all subclasses (e.g., PDOMCPPFunction)
							// call the parent implementation.
							TagManager.getInstance().syncTags(pdomBinding, method);
						}
					}
				}
				for (ICPPMethod method : old) {
					if (method instanceof PDOMBinding)
						((PDOMBinding) method).update(this, null);
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
					return 0;
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
				return CPP_CLASS_TEMPLATE_PARTIAL_SPEC_SPEC;
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
		} else if (binding instanceof ICPPClassTemplatePartialSpecialization) {
			return CPP_CLASS_TEMPLATE_PARTIAL_SPEC;
		} else if (binding instanceof ICPPTemplateParameter) {
			if (binding instanceof ICPPTemplateTypeParameter) {
				return CPP_TEMPLATE_TYPE_PARAMETER;
			} else if (binding instanceof ICPPTemplateTemplateParameter)
				return CPP_TEMPLATE_TEMPLATE_PARAMETER;
			else if (binding instanceof ICPPTemplateNonTypeParameter)
				return CPP_TEMPLATE_NON_TYPE_PARAMETER;
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
		} else if (binding instanceof ICPPFunction) {
			return CPPFUNCTION;
		} else if (binding instanceof ICPPUnknownBinding) {
			return 0;
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
		} else if (binding instanceof ICPPAliasTemplate) {
			return CPP_TEMPLATE_ALIAS;
		}

		return 0;
	}

	@Override
	protected boolean cannotAdapt(final IBinding inputBinding) throws CoreException {
		return super.cannotAdapt(inputBinding) || inputBinding instanceof ICPPAliasTemplateInstance;
	}

	@Override
	public final PDOMBinding adaptBinding(final IBinding inputBinding, boolean includeLocal) throws CoreException {
		return adaptBinding(null, inputBinding, includeLocal ? FILE_LOCAL_REC_DUMMY : null);
	}

	private final PDOMBinding adaptBinding(final PDOMNode parent, IBinding inputBinding,
			long[] fileLocalRecHolder) throws CoreException {
		if (cannotAdapt(inputBinding)) {
			return null;
		}

		PDOMBinding result= attemptFastAdaptBinding(inputBinding);
		if (result != null) {
			return result;
		}

		// Assign names to anonymous types.
		IBinding binding= PDOMASTAdapter.getAdapterForAnonymousASTBinding(inputBinding);
		if (binding == null) {
			return null;
		}

		result= doAdaptBinding(parent, binding, fileLocalRecHolder);
		if (result != null) {
			getPDOM().putCachedResult(inputBinding, result);
		}
		return result;
	}

	@Override
	public PDOMCPPNamespace[] getInlineNamespaces() {
		final Long key = record + CACHE_BASES;
		PDOMCPPNamespace[] result= (PDOMCPPNamespace[]) getPDOM().getCachedResult(key);
		if (result == null) {
			List<PDOMCPPNamespace> nslist = PDOMCPPNamespace.collectInlineNamespaces(getDB(),
					getLinkage(), record + FIRST_NAMESPACE_CHILD_OFFSET);
			if (nslist == null) {
				result= new PDOMCPPNamespace[0];
			} else {
				result= nslist.toArray(new PDOMCPPNamespace[nslist.size()]);
			}
			getPDOM().putCachedResult(key, result, true);
		}
		return result;
	}

	/**
	 * Finds the equivalent binding, or binding placeholder within this PDOM.
	 */
	private final PDOMBinding doAdaptBinding(PDOMNode parent, IBinding binding, long[] fileLocalRecHolder)
			throws CoreException {
		if (parent == null) {
			parent= adaptOrAddParent(false, binding);
		}
		if (parent == this) {
			PDOMBinding glob= CPPFindBinding.findBinding(getIndex(), this, binding, 0);
			if (fileLocalRecHolder == null)
				return glob;
			final long loc= getLocalToFileRec(parent, binding, glob);
			if (loc == 0)
				return glob;
			fileLocalRecHolder[0]= loc;
			return CPPFindBinding.findBinding(getIndex(), this, binding, loc);
		}
		if (parent instanceof PDOMCPPNamespace) {
			final BTree btree = ((PDOMCPPNamespace) parent).getIndex();
			PDOMBinding glob= CPPFindBinding.findBinding(btree, this, binding, 0);
			if (fileLocalRecHolder == null)
				return glob;
			final long loc= getLocalToFileRec(parent, binding, glob);
			if (loc == 0)
				return glob;
			fileLocalRecHolder[0]= loc;
			return CPPFindBinding.findBinding(btree, this, binding,	loc);
		}
		if (binding instanceof ICPPTemplateParameter && parent instanceof IPDOMCPPTemplateParameterOwner) {
			return (PDOMBinding) ((IPDOMCPPTemplateParameterOwner) parent).adaptTemplateParameter(
					(ICPPTemplateParameter) binding);
		}
		if (parent instanceof IPDOMMemberOwner) {
			PDOMBinding glob= CPPFindBinding.findBinding(parent, this, binding, 0);
			final long loc= getLocalToFileRec(parent, binding, glob);
			if (loc == 0)
				return glob;
			fileLocalRecHolder[0]= loc;
			return CPPFindBinding.findBinding(parent, this, binding, loc);
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
 		IBinding owner= binding.getOwner();
		if (owner instanceof IFunction && !(binding instanceof ICPPTemplateParameter)) {
			return null;
		}

		if (binding instanceof IIndexBinding) {
			IIndexBinding ib= (IIndexBinding) binding;
			// Don't adapt file local bindings from other fragments to this one.
			if (ib.isFileLocal()) {
				return null;
			}
		}

		if (owner == null)
			return this;

		return adaptOrAddBinding(add, owner);
 	}

	private PDOMBinding adaptOrAddBinding(boolean add, IBinding binding) throws CoreException {
		if (add) {
			return addBinding(binding, null);
		}
		return adaptBinding(binding);
	}

	private void handlePostProcesses() {
		while (!postProcesses.isEmpty()) {
			postProcesses.removeFirst().run();
		}
	}

	@Override
	public PDOMNode getNode(long record, int nodeType) throws CoreException {
		switch (nodeType) {
		case CPPVARIABLE:
			return new PDOMCPPVariable(this, record);
		case CPPFUNCTION:
			return new PDOMCPPFunction(this, record);
		case CPPCLASSTYPE:
			return new PDOMCPPClassType(this, record);
		case CPPFIELD:
			return new PDOMCPPField(this, record);
		case CPP_CONSTRUCTOR:
			return new PDOMCPPConstructor(this, record);
		case CPPMETHOD:
			return new PDOMCPPMethod(this, record);
		case CPPNAMESPACE:
			return new PDOMCPPNamespace(this, record);
		case CPPNAMESPACEALIAS:
			return new PDOMCPPNamespaceAlias(this, record);
		case CPP_USING_DECLARATION:
			return new PDOMCPPUsingDeclaration(this, record);
		case CPPENUMERATION:
			return new PDOMCPPEnumeration(this, record);
		case CPPENUMERATOR:
			return new PDOMCPPEnumerator(this, record);
		case CPPTYPEDEF:
			return new PDOMCPPTypedef(this, record);
		case CPP_FUNCTION_TEMPLATE:
			return new PDOMCPPFunctionTemplate(this, record);
		case CPP_METHOD_TEMPLATE:
			return new PDOMCPPMethodTemplate(this, record);
		case CPP_CONSTRUCTOR_TEMPLATE:
			return new PDOMCPPConstructorTemplate(this, record);
		case CPP_CLASS_TEMPLATE:
			return new PDOMCPPClassTemplate(this, record);
		case CPP_CLASS_TEMPLATE_PARTIAL_SPEC:
			return new PDOMCPPClassTemplatePartialSpecialization(this, record);
		case CPP_CLASS_TEMPLATE_PARTIAL_SPEC_SPEC:
			return new PDOMCPPClassTemplatePartialSpecializationSpecialization(this, record);
		case CPP_FUNCTION_INSTANCE:
			return new PDOMCPPFunctionInstance(this, record);
		case CPP_METHOD_INSTANCE:
			return new PDOMCPPMethodInstance(this, record);
		case CPP_CONSTRUCTOR_INSTANCE:
			return new PDOMCPPConstructorInstance(this, record);
		case CPP_CLASS_INSTANCE:
			return new PDOMCPPClassInstance(this, record);
		case CPP_TEMPLATE_TYPE_PARAMETER:
			return new PDOMCPPTemplateTypeParameter(this, record);
		case CPP_TEMPLATE_TEMPLATE_PARAMETER:
			return new PDOMCPPTemplateTemplateParameter(this, record);
		case CPP_TEMPLATE_NON_TYPE_PARAMETER:
			return new PDOMCPPTemplateNonTypeParameter(this, record);
		case CPP_FIELD_SPECIALIZATION:
			return new PDOMCPPFieldSpecialization(this, record);
		case CPP_FUNCTION_SPECIALIZATION:
			return new PDOMCPPFunctionSpecialization(this, record);
		case CPP_METHOD_SPECIALIZATION:
			return new PDOMCPPMethodSpecialization(this, record);
		case CPP_CONSTRUCTOR_SPECIALIZATION:
			return new PDOMCPPConstructorSpecialization(this, record);
		case CPP_CLASS_SPECIALIZATION:
			return new PDOMCPPClassSpecialization(this, record);
		case CPP_FUNCTION_TEMPLATE_SPECIALIZATION:
			return new PDOMCPPFunctionTemplateSpecialization(this, record);
		case CPP_METHOD_TEMPLATE_SPECIALIZATION:
			return new PDOMCPPMethodTemplateSpecialization(this, record);
		case CPP_CONSTRUCTOR_TEMPLATE_SPECIALIZATION:
			return new PDOMCPPConstructorTemplateSpecialization(this, record);
		case CPP_CLASS_TEMPLATE_SPECIALIZATION:
			return new PDOMCPPClassTemplateSpecialization(this, record);
		case CPP_TYPEDEF_SPECIALIZATION:
			return new PDOMCPPTypedefSpecialization(this, record);
		case CPP_USING_DECLARATION_SPECIALIZATION:
			return new PDOMCPPUsingDeclarationSpecialization(this, record);
		case CPP_TEMPLATE_ALIAS:
			return new PDOMCPPAliasTemplate(this, record);
		case CPP_ENUMERATION_SPECIALIZATION:
			return new PDOMCPPEnumerationSpecialization(this, record);
		case CPP_ENUMERATOR_SPECIALIZATION:
			return new PDOMCPPEnumeratorSpecialization(this, record);
		}
		assert false : "nodeid= " + nodeType; //$NON-NLS-1$
		return null;
	}

	@Override
	public IBTreeComparator getIndexComparator() {
		return new CPPFindBinding.CPPBindingBTreeComparator(this);
	}

	@Override
	public void onCreateName(PDOMFile file, IASTName name, PDOMName pdomName) throws CoreException {
		super.onCreateName(file, name, pdomName);

		IASTNode parentNode= name.getParent();
		if (parentNode instanceof ICPPASTQualifiedName) {
			if (name != ((ICPPASTQualifiedName) parentNode).getLastName())
				return;
	    	parentNode = parentNode.getParent();
		}
		if (name.getPropertyInParent() == ICPPASTBaseSpecifier.NAME ||
				(name.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_NAME &&
				parentNode.getPropertyInParent() == ICPPASTBaseSpecifier.NAME)) {
			pdomName.setIsBaseSpecifier();
		} else if (parentNode instanceof ICPPASTUsingDirective) {
			IASTNode parent= name.getParent();
			if (parent instanceof ICPPASTQualifiedName) {
				name = (IASTName) parent;
			}
			IScope container= CPPVisitor.getContainingScope(name);
			boolean doit= false;
			PDOMCPPNamespace containerNS= null;

			IASTNode node= ASTInternal.getPhysicalNodeOfScope(container);
			if (node instanceof IASTTranslationUnit) {
				doit= true;
			} else if (node instanceof ICPPASTNamespaceDefinition) {
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
				long rec= file.getLastUsingDirectiveRec();
				PDOMCPPUsingDirective ud= new PDOMCPPUsingDirective(this, rec, containerNS,
						pdomName.getBinding(), pdomName.getFileLocation().getNodeOffset());
				file.setLastUsingDirective(ud.getRecord());
			}
		} else if (parentNode instanceof ICPPASTElaboratedTypeSpecifier) {
			ICPPASTElaboratedTypeSpecifier elaboratedSpecifier = (ICPPASTElaboratedTypeSpecifier) parentNode;
			if (elaboratedSpecifier.isFriend()) {
				pdomName.setIsFriendSpecifier();
				PDOMName enclClassName = (PDOMName) pdomName.getEnclosingDefinition();
				if (enclClassName != null) {
					PDOMBinding enclClassBinding = enclClassName.getBinding();
					if (enclClassBinding instanceof PDOMCPPClassType) {
						((PDOMCPPClassType) enclClassBinding).addFriend(new PDOMCPPFriend(this, pdomName));
					}
				}
			}
		} else if (parentNode instanceof ICPPASTFunctionDeclarator) {
			IASTDeclSpecifier declSpec = null;
			if (parentNode.getParent() instanceof IASTSimpleDeclaration) {
				declSpec = ((IASTSimpleDeclaration) parentNode.getParent()).getDeclSpecifier();
			} else if (parentNode.getParent() instanceof IASTFunctionDefinition) {
				declSpec = ((IASTFunctionDefinition) parentNode.getParent()).getDeclSpecifier();
			}
			if (declSpec instanceof ICPPASTDeclSpecifier) {
				if (((ICPPASTDeclSpecifier) declSpec).isFriend()) {
					pdomName.setIsFriendSpecifier();
					PDOMName enclClassName = (PDOMName) pdomName.getEnclosingDefinition();
					if (enclClassName != null) {
						PDOMBinding enclClassBinding = enclClassName.getBinding();
						if (enclClassBinding instanceof PDOMCPPClassType) {
							((PDOMCPPClassType) enclClassBinding).addFriend(new PDOMCPPFriend(this,	pdomName));
						}
					}
				}
			}
		} else if (parentNode instanceof ICPPASTNamespaceDefinition) {
			ICPPASTNamespaceDefinition nsdef= (ICPPASTNamespaceDefinition) parentNode;
			if (nsdef.isInline()) {
				pdomName.setIsInlineNamespace();
			}
		} else if (parentNode instanceof ICPPASTCompositeTypeSpecifier) {
			IBinding classBinding = name.resolveBinding();
			if (classBinding instanceof ICPPClassType) {
				ICPPBase[] bases;
				if (classBinding instanceof ICPPClassSpecialization) {
					bases= ((ICPPClassSpecialization) classBinding).getBases(name);
				} else {
					bases= ((ICPPClassType) classBinding).getBases();
				}
				if (bases.length > 0) {
					PDOMBinding pdomBinding = pdomName.getBinding();
					if (pdomBinding instanceof PDOMCPPClassType) {
						((PDOMCPPClassType) pdomBinding).addBases(pdomName, bases);
					} else if (pdomBinding instanceof PDOMCPPClassSpecialization) {
						((PDOMCPPClassSpecialization) pdomBinding).addBases(pdomName, bases);
					}
				}
			}
		}
	}

	@Override
	public ICPPUsingDirective[] getUsingDirectives(PDOMFile file) throws CoreException {
		long rec= file.getLastUsingDirectiveRec();
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
		if (pdomName.isFriendSpecifier()) {
			PDOMName enclClassName = (PDOMName) pdomName.getEnclosingDefinition();
			if (enclClassName != null) {
				PDOMBinding enclClassBinding = enclClassName.getBinding();
				if (enclClassBinding instanceof PDOMCPPClassType) {
					PDOMCPPClassType ownerClass = (PDOMCPPClassType) enclClassBinding;
					ownerClass.removeFriend(pdomName);
				}
			}
		} else if (pdomName.isDefinition()) {
			PDOMBinding binding = pdomName.getBinding();
			if (binding instanceof PDOMCPPClassType) {
				((PDOMCPPClassType) binding).removeBases(pdomName);
			} else if (binding instanceof PDOMCPPClassSpecialization) {
				((PDOMCPPClassSpecialization) binding).removeBases(pdomName);
			}
		}
	}

	@Override
	protected PDOMFile getLocalToFile(IBinding binding, PDOMBinding glob) throws CoreException {
		PDOM pdom = getPDOM();
		if (pdom instanceof WritablePDOM) {
			final WritablePDOM wpdom= (WritablePDOM) pdom;
			PDOMFile file= null;
			if (binding instanceof ICPPUsingDeclaration) {
				IASTNode node= ASTInternal.getDeclaredInOneFileOnly(binding);
				if (node != null) {
					file= wpdom.getFileForASTNode(getLinkageID(), node);
				}
			} else if (binding instanceof ICPPNamespaceAlias) {
				IASTNode node= ASTInternal.getDeclaredInSourceFileOnly(getPDOM(), binding, false, glob);
				if (node != null) {
					file= wpdom.getFileForASTNode(getLinkageID(), node);
				}
			}
			if (file == null && !(binding instanceof IIndexBinding)) {
				IBinding owner= binding.getOwner();
				if (owner instanceof ICPPNamespace) {
					if (owner.getNameCharArray().length == 0) {
						IASTNode node= ASTInternal.getDeclaredInSourceFileOnly(getPDOM(), binding, false, glob);
						if (node != null) {
							file= wpdom.getFileForASTNode(getLinkageID(), node);
						}
					}
				}
			}
			if (file != null) {
				return file;
			}
		}
		if (binding instanceof ICPPMember) {
			return null;
		}
		return super.getLocalToFile(binding, glob);
	}

	@Override
	public PDOMBinding addTypeBinding(IBinding binding) throws CoreException {
		return addBinding(binding, null);
	}

	@Override
	public IType unmarshalType(ITypeMarshalBuffer buffer) throws CoreException {
		short firstBytes= buffer.getShort();
		switch ((firstBytes & ITypeMarshalBuffer.KIND_MASK)) {
		case ITypeMarshalBuffer.ARRAY_TYPE:
			return CPPArrayType.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.BASIC_TYPE:
			return CPPBasicType.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.CVQUALIFIER_TYPE:
			return CPPQualifierType.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.FUNCTION_TYPE:
			return CPPFunctionType.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.POINTER_TYPE:
			return CPPPointerType.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.PROBLEM_TYPE:
			return ProblemType.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.REFERENCE_TYPE:
			return CPPReferenceType.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.PACK_EXPANSION_TYPE:
			return CPPParameterPackType.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.POINTER_TO_MEMBER_TYPE:
			return CPPPointerToMemberType.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.DEPENDENT_EXPRESSION_TYPE:
			return TypeOfDependentExpression.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.UNKNOWN_MEMBER:
			IBinding binding= CPPUnknownMember.unmarshal(getPDOM(), firstBytes, buffer);
			if (binding instanceof IType)
				return (IType) binding;
			break;
		case ITypeMarshalBuffer.UNKNOWN_MEMBER_CLASS_INSTANCE:
			return CPPUnknownClassInstance.unmarshal(getPDOM(), firstBytes, buffer);
		case ITypeMarshalBuffer.DEFERRED_CLASS_INSTANCE:
			return CPPDeferredClassInstance.unmarshal(getPDOM(), firstBytes, buffer);
		case ITypeMarshalBuffer.ALIAS_TEMPLATE:
			return CPPAliasTemplateInstance.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.TYPE_TRANSFORMATION:
			return CPPUnaryTypeTransformation.unmarshal(firstBytes, buffer);
		}

		throw new CoreException(CCorePlugin.createStatus("Cannot unmarshal a type, first bytes=" + firstBytes)); //$NON-NLS-1$
	}

	@Override
	public IBinding unmarshalBinding(ITypeMarshalBuffer buffer) throws CoreException {
		short firstBytes= buffer.getShort();
		switch ((firstBytes & ITypeMarshalBuffer.KIND_MASK)) {
		case ITypeMarshalBuffer.UNKNOWN_MEMBER:
			return CPPUnknownMember.unmarshal(getPDOM(), firstBytes, buffer);
		case ITypeMarshalBuffer.UNKNOWN_MEMBER_CLASS_INSTANCE:
			return CPPUnknownClassInstance.unmarshal(getPDOM(), firstBytes, buffer);
		case ITypeMarshalBuffer.DEFERRED_CLASS_INSTANCE:
			return CPPDeferredClassInstance.unmarshal(getPDOM(), firstBytes, buffer);
		}

		throw new CoreException(CCorePlugin.createStatus("Cannot unmarshal a type, first bytes=" + firstBytes)); //$NON-NLS-1$
	}

	@Override
	public ISerializableEvaluation unmarshalEvaluation(ITypeMarshalBuffer buffer) throws CoreException {
		short firstBytes= buffer.getShort();
		if (firstBytes == TypeMarshalBuffer.NULL_TYPE)
			return null;
		switch ((firstBytes & ITypeMarshalBuffer.KIND_MASK)) {
		case ITypeMarshalBuffer.EVAL_BINARY:
			return EvalBinary.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.EVAL_BINARY_TYPE_ID:
			return EvalBinaryTypeId.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.EVAL_BINDING:
			return EvalBinding.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.EVAL_COMMA:
			return EvalComma.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.EVAL_COMPOUND:
			return EvalCompound.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.EVAL_CONDITIONAL:
			return EvalConditional.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.EVAL_FIXED:
			return EvalFixed.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.EVAL_FUNCTION_CALL:
			return EvalFunctionCall.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.EVAL_FUNCTION_SET:
			return EvalFunctionSet.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.EVAL_ID:
			return EvalID.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.EVAL_INIT_LIST:
			return EvalInitList.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.EVAL_MEMBER_ACCESS:
			return EvalMemberAccess.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.EVAL_PARAMETER_PACK:
			return EvalParameterPack.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.EVAL_TYPE_ID:
			return EvalTypeId.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.EVAL_UNARY:
			return EvalUnary.unmarshal(firstBytes, buffer);
		case ITypeMarshalBuffer.EVAL_UNARY_TYPE_ID:
			return EvalUnaryTypeID.unmarshal(firstBytes, buffer);
		}
		throw new CoreException(CCorePlugin.createStatus("Cannot unmarshal an evaluation, first bytes=" + firstBytes)); //$NON-NLS-1$
	}
}
