/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb.viewmodel;

import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.LaunchRootVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.StackFramesVMNode;
import org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch.ContainerVMNode;
import org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch.GdbStandardProcessVMNode;
import org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch.LaunchVMProvider;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

@SuppressWarnings("restriction")
public class GdbExtendedLaunchVMProvider extends LaunchVMProvider {
	@ThreadSafe
	public GdbExtendedLaunchVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext,
			DsfSession session) {
		super(adapter, presentationContext, session);
	}

	@Override
	protected void createNodes() {
		IRootVMNode launchNode = new LaunchRootVMNode(this);
		setRootNode(launchNode);

		// Container node to contain all processes and threads
		IVMNode containerNode = new ContainerVMNode(this, getSession());
		IVMNode processesNode = new GdbStandardProcessVMNode(this);
		addChildNodes(launchNode, new IVMNode[] { containerNode, processesNode });

		IVMNode threadsNode = new GdbExtendedThreadVMNode(this, getSession());
		addChildNodes(containerNode, new IVMNode[] { threadsNode });

		IVMNode stackFramesNode = new StackFramesVMNode(this, getSession());
		addChildNodes(threadsNode, new IVMNode[] { stackFramesNode });
	}
}
