/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.base.ICompositeChange;
import org.eclipse.cdt.internal.corext.refactoring.changes.TextChange;
import org.eclipse.cdt.internal.corext.refactoring.changes.TextChange.EditChange;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A default content provider to present a hierarchy of <code>IChange</code>
 * objects in a tree viewer.
 */
class ChangeElementContentProvider  implements ITreeContentProvider {
	
	private static final ChangeElement[] EMPTY_CHILDREN= new ChangeElement[0];
	
	private static class OffsetComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			EditChange c1= (EditChange)o1;
			EditChange c2= (EditChange)o2;
			int p1= getOffset(c1);
			int p2= getOffset(c2);
			if (p1 < p2)
				return -1;
			if (p1 > p2)
				return 1;
			// same offset
			return 0;	
		}
		private int getOffset(EditChange edit) {
			return edit.getTextRange().getOffset();
		}
	}
	
	/* non Java-doc
	 * @see ITreeContentProvider#inputChanged
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}
	
	/* non Java-doc
	 * @see ITreeContentProvider#getChildren
	 */
	public Object[] getChildren(Object o) {
		ChangeElement element= (ChangeElement)o;
		ChangeElement[] children= element.getChildren();
		if (children == null) {
			children= createChildren(element);
		}
		return children;
	}
	
	/* non Java-doc
	 * @see ITreeContentProvider#getParent
	 */
	public Object getParent(Object element){
		return ((ChangeElement)element).getParent();
	}
	
	/* non Java-doc
	 * @see ITreeContentProvider#hasChildren
	 */
	public boolean hasChildren(Object element){
		Object[] children= getChildren(element);
		return children != null && children.length > 0;
	}
	
	/* non Java-doc
	 * @see ITreeContentProvider#dispose
	 */
	public void dispose(){
	}
	
	/* non Java-doc
	 * @see ITreeContentProvider#getElements
	 */
	public Object[] getElements(Object element){
		return getChildren(element);
	}
	
	private ChangeElement[] createChildren(ChangeElement object) {
		ChangeElement[] result= EMPTY_CHILDREN;
		if (!(object instanceof DefaultChangeElement))
			return result;
		
		DefaultChangeElement changeElement= (DefaultChangeElement)object;
		IChange change= changeElement.getChange();
		if (change instanceof ICompositeChange) {
			IChange[] children= ((ICompositeChange)change).getChildren();
			result= new ChangeElement[children.length];
			for (int i= 0; i < children.length; i++) {
				result[i]= new DefaultChangeElement(changeElement, children[i]);
			}
		} 
////////////////		
//		else if (change instanceof TranslationUnitChange) {
//			List children= new ArrayList(5);
//			TranslationUnitChange cunitChange= (TranslationUnitChange)change;
//			ITranslationUnit cunit= cunitChange.getTranslationUnit();
//			Map map= new HashMap(20);
//			EditChange[] changes=getSortedTextEditChanges(cunitChange);
//			for (int i= 0; i < changes.length; i++) {
//				EditChange tec= changes[i];
//				try {
//					ICElement element= getModifiedCElement(tec, cunit);
//					if (element.equals(cunit)) {
//						children.add(new TextEditChangeElement(changeElement, tec));
//					} else {
//						PseudoCChangeElement pjce= getChangeElement(map, element, children, changeElement);
//						pjce.addChild(new TextEditChangeElement(pjce, tec));
//					}
//				} catch (CModelException e) {
//					children.add(new TextEditChangeElement(changeElement, tec));
//				}
//			}
//			result= (ChangeElement[]) children.toArray(new ChangeElement[children.size()]);
//		} 
//		else if (change instanceof TextChange) {
//			EditChange[] changes= getSortedTextEditChanges((TextChange)change);
//			result= new ChangeElement[changes.length];
//			for (int i= 0; i < changes.length; i++) {
//				result[i]= new TextEditChangeElement(changeElement, changes[i]);
//			}
//		}
///////////		
		changeElement.setChildren(result);
		return result;
	}
	
	private EditChange[] getSortedTextEditChanges(TextChange change) {
		EditChange[] edits= change.getTextEditChanges();
		List result= new ArrayList(edits.length);
		for (int i= 0; i < edits.length; i++) {
			if (!edits[i].isEmpty())
				result.add(edits[i]);
		}
		Comparator comparator= new OffsetComparator();
		Collections.sort(result, comparator);
		return (EditChange[])result.toArray(new EditChange[result.size()]);
	}
	
	private PseudoCChangeElement getChangeElement(Map map, ICElement element, List children, ChangeElement cunitChange) {
		PseudoCChangeElement result= (PseudoCChangeElement)map.get(element);
		if (result != null)
			return result;
		ICElement parent= element.getParent();
		if (parent instanceof ITranslationUnit) {
			result= new PseudoCChangeElement(cunitChange, element);
			children.add(result);
			map.put(element, result);
		} else {
			PseudoCChangeElement parentChange= getChangeElement(map, parent, children, cunitChange);
			result= new PseudoCChangeElement(parentChange, element);
			parentChange.addChild(result);
			map.put(element, result);
		}
		return result;
	}
	
	private ICElement getModifiedCElement(EditChange edit, ITranslationUnit cunit) throws CModelException {
		IRegion range= edit.getTextRange();
		if (range.getOffset() == 0 && range.getLength() == 0)
			return cunit;
		ICElement result= getElementAt(cunit, range.getOffset());
		if (result == null)
			return cunit;
		
		try {
			while(true) {
				ISourceReference ref= (ISourceReference)result;
				IRegion sRange= new Region(ref.getSourceRange().getStartPos(), ref.getSourceRange().getLength());
				if (result.getElementType() == ICElement.C_UNIT || result.getParent() == null || edit.coveredBy(sRange))
					break;
				result= result.getParent();
			}
		} catch(CModelException e) {
			// Do nothing, use old value.
		} catch(ClassCastException e) {
			// Do nothing, use old value.
		}
		return result;
	}
	
	protected ICElement getElementAt(IParent unit, int position) throws CModelException {
		if (unit instanceof ISourceReference) {
			ICElement[] children = unit.getChildren();
			for (int i = 0; i < children.length; i++) {
				ICElement aChild = children[i];
				if (aChild instanceof ISourceReference) {
					ISourceReference child = (ISourceReference) children[i];
					ISourceRange range = child.getSourceRange();
					if (position < range.getStartPos() + range.getLength() && position >= range.getStartPos()) {
						if (child instanceof IParent) {
							return getElementAt((IParent)child, position);
						} else {
							return ((ICElement)child);
						}
					}
				}
			}
		} 
		return null;
	}
}

