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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;

import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;

class THGraph {
	private static final ICElement[] NO_MEMBERS = new ICElement[0];
	private THGraphNode fInputNode= null;
	private HashSet fRootNodes= new HashSet();
	private HashSet fLeaveNodes= new HashSet();
	private HashMap fNodes= new HashMap();
	
	public THGraph() {
	}

	public THGraphNode getInputNode() {
		return fInputNode;
	}
	
	public THGraphNode getNode(ICElement elem) {
		return (THGraphNode) fNodes.get(elem);
	}

	private THGraphNode addNode(ICElement input) {
		THGraphNode node= (THGraphNode) fNodes.get(input); 

		if (node == null) {
			node= new THGraphNode(input);
			fNodes.put(input, node);
			fRootNodes.add(node);
			fLeaveNodes.add(node);
		}
		return node;
	}
	
	private THGraphEdge addEdge(THGraphNode from, THGraphNode to) {
		if (createsLoopOrIsDuplicate(from, to)) {
			return null;
		}
		THGraphEdge edge= new THGraphEdge(from, to);
		from.startEdge(edge);
		to.endEdge(edge);
		fRootNodes.remove(to);
		fLeaveNodes.remove(from);
		return edge;
	}

	private boolean createsLoopOrIsDuplicate(THGraphNode from, THGraphNode to) {
		if (from == to) {
			return true;
		}
		if (to.getOutgoing().isEmpty() || from.getIncoming().isEmpty()) {
			return false;
		}
		
		HashSet checked= new HashSet();
		ArrayList stack= new ArrayList();
		stack.add(to);
		
		while (!stack.isEmpty()) {
			THGraphNode node= (THGraphNode) stack.remove(stack.size()-1);
			List out= node.getOutgoing();
			for (Iterator iterator = out.iterator(); iterator.hasNext();) {
				THGraphEdge	edge= (THGraphEdge) iterator.next();
				node= edge.getEndNode();
				if (node == from) {
					return true;
				}
				if (checked.add(node)) {
					stack.add(node);
				}
			}
		}
		// check if edge is already there.
		List out= from.getOutgoing();
		for (Iterator iterator = out.iterator(); iterator.hasNext();) {
			THGraphEdge edge = (THGraphEdge) iterator.next();
			if (edge.getEndNode() == to) {
				return true;
			}
		}
		return false;
	}

	public Collection getRootNodes() {
		return fRootNodes;
	}

	public Collection getLeaveNodes() {
		return fLeaveNodes;
	}

	public void defineInputNode(IIndex index, ICElement input) {
		if (input instanceof ICElementHandle) {
			fInputNode= addNode(input);
		}
		else if (input != null) { 
			try {
				ICElement inputHandle= null;
				IIndexName name= IndexUI.elementToName(index, input);
				if (name != null) {
					inputHandle= IndexUI.getCElementForName(input.getCProject(), index, name);
				} 
				fInputNode= addNode(inputHandle == null ? input : inputHandle);
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			} catch (DOMException e) {
				CUIPlugin.getDefault().log(e);
			}
		}
	}

	public void addSuperClasses(IIndex index, IProgressMonitor monitor) {
		if (fInputNode == null) {
			return;
		}
		HashSet handled= new HashSet();
		ArrayList stack= new ArrayList();
		stack.add(fInputNode.getElement());
		handled.add(fInputNode.getElement());
		while (!stack.isEmpty()) {
			if (monitor.isCanceled()) {
				return;
			}
			ICElement elem= (ICElement) stack.remove(stack.size()-1);
			THGraphNode graphNode= addNode(elem);
			try {
				IBinding binding = IndexUI.elementToBinding(index, elem);
				if (binding != null) {
					addMembers(index, graphNode, binding);
				}
				if (binding instanceof ICPPClassType) {
					ICPPClassType ct= (ICPPClassType) binding;
					ICPPBase[] bases= ct.getBases();
					for (int i = 0; i < bases.length; i++) {
						if (monitor.isCanceled()) {
							return;
						}
						ICPPBase base= bases[i];
						IName name= base.getBaseClassSpecifierName();
						IBinding basecl= name != null ? index.findBinding(name) : base.getBaseClass();
						ICElementHandle[] baseElems= IndexUI.findRepresentative(index, basecl);
						for (int j = 0; j < baseElems.length; j++) {
							ICElementHandle baseElem = baseElems[j];
							THGraphNode baseGraphNode= addNode(baseElem);
							addMembers(index, baseGraphNode, basecl);							
							addEdge(graphNode, baseGraphNode);
							if (handled.add(baseElem)) {
								stack.add(baseElem);
							}
						}
					}
				}
				else if (binding instanceof ITypedef) {
					ITypedef ct= (ITypedef) binding;
					IType type= ct.getType();
					if (type instanceof IBinding) {
						IBinding basecl= (IBinding) type;
						ICElementHandle[] baseElems= IndexUI.findRepresentative(index, basecl);
						if (baseElems.length > 0) {
							ICElementHandle baseElem= baseElems[0];
							THGraphNode baseGraphNode= addNode(baseElem);
							addMembers(index, baseGraphNode, basecl);							
							addEdge(graphNode, baseGraphNode);
							if (handled.add(baseElem)) {
								stack.add(baseElem);
							}
						}
					}
				}
			} catch (DOMException e) {
				CUIPlugin.getDefault().log(e);
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
		}
	}

	public void addSubClasses(IIndex index, IProgressMonitor monitor) {
		if (fInputNode == null) {
			return;
		}
		HashSet handled= new HashSet();
		ArrayList stack= new ArrayList();
		ICElement element = fInputNode.getElement();
		stack.add(element);
		handled.add(element);
		while (!stack.isEmpty()) {
			if (monitor.isCanceled()) {
				return;
			}
			ICElement elem= (ICElement) stack.remove(stack.size()-1);
			THGraphNode graphNode= addNode(elem);
			try {
				IBinding binding = IndexUI.elementToBinding(index, elem);
				if (binding != null) {
					IIndexName[] names= index.findNames(binding, IIndex.FIND_REFERENCES | IIndex.FIND_DEFINITIONS);
					for (int i = 0; i < names.length; i++) {
						if (monitor.isCanceled()) {
							return;
						}
						IIndexName indexName = names[i];
						if (indexName.isBaseSpecifier()) {
							IIndexName subClassDef= indexName.getEnclosingDefinition();
							if (subClassDef != null) {
								IBinding subClass= index.findBinding(subClassDef);
								ICElementHandle[] subClassElems= IndexUI.findRepresentative(index, subClass);
								if (subClassElems.length > 0) {
									ICElementHandle subClassElem= subClassElems[0];
									THGraphNode subGraphNode= addNode(subClassElem);
									addMembers(index, subGraphNode, subClass);							
									addEdge(subGraphNode, graphNode);
									if (handled.add(subClassElem)) {
										stack.add(subClassElem);
									}
								}
							}
						}
					}
				}
			} catch (DOMException e) {
				CUIPlugin.getDefault().log(e);
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
		}
	}
	
	private void addMembers(IIndex index, THGraphNode graphNode, IBinding binding) throws DOMException, CoreException {
		if (graphNode.getMembers(false) == null) {
			ArrayList memberList= new ArrayList();
			if (binding instanceof ICPPClassType) {
				ICPPClassType ct= (ICPPClassType) binding;
				IBinding[] members= ct.getDeclaredFields();
				addMemberElements(index, members, memberList);
				members= ct.getDeclaredMethods();
				addMemberElements(index, members, memberList);
			}
			else if (binding instanceof ICompositeType) {
				ICompositeType ct= (ICompositeType) binding;
				IBinding[] members= ct.getFields();
				addMemberElements(index, members, memberList);
			}
			else if (binding instanceof IEnumeration) {
				IEnumeration ct= (IEnumeration) binding;
				IBinding[] members= ct.getEnumerators();
				addMemberElements(index, members, memberList);
			}
			if (memberList.isEmpty()) {
				graphNode.setMembers(NO_MEMBERS);
			}
			else {
				graphNode.setMembers((ICElement[]) memberList.toArray(new ICElement[memberList.size()]));
			}
		}
	}
	
	private void addMemberElements(IIndex index, IBinding[] members, ArrayList memberList) throws CoreException, DOMException {
		for (int i = 0; i < members.length; i++) {
			IBinding binding = members[i];
			ICElement[] elems= IndexUI.findRepresentative(index, binding);
			if (elems.length > 0) {
				memberList.add(elems[0]);
			}
		}
	}

	public boolean isTrivial() {
		return fNodes.size() < 2;
	}
}
