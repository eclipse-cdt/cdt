/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.sourcedependency.impl;

/**
 * @author bgheorgh
 */
public class Node {

	int fileRef;
	int nodeId;
	
	public Node(int file, int id){
		this.fileRef = file;
		this.nodeId = id;
	}
	
	public int getFileRef(){
		return fileRef;
	}
	
	public int getNodeId(){
		return nodeId;
	}
	
	public String toString() {
	 StringBuffer tempBuffer = new StringBuffer();
	 tempBuffer.append("[FileRef: ");
	 tempBuffer.append(fileRef);
	 tempBuffer.append(", Id: ");
	 tempBuffer.append(nodeId);	
	 tempBuffer.append("]");
	 return tempBuffer.toString();
	}

}
