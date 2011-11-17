/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public final class PathSettingsContainer {
	private static final Object INEXISTENT_VALUE = new Object();
	private static final String ROOY_PATH_NAME = Path.ROOT.toString();
//	private static final boolean DEBUG = true;
//	private static final int INIT_CHILDREN_MAP_CAPACITY = 2;

//	private Map fChildrenMap;
//	private Map fPatternMap;
	private PatternNameMap fPatternChildrenMap;
	private Object fValue;
	private IPath fPath;
	private String fName;
	private PathSettingsContainer fRootContainer;
	private PathSettingsContainer fDirectParentContainer;
	private List<IPathSettingsContainerListener> fListeners;

	private boolean fIsPatternMode;

	private static final int ADDED = 1;
	private static final int REMOVED = 2;
	private static final int VALUE_CHANGED = 3;
	private static final int PATH_CHANGED = 4;

	private static class PatternSearchInfo {
		Set<PathSettingsContainer> fStoreSet;
		int fNumDoubleStarEls;
	}

	public static PathSettingsContainer createRootContainer(){
		return createRootContainer(false);
	}

	public static PathSettingsContainer createRootContainer(boolean patternMode){
		return new PathSettingsContainer(patternMode);
	}

	private PathSettingsContainer(boolean pattternMode){
		this(null, null, ROOY_PATH_NAME, pattternMode);
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

	private boolean isDoubleStarName(){
		return PatternNameMap.DOUBLE_STAR_PATTERN.equals(getName());
	}

	private PathSettingsContainer getDoubleStarChild(){
		PatternNameMap pMap = getPatternChildrenMap(false);
		if(pMap != null){
			return pMap.containsDoubleStar() ? (PathSettingsContainer)pMap.get(PatternNameMap.DOUBLE_STAR_PATTERN) : null;
		}
		return null;
	}

	private List<PathSettingsContainer> getChildren(String name){
		PatternNameMap pMap = getPatternChildrenMap(false);
		if(pMap != null){
			return pMap.getValues(name);
		}
		return null;
	}


	private void notifyChange(PathSettingsContainer container, int type, Object oldValue, boolean childrenAffected){
		List<IPathSettingsContainerListener> list = getListenersList(false);
		if(list != null && list.size() > 0){
			for (IPathSettingsContainerListener listener : list) {
				switch(type){
				case ADDED:
					listener.containerAdded(container);
					break;
				case REMOVED:
					listener.aboutToRemove(container);
					break;
				case VALUE_CHANGED:
					listener.containerValueChanged(container, oldValue);
					break;
				case PATH_CHANGED:
					listener.containerPathChanged(container, (IPath)oldValue, childrenAffected);
					break;
				}

			}
		}
		PathSettingsContainer parent = getParentContainer();
		if(parent != null)
			parent.notifyChange(container, type, oldValue, childrenAffected);
	}

	private List<IPathSettingsContainerListener> getListenersList(boolean create){
		if(fListeners == null && create)
			fListeners = new ArrayList<IPathSettingsContainerListener>();
		return fListeners;
	}

	public boolean hasChildren(){
		PatternNameMap pMap = getPatternChildrenMap(false);
		return pMap != null && pMap.size() != 0;
	}

	public PathSettingsContainer getChildContainer(IPath path, boolean create, boolean exactPath){
		return getChildContainer(path, create, exactPath, fIsPatternMode);
	}

	public PathSettingsContainer getChildContainer(IPath path, boolean create, boolean exactPath, boolean patternSearch){
		PathSettingsContainer container = findContainer(path, create, exactPath, patternSearch, -1, null);
		if(container != null && container.internalGetValue() == INEXISTENT_VALUE){
			if(create){
				container.internalSetValue(null);
				notifyChange(container, ADDED, null, false);
			} else if(!exactPath){
				for(;
					container.internalGetValue() == INEXISTENT_VALUE;
					container = container.getDirectParentContainer()) {
				}
			} else if(container.internalGetValue() == INEXISTENT_VALUE){
				container = null;
			}

		}
		return container;
	}

	static IPath toNormalizedContainerPath(IPath path){
		return Path.ROOT.append(path);
	}

	public PathSettingsContainer[] getChildren(final boolean includeThis){
		final List<PathSettingsContainer> list = new ArrayList<PathSettingsContainer>();
		accept(new IPathSettingsContainerVisitor(){

			@Override
			public boolean visit(PathSettingsContainer container) {
				if(container != PathSettingsContainer.this || includeThis)
					list.add(container);
				return true;
			}
		});

		return list.toArray(new PathSettingsContainer[list.size()]);
	}

	public PathSettingsContainer[] getChildrenForPath(IPath path, boolean includePath){
		PathSettingsContainer cr = findContainer(path, false, true, fIsPatternMode, -1, null);
		if(cr != null)
			return cr.getChildren(includePath);
		return new PathSettingsContainer[0];
	}

	public PathSettingsContainer[] getDirectChildrenForPath(IPath path){
		PathSettingsContainer cr = findContainer(path, false, true, fIsPatternMode, -1, null);
		if(cr != null)
			return cr.getDirectChildren();
		return new PathSettingsContainer[0];
	}

/*	public PathSettingsContainer[] getDirectChildrenForPath(IPath path, boolean searchPatterns){
		if(!searchPatterns)
			return getDirectChildrenForPath(path);

		if(!isRoot() && !PatternNameMap.isPatternName(fName)){
			return getDirectParentContainer().getDirectChildrenForPath(
					new Path(fName).append(path), true);
		}
		return searchPatternsDirectChildrenForPath(path);
	}

	private PathSettingsContainer[] searchPatternsDirectChildrenForPath(IPath path){
		Set set = new HashSet();
		findContainer(path, false, false, path.segmentCount(), set);

		if(DEBUG){
			for(Iterator iter = set.iterator(); iter.hasNext();){
				PathSettingsContainer child = (PathSettingsContainer)iter.next();
				if(child.fValue == INEXISTENT_VALUE)
					throw new IllegalStateException();
			}
		}

		return (PathSettingsContainer[])set.toArray(new PathSettingsContainer[set.size()]);
	}
*/
	public PathSettingsContainer[] getDirectChildren(){
		List<PathSettingsContainer> list = doGetDirectChildren(null);
		if(list == null || list.size() == 0)
			return new PathSettingsContainer[0];
		return list.toArray(new PathSettingsContainer[list.size()]);
	}

	private List<PathSettingsContainer> doGetDirectChildren(List<PathSettingsContainer> list){
		PatternNameMap pMap = getPatternChildrenMap(false);
		if(pMap != null){
			if(list == null)
				list = new ArrayList<PathSettingsContainer>();
			for (PathSettingsContainer cr : pMap.values()) {
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
		final List<Object> list = new ArrayList<Object>();
		accept(new IPathSettingsContainerVisitor(){

			@Override
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
			if (fDirectParentContainer != null) {
				fDirectParentContainer.deleteChild(this);
				fDirectParentContainer.checkRemove();
				fDirectParentContainer = null;
			}
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


		Collection<PathSettingsContainer> c = pMap.values();
		PathSettingsContainer childContainers[] = c.toArray(new PathSettingsContainer[c.size()]);

		for (PathSettingsContainer childContainer : childContainers) {
			childContainer.removeChildren();
			childContainer.remove();
		}
	}

	private void deleteChild(PathSettingsContainer child){
		getPatternChildrenMap(false).remove(child.getName());
	}

	private String getName(){
		return fName;
	}

	private PathSettingsContainer findContainer(IPath path, boolean create, boolean exactPath, boolean patternSearch, int matchDepth, PatternSearchInfo psi){
		PathSettingsContainer container = null;
		if(path.segmentCount() == 0)
			container = this;
		else if (create || exactPath || !patternSearch || !fIsPatternMode) {
			PathSettingsContainer child = getExacChild(path.segment(0), create);
			if(child != null)
				container = child.findContainer(path.removeFirstSegments(1), create, exactPath, patternSearch, stepDepth(matchDepth), psi);
			else if(!exactPath)
				container = this;
		} else {
			//looking for container using patterns in read mode (i.e. not creating new container)
			if(psi == null)
				psi = new PatternSearchInfo();
			container = processPatterns(path, matchDepth, 0, psi);
//			do {
//				List list = getChildren(path.segment(0));
//				if(list != null){
//					int size = list.size();
//					PathSettingsContainer child, childFound;
//
//					for(int i = 0; i < size; i++){
//						child = (PathSettingsContainer)list.get(i);
//						if(directChildren && child.fValue != INEXISTENT_VALUE){
//							childFound = child;
//						} else {
//							childFound = child.findContainer(path.removeFirstSegments(1), false, false, directChildren, storeSet);
//						}
//
//						if(childFound.fValue != INEXISTENT_VALUE){
//							if(container == null
//									|| container.getValue() == INEXISTENT_VALUE
//									|| container.getPath().segmentCount() < childFound.getPath().segmentCount()){
//								container = childFound;
//							}
//							if(storeSet != null)
//								storeSet.add(container);
//						}
//					}
//				}
//				if(container == null || storeSet != null){
//					PathSettingsContainer dsChild = getDoubleStarChild();
//					if(dsChild != null && !storeSet.contains(dsChild)){
//						container = dsChild.findContainer(path, false, false, directChildren, storeSet);
//					}
//
//					if(container == null){
//						if(isDoubleStarName()){
//							if(path.segmentCount() != 0){
//								path = path.removeFirstSegments(1);
//								continue;
//							}
//						} else {
//							container = this;
//						}
//					}
//				}
//				break;
//			} while(true);
		}
		return container;
	}

	static boolean pathsEqual(IPath p1, IPath p2) {
		if (p1 == p2)
			return true;

		int i = p1.segmentCount();
		if(i != p2.segmentCount())
			return false;

		while (--i >= 0)
			if (!p1.segment(i).equals(p2.segment(i)))
				return false;

		return true;
	}

	private PathSettingsContainer processPatterns(IPath path, int matchDepth, int depth, PatternSearchInfo psi){
		Set<PathSettingsContainer> storeSet = psi.fStoreSet;
		PathSettingsContainer container = null;
		String name = path.segment(0);
		List<PathSettingsContainer> list = getChildren(name);
		PathSettingsContainer child, childFound;
		boolean exactPathFound = false;
		if(list != null){
			int size = list.size();

			for(int i = 0; i < size; i++){
				child = list.get(i);
				if(matchDepth == 0 && child.fValue != INEXISTENT_VALUE){
					childFound = child;
				} else {
					childFound = child.findContainer(path.removeFirstSegments(1), false, false, true, stepDepth(matchDepth), psi);
				}

				if(childFound != null  && childFound.fValue != INEXISTENT_VALUE){
					if(!exactPathFound
							&& path.segmentCount() == 1
							&& child != childFound
							&& name.equals(childFound.fName)){
						container = childFound;
						exactPathFound = true;
					} else if(container == null
							|| container.getValue() == INEXISTENT_VALUE
							|| container.getPath().segmentCount() < childFound.getPath().segmentCount()){
						container = childFound;
					}
					if(storeSet != null)
						storeSet.add(container);
					else if(exactPathFound)
						break;
				}
			}
		}

		if(!exactPathFound || storeSet != null){
			child = getDoubleStarChild();
			if(child != null/* && !storeSet.contains(child)*/){
				if(matchDepth == 0 && child.fValue != INEXISTENT_VALUE){
					childFound = child;
				} else {
					childFound = child.findContainer(path, false, false, true, matchDepth, psi);
				}

				if(childFound != null && childFound.fValue != INEXISTENT_VALUE){
					psi.fNumDoubleStarEls++;
					if(container == null
							|| container.getValue() == INEXISTENT_VALUE
							|| container.getPath().segmentCount() < childFound.getPath().segmentCount() + depth - psi.fNumDoubleStarEls){
						container = childFound;
					}
					if(storeSet != null)
						storeSet.add(container);
				}
			}

			if(container == null){
				if(isDoubleStarName()){
					if(path.segmentCount() > 1){
						childFound = processPatterns(path.removeFirstSegments(1), stepDepth(matchDepth), depth + 1, psi);
						if(childFound != null && childFound.fValue != INEXISTENT_VALUE){
							container = childFound;
							if(storeSet != null)
								storeSet.add(container);
						}
					}
				} else if (matchDepth < 0 && fValue != INEXISTENT_VALUE){
					container = this;
					if(storeSet != null)
						storeSet.add(container);
				}
			}
		}
		return container;
	}

	private int stepDepth(int depth){
		return depth == 0 ? depth : depth-1;
	}

	public void accept(IPathSettingsContainerVisitor visitor){
		doAccept(visitor);
	}

	private boolean doAccept(IPathSettingsContainerVisitor visitor){
		if(fValue != INEXISTENT_VALUE && !visitor.visit(this))
			return false;

		PatternNameMap pMap = getPatternChildrenMap(false);
		if(pMap != null){
			for (PathSettingsContainer child : pMap.values()) {
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
				for(Iterator<PathSettingsContainer> iter = fPatternChildrenMap.values().iterator(); iter.hasNext();){
					PathSettingsContainer child = iter.next();
					iter.remove();
					child.setParent(cr);
					cr.connectChild(child);
				}
			}
		} else {

		}

		PathSettingsContainer newParent = fRootContainer.findContainer(path.removeLastSegments(1), true, true, false, -1, null);
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
		List<IPathSettingsContainerListener> list = getListenersList(true);
		list.add(listenet);
	}

	public void removeContainerListener(IPathSettingsContainerListener listenet){
		List<IPathSettingsContainerListener> list = getListenersList(false);
		if(list != null)
			list.remove(listenet);
	}

	private void setParent(PathSettingsContainer parent){
		fDirectParentContainer = parent;
	}

	@Override
	public String toString() {
		return contributeToString(new StringBuffer(), 0).toString();
	}

	private StringBuffer contributeToString(StringBuffer buf, int depth){
		for (int i= 0; i < depth; i++) {
			buf.append('\t');
		}
		buf.append('[').append(getPath()).append(']').append('\n');

		PathSettingsContainer[] directChildren = getDirectChildren();
		if(directChildren.length != 0){
			int nextDepth = depth + 1;
			for (PathSettingsContainer child : directChildren) {
				child.contributeToString(buf, nextDepth);
			}
		}
		return buf;
	}

	static boolean hasSpecChars(IPath path){
		int count = path.segmentCount();
		for(int i = 0; i < count; i++){
			if(PatternNameMap.isPatternName(path.segment(i)))
				return true;
		}
		return false;
	}
}
