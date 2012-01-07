/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;

/**
 * Configures C Editor typing preferences.
 */
class CodeStyleBlock extends OptionsConfigurationBlock {
	private static final Key CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER = getCDTUIKey(PreferenceConstants.CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER);

	private static Key[] getAllKeys() {
		return new Key[] {
				CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER,
			};
	}

	public CodeStyleBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getAllKeys(), container);
	}
	
	@Override
	public Control createContents(Composite parent) {
		ScrolledPageContent scrolled= new ScrolledPageContent(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolled.setExpandHorizontal(true);
		scrolled.setExpandVertical(true);
		
		Composite control= new Composite(scrolled, SWT.NONE);
		GridLayout layout= new GridLayout();
		control.setLayout(layout);

		Composite composite = addSubsection(control, PreferencesMessages.CodeStyleBlock_class_member_order); 
		fillClassMemberOrderSection(composite);
		
		scrolled.setContent(control);
		final Point size= control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrolled.setMinSize(size.x, size.y);
		return scrolled;
	}

	private void fillClassMemberOrderSection(Composite composite) {
		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		composite.setLayout(layout);

		addRadioButton(composite, PreferencesMessages.CodeStyleBlock_public_private,
				CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER, FALSE_TRUE, 0);
		addRadioButton(composite, PreferencesMessages.CodeStyleBlock_private_public,
				CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER, TRUE_FALSE, 0);
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
	}
}
