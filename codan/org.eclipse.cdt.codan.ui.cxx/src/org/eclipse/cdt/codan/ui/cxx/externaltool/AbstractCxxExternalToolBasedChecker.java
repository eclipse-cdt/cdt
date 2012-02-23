/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.ui.cxx.externaltool;

import org.eclipse.cdt.codan.core.cxx.util.FileTypes;
import org.eclipse.cdt.codan.core.externaltool.AbstractExternalToolBasedChecker;
import org.eclipse.cdt.codan.core.externaltool.ConfigurationSettings;
import org.eclipse.cdt.codan.core.externaltool.IArgsSeparator;
import org.eclipse.cdt.codan.core.externaltool.IInvocationParametersProvider;
import org.eclipse.cdt.codan.core.externaltool.InvocationParametersProvider;
import org.eclipse.cdt.codan.core.externaltool.SpaceArgsSeparator;
import org.eclipse.cdt.codan.internal.ui.externaltool.ConsolePrinterProvider;
import org.eclipse.cdt.codan.ui.CodanEditorUtility;
import org.eclipse.cdt.codan.ui.cxx.util.CEditors;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * Base class for checkers that invoke external command-line tools to perform code checking
 * on C++ files.
 * <p>
 * A file, to be processed by this type of checker, must:
 * <ol>
 * <li>a C++ file</li>
 * <li>be in the current active editor</li>
 * <li>not have any unsaved changes</li>
 * </ol>
 * </p>
 * By default, implementations of this checker are not allowed to run while the user types, since
 * external tools cannot see unsaved changes.
 *
 * @author alruiz@google.com (Alex Ruiz)
 * 
 * @since 3.0
 */
public abstract class AbstractCxxExternalToolBasedChecker extends AbstractExternalToolBasedChecker {
	/**
	 * Constructor
	 * @param configurationSettings user-configurable external tool configuration settings.
	 */
	public AbstractCxxExternalToolBasedChecker(ConfigurationSettings configurationSettings) {
		this(new InvocationParametersProvider(), new SpaceArgsSeparator(), configurationSettings);
	}

	/**
	 * Constructor.
	 * @param parametersProvider provides the parameters to pass when invoking the external tool.
	 * @param argsSeparator separates the arguments to pass to the external tool executable. These
	 *        arguments are stored in a single {@code String}.
	 * @param configurationSettings user-configurable external tool configuration settings.
	 */
	public AbstractCxxExternalToolBasedChecker(IInvocationParametersProvider parametersProvider,
			IArgsSeparator argsSeparator, ConfigurationSettings configurationSettings) {
		super(parametersProvider, argsSeparator, new ConsolePrinterProvider(),
				configurationSettings);
	}

	/**
	 * Indicates whether the external tool is capable of processing the given
	 * <code>{@link IResource}</code>.
	 * <p>
	 * The minimum requirements that the given {@code IResource} should satisfy are:
	 * <ul>
	 * <li>should be C/C++ file</li>
	 * <li>should be displayed in the current active {@code CEditor}</li>
	 * <li>should not have any unsaved changes</li>
	 * </ul>
	 * </p>
	 * @param resource the given {@code IResource}.
	 * @return {@code true} if the external tool is capable of processing the given file,
	 *         {@code false} otherwise.
	 */
	@Override
	public boolean enabledInContext(IResource resource) {
		return isFileOfSupportedType(resource) && isOpenInActiveCleanEditor(resource);
	}

	private boolean isFileOfSupportedType(IResource resource) {
		return FileTypes.isCppFile(resource) || FileTypes.isHeaderFile(resource);
	}

	private boolean isOpenInActiveCleanEditor(IResource resource) {
		TextEditor editor = CEditors.activeCEditor();
		if (editor == null) {
			return false;
		}
		return !editor.isDirty() &&	CodanEditorUtility.isResourceOpenInEditor(resource, editor);
	}
}
