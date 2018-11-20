/*******************************************************************************
 * Copyright (c) 2018 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Provide access to the checkbox, this makes it equivalent to
 * org.eclipse.jface.preference.StringFieldEditor
 */
public class BooleanFieldEditor2 extends BooleanFieldEditor {

	public BooleanFieldEditor2(String prefBuildconsoleWrapLines, String resourceString, Composite parent) {
		super(prefBuildconsoleWrapLines, resourceString, parent);
	}

	@Override
	public Button getChangeControl(Composite parent) {
		return super.getChangeControl(parent);
	}

}
