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

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCPU;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerLoadInfo;

@SuppressWarnings("restriction")
/** represents an epiphany CPU/eCore */
public class EpiphanyModelCPU extends VisualizerCPU /*implements Comparable<VisualizerCPU>, IVisualizerModelObject */
{
	// --- members ---
	
	/** Label associated to eCore - a more user-friendly id */
	protected String m_label; 
	
	/** Each Epiphany core/CPU has an associated mesh router */
	protected EpiphanyModelMeshRouter m_meshRouter;

	// constants
	
	
	// --- constructors/destructors ---
	
	/** Constructor */
	public EpiphanyModelCPU(int id, String label) {
		super(id);
		m_label = label;
		// give mesh router same id as the eCore
		this.m_meshRouter = new EpiphanyModelMeshRouter(this, id);
		// set default CPU load to zero
		this.setLoadInfo(new VisualizerLoadInfo(0));
	}
	
	/** Dispose method */
	public void dispose() {
		super.dispose();
		if (m_meshRouter != null) {
			m_meshRouter.dispose();
			m_meshRouter = null;
		}
	}
	
	
	// --- Object methods ---
	
	/** Returns string representation. */
	@Override
	public String toString() {
		return "Epiphany eCore id:" + m_id + ", Label: "+ m_label; //$NON-NLS-1$
	}
	
	
	// --- accessors ---
	
	/** Gets the mesh router object associated to this eCore */
	public EpiphanyModelMeshRouter getMeshRouter() {
		return m_meshRouter;
	}
	
	/** Sets the mesh router object associated to this eCore */
	public void setMeshRouter(EpiphanyModelMeshRouter router) {
		m_meshRouter = router;
	}
	
	/** get the more user-friendly version of the coreId */
	public String getLabel() {
		return m_label;
	}
	
}