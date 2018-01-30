/*******************************************************************************
 * Copyright (c) 2016,2018 IAR Systems AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IAR Systems - initial API and implementation
 *     Red Hat Inc. - modified for use in Meson build
 *******************************************************************************/
package org.eclipse.cdt.meson.ui.properties;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncherManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.meson.core.IMesonConstants;
import org.eclipse.cdt.meson.ui.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property page for CMake projects. The only thing we have here at the moment is a button
 * to launch the CMake GUI configurator (cmake-qt-gui).
 * 
 * We assume that the build directory is in project/build/configname, which is where
 * the CMake project wizard puts it. We also assume that "cmake-gui" is in the user's 
 * PATH.
 */
public class MesonPropertyPage extends PropertyPage {
	
	private IProject project;
	private List<IMesonPropertyPageControl> componentList = new ArrayList<>();
	private boolean configured;
	private CBuildConfiguration buildConfig;
	
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(1, true));
		
		project = (IProject) getElement();
		String configName;
		try {
			buildConfig = ((CBuildConfiguration)project.getActiveBuildConfig().getAdapter(ICBuildConfiguration.class));
			configName = ((CBuildConfiguration)project.getActiveBuildConfig().getAdapter(ICBuildConfiguration.class)).getName();
			IPath sourceDir = project.getLocation();
			String buildDir = project.getLocation().append("build").append(configName).toOSString(); //$NON-NLS-1$
			IPath buildPath = new Path(buildDir).append("build.ninja"); //$NON-NLS-1$
			configured = buildPath.toFile().exists();
			if (configured) {

				ICommandLauncher launcher = CommandLauncherManager.getInstance().getCommandLauncher(project.getActiveBuildConfig().getAdapter(ICBuildConfiguration.class));
				Process p = launcher.execute(new Path("meson"), new String[] { "configure",  buildDir}, new String[0], sourceDir, new NullProgressMonitor());
				ByteArrayOutputStream stdout = new ByteArrayOutputStream();
				ByteArrayOutputStream stderr = new ByteArrayOutputStream();
				int rc = -1;
				try {
					if (launcher.waitAndRead(stdout, stderr, new NullProgressMonitor()) == ICommandLauncher.OK) {
						p.waitFor();
					}
					rc = p.exitValue();
				} catch (InterruptedException e) {
					// ignore for now
				}
				if (rc == 0) {
					componentList = parseConfigureOutput(stdout, composite);
				}

			} else {
				Map<String, String> argMap = new HashMap<>();
				String mesonArgs = buildConfig.getProperty(IMesonConstants.MESON_ARGUMENTS);
				if (mesonArgs != null) {
					String[] argStrings = mesonArgs.split("\\s+");
					for (String argString : argStrings) {
						String[] s = argString.split("=");
						if (s.length == 2) {
							argMap.put(s[0], s[1]);
						} else {
							argMap.put(argString, "true");
						}
					}
				}
				
				String defaultBuildType = "release";
				if (configName.contains("debug")) { //$NON-NLS-1$
					defaultBuildType = "debug"; //$NON-NLS-1$
				}
				componentList = defaultOptions(composite, argMap, defaultBuildType);
			}
		} catch (CoreException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		return composite;
	}
	
	public void update() {
		setErrorMessage(null);
		for (IMesonPropertyPageControl control : componentList) {
			if (!control.isValid()) {
				setValid(false);
				setErrorMessage(control.getErrorMessage());
			}
		}
	}
	
	public enum ParseState { 
		INIT, GROUP, OPTION, OPTION_WITH_VALUES, ARGS 
	};
	
	@Override
	public boolean performOk() {
		List<String> args = new ArrayList<>();
		if (configured) {
			args.add("configure"); //$NON-NLS-1$
			for (IMesonPropertyPageControl control : componentList) {
				if (control.isValueChanged()) {
					args.add(control.getConfiguredString()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (args.size() == 1) {
				return true;
			}
			try {
				String configName = ((CBuildConfiguration)project.getActiveBuildConfig().getAdapter(ICBuildConfiguration.class)).getName();
				IPath sourceDir = project.getLocation();
				String buildDir = project.getLocation().append("build").append(configName).toOSString(); //$NON-NLS-1$
				ICommandLauncher launcher = CommandLauncherManager.getInstance().getCommandLauncher(project.getActiveBuildConfig().getAdapter(ICBuildConfiguration.class));
				args.add(buildDir);
				Process p = launcher.execute(new Path("meson"), args.toArray(new String[0]), new String[0], sourceDir, new NullProgressMonitor());
				int rc = -1;
				IConsole console = CCorePlugin.getDefault().getConsole();
				console.start(project);
				try (OutputStream stdout = console.getOutputStream()) {
					OutputStream stderr = stdout;
					StringBuilder buf = new StringBuilder();
					buf.append("meson");
					for (String arg : args) {
						buf.append(" "); //$NON-NLS-1$
						buf.append(arg);
					}
					buf.append(System.lineSeparator());
					stdout.write(buf.toString().getBytes());
					stdout.flush();
					try {

						if (launcher.waitAndRead(stdout, stderr, new NullProgressMonitor()) == ICommandLauncher.OK) {
							p.waitFor();
						}
						rc = p.exitValue();
						stdout.write(NLS.bind(Messages.MesonPropertyPage_terminated_rc, rc).getBytes());
						stdout.flush();
						if (rc != 0) {
							Display.getDefault().syncExec(() -> {
								MessageDialog.openError(getShell(), null, Messages.MesonPropertyPage_configure_failed);
							});
						}
					} catch (InterruptedException e) {
						// ignore for now
					}
				} catch (IOException e2) {
					Activator.log(e2);
					return false;
				}
			} catch (CoreException e3) {
				// TODO Auto-generated catch block
				Activator.log(e3);
				return false;
			}
		} else {
			StringBuilder mesonargs = new StringBuilder();
			for (IMesonPropertyPageControl control : componentList) {
				System.out.println(control.getUnconfiguredString());
				mesonargs.append(control.getUnconfiguredString());
				mesonargs.append(" "); //$NON-NLS-1$
			}
			buildConfig.setProperty(IMesonConstants.MESON_ARGUMENTS, mesonargs.toString());
		}
		return true;
	}
	
	/**
	 * Parse output of meson configure call to determine options to show to user
	 * @param stdout - ByteArrayOutputStream containing output of command
	 * @param composite - Composite to add Controls to
	 * @return - list of Controls
	 */
	List<IMesonPropertyPageControl> parseConfigureOutput(ByteArrayOutputStream stdout, Composite composite) {
		List<IMesonPropertyPageControl> controls = new ArrayList<>();
		
		try {
			String[] lines = stdout.toString(StandardCharsets.UTF_8.name()).split("\\r?\\n"); //$NON-NLS-1$
			ParseState state = ParseState.INIT;
			Pattern optionPattern = Pattern.compile(Messages.MesonPropertyPage_option_pattern);
			Pattern optionWithValuesPattern = Pattern.compile(Messages.MesonPropertyPage_option_with_values_pattern);
			Pattern optionLine = Pattern.compile("(\\w+)\\s+(\\w+)\\s+(.*)$"); //$NON-NLS-1$
			Pattern optionWithValuesLine = Pattern.compile("(\\w+)\\s+(\\w+)\\s+\\[(\\w+)((,\\s+\\w+)*)\\]\\s+(.*)$");
			Pattern compilerOrLinkerArgs = Pattern.compile(Messages.MesonPropertyPage_compiler_or_link_args);
			Pattern argLine = Pattern.compile("(\\w+)\\s+\\[([^\\]]*)\\]"); //$NON-NLS-1$
			Pattern groupPattern = Pattern.compile("(([^:]*)):"); //$NON-NLS-1$
			String groupName = ""; //$NON-NLS-1$
			Composite parent = composite;
			for (String line : lines) {
				line = line.trim();
				switch (state) {
				case INIT:
					Matcher argMatcher = compilerOrLinkerArgs.matcher(line);
					if (argMatcher.matches()) {
						state = ParseState.ARGS;
						Group group = new Group(composite, SWT.BORDER);
						group.setLayout(new GridLayout(2, true));
						group.setLayoutData(new GridData(GridData.FILL_BOTH));
						group.setText(argMatcher.group(1));
						parent = group;
					} else {
						Matcher groupMatcher = groupPattern.matcher(line);
						if (groupMatcher.matches()) {
							groupName = groupMatcher.group(1);
							state = ParseState.GROUP;
						}
						parent = composite;
					}
					break;
				case GROUP:
					Matcher m = optionPattern.matcher(line);
					if (m.matches()) {
						state = ParseState.OPTION;
						Group group = new Group(composite, SWT.BORDER);
						group.setLayout(new GridLayout(2, true));
						group.setLayoutData(new GridData(GridData.FILL_BOTH));
						group.setText(groupName);
						parent = group;
						break;
					}
					m = optionWithValuesPattern.matcher(line);
					if (m.matches()) {
						state = ParseState.OPTION_WITH_VALUES;
						Group group = new Group(composite, SWT.BORDER);
						group.setLayout(new GridLayout(2, true));
						group.setLayoutData(new GridData(GridData.FILL_BOTH));
						group.setText(groupName);
						parent = group;
					}
				
					break;
				case ARGS:
					Matcher m2 = argLine.matcher(line);
					if (m2.matches()) {
						String argName = m2.group(1);
						String argValue = m2.group(2);
						argValue = argValue.replaceAll("',", ""); //$NON-NLS-1$ //$NON-NLS-2$
						argValue = argValue.replaceAll("'", ""); //$NON-NLS-1$ //$NON-NLS-2$
						String argDescription = Messages.MesonPropertyPage_arg_description;
						IMesonPropertyPageControl argControl = new MesonPropertyText(parent, argName, argValue, argDescription);
						controls.add(argControl);
					}
					state = ParseState.INIT;
					parent = composite;
					break;
				case OPTION:
					Matcher m3 = optionLine.matcher(line);
					if (line.startsWith("----")) { //$NON-NLS-1$
						break;
					}
					if (line.isEmpty()) {
						state = ParseState.INIT;
						parent = composite;
						break;
					}
					if (m3.matches()) {
						String name = m3.group(1);
						String value = m3.group(2);
						String description = m3.group(3);
						boolean isInteger = false;
						try {
							Integer.parseInt(value);
							isInteger = true;
						} catch (NumberFormatException e) {
							// do nothing
						}
						if (isInteger) {
							IMesonPropertyPageControl control = new MesonPropertyInteger(parent, this, name, value, description);
							controls.add(control);
						} else if (Messages.MesonPropertyPage_true.equals(value) ||
								Messages.MesonPropertyPage_false.equals(value)) {
							IMesonPropertyPageControl control = new MesonPropertyCheckbox(parent, name, Boolean.getBoolean(value), description);
							controls.add(control);
						} else {
							IMesonPropertyPageControl control = new MesonPropertyText(parent, name, value, description);
							controls.add(control);
						}
					}
					break;
				case OPTION_WITH_VALUES:
					Matcher m4 = optionWithValuesLine.matcher(line);
					if (line.startsWith("----")) { //$NON-NLS-1$
						break;
					}
					if (line.isEmpty()) {
						state = ParseState.INIT;
						parent = composite;
						break;
					}
					if (m4.matches()) {
						String name = m4.group(1);
						String value = m4.group(2);
						String possibleValue = m4.group(3);
						String extraValues = m4.group(4);
						String description = m4.group(6);
						String[] values = new String[] {possibleValue};
						if (!extraValues.isEmpty()) {
							values = extraValues.split(",\\s+");
							values[0] = possibleValue;
						}
						IMesonPropertyPageControl control = new MesonPropertyCombo(parent, name, values, value, description);
						controls.add(control);
					}
					break;
				}
			}
		} catch (UnsupportedEncodingException e) {
			return controls;
		}
		return controls;
	}
	
	/**
	 * Create list of options for initial meson call
	 * @param stdout - ByteArrayOutputStream containing output of command
	 * @param composite - Composite to add Controls to
	 * @return - list of Controls
	 */
	List<IMesonPropertyPageControl> defaultOptions(Composite composite, Map<String, String> argMap, String defaultBuildType) {
		List<IMesonPropertyPageControl> controls = new ArrayList<>();
		
		Group group = new Group(composite, SWT.BORDER);
		group.setLayout(new GridLayout(2, true));
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		group.setText("Options");

		IMesonPropertyPageControl prefix = new MesonPropertyText(group, "prefix", argMap.get("--prefix"), //$NON-NLS-1$ //$NON-NLS-2$ 
				Messages.MesonPropertyPage_prefix_tooltip);
		controls.add(prefix);
		IMesonPropertyPageControl libdir = new MesonPropertyText(group, "libdir", argMap.get("--libdir"), //$NON-NLS-1$ //$NON-NLS-2$ 
				Messages.MesonPropertyPage_libdir_tooltip);
		controls.add(libdir);
		IMesonPropertyPageControl libexecdir = new MesonPropertyText(group, "libexecdir", argMap.get("--libexecdir"), //$NON-NLS-1$ //$NON-NLS-2$
				Messages.MesonPropertyPage_libexecdir_tooltip);
		controls.add(libexecdir);
		IMesonPropertyPageControl bindir = new MesonPropertyText(group, "bindir", argMap.get("--bindir"), //$NON-NLS-1$ //$NON-NLS-2$
				Messages.MesonPropertyPage_bindir_tooltip);
		controls.add(bindir);
		IMesonPropertyPageControl sbindir = new MesonPropertyText(group, "sbindir", argMap.get("--sbindir"), //$NON-NLS-1$ //$NON-NLS-2$ 
				Messages.MesonPropertyPage_sbindir_tooltip);
		controls.add(sbindir);
		IMesonPropertyPageControl includedir = new MesonPropertyText(group, "includedir", argMap.get("--includedir"), //$NON-NLS-1$ //$NON-NLS-2$ 
				Messages.MesonPropertyPage_includedir_tooltip);
		controls.add(includedir);
		IMesonPropertyPageControl datadir = new MesonPropertyText(group, "datadir", argMap.get("--datadir"), //$NON-NLS-1$ //$NON-NLS-2$ 
				Messages.MesonPropertyPage_datadir_tooltip);
		controls.add(datadir);
		IMesonPropertyPageControl mandir = new MesonPropertyText(group, "mandir", argMap.get("--mandir"), //$NON-NLS-1$ //$NON-NLS-2$ 
				Messages.MesonPropertyPage_mandir_tooltip);
		controls.add(mandir);
		IMesonPropertyPageControl infodir = new MesonPropertyText(group, "infodir", argMap.get("--infodir"), //$NON-NLS-1$ //$NON-NLS-2$
				Messages.MesonPropertyPage_infodir_tooltip);
		controls.add(infodir);
		IMesonPropertyPageControl localedir = new MesonPropertyText(group, "localedir", argMap.get("--localedir"), //$NON-NLS-1$ //$NON-NLS-2$ 
				Messages.MesonPropertyPage_localedir_tooltip);
		controls.add(localedir);
		IMesonPropertyPageControl sysconfdir = new MesonPropertyText(group, "sysconfdir", argMap.get("--sysconfdir"), //$NON-NLS-1$ //$NON-NLS-2$ 
				Messages.MesonPropertyPage_sysconfdir_tooltip);
		controls.add(sysconfdir);
		IMesonPropertyPageControl localstatedir = new MesonPropertyText(group, "localstatedir", argMap.get("--localstatedir"), //$NON-NLS-1$ //$NON-NLS-2$ 
				Messages.MesonPropertyPage_localstatedir_tooltip);
		controls.add(localstatedir);
		IMesonPropertyPageControl sharedstatedir = new MesonPropertyText(group, "sharedstatedir", argMap.get("--sharedstatedir"), //$NON-NLS-1$ //$NON-NLS-2$
				Messages.MesonPropertyPage_sharedstatedir_tooltip);
		controls.add(sharedstatedir);
		IMesonPropertyPageControl buildtype = new MesonPropertyCombo(group, "buildtype", //$NON-NLS-1$ 
				new String[] {"plain", "debug", "debugoptimized", "release", "minsize"}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				argMap.get("--buildtype") != null ? argMap.get("--buildtype") : defaultBuildType, //$NON-NLS-1$ //$NON-NLS-2$ 
						Messages.MesonPropertyPage_buildtype_tooltip);
		controls.add(buildtype);
		IMesonPropertyPageControl strip = new MesonPropertySpecialCheckbox(group, "strip", argMap.get("--strip") != null, //$NON-NLS-1$ //$NON-NLS-2$
				Messages.MesonPropertyPage_strip_tooltip);
		controls.add(strip);
		IMesonPropertyPageControl unity = new MesonPropertyCombo(group, "unity", //$NON-NLS-1$ 
				new String[] {"on", "off", "subprojects"}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				argMap.get("--unity") != null ? argMap.get("--unity") : "off", //$NON-NLS-1$ //$NON-NLS-2$
						Messages.MesonPropertyPage_unity_tooltip);
		controls.add(unity);
		IMesonPropertyPageControl werror = new MesonPropertySpecialCheckbox(group, "werror", //$NON-NLS-1$
				argMap.get("--werror") != null, Messages.MesonPropertyPage_werror_tooltip); //$NON-NLS-1$
		controls.add(werror);
		IMesonPropertyPageControl layout = new MesonPropertyCombo(group, "layout", //$NON-NLS-1$ 
				new String[] {"mirror", "flat"}, //$NON-NLS-1$ //$NON-NLS-2$
				argMap.get("--mirror") != null ? argMap.get("--mirror") : "mirror", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
						Messages.MesonPropertyPage_layout_tooltip);
		controls.add(layout);
		IMesonPropertyPageControl default_library = new MesonPropertyCombo(group, "default-library", //$NON-NLS-1$
				new String[] {"shared", "static"}, //$NON-NLS-1$ //$NON-NLS-2$
				argMap.get("--default-library") != null ? argMap.get("--default-library") : "shared", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						Messages.MesonPropertyPage_default_library_tooltip);
		controls.add(default_library);
		IMesonPropertyPageControl warnlevel = new MesonPropertyCombo(group, "warnlevel", //$NON-NLS-1$
				new String[] {"1","2","3"}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				argMap.get("--warnlevel") != null ? argMap.get("--warnlevel") : "1", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
						Messages.MesonPropertyPage_warnlevel_tooltip);
		controls.add(warnlevel);
		IMesonPropertyPageControl stdsplit = new MesonPropertySpecialCheckbox(group, "stdsplit", //$NON-NLS-1$
				argMap.get("--stdsplit") != null, Messages.MesonPropertyPage_stdsplit_tooltip); //$NON-NLS-1$
		controls.add(stdsplit);
		IMesonPropertyPageControl errorlogs = new MesonPropertySpecialCheckbox(group, "errorlogs", //$NON-NLS-1$
				argMap.get("--errorlogs") != null, Messages.MesonPropertyPage_errorlogs_tooltip); //$NON-NLS-1$
		controls.add(errorlogs);
		IMesonPropertyPageControl cross_file = new MesonPropertyText(group, "cross-file", //$NON-NLS-1$ 
				argMap.get("--cross-file"), Messages.MesonPropertyPage_cross_file_tooltip); //$NON-NLS-1$
		controls.add(cross_file);
		IMesonPropertyPageControl wrap_mode = new MesonPropertyCombo(group, "wrap-mode", //$NON-NLS-1$ 
				new String[] {"default", "nofallback", "nodownload"}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				argMap.get("--wrap-mode") != null ? argMap.get("--wrap-mode") : "default", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
				Messages.MesonPropertyPage_wrap_mode_tooltip);
		controls.add(wrap_mode);

		return controls;
	}
}