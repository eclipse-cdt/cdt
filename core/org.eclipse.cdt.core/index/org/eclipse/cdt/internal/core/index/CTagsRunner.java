package org.eclipse.cdt.internal.core.index;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.index.ITagEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class CTagsRunner implements Runnable {

	IndexManager manager;

	public CTagsRunner(IndexManager indexManager) {
		manager = indexManager;
	}

	public void run () {
		// It should be ok since we are being started on startup
		// the delay should allow things to settle down. 
		manager.delay();
		while (true) {
			IResource resource = null;
			RequestList requestList = manager.getRequestList();
			try {
				resource = requestList.removeItem();
			} catch (Exception e) {
				//e.printStackTrace();
			}

			if (resource != null) {
				switch (resource.getType()) {
					case IResource.FILE:
						IProject project = resource.getProject();
						Map projectsMap = manager.getProjectsMap();
						Map filesMap = (Map)projectsMap.get(project.getLocation());
						if (filesMap == null) {
							filesMap = Collections.synchronizedMap(new HashMap());
							projectsMap.put(project.getLocation(), filesMap);
						}
						
						try {
							CTagsCmd cmd = new CTagsCmd();
							IFile file = (IFile)resource;
							IPath path = file.getLocation();
							if (path != null) {
								ITagEntry[] entries = cmd.getTagEntries(file, path.toOSString());
								filesMap.put(resource.getLocation(), entries);
							}
						} catch (IOException e) {
						}
						//System.out.println("indexing " + resource.getName());
					break;

					case IResource.FOLDER:
					case IResource.PROJECT:
						System.out.println("Can not index folders " + resource.getName());
					break;
				}
			}
			//System.out.println("Indexing " + filename);
		}
	}
}
