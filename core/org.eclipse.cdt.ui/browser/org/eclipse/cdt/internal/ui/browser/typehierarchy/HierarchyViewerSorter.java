/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.typehierarchy;

import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.ui.CElementSorter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
  */
public class HierarchyViewerSorter extends ViewerSorter {
	
	private static final int OTHER= 0;
	private static final int CLASS= 1;
	private static final int FIELD= 2;
	private static final int METHOD= 3;
	
	private TypeHierarchyLifeCycle fHierarchy;
	private boolean fSortByDefiningType;
	private CElementSorter fNormalSorter;
	
	public HierarchyViewerSorter(TypeHierarchyLifeCycle cycle) {
		fHierarchy= cycle;
		fNormalSorter= new CElementSorter();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
	 */
//	public int category(Object element) {
//		if (element instanceof ICElement) {
//		    ICElement type= (ICElement) element;
//			ITypeHierarchy hierarchy= fHierarchy.getHierarchy();
//			if (hierarchy != null) {
//				return CLASS;
//			}
//		}
//		return OTHER;
//	}

//	public boolean isSorterProperty(Object element, Object property) {
//		return true;
//	}
	
	public int category(Object obj) {
		if (obj instanceof ICElement) {
			ICElement elem= (ICElement)obj;
			switch (elem.getElementType()) {
				case ICElement.C_CLASS:
				case ICElement.C_STRUCT:
				    return CLASS;
//				case ICElement.C_UNION:
//				    return 3;
				case ICElement.C_FIELD:
				    return FIELD;
				
				case ICElement.C_METHOD:
				case ICElement.C_METHOD_DECLARATION:
					return METHOD;
//				{
//				    IMethodDeclaration method = (IMethodDeclaration) elem;
//				    try {
//				        // sort constructor and destructor first
//				        if (method.isConstructor() || method.isDestructor())
//				            return 10;
//				    } catch (CModelException e) {
//				    }
//				    return 20;
//				}
			}
			
		}
		return OTHER;
	}
	
	public boolean isSortByDefiningType() {
		return fSortByDefiningType;
	}

	public void setSortByDefiningType(boolean sortByDefiningType) {
		fSortByDefiningType= sortByDefiningType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(null, null, null)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		int cat1= category(e1);
		int cat2= category(e2);

		if (cat1 != cat2)
			return cat1 - cat2;
		
		ITypeHierarchy hierarchy= fHierarchy.getHierarchy();
		if (hierarchy == null) {
			return fNormalSorter.compare(viewer, e1, e2);
		}
		
		if (cat1 == FIELD || cat1 == METHOD) { // method or field
			if (fSortByDefiningType) {
				try {
				    ICElement def1= (e1 instanceof IMethodDeclaration) ? getDefiningType(hierarchy, (IMethodDeclaration) e1) : null;
				    ICElement def2= (e2 instanceof IMethodDeclaration) ? getDefiningType(hierarchy, (IMethodDeclaration) e2) : null;
					if (def1 != null) {
						if (def2 != null) {
							if (!def2.equals(def1)) {
								return compareInHierarchy(hierarchy, def1, def2);
							}
						} else {
							return -1;						
						}					
					} else {
						if (def2 != null) {
							return 1;
						}	
					}
				} catch (CModelException e) {
					// ignore, default to normal comparison
				}
			}
			return fNormalSorter.compare(viewer, e1, e2); // use appearance pref page settings
		}
		String name1= ((ICElement) e1).getElementName(); //$NON-NLS-1$
		String name2= ((ICElement) e2).getElementName(); //$NON-NLS-1$
		return getCollator().compare(name1, name2);
	}
	
	private ICElement getDefiningType(ITypeHierarchy hierarchy, IMethodDeclaration method) throws CModelException {
		ICElement declaringType= TypeUtil.getDeclaringClass(method);
		if ((method.getVisibility() == ASTAccessVisibility.PRIVATE) || method.isStatic() || method.isConstructor() || method.isDestructor()) {
		    return null;
		}
	
		ICElement res= TypeUtil.findMethodDeclarationInHierarchy(hierarchy, declaringType, method.getElementName(), method.getParameterTypes(), false, false);
		if (res == null || method.equals(res)) {
			return null;
		}
		return TypeUtil.getDeclaringClass(res);
	}
	

	private int compareInHierarchy(ITypeHierarchy hierarchy, ICElement def1, ICElement def2) {
		if (isSuperType(hierarchy, def1, def2)) {
			return 1;
		} else if (isSuperType(hierarchy, def2, def1)) {
			return -1;
		}
		String name1= def1.getElementName();
		String name2= def2.getElementName();
		
		return getCollator().compare(name1, name2);
	}

	private boolean isSuperType(ITypeHierarchy hierarchy, ICElement def1, ICElement def2) {
		ICElement[] superTypes= hierarchy.getSupertypes(def1);
		if (superTypes != null) {
		    for (int i = 0; i < superTypes.length; ++i) {
				if (superTypes[i].equals(def2) || isSuperType(hierarchy, superTypes[i], def2)) {
					return true;
				}
		    }
		}
		return false;
	}

}
