/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.util.HashMap;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.SubProgressMonitor;

public class MakeTarget extends PlatformObject implements IMakeTarget {

	private final MakeTargetManager manager;
	private String name;
	private String target;
	private String buildArguments;
	private IPath buildCommand;
	private boolean isDefaultBuildCmd;
	private boolean isStopOnError;
	private boolean runAllBuidlers = true;
	private String targetBuilderID;
	private IContainer container;

	MakeTarget(MakeTargetManager manager, IProject project, String targetBuilderID, String name) throws CoreException {
		this.manager = manager;
		this.targetBuilderID = targetBuilderID;
		this.name = name;
		IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(project, manager.getBuilderID(targetBuilderID));
		buildCommand = info.getBuildCommand();
		buildArguments = info.getBuildArguments();
		isDefaultBuildCmd = info.isDefaultBuildCmd();
		isStopOnError = info.isStopOnError();
	}

	public void setContainer(IContainer container) {
		this.container = container;
	}

	void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getTargetBuilderID() {
		return targetBuilderID;
	}

	public boolean isStopOnError() {
		return isStopOnError;
	}

	public void setStopOnError(boolean stopOnError) throws CoreException {
		isStopOnError = stopOnError;
		manager.updateTarget(this);
	}

	public boolean isDefaultBuildCmd() {
		return isDefaultBuildCmd;
	}

	public void setUseDefaultBuildCmd(boolean useDefault) throws CoreException {
		isDefaultBuildCmd = useDefault;
		manager.updateTarget(this);
	}

	public IPath getBuildCommand() {
		return buildCommand != null ? buildCommand : new Path(""); //$NON-NLS-1$
	}

	public void setBuildCommand(IPath command) throws CoreException {
		buildCommand = command;
		manager.updateTarget(this);
	}

	public String getBuildArguments() {
		return buildArguments != null ? buildArguments : ""; //$NON-NLS-1$
	}

	public void setBuildArguments(String arguments) throws CoreException {
		buildArguments = arguments;
		manager.updateTarget(this);
	}

	public IContainer getContainer() {
		return container;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof MakeTarget) {
			MakeTarget other = (MakeTarget)obj;
			return (container != null ? container.equals(other.getContainer()) : other.getContainer() == null) && name.equals(other.getName());
		}
		return false;
	}

	public int hashCode() {
		return container.hashCode() * 17 + name.hashCode();
	}

	public void build(IProgressMonitor monitor) throws CoreException {
		final IProject project = container.getProject();
		final String builderID = manager.getBuilderID(targetBuilderID);
		final HashMap infoMap = new HashMap();

		IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(infoMap, builderID);
		if (buildArguments != null) {
			info.setBuildArguments(buildArguments);
		}
		if (buildCommand != null) {
			info.setBuildCommand(buildCommand);
		}
		info.setUseDefaultBuildCmd(isDefaultBuildCmd);
		info.setStopOnError(isStopOnError);
		info.setFullBuildEnable(true);
		info.setFullBuildTarget(target);
		if (container != null) {
			info.setBuildLocation(container.getFullPath());
		}
		IMakeBuilderInfo projectInfo = MakeCorePlugin.createBuildInfo(project, builderID);
		info.setErrorParsers(projectInfo.getErrorParsers());
		IWorkspaceRunnable op = new IWorkspaceRunnable() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public void run(IProgressMonitor monitor) throws CoreException {
				if (runAllBuidlers) {
					ICommand[] commands = project.getDescription().getBuildSpec();
					monitor.beginTask("", commands.length); //$NON-NLS-1$
					for (int i = 0; i < commands.length; i++) {
						if (commands[i].getBuilderName().equals(builderID)) {
							project.build(IncrementalProjectBuilder.FULL_BUILD, builderID, infoMap, new SubProgressMonitor(monitor, 1));
						} else {
							project.build(IncrementalProjectBuilder.FULL_BUILD, commands[i].getBuilderName(),
									commands[i].getArguments(), new SubProgressMonitor(monitor, 1));
						}
					}
					monitor.done();
				} else {
					project.build(IncrementalProjectBuilder.FULL_BUILD, builderID, infoMap, monitor);
				}
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(op, monitor);
		} finally {
			monitor.done();
		}
	}

	public void setBuildTarget(String target) throws CoreException {
		this.target = target;
		manager.updateTarget(this);
	}

	public String getBuildTarget() {
		return target != null ? target : ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.make.core.IMakeTarget#setRunAllBuilders(boolean)
	 */
	public void setRunAllBuilders(boolean runAllBuilders) {
		this.runAllBuidlers = runAllBuilders;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.make.core.IMakeTarget#runAllBuilders()
	 */
	public boolean runAllBuilders() {
		return runAllBuidlers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IProject.class)) {
			return container.getProject();
		} else if (adapter.equals(IResource.class)) {
			return container;
		}
		return super.getAdapter(adapter);
	}
}
