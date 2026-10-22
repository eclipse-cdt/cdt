/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.ui.internal.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.cdt.cmake.core.CMakeNature;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.launch.ui.corebuild.CoreBuildTab;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.launchbar.ui.ILaunchBarLaunchConfigDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * Tests the Build tab of a CMake launch configuration when there are no toolchains for the launch target.
 * <p>
 * The tab is exercised through {@link CoreBuildTab}, as the launch bar does, so that this covers the whole chain
 * of CoreBuildTab, CMakeBuildTab and CommonBuildTab.
 *
 * To run these tests manually from PDE, use the SWTBot Test launch delegate with Main > Run as application
 * set to org.eclipse.ui.ide.workbench.
 */
public class NoToolchainsBuildTabTest {

	private static final String PROJECT_NAME = "NoToolchainsBuildTabTest";
	private static final String TARGET_TYPE = "org.eclipse.cdt.cmake.ui.tests.targetType";
	private static final String TARGET_ID = "NoToolchainTarget";
	private static final String LAUNCH_CONFIG_TYPE = "org.eclipse.cdt.launch.applicationLaunchType";

	private IProject project;
	private ILaunchTargetManager targetManager;
	private ILaunchTarget target;
	private ILaunchConfiguration launchConfig;

	private Shell shell;
	private CoreBuildTab tab;

	@BeforeEach
	public void setUp() throws Exception {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
		project.create(null);
		project.open(null);
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] { CMakeNature.ID });
		project.setDescription(description, null);

		// A target that no toolchain can match
		// This test bundle is never started, so it has no BundleContext; use the bundle that provides the service
		Bundle launchBarCore = FrameworkUtil.getBundle(ILaunchTargetManager.class);
		launchBarCore.start();
		BundleContext context = launchBarCore.getBundleContext();
		targetManager = context.getService(context.getServiceReference(ILaunchTargetManager.class));
		ILaunchTargetWorkingCopy targetCopy = targetManager.addLaunchTarget(TARGET_TYPE, TARGET_ID).getWorkingCopy();
		targetCopy.setAttribute(ILaunchTarget.ATTR_OS, "no-such-os");
		targetCopy.setAttribute(ILaunchTarget.ATTR_ARCH, "no-such-arch");
		target = targetCopy.save();

		ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType(LAUNCH_CONFIG_TYPE);
		ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, PROJECT_NAME);
		workingCopy.setMappedResources(new IResource[] { project });
		launchConfig = workingCopy.doSave();
	}

	@AfterEach
	public void tearDown() throws Throwable {
		runOnUiThread(() -> {
			if (shell != null) {
				shell.dispose();
			}
		});
		if (launchConfig != null) {
			launchConfig.delete();
		}
		if (target != null) {
			targetManager.removeLaunchTarget(target);
		}
		if (project != null) {
			project.delete(true, true, null);
		}
	}

	@Test
	public void errorIsReported() throws Throwable {
		runOnUiThread(() -> {
			createAndInitializeTab();

			assertFalse(tab.isValid(launchConfig), "launch configuration should be invalid without toolchains");
			String message = tab.getErrorMessage();
			assertNotNull(message, "an error message should be set");
			assertTrue(message.contains(TARGET_ID), "error message should name the target: " + message);
		});
	}

	@Test
	public void helpLinkIsShownAndSettingsAreDisabled() throws Throwable {
		runOnUiThread(() -> {
			createAndInitializeTab();

			List<Link> links = findControls(shell, Link.class);
			assertTrue(links.stream().anyMatch(Link::getVisible), "help link should be visible");

			List<Button> checkboxes = new ArrayList<>(findControls(shell, Button.class));
			checkboxes.removeIf(button -> (button.getStyle() & SWT.CHECK) == 0);
			assertFalse(checkboxes.isEmpty(), "expected the CMake settings checkbox");
			for (Button checkbox : checkboxes) {
				assertFalse(checkbox.getEnabled(), "CMake settings should be disabled: " + checkbox.getText());
			}
		});
	}

	@Test
	public void applyingWithoutToolchainsDoesNotFail() throws Throwable {
		runOnUiThread(() -> {
			createAndInitializeTab();

			ILaunchConfigurationWorkingCopy workingCopy = launchConfig.getWorkingCopy();
			tab.setDefaults(workingCopy);
			tab.performApply(workingCopy);
			assertFalse(workingCopy.hasAttribute(ICBuildConfiguration.TOOLCHAIN_ID));
			assertFalse(workingCopy.hasAttribute(ICBuildConfiguration.TOOLCHAIN_TYPE));
		});
	}

	/**
	 * Creates the tab the same way the launch bar dialog does. Must be called on the UI thread.
	 */
	private void createAndInitializeTab() throws Exception {
		shell = new Shell(Display.getDefault());
		tab = new CoreBuildTab();
		tab.setLaunchConfigurationDialog(createDialog());
		tab.createControl(shell);
		tab.initializeFrom(launchConfig);
	}

	/**
	 * The tabs only use the launch target and the mode from the dialog.
	 */
	private ILaunchBarLaunchConfigDialog createDialog() {
		return (ILaunchBarLaunchConfigDialog) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { ILaunchBarLaunchConfigDialog.class }, (proxy, method, args) -> {
					switch (method.getName()) {
					case "getLaunchTarget":
						return target;
					case "getMode":
						return ILaunchManager.RUN_MODE;
					default:
						Class<?> returnType = method.getReturnType();
						return returnType.isPrimitive() && returnType != void.class
								? Array.get(Array.newInstance(returnType, 1), 0)
								: null;
					}
				});
	}

	private static <T extends Control> List<T> findControls(Composite parent, Class<T> type) {
		List<T> result = new ArrayList<>();
		for (Control child : parent.getChildren()) {
			if (type.isInstance(child)) {
				result.add(type.cast(child));
			}
			if (child instanceof Composite) {
				result.addAll(findControls((Composite) child, type));
			}
		}
		return result;
	}

	private static void runOnUiThread(Executable executable) throws Throwable {
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Display.getDefault().syncExec(() -> {
			try {
				executable.execute();
			} catch (Throwable t) {
				failure.set(t);
			}
		});
		if (failure.get() != null) {
			throw failure.get();
		}
	}
}
