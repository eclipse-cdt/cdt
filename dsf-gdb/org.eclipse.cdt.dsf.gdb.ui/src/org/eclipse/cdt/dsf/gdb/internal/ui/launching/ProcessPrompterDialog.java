/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *     Marc Khouzam (Ericsson) - Add support for multi-attach (Bug 293679)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.util.Arrays;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

/**
 * Process prompter that allows the user to select one or more entries
 * in the top pane.  Those entries are displayed in the bottom pane.
 * No selection is allowed in the bottom pane.  The result returned
 * is the list of all selections of the top pane (shown in the bottom
 * pane).
 * 
 * The dialog also has a "New..." button that allows to start a new
 * process. If the method getBinaryPath() returns a non-null string, 
 * it implies that a new process should be created and the return
 * string indicates the location of the binary.
 * 
 * Note that getBinaryPath() should be checked before calling getResult()
 * as it takes precedence over it.
 *
 */
public class ProcessPrompterDialog extends TwoPaneElementSelector {
	private static final int NEW_BUTTON_ID = 9876;
	private String fBinaryPath;
	private boolean fSupportsNewProcess;

	public ProcessPrompterDialog(Shell parent, ILabelProvider elementRenderer,
			ILabelProvider qualifierRenderer, boolean supportsNewProcess) {
		super(parent, elementRenderer, qualifierRenderer);
		fSupportsNewProcess = supportsNewProcess;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button newButton = createButton(
				parent, NEW_BUTTON_ID, LaunchUIMessages.getString("ProcessPrompterDialog.New"), false); //$NON-NLS-1$
		newButton.setEnabled(fSupportsNewProcess);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == NEW_BUTTON_ID) {
			FileDialog fd = new FileDialog(getShell(), SWT.NONE);
			fBinaryPath = fd.open();

			setReturnCode(OK);
			close();
		}
		super.buttonPressed(buttonId);
	}
	
	public String getBinaryPath() {
		return fBinaryPath;
	}
	
	/*
	 * The result should be every selected element.
	 */
    @Override
    protected void computeResult() {
        setResult(Arrays.asList(getSelectedElements()));
    }

    /*
     * Disable the ability to select items in the bottom pane.
     */
    @Override
    protected Table createLowerList(Composite parent) {
    	final Table list = super.createLowerList(parent);
    	
    	// First remove listeners such as the double click.
    	// We don't want the user to trigger the action by
    	// double-clicking on the bottom pane.
    	int[] events = { SWT.Selection, SWT.MouseDoubleClick };
    	for (int event : events) {
    		Listener[] selectionListeners = list.getListeners(event);
    		for (Listener listener : selectionListeners) {
    			list.removeListener(event, listener);
    		}
    	}
    	
    	// Now add a listener to prevent selection
    	list.addListener(SWT.EraseItem, new Listener() {
            @Override
    	    public void handleEvent(Event event) {
    	    	if ((event.detail & SWT.SELECTED) != 0) {
    	    		event.detail &= ~SWT.SELECTED;
    	    		// Removing the SELECTED event did not work properly.
    	    		// The foreground text became invisible.
    	    		// Let's simply deselect everything
    	    		list.deselectAll();
    	    	}
    	    }
    	});
    	return list;
    }
}
