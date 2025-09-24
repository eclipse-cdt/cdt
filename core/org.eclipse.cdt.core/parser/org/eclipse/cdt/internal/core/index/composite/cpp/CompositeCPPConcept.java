/*******************************************************************************
 * Copyright (c) 2025 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConceptDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConcept;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPConcept extends CompositeCPPBinding implements ICPPConcept {

	public CompositeCPPConcept(ICompositesFactory cf, ICPPConcept concept) {
		super(cf, concept);
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		return TemplateInstanceUtil.convert(cf, ((ICPPConcept) rbinding).getTemplateParameters());
	}

	@Override
	public ICPPASTConceptDefinition getConceptDefinition() {
		// TODO Auto-generated method stub
		return null;
	}
}
