/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Niefer (IBM Corporation) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

public abstract class CPPTemplateDefinition extends PlatformObject
		implements ICPPTemplateDefinition, ICPPInternalTemplate {
	public static final class CPPTemplateProblem extends ProblemBinding implements ICPPTemplateDefinition {
		public CPPTemplateProblem(IASTNode node, int id, char[] arg) {
			super(node, id, arg);
		}

		@Override
		public ICPPTemplateParameter[] getTemplateParameters() {
			return ICPPTemplateParameter.EMPTY_TEMPLATE_PARAMETER_ARRAY;
		}
	}

	protected IASTName[] declarations;
	protected IASTName definition;

	private ICPPTemplateParameter[] templateParameters;
	private ObjectMap instances;

	private ICPPClassTemplate indexBinding;
	private boolean checkedIndex;

	public CPPTemplateDefinition(IASTName name) {
		if (name != null) {
			ASTNodeProperty prop = name.getPropertyInParent();
			if (prop == ICPPASTQualifiedName.SEGMENT_NAME) {
				prop = name.getParent().getPropertyInParent();
			}
			if (prop == IASTCompositeTypeSpecifier.TYPE_NAME) {
				definition = name;
			} else if (prop == IASTElaboratedTypeSpecifier.TYPE_NAME) {
				declarations = new IASTName[] { name };
			} else {
				IASTNode parent = name.getParent();
				while (!(parent instanceof IASTDeclaration))
					parent = parent.getParent();
				if (parent instanceof IASTFunctionDefinition) {
					definition = name;
				} else {
					declarations = new IASTName[] { name };
				}
			}
		}
	}

	@Override
	public final void addInstance(ICPPTemplateArgument[] arguments, ICPPTemplateInstance instance) {
		if (instances == null)
			instances = new ObjectMap(2);
		String key = ASTTypeUtil.getArgumentListString(arguments, true);
		instances.put(key, instance);
	}

	@Override
	public final ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments) {
		if (instances != null) {
			String key = ASTTypeUtil.getArgumentListString(arguments, true);
			ICPPTemplateInstance cand = (ICPPTemplateInstance) instances.get(key);
			if (cand != null)
				return cand;
		}

		final ICPPClassTemplate ib = getIndexBinding();
		if (ib instanceof ICPPInstanceCache) {
			ICPPTemplateInstance cand = ((ICPPInstanceCache) ib).getInstance(arguments);
			if (cand instanceof IIndexBinding) {
				if (getTemplateName().getTranslationUnit().getIndexFileSet()
						.containsDeclaration((IIndexBinding) cand)) {
					return cand;
				}
			} else {
				return cand;
			}
		}
		return null;
	}

	protected ICPPClassTemplate getIndexBinding() {
		if (!checkedIndex) {
			checkedIndex = true;
			IASTName name = getTemplateName();
			if (name != null) {
				IASTTranslationUnit tu = name.getTranslationUnit();
				if (tu != null) {
					IIndex index = tu.getIndex();
					if (index != null) {
						IIndexBinding ib = index.adaptBinding(this);
						if (ib instanceof ICPPClassTemplate)
							indexBinding = (ICPPClassTemplate) ib;
					}
				}
			}
		}
		return indexBinding;
	}

	@Override
	public ICPPTemplateInstance[] getAllInstances() {
		if (instances != null) {
			ICPPTemplateInstance[] result = new ICPPTemplateInstance[instances.size()];
			for (int i = 0; i < instances.size(); i++) {
				result[i] = (ICPPTemplateInstance) instances.getAt(i);
			}
			return result;
		}
		return ICPPTemplateInstance.EMPTY_TEMPLATE_INSTANCE_ARRAY;
	}

	public IASTName getTemplateName() {
		if (definition != null)
			return definition;
		if (declarations != null && declarations.length > 0)
			return declarations[0];
		return null;
	}

	@Override
	public String getName() {
		return new String(getNameCharArray());
	}

	@Override
	public char[] getNameCharArray() {
		return getTemplateName().getSimpleID();
	}

	@Override
	public IScope getScope() {
		return CPPVisitor.getContainingScope(getTemplateName());
	}

	@Override
	public String[] getQualifiedName() {
		return CPPVisitor.getQualifiedName(this);
	}

	@Override
	public char[][] getQualifiedNameCharArray() {
		return CPPVisitor.getQualifiedNameCharArray(this);
	}

	@Override
	public boolean isGloballyQualified() {
		return true;
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		if (templateParameters == null) {
			ICPPTemplateParameter[] result = ICPPTemplateParameter.EMPTY_TEMPLATE_PARAMETER_ARRAY;
			ICPPASTTemplateDeclaration template = CPPTemplates.getTemplateDeclaration(getTemplateName());
			if (template != null) {
				ICPPASTTemplateParameter[] params = template.getTemplateParameters();
				for (ICPPASTTemplateParameter param : params) {
					IBinding p = CPPTemplates.getTemplateParameterName(param).resolveBinding();
					if (p instanceof ICPPTemplateParameter) {
						result = ArrayUtil.append(result, (ICPPTemplateParameter) p);
					}
				}
			}
			templateParameters = ArrayUtil.trim(result);
		}
		return templateParameters;
	}

	@Override
	public void addDefinition(IASTNode node) {
		if (node instanceof ICPPASTCompositeTypeSpecifier) {
			node = ((ICPPASTCompositeTypeSpecifier) node).getName();
			if (node instanceof ICPPASTQualifiedName) {
				node = ((ICPPASTQualifiedName) node).getLastName();
			}
		}
		if (!(node instanceof IASTName))
			return;
		updateTemplateParameterBindings((IASTName) node);
		definition = (IASTName) node;
	}

	@Override
	public void addDeclaration(IASTNode node) {
		if (node instanceof ICPPASTElaboratedTypeSpecifier) {
			node = ((ICPPASTElaboratedTypeSpecifier) node).getName();
			if (node instanceof ICPPASTQualifiedName) {
				node = ((ICPPASTQualifiedName) node).getLastName();
			}
		}
		if (!(node instanceof IASTName))
			return;
		IASTName declName = (IASTName) node;
		updateTemplateParameterBindings(declName);
		if (declarations == null) {
			declarations = new IASTName[] { declName };
		} else {
			// Keep the lowest offset declaration in [0].
			if (declarations.length > 0 && ((ASTNode) node).getOffset() < ((ASTNode) declarations[0]).getOffset()) {
				declarations = ArrayUtil.prepend(IASTName.class, declarations, declName);
			} else {
				declarations = ArrayUtil.append(IASTName.class, declarations, declName);
			}
		}
	}

	@Override
	public IBinding resolveTemplateParameter(ICPPTemplateParameter templateParameter) {
		int pos = templateParameter.getParameterPosition();

		int tdeclLen = declarations == null ? 0 : declarations.length;
		for (int i = -1; i < tdeclLen; i++) {
			IASTName tdecl;
			if (i == -1) {
				tdecl = definition;
				if (tdecl == null)
					continue;
			} else {
				tdecl = declarations[i];
				if (tdecl == null)
					break;
			}

			ICPPASTTemplateParameter[] params = CPPTemplates.getTemplateDeclaration(tdecl).getTemplateParameters();
			if (pos < params.length) {
				final IASTName oName = CPPTemplates.getTemplateParameterName(params[pos]);
				return oName.resolvePreBinding();
			}
		}
		return templateParameter;
	}

	final protected void updateTemplateParameterBindings(IASTName name) {
		final ICPPASTTemplateDeclaration templateDeclaration = CPPTemplates.getTemplateDeclaration(name);
		if (templateDeclaration == null)
			return;

		ICPPASTTemplateParameter[] updateParams = templateDeclaration.getTemplateParameters();
		int k = 0;
		int tdeclLen = declarations == null ? 0 : declarations.length;
		for (int i = -1; i < tdeclLen && k < updateParams.length; i++) {
			IASTName tdecl;
			if (i == -1) {
				tdecl = definition;
				if (tdecl == null)
					continue;
			} else {
				tdecl = declarations[i];
				if (tdecl == null)
					break;
			}

			ICPPASTTemplateParameter[] params = CPPTemplates.getTemplateDeclaration(tdecl).getTemplateParameters();
			int end = Math.min(params.length, updateParams.length);
			for (; k < end; k++) {
				final IASTName oName = CPPTemplates.getTemplateParameterName(params[k]);
				IBinding b = oName.resolvePreBinding();
				IASTName n = CPPTemplates.getTemplateParameterName(updateParams[k]);
				n.setBinding(b);
				ASTInternal.addDeclaration(b, n);
			}
		}
	}

	@Override
	public IASTNode[] getDeclarations() {
		return declarations;
	}

	@Override
	public IASTNode getDefinition() {
		return definition;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public final IBinding getOwner() {
		IASTName templateName = getTemplateName();
		if (templateName == null)
			return null;

		return CPPVisitor.findNameOwner(templateName, false);
	}
}
