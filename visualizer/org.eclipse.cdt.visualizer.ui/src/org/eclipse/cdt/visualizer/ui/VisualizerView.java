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

// Package declaration
package org.eclipse.cdt.visualizer.ui;

// Java classes
import java.util.List;

// SWT/JFace classes
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.jface.action.IToolBarManager;

// Eclipse/CDT classes
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.IActionBars;

// Custom classes
import org.eclipse.cdt.visualizer.ui.events.IVisualizerViewerListener;
import org.eclipse.cdt.visualizer.ui.events.VisualizerViewerEvent;
import org.eclipse.cdt.visualizer.ui.util.SelectionProviderAdapter;
import org.eclipse.cdt.visualizer.ui.util.SelectionUtils;
import org.eclipse.cdt.visualizer.ui.util.WorkbenchSelectionAdapter;


// ----------------------------------------------------------------------------
// VisualizerView
// ----------------------------------------------------------------------------

/**
 * CDT Visualizer View class.
 * 
 * This is the default implementation of the Visualizer View.
 * It can also serve as a base class for custom visualizer views.
 * 
 * The Visualizer View is a simple container with a toolbar,
 * which presents an instance of an IVisualizerViewer,
 * and mediates passing of selections to and from the viewer.
 * 
 * The intent of the VisualizerView class is to encapsulate the
 * standard Eclipse workbench view support, so the viewer does
 * not have to worry about such things.
 */
public class VisualizerView
	extends ViewPart
	implements IVisualizerViewerListener,
			   ISelectionChangedListener
{
	// --- members ---
	
	/** Whether view has been initialized */
	protected boolean m_initialized = false;
	
	/** Parent control of view. */
	protected Composite m_parentControl = null;

	/** Contained IVisualizerViewer control. */
	protected IVisualizerViewer m_viewer = null;
	
	/** Selection change event manager */
	protected WorkbenchSelectionAdapter m_workbenchSelectionAdapter = null;

    /** Context menu manager. */
    protected MenuManager m_contextMenuManager = null;
    
    /** Last context menu display location. */
    protected Point m_contextMenuLocation = null;

	
	// --- constructors/destructors ---

	/** Constructor */
	public VisualizerView() {
		super();
	}

	/** Dispose method */
	public void dispose() {
		super.dispose();
		setViewer(null);
		disposeSelectionHandling();
	}


	// --- accessors ---
	
	/** Returns whether view has been initialized. */
	public boolean isInitialized()
	{
		return m_initialized;
	}

	/** Gets contained viewer control. */
	public IVisualizerViewer getViewer()
	{
		return m_viewer;
	}
	
	/** Sets contained viewer control. */
	public void setViewer(IVisualizerViewer viewer)
	{
		if (m_viewer != null) {
			m_viewer.removeSelectionChangedListener(this);
			m_viewer.removeVisualizerViewerListener(this);
		}
		
		m_viewer = viewer;
		
		if (m_viewer != null)
		{
			m_viewer.addVisualizerViewerListener(this);
			m_viewer.addSelectionChangedListener(this);
			updateUI();
		}
	}
	
	
	// --- ViewPart implementation ---

	// IMPORTANT: a view may be loaded before the plugins, etc.
	// that its content depends on, since Eclipse loads saved workbench "memento" state,
	// including current perspective and view layout, before loading dependent plugins,
	// and lazily loads plugins based on classes that get touched.
	//
	// Hence, a view class should invoke setInitialized(true) at the end
	// of its createPartControl() method, and any methods that touch/repaint/update
	// the view's controls, etc. should call isInitialized() to be sure
	// these controls have been created.
	
	/** Invoked when UI controls need to be created */
	public void createPartControl(Composite parent) {
		m_parentControl = parent;
		
		// Reminder: Don't muck with the toolbar or toolbar menu here.
		// (I.e. don't try to clean them out or set initial items.)
		// VisualizerViewer's selection handling code
		// allows the selected visualizer to set their content, and
		// any fiddling you do here will only interfere with that.

		// set up context menu support
		initializeContextMenu();
		
		// set up selection handling
		initializeSelectionHandling();
		
		// initialize viewer control
		initializeViewer();
		
		m_initialized = true;
	}
	
	/** Invoked when view should take the focus.
	 *  Note: this can be invoked before the view is fully initialized
	 *  (for example, when loading views from workspace memento information),
	 *  in which case it should silently do nothing.
	 */
	public void setFocus() {
		if (m_viewer != null) m_viewer.setFocus();
	}

	
	// --- initialization support ---
	
	/**
	 * Creates and returns VisualizerViewer control.
	 * Intended to be overridden by derived types.
	 */
	protected IVisualizerViewer createViewer(Composite parent)
	{
		return (m_viewer != null) ? m_viewer : new VisualizerViewer(this, parent);
	}

	/**
	 * Invoked by createPartControl() method when view instance is created.
	 * Intended to be overridden by derived classes.
	 */
	protected void initializeViewer() {
		IVisualizerViewer viewer = createViewer(m_parentControl);
		setViewer(viewer);
	}

	
	// --- tab name management ---
	
	/** Sets displayed tab name and description for this view. */
	public void setTabName(String displayName) 
	{
		setPartName(displayName);
	}

	/** Sets displayed tab name and description for this view. */
	public void setTabDescription(String description) 
	{
		setTitleToolTip(description);
	}

	
	// --- selection handling ---

	/** Initializes selection handling for this view. */
	protected void initializeSelectionHandling() {
		// create workbench selection change adapter,
		// to hook us into the workbench selection event mechanism
		m_workbenchSelectionAdapter = new WorkbenchSelectionAdapter(this);
		m_workbenchSelectionAdapter.addSelectionChangedListener(this);
	}

	/** Disposes selection handling for this view. */
	protected void disposeSelectionHandling() {
		if (m_workbenchSelectionAdapter != null) {
			m_workbenchSelectionAdapter.dispose();
			m_workbenchSelectionAdapter = null;
		}
	}

	/** Gets current workbench selection. */
	public ISelection getWorkbenchSelection() {
		return m_workbenchSelectionAdapter.getSelection();
	}

	/** Sets current workbench selection, and raises selection changed event. */
	public void setWorkbenchSelection(ISelection selection) {
		m_workbenchSelectionAdapter.setSelection(this, selection);
	}

	/** Sets current workbench selection, and raises selection changed event. */
	public void setWorkbenchSelection(List<?> selection) {
		ISelection iselection = SelectionUtils.toSelection(selection);
		m_workbenchSelectionAdapter.setSelection(this, iselection);
	}

	// --- ISelectionChangedListener implementation ---

	/** Invoked by WorkbenchSelectionAdapter when selection changes,
	 *  and by viewer when visualizer selection changes.
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		Object source = event.getSource();
		if (source instanceof SelectionProviderAdapter) {
			source = ((SelectionProviderAdapter) source).getActualSource();
		}
		// if the source is the current VisualizerViewer,
		// it's a user selection in the viewer, so we want to pass
		// the selection change on to the workbench
		if (source == m_viewer) {
			// tell viewer about workbench selection
			ISelection selection = event.getSelection();
			setWorkbenchSelection(selection);
			
			// update toolbar/menu to reflect changed selection
			updateUI();
		}
		// if the source is this view, it's an echo of a workbench
		// event we sent out, so ignore it.
		else if (source == this) {
			// Do nothing.
		}
		// else this is a selection change from some other view
		// in the workbench, which we should pass down to the viewer
		else {
			ISelection selection = event.getSelection();
			workbenchSelectionChanged(selection);
			
			// update toolbar/menu to reflect changed selection
			updateUI();
		}
	}

	/**
	 * Invoked from selectionChanged() when workbench's selection changes,
	 * but only if the selection change did not come from this view.
	 */
	public void workbenchSelectionChanged(ISelection selection) {
		if (m_viewer != null) {
			m_viewer.workbenchSelectionChanged(selection);
		}
	}
	
	
	// --- IVisulizerViewerListener implementation ---

	/** Invoked when visualizer in view has changed. */
	public void visualizerEvent(IVisualizerViewer source, VisualizerViewerEvent event) {
		switch (event.getType()) {
		case VisualizerViewerEvent.VISUALIZER_CHANGED:
			updateUI();
			break;
		case VisualizerViewerEvent.VISUALIZER_CONTEXT_MENU:
			showContextMenu(event.x, event.y);
		}
	}
	
	
	// --- update methods ---
	
	/** Updates tab name, toolbar, etc. from viewer. */
	public void updateUI() {

		// Update tab name/tooltip
		// TODO: internationalize these default strings
		String name        = "Visualizer View";
		String description = "Displays visualizations of launches.";
		if (m_viewer != null) {
			name = m_viewer.getVisualizerDisplayName();
			description = m_viewer.getVisualizerDescription();
			
		}
		setTabName(name);
		setTabDescription(description);

		// Update toolbar & toolbar menu
		if (m_viewer != null) {
			IActionBars actionBars = getViewSite().getActionBars();
			
			// Allow presentation to set the displayed toolbar content, if any
			IToolBarManager toolBarManager = actionBars.getToolBarManager();
			toolBarManager.removeAll();
			m_viewer.populateToolBar(toolBarManager);
			toolBarManager.update(true);
			
			// Allow presentation to set the toolbar's menu content, if any
			IMenuManager menuManager = actionBars.getMenuManager();
			menuManager.removeAll();
			m_viewer.populateMenu(menuManager);
			menuManager.update(true);
			
			// Note: when context menu is invoked,
			// the poplateContextMenu() method is called by the view,
			// which in turn delegates to the current visualizer
			// to populate the context menu.
			
			// Propagate the changes
			actionBars.updateActionBars();
		}
	}
	
	
	// --- context menu support ---
	
	/** Sets up context menu support. */
	protected void initializeContextMenu() {
		m_contextMenuManager = new MenuManager(); 
		m_contextMenuManager.addMenuListener(new IMenuListener2() {
			public void menuAboutToShow(IMenuManager m) {
				VisualizerView.this.contextMenuShow(m);
			}
			public void menuAboutToHide(IMenuManager m) {
				VisualizerView.this.contextMenuHide(m);
			}
		});

		// We associate the view's context menu with the parent control.
		// Viewer has the option of calling showContextMenu()
		// to display the view's context menu.
        Menu menu= m_contextMenuManager.createContextMenu(m_parentControl);
        m_parentControl.setMenu(menu);
	}
	
	/** Invoked when context menu is about to be shown. */
	protected void contextMenuShow(IMenuManager m)
	{
		m.removeAll();
		m_viewer.populateContextMenu(m);
		m.update();
	}
	
	/** Invoked when context menu is about to be hidden. */
	protected void contextMenuHide(IMenuManager m)
	{
	}
	
	/** Shows view's context menu at specified position. */
	public void showContextMenu(int x, int y)
	{
		Menu menu = m_parentControl.getMenu();
		if (menu != null) {
			menu.setLocation(x, y);
			
			// capture context menu location in relative coordinates
			m_contextMenuLocation = m_parentControl.toControl(x,y);
			
			// Note: showing menu implicitly invokes populateContextMenu()
			// to populate context menu items.
			menu.setVisible(true);
			
			// Make sure we have the focus now
			// so we'll still have it when the menu goes away,
			// and user doesn't have to click twice.
			setFocus();
		}
	}
	
	/** Gets context menu location. */
	public Point getContextMenuLocation() {
		// Just asking the menu for its own location doesn't work,
		// so we have to capture it above and return it here.
		return m_contextMenuLocation;
	}
}
