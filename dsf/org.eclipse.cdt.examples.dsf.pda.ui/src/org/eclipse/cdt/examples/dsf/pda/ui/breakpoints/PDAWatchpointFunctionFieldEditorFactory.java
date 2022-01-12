/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
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
package org.eclipse.cdt.examples.dsf.pda.ui.breakpoints;

import org.eclipse.cdt.debug.ui.breakpoints.IFieldEditorFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 *
 */
public class PDAWatchpointFunctionFieldEditorFactory implements IFieldEditorFactory {

	@Override
	public FieldEditor createFieldEditor(String name, String labelText, Composite parent) {
		return new StringFieldEditor(name, labelText, parent);
	}
}
