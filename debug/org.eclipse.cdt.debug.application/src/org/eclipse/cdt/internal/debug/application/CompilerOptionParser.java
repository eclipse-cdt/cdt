/*******************************************************************************
 * Copyright (c) 2013, 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.debug.application;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.ICompileOptionsFinder;
import org.eclipse.cdt.core.ISymbolReader;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.debug.application.GCCCompileOptionsParser;
import org.eclipse.cdt.debug.application.Messages;
import org.eclipse.cdt.utils.coff.parser.PEParser;
import org.eclipse.cdt.utils.elf.parser.GNUElfParser;
import org.eclipse.cdt.utils.macho.parser.MachOParser64;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class CompilerOptionParser implements IWorkspaceRunnable {

	private static final String GCC_COMPILE_OPTIONS_PROVIDER_ID = "org.eclipse.cdt.debug.application.DwarfLanguageSettingsProvider"; //$NON-NLS-1$
	private final IProject project;
	private final String executable;

	public CompilerOptionParser(IProject project, String executable) {
		this.project = project;
		this.executable = executable;
	}

	private class CWDTracker implements IWorkingDirectoryTracker {

		@Override
		public URI getWorkingDirectoryURI() {
			return null;
		}

	}

	@Override
	public void run(IProgressMonitor monitor) {
		try {
			// Calculate how many source files we have to process and use that as a basis
			// for our work estimate.
			IBinaryFile bf = null;
			try {
				bf = new GNUElfParser().getBinary(new Path(executable));
			} catch (IOException e) {
				// Will try other parsers
			}

			// Try Portable Executable (Windows)
			if (bf == null) {
				try {
					bf = new PEParser().getBinary(new Path(executable));
				} catch (IOException e) {
					// Will try other parsers
				}
			}

			// Try Mach-O (Mac OS X)
			if (bf == null) {
				bf = new MachOParser64().getBinary(new Path(executable));
				try {
					bf = new PEParser().getBinary(new Path(executable));
				} catch (IOException e) {
					// ignored, see below early return
				}
			}

			if (bf == null) {
				// Doesn't look like a known binary but we can let the debugger try to
				// debug it. This means that the project will likely be
				// incomplete and this will affect code navigation.
				return;
			}

			ISymbolReader reader = bf.getAdapter(ISymbolReader.class);
			String[] sourceFiles = reader.getSourceFiles();
			monitor.beginTask(Messages.GetCompilerOptions, sourceFiles.length * 2 + 1);

			for (String sourceFile : sourceFiles) {
				IPath sourceFilePath = new Path(sourceFile);
				String sourceName = sourceFilePath.lastSegment();
				IContainer c = createFromRoot(project, new Path(sourceFile));
				Path sourceNamePath = new Path(sourceName);
				IFile source = c.getFile(sourceNamePath);
				if (!source.isLinked()) {
					try {
						source.createLink(sourceFilePath, 0, null);
					} catch (Exception e) {
						// ignore file not found errors since certain headers might not be found
						// or are a different version from that used to compile the source (e.g. std headers)
					}
				}
				monitor.worked(1);
			}

			// Find the GCCCompileOptions LanguageSettingsProvider for the configuration.
			IWorkingDirectoryTracker cwdTracker = new CWDTracker();
			ICProjectDescriptionManager projDescManager = CCorePlugin.getDefault().getProjectDescriptionManager();
			ICProjectDescription projDesc = projDescManager.getProjectDescription(project, false);
			ICConfigurationDescription ccdesc = projDesc.getActiveConfiguration();
			GCCCompileOptionsParser parser = null;
			if (ccdesc instanceof ILanguageSettingsProvidersKeeper) {
				ILanguageSettingsProvidersKeeper keeper = (ILanguageSettingsProvidersKeeper) ccdesc;
				List<ILanguageSettingsProvider> list = keeper.getLanguageSettingProviders();
				for (ILanguageSettingsProvider p : list) {
					//						System.out.println("language settings provider " + p.getId());
					if (p.getId().equals(GCC_COMPILE_OPTIONS_PROVIDER_ID)) {
						parser = (GCCCompileOptionsParser) p;
					}
				}
			}
			// Start up the parser and process lines generated from the .debug_macro section.
			parser.startup(ccdesc, cwdTracker);
			// Get compile options for each source file and process via the parser
			// to generate LanguageSettingsEntries.
			if (reader instanceof ICompileOptionsFinder) {
				ICompileOptionsFinder f = (ICompileOptionsFinder) reader;
				for (String fileName : sourceFiles) {
					parser.setCurrentResourceName(fileName);
					parser.processLine(f.getCompileOptions(fileName));
					monitor.worked(1);
				}
				parser.shutdown(); // this will serialize the data to an xml file and create an event.
				monitor.worked(1);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		monitor.done();
	}

	private IContainer createFromRoot(IProject exeProject, IPath path) throws CoreException {
		int segmentCount = path.segmentCount() - 1;
		IContainer currentFolder = exeProject;

		for (int i = 0; i < segmentCount; i++) {
			currentFolder = currentFolder.getFolder(new Path(path.segment(i)));
			if (!currentFolder.exists()) {
				((IFolder) currentFolder).create(IResource.VIRTUAL | IResource.DERIVED, true,
						new NullProgressMonitor());
			}
		}

		return currentFolder;
	}
}
