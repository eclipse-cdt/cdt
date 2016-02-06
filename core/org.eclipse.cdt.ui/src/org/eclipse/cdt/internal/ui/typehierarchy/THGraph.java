/*******************************************************************************
 * Copyright (c) 2007, 2014 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;

import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;

class THGraph {
	private static final ICElement[] NO_MEMBERS = {};
	private THGraphNode fInputNode;
	private HashSet<THGraphNode> fRootNodes= new HashSet<>();
	private HashSet<THGraphNode> fLeafNodes= new HashSet<>();
	private HashMap<ICElement, THGraphNode> fNodes= new HashMap<>();
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
		THGraphEdge edge= new THGraphEdge(from, to);
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
		
		HashSet<THGraphNode> checked= new HashSet<>();
		ArrayList<THGraphNode> stack= new ArrayList<>();
		stack.add(to);
		
		while (!stack.isEmpty()) {
			THGraphNode node= stack.remove(stack.size() - 1);
			List<THGraphEdge> out= node.getOutgoing();
			for (THGraphEdge edge : out) {
				node= edge.getEndNode();
				if (node == from) {
					return true;
				}
				if (checked.add(node)) {
					stack.add(node);
				}
			}
		}
		// Check if edge is already there.
		List<THGraphEdge> out= from.getOutgoing();
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
					fFileIsIndexed= true;
					input= IndexUI.attemptConvertionToHandle(index, input);
					fInputNode= addNode(input);
				}
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		}
	}

	private void addSuperClasses(THGraphNode graphNode, IBinding subClass, IIndex index, IProgressMonitor monitor,
 int depth) {
		if (depth > CPPSemantics.MAX_INHERITANCE_DEPTH || monitor.isCanceled())
			return;
		if (subClass instanceof ICPPClassType) {
			try {
				addMembers(index, graphNode, subClass);
				ICPPBase[] bases = ((ICPPClassType) subClass).getBases();
				for (ICPPBase base : bases) {
					IType baseType = base.getBaseClassType();

					if (baseType instanceof ICPPTemplateTypeParameter) {
						if (baseType instanceof ICPPTemplateInstance) {
							ICPPTemplateArgument[] args = ((ICPPTemplateInstance) subClass)
									.getTemplateArguments();
							baseType = args[((ICPPTemplateTypeParameter) baseType).getParameterID()]
									.getTypeValue();
						}
					} else if (baseType instanceof ICPPDeferredClassInstance) {
						if (subClass instanceof ICPPTemplateInstance) {
							ICPPTemplateArgument[] args = ((ICPPTemplateInstance) subClass)
									.getTemplateArguments();
							baseType = (IType) CPPTemplates.instantiate(
									((ICPPDeferredClassInstance) baseType).getClassTemplate(), args, null);
						} else if (baseType instanceof ICPPAliasTemplateInstance) {
							// TODO how can we depict the template
							// parameter arguments?
						}
					}

					if (baseType instanceof IBinding) {
						IBinding baseBinding = (IBinding) baseType;
						try {
							ICElementHandle[] baseElems = IndexUI.findRepresentative(index, baseBinding);
							for (ICElementHandle baseElem : baseElems) {
								THGraphNode baseGraphNode = addNode(baseElem);
								addEdge(graphNode, baseGraphNode);
								addSuperClasses(baseGraphNode, baseBinding, index, monitor, depth + 1);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (subClass instanceof IEnumeration) {
			try {
				addMembers(index, graphNode, subClass);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (subClass instanceof IType) {
			IType resolved = SemanticUtil.getSimplifiedType((IType) subClass);
			if (resolved != subClass && resolved instanceof IBinding) {
				IBinding baseBinding = (IBinding) resolved;
				try {
					ICElementHandle[] baseElems = IndexUI.findRepresentative(index, baseBinding);
					for (ICElementHandle baseElem : baseElems) {
						THGraphNode baseGraphNode = addNode(baseElem);
						addEdge(graphNode, baseGraphNode);
						addSuperClasses(baseGraphNode, baseBinding, index, monitor, depth + 1);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void addSubClasses(THGraphNode graphNode, IBinding superCLass, IIndex index, IProgressMonitor monitor,
			int depth) {
		if (depth > CPPSemantics.MAX_INHERITANCE_DEPTH || monitor.isCanceled())
			return;
		if (superCLass != null) {
			// TODO(nathanridge): Also find subclasses referenced via
			// decltype-specifiers rather than names.
			try {
			IIndexName[] names = index.findNames(superCLass,
						IIndex.FIND_REFERENCES | IIndex.FIND_DEFINITIONS);
				for (IIndexName indexName : names) {
					if (monitor.isCanceled()) {
						return;
					}
					try {
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
									addSubClasses(subGraphNode, subClass, index, monitor, depth + 1);
								}
							}
						}
					} catch (CoreException e) {
						CUIPlugin.log(e);
					}
				}
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		}
	}
	
	public void addClassNodes(IIndex index, IProgressMonitor monitor) {
		if (fInputNode == null) {
			return;
		}
		ICElement elem = fInputNode.getElement();
		try {
			IBinding binding = IndexUI.elementToBinding(index, elem);
			addSuperClasses(fInputNode, binding, index, monitor, 0);
			addSubClasses(fInputNode, binding, index, monitor, 0);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addMembers(IIndex index, THGraphNode graphNode, IBinding binding) throws CoreException {
		if (graphNode.getMembers(false) == null) {
			ArrayList<ICElement> memberList= new ArrayList<>();
			if (binding instanceof ICPPClassType) {
				ICPPClassType ct= (ICPPClassType) binding;
				IBinding[] members= ClassTypeHelper.getDeclaredFields(ct, null);
				addMemberElements(index, members, memberList);
				members= ClassTypeHelper.getDeclaredMethods(ct, null);
				addMemberElements(index, members, memberList);
			} else if (binding instanceof ICompositeType) {
				ICompositeType ct= (ICompositeType) binding;
				IBinding[] members= ct.getFields();
				addMemberElements(index, members, memberList);
			} else if (binding instanceof IEnumeration) {
				IEnumeration ct= (IEnumeration) binding;
				IBinding[] members= ct.getEnumerators();
				addMemberElements(index, members, memberList);
			}

			if (memberList.isEmpty()) {
				graphNode.setMembers(NO_MEMBERS);
			} else {
				graphNode.setMembers(memberList.toArray(new ICElement[memberList.size()]));
			}
		}
	}
	
	private void addMemberElements(IIndex index, IBinding[] members, List<ICElement> memberList) 
			throws CoreException {
		for (IBinding binding : members) {
			ICElement[] elems= IndexUI.findRepresentative(index, binding);
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
