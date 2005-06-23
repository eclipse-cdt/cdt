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

package org.eclipse.cdt.internal.core.index.domsourceindexer;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IndexRequest;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public abstract class DOMIndexRequest extends IndexRequest {

		protected DOMSourceIndexer indexer = null;
		
		public DOMIndexRequest(IPath indexPath, DOMSourceIndexer indexer) {
			super(indexPath);
			this.indexer = indexer;
		}

		public void cancel() {
			indexer.jobFinishedNotification( this );
			indexer.jobWasCancelled(this.indexPath);
			this.isCancelled = true;
		}
		
		public boolean isReadyToRun() {
			IProject project = CCorePlugin.getWorkspace().getRoot().getProject(indexPath.segment(0));
			if ( !project.isAccessible() || !this.indexer.isIndexEnabled( project ) )
				return false;
			
			// tag the index as inconsistent
			indexer.aboutToUpdateIndex(indexPath, updatedIndexState());
			return true;
		}
		/*
		 * This code is assumed to be invoked while monitor has read lock
		 */
		protected void saveIfNecessary(IIndex index, ReadWriteMonitor monitor) throws IOException {
			/* if index has changed, commit these before querying */
			if (index.hasChanged()) {
				try {
					monitor.exitRead(); // free read lock
					monitor.enterWrite(); // ask permission to write
					indexer.saveIndex(index);
				} finally {
					monitor.exitWriteEnterRead(); // finished writing and reacquire read permission
				}
			}
		}
		
}
