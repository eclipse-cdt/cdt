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
package org.eclipse.cdt.arduino.core;

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

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoProjectNature;
import org.eclipse.cdt.arduino.core.internal.Messages;
import org.eclipse.cdt.arduino.core.internal.launch.ArduinoLaunchConfigurationDelegate;
import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
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
import org.eclipse.remote.core.launch.IRemoteLaunchConfigService;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@SuppressWarnings("restriction")
public class ArduinoProjectGenerator {

	public static final String BOARD_OPTION_ID = "org.eclipse.cdt.arduino.option.board"; //$NON-NLS-1$
	public static final String AVR_TOOLCHAIN_ID = "org.eclipse.cdt.arduino.toolChain.avr"; //$NON-NLS-1$

	private final IProject project;
	private IFile sourceFile;
	
	public ArduinoProjectGenerator(IProject project) {
		this.project = project;
	}
	
	public void setupArduinoProject(IProgressMonitor monitor) throws CoreException {
		// create the CDT-ness of the project
		IProjectDescription projDesc = project.getDescription();
		CCorePlugin.getDefault().createCDTProject(projDesc, project, monitor);
		
		String[] oldIds = projDesc.getNatureIds();
		String[] newIds = new String[oldIds.length + 3];
		System.arraycopy(oldIds, 0, newIds, 0, oldIds.length);
		newIds[newIds.length - 1] = ArduinoProjectNature.ID;
		newIds[newIds.length - 2] = CCProjectNature.CC_NATURE_ID;
		newIds[newIds.length - 3] = CProjectNature.C_NATURE_ID;
		projDesc.setNatureIds(newIds);
		project.setDescription(projDesc, monitor);

		ICProjectDescription cprojDesc = CCorePlugin.getDefault().createProjectDescription(project, false);
		ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
		ManagedProject mProj = new ManagedProject(cprojDesc);
		info.setManagedProject(mProj);

		Board board = null;
		
		IRemoteServicesManager remoteManager = Activator.getService(IRemoteServicesManager.class);
		IRemoteLaunchConfigService remoteLaunchService = Activator.getService(IRemoteLaunchConfigService.class);
		IRemoteConnection remoteConnection = remoteLaunchService.getLastActiveConnection(ArduinoLaunchConfigurationDelegate.getLaunchConfigurationType());
		if (remoteConnection != null) {
			IArduinoRemoteConnection arduinoRemote = remoteConnection.getService(IArduinoRemoteConnection.class);
			board = arduinoRemote.getBoard();
		} else {
			IRemoteConnectionType connectionType = remoteManager.getConnectionType(ArduinoRemoteConnection.TYPE_ID);
			Collection<IRemoteConnection> connections = connectionType.getConnections();
			if (!connections.isEmpty()) {
				IRemoteConnection firstConnection = connections.iterator().next();
				IArduinoRemoteConnection firstArduino = firstConnection.getService(IArduinoRemoteConnection.class);
				board = firstArduino.getBoard();
			}
		}
		
		if (board == null) {
			IArduinoBoardManager boardManager = Activator.getService(IArduinoBoardManager.class);
			board = boardManager.getBoard("uno"); // the default //$NON-NLS-1$
		}
		
		createBuildConfiguration(cprojDesc, board);

		CCorePlugin.getDefault().setProjectDescription(project, cprojDesc, true, monitor);

		// Generate files
		try {
			Configuration fmConfig = new Configuration(Configuration.VERSION_2_3_22);
			URL templateDirURL = FileLocator.find(Activator.getContext().getBundle(), new Path("/templates"), null); //$NON-NLS-1$
			fmConfig.setDirectoryForTemplateLoading(new File(FileLocator.toFileURL(templateDirURL).toURI()));

			final Map<String, Object> fmModel = new HashMap<>();
			fmModel.put("projectName", project.getName()); //$NON-NLS-1$
			
			generateFile(fmModel, fmConfig.getTemplate("Makefile"), project.getFile("Makefile")); //$NON-NLS-1$ //$NON-NLS-2$
			
			sourceFile = project.getFile(project.getName() + ".cpp"); //$NON-NLS-1$
			generateFile(fmModel, fmConfig.getTemplate("arduino.cpp"), sourceFile);  //$NON-NLS-1$
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

	private static void generateFile(Object model, Template template, final IFile outputFile) throws TemplateException, IOException, CoreException {
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
	
	public static ICConfigurationDescription createBuildConfiguration(ICProjectDescription projDesc, Board board) throws CoreException {
		ManagedProject managedProject = new ManagedProject(projDesc);
		String configId = ManagedBuildManager.calculateChildId(AVR_TOOLCHAIN_ID, null);
		IToolChain avrToolChain = ManagedBuildManager.getExtensionToolChain(AVR_TOOLCHAIN_ID);
		org.eclipse.cdt.managedbuilder.internal.core.Configuration newConfig = new org.eclipse.cdt.managedbuilder.internal.core.Configuration(managedProject, (ToolChain) avrToolChain, configId, board.getId());
		IToolChain newToolChain = newConfig.getToolChain();
		IOption newOption = newToolChain.getOptionBySuperClassId(BOARD_OPTION_ID);
		ManagedBuildManager.setOption(newConfig, newToolChain, newOption, board.getId());

		CConfigurationData data = newConfig.getConfigurationData();
		return projDesc.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
	}

	public static Board getBoard(IConfiguration configuration) throws CoreException {
		try {
			IToolChain toolChain = configuration.getToolChain();
			IOption boardOption = toolChain.getOptionBySuperClassId(BOARD_OPTION_ID);
			String boardId = boardOption.getStringValue();
			
			IArduinoBoardManager boardManager = Activator.getService(IArduinoBoardManager.class);
			Board board = boardManager.getBoard(boardId);
			if (board == null) {
				board = boardManager.getBoard("uno"); //$NON-NLS-1$
			}
			return board;
		} catch (BuildException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), e.getLocalizedMessage(), e));
		}
		
	}

	public IFile getSourceFile() {
		return sourceFile;
	}
	
}
