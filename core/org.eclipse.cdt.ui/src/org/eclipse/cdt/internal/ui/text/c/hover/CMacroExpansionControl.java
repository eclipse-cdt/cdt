/*******************************************************************************
 * Copyright (c) 2007, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.c.hover;

import org.eclipse.cdt.internal.ui.text.AbstractSourceViewerInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * Information control for macro expansion.
 *
 * @since 5.0
 */
public class CMacroExpansionControl extends AbstractSourceViewerInformationControl {

	private CMacroExpansionInput fInput;

	/**
	 * Creates a new control for use as a hover which does not take the focus.
	 *
	 * @param parent  parent shell
	 * @param statusFieldText  text to be displayed in the status field, may be <code>null</code>
	 */
	public CMacroExpansionControl(Shell parent, String statusFieldText) {
		super(parent, statusFieldText);
		setTitleText(CHoverMessages.CMacroExpansionControl_title_macroExpansion);
	}

	/**
	 * Creates a new control for use as a hover which optionally takes the focus.
	 *
	 * @param parent  parent shell
	 * @param isResizable  whether this control should be resizable
	 */
	public CMacroExpansionControl(Shell parent, boolean isResizable) {
		super(parent, isResizable);
		setTitleText(CHoverMessages.CMacroExpansionControl_title_macroExpansion);
	}

	@Override
	protected boolean hasHeader() {
		return true;
	}

	@Override
	public void setInput(Object input) {
		if (input instanceof CMacroExpansionInput) {
			CMacroExpansionInput macroExpansionInput = (CMacroExpansionInput) input;
			setInformation(macroExpansionInput.fExplorer.getFullExpansion().getCodeAfterStep());
			fInput = macroExpansionInput;
			updateStatusText();
		} else {
			fInput = null;
			super.setInput(input);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (!visible) {
			setInput(null);
		}
	}

	private void updateStatusText() {
		if (fInput == null) {
			return;
		}
		if (fInput.fExplorer.getExpansionStepCount() > 1) {
			IBindingService bindingService = PlatformUI.getWorkbench().getAdapter(IBindingService.class);
			if (bindingService != null) {
				String keySequence = bindingService
						.getBestActiveBindingFormattedFor(ITextEditorActionDefinitionIds.SHOW_INFORMATION);
				if (keySequence != null) {
					setStatusText(NLS.bind(CHoverMessages.CMacroExpansionControl_exploreMacroExpansion, keySequence));
				}
			}
		}
	}

	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				if (fInput != null && fInput.fExplorer.getExpansionStepCount() > 1) {
					return new CMacroExpansionExplorationControl(parent, true);
				} else {
					return new CMacroExpansionControl(parent, true);
				}
			}
		};
	}
}
