/*
 * Created on Sep 5, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.internal.core.sourcedependency;

import java.io.IOException;

import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author bgheorgh
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class RemoveFromDependencyTree extends DependencyRequest {
	String resourceName;

		public RemoveFromDependencyTree(String resourceName, IPath dependencyTreePath, DependencyManager manager) {
			super(dependencyTreePath, manager);
			this.resourceName = resourceName;
		}
	
		public boolean execute(IProgressMonitor progressMonitor) {

			if (progressMonitor != null && progressMonitor.isCanceled()) return true;

			/* ensure no concurrent write access to index */
			IDependencyTree depTree = manager.getDependencyTree(this.dependencyTreePath, true, /*reuse index file*/ false /*create if none*/);
			if (depTree == null) return true;
			ReadWriteMonitor monitor = manager.getMonitorFor(depTree);
			if (monitor == null) return true; // index got deleted since acquired

			try {
				monitor.enterWrite(); // ask permission to write
				depTree.remove(resourceName);
			} catch (IOException e) {
				if (DependencyManager.VERBOSE) {
					JobManager.verbose("-> failed to remove " + this.resourceName + " from index because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
					e.printStackTrace();
				}
				return false;
			} finally {
				monitor.exitWrite(); // free write lock
			}
			return true;
		}
	
		public String toString() {
			return "removing " + this.resourceName + " from dep Tree " + this.dependencyTreePath; //$NON-NLS-1$ //$NON-NLS-2$
		}
}
