/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoardManager;
import org.eclipse.cdt.arduino.core.internal.build.ArduinoBuilder;
import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@SuppressWarnings("restriction")
public class ArduinoProjectGenerator {

	private final IProject project;
	private IFile sourceFile;

	public ArduinoProjectGenerator(IProject project) {
		this.project = project;
	}

	public void setupArduinoProject(IProgressMonitor monitor) throws CoreException {
		// Add Arduino nature
		IProjectDescription projDesc = project.getDescription();
		String[] oldIds = projDesc.getNatureIds();
		String[] newIds = new String[oldIds.length + 1];
		System.arraycopy(oldIds, 0, newIds, 0, oldIds.length);
		newIds[newIds.length - 1] = ArduinoProjectNature.ID;
		projDesc.setNatureIds(newIds);

		// Add Arduino Builder
		ICommand command = projDesc.newCommand();
		command.setBuilderName(ArduinoBuilder.ID);
		projDesc.setBuildSpec(new ICommand[] { command });

		project.setDescription(projDesc, monitor);

		// create the CDT natures and build setup
		CCorePlugin.getDefault().createCDTProject(projDesc, project, monitor);
		ICProjectDescription cprojDesc = CCorePlugin.getDefault().createProjectDescription(project, false);
		ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
		ManagedProject mProj = new ManagedProject(cprojDesc);
		info.setManagedProject(mProj);

		// TODO make this a preference, the default board
		String boardName = "Arduino Uno"; //$NON-NLS-1$
		String platformName = "Arduino AVR Boards"; //$NON-NLS-1$
		String packageName = "arduino"; //$NON-NLS-1$

		IRemoteServicesManager remoteManager = Activator.getService(IRemoteServicesManager.class);
		IRemoteConnectionType connectionType = remoteManager.getConnectionType(ArduinoRemoteConnection.TYPE_ID);
		Collection<IRemoteConnection> connections = connectionType.getConnections();
		if (!connections.isEmpty()) {
			IRemoteConnection firstConnection = connections.iterator().next();
			IArduinoRemoteConnection firstArduino = firstConnection.getService(IArduinoRemoteConnection.class);
			boardName = firstArduino.getBoardName();
			platformName = firstArduino.getPlatformName();
			packageName = firstArduino.getPackageName();
		}

		ArduinoBoardManager.instance.createBuildConfiguration(cprojDesc, boardName, platformName, packageName);
		CCorePlugin.getDefault().setProjectDescription(project, cprojDesc, true, monitor);

		// Generate files
		try {
			Configuration fmConfig = new Configuration(Configuration.VERSION_2_3_22);
			URL templateDirURL = FileLocator.find(Activator.getContext().getBundle(), new Path("/templates"), null); //$NON-NLS-1$
			fmConfig.setDirectoryForTemplateLoading(new File(FileLocator.toFileURL(templateDirURL).toURI()));

			final Map<String, Object> fmModel = new HashMap<>();
			fmModel.put("projectName", project.getName()); //$NON-NLS-1$

			generateFile(fmModel, fmConfig.getTemplate("Makefile"), project.getFile("Makefile")); //$NON-NLS-1$ //$NON-NLS-2$
			generateFile(fmModel, fmConfig.getTemplate("arduino.mk"), project.getFile("arduino.mk")); //$NON-NLS-1$ //$NON-NLS-2$

			sourceFile = project.getFile(project.getName() + ".cpp"); //$NON-NLS-1$
			generateFile(fmModel, fmConfig.getTemplate("arduino.cpp"), sourceFile); //$NON-NLS-1$
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), e.getLocalizedMessage(), e));
		} catch (URISyntaxException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), e.getLocalizedMessage(), e));
		} catch (TemplateException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), e.getLocalizedMessage(), e));
		}

		// Do the initial build
		project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
	}

	private static void generateFile(Object model, Template template, final IFile outputFile)
			throws TemplateException, IOException, CoreException {
		final PipedInputStream in = new PipedInputStream();
		PipedOutputStream out = new PipedOutputStream(in);
		final Writer writer = new OutputStreamWriter(out);
		Job job = new Job(Messages.ArduinoProjectGenerator_0) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					outputFile.create(in, true, monitor);
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(outputFile.getProject());
		job.schedule();
		template.process(model, writer);
		writer.close();
		try {
			job.join();
		} catch (InterruptedException e) {
			// TODO anything?
		}
		IStatus status = job.getResult();
		if (!status.isOK())
			throw new CoreException(status);
	}

	public IFile getSourceFile() {
		return sourceFile;
	}

}
