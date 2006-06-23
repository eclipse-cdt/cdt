/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.c.hover;

import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.text.HTMLTextPresenter;
import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.IKeySequenceBinding;
import org.eclipse.ui.keys.KeySequence;

/**
 * AbstractCEditorTextHover
 * Abstract class for providing hover information for C elements.
 * 
 */
public class AbstractCEditorTextHover implements ICEditorTextHover, ITextHoverExtension {

	private IEditorPart fEditor;
	private ICommand fCommand;
//	{
//		ICommandManager commandManager= PlatformUI.getWorkbench().getCommandSupport().getCommandManager();
//		fCommand= commandManager.getCommand(ICEditorActionDefinitionIds.SHOW_JAVADOC);
//		if (!fCommand.isDefined())
//			fCommand= null;
//	}

	/*
	 * @see IJavaEditorTextHover#setEditor(IEditorPart)
	 */
	public void setEditor(IEditorPart editor) {
		fEditor= editor;
	}

	protected IEditorPart getEditor() {
		return fEditor;
	}
	
	/*
	 * @see ITextHover#getHoverRegion(ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return CWordFinder.findWord(textViewer.getDocument(), offset);
	}

//	protected ICodeAssist getCodeAssist() {
//		if (fEditor != null) {
//			IEditorInput input= fEditor.getEditorInput();
//			if (input instanceof IClassFileEditorInput) {
//				IClassFileEditorInput cfeInput= (IClassFileEditorInput) input;
//				return cfeInput.getClassFile();
//			}
//			
//			IWorkingCopyManager manager= CUIPlugin.getDefault().getWorkingCopyManager();				
//			return manager.getWorkingCopy(input);
//		}
//		
//		return null;
//	}
	

	/*
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
	
//		ICodeAssist resolve= getCodeAssist();
//		if (resolve != null) {
//			try {
//				ICElement[] result= null;
//				
//				synchronized (resolve) {
//					result= resolve.codeSelect(hoverRegion.getOffset(), hoverRegion.getLength());
//				}
//				
//				if (result == null)
//					return null;
//				
//				int nResults= result.length;	
//				if (nResults == 0)
//					return null;
//				
//				return getHoverInfo(result);
//				
//			} catch (CModelException x) {
//				CUIPlugin.log(x.getStatus());
//			}
//		}
		return null;
	}

	/**
	 * Provides hover information for the given C elements.
	 * 
	 * @param cElements the C elements for which to provide hover information
	 * @return the hover information string
	 */
	protected String getHoverInfo(ICElement[] cElements) {
		return null;
	}

	/*
	 * @see ITextHoverExtension#getHoverControlCreator()
	 * @since 3.0
	 */
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, SWT.NONE, new HTMLTextPresenter(true), getTooltipAffordanceString());
			}
		};
	}
	
	/**
	 * Returns the tool tip affordance string.
	 * 
	 * @return the affordance string or <code>null</code> if disabled or no key binding is defined
	 * @since 3.0
	 */
	protected String getTooltipAffordanceString() {
//		if (!CUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE))
//			return null;
//		
//		KeySequence[] sequences= getKeySequences();
//		if (sequences == null)
//			return null;
//		
//		String keySequence= sequences[0].format();
//		return CHoverMessages.getFormattedString("JavaTextHover.makeStickyHint", keySequence); //$NON-NLS-1$
		return null;
	}

	/**
	 * Returns the array of valid key sequence bindings for the
	 * show tool tip description command.
	 * 
	 * @return the array with the {@link KeySequence}s
	 * 
	 * @since 3.0
	 */
	private KeySequence[] getKeySequences() {
		if (fCommand != null) {
			List list= fCommand.getKeySequenceBindings();
			if (!list.isEmpty()) {
				KeySequence[] keySequences= new KeySequence[list.size()];
				for (int i= 0; i < keySequences.length; i++) {
					keySequences[i]= ((IKeySequenceBinding) list.get(i)).getKeySequence();
				}
				return keySequences;
			}		
		}
		return null;
	}

}
