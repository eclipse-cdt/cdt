/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.util.IPathSettingsContainerVisitor;
import org.eclipse.cdt.core.settings.model.util.PathSettingsContainer;
import org.eclipse.core.runtime.IPath;

public class ResourceDescriptionHolder {
	private PathSettingsContainer fPathSettingContainer;
	private boolean fIncludeCurrent;

	public ResourceDescriptionHolder(PathSettingsContainer pathContainer,
			boolean includeCurrent){
		fPathSettingContainer = pathContainer;
		fIncludeCurrent = includeCurrent;
	}

	public ICResourceDescription getResourceDescription(IPath path, boolean exactPath){
		PathSettingsContainer container = fPathSettingContainer.getChildContainer(path, false, exactPath);
		if(container != null)
			return (ICResourceDescription)container.getValue();
		return null;
	}

	public IPath getCurrentPath(){
		return fPathSettingContainer.getPath();
	}

	public void setCurrentPath(IPath path){
		//TODO: do we need to move children here?
		fPathSettingContainer.setPath(path, true);
	}

	public void addResourceDescription(IPath path, ICResourceDescription des){
		PathSettingsContainer container = fPathSettingContainer.getChildContainer(path, true, true);
		container.setValue(des);
	}

	public ICResourceDescription[] getResourceDescriptions(final int kind){
		final List<ICResourceDescription> list = new ArrayList<ICResourceDescription>();
		fPathSettingContainer.accept(new IPathSettingsContainerVisitor(){

			@Override
			public boolean visit(PathSettingsContainer container) {
				ICResourceDescription des = (ICResourceDescription)container.getValue();
				if((container != fPathSettingContainer || fIncludeCurrent)
						&& des != null && (kind & des.getType()) == des.getType()){
					list.add(des);
				}
				return true;
			}

		});

		if(kind == ICSettingBase.SETTING_FILE)
			return list.toArray(new ICFileDescription[list.size()]);
		else if(kind == ICSettingBase.SETTING_FOLDER)
			return list.toArray(new ICFolderDescription[list.size()]);

		return list.toArray(new ICResourceDescription[list.size()]);
	}

	public ICResourceDescription[] getResourceDescriptions(){
		final List<Object> list = new ArrayList<Object>();
		fPathSettingContainer.accept(new IPathSettingsContainerVisitor(){

			@Override
			public boolean visit(PathSettingsContainer container) {
				list.add(container.getValue());
				return true;
			}

		});
		return list.toArray(new ICResourceDescription[list.size()]);
	}

	public void removeResurceDescription(IPath path){
		fPathSettingContainer.removeChildContainer(path);
	}

	public ICResourceDescription getCurrentResourceDescription(){
		return (ICResourceDescription)fPathSettingContainer.getValue();
	}

	public ICResourceDescription[] getDirectChildren(){
		PathSettingsContainer dc[] = fPathSettingContainer.getDirectChildren();
		ICResourceDescription rcDess[] = new ICResourceDescription[dc.length];

		for(int i = 0; i < dc.length; i++){
			rcDess[i] = (ICResourceDescription)dc[i].getValue();
		}

		return rcDess;
	}

//	public ICSourceEntry[] calculateSourceEntriesFromPaths(IProject project, IPath paths[]){
//		if(paths == null || paths.length == 0)
//			paths = new IPath[]{new Path("")}; //$NON-NLS-1$
//
////		Set set = new HashSet(paths.length);
//		PathSettingsContainer cr = PathSettingsContainer.createRootContainer();
//		IPath pi, pj;
//		List entriesList = new ArrayList(paths.length);
//		IPath projPath = project != null ? project.getFullPath() : null;
//
//		for(int i = 0; i < paths.length; i++){
//			pi = paths[i];
////			set.clear();
//			cr.removeChildren();
//			cr.setValue(null);
//			for(int j = 0; j < paths.length; j++){
//				pj = paths[j];
//				if(pi != pj && pi.isPrefixOf(pj)){
////					set.add(pj);
//					cr.getChildContainer(pj, true, true);
//				}
//			}
//
//			PathSettingsContainer children[] = fPathSettingContainer.getDirectChildrenForPath(pi);
//			for(int k = 0; k < children.length; k++){
//				PathSettingsContainer child = children[k];
//				IPath childPath = child.getPath();
//				PathSettingsContainer parentExclusion = cr.getChildContainer(childPath, false, false);
//				IPath parentExclusionPath = parentExclusion.getPath();
//				if(parentExclusionPath.segmentCount() > 0 && !parentExclusionPath.equals(childPath) && parentExclusionPath.isPrefixOf(childPath))
//					continue;
//
//				ICResourceDescription rcDes = (ICResourceDescription)child.getValue();
//				if(rcDes.isExcluded()){
////					set.add(rcDes.getPath());
//					cr.getChildContainer(childPath, true, true);
//				}
//			}
//
//			PathSettingsContainer exclusions[] = cr.getChildren(false);
////			IPath exlusionPaths[] = new IPath[set.size()];
//			IPath exlusionPaths[] = new IPath[exclusions.length];
////			int k = 0;
//			int segCount = pi.segmentCount();
////			for(Iterator iter = set.iterator(); iter.hasNext(); k++) {
////				IPath path = (IPath)iter.next();
////				exlusionPaths[k] = path.removeFirstSegments(segCount).makeRelative();
////			}
//			for(int k = 0; k < exlusionPaths.length; k++) {
//				exlusionPaths[k] = exclusions[k].getPath().removeFirstSegments(segCount).makeRelative();
//			}
//			if(projPath != null)
//				pi = projPath.append(pi);
//			entriesList.add(new CSourceEntry(pi, exlusionPaths, 0));
//		}
//
//		return (ICSourceEntry[])entriesList.toArray(new ICSourceEntry[entriesList.size()]);
//	}

	public ICFolderDescription getParentFolderDescription(){
		PathSettingsContainer parent = fPathSettingContainer.getParentContainer();
		if(parent != null)
			return (ICFolderDescription)parent.getValue();
		return null;
	}

	public static IPath normalizePath(IPath path){
		return path.makeRelative();
	}
}
