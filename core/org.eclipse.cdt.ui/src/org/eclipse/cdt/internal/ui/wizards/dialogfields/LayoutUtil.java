/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.dialogfields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class LayoutUtil {
	
	/**
	 * Calculates the number of columns needed by field editors
	 */
	public static int getNumberOfColumns(DialogField[] editors) {
		int nCulumns= 0;
		for (int i= 0; i < editors.length; i++) {
			nCulumns= Math.max(editors[i].getNumberOfControls(), nCulumns);
		}
		return nCulumns;
	}
	
	/**
	 * Creates a composite and fills in the given editors.
	 * @param labelOnTop Defines if the label of all fields should be on top of the fields
	 */	
	public static void doDefaultLayout(Composite parent, DialogField[] editors, boolean labelOnTop) {
		doDefaultLayout(parent, editors, labelOnTop, 0, 0, 0, 0);
	}

	/**
	 * Creates a composite and fills in the given editors.
	 * @param labelOnTop Defines if the label of all fields should be on top of the fields
	 * @param minWidth The minimal width of the composite
	 * @param minHeight The minimal height of the composite 
	 */
	public static void doDefaultLayout(Composite parent, DialogField[] editors, boolean labelOnTop, int minWidth, int minHeight) {
		doDefaultLayout(parent, editors, labelOnTop, minWidth, minHeight, 0, 0);
	}

	/**
	 * Creates a composite and fills in the given editors.
	 * @param labelOnTop Defines if the label of all fields should be on top of the fields
	 * @param minWidth The minimal width of the composite
	 * @param minHeight The minimal height of the composite
	 * @param marginWidth The margin width to be used by the composite
	 * @param marginHeight The margin height to be used by the composite
	 * @deprecated
	 */	
	public static void doDefaultLayout(Composite parent, DialogField[] editors, boolean labelOnTop, int minWidth, int minHeight, int marginWidth, int marginHeight) {
		int nCulumns= getNumberOfColumns(editors);
		Control[][] controls= new Control[editors.length][];
		for (int i= 0; i < editors.length; i++) {
			controls[i]= editors[i].doFillIntoGrid(parent, nCulumns);
		}
		if (labelOnTop) {
			nCulumns--;
			modifyLabelSpans(controls, nCulumns);
		}
		GridLayout layout= new GridLayout();
		if (marginWidth != SWT.DEFAULT) {
			layout.marginWidth= marginWidth;
		}
		if (marginHeight != SWT.DEFAULT) {
			layout.marginHeight= marginHeight;
		}
		layout.numColumns= nCulumns;		
		parent.setLayout(layout);
	}
	
	private static void modifyLabelSpans(Control[][] controls, int nCulumns) {
		for (int i= 0; i < controls.length; i++) {
			setHorizontalSpan(controls[i][0], nCulumns);
		}
	}
	
	/**
	 * Sets the span of a control. Assumes that GridData is used.
	 */
	public static void setHorizontalSpan(Control control, int span) {
		Object ld= control.getLayoutData();
		if (ld instanceof GridData) {
			((GridData)ld).horizontalSpan= span;
		} else if (span != 1) {
			GridData gd= new GridData();
			gd.horizontalSpan= span;
			control.setLayoutData(gd);
		}
	}	

	/**
	 * Sets the width hint of a control. Assumes that GridData is used.
	 */
	public static void setWidthHint(Control control, int widthHint) {
		Object ld= control.getLayoutData();
		if (ld instanceof GridData) {
			((GridData)ld).widthHint= widthHint;
		}
	}
	
	/**
	 * Sets the heigthHint hint of a control. Assumes that GridData is used.
	 */
	public static void setHeightHint(Control control, int heigthHint) {
		Object ld= control.getLayoutData();
		if (ld instanceof GridData) {
			((GridData)ld).heightHint= heigthHint;
		}
	}	
	
	/**
	 * Sets the horizontal indent of a control. Assumes that GridData is used.
	 */
	public static void setHorizontalIndent(Control control, int horizontalIndent) {
		Object ld= control.getLayoutData();
		if (ld instanceof GridData) {
			((GridData)ld).horizontalIndent= horizontalIndent;
		}
	}
	
	/**
	 * Sets the horizontal indent of a control. Assumes that GridData is used.
	 */
	public static void setHorizontalGrabbing(Control control) {
		Object ld= control.getLayoutData();
		if (ld instanceof GridData) {
			((GridData)ld).grabExcessHorizontalSpace= true;
		}
	}		

}
