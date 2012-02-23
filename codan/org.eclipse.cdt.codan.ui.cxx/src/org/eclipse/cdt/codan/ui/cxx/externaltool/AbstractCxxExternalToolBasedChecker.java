/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.ui.cxx.externaltool;

import org.eclipse.cdt.codan.core.externaltool.ConfigurationSettings;
import org.eclipse.cdt.codan.core.externaltool.IArgsSeparator;
import org.eclipse.cdt.codan.core.externaltool.IInvocationParametersProvider;
import org.eclipse.cdt.codan.core.externaltool.ISupportedResourceVerifier;
import org.eclipse.cdt.codan.core.externaltool.InvocationParametersProvider;
import org.eclipse.cdt.codan.core.externaltool.SpaceDelimitedArgsSeparator;
import org.eclipse.cdt.codan.core.model.AbstractExternalToolBasedChecker;
import org.eclipse.cdt.codan.ui.externaltool.CommandLauncher;

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
 */
public abstract class AbstractCxxExternalToolBasedChecker extends AbstractExternalToolBasedChecker {
	/**
	 * Constructor
	 * @param configurationSettings user-configurable external tool configuration settings.
	 */
	public AbstractCxxExternalToolBasedChecker(ConfigurationSettings configurationSettings) {
		this(new InvocationParametersProvider(), new CxxSupportedResourceVerifier(),
				new SpaceDelimitedArgsSeparator(), configurationSettings);
	}

	/**
	 * Constructor.
	 * @param parametersProvider provides the parameters to pass when invoking the external tool.
	 * @param supportedResourceVerifier indicates whether a resource can be processed by the
	 *        external tool.
	 * @param argsSeparator separates the arguments to pass to the external tool executable. These
	 *        arguments are stored in a single {@code String}.
	 * @param configurationSettings user-configurable external tool configuration settings.
	 */
	public AbstractCxxExternalToolBasedChecker(IInvocationParametersProvider parametersProvider,
			ISupportedResourceVerifier supportedResourceVerifier, IArgsSeparator argsSeparator,
			ConfigurationSettings configurationSettings) {
		super(parametersProvider, supportedResourceVerifier, argsSeparator, new CommandLauncher(),
				configurationSettings);
	}
}
