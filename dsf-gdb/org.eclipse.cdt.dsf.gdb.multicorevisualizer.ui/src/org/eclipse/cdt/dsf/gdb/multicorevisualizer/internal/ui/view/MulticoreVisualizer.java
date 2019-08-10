/*******************************************************************************
 * Copyright (c) 2012, 2015 Tilera Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *     IBM Corporation
 *     Marc Dumais (Ericsson) - Bug 399281
 *     Marc Dumais (Ericsson) - Add CPU/core load information to the multicore visualizer (Bug 396268)
 *     Marc Dumais (Ericsson) - Bug 399419
 *     Marc Dumais (Ericsson) - Bug 405390
 *     Marc Dumais (Ericsson) - Bug 409006
 *     Marc Dumais (Ericsson) - Bug 407321
 *     Marc-Andre Laperle (Ericsson) - Bug 411634
 *     Marc Dumais (Ericsson) - Bug 409965
 *     Xavier Raynaud (kalray) - Bug 431935
 *     Marc Dumais (Ericsson) - Bug 441713
 *     Marc Dumais (Ericsson) - Bug 442312
 *     Marc Dumais (Ericsson) - Bug 451392
 *     Marc Dumais (Ericsson) - Bug 453206
 *     Marc Dumais (Ericsson) - Bug 458076
 *     Alvaro Sanchez-Leon (Ericsson) - Bug 459114 - override construction of the data model
 *     Marc Dumais (Ericsson) - Bug 460737
 *     Marc Dumais (Ericsson) - Bug 460837
 *     Marc Dumais (Ericsson) - Bug 460476
 *     Marc Khouzam (Ericsson) - Use DSF usual async pattern (Bug 459114)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateCountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.gdb.launching.GDBProcess;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions.EnableLoadMetersAction;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions.FilterCanvasAction;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions.PinToDebugSessionAction;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions.RefreshAction;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions.SelectAllAction;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions.SetLoadMeterPeriodAction;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions.ShowDebugToolbarAction;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCPU;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCore;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerLoadInfo;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DSFDebugModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DSFSessionState;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DebugViewUtils;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.IDSFTargetDataProxy;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.PersistentSettingsManager;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.PersistentSettingsManager.PersistentParameter;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.ICPUDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.ICoreDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2.ILoadInfo;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvas;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvasVisualizer;
import org.eclipse.cdt.visualizer.ui.plugin.CDTVisualizerUIPlugin;
import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.cdt.visualizer.ui.util.SelectionUtils;
import org.eclipse.cdt.visualizer.ui.util.Timer;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.internal.ui.commands.actions.DropToFrameCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.ResumeCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.StepIntoCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.StepOverCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.StepReturnCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.SuspendCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.TerminateCommandAction;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * The Multicore Visualizer is a generic visualizer that displays
 * CPUs, cores, threads graphically.
 *
 * This visualizer uses the CDT Visualizer framework.
 */
@SuppressWarnings("restriction")
public class MulticoreVisualizer extends GraphicCanvasVisualizer implements IPinnable {
	// --- constants ---

	private static final String THE_THREAD_ID_DOES_NOT_CONVERT_TO_AN_INTEGER = "The thread id does not convert to an integer: "; //$NON-NLS-1$

	/** Eclipse ID for this view */
	public static final String ECLIPSE_ID = "org.eclipse.cdt.dsf.gdb.multicorevisualizer.visualizer"; //$NON-NLS-1$

	// --- members ---

	/**
	 * The data model drawn by this visualizer.
	 */
	protected VisualizerModel fDataModel;

	/**
	 * Proxy to the target data needed to build the model
	 */
	protected IDSFTargetDataProxy fTargetData;

	/** Downcast reference to canvas. */
	protected MulticoreVisualizerCanvas m_canvas;

	/** DSF debug context session object. */
	protected DSFSessionState m_sessionState;

	/** Event listener class for DSF events */
	protected MulticoreVisualizerEventListener fEventListener;

	/** Cached reference to Debug View viewer. */
	protected TreeModelViewer m_debugViewer = null;

	/** Model changed listener, attached to Debug View. */
	protected IModelChangedListener m_modelChangedListener = null;

	/** Debug view selection changed listener, attached to Debug View. */
	protected ISelectionChangedListener m_debugViewSelectionChangedListener = null;

	/** Unique id that differentiates the possible multiple instances of the MV.
	 * It's derived from the secondary view Part id of the view associated to the
	 * current instance of the MV. */
	protected String m_visualizerInstanceId = null;

	// This is used to cache the CPU and core
	// contexts, each time the model is recreated.  This way
	// we can avoid asking the backend for the CPU/core
	// geometry each time we want to update the load information.
	protected List<IDMContext> m_cpuCoreContextsCache = null;

	/** Main switch that determines if we should display the load meters */
	private PersistentParameter<Boolean> m_loadMetersEnabled;

	/** Timer used to trigger the update of the CPU/core load meters */
	protected Timer m_updateLoadMeterTimer = null;

	/** update period for the load meters */
	private PersistentParameter<Integer> m_loadMeterTimerPeriod;

	// Load meters refresh periods, in ms
	/** constant for the very short load meters update period */
	private static final int LOAD_METER_TIMER_MIN = 100;
	/** constant for the short load meters update period */
	private static final int LOAD_METER_TIMER_FAST = 500;
	/** constant for the medium load meters update period */
	private static final int LOAD_METER_TIMER_MEDIUM = 1000;
	/** constant for the long load meters update period */
	private static final int LOAD_METER_TIMER_SLOW = 5000;

	/** Whether to show debug actions in toolbar, by default */
	private static final boolean SHOW_DEBUG_ACTIONS_IN_MV_TOOLBAR_DEFAULT = true;

	/** Currently pinned session id, if any  */
	private String m_currentPinedSessionId = null;

	// --- UI members ---

	/** Whether actions have been initialized. */
	protected boolean m_actionsInitialized = false;

	/** Toolbar / menu action */
	protected Separator m_separatorAction = null;

	/** Toolbar / menu action */
	protected ResumeCommandAction m_resumeAction = null;

	/** Toolbar / menu action */
	protected SuspendCommandAction m_suspendAction = null;

	/** Toolbar / menu action */
	protected TerminateCommandAction m_terminateAction = null;

	/** Toolbar / menu action */
	protected StepReturnCommandAction m_stepReturnAction = null;

	/** Toolbar / menu action */
	protected StepOverCommandAction m_stepOverAction = null;

	/** Toolbar / menu action */
	protected StepIntoCommandAction m_stepIntoAction = null;

	/** Toolbar / menu action */
	protected DropToFrameCommandAction m_dropToFrameAction = null;

	/** Toolbar / menu action */
	protected SelectAllAction m_selectAllAction = null;

	/** Toolbar / menu action */
	protected RefreshAction m_refreshAction = null;

	/** Sub-menu */
	protected IMenuManager m_loadMetersSubMenu = null;

	/** Sub-sub menu */
	protected IMenuManager m_loadMetersRefreshSubSubmenu = null;

	/** Menu action */
	protected EnableLoadMetersAction m_enableLoadMetersAction = null;

	/** Menu action */
	protected List<SetLoadMeterPeriodAction> m_setLoadMeterPeriodActions = null;

	/** Menu action */
	protected FilterCanvasAction m_setFilterAction = null;

	/** Menu action */
	protected FilterCanvasAction m_clearFilterAction = null;

	/** Menu action */
	protected PinToDebugSessionAction m_pinToDbgSessionAction = null;

	/** Menu action */
	protected ShowDebugToolbarAction m_showDebugToolbarAction = null;

	/** persistent settings manager */
	protected PersistentSettingsManager m_persistentSettingsManager = null;

	// --- constructors/destructors ---

	/** Constructor. */
	public MulticoreVisualizer() {
		fTargetData = new DSFDebugModel();
	}

	/** Dispose method. */
	@Override
	public void dispose() {
		super.dispose();
		removeDebugViewerListener();
		disposeActions();
		disposeLoadMeterTimer();
		removeEventListener();
		// dispose CPU/core contexts cache
		if (m_cpuCoreContextsCache != null) {
			m_cpuCoreContextsCache.clear();
			m_cpuCoreContextsCache = null;
		}
	}

	// --- init methods ---

	/** Invoked when visualizer is created, to permit any initialization. */
	@Override
	public void initializeVisualizer() {
		fEventListener = new MulticoreVisualizerEventListener(this);
		m_cpuCoreContextsCache = new ArrayList<>();
		m_visualizerInstanceId = getViewer().getView().getViewSite().getSecondaryId();

		// The first visualizer view will have a null secondary id - override that
		if (m_visualizerInstanceId == null) {
			m_visualizerInstanceId = "0"; //$NON-NLS-1$
		}
		initializePersistentParameters(m_visualizerInstanceId);
	}

	/**
	 * Initialize the persistent parameters
	 */
	protected void initializePersistentParameters(String visualizerInstanceId) {
		// setting managers
		m_persistentSettingsManager = new PersistentSettingsManager("MulticoreVisualizer", visualizerInstanceId); //$NON-NLS-1$

		// define persistent parameters:
		m_loadMetersEnabled = m_persistentSettingsManager.getNewParameter(Boolean.class, "enableLoadMeters", true, //$NON-NLS-1$
				false);
		m_loadMeterTimerPeriod = m_persistentSettingsManager.getNewParameter(Integer.class, "loadMeterTimerPeriod", //$NON-NLS-1$
				true, LOAD_METER_TIMER_MEDIUM);
	}

	/**
	 * Sets-up the timer associated to load meters refresh
	 */
	protected void initializeLoadMeterTimer() {
		if (!getLoadMetersEnabled())
			return;
		m_updateLoadMeterTimer = getLoadTimer(m_sessionState, getLoadMeterTimerPeriod());
		// one-shot timer (re-scheduled upon successful triggering)
		m_updateLoadMeterTimer.setRepeating(false);
	}

	/**
	 * disposes of the load meter timer
	 */
	protected void disposeLoadMeterTimer() {
		if (m_updateLoadMeterTimer != null) {
			m_updateLoadMeterTimer.dispose();
			m_updateLoadMeterTimer = null;
		}
	}

	/** Invoked when visualizer is disposed, to permit any cleanup. */
	@Override
	public void disposeVisualizer() {
		// handle any other cleanup
		dispose();
	}

	// --- accessors ---

	/** Returns non-localized unique name for this visualizer. */
	@Override
	public String getName() {
		return "multicore"; //$NON-NLS-1$
	}

	/** Returns localized name to display for this visualizer. */
	@Override
	public String getDisplayName() {
		return Messages.MulticoreVisualizer_name;
	}

	/** Returns localized tooltip text to display for this visualizer. */
	@Override
	public String getDescription() {
		return Messages.MulticoreVisualizer_tooltip;
	}

	/**
	 * takes care of the details of changing the load meter timer period
	 */
	public void setLoadMeterTimerPeriod(int p) {
		assert (p > LOAD_METER_TIMER_MIN);
		if (getLoadMeterTimerPeriod() == p)
			return;
		m_loadMeterTimerPeriod.set(p > LOAD_METER_TIMER_MIN ? p : LOAD_METER_TIMER_MIN);
		disposeLoadMeterTimer();
		initializeLoadMeterTimer();
	}

	/** Gets the load meter period */
	public int getLoadMeterTimerPeriod() {
		return m_loadMeterTimerPeriod != null ? m_loadMeterTimerPeriod.value() : 0;
	}

	/**
	 * enables or disables the load meters
	 */
	public void enableLoadMeters(boolean enabled) {
		if (getLoadMetersEnabled() == enabled)
			return;
		setLoadMetersEnabled(enabled);
		// save load meter enablement in model
		fDataModel.setLoadMetersEnabled(getLoadMetersEnabled());
		disposeLoadMeterTimer();
		initializeLoadMeterTimer();
	}

	/** Returns whether the load meters are enabled */
	public boolean getLoadMetersEnabled() {
		return m_loadMetersEnabled != null ? m_loadMetersEnabled.value() : false;
	}

	public void setLoadMetersEnabled(boolean enabled) {
		m_loadMetersEnabled.set(enabled);
	}

	// --- canvas management ---

	/** Creates and returns visualizer canvas control. */
	@Override
	public GraphicCanvas createCanvas(Composite parent) {
		m_canvas = new MulticoreVisualizerCanvas(parent);
		m_canvas.addSelectionChangedListener(this);
		return m_canvas;
	}

	/** Invoked when canvas control should be disposed. */
	@Override
	public void disposeCanvas() {
		if (m_canvas != null) {
			m_canvas.removeSelectionChangedListener(this);
			m_canvas.dispose();
			m_canvas = null;
		}
		disposeLoadMeterTimer();
	}

	/** Invoked after visualizer control creation, */
	@Override
	protected void initializeCanvas(GraphicCanvas canvas) {
		// Any workbench views left open at application shutdown may be instanced
		// before our plugins are fully loaded, so make sure resource manager is initialized.
		// Note: this also associates the resource manager with the Colors class;
		// until this is done, the Colors constants are null.
		CDTVisualizerUIPlugin.getResources();

		m_canvas.setBackground(Colors.BLACK);
		m_canvas.setForeground(Colors.GREEN);
	}

	/** Returns downcast reference to grid view canvas. */
	public MulticoreVisualizerCanvas getMulticoreVisualizerCanvas() {
		return (MulticoreVisualizerCanvas) getCanvas();
	}

	/** Sets-up a canvas filter */
	public void applyCanvasFilter() {
		m_canvas.applyFilter();
		refresh();
	}

	/** Removes current canvas filter */
	public void clearCanvasFilter() {
		m_canvas.clearFilter();
		refresh();
	}

	/** Tells if a canvas filter is in effect */
	public boolean isCanvasFilterActive() {
		return m_canvas.isFilterActive();
	}

	/** Return the data model backing this multicore visualizer */
	public VisualizerModel getModel() {
		return fDataModel;
	}

	// --- action management ---

	/** Creates actions for menus/toolbar. */
	protected void createActions() {
		if (m_actionsInitialized)
			return; // already done

		LaunchView debugView = DebugViewUtils.getDebugView();

		m_separatorAction = new Separator();

		m_resumeAction = new ResumeCommandAction();
		if (debugView != null)
			m_resumeAction.init(debugView);

		m_suspendAction = new SuspendCommandAction();
		if (debugView != null)
			m_suspendAction.init(debugView);

		m_terminateAction = new TerminateCommandAction();
		if (debugView != null)
			m_terminateAction.init(debugView);

		m_stepReturnAction = new StepReturnCommandAction();
		if (debugView != null)
			m_stepReturnAction.init(debugView);

		m_stepOverAction = new StepOverCommandAction();
		if (debugView != null)
			m_stepOverAction.init(debugView);

		m_stepIntoAction = new StepIntoCommandAction();
		if (debugView != null)
			m_stepIntoAction.init(debugView);

		m_dropToFrameAction = new DropToFrameCommandAction();
		if (debugView != null)
			m_dropToFrameAction.init(debugView);

		m_selectAllAction = new SelectAllAction();
		m_selectAllAction.init(this);

		m_refreshAction = new RefreshAction();
		m_refreshAction.init(this);

		// create load meters sub-menu and associated actions
		m_loadMetersSubMenu = new MenuManager(
				MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.LoadMeterSubmenu.text")); //$NON-NLS-1$
		m_loadMetersRefreshSubSubmenu = new MenuManager(
				MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.LoadMetersRefreshSubSubmenu.text")); //$NON-NLS-1$

		m_enableLoadMetersAction = new EnableLoadMetersAction(getLoadMetersEnabled());
		m_enableLoadMetersAction.init(this);
		// enable the load meter sub-menu
		m_enableLoadMetersAction.setEnabled(true);

		m_setLoadMeterPeriodActions = new ArrayList<>();
		m_setLoadMeterPeriodActions.add(new SetLoadMeterPeriodAction(
				MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.SetLoadMeterPeriod.fast.text"), //$NON-NLS-1$
				LOAD_METER_TIMER_FAST));

		// TODO: the default load meter refresh speed is set here but we could instead rely on the value saved in the data store
		SetLoadMeterPeriodAction defaultAction = new SetLoadMeterPeriodAction(
				MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.SetLoadMeterPeriod.medium.text"), //$NON-NLS-1$
				LOAD_METER_TIMER_MEDIUM);
		m_setLoadMeterPeriodActions.add(defaultAction);

		m_setLoadMeterPeriodActions.add(new SetLoadMeterPeriodAction(
				MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.SetLoadMeterPeriod.slow.text"), //$NON-NLS-1$
				LOAD_METER_TIMER_SLOW));
		for (SetLoadMeterPeriodAction act : m_setLoadMeterPeriodActions) {
			act.init(this);
			act.setEnabled(true);
		}
		defaultAction.setChecked(true);
		defaultAction.run();

		// canvas filter actions - they will be dynamically enabled/disabled
		// according to canvas selection
		m_setFilterAction = new FilterCanvasAction(true);
		m_setFilterAction.init(this);
		m_setFilterAction.setEnabled(false);

		m_clearFilterAction = new FilterCanvasAction(false);
		m_clearFilterAction.init(this);
		m_clearFilterAction.setEnabled(false);

		m_pinToDbgSessionAction = new PinToDebugSessionAction();
		m_pinToDbgSessionAction.init(this);
		m_pinToDbgSessionAction.setEnabled(false);

		// default: do not show debug actions
		m_showDebugToolbarAction = new ShowDebugToolbarAction(SHOW_DEBUG_ACTIONS_IN_MV_TOOLBAR_DEFAULT,
				m_visualizerInstanceId);
		m_showDebugToolbarAction.init(this);
		m_showDebugToolbarAction.setEnabled(true);

		// Note: debug view may not be initialized at startup,
		// so we'll pretend the actions are not yet updated,
		// and reinitialize them later.
		m_actionsInitialized = (debugView != null);
	}

	/** Updates actions displayed on menu/toolbars. */
	protected void updateActions() {
		if (!m_actionsInitialized)
			return;

		boolean enabled = hasSelection();
		m_selectAllAction.setEnabled(enabled);
		m_refreshAction.setEnabled(enabled);

		// enable "filter-to selection" menu item if there is a
		// canvas selection
		m_setFilterAction.setEnabled(m_canvas.hasSelection());

		// enable "Clear filter" menu item if filter is active
		m_clearFilterAction.setEnabled(isCanvasFilterActive());

		// show the load meter refresh speed sub-menu only
		// if the load meters are enabled
		m_loadMetersRefreshSubSubmenu.setVisible(getLoadMetersEnabled());

		// Enable pinning menu item when there is a current debug session
		m_pinToDbgSessionAction.setEnabled(m_sessionState != null);

		// We should not change the enablement of the debug view
		// actions, as they are automatically enabled/disabled
		// by the platform.
	}

	/** Updates actions specific to context menu. */
	protected void updateContextMenuActions(Point location) {
	}

	/** Cleans up actions. */
	protected void disposeActions() {
		if (!m_actionsInitialized) {
			return;
		}

		if (m_resumeAction != null) {
			m_resumeAction.dispose();
			m_resumeAction = null;
		}

		if (m_suspendAction != null) {
			m_suspendAction.dispose();
			m_suspendAction = null;
		}

		if (m_terminateAction != null) {
			m_terminateAction.dispose();
			m_terminateAction = null;
		}

		if (m_stepReturnAction != null) {
			m_stepReturnAction.dispose();
			m_stepReturnAction = null;
		}

		if (m_stepOverAction != null) {
			m_stepOverAction.dispose();
			m_stepOverAction = null;
		}

		if (m_stepIntoAction != null) {
			m_stepIntoAction.dispose();
			m_stepIntoAction = null;
		}

		if (m_dropToFrameAction != null) {
			m_dropToFrameAction.dispose();
			m_dropToFrameAction = null;
		}

		if (m_selectAllAction != null) {
			m_selectAllAction.dispose();
			m_selectAllAction = null;
		}

		if (m_refreshAction != null) {
			m_refreshAction.dispose();
			m_refreshAction = null;
		}

		if (m_loadMetersSubMenu != null) {
			m_loadMetersSubMenu.dispose();
			m_loadMetersSubMenu = null;
		}

		if (m_loadMetersRefreshSubSubmenu != null) {
			m_loadMetersRefreshSubSubmenu.dispose();
			m_loadMetersRefreshSubSubmenu = null;
		}

		if (m_enableLoadMetersAction != null) {
			m_enableLoadMetersAction.dispose();
			m_enableLoadMetersAction = null;
		}

		if (m_setLoadMeterPeriodActions != null) {
			for (SetLoadMeterPeriodAction act : m_setLoadMeterPeriodActions) {
				act.dispose();
			}
			m_setLoadMeterPeriodActions.clear();
			m_setLoadMeterPeriodActions = null;
		}

		if (m_setFilterAction != null) {
			m_setFilterAction.dispose();
			m_setFilterAction = null;
		}

		if (m_clearFilterAction != null) {
			m_clearFilterAction.dispose();
			m_clearFilterAction = null;
		}

		if (m_pinToDbgSessionAction != null) {
			m_pinToDbgSessionAction.dispose();
			m_pinToDbgSessionAction = null;
		}

		if (m_showDebugToolbarAction != null) {
			m_showDebugToolbarAction.dispose();
			m_showDebugToolbarAction = null;
		}

		m_actionsInitialized = false;
	}

	// --- menu/toolbar management ---

	/** Invoked when visualizer is selected, to populate the toolbar. */
	@Override
	public void populateToolBar(IToolBarManager toolBarManager) {
		// initialize menu/toolbar actions, if needed
		createActions();

		// display debug buttons only if MV is not pinned
		// note: if in the future we want to display the debug buttons even
		// when pinned, all that needs to be done it to remove this check.
		if (!m_pinToDbgSessionAction.isChecked()) {
			// only show the debug actions in toolbar, if configured to do so
			if (m_showDebugToolbarAction.isChecked()) {
				toolBarManager.add(m_resumeAction);
				toolBarManager.add(m_suspendAction);
				toolBarManager.add(m_terminateAction);

				toolBarManager.add(m_separatorAction);

				toolBarManager.add(m_stepReturnAction);
				toolBarManager.add(m_stepOverAction);
				toolBarManager.add(m_stepIntoAction);
				toolBarManager.add(m_dropToFrameAction);
			}
		}
		toolBarManager.add(m_pinToDbgSessionAction);

		updateActions();
	}

	/** Invoked when visualizer is selected, to populate the toolbar's menu. */
	@Override
	public void populateMenu(IMenuManager menuManager) {
		// initialize menu/toolbar actions, if needed
		createActions();

		menuManager.add(m_showDebugToolbarAction);

		// TODO: Anything we want to hide on the toolbar menu?
		updateActions();
	}

	/** Invoked when visualizer view's context menu is invoked, to populate it. */
	@Override
	public void populateContextMenu(IMenuManager menuManager) {
		// initialize menu/toolbar actions, if needed
		createActions();

		menuManager.add(m_resumeAction);
		menuManager.add(m_suspendAction);
		menuManager.add(m_terminateAction);

		menuManager.add(m_separatorAction);

		menuManager.add(m_stepReturnAction);
		menuManager.add(m_stepOverAction);
		menuManager.add(m_stepIntoAction);
		menuManager.add(m_dropToFrameAction);

		menuManager.add(m_separatorAction);

		menuManager.add(m_selectAllAction);
		menuManager.add(m_refreshAction);

		menuManager.add(m_separatorAction);

		// add load meters sub-menus and actions
		m_loadMetersSubMenu.removeAll();
		m_loadMetersRefreshSubSubmenu.removeAll();

		menuManager.add(m_loadMetersSubMenu);

		m_loadMetersSubMenu.add(m_enableLoadMetersAction);
		m_loadMetersSubMenu.add(m_loadMetersRefreshSubSubmenu);

		for (SetLoadMeterPeriodAction act : m_setLoadMeterPeriodActions) {
			m_loadMetersRefreshSubSubmenu.add(act);
		}

		// add filtering options
		menuManager.add(m_separatorAction);
		menuManager.add(m_setFilterAction);
		menuManager.add(m_clearFilterAction);

		updateActions();
		Point location = m_viewer.getContextMenuLocation();
		updateContextMenuActions(location);
	}

	// --- visualizer selection management ---

	/** Invoked when visualizer has been selected. */
	@Override
	public void visualizerSelected() {
		updateActions();
	}

	/** Invoked when another visualizer has been selected, hiding this one. */
	@Override
	public void visualizerDeselected() {
	}

	// --- workbench selection management ---

	/**
	 * Tests whether if the IVisualizer can display the selection
	 * (or something reachable from it).
	 */
	@Override
	public int handlesSelection(ISelection selection) {
		// By default, we don't support anything.
		int result = 0;

		Object sel = SelectionUtils.getSelectedObject(selection);
		if (sel instanceof GdbLaunch || sel instanceof GDBProcess || sel instanceof IDMVMContext) {
			result = 1;
		} else {
			result = 0;
		}

		// While we're here, see if we need to attach debug view listener
		updateDebugViewListener();

		return result;
	}

	/**
	 * Adds listener to debug view's viewer, so we can detect
	 * Debug View updates (which it doesn't bother to properly
	 * communicate to the rest of the world, sigh).
	 */
	protected void updateDebugViewListener() {
		attachDebugViewerListener();
	}

	/** Attaches debug viewer listener. */
	protected void attachDebugViewerListener() {
		// NOTE: debug viewer might not exist yet, so we
		// attach the listener at the first opportunity to do so.
		if (m_debugViewer == null) {
			m_debugViewer = DebugViewUtils.getDebugViewer();
			if (m_debugViewer != null) {
				m_modelChangedListener = (delta, proxy) -> GUIUtils.exec(() -> updateDebugContext());
				m_debugViewSelectionChangedListener = event -> GUIUtils
						.exec(() -> updateCanvasSelectionFromDebugView());
				m_debugViewer.addModelChangedListener(m_modelChangedListener);
				m_debugViewer.addSelectionChangedListener(m_debugViewSelectionChangedListener);
			}
		}
	}

	/** Removes debug viewer listener. */
	protected void removeDebugViewerListener() {
		if (m_modelChangedListener != null && m_debugViewSelectionChangedListener != null) {
			if (m_debugViewer != null) {
				m_debugViewer.removeModelChangedListener(m_modelChangedListener);
				m_debugViewer.removeSelectionChangedListener(m_debugViewSelectionChangedListener);
				m_debugViewer = null;
				m_modelChangedListener = null;
				m_debugViewSelectionChangedListener = null;
			}
		}
	}

	private void removeEventListener() {
		if (m_sessionState != null) {
			m_sessionState.removeServiceEventListener(fEventListener);
		}
	}

	/**
	 * Invoked by VisualizerViewer when workbench selection changes.
	 */
	@Override
	public void workbenchSelectionChanged(ISelection selection) {
		// See if we need to update our debug info from
		// the workbench selection. This will be done asynchronously.
		boolean changed = updateDebugContext();

		if (changed) {
			update();
		} else {
			// Even if debug info doesn't change, we still want to
			// check whether the canvas selection needs to change
			// to reflect the current workbench selection.
			updateCanvasSelection();
		}

		// Also check whether we need to attach debug view listener.
		updateDebugViewListener();
	}

	/** Refreshes visualizer content from model. */
	public void refresh() {
		m_canvas.requestRecache();
		m_canvas.requestUpdate();
	}

	/** Updates the UI elements such as the toolbar and context menu */
	public void raiseVisualizerChangedEvent() {
		// FIXME: replace hack below by raising a new VisualizerChanged
		// event, listened-to by VisualizerViewer, that causes it to raise
		// its own VISUALIZER_CHANGED event. See bug 442584 for details

		// for now do a non-change to the selection to trigger a call to
		// VisualizerView#updateUI()
		setSelection(getSelection());
	}

	// --- ISelectionChangedListener implementation ---

	/**
	 * Invoked when visualizer control's selection changes.
	 * Sets control selection as its own selection,
	 * and raises selection changed event for any listeners.
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);

		// Force Debug View's selection to reflect visualizer selection,
		// since debug view doesn't update itself from the workbench selection.
		// NOTE: This can be overridden by the model selection policy, if there is one.
		ISelection debugViewSelection = visualizerToDebugViewSelection(getSelection());
		DebugViewUtils.setDebugViewSelection(debugViewSelection);

		// update actions to reflect change of selection
		updateActions();
	}

	// --- Selection conversion methods ---

	/** Gets debug view selection from visualizer selection. */
	protected ISelection visualizerToDebugViewSelection(ISelection visualizerSelection) {
		MulticoreVisualizerSelectionFinder selectionFinder = new MulticoreVisualizerSelectionFinder();
		ISelection workbenchSelection = selectionFinder.findSelection(visualizerSelection);
		return workbenchSelection;
	}

	/** Gets visualizer selection from debug view selection. */
	protected ISelection workbenchToVisualizerSelection(ISelection workbenchSelection) {
		ISelection visualizerSelection = null;

		List<Object> items = SelectionUtils.getSelectedObjects(workbenchSelection);

		if (m_canvas != null) {
			// Use the current canvas model to match Debug View items
			// with corresponding threads, if any.
			VisualizerModel model = m_canvas.getModel();
			if (model != null) {

				Set<Object> selected = new HashSet<>();

				for (Object item : items) {

					// Currently, we ignore selections other than DSF context objects.
					// TODO: any other cases where we could map selections to canvas?
					if (item instanceof IDMVMContext) {
						IDMContext context = ((IDMVMContext) item).getDMContext();

						IMIProcessDMContext processContext = DMContexts.getAncestorOfType(context,
								IMIProcessDMContext.class);
						int pid = Integer.parseInt(processContext.getProcId());

						IMIExecutionDMContext execContext = DMContexts.getAncestorOfType(context,
								IMIExecutionDMContext.class);

						int tid = 0;
						if (execContext != null) {
							try {
								tid = Integer.parseInt(execContext.getThreadId());
							} catch (NumberFormatException e) {
								// continue tid=0
								assert false : THE_THREAD_ID_DOES_NOT_CONVERT_TO_AN_INTEGER + execContext.getThreadId();
							}
						}

						if (tid == 0) { // process
							List<VisualizerThread> threads = model.getThreadsForProcess(pid);
							if (threads != null) {
								selected.addAll(threads);
							}
						} else { // thread
							VisualizerThread thread = model.getThread(tid);
							// here "tid" is the "GDB thread id", which is not
							// unique across sessions, so make sure the thread
							// belongs to the correct process, before selecting it
							if (thread != null && thread.getPID() == pid) {
								selected.add(thread);
							}
						}
					}
				}
				visualizerSelection = SelectionUtils.toSelection(selected);
			}
		}

		return visualizerSelection;
	}

	// --- IPinnable implementation ---

	/**
	 * Pins the multicore visualizer to the current debug session, preventing
	 * it from switching to a different session.
	 */
	@Override
	public void pin() {
		// No current session - do nothing
		if (m_sessionState == null)
			return;

		m_currentPinedSessionId = m_sessionState.getSessionID();

		m_showDebugToolbarAction.setEnabled(false);
	}

	/**
	 * Unpins the visualizer.
	 */
	@Override
	public void unpin() {
		m_currentPinedSessionId = null;
		// force visualizer to re-evaluate its current session and
		// display the correct one, if needed
		workbenchSelectionChanged(null);

		m_showDebugToolbarAction.setEnabled(true);
	}

	/** Returns whether the MV is currently pinned to a session */
	@Override
	public boolean isPinned() {
		return m_currentPinedSessionId != null;
	}

	// --- DSF Context Management ---

	/** Updates debug context being displayed by canvas.
	 *  Returns true if canvas context actually changes, false if not.
	 */
	public boolean updateDebugContext() {
		// is the visualizer pinned? Then inhibit context change
		if (isPinned())
			return false;

		String sessionId = null;
		IAdaptable debugContext = DebugUITools.getDebugContext();
		if (debugContext instanceof IDMVMContext) {
			sessionId = ((IDMVMContext) debugContext).getDMContext().getSessionId();
		} else if (debugContext instanceof GdbLaunch) {
			GdbLaunch gdbLaunch = (GdbLaunch) debugContext;
			if (gdbLaunch.isTerminated() == false) {
				sessionId = gdbLaunch.getSession().getId();
			}
		} else if (debugContext instanceof GDBProcess) {
			ILaunch launch = ((GDBProcess) debugContext).getLaunch();
			if (launch.isTerminated() == false && launch instanceof GdbLaunch) {
				sessionId = ((GdbLaunch) launch).getSession().getId();
			}
		}

		return setDebugSession(sessionId);
	}

	/** Sets debug context being displayed by canvas.
	 *  Returns true if canvas context actually changes, false if not.
	 */
	public boolean setDebugSession(String sessionId) {
		boolean changed = false;

		if (m_sessionState != null && !m_sessionState.getSessionID().equals(sessionId)) {
			// stop timer that updates the load meters
			disposeLoadMeterTimer();

			m_sessionState.removeServiceEventListener(fEventListener);
			m_sessionState.dispose();
			m_sessionState = null;
			changed = true;
		}

		if (m_sessionState == null && sessionId != null) {
			m_sessionState = new DSFSessionState(sessionId);
			m_sessionState.addServiceEventListener(fEventListener);
			// start timer that updates the load meters
			initializeLoadMeterTimer();
			changed = true;
		}

		return changed;
	}

	// --- Update methods ---

	/** Updates visualizer canvas state. */
	public void update() {
		// Create new VisualizerModel and hand it to canvas,
		// TODO: cache the VisualizerModel somehow and update it,
		// rather than creating it from scratch each time.
		if (m_sessionState == null) {
			// no state to display, we can immediately clear the canvas
			setCanvasModel(null);
			return;
		}
		// clear CPU/core cache
		m_cpuCoreContextsCache.clear();

		fDataModel = new VisualizerModel(m_sessionState.getSessionID());
		getVisualizerModel(fDataModel);
	}

	/** Sets canvas model. (Also updates canvas selection.) */
	protected void setCanvasModel(VisualizerModel model) {
		final VisualizerModel model_f = model;
		GUIUtils.exec(() -> {
			if (m_canvas != null) {
				m_canvas.setModel(model_f);
				// Update the canvas's selection from the current workbench selection.
				updateCanvasSelectionInternal();
			}
		});
	}

	/** Updates canvas selection from current workbench selection. */
	protected void updateCanvasSelection() {
		GUIUtils.exec(() -> updateCanvasSelectionInternal());
	}

	/** Updates canvas selection from current workbench selection.
	 *  Note: this method assumes it is called on the UI thread. */
	protected void updateCanvasSelectionInternal() {
		updateCanvasSelectionInternal(SelectionUtils.getWorkbenchSelection());
	}

	/** Updates canvas selection from current debug view selection.
	 *  Note: this method assumes it is called on the UI thread. */
	protected void updateCanvasSelectionFromDebugView() {
		updateCanvasSelectionInternal(DebugViewUtils.getDebugViewSelection());
	}

	/** Updates canvas selection from current workbench selection.
	 *  Note: this method assumes it is called on the UI thread. */
	protected void updateCanvasSelectionInternal(ISelection selection) {
		ISelection canvasSelection = workbenchToVisualizerSelection(selection);

		// canvas does not raise a selection changed event in this case
		// to avoid circular selection update events
		if (canvasSelection != null)
			m_canvas.setSelection(canvasSelection, false);
	}

	/** Selects all thread(s) displayed in the canvas. */
	public void selectAll() {
		m_canvas.selectAll();
	}

	// --- Visualizer model update methods ---

	/**
	 * Starts visualizer model request.
	 */
	protected void getVisualizerModel(final VisualizerModel model) {
		m_sessionState.execute(new DsfRunnable() {
			@Override
			public void run() {
				// get model asynchronously starting at the top of the hierarchy
				getCPUs(model, new ImmediateRequestMonitor() {
					@Override
					protected void handleCompleted() {
						model.setLoadMetersEnabled(getLoadMetersEnabled());
						updateLoads(model);
						model.sort();
						setCanvasModel(model);
					}
				});
			}
		});
	}

	@ConfinedToDsfExecutor("getSession().getExecutor()")
	protected void getCPUs(final VisualizerModel model, final RequestMonitor rm) {
		fTargetData.getCPUs(m_sessionState, new ImmediateDataRequestMonitor<ICPUDMContext[]>() {
			@Override
			protected void handleCompleted() {
				ICPUDMContext[] cpuContexts = isSuccess() ? getData() : null;
				getCores(cpuContexts, model, rm);
			}
		});
	}

	@ConfinedToDsfExecutor("getSession().getExecutor()")
	protected void getCores(ICPUDMContext[] cpuContexts, final VisualizerModel model, final RequestMonitor rm) {
		if (cpuContexts == null || cpuContexts.length == 0) {
			// Whoops, no CPU data.
			// We'll fake a CPU and use it to contain any cores we find.

			model.addCPU(new VisualizerCPU(0));

			// Collect core data.
			fTargetData.getCores(m_sessionState, new ImmediateDataRequestMonitor<ICoreDMContext[]>() {
				@Override
				protected void handleCompleted() {
					// Get Cores
					ICoreDMContext[] coreContexts = isSuccess() ? getData() : null;

					ICPUDMContext cpu = null;
					if (coreContexts != null && coreContexts.length > 0) {
						// TODO: This keeps the functionality to the same level before change: 459114,
						// although it's noted that this does not cover the possibility to have multiple CPU's
						// within the list of resolved cores
						cpu = DMContexts.getAncestorOfType(coreContexts[0], ICPUDMContext.class);
					}

					// Continue
					getThreads(cpu, coreContexts, model, rm);
				}
			});
		} else {
			// save CPU contexts
			m_cpuCoreContextsCache.addAll(Arrays.asList(cpuContexts));

			final CountingRequestMonitor crm = new ImmediateCountingRequestMonitor(rm);
			crm.setDoneCount(cpuContexts.length);

			for (final ICPUDMContext cpuContext : cpuContexts) {
				int cpuID = Integer.parseInt(cpuContext.getId());
				model.addCPU(new VisualizerCPU(cpuID));

				// Collect core data.
				fTargetData.getCores(m_sessionState, cpuContext, new ImmediateDataRequestMonitor<ICoreDMContext[]>() {
					@Override
					protected void handleCompleted() {
						ICoreDMContext[] coreContexts = isSuccess() ? getData() : null;
						getThreads(cpuContext, coreContexts, model, crm);
					}
				});
			}
		}
	}

	@ConfinedToDsfExecutor("getSession().getExecutor()")
	protected void getThreads(final ICPUDMContext cpuContext, ICoreDMContext[] coreContexts,
			final VisualizerModel model, RequestMonitor rm) {
		if (coreContexts == null || coreContexts.length == 0) {
			// no cores for this cpu context
			// That's fine.
			rm.done();
		} else {
			// save core contexts
			m_cpuCoreContextsCache.addAll(Arrays.asList(coreContexts));

			int cpuID = Integer.parseInt(cpuContext.getId());
			VisualizerCPU cpu = model.getCPU(cpuID);

			final CountingRequestMonitor crm = new ImmediateCountingRequestMonitor(rm);
			crm.setDoneCount(coreContexts.length);

			for (final ICoreDMContext coreContext : coreContexts) {
				int coreID = Integer.parseInt(coreContext.getId());
				cpu.addCore(new VisualizerCore(cpu, coreID));

				// Collect thread data
				fTargetData.getThreads(m_sessionState, cpuContext, coreContext,
						new ImmediateDataRequestMonitor<IDMContext[]>() {
							@Override
							protected void handleCompleted() {
								IDMContext[] threadContexts = isSuccess() ? getData() : null;
								getThreadData(cpuContext, coreContext, threadContexts, model, crm);
							}
						});
			}
		}
	}

	@ConfinedToDsfExecutor("getSession().getExecutor()")
	protected void getThreadData(final ICPUDMContext cpuContext, final ICoreDMContext coreContext,
			IDMContext[] threadContexts, final VisualizerModel model, RequestMonitor rm) {
		if (threadContexts == null || threadContexts.length == 0) {
			// no threads for this core
			// That's fine.
			rm.done();
		} else {
			final CountingRequestMonitor crm = new ImmediateCountingRequestMonitor(rm);
			crm.setDoneCount(threadContexts.length);

			for (IDMContext threadContext : threadContexts) {
				final IMIExecutionDMContext execContext = DMContexts.getAncestorOfType(threadContext,
						IMIExecutionDMContext.class);
				// Don't add the thread to the model just yet, let's wait until we have its data and execution state.
				// Collect thread data
				fTargetData.getThreadData(m_sessionState, cpuContext, coreContext, execContext,
						new ImmediateDataRequestMonitor<IThreadDMData>() {
							@Override
							protected void handleCompleted() {
								IThreadDMData threadData = isSuccess() ? getData() : null;
								getThreadExecutionState(cpuContext, coreContext, execContext, threadData, model, crm);
							}
						});
			}
		}
	}

	/** Invoked when getThreads() request completes. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	protected void getThreadExecutionState(final ICPUDMContext cpuContext, final ICoreDMContext coreContext,
			final IMIExecutionDMContext execContext, final IThreadDMData threadData, final VisualizerModel model,
			final RequestMonitor rm) {
		// Get the execution state
		fTargetData.getThreadExecutionState(m_sessionState, cpuContext, coreContext, execContext, threadData,
				new ImmediateDataRequestMonitor<VisualizerExecutionState>() {
					@Override
					protected void handleCompleted() {
						final VisualizerExecutionState state = isSuccess() ? getData() : null;
						if (state != null && !(state.equals(VisualizerExecutionState.RUNNING))) {
							// Get the frame data
							fTargetData.getTopFrameData(m_sessionState, execContext,
									new ImmediateDataRequestMonitor<IFrameDMData>() {
										@Override
										protected void handleCompleted() {
											IFrameDMData frameData = isSuccess() ? getData() : null;
											getThreadExecutionStateDone(cpuContext, coreContext, execContext,
													threadData, frameData, state, model, rm);
										}
									});
						} else {
							// frame data is not valid
							getThreadExecutionStateDone(cpuContext, coreContext, execContext, threadData, null, state,
									model, rm);
						}
					}
				});
	}

	/** Invoked when getThreadExecutionState() request completes. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	protected void getThreadExecutionStateDone(ICPUDMContext cpuContext, ICoreDMContext coreContext,
			IMIExecutionDMContext execContext, IThreadDMData threadData, IFrameDMData frame,
			VisualizerExecutionState state, VisualizerModel model, RequestMonitor rm) {
		int cpuID = Integer.parseInt(cpuContext.getId());
		VisualizerCPU cpu = model.getCPU(cpuID);
		int coreID = Integer.parseInt(coreContext.getId());
		VisualizerCore core = cpu.getCore(coreID);

		if (state == null) {
			// Unable to obtain execution state.  Assume running
			state = VisualizerExecutionState.RUNNING;
		}

		IMIProcessDMContext processContext = DMContexts.getAncestorOfType(execContext, IMIProcessDMContext.class);
		int pid = Integer.parseInt(processContext.getProcId());
		int tid;
		try {
			tid = Integer.parseInt(execContext.getThreadId());
		} catch (NumberFormatException e) {
			rm.setStatus(new Status(IStatus.ERROR, MulticoreVisualizerUIPlugin.PLUGIN_ID, IStatus.ERROR,
					"Unxepected thread id format:" + execContext.getThreadId(), e)); //$NON-NLS-1$
			rm.done();
			assert false : THE_THREAD_ID_DOES_NOT_CONVERT_TO_AN_INTEGER + execContext.getThreadId();
			return;
		}

		String osTIDValue = threadData.getId();

		// If we can't get the real Linux OS tid, fallback to using the gdb thread id
		int osTid = (osTIDValue == null) ? tid : Integer.parseInt(osTIDValue);

		// add thread if not already there - there is a potential race condition where a
		// thread can be added twice to the model: once at model creation and once more
		// through the listener.   Checking at both places to prevent this.
		VisualizerThread t = model.getThread(tid);
		if (t == null) {
			model.addThread(new VisualizerThread(core, pid, osTid, tid, state, frame));
		}
		// if the thread is already in the model, update it's parameters.
		else {
			t.setCore(core);
			t.setTID(osTid);
			t.setState(state);
			t.setLocationInfo(frame);
		}

		rm.done();
	}

	/** Updates the loads for all cpus and cores */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	protected void updateLoads(final VisualizerModel model) {
		if (m_cpuCoreContextsCache.isEmpty()) {
			// not ready to get load info yet
			return;
		}
		// if meters not enabled, do not query backend
		if (!getLoadMetersEnabled()) {
			return;
		}

		final CountingRequestMonitor crm = new ImmediateCountingRequestMonitor() {
			@Override
			protected void handleSuccess() {
				// canvas may have been disposed since the transaction has started
				if (m_canvas != null) {
					m_canvas.refreshLoadMeters();
					m_canvas.requestUpdate();
				}
				if (m_updateLoadMeterTimer != null) {
					// re-start timer
					m_updateLoadMeterTimer.start();
				}
			}
		};
		crm.setDoneCount(m_cpuCoreContextsCache.size());

		// ask load for each CPU and core
		for (final IDMContext context : m_cpuCoreContextsCache) {
			fTargetData.getLoad(m_sessionState, context, new ImmediateDataRequestMonitor<ILoadInfo>() {
				@Override
				protected void handleCompleted() {
					ILoadInfo loadInfo = isSuccess() ? getData() : null;
					getLoadDone(context, loadInfo, model, crm);
				}
			});
		}
	}

	/** Invoked when a getLoad() request completes. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	protected void getLoadDone(IDMContext context, ILoadInfo load, VisualizerModel model, RequestMonitor rm) {
		Integer l = null;

		if (load != null) {
			l = Integer.valueOf(load.getLoad());
		}

		// CPU context? Update the correct CPU in the model
		if (context instanceof ICPUDMContext) {
			ICPUDMContext cpuContext = (ICPUDMContext) context;
			VisualizerCPU cpu = model.getCPU(Integer.parseInt(cpuContext.getId()));
			cpu.setLoadInfo(new VisualizerLoadInfo(l));
		}
		// Core context? Update the correct core in the model
		else if (context instanceof ICoreDMContext) {
			ICoreDMContext coreContext = (ICoreDMContext) context;
			VisualizerCore core = model.getCore(Integer.parseInt(coreContext.getId()));
			core.setLoadInfo(new VisualizerLoadInfo(l));
		}

		rm.done();
	}

	private Timer getLoadTimer(final DSFSessionState sessionState, final int timeout) {
		Timer t = new Timer(timeout) {
			@Override
			public void run() {
				if (sessionState != null) {
					DsfSession session = DsfSession.getSession(sessionState.getSessionID());
					if (session != null) {
						DsfExecutor executor = session.getExecutor();
						if (executor != null) {
							executor.execute(() -> updateLoads(fDataModel));
						}
					}
				}
			}
		};

		return t;
	}
}
