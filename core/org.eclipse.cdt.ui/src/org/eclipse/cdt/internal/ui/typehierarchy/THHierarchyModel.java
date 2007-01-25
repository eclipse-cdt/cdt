/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.typehierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

class THHierarchyModel {
    public class BackgroundJob extends Job {
		public BackgroundJob() {
			super(Messages.THHierarchyModel_Job_title);
		}

		protected IStatus run(IProgressMonitor monitor) {
			return onComputeGraph(this, monitor);
		}
	}
    
	static final int TYPE_HIERARCHY = 0;
    static final int SUB_TYPE_HIERARCHY = 1;
    static final int SUPER_TYPE_HIERARCHY = 2;

	static final int END_OF_COMPUTATION = 0;
	
	private static final ISchedulingRule RULE = new THSchedulingRule();
	private static final Object[] NO_CHILDREN= new Object[0];

	private ICElement fInput;
	private int fHierarchyKind;
	private boolean fShowInheritedMembers;
	
	private THGraph fGraph;
	private THNode[] fRootNodes;
	private THNode fSelectedTypeNode;
	private ICElement fTypeToSelect;
	private ICElement fSelectedMember;
	private String fMemberSignatureToSelect;
	
	private Job fJob;
	private Display fDisplay;
	private THViewPart fView;
	
	public THHierarchyModel(THViewPart view, Display display) {
		fDisplay= display;
		fView= view;
	}
	
	public ICElement getInput() {
		return fInput;
	}

	public int getHierarchyKind() {
		return fHierarchyKind;
	}

	public void setHierarchyKind(int hierarchyKind) {
		fHierarchyKind = hierarchyKind;
		computeNodes();
	}

	public boolean isShowInheritedMembers() {
		return fShowInheritedMembers;
	}

	public void setShowInheritedMembers(boolean showInheritedMembers) {
		fShowInheritedMembers = showInheritedMembers;
		updateSelectedMember();
		updateImplementors();
	}

	public Object[] getHierarchyRootElements() {
		if (fRootNodes == null) {
			return new Object[] {"..."}; //$NON-NLS-1$
		}
		return fRootNodes;
	}

//	public void setWorkingSetFilter(WorkingSetFilterUI filterUI) {
//		
//	}

	synchronized public void setInput(ICElement input, ICElement member) {
		stopGraphComputation();
		fInput= input;
		fSelectedMember= member;
		fMemberSignatureToSelect= TypeHierarchyUI.getLocalElementSignature(fSelectedMember);
		fRootNodes= null;
		fSelectedTypeNode= null;
		fTypeToSelect= input;
	}

	synchronized public void computeGraph() {
		if (fJob != null) {
			fJob.cancel();
		}
		fJob= new BackgroundJob();
		fJob.setRule(RULE);
		IWorkbenchSiteProgressService ps= (IWorkbenchSiteProgressService) fView.getSite().getAdapter(IWorkbenchSiteProgressService.class);
		if (ps != null) {
			ps.schedule(fJob, 0L, true);
		}
		else {
			fJob.schedule();
		}
	}

	synchronized public void stopGraphComputation() {
		if (fJob != null) {
			fJob.cancel();
		}
		fJob= null;
	}

	protected IStatus onComputeGraph(Job job, IProgressMonitor monitor) {
		THGraph graph= new THGraph();
		try {
			ICProject[] scope= CoreModel.getDefault().getCModel().getCProjects();
			IIndex index= CCorePlugin.getIndexManager().getIndex(scope);
			index.acquireReadLock();
			try {
				if (monitor.isCanceled()) 
					return Status.CANCEL_STATUS;
				graph.defineInputNode(index, fInput);
				graph.addSuperClasses(index, monitor);
				if (monitor.isCanceled()) 
					return Status.CANCEL_STATUS;
				graph.addSubClasses(index, monitor);
				if (monitor.isCanceled()) 
					return Status.CANCEL_STATUS;
			}
			finally {
				index.releaseReadLock(); 
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
		finally {
			onJobDone(graph, job);
		}			
		return Status.OK_STATUS;
	}
	
	
	protected void computeNodes() {
		if (fGraph == null) {
			return;
		}
		boolean fwd= fHierarchyKind == SUPER_TYPE_HIERARCHY;
		ArrayList stack= new ArrayList();
		ArrayList roots= new ArrayList();
		ArrayList leaves= new ArrayList();
		
		THNode[] newSelection= new THNode[2];
		Collection groots;
		if (fHierarchyKind == TYPE_HIERARCHY) {
			groots= fGraph.getLeaveNodes();
		}
		else {
			THGraphNode node= fGraph.getInputNode();
			if (node != null) {
				groots= Collections.singleton(node);
			}
			else {
				groots= Collections.EMPTY_SET;
			}
		}
		
		for (Iterator iterator = groots.iterator(); iterator.hasNext();) {
			THGraphNode gnode = (THGraphNode) iterator.next();
			THNode node = createNode(newSelection, null, gnode);
			roots.add(node);
			stack.add(node);
		}
		
		while(!stack.isEmpty()) {
			THNode node= (THNode) stack.remove(stack.size()-1);
			THGraphNode gnode= fGraph.getNode(node.getElement());
			List edges= fwd ? gnode.getOutgoing() : gnode.getIncoming();
			if (edges.isEmpty()) {
				leaves.add(node);
			}
			else {
				for (Iterator iterator = edges.iterator(); iterator.hasNext();) {
					THGraphEdge edge = (THGraphEdge) iterator.next();
					THGraphNode gchildNode= fwd ? edge.getEndNode() : edge.getStartNode();
					THNode childNode= createNode(newSelection, node, gchildNode);
					node.addChild(childNode);
					stack.add(childNode);
				}
			}
		}
		fSelectedTypeNode= newSelection[0];
		if (fSelectedTypeNode == null) {
			fSelectedTypeNode= newSelection[1];
		}
		if (fSelectedTypeNode != null) {
			fTypeToSelect= fSelectedTypeNode.getElement();
			updateSelectedMember();
		}
		
		fRootNodes= (THNode[]) roots.toArray(new THNode[roots.size()]);
		updateImplementors();
	}

	private void updateSelectedMember() {
		ICElement oldSelection= fSelectedMember;
		fSelectedMember= null;
		if (fSelectedTypeNode != null && fMemberSignatureToSelect != null) {
			THGraphNode gnode= fGraph.getNode(fSelectedTypeNode.getElement());
			if (gnode != null) {
				ICElement[] members= gnode.getMembers(fShowInheritedMembers);
				for (int i = 0; i < members.length; i++) {
					ICElement member= members[i];
					if (member.equals(oldSelection)) {
						fSelectedMember= member;
						return;
					}
				}
				for (int i = 0; i < members.length; i++) {
					ICElement member= members[i];
					if (fMemberSignatureToSelect.equals(TypeHierarchyUI.getLocalElementSignature(member))) {
						fSelectedMember= member;
						return;
					}
				}
			}
		}	
	}

	private THNode createNode(THNode[] newSelection, THNode parent, THGraphNode gnode) {
		ICElement element = gnode.getElement();
		THNode node= new THNode(parent, element);
		if (newSelection[0] == null) {
			if (node.equals(fSelectedTypeNode)) {
				newSelection[0]= node;
			}
			else if (newSelection[1] == null) {
				if (element.equals(fTypeToSelect)) {
					newSelection[1]= node;
				}
			}
		}
		return node;
	}

	synchronized private void onJobDone(final THGraph graph, Job job) {
		if (fJob == job) {
			fJob= null;
			fDisplay.asyncExec(new Runnable(){
				public void run() {
					fGraph= graph;
					THGraphNode inputNode= fGraph.getInputNode();
					if (inputNode == null) {
						fView.setMessage(Messages.THHierarchyModel_errorComputingHierarchy);
					}
					else {
						if (fTypeToSelect == fInput) {
							fTypeToSelect= inputNode.getElement();
						}
						fInput= inputNode.getElement();
					}
					computeNodes();
					notifyEvent(END_OF_COMPUTATION);
				}
			});
		}
	}

	private void notifyEvent(int event) {
		fView.onEvent(event);
	}

	synchronized public void refresh() {
		computeGraph();
	}

	public boolean isComputed() {
		return fRootNodes!=null;
	}

	public THNode getSelectionInHierarchy() {
		return fSelectedTypeNode;
	}

	public void onHierarchySelectionChanged(THNode node) {
		fSelectedTypeNode= node;
		if (node != null) {
			fTypeToSelect= node.getElement();
		}
		updateSelectedMember();
		updateImplementors();
	}

	public Object[] getMembers() {
		if (fSelectedTypeNode != null) {
			THGraphNode gnode= fGraph.getNode(fSelectedTypeNode.getElement());
			Object[] result= gnode.getMembers(fShowInheritedMembers);
			if (result != null) {
				return result;
			}
		}
		return NO_CHILDREN;
	}

	public void onMemberSelectionChanged(ICElement elem) {
		fSelectedMember= elem;
		if (fSelectedMember != null) {
			fMemberSignatureToSelect= TypeHierarchyUI.getLocalElementSignature(fSelectedMember);
		}
		updateImplementors();
	}

	private void updateImplementors() {
		if (fRootNodes != null) {
			for (int i = 0; i < fRootNodes.length; i++) {
				THNode node = fRootNodes[i];
				updateImplementors(node);
			}
		}
	}

	private void updateImplementors(THNode node) {
		node.setIsImplementor(isImplementor(node.getElement()));
		THNode[] children= node.getChildren();
		for (int i = 0; i < children.length; i++) {
			THNode child = children[i];
			updateImplementors(child);
		}
	}

	private boolean isImplementor(ICElement element) {
		if (element == null || fSelectedMember == null || fMemberSignatureToSelect == null) {
			return false;
		}
		THGraphNode gnode= fGraph.getNode(element);
		if (gnode != null) {
			ICElement[] members= gnode.getMembers(false);
			if (members != null) {
				for (int i = 0; i < members.length; i++) {
					ICElement member = members[i];
					if (member == fSelectedMember) {
						return true;
					}
					if (fMemberSignatureToSelect.equals(TypeHierarchyUI.getLocalElementSignature(member))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public ICElement getSelectedMember() {
		return fSelectedMember;
	}
}
