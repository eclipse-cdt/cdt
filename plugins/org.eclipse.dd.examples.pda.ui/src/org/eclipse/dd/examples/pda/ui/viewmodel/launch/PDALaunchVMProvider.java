/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson			  - Modified for new functionality	
 *******************************************************************************/
package org.eclipse.dd.examples.pda.ui.viewmodel.launch;

import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch.AbstractLaunchVMProvider;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch.LaunchRootVMNode;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch.StackFramesVMNode;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch.StandardProcessVMNode;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMNode;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;


/**
 * View Model provider for the Launch (AKA Debug) view. The launch VM 
 * provider is configured with three nodes:
 * <ul>
 * <li> LaunchRootVMNode - This is the root of the PDA view model.</li>  
 * <li> PDAVirtualMachineVMNode - Supplies the element representing PDA VM</li>
 * <li> PDAThreadsVMNode - Supplies the PDA thread elements</li>
 * <li> StackFramesVMNode - Supplies the stack frame elements.</li>  
 * <li> StandardProcessVMNode - Supplies elements representing the PDA 
 * debugger process.</li>
 * </ul> 
 */
@SuppressWarnings("restriction")
public class PDALaunchVMProvider extends AbstractLaunchVMProvider 
    implements IDebugEventSetListener, ILaunchesListener2
{
    @ThreadSafe
    public PDALaunchVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext, DsfSession session) 
    {
        super(adapter, presentationContext, session);
        
        IRootVMNode launchNode = new LaunchRootVMNode(this);
        setRootNode(launchNode);

        // Launch node is a parent to the processes and program nodes.
        IVMNode pdaVirtualMachineNode = new PDAVirtualMachineVMNode(this, getSession());
        IVMNode processesNode = new StandardProcessVMNode(this);
        addChildNodes(launchNode, new IVMNode[] { pdaVirtualMachineNode, processesNode});
        
        // Virtual machine node is under the PDA threads node.
        IVMNode threadsNode = new PDAThreadsVMNode(this, getSession());
        addChildNodes(pdaVirtualMachineNode, new IVMNode[] { threadsNode });
        
        // Stack frames node is under the PDA threads node.
        IVMNode stackFramesNode = new StackFramesVMNode(this, getSession());
        addChildNodes(threadsNode, new IVMNode[] { stackFramesNode });

        // Register the LaunchVM provider as a listener to debug and launch 
        // events.  These events are used by the launch and processes nodes.
        DebugPlugin.getDefault().addDebugEventListener(this);
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
    }
}
