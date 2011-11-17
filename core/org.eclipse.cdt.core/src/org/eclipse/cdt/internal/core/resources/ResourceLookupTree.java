/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.resources;

import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Allows for looking up resources by location or name. When using this class 100 bytes per resource
 * are needed. Therefore the support is limited to header-files int non-cdt projects and all files
 * except non-cdt-files in CDT projects.
 *
 * The information for a project is initialized when first requested and then it is kept up to date
 * using a resource change listener. No memory is used, as long as the class is not used.
 * When information is not used for more than 10 minutes, the data-structures will be held via a weak
 * reference, only and are subject to garbage collection.
 *
 * The node map stores a map from hash-code of file-names to nodes.
 * A node contains the name of a file plus a link to the parent resource. From that we can compute
 * the resource path and obtain further information via the resource.
 */
class ResourceLookupTree implements IResourceChangeListener, IResourceDeltaVisitor, IResourceProxyVisitor {
	private static final int UNREF_DELAY = 10 * 60000; // 10 min

	private static final boolean VISIT_CHILDREN = true;
	private static final boolean SKIP_CHILDREN = false;
	private static final IFile[] NO_FILES = new IFile[0];
	private static final int TRIGGER_RECALC=
		IResourceDelta.TYPE | IResourceDelta.REPLACED |
		IResourceDelta.LOCAL_CHANGED | IResourceDelta.OPEN;

	private static class Extensions {
		private final boolean fInvert;
		private final Set<String> fExtensions;
		Extensions(Set<String> extensions, boolean invert) {
			fInvert= invert;
			fExtensions= extensions;
		}
		boolean isRelevant(String filename) {
			// accept all files without extension
			final int idx= filename.lastIndexOf('.');
			if (idx < 0)
				return true;

			return fExtensions.contains(filename.substring(idx+1).toUpperCase()) != fInvert;
		}
	}

	private static class Node {
		final Node fParent;
		final char[] fResourceName;
		final boolean fHasFileLocationName;
		final boolean fIsFileLinkTarget;

		boolean fDeleted;
		boolean fHasChildren;
		int fCanonicHash;

		Node(Node parent, char[] name, boolean hasFileLocationName, boolean isFileLinkTarget) {
			fParent= parent;
			fResourceName= name;
			fHasFileLocationName= hasFileLocationName;
			fIsFileLinkTarget= isFileLinkTarget;
			if (parent != null)
				parent.fHasChildren= true;
		}
	}

	private final Object fLock= new Object();
	private final Job fUnrefJob;
	private SoftReference<Map<Integer, Object>> fNodeMapRef;
	private Map<Integer, Object> fNodeMap;
	private final Map<String, Extensions> fFileExtensions;
	private Extensions fCDTProjectExtensions;
	private Extensions fDefaultExtensions;
	private Extensions fCurrentExtensions;
	private Node fRootNode;
	private boolean fNeedCleanup;
	private Node fLastFolderNode;

	public ResourceLookupTree() {
		fRootNode= new Node(null, CharArrayUtils.EMPTY, false, false) {};
		fFileExtensions= new HashMap<String, Extensions>();
		fUnrefJob= new Job("Timer") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				unrefNodeMap();
				return Status.OK_STATUS;
			}
		};
		fUnrefJob.setSystem(true);
	}

	public void startup() {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	public void shutdown() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		synchronized (fLock) {
			fNodeMap= null;
			fNodeMapRef= null;
			fFileExtensions.clear();
		}
	}

	/**
	 * Handle resource change notifications.
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta= event.getDelta();
		synchronized (fLock) {
			if (fNodeMapRef == null)
				return;
			boolean unsetMap= false;
			if (fNodeMap == null) {
				fNodeMap= fNodeMapRef.get();
				if (fNodeMap == null)
					return;
				unsetMap= true;
			}
			try {
				delta.accept(this);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			} finally {
				if (fNeedCleanup)
					cleanup();
				fCurrentExtensions= null;
				fNeedCleanup= false;
				if (unsetMap)
					fNodeMap= null;
			}
		}
	}

	/**
	 * Handles resource change notifications by visiting the delta.
	 */
	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		assert Thread.holdsLock(fLock);

		final IResource res= delta.getResource();
		if (res instanceof IWorkspaceRoot)
			return VISIT_CHILDREN;

		if (res instanceof IProject) {
			// project not yet handled
			final String name = res.getName();
			final Extensions exts= fFileExtensions.get(name);
			if (exts == null)
				return SKIP_CHILDREN;

			switch (delta.getKind()) {
			case IResourceDelta.ADDED:	// new projects should not yet be part of the tree
			case IResourceDelta.REMOVED:
				fFileExtensions.remove(name);
				remove(res);
				return SKIP_CHILDREN;

			case IResourceDelta.CHANGED:
				if ((delta.getFlags() & (TRIGGER_RECALC | IResourceDelta.DESCRIPTION)) != 0) {
					fFileExtensions.remove(name);
					remove(res);
					return SKIP_CHILDREN;
				}
				break;
			}
			fCurrentExtensions= exts;
			return VISIT_CHILDREN;
		}

		// file or folder
		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			add(res);
			return SKIP_CHILDREN;

		case IResourceDelta.CHANGED:
			if ((delta.getFlags() & TRIGGER_RECALC) != 0) {
				remove(res);
				add(res);
				return SKIP_CHILDREN;
			}
			return VISIT_CHILDREN;

		case IResourceDelta.REMOVED:

			remove(res);
			return SKIP_CHILDREN;
		}
		return VISIT_CHILDREN;
	}


	/**
	 * Add a resource to the tree.
	 */
	private void add(IResource res) {
		assert Thread.holdsLock(fLock);

		if (res instanceof IFile) {
			final String resName = res.getName();
			String linkedName= null;
			if (res.isLinked()) {
				URI uri= res.getLocationURI();
				if (uri != null) {
					linkedName= LocationAdapter.URI.extractName(uri);
					if (linkedName.length() > 0 && fCurrentExtensions.isRelevant(linkedName)) {
						if (linkedName.equals(resName)) {
							createFileNode(res.getFullPath(), null);
						} else {
							createFileNode(res.getFullPath(), linkedName);
						}
					}
				}
			} else if (fCurrentExtensions.isRelevant(resName)) {
				createFileNode(res.getFullPath(), null);
			}
		} else {
			try {
				res.accept(this, 0);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
	}

	/**
	 * Add a resource tree by using a resource proxy visitor.
	 */
	@Override
	public boolean visit(IResourceProxy proxy) throws CoreException {
		if (proxy.getType() == IResource.FILE) {
			if (fCurrentExtensions.isRelevant(proxy.getName())) {
				if (proxy.isLinked()) {
					IResource res= proxy.requestResource();
					if (res instanceof IFile) {
						add(res);
					}
					return true;
				}
				createFileNode(proxy.requestFullPath(), null);
			}
		}
		return true;
	}


	public void unrefNodeMap() {
		synchronized (fLock) {
			fNodeMap= null;
		}
	}

	public void simulateNodeMapCollection() {
		synchronized (fLock) {
			fNodeMap= null;
			fNodeMapRef= new SoftReference<Map<Integer, Object>>(null);
		}
	}

	/**
	 * Initializes nodes for the given projects. Also creates the node map if it was collected.
	 */
	private void initializeProjects(IProject[] projects) {
		assert Thread.holdsLock(fLock);

		if (fNodeMap == null) {
			if (fNodeMapRef != null) {
				fNodeMap= fNodeMapRef.get();
			}

			if (fNodeMap == null) {
				fFileExtensions.clear();
				fNodeMap= new HashMap<Integer, Object>();
				fNodeMapRef= new SoftReference<Map<Integer, Object>>(fNodeMap);
			}
		}
		fUnrefJob.cancel();
		fUnrefJob.schedule(UNREF_DELAY);

		for (IProject project : projects) {
			if (project.isOpen() && !fFileExtensions.containsKey(project.getName())) {
				Extensions ext= fDefaultExtensions;
				try {
					if (project.hasNature(CProjectNature.C_NATURE_ID)) {
						ext= fCDTProjectExtensions;
					}
				} catch (CoreException e) {
					CCorePlugin.log(e);
					// treat as non-cdt project
				}
				fCurrentExtensions= ext;
				add(project);
				fFileExtensions.put(project.getName(), ext);
				fCurrentExtensions= null;
			}
		}
	}

	/**
	 * Initializes file-extensions and node map
	 */
	private void initFileExtensions() {

		if (fDefaultExtensions == null) {
			HashSet<String> cdtContentTypes= new HashSet<String>();
			String[] registeredContentTypes= CoreModel.getRegistedContentTypeIds();
			cdtContentTypes.addAll(Arrays.asList(registeredContentTypes));

			final IContentTypeManager ctm= Platform.getContentTypeManager();
			final IContentType[] ctts= ctm.getAllContentTypes();

			Set<String> cdtExtensions= new HashSet<String>();
			for (IContentType ctt : ctts) {
				IContentType basedOn= ctt;
				while (basedOn != null) {
					if (cdtContentTypes.contains(basedOn.getId())) {
						addFileSpecs(ctt, cdtExtensions);
						break;
					}
					basedOn= basedOn.getBaseType();
				}
			}
			fDefaultExtensions= new Extensions(cdtExtensions, false);

			Set<String> nonCDTExtensions= new HashSet<String>();
			outer: for (IContentType ctt : ctts) {
				IContentType basedOn= ctt;
				while (basedOn != null) {
					if (cdtContentTypes.contains(basedOn.getId()))
						continue outer;
					basedOn= basedOn.getBaseType();
				}
				// this is a non-cdt content type
				addFileSpecs(ctt, nonCDTExtensions);
			}
			// Bug 323659: In case there is another content type for a cdt file-extension we need
			// to remove it.
			nonCDTExtensions.removeAll(cdtExtensions);
			fCDTProjectExtensions= new Extensions(nonCDTExtensions, true);
		}
	}

	private void addFileSpecs(IContentType ctt, Set<String> result) {
		String[] fspecs= ctt.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
		for (String fspec : fspecs) {
			result.add(fspec.toUpperCase());
		}
	}

	/**
	 * Inserts a node for the given path.
	 */
	private void createFileNode(IPath fullPath, String fileLink) {
		final String[] segments= fullPath.segments();
		final boolean isFileLinkTarget= fileLink != null;
		final char[][] charArraySegments = toCharArrayArray(segments, fileLink);
		createNode(charArraySegments, charArraySegments.length, true, isFileLinkTarget);
	}

	private char[][] toCharArrayArray(String[] segments, String fileLink) {
		final int segmentLen = segments.length;
		char[][] chsegs;
		if (fileLink != null) {
			chsegs= new char[segmentLen+1][];
			chsegs[segmentLen]= fileLink.toCharArray();
		} else {
			chsegs= new char[segmentLen][];
		}
		for (int i = 0; i < segmentLen; i++) {
			chsegs[i]= segments[i].toCharArray();
		}
		return chsegs;
	}

	/**
	 * Inserts a node for the given path.
	 */
	private Node createNode(char[][] segments, int segmentCount, boolean hasFileLocationName, boolean isFileLinkTarget) {
		assert Thread.holdsLock(fLock);

		if (segmentCount == 0)
			return fRootNode;

		if (!hasFileLocationName && fLastFolderNode != null) {
			if (isNodeForSegments(fLastFolderNode, segments, segmentCount, isFileLinkTarget))
				return fLastFolderNode;
		}

		final char[] name= segments[segmentCount-1];
		final int hash= hashCode(name);

		// search for existing node
		Object obj= fNodeMap.get(hash);

		Node[] nodes= null;
		int len= 0;
		if (obj != null) {
			if (obj instanceof Node) {
				Node node= (Node) obj;
				if (isNodeForSegments(node, segments, segmentCount, isFileLinkTarget)) {
					if (!hasFileLocationName)
						fLastFolderNode= node;
					return node;
				}
				nodes= new Node[]{node, null};
				fNodeMap.put(hash, nodes);
				len= 1;
			} else {
				nodes= (Node[]) obj;
				for (len=0; len < nodes.length; len++) {
					Node node = nodes[len];
					if (node == null)
						break;
					if (isNodeForSegments(node, segments, segmentCount, isFileLinkTarget)) {
						if (!hasFileLocationName)
							fLastFolderNode= node;
						return node;
					}
				}
			}
		}
		final Node parent= createNode(segments, segmentCount-1, false, false);
		Node node= new Node(parent, name, hasFileLocationName, isFileLinkTarget);
		if (nodes == null) {
			fNodeMap.put(hash, node);
		} else {
			if (len == nodes.length) {
				Node[] newNodes= new Node[len+2];
				System.arraycopy(nodes, 0, newNodes, 0, len);
				nodes= newNodes;
				fNodeMap.put(hash, nodes);
			}
			nodes[len]= node;
		}

		if (!hasFileLocationName)
			fLastFolderNode= node;
		return node;
	}

	/**
	 * Checks whether the given node matches the given segments.
	 */
	private boolean isNodeForSegments(Node node, char[][] segments, int segmentLength, boolean isFileLinkTarget) {
		assert Thread.holdsLock(fLock);

		if (node.fIsFileLinkTarget != isFileLinkTarget)
			return false;

		while(segmentLength > 0 && node != null) {
			if (!CharArrayUtils.equals(segments[--segmentLength], node.fResourceName))
				return false;
			node= node.fParent;
		}
		return node == fRootNode;
	}

	/**
	 * Remove a resource from the tree
	 */
	private void remove(IResource res) {
		assert Thread.holdsLock(fLock);

		final char[] name= res.getName().toCharArray();
		final int hash= hashCode(name);

		Object obj= fNodeMap.get(hash);
		if (obj == null)
			return;

		final IPath fullPath= res.getFullPath();
		final int segmentCount= fullPath.segmentCount();
		if (segmentCount == 0)
			return;

		final char[][]segments= toCharArrayArray(fullPath.segments(), null);
		if (obj instanceof Node) {
			final Node node= (Node) obj;
			if (!node.fDeleted && isNodeForSegments(node, segments, segmentCount, false)) {
				node.fDeleted= true;
				if (node.fHasChildren)
					fNeedCleanup= true;
				fNodeMap.remove(hash);
			}
		} else {
			final Node[] nodes= (Node[]) obj;
			for (int i= 0; i < nodes.length; i++) {
				Node node = nodes[i];
				if (node == null)
					return;
				if (!node.fDeleted && isNodeForSegments(node, segments, segmentCount, false)) {
					remove(nodes, i);

					if (nodes[0] == null)
						fNodeMap.remove(hash);

					node.fDeleted= true;
					if (node.fHasChildren)
						fNeedCleanup= true;

					return;
				}
			}
		}
	}

	private void remove(Node[] nodes, int i) {
    	int idx= lastValid(nodes, i);
    	if (idx > 0) {
    		nodes[i]= nodes[idx];
    		nodes[idx]= null;
    	}
	}

	private int lastValid(Node[] nodes, int left) {
		int right= nodes.length-1;
    	while (left < right) {
    		int mid= (left+right+1)/2;  // ==> mid > left
    		if (nodes[mid] == null)
    			right= mid-1;
    		else
    			left= mid;
    	}
		return right;
	}

	private void cleanup() {
		assert Thread.holdsLock(fLock);
		fLastFolderNode= null;

		for (Iterator<Object> iterator = fNodeMap.values().iterator(); iterator.hasNext();) {
			Object obj= iterator.next();
			if (obj instanceof Node) {
				if (isDeleted((Node) obj)) {
					iterator.remove();
				}
			} else {
				Node[] nodes= (Node[]) obj;
				int j= 0;
				for (int i = 0; i < nodes.length; i++) {
					final Node node = nodes[i];
					if (node == null) {
						if (j==0) {
							iterator.remove();
						}
						break;
					}
					if (!isDeleted(node)) {
						if (i != j) {
							nodes[j]= node;
							nodes[i]= null;
						}
						j++;
					} else {
						nodes[i]= null;
					}
				}
			}
		}
	}

	private boolean isDeleted(Node node) {
		while(node != null) {
			if (node.fDeleted)
				return true;
			node= node.fParent;
		}
		return false;
	}

	/**
	 * Computes a case insensitive hash-code for file names.
	 */
	private int hashCode(char[] name) {
		int h= 0;
		final int len = name.length;
		for (int i = 0; i < len; i++) {
			h = 31*h + Character.toUpperCase(name[i]);
		}
		return h;
	}

	/**
	 * Searches for all files with the given location. In case the name of the location is
	 * a cdt-content type the lookup tree is consulted, otherwise as a fallback the platform's
	 * method is called.
	 */
	public IFile[] findFilesForLocationURI(URI location) {
		return findFilesForLocation(location, LocationAdapter.URI);
	}

	/**
	 * Searches for all files with the given location. In case the name of the location is
	 * a cdt-content type the lookup tree is consulted, otherwise as a fallback the platform's
	 * method is called.
	 */
	public IFile[] findFilesForLocation(IPath location) {
		return findFilesForLocation(location, LocationAdapter.PATH);
	}

	/**
	 * Searches for all files with the given location. In case the name of the location is
	 * a cdt-content type the lookup tree is consulted, otherwise as a fallback the platform's
	 * method is called.
	 */
	public <T> IFile[] findFilesForLocation(T location, LocationAdapter<T> adapter) {
		initFileExtensions();
		String name= adapter.extractName(location);
		Node[] candidates= null;
		synchronized (fLock) {
			initializeProjects(ResourcesPlugin.getWorkspace().getRoot().getProjects());
			Object obj= fNodeMap.get(hashCode(name.toCharArray()));
			if (obj != null) {
				candidates= convert(obj);
				IFile[] result= extractMatchesForLocation(candidates, location, adapter);
				if (result.length > 0)
					return result;
			}
		}

		// fall back to platform functionality
		return adapter.platformsFindFilesForLocation(location);
	}

	private Node[] convert(Object obj) {
		if (obj instanceof Node)
			return new Node[] {(Node) obj};

		final Node[] nodes= (Node[]) obj;
		final int len= lastValid(nodes, -1)+1;
		final Node[] result= new Node[len];
		System.arraycopy(nodes, 0, result, 0, len);
		return result;
	}

	/**
	 * Returns an array of files for the given name. Search is limited to the supplied projects.
	 */
	public IFile[] findFilesByName(IPath relativeLocation, IProject[] projects, boolean ignoreCase) {
		final int segCount= relativeLocation.segmentCount();
		if (segCount < 1)
			return NO_FILES;

		final String name= relativeLocation.lastSegment();
		Node[] candidates;

		initFileExtensions();
		synchronized (fLock) {
			initializeProjects(projects);
			Object obj= fNodeMap.get(hashCode(name.toCharArray()));
			if (obj == null) {
				return NO_FILES;
			}
			candidates= convert(obj);
		}
		String suffix= relativeLocation.toString();
		while(suffix.startsWith("../")) { //$NON-NLS-1$
			suffix= suffix.substring(3);
		}
		Set<String> prjset= new HashSet<String>();
		for (IProject prj : projects) {
			prjset.add(prj.getName());
		}
		return extractMatchesForName(candidates, name, suffix, ignoreCase, prjset);
	}

	/**
	 * Selects the actual matches for the list of candidate nodes.
	 */
	private IFile[] extractMatchesForName(Node[] candidates, String name, String suffix, boolean ignoreCase, Set<String> prjSet) {
		final char[] n1= name.toCharArray();
		final int namelen = n1.length;
		int resultIdx= 0;

		if (ignoreCase) {
			for (int j = 0; j < namelen; j++) {
				n1[j]= Character.toUpperCase(n1[j]);
			}
		}
		final int suffixLen= suffix.length();
		final IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		IFile[] result= null;
		outer: for (int i = 0; i < candidates.length; i++) {
			final Node node = candidates[i];
			if (node.fHasFileLocationName && checkProject(node, prjSet)) {
				final char[] n2= node.fResourceName;
				if (namelen == n2.length) {
					for (int j = 0; j < n2.length; j++) {
						final char c= ignoreCase ? Character.toUpperCase(n2[j]) : n2[j];
						if (c != n1[j])
							continue outer;
					}
					final IFile file= root.getFile(createPath(node));
					final URI loc= file.getLocationURI();
					if (loc != null) {
						String path= loc.getPath();
						final int len= path.length();
						if (len >= suffixLen &&
								suffix.regionMatches(ignoreCase, 0, path, len-suffixLen, suffixLen)) {
							if (result == null)
								result= new IFile[candidates.length-i];
							result[resultIdx++]= root.getFile(createPath(node));
						}
					}
				}
			}
		}
		if (result==null)
			return NO_FILES;

		if (resultIdx < result.length) {
			IFile[] copy= new IFile[resultIdx];
			System.arraycopy(result, 0, copy, 0, resultIdx);
			return copy;
		}
		return result;
	}

	private boolean checkProject(Node node, Set<String> prjSet) {
		while(true) {
			final Node n= node.fParent;
			if (n == fRootNode)
				break;
			if (n == null)
				return false;
			node= n;
		}
		return prjSet.contains(new String(node.fResourceName));
	}

	private IPath createPath(Node node) {
		if (node == fRootNode)
			return Path.ROOT;

		if (node.fIsFileLinkTarget)
			return createPath(node.fParent);

		return createPath(node.fParent).append(new String(node.fResourceName));
	}

	/**
	 * Selects the actual matches from the list of candidates
	 */
	private <T> IFile[] extractMatchesForLocation(Node[] candidates, T location, LocationAdapter<T> adapter) {
		final IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		final String searchPath= adapter.getCanonicalPath(location);
		IFile[] result= null;
		int resultIdx= 0;
		for (int i = 0; i < candidates.length; i++) {
			final Node node = candidates[i];
			if (node.fHasFileLocationName) {
				final IFile file= root.getFile(createPath(node));
				final T loc= adapter.getLocation(file);
				if (loc != null) {
					if (!loc.equals(location)) {
						if (searchPath == null)
							continue;

						if (node.fCanonicHash != 0 && node.fCanonicHash != searchPath.hashCode())
							continue;

						final String candPath= adapter.getCanonicalPath(loc);
						if (candPath == null)
							continue;

						node.fCanonicHash= candPath.hashCode();
						if (!candPath.equals(searchPath))
							continue;
					}
					if (result == null)
						result= new IFile[candidates.length-i];
					result[resultIdx++]= root.getFile(createPath(node));
				}
			}
		}
		if (result==null)
			return NO_FILES;

		if (resultIdx < result.length) {
			IFile[] copy= new IFile[resultIdx];
			System.arraycopy(result, 0, copy, 0, resultIdx);
			return copy;
		}
		return result;
	}

	@SuppressWarnings("nls")
	public void dump() {
		List<String> lines= new ArrayList<String>();
		synchronized (fLock) {
			for (Object object : fNodeMap.values()) {
				Node[] nodes= convert(object);
				for (final Node node : nodes) {
					if (node == null) {
						break;
					}
					lines.add(toString(node));
				}
			}
		}
		Collections.sort(lines);
		System.out.println("Dumping files:");
		for (String line : lines) {
			System.out.println(line);
		}
		System.out.flush();
	}

	@SuppressWarnings("nls")
	private String toString(Node node) {
		if (node == fRootNode)
			return "";

		return toString(node.fParent) + "/" + new String(node.fResourceName);
	}
}
