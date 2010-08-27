/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM Corporation) - Initial API and implementation 
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
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
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

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
	}

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
		String key= ASTTypeUtil.getArgumentListString(arguments, true);
		instances.put(key, instance);
	}

	public final ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments) {
		if (instances != null) {
			String key= ASTTypeUtil.getArgumentListString(arguments, true);
			return (ICPPTemplateInstance) instances.get(key);
		}
		return null;
	}
	
	public ICPPTemplateInstance[] getAllInstances() {
		if (instances != null) {
			ICPPTemplateInstance[] result= new ICPPTemplateInstance[instances.size()];
			for (int i= 0; i < instances.size(); i++) {
				result[i]= (ICPPTemplateInstance) instances.getAt(i);
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return new String(getNameCharArray());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return getTemplateName().getSimpleID();
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
	
	public IBinding resolveTemplateParameter(ICPPTemplateParameter templateParameter) {
		int pos= templateParameter.getParameterPosition();
		
    	int tdeclLen= declarations == null ? 0 : declarations.length;
    	for (int i= -1; i < tdeclLen; i++) {
    		IASTName tdecl;
    		if (i == -1) {
    			tdecl= definition;
    			if (tdecl == null)
    				continue;
    		} else {
    			tdecl= declarations[i];
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
    	int k= 0;
    	int tdeclLen= declarations == null ? 0 : declarations.length;
    	for (int i= -1; i < tdeclLen && k < updateParams.length; i++) {
    		IASTName tdecl;
    		if (i == -1) {
    			tdecl= definition;
    			if (tdecl == null)
    				continue;
    		} else {
    			tdecl= declarations[i];
    			if (tdecl == null)
    				break;
    		}
    		
    		ICPPASTTemplateParameter[] params = CPPTemplates.getTemplateDeclaration(tdecl).getTemplateParameters();
    		int end= Math.min(params.length, updateParams.length);
    		for (; k < end; k++) {
    			final IASTName oName = CPPTemplates.getTemplateParameterName(params[k]);
    			IBinding b= oName.resolvePreBinding();
    			IASTName n = CPPTemplates.getTemplateParameterName(updateParams[k]);
    			n.setBinding(b);
    			ASTInternal.addDeclaration(b, n);
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
