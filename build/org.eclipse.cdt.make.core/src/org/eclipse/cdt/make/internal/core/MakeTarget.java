/*
 * Created on 19-Aug-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.util.HashMap;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class MakeTarget implements IMakeTarget {

	private String buildArguments;
	private IPath buildCommand;
	private boolean isDefaultBuildCmd;
	private boolean isStopOnError;
	private String name;
	private String targetBuilderID;
	private IContainer container;

	MakeTarget(IContainer container, String targetBuilderID, String name) {
		this.container = container;
		this.targetBuilderID = targetBuilderID;
		this.name = name;
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

	public void setStopOnError(boolean stopOnError) {
		isStopOnError = stopOnError;
	}

	public boolean isDefaultBuildCmd() {
		return isDefaultBuildCmd;
	}

	public void setUseDefaultBuildCmd(boolean useDefault) {
		isDefaultBuildCmd = useDefault;
	}

	public IPath getBuildCommand() {
		return buildCommand;
	}

	public void setBuildCommand(IPath command) {
		buildCommand = command;
	}

	public String getBuildArguments() {
		return buildArguments;
	}

	public void setBuildArguments(String arguments) {
		buildArguments = arguments;
	}

	public IContainer getContainer() {
		return container;
	}

	public void build(IProgressMonitor monitor) throws CoreException {
		IProject project = container.getProject();
		String builderID = MakeCorePlugin.getDefault().getTargetManager().getBuilderID(targetBuilderID);
		HashMap infoMap = new HashMap();
		IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(infoMap, builderID);
		info.setBuildArguments(buildArguments);
		info.setBuildCommand(buildCommand);
		info.setUseDefaultBuildCmd(isDefaultBuildCmd);
		info.setStopOnError(isStopOnError);
		info.setFullBuildEnable(true);
		info.setFullBuildTarget(buildArguments);
		info.setBuildLocation(container.getLocation());
		project.build(IncrementalProjectBuilder.FULL_BUILD, builderID, infoMap, monitor);
	}
}
