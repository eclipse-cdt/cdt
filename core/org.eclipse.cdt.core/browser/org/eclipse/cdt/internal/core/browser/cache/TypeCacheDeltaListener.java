/*
 * Created on Apr 20, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.internal.core.browser.cache;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;


/**
 * Listener for changes to CModel.
 * @see org.eclipse.cdt.core.model.IElementChangedListener
 * @since 3.0
 */
public class TypeCacheDeltaListener implements IElementChangedListener {
	
	private TypeCache fTypeCache;
	private TypeCacherJob fTypeCacherJob;
	private Set fPaths= new HashSet(5);
	private Set fPrefixes= new HashSet(5);
	private boolean fFlushAll= false;
	private boolean fCreateBackgroundJob= true;
	
	public TypeCacheDeltaListener(TypeCache cache, boolean createBackgroundJob, TypeCacherJob job) {
		fTypeCache= cache;
		fTypeCacherJob= job;
		fCreateBackgroundJob= createBackgroundJob;
	}
	
	public void setBackgroundJobEnabled(boolean enabled) {
		fCreateBackgroundJob= enabled;
	}
	
	/*
	 * @see IElementChangedListener#elementChanged
	 */
	public void elementChanged(ElementChangedEvent event) {
		fPaths.clear();
		fPrefixes.clear();
		fFlushAll= false;

		boolean needsFlushing= processDelta(event.getDelta());
		if (needsFlushing) {
			// cancel background job
			if (fTypeCacherJob.getState() == Job.RUNNING) {
				// wait for job to finish?
				try {
					fTypeCacherJob.cancel();
					fTypeCacherJob.join();
				} catch (InterruptedException ex) {
				}
			}
			
			if (fFlushAll) {
				// flush the entire cache
				fTypeCacherJob.setSearchPaths(null);
				fTypeCache.flushAll();
			} else {
				// flush affected files from cache
				Set searchPaths= new HashSet(10);
				getPrefixMatches(fPrefixes, searchPaths);
				searchPaths.addAll(fPaths);
				fTypeCacherJob.setSearchPaths(searchPaths);
				fTypeCache.flush(searchPaths);
			}

			// restart the background job
			if (fCreateBackgroundJob) {
				fTypeCacherJob.setPriority(Job.BUILD);
				fTypeCacherJob.schedule();
			}
		}
	}
	
	/*
	 * returns true if the cache needs to be flushed
	 */
	private boolean processDelta(ICElementDelta delta) {
		ICElement elem= delta.getElement();
		int pathEntryChanged= ICElementDelta.F_ADDED_PATHENTRY_SOURCE | ICElementDelta.F_REMOVED_PATHENTRY_SOURCE |
								ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE | ICElementDelta.F_CHANGED_PATHENTRY_MACRO;
		boolean isAddedOrRemoved= (delta.getKind() != ICElementDelta.CHANGED)
		 || ((delta.getFlags() & pathEntryChanged) != 0);
		
		switch (elem.getElementType()) {
			case ICElement.C_MODEL:
			{
				if (isAddedOrRemoved) {
					// CModel has changed
					// flush the entire cache
					fFlushAll= true;
					return true;
				}
				return processDeltaChildren(delta);
			}
			
			case ICElement.C_PROJECT:
			case ICElement.C_CCONTAINER:
			{
				if (isAddedOrRemoved) {
					// project or folder has changed
					// flush all files with matching prefix
					IPath path= elem.getPath();
					if (path != null)
						fPrefixes.add(path);
					return true;
				}
				return processDeltaChildren(delta);
			}
			
			case ICElement.C_NAMESPACE:
			case ICElement.C_TEMPLATE_CLASS:
			case ICElement.C_CLASS:
			case ICElement.C_STRUCT:
			case ICElement.C_UNION:
			case ICElement.C_ENUMERATION:
			case ICElement.C_TYPEDEF:
			case ICElement.C_INCLUDE:
			case ICElement.C_UNIT:
			{
				if (isAddedOrRemoved) {
					// CElement has changed
					// flush file from cache
					IPath path= elem.getPath();
					if (path != null)
						fPaths.add(path);
					return true;
				}
				return processDeltaChildren(delta);
			}
				
			default:
				// fields, methods, imports ect
				return false;
		}	
	}

	private boolean processDeltaChildren(ICElementDelta delta) {
		ICElementDelta[] children= delta.getAffectedChildren();
		for (int i= 0; i < children.length; i++) {
			if (processDelta(children[i])) {
				return true;
			}
		}
		return false;
	}
	
	private boolean getPrefixMatches(Set prefixes, Set results) {
		Set pathSet= fTypeCache.getAllFiles();
		if (pathSet.isEmpty() || prefixes == null || prefixes.isEmpty())
			return false;

		for (Iterator pathIter= pathSet.iterator(); pathIter.hasNext(); ) {
			IPath path= (IPath) pathIter.next();

			// find paths which match prefix
			for (Iterator prefixIter= prefixes.iterator(); prefixIter.hasNext(); ) {
				IPath prefix= (IPath) prefixIter.next();
				if (prefix.isPrefixOf(path)) {
					results.add(path);
					break;
				}
			}
		}

		return !results.isEmpty();
	}
}
