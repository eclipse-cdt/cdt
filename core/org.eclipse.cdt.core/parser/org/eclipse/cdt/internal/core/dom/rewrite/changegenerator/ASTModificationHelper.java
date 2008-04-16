/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.changegenerator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationMap;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ContainerNode;

public class ASTModificationHelper {

	private final ASTModificationStore modificationStore;

	public ASTModificationHelper(ASTModificationStore modificationStore) {
		this.modificationStore = modificationStore;
	}

	
	public <T extends IASTNode> T[] createModifiedChildArray(IASTNode parent, T[] unmodifiedChildren, Class<T> clazz){
		ArrayList<T> modifiedChildren = new ArrayList<T>(Arrays.asList(unmodifiedChildren));
		for(T currentChild : unmodifiedChildren){
			for(ASTModification childModification : modificationsForNode(currentChild)){
				try{
					final T newNode = cast(childModification.getNewNode(), clazz);
					switch(childModification.getKind()){
					case REPLACE:
						if (newNode != null) {
							modifiedChildren.add(modifiedChildren.indexOf(childModification.getTargetNode()), newNode);
						}
						modifiedChildren.remove(childModification.getTargetNode());
						break;
					case INSERT_BEFORE:
						if (newNode != null) {
							modifiedChildren.add(modifiedChildren.indexOf(childModification.getTargetNode()), newNode);
						}
						break;
					case APPEND_CHILD:
						throw new UnhandledASTModificationException(childModification);
	
					}
				}catch(ClassCastException e){
					throw new UnhandledASTModificationException(childModification);
				}
			} 
		}

		for(ASTModification parentModification : modificationsForNode(parent)){
			if(parentModification.getKind() == ModificationKind.APPEND_CHILD){
				IASTNode newNode = parentModification.getNewNode();
				T newTNode= cast(newNode, clazz);
				if (newTNode != null) {
					modifiedChildren.add(newTNode);
				}
				else if (newNode instanceof ContainerNode){
					ContainerNode nodeContainer = (ContainerNode) newNode;
					for(IASTNode currentNode : nodeContainer.getNodes()){
						T tnode= cast(currentNode, clazz);
						if(tnode != null){
							modifiedChildren.add(tnode);
						}
					}
				}
			}
		}
		
		return modifiedChildren.toArray(newArrayInstance(clazz, modifiedChildren.size()));
	}


	@SuppressWarnings("unchecked")
	private <T> T[] newArrayInstance(Class<T> clazz, int size) {
		return (T[]) Array.newInstance(clazz, size);
	}

	@SuppressWarnings("unchecked")
	private <T> T cast(IASTNode node, Class<T> clazz) {
		if (clazz.isInstance(node)){
			return (T) node;
		}
		return null;
	}


	public List<ASTModification> modificationsForNode(
			IASTNode targetNode) {
		ASTModificationMap rootModifications = modificationStore.getRootModifications();
		if(rootModifications == null){
			rootModifications = new ASTModificationMap();
		}
		List<ASTModification> modificationsForNode = rootModifications.getModificationsForNode(targetNode);
		return modificationsForNode;
	}
	
	
	public IASTInitializer getInitializer(IASTDeclarator decl) {
		IASTInitializer initializer = decl.getInitializer();
		
		if(initializer != null){
			for(ASTModification childModification : modificationsForNode(initializer)){
				switch(childModification.getKind()){
				case REPLACE:
					if(childModification.getNewNode() instanceof IASTInitializer){
						return (IASTInitializer)childModification.getNewNode();
					}
					throw new UnhandledASTModificationException(childModification);
					
				case INSERT_BEFORE:
					throw new UnhandledASTModificationException(childModification);
					
				case APPEND_CHILD:
					throw new UnhandledASTModificationException(childModification);
				}
			}
		}
		else
		{
			for(ASTModification parentModification : modificationsForNode(decl)){
				if(parentModification.getKind() == ModificationKind.APPEND_CHILD){
					IASTNode newNode = parentModification.getNewNode();
					if(newNode instanceof IASTInitializer){
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
		for(ASTModification currentModification : modifications){
			try{
				if(currentModification.getKind() == ModificationKind.REPLACE){
					return (T) currentModification.getNewNode();
				}
			}
			catch(ClassCastException e){
				throw new UnhandledASTModificationException(currentModification);
			}
		}
		return replacedNode;
	}
}
