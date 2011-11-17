/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model.ext;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IStructureTemplate;
import org.eclipse.cdt.internal.core.model.Template;

public class StructureTemplateHandle extends StructureHandle implements IStructureTemplate {

	private Template fTemplate;

	public StructureTemplateHandle(ICElement parent, ICPPClassTemplate classTemplate) throws DOMException {
		super(parent, classTemplate);
		fTemplate= new Template(classTemplate.getName());
		ICPPTemplateParameter[] tpars = classTemplate.getTemplateParameters();
		String[] args= new String[tpars.length];
		for (int i = 0; i < args.length; i++) {
			args[i]= tpars[i].getName();
		}
		fTemplate.setTemplateInfo(null, args);
	}

	public StructureTemplateHandle(ICElement parent, ICPPClassTemplatePartialSpecialization classTemplate) throws DOMException {
		super(parent, classTemplate);
		fTemplate= new Template(classTemplate.getName());
		ICPPTemplateArgument[] targs = classTemplate.getTemplateArguments();
		String[] args= new String[targs.length];
		for (int i = 0; i < args.length; i++) {
			args[i]= ASTTypeUtil.getArgumentString(targs[i], false);
		}
		fTemplate.setTemplateInfo(null, args);
	}

	public StructureTemplateHandle(ICElement parent, ICPPClassSpecialization classBinding, ICPPClassTemplate ct) throws DOMException {
		super(parent, classBinding);
		fTemplate= new Template(classBinding.getName());
		ICPPTemplateParameterMap map = classBinding.getTemplateParameterMap();
		ICPPTemplateParameter[] tpars = ct.getTemplateParameters();
		List<String> args= new ArrayList<String>(tpars.length);
		for (ICPPTemplateParameter par : tpars) {
			if (par.isParameterPack()) {
				ICPPTemplateArgument[] pack= map.getPackExpansion(par);
				if (pack != null) {
					for (ICPPTemplateArgument p : pack) {
						args.add(ASTTypeUtil.getArgumentString(p, false));
					}
					continue;
				}
			}
			ICPPTemplateArgument arg = map.getArgument(par);
			args.add(arg == null ? par.getName() : ASTTypeUtil.getArgumentString(arg, false));
		}
		fTemplate.setTemplateInfo(null, args.toArray(new String[args.size()]));
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
	public String[] getTemplateArguments() {
		return  fTemplate.getTemplateArguments();
	}

	@Override
	public String getTemplateSignature() throws CModelException {
		return fTemplate.getTemplateSignature();
	}

}
