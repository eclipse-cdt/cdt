/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeCommonBuildInfo;
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.osgi.service.environment.Constants;

public class MakeTarget extends PlatformObject implements IMakeTarget {
	private final static int USE_PROJECT_ENV_SETTING = 3;
	private final MakeTargetManager manager;
	private final IProject project;
	private String name;
	private boolean isDefaultBuildCmd;
	private boolean isStopOnError;
	boolean runAllBuidlers = true;
	private String targetBuilderID;
	private IContainer container;
	private int appendEnvironment = USE_PROJECT_ENV_SETTING;
	private boolean appendProjectEnvironment = true;
	private Map buildEnvironment = new HashMap();
	private Map targetAttributes = new HashMap();

	MakeTarget(MakeTargetManager manager, IProject project, String targetBuilderID, String name) throws CoreException {
		this.manager = manager;
		this.project = project; 
		this.targetBuilderID = targetBuilderID;
		this.name = name;
		IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(project, manager.getBuilderID(targetBuilderID));
		setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, info.getBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, "make")); //$NON-NLS-1$
		setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, info.getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, "")); //$NON-NLS-1$
		isDefaultBuildCmd = info.isDefaultBuildCmd();
		isStopOnError = info.isStopOnError();
	}

	public IProject getProject() {
		return project;
	}
	
	public void setContainer(IContainer container) {
		this.container = container;
	}

	void setName(String name) {
		this.name = name;
	}
	
	Map getAttributeMap() {
		return targetAttributes;
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
		if (isDefaultBuildCmd()) {
			IMakeBuilderInfo info;
			try {
				info = MakeCorePlugin.createBuildInfo(getProject(), manager.getBuilderID(targetBuilderID));
				return info.getBuildCommand();
			} catch (CoreException e) {
			}
		}
		String result = getBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, "make"); //$NON-NLS-1$
		try {
			result = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(result, false);
		} catch (CoreException e) {
		}
		return new Path(result);
	}

	public void setBuildCommand(IPath command) throws CoreException {
		setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, command.toString());
	}

	public String getBuildArguments() {
		String result = getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, ""); //$NON-NLS-1$
		try {
			result = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(result, false);
		} catch (CoreException e) {
		}
		return result;
	}

	public void setBuildArguments(String arguments) throws CoreException {
		setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, arguments);
	}
	
	public void setBuildTarget(String target) throws CoreException {
		setBuildAttribute(IMakeTarget.BUILD_TARGET, target);
	}

	public String getBuildTarget() {
		String result = getBuildAttribute(IMakeTarget.BUILD_TARGET, ""); //$NON-NLS-1$
		try {
			result = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(result, false);
		} catch (CoreException e) {
		}
		return result;	
	}

	public void setRunAllBuilders(boolean runAllBuilders) throws CoreException {
		this.runAllBuidlers = runAllBuilders;
		manager.updateTarget(this);
	}

	public boolean runAllBuilders() {
		return runAllBuidlers;
	}

	public void setBuildAttribute(String name, String value) throws CoreException {
		targetAttributes.put(name, value);
		manager.updateTarget(this);
	}

	public String getBuildAttribute(String name, String defaultValue) {
		String value = (String)targetAttributes.get(name);
		return value != null ? value : defaultValue;
	}

	public IPath getBuildLocation() {
		return container.getLocation();
	}

	public void setBuildLocation(IPath location) throws CoreException {
		throw new UnsupportedOperationException();
	}

	public String[] getErrorParsers() {
		IMakeBuilderInfo projectInfo;
		try {
			projectInfo = MakeCorePlugin.createBuildInfo(getProject(), manager.getBuilderID(targetBuilderID));
			return projectInfo.getErrorParsers();
		} catch (CoreException e) {
		}
		return new String[0];
	}

	public void setErrorParsers(String[] parsers) throws CoreException {
		throw new UnsupportedOperationException();
	}

	public Map getExpandedEnvironment() throws CoreException {
		Map env = null;
		if (appendProjectEnvironment()) {
			IMakeBuilderInfo projectInfo;
			projectInfo = MakeCorePlugin.createBuildInfo(getProject(), manager.getBuilderID(targetBuilderID));
			env = projectInfo.getEnvironment();
		}
		if (env == null) {
			env = getEnvironment();
		} else {
			env.putAll(getEnvironment());
		}
		HashMap envMap = new HashMap(env.entrySet().size());
		Iterator iter = env.entrySet().iterator();
		boolean win32 = Platform.getOS().equals(Constants.OS_WIN32);
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			String key = (String)entry.getKey();
			if (win32) {
				// Win32 vars are case insensitive. Uppercase everything so
				// that (for example) "pAtH" will correctly replace "PATH"
				key = key.toUpperCase();
			}
			String value = (String)entry.getValue();
			// translate any string substitution variables
			String translated = value;
			translated = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(value, false);
			envMap.put(key, translated);
		}
		return envMap;
	}
	
	public boolean appendProjectEnvironment() {
		return appendProjectEnvironment;
	}

	public void setAppendProjectEnvironment(boolean append) {
		appendProjectEnvironment = append;
	}	

	public Map getEnvironment() {
		return buildEnvironment;
	}

	public void setEnvironment(Map env) throws CoreException {
		buildEnvironment = new HashMap(env);
		manager.updateTarget(this);
	}

	public void setAppendEnvironment(boolean append) throws CoreException {	
		appendEnvironment = append ? 1 : 0;
		manager.updateTarget(this);
	}	

	public boolean appendEnvironment() {
		return appendEnvironment == USE_PROJECT_ENV_SETTING ? getProjectEnvSetting(): appendEnvironment == 1;
	}
	
	private boolean getProjectEnvSetting() {
		IMakeBuilderInfo projectInfo;
		try {
			projectInfo = MakeCorePlugin.createBuildInfo(getProject(), manager.getBuilderID(targetBuilderID));
			return projectInfo.appendEnvironment();
		} catch (CoreException e) {
		}
		return false;
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
		return container.hashCode() * 17 + name != null ? name.hashCode(): 0;
	}

	public void build(IProgressMonitor monitor) throws CoreException {
		final String builderID = manager.getBuilderID(targetBuilderID);
		final HashMap infoMap = new HashMap();

		IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(infoMap, builderID);
		info.setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, getBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, "make")); //$NON-NLS-1$
		info.setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, "")); //$NON-NLS-1$
		info.setUseDefaultBuildCmd(isDefaultBuildCmd());
		info.setStopOnError(isStopOnError());
		info.setIncrementalBuildEnable(true);
		info.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL, getBuildAttribute(IMakeTarget.BUILD_TARGET, "")); //$NON-NLS-1$
		info.setEnvironment(getExpandedEnvironment());
		info.setAppendEnvironment(appendEnvironment());
		if (container != null) {
			info.setBuildAttribute(IMakeCommonBuildInfo.BUILD_LOCATION, container.getFullPath().toString());
		}
		IMakeBuilderInfo projectInfo = MakeCorePlugin.createBuildInfo(getProject(), builderID);
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

	public Object getAdapter(Class adapter) {
		if (adapter.equals(IProject.class)) {
			return getProject();
		} else if (adapter.equals(IResource.class)) {
			return container;
		}
		return super.getAdapter(adapter);
	}
}
