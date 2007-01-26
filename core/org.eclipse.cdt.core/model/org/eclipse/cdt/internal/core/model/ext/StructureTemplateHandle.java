/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.model.ext;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IStructureTemplate;
import org.eclipse.cdt.internal.core.model.Template;

public class StructureTemplateHandle extends StructureHandle implements IStructureTemplate {

	private Template fTemplate;

	public StructureTemplateHandle(ICElement parent, ICompositeType classTemplate) throws DOMException {
		super(parent, classTemplate);
		fTemplate= new Template(classTemplate.getName());
		if (classTemplate instanceof ICPPClassTemplate) {
			ICPPClassTemplate ct= (ICPPClassTemplate) classTemplate;
			ICPPTemplateParameter[] tps= ct.getTemplateParameters();
			String[] types= new String[tps.length];
			for (int i = 0; i < tps.length; i++) {
				ICPPTemplateParameter tp = tps[i];
				types[i]= tp.getName();
			}
			fTemplate.setTemplateParameterTypes(types);
		}
	}

	public int getNumberOfTemplateParameters() {
		return fTemplate.getNumberOfTemplateParameters();
	}

	public String[] getTemplateParameterTypes() {
		return fTemplate.getTemplateParameterTypes();
	}

	public String getTemplateSignature() throws CModelException {
		return fTemplate.getTemplateSignature();
	}
}
