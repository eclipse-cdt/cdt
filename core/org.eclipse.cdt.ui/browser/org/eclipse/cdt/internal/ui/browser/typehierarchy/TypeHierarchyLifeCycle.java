/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.typehierarchy;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy;
import org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchyChangedListener;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * Manages a type hierarchy, to keep it refreshed, and to allow it to be shared.
 */
public class TypeHierarchyLifeCycle implements ITypeHierarchyChangedListener, IElementChangedListener {
	
	private boolean fHierarchyRefreshNeeded;
	private ITypeHierarchy fHierarchy;
	private ICElement fInputElement;
	
	private List fChangeListeners;
	
	public TypeHierarchyLifeCycle() {
		this(true);
	}	
	
	public TypeHierarchyLifeCycle(boolean isSuperTypesOnly) {
		fHierarchy= null;
		fInputElement= null;
		fChangeListeners= new ArrayList(2);
	}
	
	public ITypeHierarchy getHierarchy() {
		return fHierarchy;
	}
	
	public ICElement getInputElement() {
		return fInputElement;
	}
	
	
	public void freeHierarchy() {
		if (fHierarchy != null) {
			fHierarchy.removeTypeHierarchyChangedListener(this);
			CoreModel.getDefault().removeElementChangedListener(this);
			fHierarchy= null;
			fInputElement= null;
		}
	}
	
	public void removeChangedListener(ITypeHierarchyLifeCycleListener listener) {
		fChangeListeners.remove(listener);
	}
	
	public void addChangedListener(ITypeHierarchyLifeCycleListener listener) {
		if (!fChangeListeners.contains(listener)) {
			fChangeListeners.add(listener);
		}
	}
	
	private void fireChange(ICElement[] changedTypes) {
		for (int i= fChangeListeners.size()-1; i>=0; i--) {
			ITypeHierarchyLifeCycleListener curr= (ITypeHierarchyLifeCycleListener) fChangeListeners.get(i);
			curr.typeHierarchyChanged(this, changedTypes);
		}
	}
			
	public void ensureRefreshedTypeHierarchy(final ICElement element, IRunnableContext context) throws InvocationTargetException, InterruptedException {
		if (element == null || !element.exists()) {
			freeHierarchy();
			return;
		}
		boolean hierachyCreationNeeded= (fHierarchy == null || !element.equals(fInputElement));
		
		if (hierachyCreationNeeded || fHierarchyRefreshNeeded) {
			
			IRunnableWithProgress op= new IRunnableWithProgress() {
				public void run(IProgressMonitor pm) throws InvocationTargetException, InterruptedException {
					try {
						doHierarchyRefresh(element, pm);
					} catch (CModelException e) {
						throw new InvocationTargetException(e);
					} catch (OperationCanceledException e) {
						throw new InterruptedException();
					}
				}
			};
			fHierarchyRefreshNeeded= true;
			context.run(true, true, op);
			fHierarchyRefreshNeeded= false;
		}
	}
	
	private ITypeHierarchy createTypeHierarchy(ICElement element, IProgressMonitor pm) throws CModelException {
	    if (element.getElementType() == ICElement.C_CLASS
	            || element.getElementType() == ICElement.C_STRUCT) {
            return AllTypesCache.createTypeHierarchy(element, pm);
	    } else {
//			IRegion region= JavaCore.newRegion();
//			if (element.getElementType() == ICElement.JAVA_PROJECT) {
//				// for projects only add the contained source folders
//				IPackageFragmentRoot[] roots= ((IJavaProject) element).getPackageFragmentRoots();
//				for (int i= 0; i < roots.length; i++) {
//					if (!roots[i].isExternal()) {
//						region.add(roots[i]);
//					}
//				}
//			} else if (element.getElementType() == ICElement.PACKAGE_FRAGMENT) {
//				IPackageFragmentRoot[] roots= element.getJavaProject().getPackageFragmentRoots();
//				String name= element.getElementName();
//				for (int i= 0; i < roots.length; i++) {
//					IPackageFragment pack= roots[i].getPackageFragment(name);
//					if (pack.exists()) {
//						region.add(pack);
//					}
//				}
//			} else {
//				region.add(element);
//			}
//			ICProject jproject= element.getCProject();
//			return jproject.newTypeHierarchy(region, pm);
	    	return null;
	    }
	}
	
	
	public synchronized void doHierarchyRefresh(ICElement element, IProgressMonitor pm) throws CModelException {
		boolean hierachyCreationNeeded= (fHierarchy == null || !element.equals(fInputElement));
		// to ensure the order of the two listeners always remove / add listeners on operations
		// on type hierarchies
		if (fHierarchy != null) {
			fHierarchy.removeTypeHierarchyChangedListener(this);
			CoreModel.getDefault().removeElementChangedListener(this);
		}
		if (hierachyCreationNeeded) {
			fHierarchy= createTypeHierarchy(element, pm);
			if (pm != null && pm.isCanceled()) {
				throw new OperationCanceledException();
			}
			fInputElement= element;
		} else if (fHierarchy != null) {
			fHierarchy.refresh(pm);
		}
		if (fHierarchy != null) {
		    fHierarchy.addTypeHierarchyChangedListener(this);
		}
		CoreModel.getDefault().addElementChangedListener(this);
		fHierarchyRefreshNeeded= false;
	}		
	
	/*
	 * @see ITypeHierarchyChangedListener#typeHierarchyChanged
	 */
	public void typeHierarchyChanged(ITypeHierarchy typeHierarchy) {
	 	fHierarchyRefreshNeeded= true;
 		fireChange(null);
	}		

	/*
	 * @see IElementChangedListener#elementChanged(ElementChangedEvent)
	 */
	public void elementChanged(ElementChangedEvent event) {
		if (fChangeListeners.isEmpty()) {
			return;
		}
		
		if (fHierarchyRefreshNeeded) {
			return;
		} else {
			ArrayList changedTypes= new ArrayList();
			processDelta(event.getDelta(), changedTypes);
			if (changedTypes.size() > 0) {
				fireChange((ICElement[]) changedTypes.toArray(new ICElement[changedTypes.size()]));
			}
		}
	}
	
	/*
	 * Assume that the hierarchy is intact (no refresh needed)
	 */					
	private void processDelta(ICElementDelta delta, ArrayList changedTypes) {
		ICElement element= delta.getElement();
		switch (element.getElementType()) {
//			case ICElement.TYPE:
			case ICElement.C_CLASS:
			case ICElement.C_STRUCT:
				processTypeDelta(element, changedTypes);
				processChildrenDelta(delta, changedTypes); // (inner types)
				break;
			case ICElement.C_MODEL:
			case ICElement.C_PROJECT:
//			case ICElement.PACKAGE_FRAGMENT_ROOT:
//			case ICElement.PACKAGE_FRAGMENT:
				processChildrenDelta(delta, changedTypes);
				break;
			case ICElement.C_UNIT:
				ITranslationUnit cu= (ITranslationUnit)element;
//				if (!CModelUtil.isPrimary(cu)) {
//					return;
//				}
				
				if (delta.getKind() == ICElementDelta.CHANGED && isPossibleStructuralChange(delta.getFlags())) {
//					try {
						if (cu.exists()) {
//							IType[] types= cu.getAllTypes();
						    ICElement[] types= getAllTypesForTranslationUnit(cu);							for (int i= 0; i < types.length; i++) {
								processTypeDelta(types[i], changedTypes);
							}
						}
//					} catch (CModelException e) {
//						CUIPlugin.getDefault().log(e);
//					}
				} else {
					processChildrenDelta(delta, changedTypes);
				}
				break;
//			case ICElement.CLASS_FILE:	
//				if (delta.getKind() == ICElementDelta.CHANGED) {
//					try {
//						IType type= ((IClassFile) element).getType();
//						processTypeDelta(type, changedTypes);
//					} catch (CModelException e) {
//						CUIPlugin.getDefault().log(e);
//					}
//				} else {
//					processChildrenDelta(delta, changedTypes);
//				}
//				break;				
		}
	}
	
	private boolean isPossibleStructuralChange(int flags) {
		return (flags & (ICElementDelta.F_CONTENT | ICElementDelta.F_FINE_GRAINED)) == ICElementDelta.F_CONTENT;
	}
	
	private void processTypeDelta(ICElement type, ArrayList changedTypes) {
		if (getHierarchy().contains(type)) {
			changedTypes.add(type);
		}
	}
	
	private void processChildrenDelta(ICElementDelta delta, ArrayList changedTypes) {
		ICElementDelta[] children= delta.getAffectedChildren();
		for (int i= 0; i < children.length; i++) {
			processDelta(children[i], changedTypes); // recursive
		}
	}
	
	private static ICElement[] getAllTypesForTranslationUnit(ITranslationUnit unit) {
	    return null;
	}
	

}
