/** ====================================================================
 *   Copyright 2003-2011 Fabrizio Giustina.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * ====================================================================
 */
package org.eclipse.cdt.codan.ui;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/***
 * A field editor for displaying labels not associated with other widgets.
 *
 * @author fgiust
 * @version $Revision: 1.1 $ ($Author: elaskavaia $)
 * @since 2.0
 */
public class LabelFieldEditor extends FieldEditor {
	/***
	 * Label for this field editor.
	 */
	private Label label;

	/***
	 * All labels can use the same preference name since they don't store any
	 * preference.
	 *
	 * @param labelText text for the label
	 * @param parent Composite
	 */
	public LabelFieldEditor(String labelText, Composite parent) {
		super("label", labelText, parent); //$NON-NLS-1$
	}

	/***
	 * Adjusts the field editor to be displayed correctly for the given number
	 * of columns.
	 *
	 * @param numColumns number of columns
	 */
	@Override
	protected void adjustForNumColumns(int numColumns) {
		((GridData) this.label.getLayoutData()).horizontalSpan = numColumns;
	}

	/***
	 * Fills the field editor's controls into the given parent.
	 *
	 * @param parent Composite
	 * @param numColumns cumber of columns
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		this.label = getLabelControl(parent);
		GridData gridData = new GridData();
		gridData.horizontalSpan = numColumns;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.grabExcessVerticalSpace = false;
		this.label.setLayoutData(gridData);
	}

	/***
	 * Returns the number of controls in the field editor.
	 *
	 * @return 1
	 */
	@Override
	public int getNumberOfControls() {
		return 1;
	}

	/***
	 * Labels do not persist any preferences, so this method is empty.
	 */
	@Override
	protected void doLoad() {
	}

	/***
	 * Labels do not persist any preferences, so this method is empty.
	 */
	@Override
	protected void doLoadDefault() {
	}

	/***
	 * Labels do not persist any preferences, so this method is empty.
	 */
	@Override
	protected void doStore() {
	}
}
