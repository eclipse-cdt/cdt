/*
 * Created on Aug 26, 2003
 */
package org.eclipse.cdt.internal.core.sourcedependency;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * @author bgheorgh
 */
public class DependencySelector {

	/**
	 * 
	 */
	public DependencySelector(
	ICSearchScope searchScope,
	ICElement focus,
	boolean isPolymorphicSearch,
	DependencyManager depManager) {
			this.searchScope = searchScope;
			this.focus = focus;
			this.depManager = depManager;
			this.isPolymorphicSearch = isPolymorphicSearch;
	}
	    ICSearchScope searchScope;
		ICElement focus;
		DependencyManager depManager;
		IPath[] treeKeys; // cache of the keys for looking index up
		boolean isPolymorphicSearch;

		/**
		 * Returns whether elements of the given project can see the given focus (an ICProject) 
		 */
		public static boolean canSeeFocus(ICElement focus, boolean isPolymorphicSearch, IPath projectPath) {
			//TODO: BOG Temp - Provide Proper Impl
			ICModel model = focus.getCModel();
			ICProject project = getCProject(projectPath, model);
			return true;
		}
		/*
		 *  Compute the list of paths which are keying index files.
		 */
		private void initializeIndexKeys() {
		
			ArrayList requiredIndexKeys = new ArrayList();
			IPath[] projects = this.searchScope.enclosingProjects();
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			ICElement projectFocus = this.focus == null ? null : getProject(this.focus);
			for (int i = 0; i < projects.length; i++) {
				IPath location;
				IPath path = projects[i];
				if ((!root.getProject(path.lastSegment()).exists()) // if project does not exist
					&& path.segmentCount() > 1
					&& ((location = root.getFile(path).getLocation()) == null
						|| !new java.io.File(location.toOSString()).exists()) // and internal jar file does not exist
					&& !new java.io.File(path.toOSString()).exists()) { // and external jar file does not exist
						continue;
				}
				if (projectFocus == null || canSeeFocus(projectFocus, this.isPolymorphicSearch, path)) {
					if (requiredIndexKeys.indexOf(path) == -1) {
						requiredIndexKeys.add(path);
					}
				}
			}
			this.treeKeys = new IPath[requiredIndexKeys.size()];
			requiredIndexKeys.toArray(this.treeKeys);
		}
		
		public IDependencyTree[] getIndexes() {
			if (this.treeKeys == null) {
				this.initializeIndexKeys(); 
			}
			// acquire the in-memory indexes on the fly
			int length = this.treeKeys.length;
			IDependencyTree[] indexes = new IDependencyTree[length];
			int count = 0;
			for (int i = 0; i < length; i++){
				// may trigger some index recreation work
				IDependencyTree index = depManager.getDependencyTree(treeKeys[i], true /*reuse index file*/, false /*do not create if none*/);
				if (index != null) indexes[count++] = index; // only consider indexes which are ready yet
			}
			if (count != length) {
				System.arraycopy(indexes, 0, indexes=new IDependencyTree[count], 0, count);
			}
			return indexes;
		}
		/**
		 * Returns the project that corresponds to the given path.
		 * Returns null if the path doesn't correspond to a project.
		 */
		private static ICProject getCProject(IPath path, ICModel model) {
			ICProject project = model.getCProject(path.lastSegment());
			if (project.exists()) {
				return project;
			} else {
				return null;
			}
		}
		public static ICElement getProject(ICElement element) {
			while (!(element instanceof ICProject)) {
				element = element.getParent();
			}
			return element;
		}
}
