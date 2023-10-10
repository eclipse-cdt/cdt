/*******************************************************************************
 * Copyright (c) 2023 John Dallaway and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Dallaway - initial implementation (#563)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.graphics.Color;

/**
 * Decorates translation unit source and header files
 */
public final class TranslationUnitDecorator extends BaseLabelProvider implements ILightweightLabelDecorator {

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		// if an external translation unit source file that is not present locally
		if (element instanceof ITranslationUnit tu && (null == tu.getResource()) && !tu.exists()) {
			// decorate label to indicate file is absent
			Color color = JFaceResources.getColorRegistry().get(JFacePreferences.QUALIFIER_COLOR);
			decoration.setForegroundColor(color);
		}
	}

}
