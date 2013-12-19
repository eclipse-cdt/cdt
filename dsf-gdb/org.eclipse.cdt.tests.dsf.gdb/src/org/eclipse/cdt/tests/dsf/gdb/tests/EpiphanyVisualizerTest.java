/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import java.util.Random;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelCPU;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelIO;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelIO.IOPosition;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.view.EpiphanyVisualizerContainer;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCPU;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCore;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerLoadInfo;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicObject;
import org.junit.Test;

@SuppressWarnings("restriction")
public class EpiphanyVisualizerTest {

	final static String SESSION_ID_0 = "0";
	
	/** load from 0 to 100 */
	final static int MAX_LOAD = 100;
	
	/** Number of Ecores in an Epiphany chip */
	final static int NUM_ECORES = 64;
	
	/** Number of eMesh links for each eCore */
	final static int NUM_LINKS_PER_CORE = 8;
	
	/**  eMesh links loads for all eCores of an Epiphany chip */
	private static VisualizerLoadInfo[][] ALL_LINKS_LOADS = null;
	
	/**  positions of 4 IOs */
	final static IOPosition[] IO_POSITIONS = {
		IOPosition.IO_NORTH,
		IOPosition.IO_SOUTH,
		IOPosition.IO_EAST,
		IOPosition.IO_WEST
	};
	
	/**  Connection state of 4 IOs */
	final static boolean[] IO_CONNECTED = { false, false, false, true };
	
	/**  CPU loads for 16 eCores - dynamically generated at runtime */
	private static VisualizerLoadInfo[] CPU_LOADS = null;
	
	/**  Random number generator - seeded with value that will change each time */
	private Random rand = new Random(System.currentTimeMillis());
	
	GraphicObject g = new GraphicObject();
	
	// testcases
	
	/**
	 * Test that the model correctly saves info used to create it and gives it correctly back
	 * when queried.
	 */
	@Test
	public void testEpiphanyVisualizerModel() throws Exception {
		// create a fake model
		EpiphanyModel model = createModel(SESSION_ID_0);
		
		// confirm that 16 CPUs were correctly created in the model
		org.junit.Assert.assertEquals(NUM_ECORES, model.getCPUCount());
		
		// confirm that 16 cores were correctly created in the model
		org.junit.Assert.assertEquals(NUM_ECORES, model.getCoreCount());
				
		// confirm that 16 threads were correctly created in the model
		org.junit.Assert.assertEquals(NUM_ECORES, model.getThreadCount());
		
		// Check internal params of the threads to make sure they contain what they should
		VisualizerThread thread;
		for (int i = 0; i < NUM_ECORES; i++) {
			thread = model.getThread(i); 
			org.junit.Assert.assertEquals(i , thread.getGDBTID());
			org.junit.Assert.assertEquals(i + 10000 , thread.getTID());
			org.junit.Assert.assertEquals(i , thread.getPID());
		}
		
		// test that the model correctly reports the loads that are associated to each CPU 
		int i = 0;
		for (VisualizerCPU c : model.getCPUs()) {
			EpiphanyModelCPU cpu = (EpiphanyModelCPU) c;
			org.junit.Assert.assertEquals(CPU_LOADS[i].getLoad(), cpu.getLoad());
			
			// test that the model correctly reports the loads associated to each
			// of the links of the current CPU
			Integer l[] = cpu.getMeshRouter().getLinksLoad();
			for (int j = 0; j < l.length; j++) {
				org.junit.Assert.assertEquals(ALL_LINKS_LOADS[i][j].getLoad(), l[j]);
//				System.out.println("test vector: " + ALL_LINKS_LOADS[i][j].getLoad() +" , model: " + l[j]);
			}
			
			i++;
		}
		
		// Check that the IOs were correctly created in the model
		i = 0;
		for (EpiphanyModelIO io : model.getIOs()) {
			// Verify that IO position matches
			org.junit.Assert.assertEquals(IO_POSITIONS[i], io.getPosition());
			// Verify connected state matches
			org.junit.Assert.assertEquals(IO_CONNECTED[i], io.getConnected());
			
			i++;
		}
		
	}
	
	/** 
	 * Test that Epiphany Visualizer canvas objects scale correctly.
	 * To do that create a hierarchy of graphical object using relative coordinates
	 * and sizes (bounds). Then set the real bounds for the outer-most object. Verify
	 * that child objects end-up with expected pixel bounds.
	 * 
	 *  Also test retrieval of child objects. 
	 */
	@Test
	public void testEpiphanyCanvasObjectsRelativeSizingAndRetrieval() throws Exception {
		
		int[] containerABounds = {0, 0, 10, 10};
		int[] containerBBounds = {1, 1, 8, 8};
		int[] containerCBounds = {3, 2, 2, 2};
		int[] containerDBounds = {3, 5, 2, 2};
		int[] containerEBounds = {6, 1, 1, 6};
		int[] containerFBounds = {1, 1, 1, 1};
		
		// we create the graphical objects with no margin, so as to make the sizes
		// easier to predict / understand,  for this test
		EpiphanyVisualizerContainer containerA = new EpiphanyVisualizerContainer(true, 0);
		EpiphanyVisualizerContainer containerB = new EpiphanyVisualizerContainer(true, 0);
		EpiphanyVisualizerContainer containerC = new EpiphanyVisualizerContainer(true, 0);
		EpiphanyVisualizerContainer containerD = new EpiphanyVisualizerContainer(true, 0);
		EpiphanyVisualizerContainer containerE = new EpiphanyVisualizerContainer(true, 0);
		EpiphanyVisualizerContainer containerF = new EpiphanyVisualizerContainer(true, 0);
		
		// set virtual bounds of A
		containerA.setRelativeBounds(containerABounds);
		
		// Add B within A
		containerA.addChildObject("B", containerB);
		
		// Add C, D and E within B
		containerB.addChildObject("C", containerC);
		containerB.addChildObject("D", containerD);
		containerB.addChildObject("E", containerE);
		
		// Add F within C
		containerC.addChildObject("F", containerF);
		
		// set relative bounds of the other objects, relative to their parent objects
		containerB.setRelativeBounds(containerBBounds);
		containerC.setRelativeBounds(containerCBounds);
		containerD.setRelativeBounds(containerDBounds);
		containerE.setRelativeBounds(containerEBounds);
		containerF.setRelativeBounds(containerFBounds);
		
		// Define the real pixel bounds of the outer-most container - 
		// the pixel coordinates of all children objects will be 
		// computed recursively
		containerA.setBounds(100, 50, 100, 100);
		
		// check computed bounds for containerA
		org.junit.Assert.assertEquals(100, containerA.getBounds().x);
		org.junit.Assert.assertEquals(50, containerA.getBounds().y);
		org.junit.Assert.assertEquals(100, containerA.getBounds().width);
		org.junit.Assert.assertEquals(100, containerA.getBounds().height);
		
		// check computed bounds for containerB
		org.junit.Assert.assertEquals(110, containerB.getBounds().x);
		org.junit.Assert.assertEquals(60, containerB.getBounds().y);
		org.junit.Assert.assertEquals(80, containerB.getBounds().width);
		org.junit.Assert.assertEquals(80, containerB.getBounds().height);

		// check computed bounds for containerC
		org.junit.Assert.assertEquals(140, containerC.getBounds().x);
		org.junit.Assert.assertEquals(80, containerC.getBounds().y);
		org.junit.Assert.assertEquals(20, containerC.getBounds().width);
		org.junit.Assert.assertEquals(20, containerC.getBounds().height);

		// check computed bounds for containerD
		org.junit.Assert.assertEquals(140, containerD.getBounds().x);
		org.junit.Assert.assertEquals(110, containerD.getBounds().y);
		org.junit.Assert.assertEquals(20, containerD.getBounds().width);
		org.junit.Assert.assertEquals(20, containerD.getBounds().height);

		// check computed bounds for containerE
		org.junit.Assert.assertEquals(170, containerE.getBounds().x);
		org.junit.Assert.assertEquals(70, containerE.getBounds().y);
		org.junit.Assert.assertEquals(10, containerE.getBounds().width);
		org.junit.Assert.assertEquals(60, containerE.getBounds().height);

		// check computed bounds for containerF
		org.junit.Assert.assertEquals(150, containerF.getBounds().x);
		org.junit.Assert.assertEquals(90, containerF.getBounds().y);
		org.junit.Assert.assertEquals(10, containerF.getBounds().width);
		org.junit.Assert.assertEquals(10, containerF.getBounds().height);
		
		
		// check recursive object retrieval returns expected number of child objects, for A
		org.junit.Assert.assertEquals(5, containerA.getAllObjects(true).size());
		
		// check recursive object retrieval returns expected number of child objects, for B
		org.junit.Assert.assertEquals(4, containerB.getAllObjects(true).size());
		
		// check non-recursive object retrieval returns expected number of (1st level) child objects, for A
		org.junit.Assert.assertEquals(1, containerA.getAllObjects(false).size());
		
		// check object retrieval returns expected number of "selectable" child objects, for C
		org.junit.Assert.assertEquals(1, containerC.getSelectableObjects().size());
		
		// check specific children-object retrieval
		EpiphanyVisualizerContainer obj = containerA.getObject("D");
		org.junit.Assert.assertEquals(containerD, obj);

		// check anotherspecific object retrieval
		obj = containerC.getObject("F");
		org.junit.Assert.assertEquals(containerF, obj);
		
		// Check the retrieval of an non-existing object
		obj = containerC.getObject("blablabla");
		org.junit.Assert.assertEquals(null, obj);
	}
	
	
	
	// Utility functions
	
	/** Generate random test vector - One load per eCore */
	public void genCPULoadTestVectors() {
		CPU_LOADS = new VisualizerLoadInfo[NUM_ECORES];
		
		for (int i = 0; i < NUM_ECORES; i++) {
			CPU_LOADS[i] = genRandomLoad();
		}
	}
	
	/** Generate random test vector - one load per eMesh link */
	public void genLinkLoadTestVectors() {
		ALL_LINKS_LOADS = new VisualizerLoadInfo[NUM_ECORES][NUM_LINKS_PER_CORE];
		
		for (int i = 0; i < NUM_ECORES; i++) {
			for (int j = 0; j < NUM_LINKS_PER_CORE; j++) {
				ALL_LINKS_LOADS[i][j] = genRandomLoad();
			}
		}
	}
	
	/** Generate one random load value */
	public VisualizerLoadInfo genRandomLoad() {
		int n = rand.nextInt(MAX_LOAD + 1);
//		System.out.println("n = " + n);
		
		return new VisualizerLoadInfo(n);
	}

	
	/** Create a bogus Epiphany Visualizer Model for testing purposes  */
	private EpiphanyModel createModel(String sessionId) {
		
		EpiphanyModel model = new EpiphanyModel(sessionId);
		
		// create test vectors
		genCPULoadTestVectors();
		genLinkLoadTestVectors();
		
		// create model for 16 eCore CPUs each having a single core and a single thread
		for (int i = 0; i < NUM_ECORES; i++) {				
			EpiphanyModelCPU cpu = model.addCPU(new EpiphanyModelCPU(i, Integer.toString(i)));
			
			// each CPU has one core
			int osCoreId = i;
			VisualizerCore core = cpu.addCore(new VisualizerCore(cpu, osCoreId));
			
			// add one thread per core - with fake ids
			int pid = i;
			int osTid = i + 10000;
			int gdbTid = i;
			model.addThread(new VisualizerThread(core, pid, osTid, gdbTid, VisualizerExecutionState.RUNNING));
			
			// add loads for the emesh links of this CPU
			cpu.getMeshRouter().setLinksLoads(ALL_LINKS_LOADS[i]);
			// Set load for CPU
			cpu.setLoadInfo(CPU_LOADS[i]);
		}
		
		// Add the IOs
		model.addIO(new EpiphanyModelIO(false, IOPosition.IO_NORTH) );
		model.addIO(new EpiphanyModelIO(false, IOPosition.IO_SOUTH) );
		model.addIO(new EpiphanyModelIO(false, IOPosition.IO_EAST) );
		model.addIO(new EpiphanyModelIO(true, IOPosition.IO_WEST) );
		
		return model;
	}
	
}
