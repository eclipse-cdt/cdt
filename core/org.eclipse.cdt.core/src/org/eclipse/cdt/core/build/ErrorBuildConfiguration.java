/*******************************************************************************
 * Copyright (c) 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.build.Messages;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * A Build configuration that simply spits out an error message on the console at build and clean time.
 * Used to signify that we're not sure how to build this project in it's current state.
 *
 * TODO leaving most of the implementation as default. I don't think any of these methods get called when
 * we're in this error state but we'll keep an eye open for NPE's and bad behavior.
 */
public class ErrorBuildConfiguration extends PlatformObject implements ICBuildConfiguration, ICBuildConfiguration2 {

	private final IBuildConfiguration config;
	private String errorMessage;

	public static final String NAME = "!"; //$NON-NLS-1$

	private static class Provider implements ICBuildConfigurationProvider {
		@Override
		public String getId() {
			return "buildError"; //$NON-NLS-1$
		}

		@Override
		public ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config, String name)
				throws CoreException {
			return new ErrorBuildConfiguration(config, Messages.ErrorBuildConfiguration_What);
		}
	}

	public static final Provider PROVIDER = new Provider();

	public ErrorBuildConfiguration(IBuildConfiguration config, String errorMessage) {
		this.errorMessage = errorMessage;
		this.config = config;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		try {
			console.getErrorStream().write(errorMessage);
		} catch (IOException e) {
			throw new CoreException(
					CCorePlugin.createStatus(Messages.ErrorBuildConfiguration_ErrorWritingToConsole, e));
		}
		return null;
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		try {
			console.getErrorStream().write(errorMessage);
		} catch (IOException e) {
			throw new CoreException(
					CCorePlugin.createStatus(Messages.ErrorBuildConfiguration_ErrorWritingToConsole, e));
		}
	}

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setActive() {
		// TODO Auto-generated method stub

	}

	@Override
	public URI getBuildDirectoryURI() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IBuildConfiguration getBuildConfiguration() throws CoreException {
		return config;
	}

	@Override
	public IToolChain getToolChain() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBinaryParserId() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEnvironmentVariable getVariable(String name) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEnvironmentVariable[] getVariables() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
