/*******************************************************************************
 * Copyright (c) 2010, 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Marc Khouzam (Ericsson) - Added support for collecting char pointers as strings (bug 373707)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.tracepointactions;

import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
import org.eclipse.cdt.debug.ui.breakpointactions.IBreakpointActionPage;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.CollectAction;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @since 2.1
 */
public class CollectActionPage extends PlatformObject implements IBreakpointActionPage {

	/**
	 * An exception to indicate that the user-specified string limit is invalid
	 */
	private class IllegalCollectStringLimitException extends Exception {
		private static final long serialVersionUID = -2087722354642237691L;
		public IllegalCollectStringLimitException(String message) {
			super(message);
		}
	}
	
	private Text fCollectString;
	private Button fTreatCharPtrAsStrings;
	private Text fTreatCharPtrAsStringsLimit;

	private CollectAction fCollectAction;

	/**
	 * Create the composite
	 */
	private Composite createCollectActionComposite(Composite parent, int style) {
		Composite composite = new Composite(parent, style);
		composite.setLayout(new GridLayout(2, false));

		// The label asking for what to collect
		final Label collectLabel = new Label(composite, SWT.NONE);
		collectLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		collectLabel.setText(MessagesForTracepointActions.TracepointActions_Collect_Label);

		// The user-specified string of what to collect
		fCollectString = new Text(composite, SWT.BORDER);
		fCollectString.setText(fCollectAction.getCollectString());
		fCollectString.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		// An option to collect character pointers as strings
		fTreatCharPtrAsStrings = new Button(composite, SWT.CHECK);
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd.verticalIndent = 15;
		// Store the button width before we add the text as we only care about the checkbox width
		int buttonWidth = fTreatCharPtrAsStrings.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		fTreatCharPtrAsStrings.setText(MessagesForTracepointActions.TracepointActions_Collect_Strings_Label);
		fTreatCharPtrAsStrings.setLayoutData(gd);
		fTreatCharPtrAsStrings.setSelection(fCollectAction.getCharPtrAsStrings());
		fTreatCharPtrAsStrings.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Disable/enable the limit field
				fTreatCharPtrAsStringsLimit.setEnabled(fTreatCharPtrAsStrings.getSelection());
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Disable/enable the limit field
				fTreatCharPtrAsStringsLimit.setEnabled(fTreatCharPtrAsStrings.getSelection());
			}
		});
		
		// A label asking for an optional limit of bytes of collected strings 
		final Label limitLabel = new Label(composite, SWT.NONE);
		gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gd.horizontalIndent = buttonWidth;
		limitLabel.setLayoutData(gd);
		limitLabel.setText(MessagesForTracepointActions.TracepointActions_Collect_Strings_Limit_Label);

		// A user-specified limit of bytes
		fTreatCharPtrAsStringsLimit = new Text(composite, SWT.BORDER);
		fTreatCharPtrAsStringsLimit.setText(getCharPtrAsStringLimit(fCollectAction.getCharPtrAsStringsLimit()));

		gd = new GridData(SWT.FILL, SWT.CENTER, false, false); 
		gd.horizontalIndent = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth(); 
		fTreatCharPtrAsStringsLimit.setLayoutData(gd);
		fTreatCharPtrAsStringsLimit.setEnabled(fTreatCharPtrAsStrings.getSelection());

		final ControlDecoration decoration = new ControlDecoration(fTreatCharPtrAsStringsLimit, SWT.TOP | SWT.LEFT, composite );
		decoration.hide();
		fTreatCharPtrAsStringsLimit.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				try {
					getCharPtrAsStringLimit(fTreatCharPtrAsStringsLimit.getText());
					decoration.hide();
				} catch (IllegalCollectStringLimitException exception) {
					decoration.setImage( 
							FieldDecorationRegistry.getDefault().getFieldDecoration( 
									FieldDecorationRegistry.DEC_ERROR).getImage());
					decoration.setDescriptionText(exception.getMessage());
					decoration.show();
				}				
			}
		});


		return composite;
	}

	public CollectAction getCollectAction() {
		return fCollectAction;
	}

    @Override
	public void actionDialogCanceled() {
	}

    @Override
	public void actionDialogOK() {
		fCollectAction.setCollectString(fCollectString.getText());
		fCollectAction.setCharPtrAsStrings(fTreatCharPtrAsStrings.getSelection());
		
		try {
			Integer limit = getCharPtrAsStringLimit(fTreatCharPtrAsStringsLimit.getText());
			fCollectAction.setCharPtrAsStringsLimit(limit);
		} catch (IllegalCollectStringLimitException e) {
			// ignore and keep old value
		}
	}

    @Override
	public Composite createComposite(IBreakpointAction action, Composite composite, int style) {
		fCollectAction = (CollectAction) action;
		return createCollectActionComposite(composite, style);
	}

    /**
     * Convert the user-specified string into an integer.
     * If the string is not valid, disable the limit by using null.
     * @param limitStr The string provided by the user
     * @return An non-negative integer limit, or null for no limit.
     */
    private Integer getCharPtrAsStringLimit(String limitStr) throws IllegalCollectStringLimitException {
    	limitStr = limitStr.trim();
    	Integer limit = null;
		try {
			limit = Integer.parseInt(limitStr);
			if (limit < 0) {
				throw new IllegalCollectStringLimitException(MessagesForTracepointActions.TracepointActions_Collect_Strings_Limit_Error);
			}
		} catch (NumberFormatException e) {
			if (!limitStr.isEmpty()) {
				// We only accept an empty string, which means no limit
				throw new IllegalCollectStringLimitException(MessagesForTracepointActions.TracepointActions_Collect_Strings_Limit_Error);		
			}
		}
		return limit;
    }
    
    /**
     * Convert the integer limit into a string.
     * If the string is not valid, disable the limit by using null.
     * @param limit The integer limit to convert.  Can be null for no limit.
     * @return The limit as a string, where no limit or a negative limit is the empty string.
     */
    private String getCharPtrAsStringLimit(Integer limit) {
    	if (limit == null || limit < 0) return ""; //$NON-NLS-1$
		return Integer.toString(limit);
    }
}
