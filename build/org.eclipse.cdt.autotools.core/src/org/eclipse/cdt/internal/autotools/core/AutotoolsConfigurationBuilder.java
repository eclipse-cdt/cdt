package org.eclipse.cdt.internal.autotools.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.autotools.core.configure.AutotoolsConfigurationManager;
import org.eclipse.cdt.internal.autotools.core.configure.IAConfiguration;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IMultiConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;

public class AutotoolsConfigurationBuilder extends ACBuilder {

	public static final String BUILDER_NAME = "genmakebuilderV2"; //$NON-NLS-1$
	public static final String BUILDER_ID = AutotoolsPlugin.getPluginId() + "." + BUILDER_NAME; //$NON-NLS-1$
	public static final String OLD_BUILDER_ID = "org.eclipse.linuxtools.cdt.autotools.core.genmakebuilderV2"; //$NON-NLS-1$

	private static final String BUILD_STOPPED="AutotoolsMakefileBuilder.message.stopped"; //$NON-NLS-1$
	private AutotoolsNewMakeGenerator generator;

	public AutotoolsConfigurationBuilder() {
		super();
		generator = new AutotoolsNewMakeGenerator();
	}
	
	protected boolean isCdtProjectCreated(IProject project){
		ICProjectDescription des = CoreModel.getDefault().getProjectDescription(project, false);
		return des != null && !des.isCdtProjectCreating();
	}

	@Override
	protected IProject[] build(int kind, @SuppressWarnings("rawtypes") Map args, IProgressMonitor monitor)
	throws CoreException {
		IProject project = getProject();
		if(!isCdtProjectCreated(project))
			return project.getReferencedProjects();

		boolean bPerformBuild = true;
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		if (!shouldBuild(kind, info)) {
			return new IProject[0];
		}
		if (kind == IncrementalProjectBuilder.AUTO_BUILD) {
			IResourceDelta delta = getDelta(getProject());
			if (delta != null) {
				IResource res = delta.getResource();
				if (res != null) {
					bPerformBuild = res.getProject().equals(getProject());
				}
			} else {
				bPerformBuild = false;
			}
			IConfiguration cfg = info.getDefaultConfiguration();
			if (cfg != null) {
				IAConfiguration acfg = AutotoolsConfigurationManager.getInstance().findCfg(project, cfg.getName());
				if (acfg == null || acfg.isDirty())
					bPerformBuild = true;
			}
		}
		if (bPerformBuild) {
			MultiStatus result = performMakefileGeneration(project, info, monitor);
			if (result.getSeverity() == IStatus.ERROR) {
				// Failure to create Makefile, output error message to console.
				IConsole console = CCorePlugin.getDefault().getConsole();
				console.start(project);

				OutputStream cos = console.getOutputStream();
				String errormsg = AutotoolsPlugin.getResourceString(BUILD_STOPPED);
				StringBuffer buf = new StringBuffer(errormsg);
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
				buf.append("(").append(result.getMessage()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$

				try {
					cos.write(buf.toString().getBytes());
					cos.flush();
					cos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					AutotoolsPlugin.log(e);
				}
			}
		}
		checkCancel(monitor);
		return getProject().getReferencedProjects();
	}
	
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		final IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		if (shouldBuild(CLEAN_BUILD, info)) {
			IConfiguration icfg = info.getDefaultConfiguration();
			if (icfg instanceof IMultiConfiguration) {
				IMultiConfiguration mcfg = (IMultiConfiguration)icfg;
				IConfiguration[] cfgs = (IConfiguration[])mcfg.getItems();
				for (int i = 0; i < cfgs.length; ++i) {
					IAConfiguration cfg = AutotoolsConfigurationManager.getInstance().getConfiguration(project, icfg.getName());
					cfg.setDirty(true);
				}
			} else {
				IAConfiguration cfg = AutotoolsConfigurationManager.getInstance().getConfiguration(project, icfg.getName());
				cfg.setDirty(true);  // Mark Configuration dirty so next build will do full reconfigure
			}
		}
	}

	protected MultiStatus performMakefileGeneration(IProject project, IManagedBuildInfo info,
			IProgressMonitor monitor) throws CoreException {
		MultiStatus result;
		
		try {
			generator.initialize(project, info, monitor);
			result = generator.regenerateMakefiles(false);
		} catch (CoreException e) {
			String errMsg = AutotoolsPlugin.getResourceString("MakeGenerator.didnt.generate"); //$NON-NLS-1$
			result = new MultiStatus(AutotoolsPlugin.getUniqueIdentifier(), IStatus.ERROR,
					errMsg, e);
		}
		
		return result;
	}

	/**
	 * Check whether the build has been canceled.
	 */
	public void checkCancel(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled())
			throw new OperationCanceledException();
	}

	protected boolean shouldBuild(int kind, IManagedBuildInfo info) {
		IConfiguration cfg = info.getDefaultConfiguration();
		IBuilder builder = null;
		if (cfg != null) {
			builder = cfg.getEditableBuilder();
		switch (kind) {
		case IncrementalProjectBuilder.AUTO_BUILD :
			return builder.isAutoBuildEnable();
		case IncrementalProjectBuilder.INCREMENTAL_BUILD : // now treated as the same!
		case IncrementalProjectBuilder.FULL_BUILD :
			return builder.isFullBuildEnabled() | builder.isIncrementalBuildEnabled() ;
		case IncrementalProjectBuilder.CLEAN_BUILD :
			return builder.isCleanBuildEnabled();
		}
		}
		return true;
	}

}
		
