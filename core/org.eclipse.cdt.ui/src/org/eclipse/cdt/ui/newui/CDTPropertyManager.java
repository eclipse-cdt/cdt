/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.newui.Messages;

/**
 * This class is intended to handle  
 * 
 * When new propertypage is created, it should request 
 * project description by method 
 * getProjectDescription()
 * This method, in addition, registers page in list.
 * 
 * While page is active, it can change this description
 * but should not set it, to avoid inconsistency.
 * 
 * When page's "performOK" called, it should call
 * manager's  method
 * performOk()
 *
 * Registered pages can call {@link CDTPropertyManager#remove(Object)}
 * to explicitly remove themselves from this manager.
 *
 * In addition, there are utility methods for pages: 
 * getPagesCount()
 * getPage()
 * isSaveDone()
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CDTPropertyManager {

	private static ArrayList<Object> pages = new ArrayList<Object>();
	private static ICProjectDescription prjd = null;
	private static boolean saveDone  = false;
	private static IProject project = null;
	private static DListener dListener = new DListener();
	
	
	public static ICProjectDescription getProjectDescription(PropertyPage p, IProject prj) {
		return get(p, prj);
	} 	
	public static ICProjectDescription getProjectDescription(Widget w, IProject prj) {
		return get(w, prj);
	} 	
	public static ICProjectDescription getProjectDescription(IProject prj) {
		return get(null, prj);
	} 	
	
	private static ICProjectDescription get(Object p, IProject prj) {
		// New session - clean static variables
		if (pages.size() == 0) {
			project = null;
			prjd = null;
			saveDone = false;
		}
		// Register new client
		if (p != null && !pages.contains(p)) {
			pages.add(p);
			if (p instanceof PropertyPage) {
				if (((PropertyPage)p).getControl() != null) 
					((PropertyPage)p).getControl().addDisposeListener(dListener);
			} else if (p instanceof Widget) {
				((Widget)p).addDisposeListener(dListener);
			}
		}
		// Check that we are working with the same project 
		if (project == null || !project.equals(prj)) {
			project = prj;
			prjd = null;
		}
		// obtain description if it's needed.
		if (prjd == null) {
			prjd = CoreModel.getDefault().getProjectDescription(prj);
		}
		return prjd;
	}

	/**
	 * Performs optimized (single-time) saving
	 * @param p - widget which calls this functionality
	 */
	public static void performOk(Object p) {
		if (saveDone) return;

		performOkForced(p);
		
		if (pages.size() == 0) {
			project = null;
			prjd = null;
			saveDone = false;
		}
	}
	
	public static void performCancel(Object p) {
		saveDone = true;
		
		if (pages.size() == 0) {
			project = null;
			prjd = null;
			saveDone = false;
		}
	}

	/**
	 * Explicitly remove the page from this CDTPropertyManager
	 * @param p
	 * @since 5.1
	 */
	public static void remove(Object p) {
		DListener.dispose(p);
	}

	/**
	 * Performs mandatory saving 
	 * @param p
	 */
	public static void performOkForced(Object p) {
		saveDone = true;
		try {
			CoreModel.getDefault().setProjectDescription(project, prjd);
		} catch (CoreException e) {
			CUIPlugin.getDefault().logErrorMessage(Messages.AbstractPage_11 + e.getLocalizedMessage()); 
		}
		
		if (pages.size() == 0) {
			project = null;
			prjd = null;
			saveDone = false;
		}
	}
	
	// pages utilities
	public static boolean isSaveDone() { return saveDone; }	
	public static int getPagesCount() {	return pages.size(); }
	public static Object getPage(int index) { return pages.get(index); }

	// Removes disposed items from list
	static class DListener implements DisposeListener {
		public static void dispose (Object w) {
			if (pages.contains(w)) { // Widget ?	
				pages.remove(w); 
			} else {                 // Property Page ?
				for (Object ob : pages) {
					if (ob != null && ob instanceof PropertyPage) {
						if (((PropertyPage)ob).getControl().equals(w)) {
							pages.remove(ob);
							break;
						}
					}
				}
			}

			if (pages.isEmpty()) {
				saveDone = true;
				project = null;
				prjd = null;
				saveDone = false;
			}
		}
		public void widgetDisposed(DisposeEvent e) {
			dispose(e.widget);
		}
	}
	
}
