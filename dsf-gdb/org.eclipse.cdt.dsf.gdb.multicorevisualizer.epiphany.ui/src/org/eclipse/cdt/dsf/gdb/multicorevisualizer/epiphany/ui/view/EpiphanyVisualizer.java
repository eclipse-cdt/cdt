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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelCPU;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelIO;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelIO.IOPosition;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelThread;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils.DSFDebugModelEpiphany;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils.DSFDebugModelListenerEpiphany;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils.DSFSessionStateEpiphany;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils.IEpiphanyConstants;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils.Parallella16Constants;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils.Parallella64Constants;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCore;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerLoadInfo;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view.MulticoreVisualizer;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2.IResourcesInformation;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvas;
import org.eclipse.cdt.visualizer.ui.util.SelectionUtils;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class EpiphanyVisualizer extends MulticoreVisualizer  implements DSFDebugModelListenerEpiphany {

	/** Eclipse ID for this view */
	public static final String ECLIPSE_ID = "org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui"; //$NON-NLS-1$
	
	// ATM we do not auto-discover the Epiphany target type...
	// instead chose which one is applicable below and comment-out
	// the other one.
	private static final String TARGET_TYPE = "epiphany-16";
//	private static final String TARGET_TYPE = "epiphany-64";
	
	// --- members ---
	
	private IEpiphanyConstants m_EpiphanyConstants = null;
	
	// Constants 
	
	// Load monitoring refresh periods, in ms
	// These values override the ones defined in the vanilla multicore visualizer
	private static final int LOAD_METER_TIMER_MIN = 500;
	private static final int LOAD_METER_TIMER_FAST = 1000;
	private static final int LOAD_METER_TIMER_MEDIUM = 5000; 
	private static final int LOAD_METER_TIMER_SLOW = 10000;
	
	// --- constructors/destructors ---
	
	/** Constructor. */
	public EpiphanyVisualizer()
	{
		super();
		
		if (TARGET_TYPE.compareTo("epiphany-16") == 0) {
			m_EpiphanyConstants = new Parallella16Constants();
		}
		else if (TARGET_TYPE.compareTo("epiphany-64") == 0) {
			m_EpiphanyConstants = new Parallella64Constants();
		}
	}

	/** Dispose method. */
	public void  dispose() {
		super.dispose();
	}
	
	
	// --- init methods ---
	
	
	// --- accessors ---

	/** Returns non-localized unique name for this visualizer. */
	@Override
	public String getName() {
		return "epiphany Visualizer"; 
	}

	/** Returns localized name to display for this visualizer. */
	@Override
	public String getDisplayName() {
		return "Epiphany Visualizer"; 
	}

	/** Returns localized tooltip text to display for this visualizer. */
	@Override
	public String getDescription() {
		return "Displays current state of selected epiphany debug target"; 
	}
	
	
	// --- action management ---
		
		
		
	
	// --- canvas management ---

	/** Creates and returns visualizer canvas control. */
	@Override
	public GraphicCanvas createCanvas(Composite parent)
	{
		m_canvas = new EpiphanyVisualizerCanvas(parent, m_EpiphanyConstants);
		m_canvas.addSelectionChangedListener(this);
		return m_canvas;
	}
	

// --- workbench selection management ---
	
    /**
     * Tests whether if the IVisualizer can display the selection
     * (or something reachable from it).
	 */
	@Override
	public int handlesSelection(ISelection selection)
	{
		// By default, we don't support anything.
//		int result = 0;
//		
//		Object sel = SelectionUtils.getSelectedObject(selection);
//		if (sel instanceof GdbLaunch ||
//			sel instanceof GDBProcess ||
//			sel instanceof IDMVMContext)
//		{
//			result = 2;
//		}
//		else {
//			result = 0;
//		}
//		
//		// While we're here, see if we need to attach debug view listener
//		updateDebugViewListener();
//		
//		return result;
		
		return 2;
//		return 0;
	}
	
	@Override
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
			m_sessionState = new DSFSessionStateEpiphany(sessionId);
			m_sessionState.addServiceEventListener(fEventListener);
			// start timer that updates the load meters
			initializeLoadMeterTimer();
			changed = true;
		}
		
		return changed;
	}
	
	
	
	// --- Selection conversion methods ---
	
	/** Gets debug view selection from visualizer selection. */
	protected ISelection visualizerToDebugViewSelection(ISelection visualizerSelection)
	{
		EpiphanyVisualizerSelectionFinder selectionFinder =
			new EpiphanyVisualizerSelectionFinder((EpiphanyModel) m_canvas.getModel());
		ISelection workbenchSelection =
			selectionFinder.findSelection(visualizerSelection);
		return workbenchSelection;
	}
	
	
	/** Gets visualizer selection from debug view selection. */
	@Override
	protected ISelection workbenchToVisualizerSelection(ISelection workbenchSelection)
	{
		ISelection visualizerSelection = null;
		
		List<Object> items = SelectionUtils.getSelectedObjects(workbenchSelection);
		
		if (m_canvas != null) {
			// Use the current canvas model to match Debug View items
			// with corresponding threads, if any.
			EpiphanyModel model = (EpiphanyModel) m_canvas.getModel();
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
								for (VisualizerThread t : threads) {
									tid = t.getGDBTID();
									EpiphanyModelCPU cpu = model.getCPU(tid - 1);
									if (cpu != null) {
										selected.add(cpu);
									}
								}
							}
						}
						else { // thread
							EpiphanyModelCPU cpu = model.getCPU(tid - 1);
							if (cpu != null) {
								selected.add(cpu);
							}
						}
					}
				}
				visualizerSelection = SelectionUtils.toSelection(selected);
			}
		}
		
		return visualizerSelection;
	}
	
	
	
	
	// --- Visualizer model update methods ---
	
	
//	(gdb) info os process
//	pid        user       command    cores      
//	1          root                  0000,0001,0002,0003,0100,0101,0102,0103,0200,0201,0202,0203,0300,0301,0302,0303 
//	(gdb) info inf
//	  Num  Description       Executable        
//	* 1    Remote target     /opt/adapteva/esdk.5.13.09.10/examples/matmul-16/device/Release/e_matmul.elf 
//	(gdb) info threads 
//	[New Remote target]
//	[New Thread 2]
//	[New Thread 3]
//	[New Thread 256]
//	[New Thread 257]
//	[New Thread 258]
//	[New Thread 259]
//	[New Thread 512]
//	[New Thread 513]
//	[New Thread 514]
//	[New Thread 515]
//	[New Thread 768]
//	[New Thread 769]
//	[New Thread 770]
//	[New Thread 771]
//	  Id   Target Id         Frame 
//	  16   Thread 771 (Core: 1202: Runnable) 0x0000062a in _exit ()
//	  15   Thread 770 (Core: 1201: Runnable) 0x0000062a in _exit ()
//	  14   Thread 769 (Core: 1200: Runnable) 0x0000062a in _exit ()
//	  13   Thread 768 (Core: 1163: Runnable) 0x0000062a in _exit ()
//	  12   Thread 515 (Core: 0802: Runnable) 0x0000062a in _exit ()
//	  11   Thread 514 (Core: 0801: Runnable) 0x0000062a in _exit ()
//	  10   Thread 513 (Core: 0800: Runnable) 0x0000062a in _exit ()
//	  9    Thread 512 (Core: 0763: Runnable) 0x0000062a in _exit ()
//	  8    Thread 259 (Core: 0402: Runnable) 0x0000062a in _exit ()
//	  7    Thread 258 (Core: 0401: Runnable) 0x0000062a in _exit ()
//	  6    Thread 257 (Core: 0400: Runnable) 0x0000062a in _exit ()
//	  5    Thread 256 (Core: 0363: Runnable) 0x0000062a in _exit ()
//	  4    Thread 3 (Core: 0002: Runnable) 0x0000062a in _exit ()
//	  3    Thread 2 (Core: 0001: Runnable) 0x0000062a in _exit ()
//	  2    Remote target     0x0000062a in _exit ()
//	* 1    Thread 1 (Core: 0000: Runnable) 0x0000062a in _exit ()

	
	/** 
	 * Starts visualizer model request.
	 * Calls getVisualizerModelDone() with the constructed model.
	 */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void getVisualizerModel() {
		EpiphanyModel model;
		fDataModel = new EpiphanyModel(m_sessionState.getSessionID());
		model = (EpiphanyModel)fDataModel;
		//			DSFDebugModel.getCPUs(m_sessionState, this, fDataModel);

		// mock OS thread id
		int[] osThreads = {
			101,102,103,104,201,201,203,204,301,302,303,304,401,402,403,404
		};
		
		// mock executables
		String[] execs_16 = {
				"bcrypt.elf", "bcrypt.elf",	"2d-fft.elf", "2d-fft.elf",
				"bcrypt.elf", "bcrypt.elf", "2d-fft.elf", "2d-fft.elf",
				"bcrypt.elf", "bcrypt.elf",	"matmul.elf", "matmul.elf",
				"bcrypt.elf", "bcrypt.elf", "matmul.elf", "matmul.elf"
				};

		String[] execs_64 = {
				"bcrypt.elf","bcrypt.elf","bcrypt.elf","bcrypt.elf","2d-fft.elf","2d-fft.elf","2d-fft.elf","2d-fft.elf",
				"bcrypt.elf","bcrypt.elf","bcrypt.elf","bcrypt.elf","2d-fft.elf","2d-fft.elf","2d-fft.elf","2d-fft.elf",
				"bcrypt.elf","bcrypt.elf","bcrypt.elf","bcrypt.elf","2d-fft.elf","2d-fft.elf","2d-fft.elf","2d-fft.elf",
				"bcrypt.elf","bcrypt.elf","bcrypt.elf","bcrypt.elf","2d-fft.elf","2d-fft.elf","2d-fft.elf","2d-fft.elf",
				"bcrypt.elf","bcrypt.elf","bcrypt.elf","bcrypt.elf","matmul.elf","matmul.elf","matmul.elf","matmul.elf",				
				"bcrypt.elf","bcrypt.elf","bcrypt.elf","bcrypt.elf","matmul.elf","matmul.elf","matmul.elf","matmul.elf",
				"bcrypt.elf","bcrypt.elf","bcrypt.elf","bcrypt.elf","matmul.elf","matmul.elf","matmul.elf","matmul.elf",
				"bcrypt.elf","bcrypt.elf","bcrypt.elf","bcrypt.elf","matmul.elf","matmul.elf","matmul.elf","matmul.elf",
				};

		/**
		 * for now fake the building of the Epiphany model - we will eventually
		 * need to get this information through GDB
		 * 
		 */
		// create model for n eCore CPUs each having a single core and a single thread
		for (int i = 0; i < m_EpiphanyConstants.getNumEcores(); i++) {		
			EpiphanyModelCPU cpu = model.addCPU(new EpiphanyModelCPU(i, m_EpiphanyConstants.getLabelFromId(i)));

			// each CPU has one core
			int osCoreId = i;
//			int osCoreId = Integer.parseInt("0001");
			VisualizerCore core = cpu.addCore(new VisualizerCore(cpu, osCoreId));
			
			// add one thread per core - with fake ids
			String execName = "e_matmul.elf";
			if (TARGET_TYPE == "epiphany-16") {
				execName = execs_16[i];
			}
			else {
				execName = execs_64[i];
			}
			
//			int pid = i;
//			int pid = 42000;
//			int pid = 42000 + i;
			int pid = Math.abs(execName.hashCode()); // give same pid for same executable
			
//			int pid;
//			if (execName == "bcrypt.elf") {
//				pid = 1;
//			}
//			else if (execName == "2d-fft.elf") {
//				pid = 2;
//			}
//			else {
//				pid = 3;
//			}
			
//			int osTid = osThreads[i];
			int osTid = i;
			int gdbTid = i + 1 ;
			model.addThread(new EpiphanyModelThread(core, execName, pid, osTid, gdbTid, VisualizerExecutionState.RUNNING));
		}

		// Add the IOs
		model.addIO(new EpiphanyModelIO(false, IOPosition.IO_NORTH) );
		model.addIO(new EpiphanyModelIO(false, IOPosition.IO_SOUTH) );
		model.addIO(new EpiphanyModelIO(false, IOPosition.IO_EAST) );
		model.addIO(new EpiphanyModelIO(true, IOPosition.IO_WEST) );
		
		getVisualizerModelDone(model);
	}
	
	/** Get load info from service */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	@Override
	public void updateLoads() {
		// if meters not enabled, do not query backend
		if (!m_loadMetersEnabled) {
			return;
		}
		
		VisualizerModel model = fDataModel;
		
		// We expect two responses - one for core load and one for traffic load
		model.getLoadTodo().add(2);
		
		DSFDebugModelEpiphany.getCoreLoad(m_sessionState, this, model);
		DSFDebugModelEpiphany.getTrafficLoad(m_sessionState, this, model);
	}
	
	
	/** Invoked when a getLoad() request completes */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void getCoreLoadDone(IDMContext context, IResourcesInformation loads, Object arg) 
	{
		EpiphanyModel model = (EpiphanyModel) arg;
		int coreIdIndex = 0;
		int loadIndex = 0;
		
		if (loads != null) {			
			int column = 0;
			for (String col : loads.getColumnNames()) {
				if (col.equalsIgnoreCase("coreid")) {
					coreIdIndex = column;
				}
				else if (col.equalsIgnoreCase("load")) {
					loadIndex = column;
				}
				column ++;
			}
			
			for (String[] line : loads.getContent()) {
				// Put load info into model
				int cpuId =  m_EpiphanyConstants.getIdFromLabel(line[coreIdIndex]);
				EpiphanyModelCPU cpu = model.getCPU(cpuId);
				cpu.setLoadInfo(new VisualizerLoadInfo(Integer.valueOf(line[loadIndex])));
			}	
		}
		
		loadDone(1, model);
	}

	
	@Override
	public void getTrafficLoadDone(IDMContext context, IResourcesInformation loads, Object arg) 
	{
		EpiphanyModel model = (EpiphanyModel) arg;
		int coreIdIndex = 0;
		
		if (loads != null) {			
			int column = 0;
			for (String col : loads.getColumnNames()) {
				if (col.equalsIgnoreCase("coreid")) {
					coreIdIndex = column;
				}
				column ++;
			}
			
			for (String[] line : loads.getContent()) {
				// Put load info into model
				int cpuId =  m_EpiphanyConstants.getIdFromLabel(line[coreIdIndex]);
				EpiphanyModelCPU cpu = model.getCPU(cpuId);
				// North-in, North-out, East-in, East-out, South-in, South-out, West-in, West-out
				VisualizerLoadInfo[] trafficLoads = {
						new VisualizerLoadInfo(line[1].startsWith("-") ? 0 : Integer.valueOf(line[1])),
						new VisualizerLoadInfo(line[2].startsWith("-") ? 0 : Integer.valueOf(line[2])),
						new VisualizerLoadInfo(line[5].startsWith("-") ? 0 : Integer.valueOf(line[5])),
						new VisualizerLoadInfo(line[6].startsWith("-") ? 0 : Integer.valueOf(line[6])),
						new VisualizerLoadInfo(line[3].startsWith("-") ? 0 : Integer.valueOf(line[3])),
						new VisualizerLoadInfo(line[4].startsWith("-") ? 0 : Integer.valueOf(line[4])),
						new VisualizerLoadInfo(line[7].startsWith("-") ? 0 : Integer.valueOf(line[7])),
						new VisualizerLoadInfo(line[8].startsWith("-") ? 0 : Integer.valueOf(line[8]))
					};
						
				cpu.getMeshRouter().setLinksLoads(trafficLoads);
			}	
		}
		loadDone(1, model);	
	}
	
}
