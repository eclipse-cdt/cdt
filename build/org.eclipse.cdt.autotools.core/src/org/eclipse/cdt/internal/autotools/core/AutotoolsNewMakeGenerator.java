/*******************************************************************************
 * Copyright (c) 2009, 2012 Red Hat Inc.and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated        - initial API and implementation
 *     Anna Dushistova (MontaVista)- [375007] [autotools] allow absolute paths for configure scripts
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.autotools.core.configure.AutotoolsConfigurationManager;
import org.eclipse.cdt.internal.autotools.core.configure.IAConfiguration;
import org.eclipse.cdt.internal.autotools.core.configure.IConfigureOption;
import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.makefile.ITarget;
import org.eclipse.cdt.make.core.makefile.ITargetRule;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IMultiConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.newmake.core.IMakeCommonBuildInfo;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;


@SuppressWarnings("deprecation")
public class AutotoolsNewMakeGenerator extends MarkerGenerator {

	public final String CONFIG_STATUS = "config.status"; //$NON-NLS-1$
	public final String MAKEFILE = "Makefile"; //$NON-NLS-1$
	public final String MAKEFILE_CVS = "Makefile.cvs"; //$NON-NLS-1$
	public final String SETTINGS_FILE_NAME = ".cdtconfigure"; //$NON-NLS-1$
	public final String SHELL_COMMAND = "sh"; //$NON-NLS-1$
	
	public final String AUTOGEN_TOOL_ID = "autogen"; //$NON-NLS-1$
	public final String CONFIGURE_TOOL_ID = "configure"; //$NON-NLS-1$
	
	public final String GENERATED_TARGET = AutotoolsPlugin.PLUGIN_ID + ".generated.MakeTarget"; //$NON-NLS-1$

	private static final String MAKE_TARGET_KEY = MakeCorePlugin.getUniqueIdentifier() + ".buildtargets"; //$NON-NLS-1$
	private static final String BUILD_TARGET_ELEMENT = "buildTargets"; //$NON-NLS-1$
	private static final String TARGET_ELEMENT = "target"; //$NON-NLS-1$
	private static final String TARGET_ATTR_ID = "targetID"; //$NON-NLS-1$
	private static final String TARGET_ATTR_PATH = "path"; //$NON-NLS-1$
	private static final String TARGET_ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String TARGET_STOP_ON_ERROR = "stopOnError"; //$NON-NLS-1$
	private static final String TARGET_USE_DEFAULT_CMD = "useDefaultCommand"; //$NON-NLS-1$
	private static final String TARGET_ARGUMENTS = "buildArguments"; //$NON-NLS-1$
	private static final String TARGET_COMMAND = "buildCommand"; //$NON-NLS-1$
	private static final String TARGET_RUN_ALL_BUILDERS = "runAllBuilders";
	private static final String TARGET = "buildTarget"; //$NON-NLS-1$
	private static final String DEFAULT_AUTORECONF = "autoreconf"; //$NON-NLS-1$

	private IProject project;

	private IProgressMonitor monitor;

	private IPath buildLocation;
	private String buildDir;
	private String srcDir;
	private String winOSType = "";

	private IConfiguration cfg;
	private ICConfigurationDescription cdesc;
	private IAConfiguration toolsCfg;
	private IBuilder builder;

	public void generateDependencies() throws CoreException {
		// TODO Auto-generated method stub

	}

	public MultiStatus generateMakefiles(IResourceDelta delta)
			throws CoreException {
		return regenerateMakefiles(false);
	}

	private void initializeBuildConfigDirs(IConfiguration c, IAConfiguration a) {
		IBuilder b = c.getBuilder();
		IPath buildDirectory = b.getBuildLocation();
		if (buildDirectory == null || buildDirectory.isEmpty()) {
			// default build directory to project directory
			buildDirectory = project.getLocation();
		}
		buildLocation = buildDirectory;
		buildDir = buildDirectory.toString();
		srcDir = a.getConfigToolDirectory();
		try {
			String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValue(srcDir, "", null, 
					IBuildMacroProvider.CONTEXT_CONFIGURATION, c);
			srcDir = resolved;
		} catch (BuildMacroException e) {
			// do nothing
		}
	}
	
	public void initialize(IProject project, IManagedBuildInfo info,
			IProgressMonitor monitor) {
		this.project = project;
		ICProjectDescription pdesc = CCorePlugin.getDefault().getProjectDescription(project);
		this.cdesc = pdesc.getActiveConfiguration();
		this.cfg = info.getDefaultConfiguration();
		this.builder = cfg.getBuilder();
		this.monitor = monitor;
		CUIPlugin.getDefault().getPreferenceStore().getString("dummy");
	}

	public IProject getProject() {
		return project;
	}
	
	public boolean isGeneratedResource(IResource resource) {
		// TODO Auto-generated method stub
		return false;
	}

	public void regenerateDependencies(boolean force) throws CoreException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc) Check whether the build has been cancelled. Cancellation
	 * requests propagated to the caller by throwing <code>OperationCanceledException</code>.
	 * 
	 * @see org.eclipse.core.runtime.OperationCanceledException#OperationCanceledException()
	 */
	protected void checkCancel() {
		if (monitor != null && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	/*
	 * (non-Javadoc) Return or create the makefile needed for the build. If we
	 * are creating the resource, set the derived bit to true so the CM system
	 * ignores the contents. If the resource exists, respect the existing
	 * derived setting.
	 * 
	 * @param makefilePath @return IFile
	 */
	protected IFile createFile(IPath makefilePath) throws CoreException {
		// Create or get the handle for the makefile
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();
		IFile newFile = root.getFileForLocation(makefilePath);
		if (newFile == null) {
			newFile = root.getFile(makefilePath);
		}
		// Create the file if it does not exist
		ByteArrayInputStream contents = new ByteArrayInputStream(new byte[0]);
		try {
			newFile.create(contents, false, new SubProgressMonitor(monitor, 1));
			// Make sure the new file is marked as derived
			if (!newFile.isDerived()) {
				newFile.setDerived(true);
			}

		} catch (CoreException e) {
			// If the file already existed locally, just refresh to get contents
			if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
				newFile.refreshLocal(IResource.DEPTH_ZERO, null);
			else
				throw e;
		}

		return newFile;
	}

	/*
	 * Create a directory
	 * 
	 * @param boolean @return whether the directory was created
	 */
	private boolean createDirectory(String dirName) throws CoreException {
		// Create or get the handle for the build directory
		IPath path = new Path(dirName);
		boolean rc = true;
		if (dirName.length() == 0 || dirName.equals("."))
			path = project.getLocation().append(dirName);
		File f = path.toFile();
		if (!f.exists())
			rc = f.mkdirs();

		return rc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#getMakefileName()
	 */
	public String getMakefileName() {
		return MAKEFILE;
	}

	/**
	 * Reconfigure the project.
	 * @return MultiStatus status of regeneration operation
	 * @throws CoreException
	 */
	public MultiStatus reconfigure() throws CoreException {
		return regenerateMakefiles(true);
	}
	
	public MultiStatus regenerateMakefiles(boolean reconfigure) throws CoreException {
		MultiStatus status = null;
		if (cfg instanceof IMultiConfiguration) {
			IMultiConfiguration mfcg = (IMultiConfiguration)cfg;
			Object[] objs = mfcg.getItems();
			for (int i = 0; i < objs.length; ++i) {
				IConfiguration icfg = (IConfiguration)objs[i];
				Status rc = regenerateMakefiles(icfg, reconfigure);
				if (!rc.isOK()) {
					if(status == null){
						status = new MultiStatus(
								AutotoolsPlugin.getUniqueIdentifier(),
								IStatus.ERROR,
								"",
								null);
					}
					status.add(rc);	
				}
			}
		} else {
			Status rc = regenerateMakefiles(cfg, reconfigure);
			if (!rc.isOK()) {
				if(status == null){
					status = new MultiStatus(
							AutotoolsPlugin.getUniqueIdentifier(),
							IStatus.ERROR,
							"",
							null);
				}
				status.add(rc);	
			}
		}
		if(status == null){
			status = new MultiStatus(
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					IStatus.OK,
					"",
					null);
		}
		return status;
	}
	
	private Status regenerateMakefiles(IConfiguration icfg, boolean reconfigure) throws CoreException {
		MultiStatus status;
		int rc = IStatus.OK;
		String errMsg = new String();
		boolean needFullConfigure = false;

		// See if the user has cancelled the build
		checkCancel();

		// Synchronize the Autotools configurations with the Project Description
		AutotoolsConfigurationManager.getInstance().syncConfigurations(getProject());	
		toolsCfg = AutotoolsConfigurationManager.getInstance().getConfiguration(getProject(), icfg.getId());
			
		initializeBuildConfigDirs(icfg, toolsCfg);
		// Create the top-level directory for the build output
		if (!createDirectory(buildDir)) {
			rc = IStatus.ERROR;
			errMsg = AutotoolsPlugin.getFormattedString("MakeGenerator.createdir.error", //$NON-NLS-1$
					new String[] {buildDir});
			status = new MultiStatus(AutotoolsPlugin
						.getUniqueIdentifier(), rc, errMsg, null);
		}
		checkCancel();

		// // How did we do
		// if (!getInvalidDirList().isEmpty()) {
		// status = new MultiStatus (
		// ManagedBuilderCorePlugin.getUniqueIdentifier(),
		// IStatus.WARNING,
		// new String(),
		// null);
		// // Add a new status for each of the bad folders
		// iter = getInvalidDirList().iterator();
		// while (iter.hasNext()) {
		// status.add(new Status (
		// IStatus.WARNING,
		// ManagedBuilderCorePlugin.getUniqueIdentifier(),
		// SPACES_IN_PATH,
		// ((IContainer)iter.next()).getFullPath().toString(),
		// null));
		// }
		// } else {
		// status = new MultiStatus(
		// ManagedBuilderCorePlugin.getUniqueIdentifier(),
		// IStatus.OK,
		// new String(),
		// null);
		// }

		// Get a build console for the project
		IConsole console = CCorePlugin.getDefault().getConsole("org.eclipse.cdt.autotools.ui.configureConsole"); //$NON-NLS-1$
		boolean consoleStart = true;
		
		// Make sure there's a monitor to cancel the build
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		try {
			// If a config.status file exists in the build directory, we call it
			// to
			// regenerate the makefile
			IPath configfile = buildLocation.append(CONFIG_STATUS);
			IPath topConfigFile = project.getLocation().append(CONFIG_STATUS);
			IPath makefilePath = buildLocation.append(MAKEFILE);
			IPath topMakefilePath = project.getLocation().append(MAKEFILE);
			File configStatus = configfile.toFile();
			File topConfigStatus = topConfigFile.toFile();
			File makefile = makefilePath.toFile();
			File topMakefile = topMakefilePath.toFile();

			// Check if a configure has been done in the top-level source directory
			if (!(configfile.equals(topConfigFile)) && topConfigStatus.exists()) {
				// Must perform distclean on source directory because 2nd configuration
				// cannot occur otherwise
				// There is a make target for cleaning.
				if (topMakefile != null && topMakefile.exists()) {
					String[] makeargs = new String[1];
					IPath makeCmd = new Path("make"); //$NON-NLS-1$
					String target = null;
					try {
						target = getProject().getPersistentProperty(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET);
					} catch (CoreException ce) {
						// do nothing
					}
					if (target == null)
						target = AutotoolsPropertyConstants.CLEAN_MAKE_TARGET_DEFAULT;
					String args = builder.getBuildArguments();
					if (args != null && !(args = args.trim()).equals("")) { //$NON-NLS-1$
						String[] newArgs = makeArray(args);
						makeargs = new String[newArgs.length + 1];
						System.arraycopy(newArgs, 0, makeargs, 0, newArgs.length);
					}
					makeargs[makeargs.length - 1] = target;
					rc = runCommand(makeCmd,
							project.getLocation(),
							makeargs,
							AutotoolsPlugin.getResourceString("MakeGenerator.clean.topdir"), //$NON-NLS-1$
							errMsg, console, consoleStart);
					consoleStart = false;
				}
			}
			// If the active configuration is dirty, then we need to do a full
			// reconfigure.
			if (toolsCfg.isDirty() || reconfigure) {
				needFullConfigure = true;
				// If we are going to do a full reconfigure, then if the current
				// build directory exists, we should clean it out first.  This is
				// because the reconfiguration could change compile flags, etc..
				// and the Makefile might not detect a rebuild is required.  In
				// addition, the build directory itself could have been changed and
				// we should remove the previous build.
				if (buildLocation != null && buildLocation.toFile().exists()) {
					// See what type of cleaning the user has set up in the
					// build properties dialog.
					String cleanDelete = null;
					try {
						cleanDelete = getProject().getPersistentProperty(AutotoolsPropertyConstants.CLEAN_DELETE);
					} catch (CoreException ce) {
						// do nothing
					}
					
					if (cleanDelete != null && cleanDelete.equals(AutotoolsPropertyConstants.TRUE))
						buildLocation.toFile().delete();
					else {
						// There is a make target for cleaning.
						if (makefile != null && makefile.exists()) {
							String[] makeargs = new String[1];
							IPath makeCmd = new Path("make"); //$NON-NLS-1$
							String target = null;
							try {
								target = getProject().getPersistentProperty(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET);
							} catch (CoreException ce) {
								// do nothing
							}
							if (target == null)
								target = AutotoolsPropertyConstants.CLEAN_MAKE_TARGET_DEFAULT;
							String args = builder.getBuildArguments();
							if (args != null && !(args = args.trim()).equals("")) { //$NON-NLS-1$
								String[] newArgs = makeArray(args);
								makeargs = new String[newArgs.length + 1];
								System.arraycopy(newArgs, 0, makeargs, 0, newArgs.length);
							}
							makeargs[makeargs.length - 1] = target;
							rc = runCommand(makeCmd,
									buildLocation,
									makeargs,
									AutotoolsPlugin.getFormattedString("MakeGenerator.clean.builddir", new String[]{buildDir}), //$NON-NLS-1$
									errMsg, console, consoleStart);
							consoleStart = false;
						}
					}
				}
				// Mark the scanner info as dirty.
				try {
					project.setSessionProperty(AutotoolsPropertyConstants.SCANNER_INFO_DIRTY, Boolean.TRUE);
				} catch (CoreException ce) {
					// do nothing
				}
			}
			
			ArrayList<String> configureEnvs = new ArrayList<String>();
			ArrayList<String> configureCmdParms = new ArrayList<String>();
			IPath configurePath = getConfigurePath(configureEnvs, configureCmdParms);
			String[] configArgs = getConfigArgs(configureCmdParms);
			ArrayList<String> autogenEnvs = new ArrayList<String>();
			ArrayList<String> autogenCmdParms = new ArrayList<String>();
			IPath autogenPath = getAutogenPath(autogenEnvs, autogenCmdParms);
			
			// Check if we have a config.status (meaning configure has already run).
    		if (!needFullConfigure && configStatus != null && configStatus.exists()) {
			    // If no corresponding Makefile in the same build location, then we
	            // can simply run config.status again to ensure the top level Makefile has been
				// created.
				if (makefile == null || !makefile.exists()) {
					rc = runScript(configfile, buildLocation, null, 
							AutotoolsPlugin.getFormattedString("MakeGenerator.run.config.status", new String[]{buildDir}), //$NON-NLS-1$
							errMsg, console, null, consoleStart);
					consoleStart = false;
				}
			}
			// Look for configure and configure from scratch
			else if (configurePath.toFile().exists()) {
				rc = runScript(configurePath, 
						buildLocation,
						configArgs, 
						AutotoolsPlugin.getFormattedString("MakeGenerator.gen.makefile", new String[]{buildDir}), //$NON-NLS-1$
						errMsg, console, configureEnvs, consoleStart);
				consoleStart = false;
				if (rc != IStatus.ERROR) {
					File makefileFile = buildLocation.append(MAKEFILE).toFile();
					addMakeTargetsToManager(makefileFile);
					// TODO: should we do something special if configure doesn't
					// return ok?
					toolsCfg.setDirty(false);
				}
			}
			// If no configure, look for autogen.sh which may create configure and
    		// possibly even run it.
			else if (autogenPath.toFile().exists()) {
				// Remove the existing config.status file since we use it
				// to figure out if configure was run.
				if (configStatus.exists())
					configStatus.delete();
				// Get any user-specified arguments for autogen.
				String[] autogenArgs = getAutogenArgs(autogenCmdParms);
				rc = runScript(autogenPath,
						autogenPath.removeLastSegments(1), autogenArgs,
						AutotoolsPlugin.getFormattedString("MakeGenerator.autogen.sh", new String[]{buildDir}), //$NON-NLS-1$
						errMsg, console, autogenEnvs, consoleStart);
				consoleStart = false;
				if (rc != IStatus.ERROR) {
					configStatus = configfile.toFile();
					// Check for config.status.  If it is created, then
					// autogen.sh ran configure and we should not run it
					// ourselves.
					if (configStatus == null || !configStatus.exists()) {
						if (!configurePath.toFile().exists()) {
							// no configure script either...try running autoreconf
							String[] reconfArgs = new String[1];
							String reconfCmd = project.getPersistentProperty(AutotoolsPropertyConstants.AUTORECONF_TOOL);
							if (reconfCmd == null)
								reconfCmd = DEFAULT_AUTORECONF;
							IPath reconfCmdPath = new Path(reconfCmd);
							reconfArgs[0] = "-i"; //$NON-NLS-1$
							rc = runScript(reconfCmdPath,
									project.getLocation().append(srcDir),
									reconfArgs,
									AutotoolsPlugin.getFormattedString("MakeGenerator.autoreconf", new String[]{buildDir}), //$NON-NLS-1$
									errMsg, console, null, consoleStart);
							consoleStart = false;
						}
						// Check if configure generated and if yes, run it.
						if (rc != IStatus.ERROR && configurePath.toFile().exists()) {
							rc = runScript(configurePath, 
									buildLocation,
									configArgs, 
									AutotoolsPlugin.getFormattedString("MakeGenerator.gen.makefile", new String[]{buildDir}), //$NON-NLS-1$
									errMsg, console, configureEnvs, false);
							if (rc != IStatus.ERROR) {
								File makefileFile = buildLocation.append(MAKEFILE).toFile();
								addMakeTargetsToManager(makefileFile);
								toolsCfg.setDirty(false);
							}
						}
					} else {
						File makefileFile = buildLocation.append(MAKEFILE).toFile();
						addMakeTargetsToManager(makefileFile);
						toolsCfg.setDirty(false);
					}
				}
			}
			// If nothing this far, look for a Makefile.cvs file which needs to be run. 
			else if (makefileCvsExists()) {
				String[] makeargs = new String[1];
				IPath makeCmd = new Path("make"); //$NON-NLS-1$
				makeargs[0] = "-f" + getMakefileCVSPath().toOSString(); //$NON-NLS-1$
				rc = runCommand(makeCmd,
						project.getLocation().append(buildDir),
						makeargs,
						AutotoolsPlugin.getFormattedString("MakeGenerator.makefile.cvs", new String[]{buildDir}), //$NON-NLS-1$
						errMsg, console, consoleStart);
				consoleStart = false;
				if (rc != IStatus.ERROR) {
					File makefileFile = project.getLocation().append(buildDir)
					.append(MAKEFILE).toFile();
					addMakeTargetsToManager(makefileFile);
					toolsCfg.setDirty(false);
				}
			}
			// If nothing this far, try running autoreconf -i
			else {
				String[] reconfArgs = new String[1];
				String reconfCmd = project.getPersistentProperty(AutotoolsPropertyConstants.AUTORECONF_TOOL);
				if (reconfCmd == null)
					reconfCmd = DEFAULT_AUTORECONF;
				IPath reconfCmdPath = new Path(reconfCmd);
				reconfArgs[0] = "-i"; //$NON-NLS-1$
				rc = runScript(reconfCmdPath,
						project.getLocation().append(srcDir),
						reconfArgs,
						AutotoolsPlugin.getFormattedString("MakeGenerator.autoreconf", new String[]{buildDir}), //$NON-NLS-1$
						errMsg, console, null, consoleStart);
				consoleStart = false;
				// Check if configure generated and if yes, run it.
				if (rc != IStatus.ERROR) {
					if (configurePath.toFile().exists()) {
						rc = runScript(configurePath, 
								buildLocation,
								configArgs, 
								AutotoolsPlugin.getFormattedString("MakeGenerator.gen.makefile", new String[]{buildDir}), //$NON-NLS-1$
								errMsg, console, configureEnvs, false);
						if (rc != IStatus.ERROR) {
							File makefileFile = buildLocation.append(MAKEFILE).toFile();
							addMakeTargetsToManager(makefileFile);
							// TODO: should we do something special if configure doesn't
							// return ok?
							toolsCfg.setDirty(false);
						}
					}
				}
			}
    		// If we didn't create a Makefile, consider that an error.
			if (makefile == null || !makefile.exists()) {
				rc = IStatus.ERROR;
				errMsg = AutotoolsPlugin.getResourceString("MakeGenerator.didnt.generate"); //$NON-NLS-1$
			}
		} catch (Exception e) {
			e.printStackTrace();
			// forgetLastBuiltState();
			rc = IStatus.ERROR;
		} finally {
			// getGenerationProblems().clear();
			status = new MultiStatus(AutotoolsPlugin
					.getUniqueIdentifier(), rc, errMsg, null);
			if (rc != IStatus.OK)
				status.add(new Status (
						rc,
						AutotoolsPlugin.getUniqueIdentifier(),
						0,
						errMsg,
						null));
		}
		return status;
	}

	/**
	 * Strip a command of VAR=VALUE pairs that appear ahead or behind the command and add
	 * them to a list of environment variables.
	 *
	 * @param command - command to strip
	 * @param envVars - ArrayList to add environment variables to
	 * @return stripped command
	 */
	public static String stripEnvVars(String command, ArrayList<String> envVars) {
		Pattern p1 = Pattern.compile("(\\w+[=]\\\".*?\\\"\\s+)\\w+.*");
		Pattern p2 = Pattern.compile("(\\w+[=]'.*?'\\s+)\\w+.*");
		Pattern p3 = Pattern.compile("(\\w+[=][^\\s]+\\s+)\\w+.*");
		Pattern p4 = Pattern.compile("\\w+\\s+(\\w+[=]\\\".*?\\\"\\s*)+.*");
		Pattern p5 = Pattern.compile("\\w+\\s+(\\w+[=]'.*?'\\s*)+.*"); 
		Pattern p6 = Pattern.compile("\\w+\\s+(\\w+[=][^\\s]+).*");
		boolean finished = false;
		while (!finished) {
			Matcher m1 = p1.matcher(command);
			if (m1.matches()) {
				command = command.replaceFirst("\\w+[=]\\\".*?\\\"","").trim();
				String s = m1.group(1).trim();
				envVars.add(s.replaceAll("\\\"", ""));
			} else {
				Matcher m2 = p2.matcher(command);
				if (m2.matches()) {
					command = command.replaceFirst("\\w+[=]'.*?'", "").trim();
					String s = m2.group(1).trim();
					envVars.add(s.replaceAll("'", ""));
				} else {
					Matcher m3 = p3.matcher(command);
					if (m3.matches()) {
						command = command.replaceFirst("\\w+[=][^\\s]+", "").trim();
						envVars.add(m3.group(1).trim());
					} else {
						Matcher m4 = p4.matcher(command);
						if (m4.matches()) {
							command = command.replaceFirst("\\w+[=]\\\".*?\\\"","").trim();
							String s = m4.group(1).trim();
							envVars.add(s.replaceAll("\\\"", ""));
						} else {
							Matcher m5 = p5.matcher(command);
							if (m5.matches()) {
								command = command.replaceFirst("\\w+[=]'.*?'", "").trim();
								String s = m5.group(1).trim();
								envVars.add(s.replaceAll("'", ""));
							} else {						
								Matcher m6 = p6.matcher(command);
								if (m6.matches()) {
									command = command.replaceFirst("\\w+[=][^\\s+]+", "").trim();
									envVars.add(m6.group(1).trim());
								} else {
									finished = true;
								}
							}
						}
					}
				}
			}
		}
		return command;
	}
	
	/**
	 * Strip a configure option of VAR=VALUE pairs and add
	 * them to a list of environment variables.
	 *
	 * @param str - string to strip
	 * @param envVars - ArrayList to add environment variables to
	 * @return stripped option
	 */
	public static String stripEnvVarsFromOption(String str, ArrayList<String> envVars) {
		Pattern p1 = Pattern.compile("(\\w+[=]\\\".*?\\\"\\s*).*");
		Pattern p2 = Pattern.compile("(\\w+[=]'.*?'\\s*).*");
		Pattern p3 = Pattern.compile("(\\w+[=][^\\s]+).*");
		boolean finished = false;
		while (!finished) {
			Matcher m1 = p1.matcher(str);
			if (m1.matches()) {
				str = str.replaceFirst("\\w+[=]\\\".*?\\\"","").trim();
				String s = m1.group(1).trim();
				envVars.add(s.replaceAll("\\\"", ""));
			} else {
				Matcher m2 = p2.matcher(str);
				if (m2.matches()) {
					str = str.replaceFirst("\\w+[=]'.*?'", "").trim();
					String s = m2.group(1).trim();
					envVars.add(s.replaceAll("'", ""));
				} else {
					Matcher m3 = p3.matcher(str);
					if (m3.matches()) {
						str = str.replaceFirst("\\w+[=][^\\s]+", "").trim();
						envVars.add(m3.group(1).trim());
					} else {
						finished = true;
					}
				}
			}
		}
		return str;
	}

	private IPath getBuildPath(){
		return new Path(this.buildDir); 
	}
	
	private IPath getSourcePath(){
		IPath sourcePath;
		if (srcDir.equals(""))
			sourcePath = project.getLocation();
		else
			sourcePath = project.getLocation().append(srcDir);
		return sourcePath;
	}
	
	protected IPath getConfigurePath(ArrayList<String> envVars, ArrayList<String> cmdParms) {
		IPath configPath;
		IConfigureOption configOption = toolsCfg.getOption(CONFIGURE_TOOL_ID);
		String command = "configure"; // $NON-NLS-1$
		if (configOption != null)
			command = stripEnvVars(configOption.getValue().trim(), envVars);
			
		String[] tokens = command.split("\\s");
		if (tokens.length > 1) {
			command = tokens[0];
			for (int i = 1; i < tokens.length; ++i)
				cmdParms.add(tokens[i]);
		}
		if (Path.fromOSString(command).isAbsolute()) {
			configPath = new Path(command);
		} else {
			if (srcDir.equals(""))
				configPath = project.getLocation().append(command);
			else
				configPath = project.getLocation().append(srcDir)
						.append(command);
		}
		return configPath;
	}

	protected IPath getMakefileCVSPath() {
		IPath makefileCVSPath;
		if (srcDir.equals(""))
			makefileCVSPath = project.getLocation().append(MAKEFILE_CVS);
		else
			makefileCVSPath= project.getLocation().append(srcDir).append(
					MAKEFILE_CVS);
		return makefileCVSPath;
	}
	
	protected boolean makefileCvsExists() {
		IPath makefileCVSPath = getMakefileCVSPath();
		return makefileCVSPath.toFile().exists();
	}

	protected IPath getAutogenPath(ArrayList<String> envVars, ArrayList<String> cmdParms) {
		IPath autogenPath;
		IConfigureOption autogenOption = toolsCfg.getOption(AUTOGEN_TOOL_ID);
		String command = "autogen.sh"; // $NON-NLS-1$
		if (autogenOption != null)
			command = stripEnvVars(autogenOption.getValue().trim(), envVars);

		String[] tokens = command.split("\\s");
		if (tokens.length > 1) {
			command = tokens[0];
			for (int i = 1; i < tokens.length; ++i)
				cmdParms.add(tokens[i]);
		}
			
		if (srcDir.equals(""))
			autogenPath = project.getLocation().append(command);
		else
			autogenPath = project.getLocation().append(srcDir).append(command);
		return autogenPath;
	}
	
	private String[] getAutogenArgs(ArrayList<String> cmdParms) {
		// Get the arguments to be passed to config from build model
		ArrayList<String> autogenArgs = toolsCfg.getToolArgs(AUTOGEN_TOOL_ID);
		cmdParms.addAll(autogenArgs);
		return cmdParms.toArray(new String[cmdParms.size()]);
	}

	private String[] getConfigArgs(ArrayList<String> cmdParms) {
		// Get the arguments to be passed to config from build model
		ArrayList<String> configArgs = toolsCfg.getToolArgs(CONFIGURE_TOOL_ID);
		cmdParms.addAll(configArgs);
		return cmdParms.toArray(new String[cmdParms.size()]);
	}

	// Run a command or executable (e.g. make).
	private int runCommand(IPath commandPath, IPath runPath, String[] args,
			String jobDescription, String errMsg, IConsole console, 
			boolean consoleStart) throws BuildException, CoreException,
			NullPointerException, IOException {

		int rc = IStatus.OK;
		
		removeAllMarkers(project);
		
		String[] configTargets = args;
		if (args == null)
			configTargets = new String[0];
		
		for (int i = 0; i < configTargets.length; ++i) {
			// try to resolve the build macros in any argument
			try{
				String resolved =
					ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
							configTargets[i],
							"", //$NON-NLS-1$
							" ", //$NON-NLS-1$
							IBuildMacroProvider.CONTEXT_CONFIGURATION,
							cfg);
				configTargets[i] = resolved;
			} catch (BuildMacroException e) {
			}
		}
	
		String[] msgs = new String[2];
		msgs[0] = commandPath.toString();
		msgs[1] = project.getName();
		monitor.subTask(AutotoolsPlugin.getFormattedString(
				"MakeGenerator.make.message", msgs)); //$NON-NLS-1$


		ConsoleOutputStream consoleOutStream = null;
		StringBuffer buf = new StringBuffer();

		// Launch command - main invocation
		if (consoleStart)
			console.start(project);
		
		try {
			consoleOutStream = console.getOutputStream();
			String[] consoleHeader = new String[3];

			consoleHeader[0] = jobDescription;
			consoleHeader[1] = toolsCfg.getId();
			consoleHeader[2] = project.getName();
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
			buf.append(jobDescription);
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$

			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();

			// Get a launcher for the config command
			CommandLauncher launcher = new CommandLauncher();
			// Set the environment
			IEnvironmentVariable variables[] = 
					CCorePlugin.getDefault().getBuildEnvironmentManager().getVariables(cdesc, true);
			String[] env = null;
			ArrayList<String> envList = new ArrayList<String>();
			if (variables != null) {
				for (int i = 0; i < variables.length; i++) {
					envList.add(variables[i].getName()
							+ "=" + variables[i].getValue()); //$NON-NLS-1$
				}
				env = (String[]) envList.toArray(new String[envList.size()]);
			}

			// Hook up an error parser manager
			URI uri = URIUtil.toURI(runPath);
			ErrorParserManager epm = new ErrorParserManager(project, uri, this, new String[] {ErrorParser.ID});
			epm.setOutputStream(consoleOutStream);
			epm.addErrorParser(ErrorParser.ID, new ErrorParser(getSourcePath(), getBuildPath()));

			OutputStream stdout = epm.getOutputStream();
			OutputStream stderr = stdout;

			launcher.showCommand(true);
			Process proc = launcher.execute(commandPath, configTargets, env,
					runPath, new NullProgressMonitor());
			if (proc != null) {
				try {
					// Close the input of the process since we will never write to
					// it
					proc.getOutputStream().close();
				} catch (IOException e) {
				}

				if (launcher.waitAndRead(stdout, stderr, new SubProgressMonitor(
						monitor, IProgressMonitor.UNKNOWN)) != CommandLauncher.OK) {
					errMsg = launcher.getErrorMessage();
				}

				// Force a resync of the projects without allowing the user to
				// cancel.
				// This is probably unkind, but short of this there is no way to
				// ensure
				// the UI is up-to-date with the build results
				// monitor.subTask(ManagedMakeMessages
				// .getResourceString(REFRESH));
				monitor.subTask(AutotoolsPlugin.getResourceString("MakeGenerator.refresh")); //$NON-NLS-1$
				try {
					project.refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					monitor.subTask(AutotoolsPlugin
							.getResourceString("MakeGenerator.refresh.error")); //$NON-NLS-1$
				}
			} else {
				errMsg = launcher.getErrorMessage();
			}

			// Report either the success or failure of our mission
			buf = new StringBuffer();
			if (errMsg != null && errMsg.length() > 0) {
				String errorDesc = AutotoolsPlugin
						.getResourceString("MakeGenerator.generation.error"); //$NON-NLS-1$
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				buf.append(errorDesc);
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				buf.append("(").append(errMsg).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
				rc = IStatus.ERROR;
			} else if (proc.exitValue() >= 1 || proc.exitValue() < 0) {
				// We have an invalid return code from configuration.
				String[] errArg = new String[2];
				errArg[0] = Integer.toString(proc.exitValue());
				errArg[1] = commandPath.toString();
				errMsg = AutotoolsPlugin.getFormattedString(
						"MakeGenerator.config.error", errArg); //$NON-NLS-1$
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				buf.append(AutotoolsPlugin.getResourceString("MakeGenerator.generation.error")); //$NON-NLS-1$
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				if (proc.exitValue() == 1)
					rc = IStatus.WARNING;
				else
					rc = IStatus.ERROR;
			} else {
				// Report a successful build
				String successMsg = 
						AutotoolsPlugin.getResourceString("MakeGenerator.success"); //$NON-NLS-1$
				buf.append(successMsg);
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				rc = IStatus.OK;
			}

			// Write message on the console
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();
			// // Generate any error markers that the build has discovered
			// monitor.subTask(ManagedMakeMessages
			// .getResourceString(MARKERS));
			// epm.reportProblems();

		} finally {
			consoleOutStream.close();
		}
		
		// If we have an error and no specific error markers, use the default error marker.
		if (rc == IStatus.ERROR && !hasMarkers(project)) {
			addMarker(project, -1, errMsg, SEVERITY_ERROR_BUILD, null);
		}
		
		return rc;
	}

	// Method to get the Win OS Type to distinguish between Cygwin and MingW
	private String getWinOSType() {
		if (winOSType.equals("")) {
			try {
				CommandLauncher launcher = new CommandLauncher();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				launcher.execute(
						new Path(SHELL_COMMAND), //$NON-NLS-1$
						new String[] { "-c", "echo $OSTYPE" }, //$NON-NLS-1$ //$NON-NLS-2$
						new String[0],
						new Path("."), //$NON-NLS-1$
						new NullProgressMonitor());
				if (launcher.waitAndRead(out, out) == CommandLauncher.OK)
					winOSType = out.toString().trim();
			} catch (CoreException e) {
				// do nothing
			}
		}
		return winOSType;
	}	

    // Get the path string.  We add a Win check to handle MingW.
    // For MingW, we would rather represent C:\a\b as /C/a/b which
    // doesn't cause Makefile to choke. For Cygwin we use /cygdrive/C/a/b
    private String getPathString(IPath path) {
            String s = path.toString();
            if (Platform.getOS().equals(Platform.OS_WIN32)) {
            	if (getWinOSType().equals("cygwin")) {
                    s = s.replaceAll("^([A-Z])(:)", "/cygdrive/$1");            		
            	} else {
                    s = s.replaceAll("^([A-Z])(:)", "/$1");            		
            	}
            }
            return s;
    }

	// Run an autotools script (e.g. configure, autogen.sh, config.status).
	private int runScript(IPath commandPath, IPath runPath, String[] args,
			String jobDescription, String errMsg, IConsole console,
			ArrayList<String> additionalEnvs, 
			boolean consoleStart) throws BuildException, CoreException,
			NullPointerException, IOException {

		int rc = IStatus.OK;
		boolean removePWD = false;
		
		removeAllMarkers(project);
		
		// We want to run the script via the shell command.  So, we add the command
		// script as the first argument and expect "sh" to be on the runtime path.
		// Any other arguments are placed after the script name.
		String[] configTargets = null;
		if (args == null)
			configTargets = new String[1];
		else {
			configTargets = new String[args.length+1];
			System.arraycopy(args, 0, configTargets, 1, args.length);
		}
        configTargets[0] = getPathString(commandPath);

        // Fix for bug #343879
        if (Platform.getOS().equals(Platform.OS_WIN32)
                || Platform.getOS().equals(Platform.OS_MACOSX))
        	removePWD = true;
        
        // Fix for bug #343731 and bug #371277
        // Always use sh -c for executing autotool scripts which should
        // work on all Linux POSIX compliant shells including bash, dash, as
        // well as Windows and Mac OSX.
        String command = null;
        for (String arg : configTargets) {
        	// TODO check for spaces in args
        	if (command == null)
        		command = arg;
        	else
        		command += " " + arg;
        }
        configTargets = new String[] { "-c", command };
        
        for (int i = 0; i < configTargets.length; ++i) {
			// try to resolve the build macros in any argument
			try{
				String resolved =
					ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
							configTargets[i],
							"", //$NON-NLS-1$
							" ", //$NON-NLS-1$
							IBuildMacroProvider.CONTEXT_CONFIGURATION,
							cfg);
				// strip any env-var settings from options
				// fix for bug #356278
				if (resolved.length() > 0 && resolved.charAt(0) != '-')
					resolved = stripEnvVarsFromOption(resolved, additionalEnvs);
				configTargets[i] = resolved;
			} catch (BuildMacroException e) {
			}
		}
		
		String[] msgs = new String[2];
		msgs[0] = commandPath.toString();
		msgs[1] = project.getName();
		monitor.subTask(AutotoolsPlugin.getFormattedString(
				"MakeGenerator.make.message", msgs)); //$NON-NLS-1$


		ConsoleOutputStream consoleOutStream = null;
		StringBuffer buf = new StringBuffer();

		// Launch command - main invocation
		if (consoleStart)
			console.start(project);
		
		try {
			consoleOutStream = console.getOutputStream();
			String[] consoleHeader = new String[3];

			consoleHeader[0] = jobDescription;
			consoleHeader[1] = toolsCfg.getId();
			consoleHeader[2] = project.getName();
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
			buf.append(jobDescription);
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$

			// Display command-line environment variables that have been stripped by us
			// because launch showCommand won't do this.
			if (additionalEnvs != null && additionalEnvs.size() > 0) {
				buf.append(AutotoolsPlugin
							.getResourceString("MakeGenerator.commandline.envvars"));
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
				buf.append("\t");
				for (int i = 0; i < additionalEnvs.size(); ++i) {
					String envvar = additionalEnvs.get(i);
					buf.append(envvar.replaceFirst("(\\w+=)(.*)"," $1\"$2\""));
				}
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
			}
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();

			// Get a launcher for the config command
			CommandLauncher launcher = new CommandLauncher();
			// Set the environment
			IEnvironmentVariable variables[] = 
					CCorePlugin.getDefault().getBuildEnvironmentManager().getVariables(cdesc, true);
			String[] env = null;
			ArrayList<String> envList = new ArrayList<String>();
			if (variables != null) {
				for (int i = 0; i < variables.length; i++) {
					// For Windows/Mac, check for PWD environment variable being passed.
					// Remove it for now as it is causing errors in configuration.
					// Fix for bug #343879
					if (!removePWD || !variables[i].getName().equals("PWD")) // $NON-NLS-1$
						envList.add(variables[i].getName()
								+ "=" + variables[i].getValue()); //$NON-NLS-1$
				}
				if (additionalEnvs != null)
					envList.addAll(additionalEnvs); // add any additional environment variables specified ahead of script
				env = (String[]) envList.toArray(new String[envList.size()]);
			}

			// Hook up an error parser manager
			URI uri = URIUtil.toURI(runPath);
			ErrorParserManager epm = new ErrorParserManager(project, uri, this, new String[] {ErrorParser.ID});
			epm.setOutputStream(consoleOutStream);
			epm.addErrorParser(ErrorParser.ID, new ErrorParser(getSourcePath(), getBuildPath()));

			OutputStream stdout = epm.getOutputStream();
			OutputStream stderr = stdout;

			launcher.showCommand(true);
			// Run the shell script via shell command.
			Process proc = launcher.execute(new Path(SHELL_COMMAND), configTargets, env,
					runPath, new NullProgressMonitor());
			if (proc != null) {
				try {
					// Close the input of the process since we will never write to
					// it
					proc.getOutputStream().close();
				} catch (IOException e) {
				}

				if (launcher.waitAndRead(stdout, stderr, new SubProgressMonitor(
						monitor, IProgressMonitor.UNKNOWN)) != CommandLauncher.OK) {
					errMsg = launcher.getErrorMessage();
				}

				// Force a resync of the projects without allowing the user to
				// cancel.
				// This is probably unkind, but short of this there is no way to
				// ensure
				// the UI is up-to-date with the build results
				// monitor.subTask(ManagedMakeMessages
				// .getResourceString(REFRESH));
				monitor.subTask(AutotoolsPlugin.getResourceString("MakeGenerator.refresh")); //$NON-NLS-1$
				try {
					project.refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					monitor.subTask(AutotoolsPlugin
							.getResourceString("MakeGenerator.refresh.error")); //$NON-NLS-1$
				}
			} else {
				errMsg = launcher.getErrorMessage();
			}

			// Report either the success or failure of our mission
			buf = new StringBuffer();
			if (errMsg != null && errMsg.length() > 0) {
				String errorDesc = AutotoolsPlugin
						.getResourceString("MakeGenerator.generation.error"); //$NON-NLS-1$
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				buf.append(errorDesc);
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				buf.append("(").append(errMsg).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
				rc = IStatus.ERROR;
			} else if (proc.exitValue() >= 1 || proc.exitValue() < 0) {
				// We have an invalid return code from configuration.
				String[] errArg = new String[2];
				errArg[0] = Integer.toString(proc.exitValue());
				errArg[1] = commandPath.toString();
				errMsg = AutotoolsPlugin.getFormattedString(
						"MakeGenerator.config.error", errArg); //$NON-NLS-1$
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				buf.append(AutotoolsPlugin.getResourceString("MakeGenerator.generation.error")); //$NON-NLS-1$
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				if (proc.exitValue() == 1)
					rc = IStatus.WARNING;
				else
					rc = IStatus.ERROR;
			} else {
				// Report a successful build
				String successMsg = 
						AutotoolsPlugin.getResourceString("MakeGenerator.success"); //$NON-NLS-1$
				buf.append(successMsg);
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				rc = IStatus.OK;
			}

			// Write message on the console
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();

			// // Generate any error markers that the build has discovered
			// monitor.subTask(ManagedMakeMessages
			// .getResourceString(MARKERS));
			// epm.reportProblems();
		} finally {
			consoleOutStream.close();
		}
		
		// If we have an error and no specific error markers, use the default error marker.
		if (rc == IStatus.ERROR && !hasMarkers(project)) {
			addMarker(project, -1, errMsg, SEVERITY_ERROR_BUILD, null);
		}
		
		return rc;
	}
	
	private ICStorageElement createTargetElement(ICStorageElement parent, IMakeTarget target) {
		ICStorageElement targetElem = parent.createChild(TARGET_ELEMENT);
		targetElem.setAttribute(TARGET_ATTR_NAME, target.getName());
		targetElem.setAttribute(TARGET_ATTR_ID, target.getTargetBuilderID());
		targetElem.setAttribute(TARGET_ATTR_PATH, target.getContainer().getProjectRelativePath().toString());
		ICStorageElement elem = targetElem.createChild(TARGET_COMMAND);
		elem.setValue(target.getBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, "make")); //$NON-NLS-1$

		String targetAttr = target.getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, null);
		if ( targetAttr != null) {
			elem = targetElem.createChild(TARGET_ARGUMENTS);
			elem.setValue(targetAttr);
		}

		targetAttr = target.getBuildAttribute(IMakeTarget.BUILD_TARGET, null);
		if (targetAttr != null) {
			elem = targetElem.createChild(TARGET);
			elem.setValue(targetAttr);
		}

		elem = targetElem.createChild(TARGET_STOP_ON_ERROR);
		elem.setValue(Boolean.valueOf(target.isStopOnError()).toString());

		elem = targetElem.createChild(TARGET_USE_DEFAULT_CMD);
		elem.setValue(Boolean.valueOf(target.isDefaultBuildCmd()).toString());

		elem = targetElem.createChild(TARGET_RUN_ALL_BUILDERS);
		elem.setValue(Boolean.valueOf(target.runAllBuilders()).toString());

		return targetElem;
	}
	
	/**
	 * This output method saves the information into the .cdtproject metadata file.
	 * 
	 * @param doc
	 * @throws CoreException
	 */
	private void saveTargets(IMakeTarget[] makeTargets) throws CoreException {
		// FIXME: fix this when MakeTargetManager fixes its code.
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(getProject(), true);
		ICStorageElement rootElement = descriptor.getProjectStorageElement(MAKE_TARGET_KEY);

		//Nuke the children since we are going to write out new ones
		rootElement.clear();

		// Fetch the ProjectTargets as ICStorageElements
		rootElement = rootElement.createChild(BUILD_TARGET_ELEMENT);
		for (int i = 0; i < makeTargets.length; ++i)
			createTargetElement(rootElement, makeTargets[i]);

		//Save the results
		descriptor.saveProjectData();
	}
	
	protected static class MakeTargetComparator implements Comparator<Object> {
		public int compare(Object a, Object b) {
			IMakeTarget make1 = (IMakeTarget)a;
			IMakeTarget make2 = (IMakeTarget)b;
			return make1.getName().compareToIgnoreCase(make2.getName());
		}
		
	}

	/**
	 * This method parses the given Makefile and produces MakeTargets for all targets so the
	 * end-user can access them from the MakeTargets popup-menu.
	 * 
	 * @param makefileFile the Makefile to parse
	 * @throws CoreException
	 */
	private void addMakeTargetsToManager(File makefileFile) throws CoreException {
		// We don't bother if the Makefile wasn't created.
		if (makefileFile == null || !makefileFile.exists())
			return;
		
		checkCancel();
		if (monitor == null)
			monitor = new NullProgressMonitor();
		String statusMsg = AutotoolsPlugin.getResourceString("MakeGenerator.refresh.MakeTargets");	//$NON-NLS-1$
		monitor.subTask(statusMsg);
		
		IMakeTargetManager makeTargetManager = 
			MakeCorePlugin.getDefault().getTargetManager();
		
		IMakefile makefile = MakeCorePlugin.createMakefile(makefileFile.toURI(), false, null);
		ITargetRule[] targets = makefile.getTargetRules();
		ITarget target = null;
		Map<String, IMakeTarget> makeTargets = new HashMap<String, IMakeTarget>(); // use a HashMap so duplicate names are handled
		String[] id = makeTargetManager.getTargetBuilders(getProject());
		if (id.length == 0) {
			return;
		}
		String targetBuildID = id[0];
		IMakeBuilderInfo buildInfo = MakeCorePlugin.createBuildInfo(getProject(),
				makeTargetManager.getBuilderID(targetBuildID));
		boolean isStopOnError = buildInfo.isStopOnError();
		IPath buildCommand = buildInfo.getBuildCommand();
		String defaultBuildCommand = buildCommand.toString();
		String buildArguments = buildInfo.getBuildArguments();
		
		// Bug #351660 - reset targets to a single dummy target so that
		// we will never be able to find any of the new targets we are about to
		// create and thus avoid an extraneous event notification on a change to
		// the MakeTarget.  The dummy target should have an invalid name for
		// a normal make target.
		IMakeTarget dummyTarget = makeTargetManager.createTarget(
				project, "\ndummyTarget\n", targetBuildID); //$NON-NLS-1$
		makeTargetManager.setTargets(project, new IMakeTarget[]{dummyTarget});
		
		for (int i = 0; i < targets.length; i++) {
			target = targets[i].getTarget();
			String targetName = target.toString();
			if (!isValidTarget(targetName, makeTargetManager))
				continue;
			try {
				// Bug #351660 - always create a new MakeTarget because an
				// existing MakeTarget will cause events to occur on every
				// modification whereas a new MakeTarget not yet added will
				// not cause this delay.
				IMakeTarget makeTarget = makeTargetManager.createTarget(
								project, targetName, targetBuildID);
				makeTarget.setContainer(project);
				makeTarget.setStopOnError(isStopOnError);
				makeTarget.setRunAllBuilders(false);
				makeTarget.setUseDefaultBuildCmd(true);
				makeTarget.setBuildAttribute(IMakeTarget.BUILD_ARGUMENTS, buildArguments);
				makeTarget.setBuildAttribute(IMakeTarget.BUILD_COMMAND, defaultBuildCommand);

				makeTarget.setBuildAttribute(GENERATED_TARGET, "true"); //$NON-NLS-1$
				makeTarget.setBuildAttribute(IMakeTarget.BUILD_TARGET,
						targetName);

				//TODO: should this be raw build directory in macro form?
				makeTarget.setBuildAttribute(IMakeTarget.BUILD_LOCATION,
						buildDir);
				makeTargets.put(makeTarget.getName(), makeTarget);
			} catch (CoreException e) {
				// Duplicate target.  Ignore.
			}
		}
		
		IMakeTarget[] makeTargetArray = new IMakeTarget[makeTargets.size()];
		Collection<IMakeTarget> values = makeTargets.values();
		ArrayList<IMakeTarget> valueList = new ArrayList<IMakeTarget>(values);
		valueList.toArray(makeTargetArray);
		MakeTargetComparator compareMakeTargets = new MakeTargetComparator();
		Arrays.sort(makeTargetArray, compareMakeTargets);

		// Check if we have MakeTargetManager patch which adds the ability
		// to save multiple targets at once.  If yes, use it as it updates
		// the MakeTargets now.  Otherwise, fall back to old method which
		// saves the targets externally..requiring closing the project and
		// reopening to see them.
		Class<? extends IMakeTargetManager> c = makeTargetManager.getClass();
		boolean targetsAdded = false;
		try {
			Method m = c.getMethod("setTargets", IContainer.class, IMakeTarget[].class);
			m.invoke(makeTargetManager, project, makeTargetArray);
			targetsAdded = true;
		} catch (NoSuchMethodException e) {
			// ignore and use fail-safe saveTargets method
		} catch (IllegalArgumentException e) {
			// ignore and use fail-safe saveTargets method
		} catch (IllegalAccessException e) {
			// ignore and use fail-safe saveTargets method
		} catch (InvocationTargetException e) {
			// ignore and use fail-safe saveTargets method
		}
		if (!targetsAdded)
			saveTargets(makeTargetArray);
	}

	private boolean isValidTarget(String targetName, IMakeTargetManager makeTargetManager) {
		return !(targetName.endsWith("-am") //$NON-NLS-1$
				|| targetName.endsWith("PROGRAMS") //$NON-NLS-1$
				|| targetName.endsWith("-generic") //$NON-NLS-1$
				|| (targetName.indexOf('$') >= 0)
				|| (targetName.charAt(0) == '.')
				|| targetName.equals(targetName.toUpperCase()));
	}
	
	// Turn the string into an array.
	private String[] makeArray(String string) {
		string = string.trim();
		char[] array = string.toCharArray();
		ArrayList<String> aList = new ArrayList<String>();
		StringBuilder buffer = new StringBuilder();
		boolean inComment = false;
		for (int i = 0; i < array.length; i++) {
			char c = array[i];
			boolean needsToAdd = true;
			if (array[i] == '"' || array[i] == '\'') {
				if (i > 0 && array[i - 1] == '\\') {
					inComment = false;
				} else {
					inComment = !inComment;
					needsToAdd = false; // skip it
				}
			}
			if (c == ' ' && !inComment) {
				if (buffer.length() > 0){
					String str = buffer.toString().trim();
					if(str.length() > 0){
						aList.add(str);
					}
				}
				buffer = new StringBuilder();
			} else {
				if (needsToAdd)
					buffer.append(c);
			}
		}
		if (buffer.length() > 0){
			String str = buffer.toString().trim();
			if(str.length() > 0){
				aList.add(str);
			}
		}
		return (String[])aList.toArray(new String[aList.size()]);
	}
}