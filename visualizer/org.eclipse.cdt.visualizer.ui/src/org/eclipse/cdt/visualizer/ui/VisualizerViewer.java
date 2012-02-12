/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.visualizer.core.Extension;
import org.eclipse.cdt.visualizer.ui.events.IVisualizerViewerListener;
import org.eclipse.cdt.visualizer.ui.events.VisualizerViewerEvent;
import org.eclipse.cdt.visualizer.ui.plugin.CDTVisualizerUIPlugin;
import org.eclipse.cdt.visualizer.ui.util.ListenerList;
import org.eclipse.cdt.visualizer.ui.util.SelectionManager;
import org.eclipse.cdt.visualizer.ui.util.SelectionUtils;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.PageBook;


// ----------------------------------------------------------------------------
// VisualizerViewer
// ----------------------------------------------------------------------------

/**
 * CDT Visualizer Viewer class.
 * 
 * This is the default implementation of IVisualizerViewer.
 * It can also serve as a base class for custom visualizer viewers.
 * 
 * The Visualizer Viewer is a simple container for multiple
 * IVisualizers, where the currently selected IVisualizer
 * determines which IVisualizer control is displayed in the viewer.
 * 
 * The base implementation simply displays a single IVisualizer at a time. 
 * One can programmatically switch selections, but there are no user
 * controls for doing so. (The intent is that derived types can add
 * various kinds of switching controls, like a combo box, etc.)
 */
public class VisualizerViewer extends PageBook
	implements IVisualizerViewer, MenuDetectListener,
			   ISelectionProvider, ISelectionChangedListener
{
	// --- constants ---
	
	/** Extension point name for list of IVisualizer types. */
    public static final String VISUALIZER_EXTENSION_POINT_NAME = "visualizer"; //$NON-NLS-1$
	
    
	// --- members ---
    
    /** Containing view. */
    protected VisualizerView m_view = null;
    
    /** Parent control. */
    protected Composite m_parent = null;
    
	/** List of registered visualizer types. */
    protected Map<String, IVisualizer> m_visualizers = null;
	
	/** Currently selected visualizer. */
	protected IVisualizer m_currentVisualizer = null;
	
	/** Event listeners. */
	protected ListenerList m_listeners = null;
	
	/** Viewer selection manager. */
	protected SelectionManager m_selectionManager = null;
    
	
	// --- constructors/destructors ---
	
	/** Constructor. */
	public VisualizerViewer(VisualizerView view, Composite parent) {
		super(parent, SWT.NONE);
		initVisualizerViewer(view, parent);
	}
	
	/** Dispose method. */
	public void dispose() {
		cleanupVisualizerViewer();
		super.dispose();
	}

	/** Overridden to permit subclassing of SWT component */
	protected void checkSubclass() {
		// Do nothing.
		// (Superclass implementation throws "Subclassing not allowed" exception.)
	}

	
	// --- init methods ---
	
	/** Initializes control */
	protected void initVisualizerViewer(VisualizerView view, Composite parent) {
		m_view = view;
		m_parent = parent;
		
		// Event listener support
		m_listeners = new ListenerList(this, "VisualizerViewer event listeners")
		{
			public void raise(Object listener, Object event) {
				if (listener instanceof IVisualizerViewerListener &&
					event instanceof VisualizerViewerEvent)
				{
					IVisualizerViewerListener typedListener = (IVisualizerViewerListener) listener;
					VisualizerViewerEvent typedEvent        = (VisualizerViewerEvent) event;
					typedListener.visualizerEvent(VisualizerViewer.this, typedEvent);
				}
			}
		};

		// Selection change listener support
		m_selectionManager = new SelectionManager(this, "Visualizer Viewer selection manager");
		
		// Set default colors for empty viewer.
		Display display = getDisplay();
		setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		setForeground(display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));

		// Listen to paint event to draw "No visualizers" warning if needed.
		this.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				VisualizerViewer.this.paint(e.gc);
			}
		});

		// Load any visualizers defined through extension point.
		loadVisualizers();
	}
	
	/** Cleans up control */
	protected void cleanupVisualizerViewer() {
		// perform any cleanup here
		disposeVisualizers();		
	    if (m_selectionManager != null) {
	    	m_selectionManager.dispose();
	    	m_selectionManager = null;
	    }
	}
	
	
	// --- accessors ---
	
	/** Gets containing view. */
	public VisualizerView getView() {
		return m_view;
	}
	
	/** Returns non-localized unique name for selected visualizer. */
	public String getVisualizerName()
	{
		String result = "visualizer";
		if (m_currentVisualizer != null) result = m_currentVisualizer.getName();
		return result;
	}

	/** Returns localized name to display for selected visualizer. */
	public String getVisualizerDisplayName()
	{
		String result = "Visualizer";
		if (m_currentVisualizer != null) result = m_currentVisualizer.getDisplayName();
		return result;
	}
	
	/** Returns localized tooltip text to display for selected visualizer. */
	public String getVisualizerDescription()
	{
		String result = "Visualizer";
		if (m_currentVisualizer != null) result = m_currentVisualizer.getDescription();
		return result;
	}

	
	// --- control management ---
	
	/** Gets viewer control. */
	public Control getControl() {
		return this;
	}
	
	
	// --- focus handling ---
	
	/**
	 * Invoked by VisualizerView when currently selected presentation,
	 * if any, should take the focus.
	 */
	public boolean setFocus()
	{
		boolean result = false;
		if (m_currentVisualizer != null) {
			// Tell current visualizer's control to take the focus.
			m_currentVisualizer.getControl().setFocus();
		}
		else {
			// Otherwise, let viewer take the focus.
			result = super.setFocus();
		}
		return result;
	}

	
	// --- viewer events ---
	
	/** Adds listener for viewer events. */
	public void addVisualizerViewerListener(IVisualizerViewerListener listener)
	{
		m_listeners.addListener(listener);
	}
	
	/** Removes listener for viewer events. */
	public void removeVisualizerViewerListener(IVisualizerViewerListener listener)
	{
		m_listeners.removeListener(listener);
	}

	/** Raises change event for all listeners. */
	public void raiseVisualizerChangedEvent()
	{
		VisualizerViewerEvent event = 
			new VisualizerViewerEvent(this, VisualizerViewerEvent.VISUALIZER_CHANGED);
		m_listeners.raise(event);
	}

	/** Raises context menu event for all listeners. */
	public void raiseContextMenuEvent(int x, int y)
	{
		VisualizerViewerEvent event = 
			new VisualizerViewerEvent(this, VisualizerViewerEvent.VISUALIZER_CONTEXT_MENU, x, y);
		m_listeners.raise(event);
	}

	
	// --- visualizer management ---

	/** Loads initial set of visualizers and constructs viewer controls. */
	protected void loadVisualizers()
	{
		// TODO: add error checking, logging for errors in extension declarations
		// TODO: do we need to worry about this being called more than once?
		
    	m_visualizers = new Hashtable<String, IVisualizer>();
    	
    	List<Extension> visualizers = Extension.getExtensions(
        	CDTVisualizerUIPlugin.FEATURE_ID, VISUALIZER_EXTENSION_POINT_NAME);

    	if (visualizers != null) {
	    	for(Extension e : visualizers) {
	    		String id = e.getAttribute("id");
	    		IVisualizer visualizerInstance = e.getClassAttribute();
	    		if (id != null && visualizerInstance != null) {
	    			// Add visualizer's control to viewer's "pagebook" of controls.
	    			visualizerInstance.setViewer(this);
	    			visualizerInstance.initializeVisualizer();
	    			visualizerInstance.createControl(this);
	    			m_visualizers.put(id, visualizerInstance);
	    		}
	    	}
    	}
    	
		// select initial visualization
    	selectDefaultVisualizer();
	}
	
	/** Cleans up visualizers. */
	protected void disposeVisualizers()
	{
		for (String id : m_visualizers.keySet()) {
			IVisualizer v = m_visualizers.get(id);
			Control c = v.getControl();
			c.dispose();
			v.disposeVisualizer();
		}
		m_visualizers.clear();
	}

	
	/** Selects default visualizer.
	 *  Default implementation displays visualizer that can handle
	 *  the current selection.
	 */
	public void selectDefaultVisualizer()
	{
		// fake a workbench selection changed event
		updateVisualizerFromWorkbenchSelection();
	}
	
	/** Selects specified visualizer, makes its control visible. */
	public void selectVisualizer(IVisualizer visualizer)
	{
		if (visualizer == null) return;
		if (visualizer == m_currentVisualizer) return;

		if (m_currentVisualizer != null) {
			// let the visualizer know it's being hidden
			m_currentVisualizer.visualizerDeselected();
			
			// stop listening for context menu events
			m_currentVisualizer.getControl().removeMenuDetectListener(this);
			
			// stop listening for selection changed events
			m_currentVisualizer.removeSelectionChangedListener(this);
		}
		
		m_currentVisualizer = visualizer;
		
		if (m_currentVisualizer != null) {
			// We've added visualizer's control in loadVisualizers(),
			// so all we need to do here is select it.
			showPage(visualizer.getControl());

			// start listening for context menu events
			m_currentVisualizer.getControl().addMenuDetectListener(this);

			// start listening for selection changed events
			m_currentVisualizer.addSelectionChangedListener(this);

			// raise visualizer changed event, so view knows
			// it should update tab name, toolbar, etc.
			raiseVisualizerChangedEvent();
			
			// make sure workbench knows about current visualizer selection
			updateWorkbenchFromVisualizerSelection();
			
			// no need to update visualizer from workbench selection,
			// we already do that whenever the workbench selection changes

			// let the visualizer know it's been shown
			m_currentVisualizer.visualizerDeselected();
		}
	}
	
	/** Gets current visualizer. */
	public IVisualizer getVisualizer()
	{
		return m_currentVisualizer;
	}

	
	// --- menu/toolbar management ---

	/** Invoked when visualizer is selected, to populate the toolbar. */
	public void populateToolBar(IToolBarManager toolBarManager)
	{
		if (m_currentVisualizer != null)
			m_currentVisualizer.populateToolBar(toolBarManager);
	}

	/** Invoked when visualizer is selected, to populate the toolbar's menu. */
	public void populateMenu(IMenuManager menuManager)
	{
		if (m_currentVisualizer != null)
			m_currentVisualizer.populateMenu(menuManager);
	}

	
	// --- context menu handling ---
	
	/** Invoked when context menu gesture happens on current
	 *  visualizer control.
	 */
	public void menuDetected(MenuDetectEvent e) {
		// raise event to allow view to show context menu
		raiseContextMenuEvent(e.x, e.y);
	}

	/** Invoked when context menu is about to be shown. */
	public void populateContextMenu(IMenuManager m)
	{
		if (m_currentVisualizer != null) {
			m_currentVisualizer.populateContextMenu(m);
		}
	}
	
	/** Gets context menu location. */
	public Point getContextMenuLocation() {
		return m_view.getContextMenuLocation();
	}
	
	
	// --- paint methods ---
	
	/**
	 * Invoked when viewer needs to be repainted.
     * May be overridden by derived classes.
	 * Default implementation displays "No visualizers defined." message
	 */
	public void paint(GC gc)
	{
		gc.fillRectangle(getClientArea());
		if (m_visualizers == null || m_visualizers.size() == 0) {
			String noVisualizersMessage = 
				CDTVisualizerUIPlugin.getString("VisualizerViewer.no.visualizers.defined");
			gc.drawString("(" + noVisualizersMessage + ")", 10, 10);
		}
	}
	
	
	// --- ISelectionProvider implementation ---
	
	// Delegate to selection manager.
	
	/** Adds external listener for selection change events. */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		m_selectionManager.addSelectionChangedListener(listener);
	}

	/** Removes external listener for selection change events. */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		m_selectionManager.removeSelectionChangedListener(listener);
	}
	
	/** Raises selection changed event. */
	public void raiseSelectionChangedEvent() {
		m_selectionManager.raiseSelectionChangedEvent();
	}
	
	/** Gets current externally-visible selection. */
	public ISelection getSelection()
	{
		return m_selectionManager.getSelection();
	}
	
	/** Sets externally-visible selection. */
	public void setSelection(ISelection selection)
	{
		m_selectionManager.setSelection(selection);
	}

	
	// --- workbench selection handling ---

	/**
	 * Updates visualizer from current workbench selection.
	 * NOTE: normally VisulizerView keeps the viewer in synch
	 * with the selection, so this should not need to be called
	 * except in special cases.
	 */
	public void updateVisualizerFromWorkbenchSelection() {
		ISelection selection = SelectionUtils.getWorkbenchSelection();
		workbenchSelectionChanged(selection);
	}

    /**
     * Invoked by VisualizerView when workbench selection changes,
     * and change's source is not this view.
     * 
     * Selects visualizer (if any) that knows how to display current
     * selection. Also invokes workbenchSelectionChanged() on visualizer
     * so it can update itself accordingly.
     */
	public void workbenchSelectionChanged(ISelection selection)
	{
		// See if we need to change visualizers to handle selection type.
		IVisualizer handles = null;
		int weight = 0;
		
		// First, see if the current visualizer can handle the new selection.
		// (This gives it automatic precedence if there's a tie.)
		if (m_currentVisualizer != null) {
			int w = m_currentVisualizer.handlesSelection(selection);
			if (w > weight) {
				handles = m_currentVisualizer;
				weight = w;
			}
		}

		// Next, check the list of other visualizers, to see if any
		// of them is more specific than the current one.
		for (IVisualizer v : m_visualizers.values()) {
			if (v == m_currentVisualizer) continue; // already checked
			int w = v.handlesSelection(selection);
			if (w > weight) {
				handles = v;
				weight = w;
			}
		}
		
		// If NOBODY claims ownership, and we don't have a visualizer yet,
		// then pick somebody and let them take a whack at it.
		if (handles == null && m_visualizers.size() > 0) {
			handles = m_visualizers.values().iterator().next();
		}
		
		// If we need to change visualizers, select the new one.
		// Note: this also reports the new visualizer's selection to the workbench
		if (handles != null && handles != m_currentVisualizer) {
			selectVisualizer(handles);
		}

		// Notify current visualizer that workbench selection has changed.
		// (This means a visualizer is only notified of workbench selection changes
		// if the selection is something it has reported that it can display.)
		if (m_currentVisualizer != null) {
			m_currentVisualizer.workbenchSelectionChanged(selection);
		}
	}

	
	// --- visualizer selection handling ---
	
	/** Updates workbench selection from current visualizer selection.
	 *  Note: normally the viewer keeps the selection in synch,
	 *  so you should not need to call this method except in special cases.
	 */
	public void updateWorkbenchFromVisualizerSelection() {
		// fake a selection changed event
		m_selectionManager.raiseSelectionChangedEvent();
	}

	/** Invoked when visualizer's selection changes. */
	public void selectionChanged(SelectionChangedEvent event) {
		// Publish changed visualizer selection to any listeners.
		setSelection(event.getSelection());
	}
}
