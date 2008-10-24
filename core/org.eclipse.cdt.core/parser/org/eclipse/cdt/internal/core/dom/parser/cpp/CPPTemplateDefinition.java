/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author aniefer
 */
public abstract class CPPTemplateDefinition extends PlatformObject implements ICPPTemplateDefinition, ICPPInternalTemplate {
	public static final class CPPTemplateProblem extends ProblemBinding implements ICPPTemplateDefinition {
		public CPPTemplateProblem(IASTNode node, int id, char[] arg) {
			super(node, id, arg);
		}
		public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
			throw new DOMException(this);
		}
		public ICPPClassTemplatePartialSpecialization[] getTemplateSpecializations() throws DOMException {
			throw new DOMException(this);
		}
		public String[] getQualifiedName() throws DOMException {
			throw new DOMException(this);
		}
		public char[][] getQualifiedNameCharArray() throws DOMException {
			throw new DOMException(this);
		}
		public boolean isGloballyQualified() throws DOMException {
			throw new DOMException(this);
		}
	}

	//private IASTName templateName;
	protected IASTName[] declarations;
	protected IASTName definition;
	
	private ICPPTemplateParameter[] templateParameters;
	private ObjectMap instances;

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
	
	public final void addInstance(ICPPTemplateArgument[] arguments, ICPPTemplateInstance instance) {
		if (instances == null)
			instances = new ObjectMap(2);
		instances.put(arguments, instance);
	}

	public final ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments) {
		if (instances != null) {
			loop: for (int i=0; i < instances.size(); i++) {
				ICPPTemplateArgument[] args = (ICPPTemplateArgument[]) instances.keyAt(i);
				if (args.length == arguments.length) {
					for (int j=0; j < args.length; j++) {
						if (!CPPTemplates.isSameTemplateArgument(args[j], arguments[j])) {
							continue loop;
						}
					}
					return (ICPPTemplateInstance) instances.getAt(i);
				}
			}
		}
		return null;
	}
	
	public ICPPTemplateInstance[] getAllInstances() {
		if (instances != null) {
			ICPPTemplateInstance[] result= new ICPPTemplateInstance[instances.size()];
			for (int i=0; i < instances.size(); i++) {
				result[i]= (ICPPTemplateInstance) instances.getAt(i);
			}
			return result;
		}
		return ICPPTemplateInstance.EMPTY_TEMPLATE_INSTANCE_ARRAY;
	}

	public IBinding resolveTemplateParameter(ICPPASTTemplateParameter templateParameter) {
	   	IASTName name = CPPTemplates.getTemplateParameterName(templateParameter);
    	IASTName preferredName= name;
	   	IBinding binding = name.getBinding();
    	if (binding != null)
    		return binding;
			
    	ICPPASTTemplateDeclaration templateDecl = (ICPPASTTemplateDeclaration) templateParameter.getParent();
    	ICPPASTTemplateParameter[] ps = templateDecl.getTemplateParameters();

    	int i = 0;
    	for (; i < ps.length; i++) {
    		if (templateParameter == ps[i])
    			break;
    	}
    	
    	if (definition != null || (declarations != null && declarations.length > 0)) {
    	    IASTName templateName = (definition != null) ? definition : declarations[0];
    	    ICPPASTTemplateDeclaration temp = CPPTemplates.getTemplateDeclaration(templateName);
    	    ICPPASTTemplateParameter[] params = temp.getTemplateParameters();
    	    if (params.length > i) {
    	        IASTName paramName = CPPTemplates.getTemplateParameterName(params[i]);
    	        preferredName= paramName;
    	        if (paramName.getBinding() != null) {
    	            binding = paramName.getBinding();
    	            name.setBinding(binding);
    	            if(binding instanceof ICPPInternalBinding)
    	                ((ICPPInternalBinding)binding).addDeclaration(name);
    	            return binding;
    	        }
    	    }
    	}
    	//create a new binding and set it for the corresponding parameter in all known decls
    	if (templateParameter instanceof ICPPASTSimpleTypeTemplateParameter) {
    		binding = new CPPTemplateTypeParameter(preferredName);
    	} else if (templateParameter instanceof ICPPASTParameterDeclaration) {
    		binding = new CPPTemplateNonTypeParameter(preferredName);
    	} else {
    		binding = new CPPTemplateTemplateParameter(preferredName);
    	}
    	
    	int length = (declarations != null) ? declarations.length : 0;
		int j = (definition != null) ? -1 : 0;
		for (; j < length; j++) {
	    	ICPPASTTemplateDeclaration template	= (j == -1)
	    		? CPPTemplates.getTemplateDeclaration(definition)
				: CPPTemplates.getTemplateDeclaration(declarations[j]);
			if (template == null)
				continue;
			
			ICPPASTTemplateParameter[] temp = template.getTemplateParameters();
			if (temp.length <= i)
				continue;

    		IASTName n = CPPTemplates.getTemplateParameterName(temp[i]);
    		if (n != null && n != name && n.getBinding() == null) {
    		    n.setBinding(binding);
    		    if (binding instanceof ICPPInternalBinding)
	                ((ICPPInternalBinding)binding).addDeclaration(n);
    		}

		}
    	return binding;
	}
	
	public IASTName getTemplateName() {
		if (definition != null)
			return definition;
		if (declarations != null && declarations.length > 0)
			return declarations[0];
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return getTemplateName().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return getTemplateName().toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return CPPVisitor.getContainingScope(getTemplateName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedName()
	 */
	public String[] getQualifiedName() {
		return CPPVisitor.getQualifiedName(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedNameCharArray()
	 */
	public char[][] getQualifiedNameCharArray() {
		return CPPVisitor.getQualifiedNameCharArray(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
	 */
	public boolean isGloballyQualified() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition#getParameters()
	 */
	public ICPPTemplateParameter[] getTemplateParameters() {
		if (templateParameters == null) {
			ICPPASTTemplateDeclaration template = CPPTemplates.getTemplateDeclaration(getTemplateName());
			if (template == null)
				return ICPPTemplateParameter.EMPTY_TEMPLATE_PARAMETER_ARRAY;
			ICPPASTTemplateParameter[] params = template.getTemplateParameters();
			IBinding p = null;
			ICPPTemplateParameter[] result = null;
			for (ICPPASTTemplateParameter param : params) {
				p= CPPTemplates.getTemplateParameterName(param).resolveBinding();
				if (p instanceof ICPPTemplateParameter) {
					result = (ICPPTemplateParameter[]) ArrayUtil.append(ICPPTemplateParameter.class, result, p);
				}
			}
			templateParameters = (ICPPTemplateParameter[]) ArrayUtil.trim(ICPPTemplateParameter.class, result);
		}
		return templateParameters;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDefinition(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDefinition(IASTNode node) {
	    if (node instanceof ICPPASTCompositeTypeSpecifier) {
	        node = ((ICPPASTCompositeTypeSpecifier)node).getName();
	        if (node instanceof ICPPASTQualifiedName) {
	            IASTName[] ns = ((ICPPASTQualifiedName)node).getNames();
	            node = ns[ns.length - 1];
	        }
	    }
		if (!(node instanceof IASTName))
			return;
		updateTemplateParameterBindings((IASTName) node);
		definition = (IASTName) node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDeclaration(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDeclaration(IASTNode node) {
	    if (node instanceof ICPPASTElaboratedTypeSpecifier) {
	        node = ((ICPPASTElaboratedTypeSpecifier)node).getName();
	        if (node instanceof ICPPASTQualifiedName) {
	            IASTName[] ns = ((ICPPASTQualifiedName)node).getNames();
	            node = ns[ns.length - 1];
	        }
	    }
		if (!(node instanceof IASTName))
			return;
		IASTName declName = (IASTName) node;
		updateTemplateParameterBindings(declName);
		if (declarations == null) {
	        declarations = new IASTName[] { declName };
		} else {
	        // keep the lowest offset declaration in[0]
			if (declarations.length > 0 && ((ASTNode) node).getOffset() < ((ASTNode) declarations[0]).getOffset()) {
				declarations = (IASTName[]) ArrayUtil.prepend(IASTName.class, declarations, declName);
			} else {
				declarations = (IASTName[]) ArrayUtil.append(IASTName.class, declarations, declName);
			}
	    }
	}	
	
	public void removeDeclaration(IASTNode node) {
		if (definition == node) {
			definition = null;
			return;
		}
		ArrayUtil.remove(declarations, node);
	}
	protected void updateTemplateParameterBindings(IASTName name) {
    	IASTName orig = definition != null ? definition : declarations[0];
    	ICPPASTTemplateDeclaration origTemplate = CPPTemplates.getTemplateDeclaration(orig);
    	ICPPASTTemplateDeclaration newTemplate = CPPTemplates.getTemplateDeclaration(name);
    	ICPPASTTemplateParameter[] ops = origTemplate.getTemplateParameters();
    	ICPPASTTemplateParameter[] nps = newTemplate.getTemplateParameters();
    	ICPPInternalBinding temp = null;
    	int end= Math.min(ops.length, nps.length);
    	for (int i = 0; i < end; i++) {
    		temp = (ICPPInternalBinding) CPPTemplates.getTemplateParameterName(ops[i]).getBinding();
    		if (temp != null) {
    		    IASTName n = CPPTemplates.getTemplateParameterName(nps[i]);
    			n.setBinding(temp);
    			temp.addDeclaration(n);
    		}
    	}
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#getDeclarations()
	 */
	public IASTNode[] getDeclarations() {
		return declarations;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#getDefinition()
	 */
	public IASTNode getDefinition() {
		return definition;
	}
	
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
	
	public final IBinding getOwner() {
		IASTName templateName= getTemplateName();
		if (templateName == null)
			return null;
		
		return CPPVisitor.findNameOwner(templateName, false);
	}
}
