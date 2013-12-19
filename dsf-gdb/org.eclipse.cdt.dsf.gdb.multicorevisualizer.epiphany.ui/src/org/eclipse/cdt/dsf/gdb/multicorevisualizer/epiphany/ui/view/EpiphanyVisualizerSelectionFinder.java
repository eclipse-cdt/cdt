/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.view;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelCPU;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view.MulticoreVisualizerSelectionFinder;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.jface.viewers.TreePath;

@SuppressWarnings("restriction")
public class EpiphanyVisualizerSelectionFinder extends MulticoreVisualizerSelectionFinder {

	/** Model */
	EpiphanyModel m_model = null;

	// --- constructors/destructors ---

	/** Constructor */
	public EpiphanyVisualizerSelectionFinder(EpiphanyModel model)
	{
		super();
		m_model = model;
	}

	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
	}

	// --- methods ---

	public boolean processElement(TreePath path)
	{
		boolean result = true;

		Object element = getElement(path);

		if (element instanceof IDMVMContext) {
			IDMContext context = ((IDMVMContext) element).getDMContext();
			int pid = getPID(context);
			int tid = getTID(context);

			if (isThreadContext(context))
			{
				// For each selected object
				for (Object o : m_selection) {
					// only interested in selected CPUs
					if (o instanceof EpiphanyModelCPU) {
						// find thread running on CPU
						EpiphanyModelCPU modelCpu = (EpiphanyModelCPU) o;
						for (VisualizerThread t : m_model.getThreads()) {
							if (t.getCore().getID() == modelCpu.getID()) {
								// does the thread running on CPU match the one in the debug view (current "path")? 
								if (t.getPID() == pid && t.getGDBTID() == tid) {
									// Found the thread running on selected CPU
									m_result.add(element);
									break;
								}
							}
						}
					}
				}
			}
		}

		return result;
	}
}
