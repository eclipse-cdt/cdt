/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;

import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CUILabelProvider;

/**
 * The label provider for the c model elements.
 */
public class CElementLabelProvider extends LabelProvider {

	/**
	 * Flag (bit mask) indicating that methods labels include the method return type. (appended)
	 */
	public final static int SHOW_RETURN_TYPE = 0x001;

	/**
	 * Flag (bit mask) indicating that method label include method parameter types.
	 */
	public final static int SHOW_PARAMETERS = 0x002;

	/**
	 * Flag (bit mask) indicating that method label include thrown exception.
	 */
	public final static int SHOW_EXCEPTION = 0x004;

	/**
	 * Flag (bit mask) indicating that the label should show the icons with no space
	 * reserved for overlays.
	 */
	public final static int SHOW_SMALL_ICONS = 0x100;

	/**
	 * Flag (bit mask) indicating that the label should include overlay icons
	 * for element type and modifiers.
	 */
	public final static int SHOW_OVERLAY_ICONS = 0x010;

	/**
	 * Flag (bit mask) indicating that Complation Units, Class Files, Types, Declarations and Members
	 * should be rendered qualified.
	 * Examples: java.lang.String, java.util.Vector.size()
	 * 
	 * @since 2.0
	 */
	public final static int SHOW_QUALIFIED=				0x400;

	/**
	 * Flag (bit mask) indicating that Compilation Units, Class Files, Types, Declarations and Members
	 * should be rendered qualified. The qualification is appended
	 * Examples: String - java.lang, size() - java.util.Vector
	 * 
	 * @since 2.0
	 */
	public final static int SHOW_POST_QUALIFIED=	0x800;	
	
	
	/**
	 * Constant (value <code>0</code>) indicating that the label should show 
	 * the basic images only.
	 */
	public final static int SHOW_BASICS= 0x000;
	
	
	public final static int SHOW_DEFAULT= new Integer(SHOW_PARAMETERS | SHOW_OVERLAY_ICONS).intValue();
	
	private volatile WorkbenchLabelProvider fWorkbenchLabelProvider;
	protected CElementImageProvider fImageLabelProvider;
	private CUILabelProvider fCElementLabelProvider;

	private int fFlags;
	private int fImageFlags;
	private int fTextFlags;

	public CElementLabelProvider() {
		this(SHOW_DEFAULT);
	}

	public CElementLabelProvider(int flags) {
		// WorkbenchLabelProvider may only be initialized on the UI thread
		// http://bugs.eclipse.org/247274
		if (Display.getCurrent() != null) {
			fWorkbenchLabelProvider= new WorkbenchLabelProvider();
		} else {
			// Delay initialization
			CUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (fCElementLabelProvider != null) {
						fWorkbenchLabelProvider= new WorkbenchLabelProvider();
					}
				}});
		}
		fImageLabelProvider= new CElementImageProvider();

		fFlags = flags;
		fCElementLabelProvider= new CUILabelProvider(getTextFlags() | CElementLabels.TEMPLATE_PARAMETERS, getImageFlags());
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ICElement) {
			return fCElementLabelProvider.getText(element);
		}
		if (fWorkbenchLabelProvider != null) {
			return fWorkbenchLabelProvider.getText(element);
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		return fImageLabelProvider.getImageLabel(element, getImageFlags());
	}
	
	@Override
	public void dispose() {
		if (fCElementLabelProvider != null) {
			fCElementLabelProvider.dispose();
			fCElementLabelProvider= null;
		}
		if (fWorkbenchLabelProvider != null) {
			fWorkbenchLabelProvider.dispose();
			fWorkbenchLabelProvider= null;
		}
		if(fImageLabelProvider != null) {
			fImageLabelProvider.dispose();
		}
	}

	private boolean getFlag(int flag) {
		return (fFlags & flag) != 0;
	}

	/**
	 * Gets the image flags.
	 * Can be overwritten by super classes.
	 * @return Returns a int
	 */
	public int getImageFlags() {
		fImageFlags = 0;
		if (getFlag(SHOW_OVERLAY_ICONS)) {
			fImageFlags |= CElementImageProvider.OVERLAY_ICONS;
		}
		if (getFlag(SHOW_SMALL_ICONS)) {
			fImageFlags |= CElementImageProvider.SMALL_ICONS;
		}
		return fImageFlags;
	}

	/**
	 * Gets the text flags. Can be overwritten by super classes.
	 * @return Returns a int
	 */
	public int getTextFlags() {
		fTextFlags = 0;
		if (getFlag(SHOW_RETURN_TYPE)) {
			fTextFlags |= CElementLabels.M_APP_RETURNTYPE;
		}
		if (getFlag(SHOW_PARAMETERS)) {
			fTextFlags |= CElementLabels.M_PARAMETER_TYPES;
		}
		if (getFlag(SHOW_EXCEPTION)) {
			fTextFlags |= CElementLabels.M_EXCEPTIONS;
		}
		if (getFlag(SHOW_POST_QUALIFIED)) {
			fTextFlags |= CElementLabels.M_POST_QUALIFIED;
		}
		return fTextFlags;
	}
}
