/*******************************************************************************
 * Copyright (c) 2005, 2011 QnX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Qnx Software Systems - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IStructureTemplateDeclaration;

public class StructureTemplateDeclaration extends StructureDeclaration implements IStructureTemplateDeclaration {

	Template fTemplate;

	public StructureTemplateDeclaration(ICElement parent, int kind, String name) {
		super(parent, name, kind);
		fTemplate = new Template(name);
	}

	@Override
	public String[] getTemplateParameterTypes() {
		return fTemplate.getTemplateParameterTypes();
	}

	@Override
	public String[] getTemplateArguments() {
		return fTemplate.getTemplateArguments();
	}

	public void setTemplateParameterTypes(String[] templateParameterTypes) {
		fTemplate.setTemplateInfo(templateParameterTypes, null);
	}

	@Override
	public String getTemplateSignature() throws CModelException {
		return fTemplate.getTemplateSignature();
	}

	@Override
	public int getNumberOfTemplateParameters() {
		return fTemplate.getNumberOfTemplateParameters();
	}

	@Override
	public void getHandleMemento(StringBuilder buff) {
		super.getHandleMemento(buff);
		if (fTemplate.getNumberOfTemplateParameters() > 0) {
			final String[] parameterTypes = fTemplate.getTemplateParameterTypes();
			for (String parameterType : parameterTypes) {
				buff.append(CEM_PARAMETER);
				escapeMementoName(buff, parameterType);
			}
		}
	}

}
