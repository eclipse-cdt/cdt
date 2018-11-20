/*******************************************************************************
 * Copyright (c) 2007, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.typehierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

class THGraph {
	private static final ICElement[] NO_MEMBERS = {};
	private THGraphNode fInputNode;
	private HashSet<THGraphNode> fRootNodes = new HashSet<>();
	private HashSet<THGraphNode> fLeafNodes = new HashSet<>();
	private HashMap<ICElement, THGraphNode> fNodes = new HashMap<>();
	private boolean fFileIsIndexed;

	public THGraph() {
	}

	public THGraphNode getInputNode() {
		return fInputNode;
	}

	public THGraphNode getNode(ICElement elem) {
		return fNodes.get(elem);
	}

	private THGraphNode addNode(ICElement input) {
		THGraphNode node = fNodes.get(input);

		if (node == null) {
			node = new THGraphNode(input);
			fNodes.put(input, node);
			fRootNodes.add(node);
			fLeafNodes.add(node);
		}
		return node;
	}

	private THGraphEdge addEdge(THGraphNode from, THGraphNode to) {
		if (createsLoopOrIsDuplicate(from, to)) {
			return null;
		}
		THGraphEdge edge = new THGraphEdge(from, to);
		from.startEdge(edge);
		to.endEdge(edge);
		fRootNodes.remove(to);
		fLeafNodes.remove(from);
		return edge;
	}

	private boolean createsLoopOrIsDuplicate(THGraphNode from, THGraphNode to) {
		if (from == to) {
			return true;
		}
		if (to.getOutgoing().isEmpty() || from.getIncoming().isEmpty()) {
			return false;
		}

		HashSet<THGraphNode> checked = new HashSet<>();
		ArrayList<THGraphNode> stack = new ArrayList<>();
		stack.add(to);

		while (!stack.isEmpty()) {
			THGraphNode node = stack.remove(stack.size() - 1);
			List<THGraphEdge> out = node.getOutgoing();
			for (THGraphEdge edge : out) {
				node = edge.getEndNode();
				if (node == from) {
					return true;
				}
				if (checked.add(node)) {
					stack.add(node);
				}
			}
		}
		// Check if edge is already there.
		List<THGraphEdge> out = from.getOutgoing();
		for (THGraphEdge edge : out) {
			if (edge.getEndNode() == to) {
				return true;
			}
		}
		return false;
	}

	public Collection<THGraphNode> getRootNodes() {
		return fRootNodes;
	}

	public Collection<THGraphNode> getLeafNodes() {
		return fLeafNodes;
	}

	public void defineInputNode(IIndex index, ICElement input) {
		if (input != null) {
			try {
				if (IndexUI.isIndexed(index, input)) {
					fFileIsIndexed = true;
					input = IndexUI.attemptConvertionToHandle(index, input);
					fInputNode = addNode(input);
				}
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		}
	}

	public void addSuperClasses(IIndex index, IProgressMonitor monitor) {
		if (fInputNode == null) {
			return;
		}
		HashSet<ICElement> handled = new HashSet<>();
		ArrayList<ICElement> stack = new ArrayList<>();
		stack.add(fInputNode.getElement());
		handled.add(fInputNode.getElement());
		while (!stack.isEmpty()) {
			if (monitor.isCanceled()) {
				return;
			}
			ICElement elem = stack.remove(stack.size() - 1);
			THGraphNode graphNode = addNode(elem);
			try {
				IIndexBinding binding = IndexUI.elementToBinding(index, elem);
				if (binding != null) {
					addMembers(index, graphNode, binding);
				}
				if (binding instanceof ICPPClassType) {
					ICPPClassType ct = (ICPPClassType) binding;
					ICPPBase[] bases = ct.getBases();
					for (ICPPBase base : bases) {
						if (monitor.isCanceled()) {
							return;
						}
						IType baseType = base.getBaseClassType();
						if (baseType instanceof IBinding) {
							final IBinding baseBinding = (IBinding) baseType;
							ICElementHandle[] baseElems = IndexUI.findRepresentative(index, baseBinding);
							for (ICElementHandle baseElem : baseElems) {
								THGraphNode baseGraphNode = addNode(baseElem);
								addMembers(index, baseGraphNode, baseBinding);
								addEdge(graphNode, baseGraphNode);
								if (handled.add(baseElem)) {
									stack.add(baseElem);
								}
							}
						}
					}
				} else if (binding instanceof ITypedef) {
					ITypedef ct = (ITypedef) binding;
					IType type = ct.getType();
					if (type instanceof IBinding) {
						IBinding basecl = (IBinding) type;
						ICElementHandle[] baseElems = IndexUI.findRepresentative(index, basecl);
						if (baseElems.length > 0) {
							ICElementHandle baseElem = baseElems[0];
							THGraphNode baseGraphNode = addNode(baseElem);
							addMembers(index, baseGraphNode, basecl);
							addEdge(graphNode, baseGraphNode);
							if (handled.add(baseElem)) {
								stack.add(baseElem);
							}
						}
					}
				}
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		}
	}

	public void addSubClasses(IIndex index, IProgressMonitor monitor) {
		if (fInputNode == null) {
			return;
		}
		HashSet<ICElement> handled = new HashSet<>();
		ArrayList<ICElement> stack = new ArrayList<>();
		ICElement element = fInputNode.getElement();
		stack.add(element);
		handled.add(element);
		while (!stack.isEmpty()) {
			if (monitor.isCanceled()) {
				return;
			}
			ICElement elem = stack.remove(stack.size() - 1);
			THGraphNode graphNode = addNode(elem);
			try {
				IBinding binding = IndexUI.elementToBinding(index, elem);
				if (binding != null) {
					// TODO(nathanridge): Also find subclasses referenced via decltype-specifiers rather than names.
					IIndexName[] names = index.findNames(binding, IIndex.FIND_REFERENCES | IIndex.FIND_DEFINITIONS);
					for (IIndexName indexName : names) {
						if (monitor.isCanceled()) {
							return;
						}
						if (indexName.isBaseSpecifier()) {
							IIndexName subClassDef = indexName.getEnclosingDefinition();
							if (subClassDef != null) {
								IBinding subClass = index.findBinding(subClassDef);
								ICElementHandle[] subClassElems = IndexUI.findRepresentative(index, subClass);
								if (subClassElems.length > 0) {
									ICElementHandle subClassElem = subClassElems[0];
									THGraphNode subGraphNode = addNode(subClassElem);
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
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		}
	}

	private void addMembers(IIndex index, THGraphNode graphNode, IBinding binding) throws CoreException {
		if (graphNode.getMembers(false) == null) {
			ArrayList<ICElement> memberList = new ArrayList<>();
			if (binding instanceof ICPPClassType) {
				ICPPClassType ct = (ICPPClassType) binding;
				IBinding[] members = ct.getDeclaredFields();
				addMemberElements(index, members, memberList);
				members = ct.getDeclaredMethods();
				addMemberElements(index, members, memberList);
			} else if (binding instanceof ICompositeType) {
				ICompositeType ct = (ICompositeType) binding;
				IBinding[] members = ct.getFields();
				addMemberElements(index, members, memberList);
			} else if (binding instanceof IEnumeration) {
				IEnumeration ct = (IEnumeration) binding;
				IBinding[] members = ct.getEnumerators();
				addMemberElements(index, members, memberList);
			}

			if (memberList.isEmpty()) {
				graphNode.setMembers(NO_MEMBERS);
			} else {
				graphNode.setMembers(memberList.toArray(new ICElement[memberList.size()]));
			}
		}
	}

	private void addMemberElements(IIndex index, IBinding[] members, List<ICElement> memberList) throws CoreException {
		for (IBinding binding : members) {
			ICElement[] elems = IndexUI.findRepresentative(index, binding);
			if (elems.length > 0) {
				memberList.add(elems[0]);
			}
		}
	}

	public boolean isTrivial() {
		return fNodes.size() <= 1;
	}

	public boolean isFileIndexed() {
		return fFileIsIndexed;
	}
}
