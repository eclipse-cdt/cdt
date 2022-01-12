/*******************************************************************************
 * Copyright (c) 2017 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IProject;

public class ConstPlacement {
	public static boolean placeConstRight(IASTNode node) {
		if (node == null) {
			return false;
		}
		IASTTranslationUnit translationUnit = node.getTranslationUnit();
		if (translationUnit == null) {
			return false;
		}
		ITranslationUnit originatingTU = translationUnit.getOriginatingTranslationUnit();
		return placeConstRight(originatingTU);
	}

	public static boolean placeConstRight(ITranslationUnit tu) {
		IProject project = null;
		if (tu != null) {
			ICProject cProject = tu.getCProject();
			if (cProject != null) {
				project = cProject.getProject();
			}
		}
		return placeConstRight(project);
	}

	public static boolean placeConstRight(IProject project) {
		return CCorePreferenceConstants.getPreference(CCorePreferenceConstants.PLACE_CONST_RIGHT_OF_TYPE, project,
				CCorePreferenceConstants.DEFAULT_PLACE_CONST_RIGHT_OF_TYPE);
	}
}
