/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - adapted for use in CDT
 *******************************************************************************/
package org.eclipse.cdt.ui.browser.typeinfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.cdt.internal.ui.util.StringMatcher;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredList;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;


/**
 * A dialog to select a type from a list of types.
 */
public class TypeSelectionDialog extends TwoPaneElementSelector {

	private static class TypeFilterMatcher implements FilteredList.FilterMatcher {

		private static final char END_SYMBOL= '<';
		private static final char ANY_STRING= '*';
		private final static String scopeResolutionOperator= "::"; //$NON-NLS-1$

		private StringMatcher fMatcher;
		private StringMatcher fQualifierMatcher;
		private StringMatcher fScopedQualifierMatcher;
		
		/*
		 * @see FilteredList.FilterMatcher#setFilter(String, boolean)
		 */
		public void setFilter(String pattern, boolean ignoreCase, boolean ignoreWildCards) {
			int qualifierIndex= pattern.lastIndexOf(scopeResolutionOperator); //$NON-NLS-1$

			// type			
			if (qualifierIndex == -1) {
				fQualifierMatcher= null;
				fScopedQualifierMatcher= null;
				fMatcher= new StringMatcher(adjustPattern(pattern), ignoreCase, ignoreWildCards);
				
			// qualified type
			} else {
				String pattern1 = pattern.substring(0, qualifierIndex + scopeResolutionOperator.length());
				fQualifierMatcher= new StringMatcher(adjustPattern(pattern1), ignoreCase, ignoreWildCards);
				StringBuffer buf = new StringBuffer();
				buf.append(ANY_STRING);
				buf.append(scopeResolutionOperator);
				buf.append(pattern1);
				String pattern2= buf.toString();
				fScopedQualifierMatcher= new StringMatcher(adjustPattern(pattern2), ignoreCase, ignoreWildCards);
				String pattern3 = pattern.substring(qualifierIndex + scopeResolutionOperator.length());
				fMatcher= new StringMatcher(adjustPattern(pattern3), ignoreCase, ignoreWildCards);
			}
		}

		/*
		 * @see FilteredList.FilterMatcher#match(Object)
		 */
		public boolean match(Object element) {
			if (!(element instanceof ITypeInfo))
				return false;

			TypeInfo type= (TypeInfo) element;

			if (!fMatcher.match(type.getName()))
				return false;

			if (fQualifierMatcher == null)
				return true;
			
			if (fQualifierMatcher.match(type.getQualifiedName()))
				return true;
			else
				return fScopedQualifierMatcher.match(type.getQualifiedName());
		}
		
		private String adjustPattern(String pattern) {
			int length= pattern.length();
			if (length > 0) {
				switch (pattern.charAt(length - 1)) {
					case END_SYMBOL:
						pattern= pattern.substring(0, length - 1);
						break;
					case ANY_STRING:
						break;
					default:
						pattern= pattern + ANY_STRING;
				}
			}
			return pattern;
		}
	}
	
	private static class StringComparator implements Comparator {
	    public int compare(Object left, Object right) {
	     	String leftString= (String) left;
	     	String rightString= (String) right;
	     	
			int result= leftString.compareToIgnoreCase(rightString);			
			if (result == 0)
				result= leftString.compareTo(rightString);

			return result;
	    }
	}
	
	/**
	 * Constructs a type selection dialog.
	 * @param parent  the parent shell.
	 * @param context the runnable context.
	 * @param scope   the C search scope.
	 */
	public TypeSelectionDialog(Shell parent) {
		super(parent, new TypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_TYPE_ONLY),
			new TypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_TYPE_CONTAINER_ONLY + TypeInfoLabelProvider.SHOW_ROOT_POSTFIX));
		setUpperListLabel(TypeInfoMessages.getString("TypeSelectionDialog.upperLabel")); //$NON-NLS-1$
		setLowerListLabel(TypeInfoMessages.getString("TypeSelectionDialog.lowerLabel")); //$NON-NLS-1$
	}

	/*
	 * @see AbstractElementListSelectionDialog#createFilteredList(Composite)
	 */
 	protected FilteredList createFilteredList(Composite parent) {
 		FilteredList list= super.createFilteredList(parent);
 		
		fFilteredList.setFilterMatcher(new TypeFilterMatcher());
		fFilteredList.setComparator(new StringComparator());
		
		return list;
	}
	
	/*
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
	 */
	protected void computeResult() {
		ITypeInfo selection= (ITypeInfo) getLowerSelectedElement();
		if (selection == null)
			return;
			
		List result= new ArrayList(1);
		result.add(selection);
		setResult(result);
	}
}
