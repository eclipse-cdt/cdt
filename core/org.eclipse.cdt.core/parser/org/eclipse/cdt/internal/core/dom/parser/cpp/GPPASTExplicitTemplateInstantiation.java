/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTExplicitTemplateInstantiation;

/**
 * @deprecated Replaced by {@link CPPASTExplicitTemplateInstantiation}.
 */
@Deprecated
public class GPPASTExplicitTemplateInstantiation extends CPPASTExplicitTemplateInstantiation
		implements IGPPASTExplicitTemplateInstantiation {

	public GPPASTExplicitTemplateInstantiation() {
		super();
	}

	public GPPASTExplicitTemplateInstantiation(IASTDeclaration declaration) {
		super(declaration);
	}

	@Override
	public GPPASTExplicitTemplateInstantiation copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public GPPASTExplicitTemplateInstantiation copy(CopyStyle style) {
		GPPASTExplicitTemplateInstantiation copy = new GPPASTExplicitTemplateInstantiation();
		IASTDeclaration declaration = getDeclaration();
		copy.setDeclaration(declaration == null ? null : declaration.copy(style));
		copy.setModifier(getModifier());
		return copy(copy, style);
	}
}
