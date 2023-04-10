package org.eclipse.cdt.tests.dsf.gdb.tests.nonstop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil.DefaultTimeouts;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil.DefaultTimeouts.ETimeout;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.IPath;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultiProcessRemoteTest extends BaseParametrizedTestCase {
	private static final String EXEC_NAME = "MultiThread.exe";

	private DsfServicesTracker fServicesTracker;

	private IGDBProcesses fGdbProcesses;
	private ICommandControlService fCommandControl;
	private IGDBBackend fGDBBackend;

	private List<Process> appProcesses = new ArrayList<>();

	@BeforeClass
	public static void beforeClass() {
		Assume.assumeTrue(supportsNonStop());
	}

	@Override
	public void doBeforeTest() throws Exception {
		assumeRemoteSession();
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_0);
		super.doBeforeTest();

		final DsfSession session = getGDBLaunch().getSession();

		Runnable runnable = () -> {
			fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), session.getId());
			fGdbProcesses = fServicesTracker.getService(IGDBProcesses.class);
			fCommandControl = fServicesTracker.getService(ICommandControlService.class);
			fGDBBackend = fServicesTracker.getService(IGDBBackend.class);
		};
		session.getExecutor().submit(runnable).get();
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP, true);
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, getBinary());
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
				IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE_ATTACH);
		setLaunchAttribute(ITestConstants.LAUNCH_GDB_SERVER_MULTI, Boolean.TRUE.toString());
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_BINARY,
				getLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME));
		if (testName.getMethodName().contains("_noProgram")) {
			setLaunchAttribute(ITestConstants.LAUNCH_GDB_SERVER_WITHOUT_PROGRAM, Boolean.TRUE.toString());
		}
	}

	@Override
	public void doAfterTest() throws Exception {
		if (fServicesTracker != null)
			fServicesTracker.dispose();
		super.doAfterTest();
		for (Process p : appProcesses) {
			if (p.isAlive())
				p.destroyForcibly();
		}
		appProcesses.clear();
	}

	@Override
	protected boolean isTargetExpectedToStopAfterLaunch() {
		// For some tests, we'll get stop event but not for others. Checking stop event
		// at start of launch is not required for this test class
		return false;
	}

	@Test
	public void launchGdbServerWithProgramAndStartMulipleNewExecutables() throws Throwable {
		assertEquals("Wrong number of containers at the start of launch", SyncUtil.getAllContainerContexts().length,
				getInitialProcessCount());

		final IPath execPath = fGDBBackend.getProgramPath();
		Map<String, Object> attributes = getLaunchConfiguration().getAttributes();

		IMIExecutionDMContext[] ctx = debugNewProcess(fGDBBackend.getProgramPath(), attributes);
		assertTrue("Process-1 is not started properly as no threads found for it", ctx != null && ctx.length > 0);

		ctx = debugNewProcess(execPath, attributes);
		assertTrue("Process-2 is not started properly as no threads found for it", ctx != null && ctx.length > 0);

		ctx = debugNewProcess(execPath, attributes);
		assertTrue("Process-3 is not started properly as no threads found for it", ctx != null && ctx.length > 0);
	}

	@Test
	public void launchGdbServerWithoutProgramAndStartMulipleNewExecutables_noProgram() throws Throwable {
		launchGdbServerWithProgramAndStartMulipleNewExecutables();
	}

	@Test
	public void launchGdbServerWithProgramAndConnectMultipleExecutables() throws Throwable {
		assertEquals("Wrong number of containers at the start of launch", SyncUtil.getAllContainerContexts().length,
				getInitialProcessCount());

		Process process = launchApplication();
		IMIExecutionDMContext[] ctx = attachToProcess(Long.toString(process.pid()));
		assertTrue("Process-1 is not attached properly as no threads found for it", ctx != null && ctx.length > 0);

		process = launchApplication();
		ctx = attachToProcess(Long.toString(process.pid()));
		assertTrue("Process-2 is not attached properly as no threads found for it", ctx != null && ctx.length > 0);

		process = launchApplication();
		ctx = attachToProcess(Long.toString(process.pid()));
		assertTrue("Process-3 is not attached properly as no threads found for it", ctx != null && ctx.length > 0);
	}

	@Test
	public void launchGdbServerWithoutProgramAndConnectMultipleExecutables_noProgram() throws Throwable {
		launchGdbServerWithProgramAndConnectMultipleExecutables();
	}

	@Test
	public void launchGdbServerWithProgram_DebugNewExecutable_And_ConnectExecutable() throws Throwable {
		assertEquals("Wrong number of containers at the start of launch", SyncUtil.getAllContainerContexts().length,
				getInitialProcessCount());

		final IPath execPath = fGDBBackend.getProgramPath();
		Map<String, Object> attributes = getLaunchConfiguration().getAttributes();

		IMIExecutionDMContext[] ctx = debugNewProcess(execPath, attributes);
		assertTrue("Process-1 is not started properly as no threads found for it", ctx != null && ctx.length > 0);

		Process process = launchApplication();
		ctx = attachToProcess(Long.toString(process.pid()));
		assertTrue("Process-2 is not attached properly as no threads found for it", ctx != null && ctx.length > 0);
	}

	@Test
	public void launchGdbServerWithoutProgram_DebugNewExecutable_And_ConnectExecutable_noProgram() throws Throwable {
		launchGdbServerWithProgram_DebugNewExecutable_And_ConnectExecutable();
	}

	@Test
	public void launchGdbServerWithProgram_ConnectExecutable_And_DebugNewExecutable() throws Throwable {
		assertEquals("Wrong number of containers at the start of launch", SyncUtil.getAllContainerContexts().length,
				getInitialProcessCount());

		Process process = launchApplication();
		IMIExecutionDMContext[] ctx = attachToProcess(Long.toString(process.pid()));
		assertTrue("Process-1 is not attached properly as no threads found for it", ctx != null && ctx.length > 0);

		final IPath execPath = fGDBBackend.getProgramPath();
		Map<String, Object> attributes = getLaunchConfiguration().getAttributes();

		ctx = debugNewProcess(execPath, attributes);
		assertTrue("Process-2 is not started properly as no threads found for it", ctx != null && ctx.length > 0);
	}

	@Test
	public void launchGdbServerWithoutProgram_ConnectExecutable_And_DebugNewExecutable_noProgram() throws Throwable {
		launchGdbServerWithProgram_ConnectExecutable_And_DebugNewExecutable();
	}

	private int getInitialProcessCount() {
		return Boolean.valueOf((String) getLaunchAttribute(ITestConstants.LAUNCH_GDB_SERVER_WITHOUT_PROGRAM)) ? 0 : 1;
	}

	private Process launchApplication() throws IOException {
		Process process = ProcessFactory.getFactory()
				.exec(new String[] { Path.of(getBinary()).toAbsolutePath().toString() });
		if (process.isAlive()) {
			appProcesses.add(process);
			return process;
		}

		throw new IOException("Unable to launch application");
	}

	private static String getBinary() {
		return EXEC_PATH + EXEC_NAME;
	}

	private IMIExecutionDMContext[] debugNewProcess(final IPath execPath, Map<String, Object> attributes)
			throws InterruptedException, ExecutionException, TimeoutException {
		Query<IDMContext> query = new Query<>() {
			@Override
			protected void execute(DataRequestMonitor<IDMContext> rm) {
				fGdbProcesses.debugNewProcess(fCommandControl.getContext(), execPath.toOSString(), attributes, rm);
			}
		};
		fGDBBackend.getExecutor().execute(query);
		IDMContext context = query.get(DefaultTimeouts.get(ETimeout.waitForStop), TimeUnit.MILLISECONDS);
		return fGdbProcesses.getExecutionContexts(DMContexts.getAncestorOfType(context, IMIContainerDMContext.class));
	}

	private IMIExecutionDMContext[] attachToProcess(String pid)
			throws InterruptedException, ExecutionException, TimeoutException {
		Query<IDMContext> query = new Query<>() {
			@Override
			protected void execute(DataRequestMonitor<IDMContext> rm) {
				IProcessDMContext procDmc = fGdbProcesses.createProcessContext(fCommandControl.getContext(), pid);
				fGdbProcesses.attachDebuggerToProcess(procDmc, getBinary(), rm);
			}
		};
		fGDBBackend.getExecutor().execute(query);
		IDMContext context = query.get(DefaultTimeouts.get(ETimeout.waitForStop), TimeUnit.MILLISECONDS);
		return fGdbProcesses.getExecutionContexts(DMContexts.getAncestorOfType(context, IMIContainerDMContext.class));
	}
}
