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
package org.eclipse.cdt.debug.ui.breakpoints;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * Factory for creating field editors contributed through the <code>
 * org.eclipse.cdt.debug.ui.breakpointContribution</code> extension point.
 * <p>
 * Field editors do not have a non-arg constructor, therefore custom editors
 * cannot be created directly by the extension point directly.  This factory
 * allows clients to instantiate a custom field editor which is not on the class
 * path of the CDT debug UI plugin.
 * </p>
 * @since 7.2
 */
public interface IFieldEditorFactory {

	/**
	 * Creates a field editor with given parameters.
	 *
	 * @param name Field editor's property name.
	 * @param labelText Field editors label.
	 * @param parent Field editors parent control.
	 * @return Newly created field editor.
	 */
	public FieldEditor createFieldEditor(String name, String labelText, Composite parent);
}
