/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCPU;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCore;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvas;
import org.eclipse.cdt.visualizer.ui.plugin.CDTVisualizerUIPlugin;
import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.cdt.visualizer.ui.util.MouseMonitor;
import org.eclipse.cdt.visualizer.ui.util.SelectionManager;
import org.eclipse.cdt.visualizer.ui.util.SelectionUtils;
import org.eclipse.cdt.visualizer.ui.util.Timer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;


/**
 * MulticoreVisualizer's display canvas.
 */
public class MulticoreVisualizerCanvas extends GraphicCanvas
	implements ISelectionProvider
{
	// --- constants ---

	/** Canvas update interval in milliseconds. */
	protected static final int CANVAS_UPDATE_INTERVAL = 100;
	
	/** Spacing to allow between threads, when many are displayed on same tile. */
	protected static final int THREAD_SPACING = 8;
	

	// --- members ---
	
	/** Update timer */
	protected Timer m_updateTimer = null;
	
	/** Whether we need to recache graphic objects. */
	protected boolean m_recache = true;
	
	/**
	 * Whether we need to recache objects that depend on target state.
	 */
	protected boolean m_recacheState = true;
	
	/**
	 * Whether view size has changed, requiring us to recalculate object sizes.
	 */
	protected boolean m_recacheSizes = true;
	
	/** Whether we need to repaint the canvas */
	protected boolean m_update = true;


	// --- UI members ---
	
	/** Text font */
	protected Font m_textFont = null;
	
	/** Externally visible selection manager. */
	protected SelectionManager m_selectionManager;

    /** Mouse-drag marquee graphic element */
    protected MulticoreVisualizerMarquee m_marquee = null;
    
    /** Last mouse down/up point, for shift-click selections. */
    protected Point m_lastSelectionClick = new Point(0,0);
    
	/** Mouse click/drag monitor */
    protected MouseMonitor m_mouseMonitor = null;
	

	// --- cached repaint state ---
	
	/** Current visualizer model we're displaying. */
	protected VisualizerModel m_model = null;
	
	/** Number of CPUs to display. */
	protected int m_cpu_count = 15;
	
	/** Number of Cores per CPU to display. */
	protected int m_cores_per_cpu = 3;
	
	/** List of CPUs we're displaying. */
	protected ArrayList<MulticoreVisualizerCPU> m_cpus = null;
	/** Mapping from model to view objects. */
	protected Hashtable<VisualizerCPU, MulticoreVisualizerCPU> m_cpuMap = null;

	/** List of CPU cores we're displaying. */
	protected ArrayList<MulticoreVisualizerCore> m_cores = null;
	/** Mapping from model to view objects. */
	protected Hashtable<VisualizerCore, MulticoreVisualizerCore> m_coreMap = null;
	
	/** Graphic objects representing threads */
	protected ArrayList<MulticoreVisualizerThread> m_threads = null;
	/** Mapping from model to view objects. */
	protected Hashtable<VisualizerThread, MulticoreVisualizerThread> m_threadMap = null;
	
	/** Selected PIDs. */
	protected HashSet<Integer> m_selectedPIDs = null;
	

	// --- constructors/destructors ---
	
	/** Constructor. */
	public MulticoreVisualizerCanvas(Composite parent) {
		super(parent);
		initMulticoreVisualizerCanvas(parent);
	}
	
	/** Dispose method. */
	@Override
	public void dispose() {
		cleanupMulticoreVisualizerCanvas();
		super.dispose();
	}

	
	// --- init methods ---
	
	/** Initializes control */
	protected void initMulticoreVisualizerCanvas(Composite parent) {
		// perform any initialization here
		
		// text font
		m_textFont = CDTVisualizerUIPlugin.getResources().getFont("Luxi Sans", 8); //$NON-NLS-1$
		setFont(m_textFont);
		
		// initialize cached state storage
		m_cpus        = new ArrayList<MulticoreVisualizerCPU>();
		m_cpuMap      = new Hashtable<VisualizerCPU, MulticoreVisualizerCPU>();
		
		m_cores       = new ArrayList<MulticoreVisualizerCore>();
		m_coreMap     = new Hashtable<VisualizerCore, MulticoreVisualizerCore>();
		
		m_threads     = new ArrayList<MulticoreVisualizerThread>();
		m_threadMap   = new Hashtable<VisualizerThread, MulticoreVisualizerThread>();
		
		m_selectedPIDs = new HashSet<Integer>();
		
		// mouse-drag monitor
		m_mouseMonitor = new MouseMonitor(this) {
			/** Invoked for a selection click at the specified point. */
			@Override
			public void select(int x, int y, int keys) {
				MulticoreVisualizerCanvas.this.select(x, y, keys);
			}

			/** Invoked for a double click at the specified point. */
			@Override
			public void mouseDoubleClick(int button, int x, int y, int keys) {
				MulticoreVisualizerCanvas.this.select(x, y, keys);
			}

			/** Invoked for a menu mouse down at the specified point. */
			@Override
			public void mouseDown(int button, int x, int y, int keys) {
				if (button == RIGHT_BUTTON) {
					if (! hasSelection()) {
						// If there isn't a selection currently, try to
						// select item(s) under the mouse before popping up the context menu.
						MulticoreVisualizerCanvas.this.select(x, y, keys);
					}
				}
			}

			/** Invoked when mouse is dragged. */
			@Override
			public void drag(int button, int x, int y, int keys, int dragState) {
				if (button == LEFT_BUTTON) {
					MulticoreVisualizerCanvas.this.drag(x, y, keys, dragState);
				}
			}
		};
		
		// selection marquee
		m_marquee = new MulticoreVisualizerMarquee();

		// selection manager
		m_selectionManager = new SelectionManager(this, "MulticoreVisualizerCanvas selection manager"); //$NON-NLS-1$
		
		// add update timer
		m_updateTimer = new Timer(CANVAS_UPDATE_INTERVAL) {
			@Override
			public void run() {
				update();
			}
		};
		m_updateTimer.setRepeating(false); // one-shot timer
		m_updateTimer.start();
	}
	
	/** Cleans up control */
	protected void cleanupMulticoreVisualizerCanvas() {
		if (m_updateTimer != null) {
			m_updateTimer.dispose();
			m_updateTimer = null;
		}
	    if (m_marquee != null) {
	    	m_marquee.dispose();
	    	m_marquee = null;
	    }
        if (m_mouseMonitor != null) {
            m_mouseMonitor.dispose();
            m_mouseMonitor = null;
	    }
	    if (m_selectionManager != null) {
	    	m_selectionManager.dispose();
	    	m_selectionManager = null;
	    }
        if (m_cpus != null) {
        	m_cpus.clear();
        	m_cpus = null;
        }
        if (m_cpuMap != null) {
        	m_cpuMap.clear();
        	m_cpuMap = null;
        }
        if (m_cores != null) {
        	m_cores.clear();
        	m_cores = null;
        }
        if (m_coreMap != null) {
        	m_coreMap.clear();
        	m_coreMap = null;
        }
        if (m_threads != null) {
        	m_threads.clear();
        	m_threads = null;
        }
        if (m_threadMap != null) {
        	m_threadMap.clear();
        	m_threadMap = null;
        }
        if (m_selectedPIDs != null) {
        	m_selectedPIDs.clear();
        	m_selectedPIDs = null;
        }
	}
	

	// --- accessors ---
	
	/** Gets currently displayed model. */
	public VisualizerModel getModel()
	{
		return m_model;
	}
	
	/** Sets model to display, and requests canvas update. */
	public void setModel(VisualizerModel model)
	{
		m_model = model;
		requestRecache();
		requestUpdate();
	}
	
	
	
	// --- resize methods ---
	
	/** Invoked when control is resized. */
	@Override
	public void resized(Rectangle bounds) {
		requestRecache(false, true);
		// note: resize itself will trigger an update, so we don't have to request one
	}

	
	// --- update methods ---
	
	/**
	 * Requests an update on next timer tick.
	 * NOTE: use this method instead of normal update(),
	 * multiple update requests on same tick are batched.
	 */
	public void requestUpdate() {
		GUIUtils.exec(new Runnable() { @Override public void run() {
			m_updateTimer.start();
		}});
	}
	

	// --- paint methods ---
	
	/** Requests that next paint call should recache state and/or size information */
	// synchronized so we don't change recache flags while doing a recache
	public synchronized void requestRecache() {
		requestRecache(true, true);
	}

	/** Requests that next paint call should recache state and/or size information */
	// synchronized so we don't change recache flags while doing a recache
	public synchronized void requestRecache(boolean state, boolean sizes) {
		m_recache = true;
		// NOTE: we intentionally OR these flags with any pending request(s)
		m_recacheState |= state;
		m_recacheSizes |= sizes;
	}
	
	/** Fits n square items into a rectangle of the specified size.
	 *  Returns largest edge of one of the square items that allows
	 *  them all to pack neatly in rows/columns in the specified area. */
	public int fitSquareItems(int nitems, int width, int height) {
		int max_edge = 0;
		if (width > height) {
			for (int items_per_row = nitems; items_per_row > 0; --items_per_row) {
				int rows = (int) Math.ceil(1.0 * nitems / items_per_row);
				int w = width / items_per_row;
				int h = height / rows;
				int edge = (w < h) ? w : h;
				if (edge * rows > height || edge * items_per_row > width) continue;
				if (edge > max_edge) max_edge = edge;
			}
		}
		else {
			for (int items_per_col = nitems; items_per_col > 0; --items_per_col) {
				int cols = (int) Math.ceil(1.0 * nitems / items_per_col);
				int w = width / cols;
				int h = height / items_per_col;
				int edge = (w < h) ? w : h;
				if (edge * cols > width || edge * items_per_col > height) continue;
				if (edge > max_edge) max_edge = edge;
			}
		}
		return max_edge;
	}
	
	/** Recache persistent objects (tiles, etc.) for new monitor */
	// synchronized so we don't change recache flags while doing a recache
	public synchronized void recache() {
		if (! m_recache) return; // nothing to do, free the lock quickly

		if (m_recacheState) {

			// clear all grid view objects
			clear();
			
			// clear cached state
			m_cpus.clear();
			m_cores.clear();
			m_threads.clear();

			// For debugging purposes only, allows us to force a CPU count.
			//int cpu_count = 0;
			//int force_cpu_count = 2;
			
			if (m_model != null) {
				for (VisualizerCPU cpu : m_model.getCPUs()) {
					//if (force_cpu_count >= cpu_count) break;
					//cpu_count++;
					MulticoreVisualizerCPU mcpu = new MulticoreVisualizerCPU(cpu.getID());
					m_cpus.add(mcpu);
					m_cpuMap.put(cpu, mcpu);
					for (VisualizerCore core : cpu.getCores()) {
						MulticoreVisualizerCore mcore = new MulticoreVisualizerCore(mcpu, core.getID());
						m_cores.add(mcore);
						m_coreMap.put(core, mcore);
					}
				}
			}
			/*
			while (cpu_count < force_cpu_count) {
				MulticoreVisualizerCPU mcpu = new MulticoreVisualizerCPU(cpu_count);
				m_cpus.add(mcpu);
				cpu_count++;
			}
			*/
			
			// we've recached state, which implies recacheing sizes too
			m_recacheState = false;
			m_recacheSizes = true;
		}
		
		if (m_recacheSizes) {
			// update cached size information
			
			// General margin/spacing constants.
			int cpu_margin = 8;       // margin around edges of CPU grid
			int cpu_separation = 6;   // spacing between CPUS
			
			int core_margin = 4;      // margin around cores in a CPU 
			int core_separation = 2;  // spacing between cores

			// Get overall area we have for laying out content.
			Rectangle bounds = getClientArea();
			GUIUtils.inset(bounds, cpu_margin);

			// Figure out area to allocate to each CPU box.
			int ncpus  = m_cpus.size();
			int width  = bounds.width  + cpu_separation;
			int height = bounds.height + cpu_separation;
			int cpu_edge = fitSquareItems(ncpus, width, height);
			int cpu_size = cpu_edge - cpu_separation;
			if (cpu_size < 0) cpu_size = 0;
			
			// Calculate area on each CPU for placing cores.
			int ncores  = m_cores.size();
			int cpu_width  = cpu_size - core_margin * 2 + core_separation;
			int cpu_height = cpu_size - core_margin * 2 + core_separation;
			int core_edge  = fitSquareItems(ncores, cpu_width, cpu_height);
			int core_size  = core_edge - core_separation;
			if (core_size < 0) core_size = 0;
			
			int x = bounds.x, y = bounds.y;
			for (MulticoreVisualizerCPU cpu : m_cpus) {
				cpu.setBounds(x, y, cpu_size-1, cpu_size-1);
				
				int left = x + core_margin;
				int cx = left, cy = y + core_margin;
				for (MulticoreVisualizerCore core : cpu.getCores())
				{
					core.setBounds(cx, cy, core_size, core_size);
					
					cx += core_size + core_separation;
					if (cx + core_size > x + cpu_size) {
						cx = left;
						cy += core_size + core_separation;
					}
				}
				
				x += cpu_size + cpu_separation;
				if (x + cpu_size > bounds.x + width) {
					x = bounds.x;
					y += cpu_size + cpu_separation;
				}
			}

			m_recacheSizes = false;
		}
	}
	
	/** Invoked when canvas repaint event is raised.
	 *  Default implementation clears canvas to background color.
	 */
	@Override
	public void paintCanvas(GC gc) {
		// NOTE: We have a little setup to do first,
		// so we delay clearing/redrawing the canvas until needed,
		// to minimize any potential visual flickering.
		
		// recache/resize tiles & shims if needed
		recache();

		// do any "per frame" updating/replacement of graphic objects
		
		// recalculate process/thread graphic objects on the fly
		// TODO: can we cache/pool these and just move them around?
		for (MulticoreVisualizerCore core : m_cores) {
			core.removeAllThreads();
		}
		m_threads.clear();
		m_threadMap.clear();
		
		// update based on current processes/threads
		if (m_model != null) {
			
			// NOTE: we assume that we've already created and sized the graphic
			// objects for cpus/cores in recache() above,
			// so we can use these to determine the size/location of more dynamic elements
			// like processes and threads
			
			for (VisualizerThread thread : m_model.getThreads()) {
				VisualizerCore core = thread.getCore();
				MulticoreVisualizerCore mcore = m_coreMap.get(core);
				if (mcore != null) {
					MulticoreVisualizerThread mthread =
						new MulticoreVisualizerThread(mcore, thread);
					mcore.addThread(mthread);
					m_threads.add(mthread);
					m_threadMap.put(thread, mthread);
				}
			}

			// now set sizes of processes/threads for each tile
			for (MulticoreVisualizerCore core : m_cores) {
				Rectangle bounds = core.getBounds();
				
				// how we lay out threads depends on how many there are
				List<MulticoreVisualizerThread> threads = core.getThreads();
				int threadspotsize = MulticoreVisualizerThread.THREAD_SPOT_SIZE;
				int threadheight = threadspotsize + THREAD_SPACING;
				int count = threads.size();
				int tileheight = bounds.height - 4;
				int tx = bounds.x + 2;
				int ty = bounds.y + 2;
				int dty = (count < 1) ? 0 : tileheight / count;
				if (dty > threadheight) dty = threadheight;
				if (count > 0 && dty * count <= tileheight) {
					ty = bounds.y + 2 + (tileheight - (dty * count)) / 2;
					if (ty < bounds.y + 2) ty = bounds.y + 2;
				}
				else if (count > 0) {
					dty = tileheight / count;
					if (dty > threadheight) dty = threadheight;
				}
				int t = 0;
				for (MulticoreVisualizerThread threadobj : threads) {
					int y = ty + dty * (t++);
					threadobj.setBounds(tx, y, threadspotsize, threadspotsize);
				}
			}
		}
		
		// restore canvas object highlighting from model object selection
		restoreSelection();

		// FIXME: enable secondary highlight for threads that are
		// part of a selected process.
		m_selectedPIDs.clear();
		for (MulticoreVisualizerThread mthread : m_threads) {
			if (mthread.isSelected()) {
				m_selectedPIDs.add(mthread.getPID());
			}
		}
		for (MulticoreVisualizerThread mthread : m_threads) {
			mthread.setProcessSelected(m_selectedPIDs.contains(mthread.getPID()));
		}
		
		// NOW we can clear the background
		clearCanvas(gc);

		// Make sure color/font resources are properly initialized.
		MulticoreVisualizerUIPlugin.getResources();
		
		// paint cpus
		for (MulticoreVisualizerCPU cpu : m_cpus) {
			cpu.paintContent(gc);
		}

		// paint cores
		for (MulticoreVisualizerCore core : m_cores) {
			core.paintContent(gc);
		}
		
		// paint cpus IDs on top of cores
		for (MulticoreVisualizerCPU cpu : m_cpus) {
			cpu.paintDecorations(gc);
		}

		// paint threads on top of cores
		for (MulticoreVisualizerThread thread : m_threads) {
			thread.paintContent(gc);
		}
		
		// paint drag-selection marquee last, so it's on top.
		m_marquee.paintContent(gc);
	}
	
	
	// --- mouse event handlers ---

	/** Invoked when mouse is dragged. */
	public void drag(int x, int y, int keys, int dragState)
	{
		Rectangle region = m_mouseMonitor.getDragRegion();
		switch (dragState) {
		case MouseMonitor.MOUSE_DRAG_BEGIN:
			m_marquee.setBounds(region);
			m_marquee.setVisible(true);
			update();
			break;
		case MouseMonitor.MOUSE_DRAG:
			m_marquee.setBounds(region);
			update();
			break;
		case MouseMonitor.MOUSE_DRAG_END:
		default:
			m_marquee.setBounds(region);
			m_marquee.setVisible(false);

			boolean addToSelection = MouseMonitor.isShiftDown(keys);
			boolean toggleSelection = MouseMonitor.isControlDown(keys);
			
			selectRegion(m_marquee.getBounds(), addToSelection, toggleSelection);

			// remember last mouse-up point for shift-click selection
			m_lastSelectionClick.x = x;
			m_lastSelectionClick.y = y;
			
			update();
			break;
		}
	}

	/** Invoked for a selection click at the specified point. */
	public void select(int x, int y, int keys)
	{
		boolean addToSelection = MouseMonitor.isShiftDown(keys);
		boolean toggleSelection = MouseMonitor.isControlDown(keys);
		
		selectPoint(x,y, addToSelection, toggleSelection);
	}
	

	// --- selection methods ---

	/**
	 * Selects item(s), if any, in specified region
	 * 
	 * If addToSelection is true, appends item(s) to current selection
	 * without changing selection state of other items.
	 *  
	 * If toggleSelection is true, toggles selection of item(s)
	 * without changing selection state of other items.
	 * 
	 * If both are true, deselects item(s)
	 * without changing selection state of other items.
	 *  
	 * Otherwise, selects item(s) and deselects other items.
	 */
	public void selectRegion(Rectangle region,
							 boolean addToSelection, boolean toggleSelection)
	{
		// currently, we select/deselect threads, not processes or tiles
		if (m_threads != null) {

			boolean changed = false;
			
			for (MulticoreVisualizerThread tobj : m_threads) {
				boolean within = tobj.isWithin(region);

				if (addToSelection && toggleSelection) {
					if (within) {
						tobj.setSelected(false);
						changed = true;
					}
				}
				else if (addToSelection) {
					if (within) {
						tobj.setSelected(true);
						changed = true;
					}
				}
				else if (toggleSelection) {
					if (within) {
						tobj.setSelected(! tobj.isSelected());
						changed = true;
					}
				}
				else {
					tobj.setSelected(within);
					changed = true;
				}
			}

			if (changed)
				selectionChanged();
		}
	}
	
	/**
	 * Selects item(s), if any, at specified point.
	 * 
	 * If addToSelection is true, appends item(s) to current selection
	 * without changing selection state of other items.
	 *  
	 * If toggleSelection is true, toggles selection of item(s)
	 * without changing selection state of other items.
	 * 
	 * If both are true, deselects item(s)
	 * without changing selection state of other items.
	 *  
	 * Otherwise, selects item(s) and deselects other items.
	 */
	public void selectPoint(int x, int y,
							boolean addToSelection, boolean toggleSelection)
	{
		// Currently we only allow selection of threads.
		if (m_threads != null) {

			List<MulticoreVisualizerThread> threads = new ArrayList<MulticoreVisualizerThread>();
			
			// first see if selection click landed on a thread dot.
			for (MulticoreVisualizerThread tobj : m_threads) {
				if (tobj.contains(x,y)) {
					threads.add(tobj);
					break;
				}
			}
			
			// if not, see if it landed on a core; if so, select its threads
			if (threads.isEmpty()) {
				for (MulticoreVisualizerCore tobj : m_cores) {
					if (tobj.contains(x,y)) {
						List<MulticoreVisualizerThread> corethreads = tobj.getThreads();
						threads.addAll(corethreads);
						break;
					}
				}
			}
			
			// in addToSelection case, include any threads in region
			// bracketed by last selection click and current click
			// (with some extra slop added so we pick up threads that
			// overlap the edge of this region)
			if (addToSelection) {
				int spotSize = MulticoreVisualizerThread.THREAD_SPOT_SIZE * 3;
				Rectangle r1 = new Rectangle(m_lastSelectionClick.x - spotSize/2,
										 	 m_lastSelectionClick.y - spotSize/2,
										 	spotSize, spotSize);
				Rectangle r2 = new Rectangle(x - spotSize/2, y - spotSize/2, spotSize, spotSize);
				Rectangle region = r1.union(r2);
				
				for (MulticoreVisualizerThread tobj : m_threads) {
					if (tobj.isWithin(region)) {
						threads.add(tobj);
					}
				}
			}
			
			boolean changed = false;

			for (MulticoreVisualizerThread tobj : m_threads) {
				boolean within = threads.contains(tobj);

				if (addToSelection && toggleSelection) {
					if (within) {
						tobj.setSelected(false);
						changed = true;
					}
				}
				else if (addToSelection) {
					if (within) {
						tobj.setSelected(true);
						changed = true;
					}
				}
				else if (toggleSelection) {
					if (within) {
						tobj.setSelected(! tobj.isSelected());
						changed = true;
					}
				}
				else {
					tobj.setSelected(within);
					changed = true;
				}
			}

			if (changed)
				selectionChanged();
		}
		
		// remember last mouse-up point for shift-click selection
		m_lastSelectionClick.x = x;
		m_lastSelectionClick.y = y;
	}
	
	
	// --- selection management methods ---
		
	/** Selects all items in the canvas. */
	public void selectAll()
	{
		// currently, we select/deselect threads, not processes or tiles
		if (m_threads != null) {

			for (MulticoreVisualizerThread tobj : m_threads) {
				tobj.setSelected(true);
			}
			
			selectionChanged();
		}
	}

	/** Clears selection. */
	public void clearSelection() {
		// currently, we select/deselect threads, not processes or tiles
		if (m_threads != null) {

			for (MulticoreVisualizerThread tobj : m_threads) {
				tobj.setSelected(false);
			}
			
			selectionChanged();
		}
	}
	
	/** Things to do whenever the selection changes. */
	protected void selectionChanged() {
		selectionChanged(true);
	}

	/** Things to do whenever the selection changes. */
	protected void selectionChanged(boolean raiseEvent) {
		// Note: we save selection (and raise event) now,
		// and let canvas "catch up" on its next update tick.
		updateSelection(raiseEvent);
		requestUpdate();
	}

	/** Saves current canvas selection as list of model objects. */
	protected void updateSelection(boolean raiseEvent) {
		// get model objects (if any) corresponding to canvas selection
		HashSet<VisualizerThread> selectedThreads = new HashSet<VisualizerThread>();
		if (m_threads != null) {
			for (MulticoreVisualizerThread tobj : m_threads) {
				if (tobj.isSelected()) {
					selectedThreads.add(tobj.getThread());
				}
			}
		}
		
		// update model object selection
		ISelection selection = SelectionUtils.toSelection(selectedThreads);
		setSelection(selection, raiseEvent);
	}
	
	/** Restores current selection from saved list of model objects. */
	protected void restoreSelection() {
		ISelection selection = getSelection();
		List<Object> selectedThreads = SelectionUtils.getSelectedObjects(selection);
		if (m_threads != null) {
			for (MulticoreVisualizerThread tobj : m_threads) {
				tobj.setSelected(selectedThreads.contains(tobj.getThread()));
			}
		}
	}
	
	
	// --- ISelectionProvider implementation ---
	
	// Delegate to selection manager.
	
	/** Adds external listener for selection change events. */
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		m_selectionManager.addSelectionChangedListener(listener);
	}

	/** Removes external listener for selection change events. */
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		m_selectionManager.removeSelectionChangedListener(listener);
	}
	
	/** Raises selection changed event. */
	public void raiseSelectionChangedEvent() {
		m_selectionManager.raiseSelectionChangedEvent();
	}
	
	/** Returns true if we have a selection. */
	public boolean hasSelection()
	{
		return m_selectionManager.hasSelection();
	}
	
	/** Gets current externally-visible selection. */
	@Override
	public ISelection getSelection()
	{
		return m_selectionManager.getSelection();
	}
	
	/** Sets externally-visible selection. */
	@Override
	public void setSelection(ISelection selection)
	{
		m_selectionManager.setSelection(selection);
	}
	
	/** Sets externally-visible selection. */
	public void setSelection(ISelection selection, boolean raiseEvent)
	{
		m_selectionManager.setSelection(selection, raiseEvent);
	}
	
    /** Sets whether selection events are enabled. */
    public void setSelectionEventsEnabled(boolean enabled) {
        m_selectionManager.setSelectionEventsEnabled(enabled);
    }
}
