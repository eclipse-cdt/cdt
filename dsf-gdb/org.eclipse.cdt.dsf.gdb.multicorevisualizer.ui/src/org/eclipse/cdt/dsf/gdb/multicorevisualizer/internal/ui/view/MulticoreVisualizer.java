/*******************************************************************************
 * Copyright (c) 2012, 2013 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.gdb.launching.GDBProcess;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions.EnableLoadMetersAction;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions.FilterCanvasAction;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions.RefreshAction;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions.SelectAllAction;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions.SetLoadMeterPeriodAction;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCPU;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCore;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerLoadInfo;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DSFDebugModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DSFDebugModelListener;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DSFSessionState;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DebugViewUtils;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.ICPUDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.ICoreDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2.ILoadInfo;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvas;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvasVisualizer;
import org.eclipse.cdt.visualizer.ui.plugin.CDTVisualizerUIPlugin;
import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.cdt.visualizer.ui.util.SelectionUtils;
import org.eclipse.cdt.visualizer.ui.util.Timer;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.internal.ui.commands.actions.DropToFrameCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.ResumeCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.StepIntoCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.StepOverCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.StepReturnCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.SuspendCommandAction;
import org.eclipse.debug.internal.ui.commands.actions.TerminateCommandAction;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
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
public class MulticoreVisualizer extends GraphicCanvasVisualizer
    implements DSFDebugModelListener
{	
	// --- constants ---
	
	/** Eclipse ID for this view */
	public static final String ECLIPSE_ID = "org.eclipse.cdt.dsf.gdb.multicorevisualizer.visualizer"; //$NON-NLS-1$

	
	// --- members ---
	
	/**
	 * The data model drawn by this visualizer.
	 */
	protected VisualizerModel fDataModel;

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
	
	// These two arrays are used to cache the CPU and core
	// contexts, each time the model is recreated.  This way
	// we can avoid asking the backend for the CPU/core
	// geometry each time we want to update the load information.
	/**
	 * @since 1.1
	 */
	protected ICPUDMContext[] m_cpuContextsCache = null;
	/**
	 * @since 1.1
	 */
	protected ICoreDMContext[] m_coreContextsCache = null;
	
	/**
	 * Main switch that determines if we should display the load meters
	 * @since 1.1
	 */
	protected boolean m_loadMetersEnabled = false;
	/**
	 * Timer used to trigger the update of the CPU/core load meters
	 * @since 1.1
	 */
	protected Timer m_updateLoadMeterTimer = null;
	/**
	 * @since 1.1
	 */
	protected int m_loadMeterTimerPeriod = LOAD_METER_TIMER_MEDIUM; // default 1000ms
	// Load meters refresh periods, in ms
	private static final int LOAD_METER_TIMER_MIN = 100;
	private static final int LOAD_METER_TIMER_FAST = 500;
	private static final int LOAD_METER_TIMER_MEDIUM = 1000; 
	private static final int  LOAD_METER_TIMER_SLOW = 5000;
	

	// --- UI members ---

	/** Whether actions have been initialized. */
	boolean m_actionsInitialized = false;
	
	/** Toolbar / menu action */
	Separator m_separatorAction = null;	

	/** Toolbar / menu action */
	ResumeCommandAction m_resumeAction = null;
	
	/** Toolbar / menu action */
	SuspendCommandAction m_suspendAction = null;
	
	/** Toolbar / menu action */
	TerminateCommandAction m_terminateAction = null;
	
	/** Toolbar / menu action */
	StepReturnCommandAction m_stepReturnAction = null;
	
	/** Toolbar / menu action */
	StepOverCommandAction m_stepOverAction = null;
	
	/** Toolbar / menu action */
	StepIntoCommandAction m_stepIntoAction = null;
	
	/** Toolbar / menu action */
	DropToFrameCommandAction m_dropToFrameAction = null;
	
	/** Toolbar / menu action */
	SelectAllAction m_selectAllAction = null;
	
	/** Toolbar / menu action */
	RefreshAction m_refreshAction = null;
	
	/** Sub-menu */
	IMenuManager m_loadMetersSubMenu = null;
	/** Sub-sub menu */
	IMenuManager m_loadMetersRefreshSubSubmenu = null;
	
	/** Menu action */
	EnableLoadMetersAction m_enableLoadMetersAction = null;
	
	/** Menu action */
	List<SetLoadMeterPeriodAction> m_setLoadMeterPeriodActions = null;
	
	/** Menu action */
	FilterCanvasAction m_setFilterAction = null;

	/** Menu action */
	FilterCanvasAction m_clearFilterAction = null;	

	// --- constructors/destructors ---
	
	/** Constructor. */
	public MulticoreVisualizer()
	{
	}
	
	/** Dispose method. */
	@Override
	public void dispose()
	{
		super.dispose();
		removeDebugViewerListener();
		disposeActions();
		disposeLoadMeterTimer();
		removeEventListener();
	}
	
	
	// --- init methods ---
	
	/** Invoked when visualizer is created, to permit any initialization. */
	@Override
	public void initializeVisualizer() {
		fEventListener = new MulticoreVisualizerEventListener(this);
	}
	
	/**
	 * @since 1.1
	 */
	protected void initializeLoadMeterTimer() {
		if (!m_loadMetersEnabled) return;
		m_updateLoadMeterTimer = DSFDebugModel.getLoadTimer(m_sessionState, m_loadMeterTimerPeriod, this);		
		// one-shot timer (re-scheduled upon successful triggering)
		m_updateLoadMeterTimer.setRepeating(false); 
	}
	
	/**
	 * @since 1.1
	 */
	protected void disposeLoadMeterTimer() {
		if(m_updateLoadMeterTimer != null) {
			m_updateLoadMeterTimer.dispose();
			m_updateLoadMeterTimer = null;
		}
	}
	
	/** Invoked when visualizer is disposed, to permit any cleanup. */
	@Override
	public void disposeVisualizer()
	{
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
	 * @since 1.1
	 */
	public void setLoadMeterTimerPeriod(int p) {
		assert (p > LOAD_METER_TIMER_MIN);
		if (m_loadMeterTimerPeriod == p) return; 
		m_loadMeterTimerPeriod = p > LOAD_METER_TIMER_MIN ? p : LOAD_METER_TIMER_MIN;
		disposeLoadMeterTimer();
		initializeLoadMeterTimer();
	}
	
	/**
	 * @since 1.1
	 */
	public void setLoadMetersEnabled(boolean enabled) {
		if (m_loadMetersEnabled == enabled) return;
		m_loadMetersEnabled = enabled;
		// save load meter enablement in model
		fDataModel.setLoadMetersEnabled(m_loadMetersEnabled);
		disposeLoadMeterTimer();
		initializeLoadMeterTimer();
	}
	
	// --- canvas management ---
	
	/** Creates and returns visualizer canvas control. */
	@Override
	public GraphicCanvas createCanvas(Composite parent)
	{
		m_canvas = new MulticoreVisualizerCanvas(parent);
		m_canvas.addSelectionChangedListener(this);
		return m_canvas;
	}
	
	/** Invoked when canvas control should be disposed. */
	@Override
	public void disposeCanvas()
	{
		if (m_canvas != null) {
			m_canvas.removeSelectionChangedListener(this);
			m_canvas.dispose();
			m_canvas = null;
		}
		disposeLoadMeterTimer();
	}
	
	/** Invoked after visualizer control creation, */
	@Override
	protected void initializeCanvas(GraphicCanvas canvas)
	{
		// Any workbench views left open at application shutdown may be instanced
		// before our plugins are fully loaded, so make sure resource manager is initialized.
		// Note: this also associates the resource manager with the Colors class;
		// until this is done, the Colors constants are null.
		CDTVisualizerUIPlugin.getResources();
		
		m_canvas.setBackground(Colors.BLACK);
		m_canvas.setForeground(Colors.GREEN);
	}
	
	/** Returns downcast reference to grid view canvas. */
	public MulticoreVisualizerCanvas getMulticoreVisualizerCanvas()
	{
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
	protected void createActions()
	{
		if (m_actionsInitialized) return; // already done
		
		LaunchView debugView = DebugViewUtils.getDebugView();
		
		m_separatorAction   = new Separator();

		m_resumeAction      = new ResumeCommandAction();
		if (debugView != null) m_resumeAction.init(debugView);
		
		m_suspendAction     = new SuspendCommandAction();
		if (debugView != null) m_suspendAction.init(debugView);
		
		m_terminateAction   = new TerminateCommandAction();
		if (debugView != null) m_terminateAction.init(debugView);

		
		m_stepReturnAction  = new StepReturnCommandAction();
		if (debugView != null) m_stepReturnAction.init(debugView);
		
		m_stepOverAction    = new StepOverCommandAction();
		if (debugView != null) m_stepOverAction.init(debugView);

		m_stepIntoAction    = new StepIntoCommandAction();
		if (debugView != null) m_stepIntoAction.init(debugView);

		m_dropToFrameAction = new DropToFrameCommandAction();
		if (debugView != null) m_dropToFrameAction.init(debugView);
		
		m_selectAllAction = new SelectAllAction();
		m_selectAllAction.init(this);
		
		m_refreshAction = new RefreshAction();
		m_refreshAction.init(this);

		// create load meters sub-menu and associated actions
		m_loadMetersSubMenu = new MenuManager(MulticoreVisualizerUIPlugin.getString(
				"MulticoreVisualizer.actions.LoadMeterSubmenu.text")); //$NON-NLS-1$
		m_loadMetersRefreshSubSubmenu = new MenuManager(MulticoreVisualizerUIPlugin.getString(
				"MulticoreVisualizer.actions.LoadMetersRefreshSubSubmenu.text")); //$NON-NLS-1$
		
		m_enableLoadMetersAction = new EnableLoadMetersAction(m_loadMetersEnabled);
		m_enableLoadMetersAction.init(this);
		// enable the load meter sub-menu 
    	m_enableLoadMetersAction.setEnabled(true);

		m_setLoadMeterPeriodActions = new ArrayList<SetLoadMeterPeriodAction>();
		m_setLoadMeterPeriodActions.add(new SetLoadMeterPeriodAction(
				MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.SetLoadMeterPeriod.fast.text"),  //$NON-NLS-1$
				LOAD_METER_TIMER_FAST));
		
		SetLoadMeterPeriodAction defaultAction = new SetLoadMeterPeriodAction(
				MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.SetLoadMeterPeriod.medium.text"),  //$NON-NLS-1$
				LOAD_METER_TIMER_MEDIUM);
		m_setLoadMeterPeriodActions.add(defaultAction);
		
		m_setLoadMeterPeriodActions.add(new SetLoadMeterPeriodAction(
				MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.SetLoadMeterPeriod.slow.text"),  //$NON-NLS-1$
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
		
		// Note: debug view may not be initialized at startup,
		// so we'll pretend the actions are not yet updated,
		// and reinitialize them later.
		m_actionsInitialized = (debugView != null);
	}
	
	/** Updates actions displayed on menu/toolbars. */
	protected void updateActions()
	{
		if (! m_actionsInitialized) return;

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
    	m_loadMetersRefreshSubSubmenu.setVisible(m_loadMetersEnabled);
		
		// We should not change the enablement of the debug view
		// actions, as they are automatically enabled/disabled
		// by the platform.
	}

	/** Updates actions specific to context menu. */
    protected void updateContextMenuActions(Point location) 
    {
    }

	/** Cleans up actions. */
	protected void disposeActions() {
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
		
		if (m_enableLoadMetersAction != null ) {
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

		m_actionsInitialized = false;
	}

	
	// --- menu/toolbar management ---

	/** Invoked when visualizer is selected, to populate the toolbar. */
	@Override
	public void populateToolBar(IToolBarManager toolBarManager)
	{
		// initialize menu/toolbar actions, if needed
		createActions();

		toolBarManager.add(m_resumeAction);
		toolBarManager.add(m_suspendAction);
		toolBarManager.add(m_terminateAction);
		
		toolBarManager.add(m_separatorAction);
		
		toolBarManager.add(m_stepReturnAction);
		toolBarManager.add(m_stepOverAction);
		toolBarManager.add(m_stepIntoAction);
		toolBarManager.add(m_dropToFrameAction);
		
		updateActions();
	}

	/** Invoked when visualizer is selected, to populate the toolbar's menu. */
	@Override
	public void populateMenu(IMenuManager menuManager)
	{
		// initialize menu/toolbar actions, if needed
		createActions();

		// TODO: Anything we want to hide on the toolbar menu?
		
		updateActions();
	}

	/** Invoked when visualizer view's context menu is invoked, to populate it. */
	@Override
	public void populateContextMenu(IMenuManager menuManager)
	{
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
	};
	
	/** Invoked when another visualizer has been selected, hiding this one. */
	@Override
	public void visualizerDeselected() {
	};

	
	// --- workbench selection management ---
	
    /**
     * Tests whether if the IVisualizer can display the selection
     * (or something reachable from it).
	 */
	@Override
	public int handlesSelection(ISelection selection)
	{
		// By default, we don't support anything.
		int result = 0;
		
		Object sel = SelectionUtils.getSelectedObject(selection);
		if (sel instanceof GdbLaunch ||
			sel instanceof GDBProcess ||
			sel instanceof IDMVMContext)
		{
			result = 1;
		}
		else {
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
	protected void updateDebugViewListener()
	{
		attachDebugViewerListener();
	}
	
	/** Attaches debug viewer listener. */
	protected void attachDebugViewerListener()
	{
		// NOTE: debug viewer might not exist yet, so we
		// attach the listener at the first opportunity to do so.
		if (m_debugViewer == null) {
			m_debugViewer = DebugViewUtils.getDebugViewer();
			if (m_debugViewer != null) {
				m_modelChangedListener =
				new IModelChangedListener() {
					@Override
					public void modelChanged(IModelDelta delta, IModelProxy proxy)
					{
						// Execute a refresh after any pending UI updates.
						GUIUtils.exec( new Runnable() { @Override public void run() {
							// check if we need to update the debug context
							updateDebugContext();
						}});
					}
				};
				m_debugViewer.addModelChangedListener(m_modelChangedListener);
			}
		}
	}

	/** Removes debug viewer listener. */
	protected void removeDebugViewerListener()
	{
		if (m_modelChangedListener != null) {
			if (m_debugViewer != null) {
				m_debugViewer.removeModelChangedListener(m_modelChangedListener);
				m_debugViewer = null;
				m_modelChangedListener = null;
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
	public void workbenchSelectionChanged(ISelection selection)
	{
		// See if we need to update our debug info from
		// the workbench selection. This will be done asynchronously.
		boolean changed = updateDebugContext();

		if (changed) {
			update();
		}
		else {
			// Even if debug info doesn't change, we still want to
			// check whether the canvas selection needs to change
			// to reflect the current workbench selection.
			updateCanvasSelection();
		}

		// Also check whether we need to attach debug view listener.
		updateDebugViewListener();
	}
	
	/** Refreshes visualizer content from model. */
	public void refresh()
	{
		m_canvas.requestRecache();
		m_canvas.requestUpdate();
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
	protected ISelection visualizerToDebugViewSelection(ISelection visualizerSelection)
	{
		MulticoreVisualizerSelectionFinder selectionFinder =
			new MulticoreVisualizerSelectionFinder();
		ISelection workbenchSelection =
			selectionFinder.findSelection(visualizerSelection);
		return workbenchSelection;
	}
	
	/** Gets visualizer selection from debug view selection. */
	protected ISelection workbenchToVisualizerSelection(ISelection workbenchSelection)
	{
		ISelection visualizerSelection = null;
		
		List<Object> items = SelectionUtils.getSelectedObjects(workbenchSelection);
		
		if (m_canvas != null) {
			// Use the current canvas model to match Debug View items
			// with corresponding threads, if any.
			VisualizerModel model = m_canvas.getModel();
			if (model != null) {

				Set<Object> selected = new HashSet<Object>();

				for (Object item : items) {

					// Currently, we ignore selections other than DSF context objects.
					// TODO: any other cases where we could map selections to canvas?
					if (item instanceof IDMVMContext)
					{
						IDMContext context = ((IDMVMContext) item).getDMContext();

						IMIProcessDMContext processContext =
								DMContexts.getAncestorOfType(context, IMIProcessDMContext.class);
						int pid = Integer.parseInt(processContext.getProcId());

						IMIExecutionDMContext execContext =
								DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
						int tid = (execContext == null) ? 0 : execContext.getThreadId();

						if (tid == 0) { // process
							List<VisualizerThread> threads = model.getThreadsForProcess(pid);
							if (threads != null) {
								selected.addAll(threads);
							}
						}
						else { // thread
							VisualizerThread thread = model.getThread(tid);
							if (thread != null) {
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
	

	// --- DSF Context Management ---
	
	/** Updates debug context being displayed by canvas.
	 *  Returns true if canvas context actually changes, false if not.
	 */
	public boolean updateDebugContext()
	{
		String sessionId = null;
		IAdaptable debugContext = DebugUITools.getDebugContext();
		if (debugContext instanceof IDMVMContext) {
			sessionId = ((IDMVMContext)debugContext).getDMContext().getSessionId();
		} else if (debugContext instanceof GdbLaunch) {
			GdbLaunch gdbLaunch = (GdbLaunch)debugContext;
			if (gdbLaunch.isTerminated() == false) {
				sessionId = gdbLaunch.getSession().getId();
			}
		} else if (debugContext instanceof GDBProcess) {
			ILaunch launch = ((GDBProcess)debugContext).getLaunch();
			if (launch.isTerminated() == false &&
					launch instanceof GdbLaunch) {
				sessionId = ((GdbLaunch)launch).getSession().getId();
			}
		}
		
		return setDebugSession(sessionId);
	}

	/** Sets debug context being displayed by canvas.
	 *  Returns true if canvas context actually changes, false if not.
	 */
	public boolean setDebugSession(String sessionId) {
		boolean changed = false;

		if (m_sessionState != null &&
			! m_sessionState.getSessionID().equals(sessionId))
		{
			// stop timer that updates the load meters
			disposeLoadMeterTimer();
			m_cpuContextsCache = null;
			m_coreContextsCache = null;
			
			m_sessionState.removeServiceEventListener(fEventListener);
			m_sessionState.dispose();
			m_sessionState = null;
			changed = true;
		}
		
		if (m_sessionState == null &&
			sessionId != null)
		{
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
		m_sessionState.execute(new DsfRunnable() { @Override public void run() {
			// get model asynchronously, and update canvas
			// in getVisualizerModelDone().
			getVisualizerModel();
		}});
	}
	
	/** Sets canvas model. (Also updates canvas selection.) */
	protected void setCanvasModel(VisualizerModel model) {
		final VisualizerModel model_f = model;
		GUIUtils.exec(new Runnable() { @Override public void run() {
			if(m_canvas != null) {
				m_canvas.setModel(model_f);
				// Update the canvas's selection from the current workbench selection.
				updateCanvasSelectionInternal();
			}
		}});
	}
	
	/** Updates canvas selection from current workbench selection. */
	protected void updateCanvasSelection() {
		GUIUtils.exec(new Runnable() { @Override public void run() {
			// Update the canvas's selection from the current workbench selection.
			updateCanvasSelectionInternal();
		}});
	}
	
	/** Updates canvas selection from current workbench selection.
	 *  Note: this method assumes it is called on the UI thread. */
	protected void updateCanvasSelectionInternal()
	{
		updateCanvasSelectionInternal(SelectionUtils.getWorkbenchSelection());
	}
	
	/** Updates canvas selection from current workbench selection.
	 *  Note: this method assumes it is called on the UI thread. */
	protected void updateCanvasSelectionInternal(ISelection selection)
	{
		ISelection canvasSelection = workbenchToVisualizerSelection(selection);
		
		// canvas does not raise a selection changed event in this case
		// to avoid circular selection update events
		if (canvasSelection != null)
			m_canvas.setSelection(canvasSelection, false);
	}
	
	
	/** Selects all thread(s) displayed in the canvas. */
	public void selectAll()
	{
		m_canvas.selectAll();
	}
	
	// --- Visualizer model update methods ---
	
	/** Starts visualizer model request.
	 *  Calls getVisualizerModelDone() with the constructed model.
	 */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void getVisualizerModel() {
		fDataModel = new VisualizerModel(m_sessionState.getSessionID());
		DSFDebugModel.getCPUs(m_sessionState, this, fDataModel);
	}
	
	/** Invoked when getModel() request completes. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void getVisualizerModelDone(VisualizerModel model) {
		fDataModel.setLoadMetersEnabled(m_loadMetersEnabled);
		updateLoads();
		model.sort();
		setCanvasModel(model);
	}
	
	
	// --- DSFDebugModelListener implementation ---

	/** Invoked when DSFDebugModel.getCPUs() completes. */
	@Override
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void getCPUsDone(ICPUDMContext[] cpuContexts, Object arg)
	{
		// save CPU contexts
		m_cpuContextsCache = cpuContexts;
		VisualizerModel model = (VisualizerModel) arg;
		
		if (cpuContexts == null || cpuContexts.length == 0) {
			// Whoops, no CPU data.
			// We'll fake a CPU and use it to contain any cores we find.
			
			model.addCPU(new VisualizerCPU(0));
			
			// keep track of CPUs left to visit
			model.getTodo().add(1);
			
			// Collect core data.
			DSFDebugModel.getCores(m_sessionState, this, model);
		} else {
			// keep track of CPUs left to visit
			int count = cpuContexts.length;
			model.getTodo().add(count);
			
			for (ICPUDMContext cpuContext : cpuContexts) {
				int cpuID = Integer.parseInt(cpuContext.getId());
				model.addCPU(new VisualizerCPU(cpuID));
				
				// Collect core data.
				DSFDebugModel.getCores(m_sessionState, cpuContext, this, model);
			}
			
		}
	}

	
	/** Invoked when getCores() request completes. */
	@Override
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void getCoresDone(ICPUDMContext cpuContext,
							 ICoreDMContext[] coreContexts,
							 Object arg)
	{
		coreContexts = null;
		// save core contexts
		m_coreContextsCache = coreContexts;
		VisualizerModel model = (VisualizerModel) arg;

		if (coreContexts == null || coreContexts.length == 0) {
			// no cores for this cpu context
			// That's fine.
		} else {
			int cpuID = Integer.parseInt(cpuContext.getId());
			VisualizerCPU cpu = model.getCPU(cpuID);

			// keep track of Cores left to visit
			int count = coreContexts.length;
			model.getTodo().add(count);
			
			for (ICoreDMContext coreContext : coreContexts) {
				int coreID = Integer.parseInt(coreContext.getId());
				cpu.addCore(new VisualizerCore(cpu, coreID));
				
				// Collect thread data
				DSFDebugModel.getThreads(m_sessionState, cpuContext, coreContext, this, model);
			}			
		}
		
		// keep track of CPUs visited
		// note: do this _after_ incrementing for cores
		done(1, model);
	}

	
	/** Invoked when getThreads() request completes. */
	@Override
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void getThreadsDone(ICPUDMContext  cpuContext,
							   ICoreDMContext coreContext,
							   IDMContext[] threadContexts,
							   Object arg)
	{
		VisualizerModel model = (VisualizerModel) arg;
		
		if (threadContexts == null || threadContexts.length == 0) {
			// no threads for this core
			// That's fine.
		} else {
			// keep track of threads left to visit
			int count = threadContexts.length;
			model.getTodo().add(count);

			for (IDMContext threadContext : threadContexts) {
				IMIExecutionDMContext execContext =
					DMContexts.getAncestorOfType(threadContext, IMIExecutionDMContext.class);

				// Don't add the thread to the model just yet, let's wait until we have its data and execution state.
				// Collect thread data
				DSFDebugModel.getThreadData(m_sessionState, cpuContext, coreContext, execContext, this, model);
			}
			
		}
		
		// keep track of cores visited
		// note: do this _after_ incrementing for threads
		done(1, model);
	}
	
	/** Invoked when getThreads() request completes. */
	@Override
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void getThreadDataDone(ICPUDMContext cpuContext,
			                      ICoreDMContext coreContext,
			                      IMIExecutionDMContext execContext,
			                      IThreadDMData threadData,
			                      Object arg)
	{

		// Don't add the thread to the model just yet, let's wait until we have its execution state.
		DSFDebugModel.getThreadExecutionState(m_sessionState, cpuContext, coreContext, execContext, threadData, this, arg);
	}

	
	/** Invoked when getThreadExecutionState() request completes. */
	@Override
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void getThreadExecutionStateDone(ICPUDMContext cpuContext,
			                                ICoreDMContext coreContext,
			                                IMIExecutionDMContext execContext,
			                                IThreadDMData threadData,
			                                VisualizerExecutionState state,
			                                Object arg)
	{
		VisualizerModel model = (VisualizerModel) arg;
		int cpuID  = Integer.parseInt(cpuContext.getId());
		VisualizerCPU  cpu  = model.getCPU(cpuID);
		int coreID = Integer.parseInt(coreContext.getId());
		VisualizerCore core = cpu.getCore(coreID);
		
		if (state == null) {
			// Unable to obtain execution state.  Assume running
			state = VisualizerExecutionState.RUNNING;
		}

		IMIProcessDMContext processContext =
				DMContexts.getAncestorOfType(execContext, IMIProcessDMContext.class);
		int pid = Integer.parseInt(processContext.getProcId());
		int tid = execContext.getThreadId();
		String osTIDValue = threadData.getId();

		// If we can't get the real Linux OS tid, fallback to using the gdb thread id
		int osTid = (osTIDValue == null) ? tid : Integer.parseInt(osTIDValue);

		// add thread if not already there - there is a potential race condition where a 
		// thread can be added twice to the model: once at model creation and once more 
		// through the listener.   Checking at both places to prevent this.
		VisualizerThread t = model.getThread(tid);
		if (t == null) {
			model.addThread(new VisualizerThread(core, pid, osTid, tid, state));
		}
		// if the thread is already in the model, update it's parameters.  
		else {
			t.setCore(core);
			t.setTID(osTid);
			t.setState(state);
		}

		
		// keep track of threads visited
		done(1, model);
	}
	
	
	/**
	 * @since 1.1
	 */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	@Override
	public void updateLoads() {
		if (m_cpuContextsCache == null || m_coreContextsCache == null) {
			// not ready to get load info yet
			return;
		}
		// if meters not enabled, do not query backend
		if (!m_loadMetersEnabled) {
			return;
		}
		
		VisualizerModel model = fDataModel;
		
		// keep track of how many loads we expect
		int count = m_cpuContextsCache.length + m_coreContextsCache.length;
		model.getLoadTodo().dispose();
		model.getLoadTodo().add(count);
		
		// ask load for each CPU
		for (ICPUDMContext cpuCtx : m_cpuContextsCache) {
			DSFDebugModel.getLoad(m_sessionState, cpuCtx, this, model);
		}
		// ask load for each core
		for (ICoreDMContext coreCtx : m_coreContextsCache) {
			DSFDebugModel.getLoad(m_sessionState, coreCtx, this, model);
		}
	}
	
	/**  
	 * Invoked when a getLoad() request completes.
	 * @since 1.1*/
	@Override
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void getLoadDone(IDMContext context, ILoadInfo load, Object arg) 
	{
		VisualizerModel model = (VisualizerModel) arg;
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
		else if(context instanceof ICoreDMContext) {
			ICoreDMContext coreContext = (ICoreDMContext) context;
			VisualizerCore core = model.getCore(Integer.parseInt(coreContext.getId()));
			core.setLoadInfo(new VisualizerLoadInfo(l));
		}
		
		loadDone(1, model);
	}
	
	
	/** Update "done" count for current visualizer model. */
	protected void done(int n, VisualizerModel model) {
		model.getTodo().done(n);
		if (model.getTodo().isDone()) {
			getVisualizerModelDone(model);
		}
	}
	
	/** Update "done" count for current visualizer model. */
	protected void loadDone(int n, VisualizerModel model) {
		model.getLoadTodo().done(n);
		if (model.getLoadTodo().isDone()) {
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
	}
}

