/*******************************************************************************
 * Copyright (c) 2012, 2014 Tilera Corporation and others.
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
 *     Marc Dumais (Ericsson) - bug 447897
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DebugViewTreeWalker;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.visualizer.ui.util.SelectionUtils;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;

/**
* Debug view tree walker that finds elements to select
* based on selection obtained from the multicore visualizer.
*/
@SuppressWarnings("restriction") // allow access to internal interfaces
public class MulticoreVisualizerSelectionFinder extends DebugViewTreeWalker {
	// --- members ---

	/** Selection item(s) we're currently looking for. */
	protected List<Object> m_selection = null;

	/** Result we've found, if any. */
	protected Set<Object> m_result = null;

	// --- constructors/destructors ---

	/** Constructor */
	public MulticoreVisualizerSelectionFinder() {
	}

	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
	}

	// --- methods ---

	/** Finds and returns Debug View element for specified
	 *  Visualizer selection item.
	 *  (E.g. the IDMVMContext for a VisualizerThread.
	 *  Returns null if no match is found.
	 */
	public ISelection findSelection(ISelection selection) {
		m_selection = SelectionUtils.getSelectedObjects(selection);

		m_result = new HashSet<>();
		walk();
		ISelection found = SelectionUtils.toSelection(m_result);
		return found;
	}

	/** Processes an element of the tree view.
	 *  Returns true if children of this element should be processed,
	 *  and false if they can be skipped.
	 */
	@Override
	public boolean processElement(TreePath path) {
		boolean result = true;

		Object element = getElement(path);

		if (element instanceof IDMVMContext) {
			IDMContext context = ((IDMVMContext) element).getDMContext();
			int pid = getPID(context);
			int tid = getTID(context);

			if (isThreadContext(context)) {
				for (Object o : m_selection) {
					if (o instanceof VisualizerThread) {
						VisualizerThread thread = (VisualizerThread) o;
						// The Debug view model uses the GDB thread, to we need to use that one from the Visualizer model
						if (thread.getPID() == pid && thread.getGDBTID() == tid) {
							m_result.add(element);
						}
					}
				}
			} else if (context instanceof IFrameDMContext) {
				// FIXME: if we have frame[0] under a selected thread,
				// select that stack frame instead of the thread
				if (isThreadFrameZero(context)) {
					IDMVMContext threadContext = (IDMVMContext) path.getParentPath().getLastSegment();
					if (m_result.contains(threadContext)) {
						m_result.remove(threadContext);
						m_result.add(element);
					}
				}
			}
		}

		return result;
	}

	/** Returns PID for specified debug context. */
	public static int getPID(IDMContext context) {
		IMIProcessDMContext processContext = DMContexts.getAncestorOfType(context, IMIProcessDMContext.class);
		int pid = (processContext == null) ? 0 : Integer.parseInt(processContext.getProcId());
		return pid;
	}

	/** Returns TID for specified debug context. */
	public static int getTID(IDMContext context) {
		IMIExecutionDMContext execContext = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		int tid = 0;
		if (execContext != null) {
			try {
				tid = Integer.parseInt(execContext.getThreadId());
			} catch (NumberFormatException e) {
				// Unable to resolve thread id
				assert false : "The thread id does not convert to an integer: " + execContext.getThreadId(); //$NON-NLS-1$
			}
		}

		return tid;
	}

	/** Returns true if specified context represents a thread. */
	public static boolean isThreadContext(IDMContext context) {
		// TODO: is there a more elegant way to express this?
		return context instanceof IMIExecutionDMContext && context.getParents().length >= 2
				&& (context.getParents()[0] instanceof IThreadDMContext
						|| context.getParents()[1] instanceof IThreadDMContext);
	}

	/** Returns true if context represents the topmost (0th) frame under a thread. */
	public static boolean isThreadFrameZero(IDMContext context) {
		// TODO: is there a more elegant way to express this?
		String value = context.toString();
		return (value != null && value.endsWith(".frame[0]")); //$NON-NLS-1$
	}
}
