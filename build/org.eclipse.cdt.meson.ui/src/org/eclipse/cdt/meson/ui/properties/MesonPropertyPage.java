/*******************************************************************************
 * Copyright (c) 2016,2018 IAR Systems AB
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.core.build.ICBuildCommandLauncher;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property page for Meson projects.  For unconfigured projects, we use the meson command and parse
 * the output of the --help option.  Otherwise, we use the meson configure command to find current
 * options and what may be changed via a meson configure call.
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
	private Text envText;
	private Text projText;

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(1, true));

		project = (IProject) getElement();
		String configName;
		try {
			buildConfig = ((CBuildConfiguration) project.getActiveBuildConfig().getAdapter(ICBuildConfiguration.class));
			configName = ((CBuildConfiguration) project.getActiveBuildConfig().getAdapter(ICBuildConfiguration.class))
					.getName();
			IPath sourceDir = project.getLocation();
			String buildDir = project.getLocation().append("build").append(configName).toOSString(); //$NON-NLS-1$
			IPath buildPath = new Path(buildDir).append("build.ninja"); //$NON-NLS-1$
			configured = buildPath.toFile().exists();
			if (configured) {

				ICommandLauncher launcher = CommandLauncherManager.getInstance()
						.getCommandLauncher(project.getActiveBuildConfig().getAdapter(ICBuildConfiguration.class));
				launcher.setProject(project);
				if (launcher instanceof ICBuildCommandLauncher) {
					((ICBuildCommandLauncher) launcher).setBuildConfiguration(buildConfig);
				}
				Process p = launcher.execute(new Path("meson"), new String[] { "configure", buildDir }, //$NON-NLS-1$ //$NON-NLS-2$
						new String[0], sourceDir, new NullProgressMonitor());
				if (p != null) {
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
				}
			} else {
				ICommandLauncher launcher = CommandLauncherManager.getInstance()
						.getCommandLauncher(project.getActiveBuildConfig().getAdapter(ICBuildConfiguration.class));
				launcher.setProject(project);
				if (launcher instanceof ICBuildCommandLauncher) {
					((ICBuildCommandLauncher) launcher).setBuildConfiguration(buildConfig);
				}
				Process p = launcher.execute(new Path("meson"), //$NON-NLS-1$
						new String[] { "setup", "-h" }, //$NON-NLS-1$ //$NON-NLS-2$
						new String[0], sourceDir, new NullProgressMonitor());
				if (p == null) {
					return null;
				}
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
					Map<String, String> argMap = new HashMap<>();
					String mesonArgs = buildConfig.getProperty(IMesonConstants.MESON_ARGUMENTS);
					if (mesonArgs != null) {
						String[] argStrings = mesonArgs.split("--"); //$NON-NLS-1$
						for (String argString : argStrings) {
							if (!argString.isEmpty()) {
								String[] s = argString.split("="); //$NON-NLS-1$
								if (s.length == 2) {
									argMap.put(s[0], s[1].trim());
								} else {
									argMap.put(argString.trim(), "true"); //$NON-NLS-1$
								}
							}
						}
					}

					Group group = new Group(composite, SWT.BORDER);
					GridLayout layout = new GridLayout(2, true);
					layout.marginLeft = 10;
					layout.marginRight = 10;
					group.setLayout(layout);
					group.setLayoutData(new GridData(GridData.FILL_BOTH));
					group.setText(Messages.MesonPropertyPage_env_group);

					Label envLabel = new Label(group, SWT.NONE);
					envLabel.setText(Messages.MesonPropertyPage_env_label);
					GridData data = new GridData(GridData.FILL, GridData.FILL, true, false);
					data.grabExcessHorizontalSpace = true;
					data.horizontalSpan = 1;
					envLabel.setLayoutData(data);

					String mesonEnv = buildConfig.getProperty(IMesonConstants.MESON_ENV);

					envText = new Text(group, SWT.BORDER);
					if (mesonEnv != null) {
						envText.setText(mesonEnv);
					}
					envText.setToolTipText(Messages.MesonPropertyPage_env_tooltip);
					data = new GridData(GridData.FILL, GridData.FILL, true, false);
					data.grabExcessHorizontalSpace = true;
					data.horizontalSpan = 1;
					envText.setLayoutData(data);

					group = new Group(composite, SWT.BORDER);
					layout = new GridLayout(2, true);
					layout.marginLeft = 10;
					layout.marginRight = 10;
					group.setLayout(layout);
					group.setLayoutData(new GridData(GridData.FILL_BOTH));
					group.setText(Messages.MesonPropertyPage_project_group);

					Label projLabel = new Label(group, SWT.NONE);
					projLabel.setText(Messages.MesonPropertyPage_project_label);
					data = new GridData(GridData.FILL, GridData.FILL, true, false);
					data.grabExcessHorizontalSpace = true;
					data.horizontalSpan = 1;
					projLabel.setLayoutData(data);

					String mesonProjOptions = buildConfig.getProperty(IMesonConstants.MESON_PROJECT_OPTIONS);

					projText = new Text(group, SWT.BORDER);
					if (mesonProjOptions != null) {
						projText.setText(mesonProjOptions);
					}
					projText.setToolTipText(Messages.MesonPropertyPage_project_tooltip);
					data = new GridData(GridData.FILL, GridData.FILL, true, false);
					data.grabExcessHorizontalSpace = true;
					data.horizontalSpan = 1;
					projText.setLayoutData(data);

					// default buildtype based on active build configuration
					// user can always override and we will use override from then on
					String defaultBuildType = "release"; //$NON-NLS-1$
					if (configName.contains("debug")) { //$NON-NLS-1$
						defaultBuildType = "debug"; //$NON-NLS-1$
					}
					if (argMap.get("buildtype") == null) { //$NON-NLS-1$
						argMap.put("buildtype", defaultBuildType); //$NON-NLS-1$
					}
					componentList = parseHelpOutput(stdout, composite, argMap, defaultBuildType);
				}
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
	}

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
			if (args.size() == 2) {
				return true;
			}
			try {
				String configName = ((CBuildConfiguration) project.getActiveBuildConfig()
						.getAdapter(ICBuildConfiguration.class)).getName();
				IPath sourceDir = project.getLocation();
				String buildDir = project.getLocation().append("build").append(configName).toOSString(); //$NON-NLS-1$
				ICommandLauncher launcher = CommandLauncherManager.getInstance()
						.getCommandLauncher(project.getActiveBuildConfig().getAdapter(ICBuildConfiguration.class));
				launcher.setProject(project);
				if (launcher instanceof ICBuildCommandLauncher) {
					((ICBuildCommandLauncher) launcher).setBuildConfiguration(buildConfig);
				}
				args.add(buildDir);
				Process p = launcher.execute(new Path("meson"), args.toArray(new String[0]), new String[0], sourceDir, //$NON-NLS-1$
						new NullProgressMonitor()); //$NON-NLS-2$
				int rc = -1;
				IConsole console = CCorePlugin.getDefault().getConsole();
				console.start(project);
				try (OutputStream stdout = console.getOutputStream()) {
					OutputStream stderr = stdout;
					StringBuilder buf = new StringBuilder();
					for (String arg : args) {
						buf.append(arg);
						buf.append(" "); //$NON-NLS-1$
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
			if (buildConfig != null) {
				StringBuilder mesonargs = new StringBuilder();
				for (IMesonPropertyPageControl control : componentList) {
					if (!control.getUnconfiguredString().isEmpty()) {
						mesonargs.append(control.getUnconfiguredString());
						mesonargs.append(" "); //$NON-NLS-1$
					}
				}
				buildConfig.setProperty(IMesonConstants.MESON_ARGUMENTS, mesonargs.toString());
				buildConfig.setProperty(IMesonConstants.MESON_ENV, envText.getText().trim());
				buildConfig.setProperty(IMesonConstants.MESON_PROJECT_OPTIONS, projText.getText().trim());
			}
		}
		return true;
	}

	/**
	 * Parse output of meson help call to determine options to show to user
	 * @param stdout - ByteArrayOutputStream containing output of command
	 * @param composite - Composite to add Controls to
	 * @return - list of Controls
	 */
	List<IMesonPropertyPageControl> parseHelpOutput(ByteArrayOutputStream stdout, Composite composite,
			Map<String, String> argMap, String defaultBuildType) {
		List<IMesonPropertyPageControl> controls = new ArrayList<>();

		Group group = new Group(composite, SWT.BORDER);
		GridLayout layout = new GridLayout(2, true);
		layout.marginLeft = 10;
		layout.marginRight = 10;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		group.setText(Messages.MesonPropertyPage_options_group);

		try {
			String output = stdout.toString(StandardCharsets.UTF_8.name()).replaceAll("\\r?\\n\\s+", " "); //$NON-NLS-1$ //$NON-NLS-2$
			String[] lines = output.split("--"); //$NON-NLS-1$
			Pattern optionPattern = Pattern.compile("(([a-z-]+)\\s+(([A-Z_][A-Z_]+))?\\s*(\\{.*?\\})?([^\\[\\]]*))");
			Pattern descPattern1 = Pattern.compile("([^\\.]+).*");
			Pattern descPattern = Pattern.compile("([^\\(]*)(\\(default\\:\\s+([^\\)]+)\\).*)");
			for (String line : lines) {
				Matcher optionMatcher = optionPattern.matcher(line);
				if (optionMatcher.matches() && !optionMatcher.group(2).equals("help")) {
					if (optionMatcher.group(3) != null) {
						String defaultValue = argMap.get(optionMatcher.group(2));
						String description = optionMatcher.group(6);
						Matcher m = descPattern1.matcher(optionMatcher.group(6));
						if (m.matches()) {
							description = m.group(1).trim();
						}
						IMesonPropertyPageControl control = new MesonPropertyText(group, optionMatcher.group(2),
								defaultValue, description);
						controls.add(control);
					} else if (optionMatcher.group(5) != null) {
						String defaultValue = argMap.get(optionMatcher.group(2));
						Matcher m = descPattern.matcher(optionMatcher.group(6));
						if (m.matches()) {
							String valueString = optionMatcher.group(5).replaceAll("\\{", ""); //$NON-NLS-1$ //$NON-NLS-2$
							valueString = valueString.replaceAll("\\}", ""); //$NON-NLS-1$ //$NON-NLS-2$
							String[] values = valueString.split(","); //$NON-NLS-1$
							if (defaultValue == null) {
								defaultValue = m.group(3).trim();
							}
							IMesonPropertyPageControl control = new MesonPropertyCombo(group, optionMatcher.group(2),
									values, defaultValue, m.group(1).trim());
							controls.add(control);
						}
					} else {
						boolean defaultValue = false;
						if (argMap.containsKey(optionMatcher.group(2))) {
							defaultValue = Boolean.parseBoolean(argMap.get(optionMatcher.group(2)));
						}
						IMesonPropertyPageControl control = new MesonPropertySpecialCheckbox(group,
								optionMatcher.group(2), defaultValue, optionMatcher.group(6));
						controls.add(control);
					}
				}

			}
		} catch (UnsupportedEncodingException e) {
			return controls;
		}
		return controls;
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
			Pattern optionLine = Pattern.compile("(\\w+)\\s+([\\w,\\-,/]+)\\s+(.*)$"); //$NON-NLS-1$
			Pattern optionWithValuesLine = Pattern
					.compile("(\\w+)\\s+([\\w,\\-,/]+)\\s+\\[([\\w,\\-,/]+)((,\\s+[\\w,\\-]+)*)\\]\\s+(.*)$");
			Pattern compilerOrLinkerArgs = Pattern.compile(Messages.MesonPropertyPage_compiler_or_link_args);
			Pattern argLine = Pattern.compile("(\\w+)\\s+\\[([^\\]]*)\\]"); //$NON-NLS-1$
			Pattern groupPattern = Pattern.compile("(([^:]*)):"); //$NON-NLS-1$
			String groupName = ""; //$NON-NLS-1$
			Composite parent = composite;
			for (String line : lines) {
				line = line.trim();
				boolean unprocessed = true;
				while (unprocessed) {
					unprocessed = false;
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
							if (parent == composite) {
								Group group = new Group(composite, SWT.BORDER);
								group.setLayout(new GridLayout(2, true));
								group.setLayoutData(new GridData(GridData.FILL_BOTH));
								group.setText(groupName);
								parent = group;
							}
							break;
						}
						m = optionWithValuesPattern.matcher(line);
						if (m.matches()) {
							state = ParseState.OPTION_WITH_VALUES;
							if (parent == composite) {
								Group group = new Group(composite, SWT.BORDER);
								group.setLayout(new GridLayout(2, true));
								group.setLayoutData(new GridData(GridData.FILL_BOTH));
								group.setText(groupName);
								parent = group;
							}
							break;
						}

						if (line.contains(":")) { //$NON-NLS-1$
							state = ParseState.INIT;
							unprocessed = true;
							parent = composite;
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
							IMesonPropertyPageControl argControl = new MesonPropertyText(parent, argName, argValue,
									argDescription);
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
								IMesonPropertyPageControl control = new MesonPropertyInteger(parent, this, name, value,
										description);
								controls.add(control);
							} else if (Messages.MesonPropertyPage_true.equals(value)
									|| Messages.MesonPropertyPage_false.equals(value)) {
								IMesonPropertyPageControl control = new MesonPropertyCheckbox(parent, name,
										Boolean.getBoolean(value), description);
								controls.add(control);
							} else {
								IMesonPropertyPageControl control = new MesonPropertyText(parent, name, value,
										description);
								controls.add(control);
							}
						} else {
							if (line.contains(":")) { //$NON-NLS-1$
								state = ParseState.INIT;
								parent = composite;
								unprocessed = true;
							} else {
								state = ParseState.GROUP;
								unprocessed = true;
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
							String[] values = new String[] { possibleValue };
							if (!extraValues.isEmpty()) {
								values = extraValues.split(",\\s+");
								values[0] = possibleValue;
							}
							IMesonPropertyPageControl control = new MesonPropertyCombo(parent, name, values, value,
									description);
							controls.add(control);
						} else {
							if (line.contains(":")) { //$NON-NLS-1$
								state = ParseState.INIT;
								parent = composite;
								unprocessed = true;
							} else {
								state = ParseState.GROUP;
								unprocessed = true;
							}
						}
						break;
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			return controls;
		}
		return controls;
	}

}