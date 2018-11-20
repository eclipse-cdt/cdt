/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.variable;

import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelFont;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.FontData;

/**
 * @since 2.0
 */
public class VariableLabelFont extends LabelFont {

	public VariableLabelFont() {
		super(JFaceResources.getFontRegistry().getFontData(IDebugUIConstants.PREF_VARIABLE_TEXT_FONT)[0]);
	}

	@Override
	public FontData getFontData() {
		// Requesting the font descriptor from JFaceResources every time when this method is called
		// guarantees that changes made in the Preferences dialog will be applied.
		return JFaceResources.getFontRegistry().getFontData(IDebugUIConstants.PREF_VARIABLE_TEXT_FONT)[0];
	}
}
