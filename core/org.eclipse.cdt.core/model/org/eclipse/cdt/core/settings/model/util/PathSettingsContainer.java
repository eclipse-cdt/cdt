/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class PathSettingsContainer {
	private static final Object INEXISTENT_VALUE = new Object();
//	private static final int INIT_CHILDREN_MAP_CAPACITY = 2;
	
//	private Map fChildrenMap;
//	private Map fPatternMap;
	private PatternNameMap fPatternChildrenMap;
	private Object fValue;
	private IPath fPath;
	private String fName;
	private PathSettingsContainer fRootContainer;
	private PathSettingsContainer fDirectParentContainer;
	private List fListeners;
	
	private boolean fIsPatternMode;
	
	private static final int ADDED = 1;
	private static final int REMOVED = 2;
	private static final int VALUE_CHANGED = 3;
	private static final int PATH_CHANGED = 4;
	

	public static PathSettingsContainer createRootContainer(){
		return createRootContainer(false);
	}

	public static PathSettingsContainer createRootContainer(boolean patternMode){
		return new PathSettingsContainer(patternMode);
	}

	private PathSettingsContainer(boolean pattternMode){
		this(null, null, "", pattternMode); //$NON-NLS-1$
	}
	
	private PathSettingsContainer(PathSettingsContainer root, PathSettingsContainer parent, String name, boolean patternMode){
		fRootContainer = root;
		fDirectParentContainer = parent;
		fName = name;
		fIsPatternMode = patternMode;
		if(fRootContainer == null){
			fRootContainer = this;
//			fPath = new Path(name);
		} else {
			fValue = INEXISTENT_VALUE;
		}
	}
	
	private PatternNameMap getPatternChildrenMap(boolean create){
		if(fPatternChildrenMap == null && create)
			fPatternChildrenMap = new PatternNameMap();
		return fPatternChildrenMap;
	}
	
	private PathSettingsContainer getExacChild(String name, boolean create){
		PatternNameMap pMap = getPatternChildrenMap(create);
		if(pMap != null){
			PathSettingsContainer child = (PathSettingsContainer)pMap.get(name);
			if(child == null && create){
				child = new PathSettingsContainer(fRootContainer, this, name, fIsPatternMode);
				connectChild(child);
			}
			return child;
		}
		return null;
	}
	
	private List getChildren(String name){
		PatternNameMap pMap = getPatternChildrenMap(false);
		if(pMap != null){
			return pMap.getValues(name);
		}
		return null;
	}

	
	private void notifyChange(PathSettingsContainer container, int type, Object oldValue, boolean childrenAffected){
		List list = getListenersList(false);
		if(list != null && list.size() > 0){
			for(Iterator iter = list.iterator(); iter.hasNext();){
				switch(type){
				case ADDED:
					((IPathSettingsContainerListener)iter.next()).containerAdded(container);
					break;
				case REMOVED:
					((IPathSettingsContainerListener)iter.next()).aboutToRemove(container);
					break;
				case VALUE_CHANGED:
					((IPathSettingsContainerListener)iter.next()).containerValueChanged(container, oldValue);
					break;
				case PATH_CHANGED:
					((IPathSettingsContainerListener)iter.next()).containerPathChanged(container, (IPath)oldValue, childrenAffected);
					break;
				}
					
			}
		}
		PathSettingsContainer parent = getParentContainer();
		if(parent != null)
			parent.notifyChange(container, type, oldValue, childrenAffected);
	}
	
	private List getListenersList(boolean create){
		if(fListeners == null && create)
			fListeners = new ArrayList();
		return fListeners;
	}
	
	public boolean hasChildren(){
		PatternNameMap pMap = getPatternChildrenMap(false);
		return pMap != null && pMap.size() != 0;
	}
	
	public PathSettingsContainer getChildContainer(IPath path, boolean create, boolean exactPath){
		PathSettingsContainer container = findContainer(path, create, exactPath);
		if(container != null && container.internalGetValue() == INEXISTENT_VALUE){
			if(create){
				container.internalSetValue(null);
				notifyChange(container, ADDED, null, false);
			} else if(!exactPath){
				for(;
					container.internalGetValue() == INEXISTENT_VALUE;
					container = container.getDirectParentContainer());
			} else if(container.internalGetValue() == INEXISTENT_VALUE){
				container = null;
			}

		}
		return container;
	}

	public PathSettingsContainer[] getChildren(final boolean includeThis){
		final List list = new ArrayList();
		accept(new IPathSettingsContainerVisitor(){

			public boolean visit(PathSettingsContainer container) {
				if(container != PathSettingsContainer.this || includeThis)
					list.add(container);
				return true;
			}
		});

		return (PathSettingsContainer[])list.toArray(new PathSettingsContainer[list.size()]);
	}
	
	public PathSettingsContainer[] getChildrenForPath(IPath path, boolean includePath){
		PathSettingsContainer cr = findContainer(path, false, true);
		if(cr != null)
			return cr.getChildren(includePath);
		return new PathSettingsContainer[0];
	}

	public PathSettingsContainer[] getDirectChildrenForPath(IPath path){
		PathSettingsContainer cr = findContainer(path, false, true);
		if(cr != null)
			return cr.getDirectChildren();
		return new PathSettingsContainer[0];
	}

	public PathSettingsContainer[] getDirectChildren(){
		List list = doGetDirectChildren(null);
		if(list == null || list.size() == 0)
			return new PathSettingsContainer[0];
		return (PathSettingsContainer[])list.toArray(new PathSettingsContainer[list.size()]);
	}
	
	private List doGetDirectChildren(List list){
		PatternNameMap pMap = getPatternChildrenMap(false);
		if(pMap != null){
			if(list == null)
				list = new ArrayList();
			for(Iterator iter = pMap.values().iterator(); iter.hasNext(); ){
				PathSettingsContainer cr = (PathSettingsContainer)iter.next();
				if(cr.fValue == INEXISTENT_VALUE){
					cr.doGetDirectChildren(list);
				} else {
					list.add(cr);
				}
			}
		}
		return list;
	}
	
	public Object[] getValues(final boolean includeThis){
		final List list = new ArrayList();
		accept(new IPathSettingsContainerVisitor(){

			public boolean visit(PathSettingsContainer container) {
				if(container != PathSettingsContainer.this || includeThis)
					list.add(container.getValue());
				return true;
			}
		});

		return list.toArray();
	}

	public PathSettingsContainer getParentContainer(){
		if(fDirectParentContainer != null)
			return fDirectParentContainer.getValidContainer();
		return null;
	}
	
	private PathSettingsContainer getValidContainer(){
		if(internalGetValue() == INEXISTENT_VALUE)
			return getDirectParentContainer().getValidContainer();
		return this;
	}
	
	public Object removeChildContainer(IPath path){
		PathSettingsContainer container = getChildContainer(path, false, true);
		Object value = null;
		if(container != null){
			value = container.getValue();
			container.remove();
		}
		return value;
	}
	
	public void remove(){
		if(!isValid())
			return;

		if(fValue != INEXISTENT_VALUE){
			notifyChange(this, REMOVED, null, false);
			internalSetValue(INEXISTENT_VALUE);
		}
		if(!hasChildren()) {
			getDirectParentContainer().deleteChild(this);
			fDirectParentContainer.checkRemove();
			fDirectParentContainer = null;
			fRootContainer = null;
		}
	}
	
	private void checkRemove(){
		if(fValue == INEXISTENT_VALUE && !hasChildren())
			remove();
	}
	
	private void disconnectChild(PathSettingsContainer child){
		getPatternChildrenMap(true).remove(child.getName());
	}

	private void connectChild(PathSettingsContainer child){
		getPatternChildrenMap(true).put(child.getName(), child);
	}

	public boolean isValid(){
		return fValue != INEXISTENT_VALUE && fRootContainer != null;
	}
	
	public void removeChildren(){
		PatternNameMap pMap = getPatternChildrenMap(false);
		if(pMap == null || pMap.size() == 0)
			return;
		
		
		Collection c = pMap.values();
		PathSettingsContainer childContainers[] = (PathSettingsContainer[])c.toArray(new PathSettingsContainer[c.size()]);
		
		for(int i = 0; i < childContainers.length; i++){
			childContainers[i].removeChildren();
			childContainers[i].remove();
		}
	}
	
	private void deleteChild(PathSettingsContainer child){
		getPatternChildrenMap(false).remove(child.getName());
	}
	
	private String getName(){
		return fName;
	}

	private PathSettingsContainer findContainer(IPath path, boolean create, boolean exactPath){
		PathSettingsContainer container = null;
		if(path.segmentCount() == 0)
			container = this;
		else if (create || exactPath || !fIsPatternMode) {
			PathSettingsContainer child = getExacChild(path.segment(0), create);
			if(child != null)
				container = child.findContainer(path.removeFirstSegments(1), create, exactPath);
			else if(!exactPath)
				container = this;
		} else {
			//looking for container using patterns in read mode (i.e. not creating new container)
			List list = getChildren(path.segment(0));
			if(list != null){
				int size = list.size();
				PathSettingsContainer child, childFound;
				
				for(int i = 0; i < size; i++){
					child = (PathSettingsContainer)list.get(i);
					childFound = child.findContainer(path.removeFirstSegments(1), false, false);
					if(container == null)
						container = childFound;
					else if(container.getPath().segmentCount() < childFound.getPath().segmentCount()){
						container = childFound;
					}
				}
			}
			if(container == null)
				container = this;
		}
		return container;
	}
	
	public void accept(IPathSettingsContainerVisitor visitor){
		doAccept(visitor);
	}
	
	private boolean doAccept(IPathSettingsContainerVisitor visitor){
		if(fValue != INEXISTENT_VALUE && !visitor.visit(this))
			return false;
		
		PatternNameMap pMap = getPatternChildrenMap(false);
		if(pMap != null){
			for(Iterator iter = pMap.values().iterator(); iter.hasNext();){
				PathSettingsContainer child = (PathSettingsContainer)iter.next();
				if(!child.doAccept(visitor))
					return false;
			}
		}
		return true;
	}
	
	
	public IPath getPath(){
		if(fPath == null){
			if(fDirectParentContainer != null)
				fPath = fDirectParentContainer.getPath().append(fName);
			else
				fPath = Path.ROOT;
		}
		return fPath;
	}
	
	public void setPath(IPath path, boolean moveChildren){
		if(path == null || isRoot() || path.equals(getPath()) || path.segmentCount() == 0)
			return;

		IPath oldPath = getPath();
		
		fDirectParentContainer.disconnectChild(this);
		
		if(!moveChildren){
			if(hasChildren()){
				PathSettingsContainer cr = new PathSettingsContainer(fRootContainer, fDirectParentContainer, fName, fIsPatternMode);
				for(Iterator iter = fPatternChildrenMap.values().iterator(); iter.hasNext();){
					PathSettingsContainer child = (PathSettingsContainer)iter.next();
					iter.remove();
					child.setParent(cr);
					cr.connectChild(child);
				}
			}
		} else {
			
		}
		
		PathSettingsContainer newParent = fRootContainer.findContainer(path.removeLastSegments(1), true, true);
		PathSettingsContainer oldParent = fDirectParentContainer;
		fName = path.segment(path.segmentCount()-1);
		fPath = path;

		setParent(newParent);
		newParent.connectChild(this);
		
		oldParent.checkRemove();
		notifyChange(this, PATH_CHANGED, oldPath, moveChildren);
	}
	
	private Object internalGetValue(){
		return fValue;
	}
	
	public boolean isRoot(){
		return fRootContainer == this;
	}
	
	private Object internalSetValue(Object value){
		Object oldValue = fValue;
		fValue = value;
		if(oldValue == INEXISTENT_VALUE){
			oldValue = null;
		} else if(fValue != INEXISTENT_VALUE){
			notifyChange(this, VALUE_CHANGED, oldValue, false);
		}
		return oldValue;
	}
	
	public Object setValue(Object value){
		if(fValue == INEXISTENT_VALUE)
			throw new IllegalStateException();
		return internalSetValue(value);
	}
	
	public Object getValue(){
		if(fValue == INEXISTENT_VALUE)
			throw new IllegalStateException();
		return fValue;
	}
	
	public PathSettingsContainer getRootContainer(){
		return fRootContainer;
	}
	
	private PathSettingsContainer getDirectParentContainer(){
		return fDirectParentContainer;
	}
	
	public void addContainerListener(IPathSettingsContainerListener listenet){
		List list = getListenersList(true);
		list.add(listenet);
	}

	public void removeContainerListener(IPathSettingsContainerListener listenet){
		List list = getListenersList(false);
		if(list != null)
			list.remove(listenet);
	}
	
	private void setParent(PathSettingsContainer parent){
		fDirectParentContainer = parent;
	}
}
