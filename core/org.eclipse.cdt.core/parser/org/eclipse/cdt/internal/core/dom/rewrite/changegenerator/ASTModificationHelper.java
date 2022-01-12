/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.changegenerator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ContainerNode;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

public class ASTModificationHelper {
	private final ModificationScopeStack modificationStore;

	public ASTModificationHelper(ModificationScopeStack stack) {
		this.modificationStore = stack;
	}

	public <T extends IASTNode> T[] createModifiedChildArray(IASTNode parent, T[] unmodifiedChildren, Class<T> clazz,
			NodeCommentMap commentMap) {
		ArrayList<T> modifiedChildren = new ArrayList<>(Arrays.asList(unmodifiedChildren));

		for (ASTModification parentModification : modificationsForNode(parent)) {
			switch (parentModification.getKind()) {
			case APPEND_CHILD:
				IASTNode newNode = parentModification.getNewNode();
				T appendedTNode = cast(newNode, clazz);
				if (appendedTNode != null) {
					modifiedChildren.add(appendedTNode);
				} else if (newNode instanceof ContainerNode) {
					ContainerNode nodeContainer = (ContainerNode) newNode;
					for (IASTNode currentNode : nodeContainer.getNodes()) {
						T tnode = cast(currentNode, clazz);
						if (tnode != null) {
							modifiedChildren.add(tnode);
						}
					}
				}
				break;
			case INSERT_BEFORE:
			case REPLACE:
				break;
			}
		}

		for (T currentChild : unmodifiedChildren) {
			for (ASTModification childModification : modificationsForNode(currentChild)) {
				try {
					IASTNode newNode = childModification.getNewNode();
					final T castedNewNode = cast(newNode, clazz);
					switch (childModification.getKind()) {
					case REPLACE:
						if (castedNewNode != null) {
							copyComments(castedNewNode, currentChild, commentMap);
							modifiedChildren.add(modifiedChildren.indexOf(childModification.getTargetNode()),
									castedNewNode);
						}
						modifiedChildren.remove(childModification.getTargetNode());
						break;
					case INSERT_BEFORE:
						if (castedNewNode != null) {
							modifiedChildren.add(modifiedChildren.indexOf(childModification.getTargetNode()),
									castedNewNode);
						} else if (newNode instanceof ContainerNode) {
							ContainerNode nodeContainer = (ContainerNode) newNode;
							for (IASTNode currentNode : nodeContainer.getNodes()) {
								T tnode = cast(currentNode, clazz);
								if (tnode != null) {
									modifiedChildren.add(modifiedChildren.indexOf(childModification.getTargetNode()),
											tnode);
								}
							}
						}
						break;
					case APPEND_CHILD:
						throw new UnhandledASTModificationException(childModification);
					}
				} catch (ClassCastException e) {
					throw new UnhandledASTModificationException(childModification);
				}
			}
		}
		return modifiedChildren.toArray(newArrayInstance(clazz, modifiedChildren.size()));
	}

	private void copyComments(IASTNode newNode, IASTNode oldNode, NodeCommentMap commentMap) {
		// Attach all the comments that is attached to oldNode to newNode
		List<IASTComment> leadingComments = commentMap.getLeadingCommentsForNode(oldNode);
		for (IASTComment comment : leadingComments) {
			commentMap.addLeadingCommentToNode(newNode, comment);
		}

		List<IASTComment> trailingComments = commentMap.getTrailingCommentsForNode(oldNode);
		for (IASTComment comment : trailingComments) {
			commentMap.addTrailingCommentToNode(newNode, comment);
		}

		List<IASTComment> freestandingComments = commentMap.getFreestandingCommentsForNode(oldNode);
		for (IASTComment comment : freestandingComments) {
			commentMap.addFreestandingCommentToNode(newNode, comment);
		}

		// Detach comments from oldNode (to avoid memory leak)
		Map<IASTNode, List<IASTComment>> leadingMap = commentMap.getLeadingMap();
		leadingMap.remove(oldNode);

		Map<IASTNode, List<IASTComment>> trailingMap = commentMap.getTrailingMap();
		trailingMap.remove(oldNode);

		Map<IASTNode, List<IASTComment>> freestandingMap = commentMap.getFreestandingMap();
		freestandingMap.remove(oldNode);
	}

	@SuppressWarnings("unchecked")
	private <T> T[] newArrayInstance(Class<T> clazz, int size) {
		return (T[]) Array.newInstance(clazz, size);
	}

	@SuppressWarnings("unchecked")
	private <T> T cast(IASTNode node, Class<T> clazz) {
		if (clazz.isInstance(node)) {
			return (T) node;
		}
		return null;
	}

	public List<ASTModification> modificationsForNode(IASTNode targetNode) {
		List<ASTModification> modificationsForNode;
		if (modificationStore.getModifiedNodes().contains(targetNode)) {
			modificationsForNode = modificationStore.getModificationsForNode(targetNode);
		} else {
			modificationsForNode = Collections.emptyList();
		}
		return modificationsForNode;
	}

	public IASTInitializer getInitializer(IASTDeclarator decl) {
		IASTInitializer initializer = decl.getInitializer();

		if (initializer != null) {
			for (ASTModification childModification : modificationsForNode(initializer)) {
				switch (childModification.getKind()) {
				case REPLACE:
					if (childModification.getNewNode() instanceof IASTInitializer) {
						return (IASTInitializer) childModification.getNewNode();
					} else if (childModification.getNewNode() == null) {
						return null;
					}
					throw new UnhandledASTModificationException(childModification);
				case INSERT_BEFORE:
					throw new UnhandledASTModificationException(childModification);

				case APPEND_CHILD:
					throw new UnhandledASTModificationException(childModification);
				}
			}
		} else {
			for (ASTModification parentModification : modificationsForNode(decl)) {
				if (parentModification.getKind() == ModificationKind.APPEND_CHILD) {
					IASTNode newNode = parentModification.getNewNode();
					if (newNode instanceof IASTInitializer) {
						return (IASTInitializer) newNode;
					}
				}
			}
		}
		return initializer;
	}

	@SuppressWarnings("unchecked")
	public <T extends IASTNode> T getNodeAfterReplacement(T replacedNode) {
		List<ASTModification> modifications = modificationsForNode(replacedNode);
		for (ASTModification currentModification : modifications) {
			try {
				if (currentModification.getKind() == ModificationKind.REPLACE) {
					return (T) currentModification.getNewNode();
				}
			} catch (ClassCastException e) {
				throw new UnhandledASTModificationException(currentModification);
			}
		}
		return replacedNode;
	}
}
