/*******************************************************************************
 * Copyright (c) 2000,2003,2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - adapted for use in CDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.opentype.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.ui.opentype.OpenTypeMessages;
import org.eclipse.cdt.internal.ui.opentype.TypeSearchMatch;
import org.eclipse.cdt.internal.ui.opentype.TypeSearchMatchLabelProvider;
import org.eclipse.cdt.internal.ui.opentype.TypeSearchOperation;
import org.eclipse.cdt.internal.ui.util.StringMatcher;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.util.Strings;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.util.Assert;
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

		private StringMatcher fMatcher;
		private StringMatcher fQualifierMatcher;
		
		/*
		 * @see FilteredList.FilterMatcher#setFilter(String, boolean)
		 */
		public void setFilter(String pattern, boolean ignoreCase, boolean igoreWildCards) {
			int qualifierIndex= pattern.lastIndexOf("::"); //$NON-NLS-1$

			// type			
			if (qualifierIndex == -1) {
				fQualifierMatcher= null;
				fMatcher= new StringMatcher(adjustPattern(pattern), ignoreCase, igoreWildCards);
				
			// qualified type
			} else {
				fQualifierMatcher= new StringMatcher(pattern.substring(0, qualifierIndex), ignoreCase, igoreWildCards);
				fMatcher= new StringMatcher(adjustPattern(pattern.substring(qualifierIndex + 2)), ignoreCase, igoreWildCards);
			}
		}

		/*
		 * @see FilteredList.FilterMatcher#match(Object)
		 */
		public boolean match(Object element) {
			if (!(element instanceof TypeSearchMatch))
				return false;

			TypeSearchMatch type= (TypeSearchMatch) element;

			if (!fMatcher.match(type.getName()))
				return false;

			if (fQualifierMatcher == null)
				return true;

			return fQualifierMatcher.match(type.getFullyQualifiedName());
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
	
	/*
	 * A string comparator which is aware of obfuscated code
	 * (type names starting with lower case characters).
	 */
	private static class StringComparator implements Comparator {
	    public int compare(Object left, Object right) {
	     	String leftString= (String) left;
	     	String rightString= (String) right;
	     	
	     	if (leftString.length() != 0 && rightString.length() != 0)
	     	{
		     	if (Strings.isLowerCase(leftString.charAt(0)) &&
		     		!Strings.isLowerCase(rightString.charAt(0)))
		     		return +1;
	
		     	if (Strings.isLowerCase(rightString.charAt(0)) &&
		     		!Strings.isLowerCase(leftString.charAt(0)))
		     		return -1;
			}
	     	
			int result= leftString.compareToIgnoreCase(rightString);			
			if (result == 0)
				result= leftString.compareTo(rightString);

			return result;
	    }
	}
	
	private IRunnableContext fRunnableContext;
	private ICSearchScope fScope;
	
	/**
	 * Constructs a type selection dialog.
	 * @param parent  the parent shell.
	 * @param context the runnable context.
	 * @param scope   the C search scope.
	 */
	public TypeSelectionDialog(Shell parent, IRunnableContext context, ICSearchScope scope) {
		super(parent, new TypeSearchMatchLabelProvider(TypeSearchMatchLabelProvider.SHOW_TYPE_ONLY),
			new TypeSearchMatchLabelProvider(TypeSearchMatchLabelProvider.SHOW_TYPE_CONTAINER_ONLY + TypeSearchMatchLabelProvider.SHOW_ROOT_POSTFIX));

		Assert.isNotNull(context);
		Assert.isNotNull(scope);

		fRunnableContext= context;
		fScope= scope;
		
		setUpperListLabel(OpenTypeMessages.getString("TypeSelectionDialog.upperLabel")); //$NON-NLS-1$
		setLowerListLabel(OpenTypeMessages.getString("TypeSelectionDialog.lowerLabel")); //$NON-NLS-1$
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
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		TypeSearchOperation search = new TypeSearchOperation(CUIPlugin.getWorkspace(), fScope, new SearchEngine());
		
		try {
			fRunnableContext.run(true, true, search);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, OpenTypeMessages.getString("TypeSelectionDialog.error3Title"), OpenTypeMessages.getString("TypeSelectionDialog.error3Message")); //$NON-NLS-1$ //$NON-NLS-2$
			return CANCEL;
		} catch (InterruptedException e) {
			// cancelled by user
			return CANCEL;
		}
		
		Object[] results = search.getResults();
		if (results.length == 0) {
			String title = OpenTypeMessages.getString("TypeSelectionDialog.notypes.title"); //$NON-NLS-1$
			String message = OpenTypeMessages.getString("TypeSelectionDialog.notypes.message"); //$NON-NLS-1$
			MessageDialog.openInformation(getShell(), title, message);
			return CANCEL;
		}

		setElements(results);
		return super.open();
	}
	
	/*
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
	 */
	protected void computeResult() {
		TypeSearchMatch selection= (TypeSearchMatch) getLowerSelectedElement();
		if (selection == null)
			return;
			
		List result= new ArrayList(1);
		result.add(selection);
		setResult(result);
	}
}
