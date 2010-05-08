/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.dialogs;

import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemParameterInfo;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author Alena
 * 
 */
public class ParametersComposite extends Composite {
	private FieldEditorPreferencePage page;
	private IProblem problem;
	private PreferenceStore pref;

	/**
	 * @param parent
	 * @param problem
	 * @param style
	 */
	public ParametersComposite(Composite parent, final IProblem problem) {
		super(parent, SWT.NONE);
		if (problem == null)
			throw new NullPointerException();
		this.setLayout(new GridLayout(2, false));
		this.problem = problem;
		this.pref = new PreferenceStore();
		page = new FieldEditorPreferencePage() {
			@Override
			protected void createFieldEditors() {
				noDefaultAndApplyButton();
				IProblemParameterInfo parameterInfo = problem
						.getParameterInfo();
				createFieldEditorsForParameters(parameterInfo);
			}

			/**
			 * @param info
			 */
			private void createFieldEditorsForParameters(
					IProblemParameterInfo info) {
				if (info == null)
					return;
				switch (info.getType()) {
				case TYPE_STRING:
					StringFieldEditor fe = new StringFieldEditor(info.getKey(),
							info.getLabel(), getFieldEditorParent());
					addField(fe);
					break;
				default:
					throw new UnsupportedOperationException(info.getType()
							.toString());
				}
			}
		};
		IProblemParameterInfo info = problem.getParameterInfo();
		if (info == null) {
			Label label = new Label(this, 0);
			label.setText(CodanUIMessages.ParametersComposite_None);
		}
		initPrefStore(info);
		page.setPreferenceStore(pref);
		page.createControl(parent);
		page.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	public void save(IProblemWorkingCopy problemwc) {
		page.performOk();
		IProblemParameterInfo info = problemwc.getParameterInfo();
		savePrefStore(info, problemwc);
	}

	/**
	 * @param info
	 * @param problemwc
	 */
	private void savePrefStore(IProblemParameterInfo info,
			IProblemWorkingCopy problemwc) {
		if (info == null)
			return;
		String key = info.getKey();
		Object parameter = problem.getParameter(key);
		if (parameter instanceof String) {
			String newValue = pref.getString(key);
			problemwc.setParameter(key, newValue);
		} else
			throw new UnsupportedOperationException(info.getType().toString());
	}

	/**
	 * @param info
	 */
	private void initPrefStore(IProblemParameterInfo info) {
		if (info == null)
			return;
		String key = info.getKey();
		Object parameter = problem.getParameter(key);
		if (parameter instanceof String) {
			pref.setDefault(key, (String) parameter);
			pref.setValue(key, (String) parameter);
		} else
			throw new UnsupportedOperationException(info.getType().toString());
	}
}
