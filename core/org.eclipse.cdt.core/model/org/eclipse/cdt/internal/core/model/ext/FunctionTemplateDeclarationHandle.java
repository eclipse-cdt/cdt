/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model.ext;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionTemplateDeclaration;
import org.eclipse.cdt.internal.core.model.Template;

public class FunctionTemplateDeclarationHandle extends FunctionDeclarationHandle implements IFunctionTemplateDeclaration {
	Template fTemplate;

	public FunctionTemplateDeclarationHandle(ICElement parent, ICPPFunctionTemplate func) throws DOMException {
		this(parent, ICElement.C_TEMPLATE_FUNCTION_DECLARATION, func);
	}

	public FunctionTemplateDeclarationHandle(ICElement parent, ICPPTemplateInstance func) throws DOMException {
		this(parent, ICElement.C_TEMPLATE_FUNCTION_DECLARATION, func);
	}

	protected FunctionTemplateDeclarationHandle(ICElement parent, int type, ICPPFunctionTemplate func) throws DOMException {
		super(parent, type, func);
		fTemplate= new Template(func.getName());
		ICPPTemplateParameter[] tpars = func.getTemplateParameters();
		String[] args= new String[tpars.length];
		for (int i = 0; i < args.length; i++) {
			args[i]= tpars[i].getName();
		}
		fTemplate.setTemplateInfo(null, args);
	}

	protected FunctionTemplateDeclarationHandle(ICElement parent, int type, ICPPTemplateInstance func) throws DOMException {
		super(parent, type, (IFunction) func);
		fTemplate= new Template(func.getName());
		ICPPTemplateArgument[] targs = func.getTemplateArguments();
		String[] args= new String[targs.length];
		for (int i = 0; i < args.length; i++) {
			args[i]= ASTTypeUtil.getArgumentString(targs[i], false);
		}
		fTemplate.setTemplateInfo(null, args);
	}

	@Override
	public String[] getTemplateArguments() {
		return  fTemplate.getTemplateArguments();
	}

	@Override
	public int getNumberOfTemplateParameters() {
		return fTemplate.getNumberOfTemplateParameters();
	}

	@Override
	public String[] getTemplateParameterTypes() {
		return fTemplate.getTemplateParameterTypes();
	}

	@Override
	public String getTemplateSignature() throws CModelException {
		return fTemplate.getTemplateSignature();
	}
}
