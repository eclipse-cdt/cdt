/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Tianchao Li (tianchao.li@gmail.com) - arbitrary build directory (bug #136136)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.core.runtime.SubMonitor;
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
	private final String targetBuilderID;
	private IContainer container;
	private int appendEnvironment = USE_PROJECT_ENV_SETTING;
	private boolean appendProjectEnvironment = true;
	private Map<String, String> buildEnvironment = new HashMap<>();
	private final Map<String, String> targetAttributes = new HashMap<>();

	public MakeTarget(MakeTargetManager manager, IProject project, String targetBuilderID, String name)
			throws CoreException {
		this.manager = manager;
		this.project = project;
		this.targetBuilderID = targetBuilderID;
		this.name = name;
		IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(project, manager.getBuilderID(targetBuilderID));
		setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND,
				info.getBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, "make")); //$NON-NLS-1$
		setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS,
				info.getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, "")); //$NON-NLS-1$
		isDefaultBuildCmd = info.isDefaultBuildCmd();
		isStopOnError = info.isStopOnError();
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setContainer(IContainer container) {
		this.container = container;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	Map<String, String> getAttributeMap() {
		return targetAttributes;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getTargetBuilderID() {
		return targetBuilderID;
	}

	@Override
	public boolean isStopOnError() {
		return isStopOnError;
	}

	@Override
	public void setStopOnError(boolean stopOnError) throws CoreException {
		isStopOnError = stopOnError;
		manager.updateTarget(this);
	}

	@Override
	public boolean isDefaultBuildCmd() {
		return isDefaultBuildCmd;
	}

	@Override
	public void setUseDefaultBuildCmd(boolean useDefault) throws CoreException {
		isDefaultBuildCmd = useDefault;
		manager.updateTarget(this);
	}

	@Override
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

	@Override
	public void setBuildCommand(IPath command) throws CoreException {
		setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, command.toString());
	}

	@Override
	public String getBuildArguments() {
		if (isDefaultBuildCmd()) {
			IMakeBuilderInfo info;
			try {
				info = MakeCorePlugin.createBuildInfo(getProject(), manager.getBuilderID(targetBuilderID));
				return info.getBuildArguments();
			} catch (CoreException e) {
			}
		}
		String result = getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, ""); //$NON-NLS-1$
		try {
			result = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(result, false);
		} catch (CoreException e) {
		}
		return result;
	}

	@Override
	public void setBuildArguments(String arguments) throws CoreException {
		setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, arguments);
	}

	@Override
	public void setBuildTarget(String target) throws CoreException {
		setBuildAttribute(IMakeTarget.BUILD_TARGET, target);
	}

	@Override
	public String getBuildTarget() {
		String result = getBuildAttribute(IMakeTarget.BUILD_TARGET, ""); //$NON-NLS-1$
		try {
			result = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(result, false);
		} catch (CoreException e) {
		}
		return result;
	}

	@Override
	public void setRunAllBuilders(boolean runAllBuilders) throws CoreException {
		this.runAllBuidlers = runAllBuilders;
		manager.updateTarget(this);
	}

	@Override
	public boolean runAllBuilders() {
		return runAllBuidlers;
	}

	@Override
	public void setBuildAttribute(String name, String value) throws CoreException {
		targetAttributes.put(name, value);
		manager.updateTarget(this);
	}

	@Override
	public String getBuildAttribute(String name, String defaultValue) {
		String value = targetAttributes.get(name);
		return value != null ? value : defaultValue;
	}

	@Override
	public IPath getBuildLocation() {
		return container.getLocation();
	}

	@Override
	public void setBuildLocation(IPath location) throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getErrorParsers() {
		IMakeBuilderInfo projectInfo;
		try {
			projectInfo = MakeCorePlugin.createBuildInfo(getProject(), manager.getBuilderID(targetBuilderID));
			return projectInfo.getErrorParsers();
		} catch (CoreException e) {
		}
		return new String[0];
	}

	@Override
	public void setErrorParsers(String[] parsers) throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, String> getExpandedEnvironment() throws CoreException {
		Map<String, String> env = null;
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

		HashMap<String, String> envMap = new HashMap<>(env.entrySet().size());
		boolean win32 = Platform.getOS().equals(Constants.OS_WIN32);
		for (Entry<String, String> entry : env.entrySet()) {
			String key = entry.getKey();
			if (win32) {
				// Win32 vars are case insensitive. Uppercase everything so
				// that (for example) "pAtH" will correctly replace "PATH"
				key = key.toUpperCase();
			}
			String value = entry.getValue();
			// translate any string substitution variables
			String translated = value;
			translated = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(value,
					false);
			envMap.put(key, translated);
		}
		return envMap;
	}

	@Override
	public boolean appendProjectEnvironment() {
		return appendProjectEnvironment;
	}

	@Override
	public void setAppendProjectEnvironment(boolean append) {
		appendProjectEnvironment = append;
	}

	@Override
	public Map<String, String> getEnvironment() {
		return buildEnvironment;
	}

	@Override
	public void setEnvironment(Map<String, String> env) throws CoreException {
		buildEnvironment = new HashMap<>(env);
		manager.updateTarget(this);
	}

	@Override
	public void setAppendEnvironment(boolean append) throws CoreException {
		appendEnvironment = append ? 1 : 0;
		manager.updateTarget(this);
	}

	@Override
	public boolean appendEnvironment() {
		return appendEnvironment == USE_PROJECT_ENV_SETTING ? getProjectEnvSetting() : appendEnvironment == 1;
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

	@Override
	public IContainer getContainer() {
		return container;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof MakeTarget) {
			MakeTarget other = (MakeTarget) obj;
			return (container != null ? container.equals(other.getContainer()) : other.getContainer() == null)
					&& name.equals(other.getName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return container.hashCode() * 17 + name != null ? name.hashCode() : 0;
	}

	@Override
	public void build(IProgressMonitor monitor) throws CoreException {
		final String builderID = manager.getBuilderID(targetBuilderID);
		final HashMap<String, String> infoMap = new HashMap<>();

		IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(infoMap, builderID);
		info.setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND,
				getBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, "make")); //$NON-NLS-1$
		info.setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS,
				getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, "")); //$NON-NLS-1$
		info.setUseDefaultBuildCmd(isDefaultBuildCmd());
		info.setStopOnError(isStopOnError());
		info.setIncrementalBuildEnable(true);
		info.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL,
				getBuildAttribute(IMakeTarget.BUILD_TARGET, "")); //$NON-NLS-1$
		info.setCleanBuildEnable(false);
		info.setEnvironment(getExpandedEnvironment());
		info.setAppendEnvironment(appendEnvironment());
		if (container != null && container != project) {
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
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				if (runAllBuidlers) {
					ICommand[] commands = project.getDescription().getBuildSpec();
					SubMonitor subMonitor = SubMonitor.convert(monitor, commands.length);
					for (ICommand command : commands) {
						if (command.getBuilderName().equals(builderID)) {
							project.build(IncrementalProjectBuilder.FULL_BUILD, builderID, infoMap,
									subMonitor.newChild(1));
						} else {
							project.build(IncrementalProjectBuilder.FULL_BUILD, command.getBuilderName(),
									command.getArguments(), subMonitor.newChild(1));
						}
					}
					monitor.done();
				} else {
					project.build(IncrementalProjectBuilder.FULL_BUILD, builderID, infoMap, monitor);
				}
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(op, null, IResource.NONE, monitor);
		} finally {
			monitor.done();
		}
	}

}
