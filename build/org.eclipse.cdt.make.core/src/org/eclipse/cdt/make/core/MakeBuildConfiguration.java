/********************************************************************************
 * Copyright (c) 2026 Igor V. Kovalenko and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.cdt.make.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncherManager;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.StandardBuildConfiguration;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.jsoncdb.core.CompileCommandsJsonParser;
import org.eclipse.cdt.jsoncdb.core.ISourceFileInfoConsumer;
import org.eclipse.cdt.jsoncdb.core.ParseRequest;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * @since 8.0
 */
public class MakeBuildConfiguration extends StandardBuildConfiguration {
	// TODO: copied from CMakeBuildConfiguration, maybe move up to CBuildConfiguration
	private Map<IResource, IScannerInfo> infoPerResource;

	public MakeBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);
	}

	public MakeBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain, String launchMode,
			ILaunchTarget launchTarget) throws CoreException {
		super(config, name, toolChain, launchMode, launchTarget);
	}

	private IFile getCompileCommandsFile() throws CoreException {
		// TODO: copied from CMakeBuildConfiguration, move up to CBuildConfiguration
		final IPath cdbPath = new org.eclipse.core.runtime.Path(getScannerInfoCompilationDatabasePath());
		return ResourcesPlugin.getWorkspace().getRoot().getFile(cdbPath);
	}

	/**
	 * @param console the console to print the compiler output during built-ins detection to or
	 *                <code>null</code> if no separate console is to be allocated. Ignored if
	 *                workspace preferences indicate that no console output is wanted.
	 * @param monitor the job's progress monitor
	 * TODO: copied from CMakeBuildConfiguration, move up to CBuildConfiguration
	 */
	private void processCompileCommandsFile(IConsole console, IProgressMonitor monitor) throws CoreException {
		IFile file = getCompileCommandsFile();
		CompileCommandsJsonParser parser = new CompileCommandsJsonParser(
				new ParseRequest(file, new MakeBuildIndexerInfoConsumer(this::setScannerInformation),
						CommandLauncherManager.getInstance().getCommandLauncher(this), console));
		parser.parse(monitor);
	}

	/**
	 * Overridden since the ScannerInfoCache mechanism does not satisfy our needs.
	 */
	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		// TODO: copied from CMakeBuildConfiguration, maybe move up to CBuildConfiguration
		final String cdbPath = getScannerInfoCompilationDatabasePath();

		if (cdbPath == null || cdbPath.isEmpty()) {
			return super.getScannerInformation(resource);
		}

		if (infoPerResource == null) {
			// no build was run yet, nothing detected
			try {
				processCompileCommandsFile(null, new NullProgressMonitor());
			} catch (CoreException e) {
				CCorePlugin.log(e.getStatus());
			}
		}
		return infoPerResource == null ? null : infoPerResource.get(resource);
	}

	private void setScannerInformation(Map<IResource, IScannerInfo> infoPerResource) {
		this.infoPerResource = infoPerResource;
	}

	private static class MakeBuildIndexerInfoConsumer implements ISourceFileInfoConsumer {
		/**
		 * gathered IScannerInfo objects or <code>null</code> if no new IScannerInfo was received
		 */
		private Map<IResource, IScannerInfo> infoPerResource = new HashMap<>();
		private boolean haveUpdates;
		private final Consumer<Map<IResource, IScannerInfo>> resultSetter;

		/**
		 * @param resultSetter receives the all scanner information when processing is finished
		 */
		public MakeBuildIndexerInfoConsumer(Consumer<Map<IResource, IScannerInfo>> resultSetter) {
			this.resultSetter = Objects.requireNonNull(resultSetter);
		}

		@Override
		public void acceptSourceFileInfo(String sourceFileName, List<String> systemIncludePaths,
				Map<String, String> definedSymbols, List<String> includePaths, List<String> macroFiles,
				List<String> includeFiles) {
			IFile file = getFileForCdbPath(sourceFileName);
			if (file != null) {
				ExtendedScannerInfo info = new ExtendedScannerInfo(definedSymbols,
						systemIncludePaths.stream().toArray(String[]::new), macroFiles.stream().toArray(String[]::new),
						includeFiles.stream().toArray(String[]::new), includePaths.stream().toArray(String[]::new));
				infoPerResource.put(file, info);
				haveUpdates = true;
			}
		}

		/**
		 * Gets an IFile object that corresponds to the source file name given in compilation database.
		 *
		 * @param sourceFileName the name of the source file.
		 * @return a IFile object or <code>null</code>
		 */
		private IFile getFileForCdbPath(String sourceFileName) {
			IPath path = new Path(sourceFileName);
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
			return file;
		}

		@Override
		public void shutdown() {
			if (haveUpdates) {
				// we received updates
				resultSetter.accept(infoPerResource);
				infoPerResource = null;
				haveUpdates = false;
			}
		}
	} // CMakeIndexerInfoConsumer
}
