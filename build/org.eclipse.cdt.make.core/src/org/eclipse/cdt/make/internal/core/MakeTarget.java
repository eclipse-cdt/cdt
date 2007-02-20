/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Tianchao Li (tianchao.li@gmail.com) - arbitrary build directory (bug #136136)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Builder;
import org.eclipse.cdt.managedbuilder.internal.core.BuilderFactory;
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
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.SubProgressMonitor;

public class MakeTarget extends PlatformObject implements IMakeTarget {
//	private final static int USE_PROJECT_ENV_SETTING = 3;
	private final MakeTargetManager manager;
	private final IProject project;
//	private final IConfiguration configuration;
	private IBuilder builder;

	private String name;
//	private boolean isDefaultBuildCmd;
//	private boolean isStopOnError;
	private boolean runAllBuidlers = true;
	private String targetBuilderID;
	private IContainer container;
//	private int appendEnvironment = USE_PROJECT_ENV_SETTING;
//	private boolean appendProjectEnvironment = true;
//	private Map buildEnvironment = new HashMap();
//	private Map targetAttributes = new HashMap();
	
	private static final String TARGET_ATTR_ID = "targetID"; //$NON-NLS-1$
	private static final String TARGET_ATTR_PATH = "path"; //$NON-NLS-1$
	private static final String TARGET_ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String TARGET_STOP_ON_ERROR = "stopOnError"; //$NON-NLS-1$
	private static final String TARGET_USE_DEFAULT_CMD = "useDefaultCommand"; //$NON-NLS-1$
	private static final String TARGET_ARGUMENTS = "buildArguments"; //$NON-NLS-1$
	private static final String TARGET_COMMAND = "buildCommand"; //$NON-NLS-1$
	private static final String BAD_TARGET = "buidlTarget"; //$NON-NLS-1$
	private static final String TARGET = "buildTarget"; //$NON-NLS-1$

	MakeTarget(MakeTarget target, IConfiguration cfg) {
		manager = target.manager;
		project = target.project;
		
		builder = ManagedBuildManager.createCustomBuilder(cfg, target.builder);
		
		name = target.name;
		runAllBuidlers = target.runAllBuidlers;
		targetBuilderID = target.targetBuilderID;
		container = target.container;
	}

	MakeTarget(MakeTargetManager manager, IConfiguration cfg, ICStorageElement el) throws CoreException {
		this.manager = manager;
		this.project = cfg.getOwner().getProject();
		ICStorageElement children[] = el.getChildren();
		IBuilder builder = null;

		String path = el.getAttribute(TARGET_ATTR_PATH);
		if (path != null && !path.equals("")) { //$NON-NLS-1$
			container = project.getFolder(path);
		} else {
			container = project;
		}

		
		for(int i = 0; i < children.length; i++){
			ICStorageElement child = children[i];
			if(IBuilder.BUILDER_ELEMENT_NAME.equals(child.getName())){
				builder = new Builder(cfg.getToolChain(), child, null);
			}
		}
		
		if(builder == null){
			builder = ManagedBuildManager.createCustomBuilder(cfg, cfg.getBuilder());
		}
		
		this.builder = builder;
		
		targetBuilderID = el.getAttribute(TARGET_ATTR_ID);
		name = el.getAttribute(TARGET_ATTR_NAME);

		for(int i = 0; i < children.length; i++){
			ICStorageElement child = children[i];
			String name = child.getName();
			if(TARGET_STOP_ON_ERROR.equals(name)){
				String value = child.getValue();
				if(value != null){
					builder.setStopOnError(Boolean.valueOf(value).booleanValue());
				}
			} else if (TARGET_USE_DEFAULT_CMD.equals(name)){
				String value = child.getValue();
				if(value != null){
					builder.setUseDefaultBuildCmd(Boolean.valueOf(value).booleanValue());
				}
			} else if (TARGET_COMMAND.equals(name)){
				String value = child.getValue();
				if(value != null){
					builder.setCommand(value);
				}
			} else if (TARGET_ARGUMENTS.equals(name)){
				String value = child.getValue();
				if(value != null){
					builder.setArguments(value);
				}
			} else if (BAD_TARGET.equals(name)){
				String value = child.getValue();
				if(value != null){
					builder.setBuildAttribute(IMakeTarget.BUILD_TARGET, value);
				}
			} else if (TARGET.equals(name)){
				String value = child.getValue();
				if(value != null){
					builder.setBuildAttribute(IMakeTarget.BUILD_TARGET, value);
				}
			}
		}
	}
	
	void serialize(ICStorageElement el){
		
		if(container != null){
			el.setAttribute(TARGET_ATTR_PATH, container.getProjectRelativePath().toString());
		}

		if(targetBuilderID != null)
			el.setAttribute(TARGET_ATTR_ID, targetBuilderID);
		
		if(name != null)
			el.setAttribute(TARGET_ATTR_NAME, name);

		ICStorageElement builderEl = el.createChild(IBuilder.BUILDER_ELEMENT_NAME);
		((Builder)builder).serialize(builderEl, false);
	}

	MakeTarget(MakeTargetManager manager, IConfiguration cfg, String builderId, String targetBuilderID, String name) throws CoreException {
		this.manager = manager;
//		this.configuration = cfg;
		this.project = cfg.getOwner().getProject(); 
		this.targetBuilderID = targetBuilderID;
		this.name = name;
		
		if(builderId != null){
			builder = ManagedBuildManager.createCustomBuilder(cfg, builderId);
		} /*else if (targetBuilderID != null){
			builder = ManagedBuildManager.createBuilderForEclipseBuilder(cfg, manager.getBuilderID(targetBuilderID));
		} */else {
			builder = ManagedBuildManager.createCustomBuilder(cfg, cfg.getBuilder());
		}
		
//		IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(project, manager.getBuilderID(targetBuilderID));
//		setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, info.getBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, "make")); //$NON-NLS-1$
//		setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, info.getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, "")); //$NON-NLS-1$
//		isDefaultBuildCmd = info.isDefaultBuildCmd();
//		isStopOnError = info.isStopOnError();
		manager.updateTarget(this);
		
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
	
//	Map getAttributeMap() {
//		return targetAttributes;
//	}

	public String getName() {
		return name;
	}

	public String getTargetBuilderID() {
		return targetBuilderID;
	}

	public boolean isStopOnError() {
		return builder.isStopOnError();
	}

	public void setStopOnError(boolean stopOnError) throws CoreException {
		builder.setStopOnError(stopOnError);
		manager.updateTarget(this);
	}

	public boolean isDefaultBuildCmd() {
		return builder.isDefaultBuildCmd();
	}

	public void setUseDefaultBuildCmd(boolean useDefault) throws CoreException {
		builder.setUseDefaultBuildCmd(useDefault);
		manager.updateTarget(this);
	}

	public IPath getBuildCommand() {
		return builder.getBuildCommand();
/*		if (isDefaultBuildCmd()) {
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
*/
	}

	public void setBuildCommand(IPath command) throws CoreException {
		builder.setBuildCommand(command);
//		setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, command.toString());
	}

	public String getBuildArguments() {
		return builder.getBuildArguments();
//		String result = getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, ""); //$NON-NLS-1$
//		try {
//			result = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(result, false);
//		} catch (CoreException e) {
//		}
//		return result;
	}

	public void setBuildArguments(String arguments) throws CoreException {
		builder.setBuildArguments(arguments);
//		setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, arguments);
	}
	
	public void setBuildTarget(String target) throws CoreException {
//		setBuildAttribute(IMakeTarget.BUILD_TARGET, target);
		builder.setIncrementalBuildTarget(target);
	}

	public String getBuildTarget() {
/*		String result = getBuildAttribute(IMakeTarget.BUILD_TARGET, ""); //$NON-NLS-1$
		try {
			result = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(result, false);
		} catch (CoreException e) {
		}
		return result;	
*/
		return builder.getIncrementalBuildTarget();
	}

	public void setRunAllBuilders(boolean runAllBuilders) throws CoreException {
		this.runAllBuidlers = runAllBuilders;
		manager.updateTarget(this);
	}

	public boolean runAllBuilders() {
		return runAllBuidlers;
	}

	public void setBuildAttribute(String name, String value) throws CoreException {
		if(BUILD_TARGET.equals(name))
			setBuildTarget(value);
		else
			builder.setBuildAttribute(name, value);
		manager.updateTarget(this);
	}

	public String getBuildAttribute(String name, String defaultValue) {
		String value = null;
		if(BUILD_TARGET.equals(name)){
			value = getBuildTarget();
		} else {
			value = builder.getBuildAttribute(name, defaultValue);
		}
		if(value == null)
			value = defaultValue;
		return value;
	}

	public IPath getBuildLocation() {
		return container.getLocation();
	}

	public void setBuildLocation(IPath location) throws CoreException {
		throw new UnsupportedOperationException();
	}

	public String[] getErrorParsers() {
		return builder.getErrorParsers();
	}

	public void setErrorParsers(String[] parsers) throws CoreException {
		builder.setErrorParsers(parsers);
	}

	public Map getExpandedEnvironment() throws CoreException {
		return builder.getExpandedEnvironment();
/*		Map env = null;
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
*/
	}
	
	public boolean appendProjectEnvironment() {
		return builder.appendEnvironment();
//		return appendProjectEnvironment;
	}

	public void setAppendProjectEnvironment(boolean append) {
		try {
			builder.setAppendEnvironment(append);
		} catch (CoreException e) {
			e.printStackTrace();
		}
//		appendProjectEnvironment = append;
	}	

	public Map getEnvironment() {
		return builder.getEnvironment();
	}

	public void setEnvironment(Map env) throws CoreException {
		builder.setEnvironment(env);
		manager.updateTarget(this);
	}

	public void setAppendEnvironment(boolean append) throws CoreException {
		builder.setAppendEnvironment(append);
//		appendEnvironment = append ? 1 : 0;
		manager.updateTarget(this);
	}	

	public boolean appendEnvironment() {
		return builder.appendEnvironment();
//		return appendEnvironment == USE_PROJECT_ENV_SETTING ? getProjectEnvSetting(): appendEnvironment == 1;
	}
	
/*	private boolean getProjectEnvSetting() {
		IMakeBuilderInfo projectInfo;
		try {
			projectInfo = MakeCorePlugin.createBuildInfo(getProject(), manager.getBuilderID(targetBuilderID));
			return projectInfo.appendEnvironment();
		} catch (CoreException e) {
		}
		return false;
	}
*/

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
	
	public IConfiguration getConfiguration(){
		return builder.getParent().getParent();
	}
/*	public IConfiguration getConfiguration() throws CoreException{
		IConfiguration builderCfg = builder.getParent().getParent();
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration cfg = info.getManagedProject().getConfiguration(builderCfg.getId());
		if(cfg == null){
			cfg = info.getDefaultConfiguration();
		}
		
		if(cfg != null){
			if(builderCfg != cfg){
				((Builder)builder).setParent(cfg.getToolChain());
			}
			return cfg;
		}
		throw new CoreException(new Status(
				IStatus.ERROR,
				ManagedBuilderCorePlugin.getUniqueIdentifier(),
				"builder configuration was removed"));
	}
*/
	public void build(IProgressMonitor monitor) throws CoreException {
		final String builderID = manager.getBuilderID(targetBuilderID);
/*		final HashMap infoMap = new HashMap();

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
*/
		
		
		IConfiguration cfg = builder.getParent().getParent();
		final Map infoMap = BuilderFactory.createBuildArgs(
				new IConfiguration[]{cfg},
				builder);
		
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

	public boolean isManagedBuildOn() {
		return builder.isManagedBuildOn();
	}

	public void setManagedBuildOn(boolean on) throws CoreException {
		builder.setManagedBuildOn(on);
	}

	public boolean supportsBuild(boolean managed) {
		return builder.supportsBuild(managed);
	}
	
	public void setConfiguration(IConfiguration cfg){
		if(getConfiguration() == cfg)
			return;
		builder = ManagedBuildManager.createCustomBuilder(cfg, builder);
	}

	public int getParallelizationNum() {
		return builder.getParallelizationNum();
	}

	public void setParallelizationNum(int num) throws CoreException {
		builder.setParallelizationNum(num);
	}

	public boolean supportsParallelBuild() {
		return builder.supportsParallelBuild();
	}

	public boolean supportsStopOnError(boolean on) {
		return builder.supportsStopOnError(on);
	}

	public boolean isParallelBuildOn() {
		return builder.isParallelBuildOn();
	}

	public void setParallelBuildOn(boolean on) throws CoreException {
		builder.setParallelBuildOn(on);
	}
}
