/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.c.hover;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.Splitter;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.DocumentRangeNode;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.rewrite.MacroExpansionExplorer.IMacroExpansionStep;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.ui.editor.CSourceViewer;
import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;
import org.eclipse.cdt.internal.ui.text.AbstractCompareViewerInformationControl;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.text.SimpleCSourceViewerConfiguration;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.PixelConverter;

/**
 * Information control for macro expansion exploration.
 *
 * @since 5.0
 */
public class CMacroExpansionExplorationControl extends AbstractCompareViewerInformationControl {

	private static final String COMMAND_ID_EXPANSION_BACK= "org.eclipse.cdt.ui.hover.backwardMacroExpansion"; //$NON-NLS-1$
	private static final String COMMAND_ID_EXPANSION_FORWARD= "org.eclipse.cdt.ui.hover.forwardMacroExpansion"; //$NON-NLS-1$
	private static final String CONTEXT_ID_MACRO_EXPANSION_HOVER= "org.eclipse.cdt.ui.macroExpansionHoverScope"; //$NON-NLS-1$

	private static class CDiffNode extends DocumentRangeNode implements ITypedElement {
		public CDiffNode(DocumentRangeNode parent, int type, String id, IDocument doc, int start, int length) {
			super(parent, type, id, doc, start, length);
		}
		public CDiffNode(int type, String id, IDocument doc, int start, int length) {
			super(type, id, doc, start, length);
		}
		public String getName() {
			return getId();
		}
		public String getType() {
			return "c2"; //$NON-NLS-1$
		}
		public Image getImage() {
			return null;
		}
	}

	private IHandlerService fHandlerService;
	private Collection<IHandlerActivation> fHandlerActivations;
	private IContextService fContextService;
	private IContextActivation fContextActivation;
	private int fIndex;
	private CMacroExpansionInput fInput;
	private CMacroCompareViewer fMacroCompareViewer;
	private ISourceViewer fMacroViewer;
	private StyledText fMacroText;

	/**
	 * Creates a new control for use as a "quick view" where the control immediately takes the focus.
	 * 
	 * @param parent  parent shell
	 * @param input  the input object, may be <code>null</code>
	 */
	public CMacroExpansionExplorationControl(Shell parent, CMacroExpansionInput input) {
		super(parent, SWT.RESIZE, SWT.NONE, true, true, true);
		setMacroExpansionInput(input);
	}

	/**
	 * Creates a new control for use as a "quick view" where the control immediately takes the focus.
	 * 
	 * @param parent  parent shell
	 */
	public CMacroExpansionExplorationControl(Shell parent) {
		this(parent, null);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractSourceViewerInformationControl#hasHeader()
	 */
	@Override
	protected boolean hasHeader() {
		return true;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractCompareViewerInformationControl#createCompareViewerControl(org.eclipse.swt.widgets.Composite, int, org.eclipse.compare.CompareConfiguration)
	 */
	@Override
	protected CompareViewerControl createCompareViewerControl(Composite parent, int style, CompareConfiguration compareConfig) {
		Splitter splitter= new Splitter(parent, SWT.VERTICAL);
		splitter.setLayoutData(new GridData(GridData.FILL_BOTH));
		fMacroViewer= createSourceViewer(splitter, style | SWT.V_SCROLL | SWT.H_SCROLL);
		CompareViewerControl control= super.createCompareViewerControl(splitter, style, compareConfig);
		splitter.setWeights(new int[] { 20, 80 });
		return control;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractCompareViewerInformationControl#createContentViewer(org.eclipse.swt.widgets.Composite, org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.CompareConfiguration)
	 */
	@Override
	protected Viewer createContentViewer(Composite parent, ICompareInput input, CompareConfiguration cc) {
		fMacroCompareViewer= new CMacroCompareViewer(parent, SWT.NULL, cc);
		if (fInput != null) {
			fMacroCompareViewer.setMacroExpansionInput(fInput);
			fMacroCompareViewer.setMacroExpansionStep(fIndex);
		}
		return fMacroCompareViewer;
	}

	protected ISourceViewer createSourceViewer(Composite parent, int style) {
		IPreferenceStore store= CUIPlugin.getDefault().getCombinedPreferenceStore();
		SourceViewer sourceViewer= new CSourceViewer(parent, null, null, false, style, store);
		CTextTools tools= CUIPlugin.getDefault().getTextTools();
		sourceViewer.configure(new SimpleCSourceViewerConfiguration(tools.getColorManager(), store, null, ICPartitions.C_PARTITIONING, false));
		sourceViewer.setEditable(false);

		fMacroText= sourceViewer.getTextWidget();

		Font font= JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
		fMacroText.setFont(font);

		GridData gd= new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
		gd.heightHint= fMacroText.getLineHeight() * 2;
		fMacroText.setLayoutData(gd);
		fMacroText.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		
		final Document doc= new Document();
		CUIPlugin.getDefault().getTextTools().setupCDocument(doc);
		sourceViewer.setDocument(doc);
		return sourceViewer;
	}


	@Override
	public int open() {
		getShell().addListener(SWT.Activate, new Listener() {
			public void handleEvent(Event arg0) {
				registerCommandHandlers();
			}});
		getShell().addListener(SWT.Deactivate, new Listener() {
			public void handleEvent(Event arg0) {
				unregisterCommandHandlers();
			}});

		return super.open();
	}

	protected void unregisterCommandHandlers() {
		if (fHandlerService != null) {
			fHandlerService.deactivateHandlers(fHandlerActivations);
			fHandlerActivations.clear();
			fHandlerService= null;
		}
		if (fContextActivation != null) {
			fContextService.deactivateContext(fContextActivation);
			fContextActivation= null;
		}
	}

	protected void registerCommandHandlers() {
        IHandler backwardHandler= new AbstractHandler() {
            public Object execute(ExecutionEvent event) throws ExecutionException {
                backward();
                return null;
            }
        };
        IHandler forwardHandler= new AbstractHandler() {
            public Object execute(ExecutionEvent event) throws ExecutionException {
                forward();
                return null;
            }
        };
        IHandler gotoDefinitionHandler= new AbstractHandler() {
            public Object execute(ExecutionEvent event) throws ExecutionException {
                gotoMacroDefinition();
                return null;
            }
        };

        IWorkbench workbench= PlatformUI.getWorkbench();
        fHandlerService= (IHandlerService) workbench.getService(IHandlerService.class);
        fContextService= (IContextService) workbench.getService(IContextService.class);
        fContextActivation= fContextService.activateContext(CONTEXT_ID_MACRO_EXPANSION_HOVER);
        fHandlerActivations= new ArrayList<IHandlerActivation>();
        fHandlerActivations.add(fHandlerService.activateHandler(COMMAND_ID_EXPANSION_BACK, backwardHandler));
        fHandlerActivations.add(fHandlerService.activateHandler(COMMAND_ID_EXPANSION_FORWARD, forwardHandler));
        fHandlerActivations.add(fHandlerService.activateHandler(ICEditorActionDefinitionIds.OPEN_DECL, gotoDefinitionHandler));

        String infoText= getInfoText();
        if (infoText != null) {
            setInfoText(infoText);
            //bug 234952 - truncation in the info label
            PixelConverter converter = new PixelConverter(getShell());
            Point pt = getShell().getSize();
            int stringLengthInPixel = converter.convertWidthInCharsToPixels(infoText.length()+5);
            if (pt.x < stringLengthInPixel) {
            	getShell().setSize(new Point(stringLengthInPixel, pt.y));
            }
        }
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractCompareViewerInformationControl#fillViewMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillViewMenu(IMenuManager viewMenu) {
		super.fillViewMenu(viewMenu);
		final CommandContributionItemParameter params= new CommandContributionItemParameter(
				PlatformUI.getWorkbench(), null, ICEditorActionDefinitionIds.OPEN_DECL, CommandContributionItem.STYLE_PUSH);
		viewMenu.add(new CommandContributionItem(params));
	}

	protected final void gotoMacroDefinition() {
		int index= fIndex < getStepCount() ? fIndex : 0;
		final IMacroExpansionStep step= fInput.fExplorer.getExpansionStep(index);
		IASTFileLocation fileLocation= step.getLocationOfExpandedMacroDefinition();
		if (fileLocation != null) {
			final IPath path= new Path(fileLocation.getFileName());
			final int offset= fileLocation.getNodeOffset();
			final int length= fileLocation.getNodeLength();
			IEditorPart editor;
			try {
				editor = EditorUtility.openInEditor(path, null);
				if (editor instanceof ITextEditor) {
					ITextEditor textEditor = (ITextEditor)editor;
					textEditor.selectAndReveal(offset, length);
				}
				dispose();
			} catch (PartInitException exc) {
				CUIPlugin.log(exc);
			}
		}
	}

	protected final void forward() {
		fIndex= fixIndex(fIndex + 1);
		if (fIndex > getStepCount()) {
			fIndex= 0;
		}
		showExpansion();
	}

	protected final void backward() {
		--fIndex;
		if (fIndex < 0) {
			fIndex= fixIndex(getStepCount());
		}
		showExpansion();
	}

	/**
	 * Returns the text to be shown in the popups's information area.
	 * May return <code>null</code>.
	 *
	 * @return The text to be shown in the popup's information area or <code>null</code>
	 */
	private String getInfoText() {
		IWorkbench workbench= PlatformUI.getWorkbench();
		IBindingService bindingService= (IBindingService) workbench.getService(IBindingService.class);
		String formattedBindingBack= bindingService.getBestActiveBindingFormattedFor(COMMAND_ID_EXPANSION_BACK);
		String formattedBindingForward= bindingService.getBestActiveBindingFormattedFor(COMMAND_ID_EXPANSION_FORWARD);

		String infoText= null;
		if (formattedBindingBack != null && formattedBindingForward != null) {
			infoText= NLS.bind(CHoverMessages.CMacroExpansionControl_statusText, formattedBindingBack, formattedBindingForward);
		}
		return infoText;
	}

	/*
	 * @see org.eclipse.jface.dialogs.PopupDialog#close()
	 */
	@Override
	public boolean close() {
		unregisterCommandHandlers();
		return super.close();
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractCompareViewerInformationControl#getId()
	 */
	@Override
	protected String getId() {
		return "org.eclipse.cdt.ui.text.hover.CMacroExpansionExploration"; //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractCompareViewerInformationControl#setInput(java.lang.Object)
	 */
	@Override
	public void setInput(Object input) {
		if (input instanceof CMacroExpansionInput) {
			setMacroExpansionInput((CMacroExpansionInput) input);
		} else {
			if (fMacroCompareViewer != null) {
				fMacroCompareViewer.setMacroExpansionStep(fIndex);
			}
			super.setInput(input);
		}
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractCompareViewerInformationControl#computeTrim()
	 */
	@Override
	public Rectangle computeTrim() {
		Rectangle trim= super.computeTrim();
		addInternalTrim(trim);
		return trim;
	}

	/**
	 * Adds the internal trimmings to the given trim of the shell.
	 * 
	 * @param trim the shell's trim, will be updated
	 * @since 5.0
	 */
	private void addInternalTrim(Rectangle trim) {
		Rectangle textTrim= fMacroText.computeTrim(0, 0, 0, 0);
		trim.x+= textTrim.x;
		trim.y+= textTrim.y;
		trim.width+= textTrim.width;
		trim.height+= textTrim.height;
	}

	/**
	 * Set the input for this information control.
	 * @param input
	 */
	private void setMacroExpansionInput(CMacroExpansionInput input) {
		fInput= input;
		if (fInput != null) {
			fIndex= fixIndex(input.fStartWithFullExpansion ? getStepCount() : 0);
			showExpansion();
		}
	}

	private int fixIndex(int index) {
		if (getStepCount() == 1 && index == 1) {
			return 0;
		}
		return index;
	}

	private int getStepCount() {
		return fInput.fExplorer.getExpansionStepCount();
	}

	private void showExpansion() {
		final int idxLeft= fIndex == getStepCount() ? 0 : fIndex;
		final int idxRight= fIndex + 1;

		CompareConfiguration config= getCompareConfiguration();
		config.setLeftLabel(getLabelForIndex(idxLeft));
		config.setRightLabel(getLabelForIndex(idxRight));

		final ITypedElement left= getContentForIndex(fIndex, true);
		final ITypedElement right= getContentForIndex(fIndex, false);
		
		setTitleText(CHoverMessages.bind(CHoverMessages.CMacroExpansionControl_title_macroExpansionExploration, getStepCount()));
		fMacroViewer.getDocument().set(getMacroText(fIndex));
		setInput(createCompareInput(null, left, right));
	}

	private String getLabelForIndex(int index) {
		if (index == 0) {
			return CHoverMessages.CMacroExpansionControl_title_original;
		} else if (index < getStepCount()) {
			return NLS.bind(CHoverMessages.CMacroExpansionControl_title_expansion,
					String.valueOf(index), String.valueOf(getStepCount()));
		} else {
			return CHoverMessages.CMacroExpansionControl_title_fullyExpanded;
		}
	}
	private Object createCompareInput(ITypedElement original, ITypedElement left, ITypedElement right) {
		Differencer d= new Differencer();
		return d.findDifferences(false, new NullProgressMonitor(), null, original, left, right);
	}

	private ITypedElement getContentForIndex(int index, boolean before) {
		final IMacroExpansionStep expansionStep;
		if (index < getStepCount()) {
			expansionStep= fInput.fExplorer.getExpansionStep(index);
		} else {
			expansionStep= fInput.fExplorer.getFullExpansion();
		}
		final String text;
		if (before) {
			text= expansionStep.getCodeBeforeStep();
		} else {
			text= expansionStep.getCodeAfterStep();
		}
		final Document doc= new Document(text);
		CUIPlugin.getDefault().getTextTools().setupCDocument(doc);
		return new CDiffNode(0, String.valueOf(index), doc, 0, text.length());
	}

	private String getMacroText(int index) {
		final String text;
		final int count= getStepCount();
		if (index < count) {
			final IMacroExpansionStep expansionStep= fInput.fExplorer.getExpansionStep(index);
			IMacroBinding binding= expansionStep.getExpandedMacro();
			StringBuffer buffer= new StringBuffer();
			buffer.append("#define ").append(binding.getName()); //$NON-NLS-1$
			char[][] params= binding.getParameterList();
			if (params != null) {
				buffer.append('(');
				for (int i= 0; i < params.length; i++) {
					if (i > 0) {
						buffer.append(',');
						buffer.append(' ');
					}
					char[] param= params[i];
					buffer.append(new String(param));
				}
				buffer.append(')');
			}
			buffer.append(' ');
			if (!binding.isDynamic()) {
				buffer.append(binding.getExpansionImage());
			}
			else {
				ReplaceEdit[] replacements= expansionStep.getReplacements();
				if (replacements.length == 1) {
					buffer.append(replacements[0].getText());
				}
			}
			text= buffer.toString();
		} else {
			text= ""; //$NON-NLS-1$
		}
		return text;
	}

}
