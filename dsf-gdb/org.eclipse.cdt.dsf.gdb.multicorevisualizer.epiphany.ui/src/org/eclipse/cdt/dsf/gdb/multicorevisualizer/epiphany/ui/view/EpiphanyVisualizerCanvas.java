/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.view;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.EpiphanyVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelCPU;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelIO;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelThread;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils.IEpiphanyConstants;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCPU;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view.MulticoreVisualizerCanvas;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view.MulticoreVisualizerGraphicObject;
import org.eclipse.cdt.visualizer.ui.plugin.CDTVisualizerUIPlugin;
import org.eclipse.cdt.visualizer.ui.util.SelectionManager;
import org.eclipse.cdt.visualizer.ui.util.SelectionUtils;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;


@SuppressWarnings("restriction")
public class EpiphanyVisualizerCanvas extends MulticoreVisualizerCanvas implements ISelectionProvider 
{
	// --- constants ---
	
	
	// --- members ---
	
	/** Current Epiphany visualizer model we're displaying.  */
	private EpiphanyModel m_model = null;
	
	/** Constants related to Epiphany chip we're displaying */
	private IEpiphanyConstants m_eConstants = null;
	
	
	// --- UI members ---
	
	// --- cached repaint state ---
	
	/** 1st level container, for the whole Epiphany chip */
	protected EpiphanyVisualizerChip m_epiphanyChip  = null;
	
	/** Map between model cpu and corresponding graphical object */
	protected Hashtable<EpiphanyModelCPU, EpiphanyVisualizerCPU> m_cpuMap;
		
	
	// --- constructors/destructors ---
	
	public EpiphanyVisualizerCanvas(Composite parent, IEpiphanyConstants c) {
		super(parent);
		m_eConstants = c;
		initMulticoreVisualizerCanvas(parent);
		initEpiphanyVisualizer();
	}
	
	
	// --- init methods ---
	
	/** Initializes control */
	protected void initEpiphanyVisualizer() {
		// set text font
		m_textFont = CDTVisualizerUIPlugin.getResources().getFont("Luxi Sans", 9); //$NON-NLS-1$
		this.setFont(m_textFont);
						
		// selection manager
		m_selectionManager = new SelectionManager(this, "Epiphany Visualizer selection manager"); //$NON-NLS-1$
		m_cpuMap = new Hashtable<EpiphanyModelCPU, EpiphanyVisualizerCPU>(); 
	}
	
	/** Cleans up control */
	protected void cleanupEpiphanyVisualizerCanvas() {
		super.cleanupMulticoreVisualizerCanvas();
		// cleanup local objects
		disposeGraphicalObjects();
		if (m_cpuMap != null) {
			m_cpuMap.clear();
			m_cpuMap = null;
		}
	}
	
	/** Creates the EV graphical canvas objects from scratch */
	protected void createGraphicalObjects() {
		// create Epiphany chip
		m_epiphanyChip = new EpiphanyVisualizerChip(m_eConstants);
		// set its relative position and bounds 
		m_epiphanyChip.setRelativeBounds(m_eConstants.getEpiphanyChipBounds());
		
		if (m_model != null) {
			// create IOs
			for(EpiphanyModelIO modelIO : m_model.getIOs()) {
				m_epiphanyChip.addIO(modelIO.getPosition(), modelIO.getConnected());
			}
			
			// create CPUs
			for(VisualizerCPU modelCPU : m_model.getCPUs()) {
				// figure-out the program name of the thread running on this CPU/core
				String progName = ((EpiphanyModelThread) m_model.getThread(modelCPU.getID() + 1)).getProgramName();
				int pid = ((EpiphanyModelThread) m_model.getThread(modelCPU.getID() + 1)).getPID();
				// create CPU graphical object and associate a color reflecting the process running on it
				EpiphanyVisualizerCPU cpu = m_epiphanyChip.addCPU(modelCPU.getID(), pid, progName, m_model.getLoadMetersEnabled());;
				// keep track which graphical object maps to which model object
				m_cpuMap.put((EpiphanyModelCPU)modelCPU, cpu);
			}
		}
	}
	
	/** Recursively dispose of the canvas's Graphical objects  */
	protected void disposeGraphicalObjects() {
		if (m_epiphanyChip != null) {
			m_epiphanyChip.dispose();
		}
	}
	
	
	// --- accessors ---

	/** Gets currently displayed model. */
	@Override
	public EpiphanyModel getModel()
	{
		return (EpiphanyModel) m_model;
	}

	/** Sets model to display, and requests canvas update. */
	@Override
	public void setModel(VisualizerModel model)
	{
		m_model = (EpiphanyModel) model;
		
		// Set filter associated to new model
		if (m_model != null) {
			m_canvasFilterManager.setCurrentFilter(m_model.getSessionId());
		}
		else {
			m_canvasFilterManager.setCurrentFilter(null);
		}
		requestRecache();
		requestUpdate();
	}
	
	/** Cleans up control */
	@Override
	protected void cleanupMulticoreVisualizerCanvas() {
		super.cleanupMulticoreVisualizerCanvas();
		disposeGraphicalObjects();
	}

	@Override 
	public synchronized void recache() {
		if (! m_recache) return; // nothing to do, free the lock quickly
		
		// re-create all graphical objects from scratch
		if (m_recacheState) {
			// clear all grid view objects
			clear();
			
			// dispose-of and re-create the graphical objects
			disposeGraphicalObjects();
			createGraphicalObjects();
			
			m_recacheState = false;
			m_recacheSizes = true;
		}
		
		// update state of load meters (CPU & Mesh network) to reflect latest model
		if (m_recacheLoadMeters) {
			// go thought all CPUs/cores in the model and reflect their load in canvas
			for ( EpiphanyVisualizerCPU cpu : m_epiphanyChip.getCPUs()) {
				// set all link loads for this CPU to what's in the model
				cpu.setLinksLoad(m_model.getCPU(cpu.getId()).getMeshRouter().getLinksLoad());
				// set router load
				cpu.setRouterLoad(m_model.getCPU(cpu.getId()).getMeshRouter().getLoad());
				// Set CPU load according to model
				cpu.setCPULoad(m_model.getCPU(cpu.getId()).getLoad());
			}
		}
		
		// re-size all graphical objects
		if (m_recacheSizes) {
			// size Epiphany chip to current canvas
			Rectangle bounds = getClientArea();
			// reduce bounds to a square
			int shorter = Math.min(bounds.width, bounds.height);
			
			// set pixel position and size of the Epiphany chip container, and cascade to children objects
			m_epiphanyChip.setBounds(bounds.x, bounds.y, shorter, shorter);
			m_recacheSizes = false;
			
			// test - play with getting objects out of container
//			System.out.println("****************************");
//			for ( EpiphanyVisualizerContainer o :  m_epiphanyChip.getAllObjects(true)) { 
//				System.out.println(o.toString());
//			}
			
			// test - play with getting objects out of container
//			System.out.println("****************************");
//			for ( EpiphanyVisualizerContainer o :  m_epiphanyChip.getSelectableObjects()) { 
//				System.out.println("SELECTABLE: " + o.toString());
//			}
			
			// search for all EpiphanyVisualizerCPU objects, and for each one, search one level
			// deep for its child EpiphanyVisualizerMeshRouter
//			System.out.println("****************************");
//			for ( Object o :  m_epiphanyChip.getChildObjects(EpiphanyVisualizerCPU.class, true)) {
//				EpiphanyVisualizerCPU cpu = (EpiphanyVisualizerCPU) o;
//				System.out.println(cpu.toString());
//				for ( Object oo :  cpu.getChildObjects(EpiphanyVisualizerMeshRouter.class, false)) {
//					EpiphanyVisualizerMeshRouter router = (EpiphanyVisualizerMeshRouter) oo;
//					System.out.println(router.toString());
//				}
//			}
		}
		m_recache = false;
	}
	
	
	@Override
	public void paintCanvas(GC gc) {
		if( m_model == null) return;
		recache();
		
		// clear selected state of CPUs/eCores
		for(EpiphanyModelCPU modelCpu : m_cpuMap.keySet()) {
			EpiphanyVisualizerCPU vCpu = m_cpuMap.get(modelCpu);
			vCpu.setSelected(false);
		}
		
		// restore canvas object highlighting from model object selection
		restoreSelection();
		
		// clear the background
		clearCanvas(gc);
		
		// Make sure color/font resources are properly initialized.
		EpiphanyVisualizerUIPlugin.getResources();
		
		m_epiphanyChip.paintContent(gc);
		m_epiphanyChip.paintDecorations(gc);
		m_marquee.paintContent(gc);
		
		// can be useful to debug canvas object positioning
//		m_epiphanyChip.drawGrid(gc);
	}

	// --- selection methods
	
	/**
	 * Overriding since for the EV we need the selection to select all layers
	 * of applicable objects (since they can overlap), not only the one on the top. 
	 */
	@Override
	public void selectPoint(int x, int y,
			boolean addToSelection, boolean toggleSelection)
	{
		List<MulticoreVisualizerGraphicObject> selectedObjects = new ArrayList<MulticoreVisualizerGraphicObject>();
		List<MulticoreVisualizerGraphicObject> selectableObjects = getSelectableObjects();

		// the list of selectable objects is ordered to have contained objects 
		// before container objects, so the first match we find is the specific 
		// one we want.
		for (MulticoreVisualizerGraphicObject gobj : selectableObjects) {
			if (gobj.contains(x,y)) {
				selectedObjects.add(gobj);
				
				// If a core is selected, give some info about it 
				if (gobj instanceof EpiphanyVisualizerEcore) {
					EpiphanyVisualizerEcore core = (EpiphanyVisualizerEcore) gobj;
					m_epiphanyChip.setStatusBarMessage("eCore: " + core.getLabel() + ", Program: " + core.getProgramName());
				}
//				break;
			}
		}
		
		// else we assume it landed outside any CPU; de-select everything
		if (selectedObjects.isEmpty()) {
			clearSelection();
		}

		// in addToSelection case, include any object in region
		// bracketed by last selection click and current click
		// (with some extra slop added so we pick up objects that
		// overlap the edge of this region)
		if (addToSelection) {
			int slop = SELECTION_SLOP;
			Rectangle r1 = new Rectangle(m_lastSelectionClick.x - slop/2,
					m_lastSelectionClick.y - slop/2,
					slop, slop);
			Rectangle r2 = new Rectangle(x - slop/2, y - slop/2, slop, slop);
			Rectangle region = r1.union(r2);

			for (MulticoreVisualizerGraphicObject gobj : selectableObjects) {
				if (gobj.isWithin(region)) {
					selectedObjects.add(gobj);
				}
			}
		}

		boolean changed = false;

		for (MulticoreVisualizerGraphicObject gobj : selectableObjects) {
			boolean within = selectedObjects.contains(gobj);

			if (addToSelection && toggleSelection) {
				if (within) {
					gobj.setSelected(false);
					changed = true;
				}
			}
			else if (addToSelection) {
				if (within) {
					gobj.setSelected(true);
					changed = true;
				}
			}
			else if (toggleSelection) {
				if (within) {
					gobj.setSelected(! gobj.isSelected());
					changed = true;
				}
			}
			else {
				gobj.setSelected(within);
				changed = true;
			}
		}

		if (changed)
			selectionChanged();

		// remember last mouse-up point for shift-click selection
		m_lastSelectionClick.x = x;
		m_lastSelectionClick.y = y;
	}
	
	// --- selection management methods ---
	
	@Override
	protected void updateSelection(boolean raiseEvent) {
		// get model objects (if any) corresponding to canvas selection
		HashSet<Object> selectedObjects = new HashSet<Object>();

		// TODO: Complete this 
		
		for(EpiphanyModelCPU modelCpu : m_cpuMap.keySet()) {
			EpiphanyVisualizerCPU vCpu = m_cpuMap.get(modelCpu);
			if(vCpu.isSelected()) {
				selectedObjects.add(modelCpu);
			}
		}

		// update model object selection
		ISelection selection = SelectionUtils.toSelection(selectedObjects);
		setSelection(selection, raiseEvent);
	}
	
	
	/** Restores current selection from saved list of model objects. */
	@Override
	protected void restoreSelection() {
		ISelection selection = getSelection();
		List<Object> selectedObjects = SelectionUtils.getSelectedObjects(selection);
				
		// Then mark the selected ones as selected
		for (Object modelObj : selectedObjects) {
			// Epiphany Model CPU object selected? 
			if (modelObj instanceof EpiphanyModelCPU) {
				// get corresponding canvas object and mark it as selected
				EpiphanyVisualizerCPU cpu = m_cpuMap.get(modelObj);
				if (cpu != null) {
					cpu.setSelected(true);
				}
			}
		}
	}
	
	@Override
	protected List<MulticoreVisualizerGraphicObject> getSelectableObjects () {
		List<MulticoreVisualizerGraphicObject> list = new ArrayList<MulticoreVisualizerGraphicObject>();
		for (EpiphanyVisualizerContainer o :  m_epiphanyChip.getSelectableObjects()) {
			list.add(o);
		}
		
		return list;
    }
	
}
