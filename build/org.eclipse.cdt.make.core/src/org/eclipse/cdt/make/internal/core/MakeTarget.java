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
import org.eclipse.core.runtime.Path;

public class MakeTarget implements IMakeTarget {

	private String target;
	private String buildArguments;
	private IPath buildCommand;
	private boolean isDefaultBuildCmd;
	private boolean isStopOnError;
	private String name;
	private String targetBuilderID;
	private IContainer container;
	private MakeTargetManager manager;
	
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
		return buildCommand != null ? buildCommand: new Path("");
	}

	public void setBuildCommand(IPath command) {
		buildCommand = command;
	}

	public String getBuildArguments() {
		return buildArguments != null ? buildArguments : "";
	}

	public void setBuildArguments(String arguments) {
		buildArguments = arguments;
	}

	public IContainer getContainer() {
		return container;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof MakeTarget) {
			MakeTarget other = (MakeTarget) obj;
			return container.equals(other.getContainer()) && name.equals(other.getName());
		}
		return false;
	}

	public int hashCode() {
		return container.hashCode() * 17 + name.hashCode();
	}

	public void build(IProgressMonitor monitor) throws CoreException {
		IProject project = container.getProject();
		String builderID = manager.getBuilderID(targetBuilderID);
		HashMap infoMap = new HashMap();
		IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(infoMap, builderID);
		if ( buildArguments != null) {
			info.setBuildArguments(buildArguments);
		}
		if ( buildCommand != null ) {
			info.setBuildCommand(buildCommand);
		}
		info.setUseDefaultBuildCmd(isDefaultBuildCmd);
		info.setStopOnError(isStopOnError);
		info.setFullBuildEnable(true);
		info.setFullBuildTarget(target);
		if ( container != null) {
			info.setBuildLocation(container.getFullPath());
		}
		IMakeBuilderInfo projectInfo = MakeCorePlugin.createBuildInfo(project, builderID);
		info.setErrorParsers(projectInfo.getErrorParsers());
		project.build(IncrementalProjectBuilder.FULL_BUILD, builderID, infoMap, monitor);
	}

	public void setBuildTarget(String target) {
		this.target = target;
		
	}

	public String getBuildTarget() {
		return target;
	}
}
