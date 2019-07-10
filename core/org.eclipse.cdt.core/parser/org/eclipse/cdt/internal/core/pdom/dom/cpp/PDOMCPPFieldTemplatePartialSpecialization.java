/*******************************************************************************
 * Copyright (c) 2015, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lukas Wegmann (IFS) - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFieldTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplatePartialSpecialization;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

public class PDOMCPPFieldTemplatePartialSpecialization extends PDOMCPPVariableTemplatePartialSpecialization
		implements ICPPFieldTemplate {

	public PDOMCPPFieldTemplatePartialSpecialization(PDOMCPPLinkage linkage, PDOMNode parent,
			ICPPVariableTemplatePartialSpecialization parSpec, PDOMCPPFieldTemplate pdomPrimary)
			throws CoreException, DOMException {
		super(linkage, parent, parSpec, pdomPrimary);
	}

	public PDOMCPPFieldTemplatePartialSpecialization(PDOMLinkage pdomLinkage, long record) {
		super(pdomLinkage, record);
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FIELD_TEMPLATE_PARTIAL_SPECIALIZATION;
	}

	@Override
	public ICompositeType getCompositeTypeOwner() {
		return getClassOwner();
	}

	@Override
	public int getVisibility() {
		return PDOMCPPAnnotations.getVisibility(getAnnotations());
	}

	@Override
	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	@Override
	public ICPPTemplateDefinition getPrimaryTemplate() {
		try {
			return new PDOMCPPFieldTemplate(getLinkage(), getPrimaryTemplateRec());
		} catch (CoreException e) {
			CCorePlugin.log("Failed to load primary template for " + getName(), e); //$NON-NLS-1$
			return null;
		}
	}

	@Override
	public PDOMCPPVariableTemplatePartialSpecialization getNextPartial() throws CoreException {
		long rec = getNextPartialRec();
		if (rec == 0)
			return null;
		return new PDOMCPPFieldTemplatePartialSpecialization(getLinkage(), rec);
	}

	@Override
	public int getFieldPosition() {
		return ((ICPPField) getPrimaryTemplate()).getFieldPosition();
	}
}
