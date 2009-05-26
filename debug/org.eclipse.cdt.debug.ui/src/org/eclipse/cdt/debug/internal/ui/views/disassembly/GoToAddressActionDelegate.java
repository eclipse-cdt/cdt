/*******************************************************************************
 *  Copyright (c) 2009 QNX Software Systems and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.disassembly;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.IDisassembly;
import org.eclipse.cdt.debug.core.model.IDisassemblyBlock;
import org.eclipse.cdt.debug.internal.core.model.Disassembly;
import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyEditorInput;
import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyView;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class GoToAddressActionDelegate implements IActionDelegate, IViewActionDelegate {
	private IViewPart fView;
	private IAction fAction;
	private DisassemblyView fDisassemblyView;

	public void init(IViewPart view) {
		setView(view);
		if (view instanceof DisassemblyView) {
			fDisassemblyView = (DisassemblyView) view;
		}
	}

	private void setView(IViewPart view) {
		fView = view;
	}

	protected IViewPart getView() {
		return fView;
	}

	public void run(IAction action) {
		String address;
		InputDialog dialog = new InputDialog(fView.getViewSite().getShell(), "Enter address", "Enter address to go to", "",
		        new IInputValidator() {
			        public String isValid(String in) {
				        try {
					        String input = in.trim();
					        if (input.length() == 0)
						        return "Cannot be empty address";
					        if (input.toLowerCase().startsWith("0x")) {
						        Long.parseLong(input.substring(2), 16);
					        } else {
						        Long.parseLong(input);
					        }
					        return null;
				        } catch (NumberFormatException ex) {
					        return "Must be a hexadecimal or decimal address";
				        }
			        }
		        });
		if (dialog.open() == Window.OK) {
			address = dialog.getValue();
			gotoAddress(address);
		}
	}

	protected void setAction(IAction action) {
		fAction = action;
	}

	protected IAction getAction() {
		return fAction;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		setAction(action);
	}

	private void gotoAddress(String addr) {
		IAddress address = new Addr32(addr);
		if (fDisassemblyView != null) {
			ICDebugTarget target = null;
			ISelection selection = fDisassemblyView.getSite().getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
			if (selection instanceof IStructuredSelection) {
				Object element = ((IStructuredSelection) selection).getFirstElement();
				if (element instanceof ICStackFrame) {
					IDebugTarget tar = ((ICStackFrame) element).getDebugTarget();
					if (tar instanceof ICDebugTarget) {
						target = (ICDebugTarget) tar;
					}
				}
			}
			DisassemblyEditorInput input = null;
			try {
				input = create(target, address);
				fDisassemblyView.setViewerInput(input);
			} catch (DebugException e) {
				MessageDialog dialog = new MessageDialog(fView.getViewSite().getShell(), "Wrong address", null,
				        "Cannot access memory at address " + addr, MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL },
				        0);
				dialog.open();
			}
		}
	}

	public static DisassemblyEditorInput create(ICDebugTarget target, IAddress address) throws DebugException {
		DisassemblyEditorInput input = null;
		IDisassembly disassembly = target.getDisassembly();
		if (disassembly instanceof Disassembly) {
			IDisassemblyBlock block = ((Disassembly) disassembly).getDisassemblyBlock(address);
			input = DisassemblyEditorInput.create(block);
		}
		return input;
	}


}

