/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.index;

import java.util.List;

import org.eclipse.core.resources.IProject;

public interface IIndexDelta {
	
	public class IndexDeltaType {
		
		private IndexDeltaType( int value )
		{
			this.value = value;
		}
		private final int value;
	}
	
	public static final IndexDeltaType MERGE_DELTA = new IndexDeltaType( 0 );
	
	public static final IndexDeltaType INDEX_FINISHED_DELTA = new IndexDeltaType( 1 );
	
	/**
	 * @return Returns the files.
	 */
	public List getFiles();
	/**
	 * @return Returns the project.
	 */
	public IProject getProject();
	/**
	 * @return Returns the delta type.
	 */
	public IndexDeltaType getDeltaType();

}
