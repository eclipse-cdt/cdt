/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.sourcedependency;

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
	 tempBuffer.append("[FileRef: "); //$NON-NLS-1$
	 tempBuffer.append(fileRef);
	 tempBuffer.append(", Id: "); //$NON-NLS-1$
	 tempBuffer.append(nodeId);	
	 tempBuffer.append("]"); //$NON-NLS-1$
	 return tempBuffer.toString();
	}

}
