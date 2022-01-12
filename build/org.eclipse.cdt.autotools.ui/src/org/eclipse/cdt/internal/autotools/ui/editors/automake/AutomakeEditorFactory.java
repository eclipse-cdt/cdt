/*******************************************************************************
 * Copyright (c) 2007, 2015 Red Hat Inc..
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.internal.autotools.ui.preferences.ColorManager;
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.Color;

public class AutomakeEditorFactory {
	private IWorkingCopyManager workingCopyManager;
	private AutomakeDocumentProvider automakeFileDocumentProvider;
	private static volatile AutomakeEditorFactory factory;

	/**
	 * The constructor.
	 */
	private AutomakeEditorFactory() {
		factory = this;
	}

	public synchronized AutomakeDocumentProvider getAutomakefileDocumentProvider() {
		if (automakeFileDocumentProvider == null) {
			automakeFileDocumentProvider = new AutomakeDocumentProvider();
		}
		return automakeFileDocumentProvider;
	}

	public synchronized IWorkingCopyManager getWorkingCopyManager() {
		if (workingCopyManager == null) {
			IMakefileDocumentProvider provider = getAutomakefileDocumentProvider();
			workingCopyManager = new WorkingCopyManager(provider);
		}
		return workingCopyManager;
	}

	/**
	 * Returns the preference color, identified by the given preference.
	 */
	public static Color getPreferenceColor(String key) {
		//FIXME: what do we do with Makefile editor preferences?
		return ColorManager.getDefault()
				.getColor(PreferenceConverter.getColor(AutotoolsPlugin.getDefault().getPreferenceStore(), key));
	}

	public static AutomakeEditorFactory getDefault() {
		if (factory == null)
			factory = new AutomakeEditorFactory();
		return factory;
	}
}
