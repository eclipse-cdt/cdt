/*******************************************************************************
 * Copyright (c) 2016 IAR Systems AB
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
 *******************************************************************************/
package org.eclipse.cdt.cmake.ui.properties;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.cmake.ui.internal.Activator;
import org.eclipse.cdt.cmake.ui.internal.CMakePropertyCombo;
import org.eclipse.cdt.cmake.ui.internal.CMakePropertyText;
import org.eclipse.cdt.cmake.ui.internal.ICMakePropertyPageControl;
import org.eclipse.cdt.cmake.ui.internal.Messages;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncherManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildCommandLauncher;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property page for CMake projects. The only thing we have here at the moment is a button
 * to launch the CMake GUI configurator (cmake-qt-gui).
 *
 * We assume that the build directory is in project/build/configname, which is where
 * the CMake project wizard puts it. We also assume that "cmake-gui" is in the user's
 * PATH.
 */
public class CMakePropertyPage extends PropertyPage {

	private List<ICMakePropertyPageControl> componentList = new ArrayList<>();

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());

		boolean isContainerBuild = false;
		ICBuildConfiguration cconfig = null;
		IProject project = (IProject) getElement();
		try {
			IBuildConfiguration config = project.getActiveBuildConfig();
			cconfig = config.getAdapter(ICBuildConfiguration.class);
			IToolChain toolChain = cconfig.getToolChain();
			String os = toolChain.getProperty(IToolChain.ATTR_OS);
			isContainerBuild = os.equals("linux-container"); //$NON-NLS-1$
		} catch (CoreException e2) {
			MessageDialog.openError(parent.getShell(), Messages.CMakePropertyPage_FailedToGetOS_Title,
					Messages.CMakePropertyPage_FailedToGetOS_Body + e2.getMessage());
		}

		if (isContainerBuild) {
			try {
				ICommandLauncher launcher = CommandLauncherManager.getInstance()
						.getCommandLauncher(project.getActiveBuildConfig().getAdapter(ICBuildConfiguration.class));
				launcher.setProject(project);
				if (launcher instanceof ICBuildCommandLauncher) {
					((ICBuildCommandLauncher) launcher).setBuildConfiguration(cconfig);
				}
				IPath buildPath = project.getLocation().append("build")
						.append(((CBuildConfiguration) cconfig).getName());
				Process p = launcher.execute(new Path("cmake"), new String[] { "-LAH", "." }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						new String[0], buildPath, new NullProgressMonitor());
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
			} catch (CoreException e) {
				MessageDialog.openError(parent.getShell(),
						Messages.CMakePropertyPage_FailedToGetCMakeConfiguration_Title,
						Messages.CMakePropertyPage_FailedToGetCMakeConfiguration_Body + e.getMessage());
			}

		} else {

			Button b = new Button(composite, SWT.NONE);
			b.setText(Messages.CMakePropertyPage_LaunchCMakeGui);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IProject project = (IProject) getElement();
					try {
						String configName = project.getActiveBuildConfig().getName();
						String sourceDir = project.getLocation().toOSString();
						String buildDir = project.getLocation().append("build").append(configName).toOSString(); //$NON-NLS-1$

						Runtime.getRuntime().exec(new String[] { "cmake-gui", "-H" + sourceDir, "-B" + buildDir }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					} catch (CoreException | IOException e1) {
						MessageDialog.openError(parent.getShell(),
								Messages.CMakePropertyPage_FailedToStartCMakeGui_Title,
								Messages.CMakePropertyPage_FailedToStartCMakeGui_Body + e1.getMessage());
					}
				}
			});
		}

		return composite;
	}

	@Override
	public boolean performOk() {
		List<String> args = new ArrayList<>();
		args.add("-LAH"); //$NON-NLS-1$
		for (ICMakePropertyPageControl control : componentList) {
			if (control.isValueChanged()) {
				args.add(control.getConfiguredString()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		if (args.size() == 2) {
			return true;
		}
		try {
			IProject project = (IProject) getElement();
			ICBuildConfiguration buildConfig = project.getActiveBuildConfig().getAdapter(ICBuildConfiguration.class);
			String configName = ((CBuildConfiguration) buildConfig).getName();
			IPath buildDir = project.getLocation().append("build").append(configName); //$NON-NLS-1$
			ICommandLauncher launcher = CommandLauncherManager.getInstance()
					.getCommandLauncher(project.getActiveBuildConfig().getAdapter(ICBuildConfiguration.class));
			launcher.setProject(project);
			if (launcher instanceof ICBuildCommandLauncher) {
				((ICBuildCommandLauncher) launcher).setBuildConfiguration(buildConfig);
			}
			args.add(".");
			Process p = launcher.execute(new Path("cmake"), args.toArray(new String[0]), new String[0], buildDir, //$NON-NLS-1$
					new NullProgressMonitor());
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
					stdout.write(NLS.bind(Messages.CMakePropertyPage_Terminated, rc).getBytes());
					stdout.flush();
					if (rc != 0) {
						Display.getDefault().syncExec(() -> {
							MessageDialog.openError(getShell(), null, Messages.CMakePropertyPage_FailedToConfigure);
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
		return true;
	}

	public enum ParseState {
		INIT, SEENCOMMENT
	}

	/**
	 * Parse output of cmake -LAH call to determine options to show to user
	 * @param stdout - ByteArrayOutputStream containing output of command
	 * @param composite - Composite to add Controls to
	 * @return - list of Controls
	 */
	List<ICMakePropertyPageControl> parseConfigureOutput(ByteArrayOutputStream stdout, Composite composite) {
		List<ICMakePropertyPageControl> controls = new ArrayList<>();

		try {
			ParseState state = ParseState.INIT;

			String output = stdout.toString(StandardCharsets.UTF_8.name());
			String[] lines = output.split("\\r?\\n"); //$NON-NLS-1$
			Pattern commentPattern = Pattern.compile("//(.*)"); //$NON-NLS-1$
			Pattern argPattern = Pattern.compile("(\\w+):([a-zA-Z]+)=(.*)"); //$NON-NLS-1$
			Pattern optionPattern = Pattern.compile(".*?options are:((\\s+\\w+(\\(.*\\))?)+).*"); //$NON-NLS-1$

			String lastComment = ""; //$NON-NLS-1$
			for (String line : lines) {
				line = line.trim();
				switch (state) {
				case INIT:
					Matcher commentMatcher = commentPattern.matcher(line);
					if (commentMatcher.matches()) {
						state = ParseState.SEENCOMMENT;

						lastComment = commentMatcher.group(1);
					}
					break;
				case SEENCOMMENT:
					Matcher argMatcher = argPattern.matcher(line);
					if (argMatcher.matches()) {
						String name = argMatcher.group(1);
						String type = argMatcher.group(2);
						String initialValue = argMatcher.group(3);
						Matcher optionMatcher = optionPattern.matcher(lastComment);
						if (optionMatcher.matches()) {
							String optionString = optionMatcher.group(1).trim();
							String[] options = optionString.split("\\s+"); //$NON-NLS-1$
							for (int i = 0; i < options.length; ++i) {
								options[i] = options[i].replaceAll("\\(.*?\\)", "").trim(); //$NON-NLS-1$
							}
							ICMakePropertyPageControl control = new CMakePropertyCombo(composite, name, options,
									initialValue, lastComment);
							controls.add(control);
						} else {
							if ("BOOL".equals(type)) {
								if ("ON".equals(initialValue) || ("OFF".equals(initialValue))) {
									ICMakePropertyPageControl control = new CMakePropertyCombo(composite, name,
											new String[] { "ON", "OFF" }, //$NON-NLS-1$ //$NON-NLS-2$
											initialValue, lastComment);
									controls.add(control);
								} else if ("YES".equals(initialValue) || "NO".equals(initialValue)) {
									ICMakePropertyPageControl control = new CMakePropertyCombo(composite, name,
											new String[] { "YES", "NO" }, //$NON-NLS-1$ //$NON-NLS-2$
											initialValue, lastComment);
									controls.add(control);
								} else {
									ICMakePropertyPageControl control = new CMakePropertyCombo(composite, name,
											new String[] { "TRUE", "FALSE" }, //$NON-NLS-1$ //$NON-NLS-2$
											"TRUE".equals(initialValue) ? "TRUE" : "FALSE", lastComment); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
									controls.add(control);
								}
							} else {
								ICMakePropertyPageControl control = new CMakePropertyText(composite, name, initialValue,
										lastComment);
								controls.add(control);
							}
						}
					}
					state = ParseState.INIT;
					break;
				}
			}

		} catch (UnsupportedEncodingException e) {
			return controls;
		}

		return controls;
	}

}
