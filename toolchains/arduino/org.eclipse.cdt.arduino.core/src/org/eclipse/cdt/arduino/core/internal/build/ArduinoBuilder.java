/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoTemplateGenerator;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoard;
import org.eclipse.cdt.arduino.core.internal.console.ArduinoConsoleService;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This class is responsible for generating the Makefile for the current build
 * config.
 */
public class ArduinoBuilder extends IncrementalProjectBuilder {

	public static final String ID = Activator.getId() + ".arduinoBuilder"; //$NON-NLS-1$

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();

		// What board are we building for?
		ArduinoBuildConfiguration config = getBuildConfig().getAdapter(ArduinoBuildConfiguration.class);
		ArduinoBoard board = config.getBoard();

		// Get the build console
		ArduinoConsoleService consoleService = Activator.getConsoleService();

		try {
			consoleService.writeOutput(String.format("\nBuilding project: %s\n", project.getName()));

			IFolder buildFolder = project.getFolder("build"); //$NON-NLS-1$
			if (!buildFolder.exists()) {
				buildFolder.create(true, true, monitor);
				CoreModel.newOutputEntry(buildFolder.getFullPath());
			}

			String makeFileName = board.getId() + ".mk"; //$NON-NLS-1$
			IFile makeFile = buildFolder.getFile(makeFileName);
			generateMakefile(makeFile, board, monitor);

			String[] cmd = new String[] { "make", "-f", makeFileName }; //$NON-NLS-1$ //$NON-NLS-2$
			Process process = Runtime.getRuntime().exec(cmd, null, new File(buildFolder.getLocationURI()));
			consoleService.monitor(process, null);

			buildFolder.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Build error", e));
		}

		// TODO if there are references we want to watch, return them here
		return new IProject[] { project };
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		ArduinoBuildConfiguration config = getBuildConfig().getAdapter(ArduinoBuildConfiguration.class);
		ArduinoBoard board = config.getBoard();

		ArduinoConsoleService consoleService = Activator.getConsoleService();
		try {
			consoleService.writeOutput(String.format("\nCleaning project: %s\n", project.getName()));

			IFolder buildFolder = project.getFolder("build"); //$NON-NLS-1$
			if (!buildFolder.exists()) {
				buildFolder.create(true, true, monitor);
				ICProject cproject = CoreModel.getDefault().create(project);
				IOutputEntry output = CoreModel.newOutputEntry(buildFolder.getFullPath());
				IPathEntry[] oldEntries = cproject.getRawPathEntries();
				IPathEntry[] newEntries = new IPathEntry[oldEntries.length];
				System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
				newEntries[oldEntries.length] = output;
				cproject.setRawPathEntries(newEntries, monitor);
			}

			String makeFileName = board.getId() + ".mk"; //$NON-NLS-1$
			IFile makeFile = buildFolder.getFile(makeFileName);
			generateMakefile(makeFile, board, monitor);

			String[] cmd = new String[] { "make", "-f", makeFileName, "clean" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Process process = Runtime.getRuntime().exec(cmd, null, new File(buildFolder.getLocationURI()));

			consoleService.monitor(process, null);

			buildFolder.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Build error", e));
		}
	}

	private void generateMakefile(IFile makeFile, ArduinoBoard board, IProgressMonitor monitor) throws CoreException {
		Map<String, Object> buildModel = new HashMap<>();
		buildModel.put("boardId", board.getId()); //$NON-NLS-1$

		final List<String> sourceFiles = new ArrayList<>();
		final IProject project = getProject();
		for (ISourceRoot sourceRoot : CCorePlugin.getDefault().getCoreModel().create(project).getSourceRoots()) {
			sourceRoot.getResource().accept(new IResourceProxyVisitor() {
				@Override
				public boolean visit(IResourceProxy proxy) throws CoreException {
					if (proxy.getType() == IResource.FILE) {
						if (CoreModel.isValidSourceUnitName(project, proxy.getName())) {
							sourceFiles.add(proxy.getName());
						}
					}
					return true;
				}
			}, 0);
		}
		buildModel.put("sources", sourceFiles); //$NON-NLS-1$

		ArduinoTemplateGenerator templateGen = new ArduinoTemplateGenerator();
		templateGen.generateFile(buildModel, "board.mk", makeFile, monitor); //$NON-NLS-1$
	}

}
