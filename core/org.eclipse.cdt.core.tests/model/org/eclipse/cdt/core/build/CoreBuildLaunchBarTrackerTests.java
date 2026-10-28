/*******************************************************************************
 * Copyright (c) 2026 Sergii Zolotarov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.cdt.cmake.core.CMakeNature;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.launch.CoreBuildLaunchBarTracker;
import org.eclipse.cdt.internal.core.build.CBuildConfigurationManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CoreBuildLaunchBarTracker}, see
 * <a href="https://github.com/eclipse-cdt/cdt/issues/1542">#1542</a>.
 */
public class CoreBuildLaunchBarTrackerTests extends BaseTestCase5 {
	private static final long TIMEOUT_MS = 5000;
	private static final String TC_BUILD_CONFIG_NAME_FRAGMENT = "MockToolchain"; //$NON-NLS-1$

	private final ICBuildConfigurationManager configManager = CDebugCorePlugin
			.getService(ICBuildConfigurationManager.class);
	private final ILaunchTargetManager launchTargetManager = CDebugCorePlugin.getService(ILaunchTargetManager.class);
	private final IToolChainManager toolchainManager = CDebugCorePlugin.getService(IToolChainManager.class);
	private final ILaunchBarManager launchBarManager = CDebugCorePlugin.getService(ILaunchBarManager.class);

	private final List<IToolChain> removedToolchains = new ArrayList<>();
	private IToolChain mockToolchain;
	private IProject project;
	private ILaunchTarget localTarget;
	private ILaunchTarget otherTarget;

	@BeforeEach
	public void setup() throws Exception {
		// Use only the mocked toolchain, whatever is installed on the host
		removedToolchains.addAll(toolchainManager.getAllToolChains());
		for (IToolChain toolchain : removedToolchains) {
			toolchainManager.removeToolChain(toolchain);
		}
		((CBuildConfigurationManager) configManager).reset();
		CDebugCorePlugin.getDefault().resetCoreBuildLaunchBarTracker();
		addMockToolchain();

		localTarget = launchTargetManager.getLocalLaunchTarget();
		project = createCMakeProject();
		waitForLaunchBarTracker();

		// Start from Run on the local target
		launchBarManager.setActiveLaunchTarget(localTarget);
		launchBarManager.setActiveLaunchMode(launchMode(ILaunchManager.RUN_MODE));
		waitForLaunchBarTracker();
		assertThat(activeConfig(), is(ILaunchManager.RUN_MODE + '/' + localTarget.getId()));
	}

	@AfterEach
	public void cleanup() throws Exception {
		waitForLaunchBarTracker();
		if (otherTarget != null) {
			launchTargetManager.removeLaunchTarget(otherTarget);
		}
		toolchainManager.removeToolChain(mockToolchain);
		for (IToolChain toolchain : removedToolchains) {
			toolchainManager.addToolChain(toolchain);
		}
	}

	/**
	 * Quick launch bar changes must only create build configurations for selected mode and
	 * target pairs.
	 */
	@Test
	public void quickChangesDoNotCreateConfigsForUnselectedCombinations() throws Exception {
		// Hold the tracker jobs back while the launch bar changes, as a busy workspace would
		ISchedulingRule root = ResourcesPlugin.getWorkspace().getRoot();
		Job.getJobManager().beginRule(root, null);
		try {
			launchBarManager.setActiveLaunchMode(launchMode(ILaunchManager.DEBUG_MODE)); // Debug, local
			launchBarManager.setActiveLaunchMode(launchMode(ILaunchManager.RUN_MODE)); // Run, local
			launchBarManager.setActiveLaunchTarget(addOtherTarget()); // Run, other
		} finally {
			Job.getJobManager().endRule(root);
		}
		waitForLaunchBarTracker();

		String runOnOther = ILaunchManager.RUN_MODE + '/' + otherTarget.getId();
		String debugOnOther = ILaunchManager.DEBUG_MODE + '/' + otherTarget.getId();
		assertThat(activeConfig(), is(runOnOther));
		assertThat("Debug was never selected for " + otherTarget.getId(), configs(), not(hasItem(debugOnOther))); //$NON-NLS-1$
	}

	/**
	 * Setting the active build configuration while a tracker job runs must not deadlock.
	 */
	@Test
	public void setActiveDoesNotDeadlockWithTracker() throws Exception {
		addOtherTarget();
		launchBarManager.setActiveLaunchTarget(localTarget);
		waitForLaunchBarTracker();
		// A second, inactive build configuration to activate
		launchBarManager.setActiveLaunchMode(launchMode(ILaunchManager.DEBUG_MODE));
		waitForLaunchBarTracker();
		IBuildConfiguration debugConfig = project.getActiveBuildConfig();
		launchBarManager.setActiveLaunchMode(launchMode(ILaunchManager.RUN_MODE));
		waitForLaunchBarTracker();
		CBuildConfiguration toActivate = (CBuildConfiguration) configManager.getBuildConfiguration(debugConfig);
		assertThat(toActivate, is(notNullValue()));

		NullProgressMonitor callerMonitor = new NullProgressMonitor();
		AtomicReference<Throwable> callerError = new AtomicReference<>();
		Thread caller = new Thread(() -> {
			try {
				toActivate.setActive(callerMonitor);
			} catch (OperationCanceledException e) {
				// The test cancels the caller to break a deadlock
			} catch (Throwable e) {
				callerError.set(e);
			}
		}, "Set active build configuration"); //$NON-NLS-1$
		caller.setDaemon(true);

		// Pause the tracker job while the caller runs, until the caller waits or is done
		CountDownLatch trackerPaused = new CountDownLatch(1);
		AtomicBoolean callerRan = new AtomicBoolean();
		when(mockToolchain.matches(anyMap())).thenAnswer(invocation -> {
			Job job = Job.getJobManager().currentJob();
			if (job != null && job.belongsTo(CoreBuildLaunchBarTracker.JOB_FAMILY_CORE_BUILD_LAUNCH_BAR_TRACKER)
					&& trackerPaused.getCount() > 0) {
				trackerPaused.countDown();
				callerRan.set(awaitWaitingOrDone(caller));
			}
			return true;
		});
		launchBarManager.setActiveLaunchTarget(otherTarget);
		assertThat("Tracker job did not run", trackerPaused.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)); //$NON-NLS-1$
		caller.start();

		caller.join(TIMEOUT_MS);
		boolean deadlocked = !trackerJobsFinished() || caller.isAlive();
		if (deadlocked) {
			// Break the deadlock so that the other tests can run
			callerMonitor.setCanceled(true);
			caller.join(TIMEOUT_MS);
			trackerJobsFinished();
		}
		assertThat("Caller and tracker job deadlocked", deadlocked, is(false)); //$NON-NLS-1$
		assertThat("Caller did not run while the tracker job was paused", callerRan.get(), is(true)); //$NON-NLS-1$
		assertThat("Caller failed: " + callerError.get(), callerError.get(), is((Throwable) null)); //$NON-NLS-1$
	}

	private ILaunchTarget addOtherTarget() {
		otherTarget = launchTargetManager.addLaunchTarget(ILaunchTargetManager.localLaunchTargetTypeId,
				"trackerTestTarget"); //$NON-NLS-1$
		return otherTarget;
	}

	private ILaunchMode launchMode(String id) throws CoreException {
		for (ILaunchMode mode : launchBarManager.getLaunchModes()) {
			if (mode.getIdentifier().equals(id)) {
				return mode;
			}
		}
		throw new AssertionError("No launch mode " + id); //$NON-NLS-1$
	}

	/**
	 * @return the active build configuration, as "mode/targetId"
	 */
	private String activeConfig() throws CoreException {
		return describe(configManager.getBuildConfiguration(project.getActiveBuildConfig()));
	}

	/**
	 * @return the project's core build configurations, as "mode/targetId"
	 */
	private Set<String> configs() throws CoreException {
		Set<String> configs = new LinkedHashSet<>();
		for (IBuildConfiguration config : project.getBuildConfigs()) {
			ICBuildConfiguration cconfig = configManager.getBuildConfiguration(config);
			if (cconfig != null && !(cconfig instanceof ErrorBuildConfiguration)) {
				configs.add(describe(cconfig));
			}
		}
		return configs;
	}

	private static String describe(ICBuildConfiguration config) {
		if (config == null) {
			return null;
		}
		ILaunchTarget target = config.getLaunchTarget();
		return config.getLaunchMode() + '/' + (target == null ? null : target.getId());
	}

	private IProject createCMakeProject() throws Exception {
		IProject project = ResourceHelper.createProject(getName());
		IProjectDescription description = project.getDescription();
		description.setNatureIds(
				new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID, CMakeNature.ID });
		project.setDescription(description, null);
		return project;
	}

	private void addMockToolchain() throws CoreException {
		mockToolchain = mock(IToolChain.class);
		when(mockToolchain.getProperty(anyString())).thenAnswer(invocation -> {
			String key = invocation.getArgument(0);
			return switch (key) {
			case IToolChain.ATTR_OS -> Platform.getOS();
			case IToolChain.ATTR_ARCH -> Platform.getOSArch();
			default -> null;
			};
		});
		when(mockToolchain.getTypeId()).thenReturn("tc_typeId"); //$NON-NLS-1$
		when(mockToolchain.getId()).thenReturn("tcId"); //$NON-NLS-1$
		// Match every launch target, including the test's own
		when(mockToolchain.matches(anyMap())).thenReturn(true);
		when(mockToolchain.getBuildConfigNameFragment()).thenReturn(TC_BUILD_CONFIG_NAME_FRAGMENT);
		when(mockToolchain.getName()).thenReturn("MockToolchainName"); //$NON-NLS-1$
		toolchainManager.addToolChain(mockToolchain);
	}

	private static void waitForLaunchBarTracker() throws InterruptedException {
		Job.getJobManager().join(CoreBuildLaunchBarTracker.JOB_FAMILY_CORE_BUILD_LAUNCH_BAR_TRACKER, null);
	}

	/**
	 * @return whether all tracker jobs finished within {@link #TIMEOUT_MS}
	 */
	private static boolean trackerJobsFinished() throws InterruptedException {
		for (Job job : Job.getJobManager().find(CoreBuildLaunchBarTracker.JOB_FAMILY_CORE_BUILD_LAUNCH_BAR_TRACKER)) {
			if (!job.join(TIMEOUT_MS, null)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return whether the thread waited or ended within {@link #TIMEOUT_MS}
	 */
	private static boolean awaitWaitingOrDone(Thread thread) throws InterruptedException {
		long end = System.currentTimeMillis() + TIMEOUT_MS;
		while (System.currentTimeMillis() < end) {
			Thread.State state = thread.getState();
			if (state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING
					|| state == Thread.State.TERMINATED) {
				return true;
			}
			Thread.sleep(10);
		}
		return false;
	}
}
