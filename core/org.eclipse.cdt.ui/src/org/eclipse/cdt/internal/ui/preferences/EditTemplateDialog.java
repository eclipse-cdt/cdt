package org.eclipse.cdt.internal.ui.preferences;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.internal.corext.template.ContextTypeRegistry;
import org.eclipse.cdt.internal.corext.template.Template;
import org.eclipse.cdt.internal.corext.template.TemplateMessages;
import org.eclipse.cdt.internal.corext.template.TemplateTranslator;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.dialogs.StatusDialog;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;
import org.eclipse.cdt.internal.ui.text.template.TemplateVariableProcessor;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;


/**
 * Dialog to edit a template.
 */
public class EditTemplateDialog extends StatusDialog {

	private static class SimpleCSourceViewerConfiguration extends CSourceViewerConfiguration {

		private final IContentAssistProcessor fProcessor;

		SimpleCSourceViewerConfiguration(CTextTools tools, CEditor editor, IContentAssistProcessor processor) {
			super(tools, editor);
			fProcessor= processor;
		}
		
		/*
		 * @see SourceViewerConfiguration#getContentAssistant(ISourceViewer)
		 */
		public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

			IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();

			ContentAssistant assistant= new ContentAssistant();
			assistant.setContentAssistProcessor(fProcessor, IDocument.DEFAULT_CONTENT_TYPE);

			//assistant.enableAutoInsert(store.getBoolean(ContentAssistPreference.AUTOINSERT));
			boolean enabled = (store.getBoolean(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOT) 
							|| store.getBoolean(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_ARROW)
							|| store.getBoolean(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOUBLECOLON)
			);
			assistant.enableAutoActivation(enabled);
			
			assistant.setAutoActivationDelay(store.getInt(ContentAssistPreference.AUTOACTIVATION_DELAY));
			
			assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
			assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
			//assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

			Display display= sourceViewer.getTextWidget().getDisplay();
			
			Color background= createColor(store, ContentAssistPreference.PROPOSALS_BACKGROUND, display);			
			assistant.setContextInformationPopupBackground(background);
			assistant.setContextSelectorBackground(background);
			assistant.setProposalSelectorBackground(background);

			Color foreground= createColor(store, ContentAssistPreference.PROPOSALS_FOREGROUND, display);
			assistant.setContextInformationPopupForeground(foreground);
			assistant.setContextSelectorForeground(foreground);
			assistant.setProposalSelectorForeground(foreground);
			
			return assistant;
		}	

		/**
		 * Creates a color from the information stored in the given preference store.
		 * Returns <code>null</code> if there is no such information available.
		 */
		private Color createColor(IPreferenceStore store, String key, Display display) {
		
			RGB rgb= null;		
			
			if (store.contains(key)) {
				
				if (store.isDefault(key))
					rgb= PreferenceConverter.getDefaultColor(store, key);
				else
					rgb= PreferenceConverter.getColor(store, key);
			
				if (rgb != null)
					return new Color(display, rgb);
			}
			
			return null;
		}	
	}

	private static class TextViewerAction extends Action implements IUpdate {
	
		private int fOperationCode= -1;
		private ITextOperationTarget fOperationTarget;
	
		public TextViewerAction(ITextViewer viewer, int operationCode) {
			fOperationCode= operationCode;
			fOperationTarget= viewer.getTextOperationTarget();
			update();
		}
	
		/**
		 * Updates the enabled state of the action.
		 * Fires a property change if the enabled state changes.
		 * 
		 * @see Action#firePropertyChange(String, Object, Object)
		 */
		public void update() {
	
			boolean wasEnabled= isEnabled();
			boolean isEnabled= (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
			setEnabled(isEnabled);
	
			if (wasEnabled != isEnabled) {
				firePropertyChange(ENABLED, wasEnabled ? Boolean.TRUE : Boolean.FALSE, isEnabled ? Boolean.TRUE : Boolean.FALSE);
			}
		}
		
		/**
		 * @see Action#run()
		 */
		public void run() {
			if (fOperationCode != -1 && fOperationTarget != null) {
				fOperationTarget.doOperation(fOperationCode);
			}
		}
	}	

	private final Template fTemplate;
	
	protected Text fNameText;
	private Text fDescriptionText;
	protected Combo fContextCombo;
	protected SourceViewer fPatternEditor;	
	private Button fInsertVariableButton;

	protected TemplateTranslator fTranslator= new TemplateTranslator();	
	protected boolean fSuppressError= true; // #4354	
	private Map fGlobalActions= new HashMap(10);
	private List fSelectionActions = new ArrayList(3);	
	private Vector fContextTypes= new Vector();
	
	protected final TemplateVariableProcessor fProcessor= new TemplateVariableProcessor();
		
	public EditTemplateDialog(Shell parent, Template template, boolean edit) {
		super(parent);
		
		setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
		
		String title= edit
			? TemplateMessages.getString("EditTemplateDialog.title.edit") //$NON-NLS-1$
			: TemplateMessages.getString("EditTemplateDialog.title.new"); //$NON-NLS-1$
		setTitle(title);

		fTemplate= template;
		
		ContextTypeRegistry registry= ContextTypeRegistry.getInstance();
		for (Iterator iterator= registry.iterator(); iterator.hasNext(); )
			fContextTypes.add(iterator.next());

		if (fContextTypes.size() > 0)
			fProcessor.setContextType(ContextTypeRegistry.getInstance().getContextType((String) fContextTypes.get(0)));
	}
	
	/*
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite ancestor) {
		Composite parent= new Composite(ancestor, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createLabel(parent, TemplateMessages.getString("EditTemplateDialog.name")); //$NON-NLS-1$	
		
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout= new GridLayout();		
		layout.numColumns= 3;
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		composite.setLayout(layout);

		fNameText= createText(composite);
		fNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (fSuppressError && (fNameText.getText().trim().length() != 0))
					fSuppressError= false;

				updateButtons();
			}
		});

		createLabel(composite, TemplateMessages.getString("EditTemplateDialog.context")); //$NON-NLS-1$		
		fContextCombo= new Combo(composite, SWT.READ_ONLY);

		for (Iterator iterator= fContextTypes.iterator(); iterator.hasNext(); )
			fContextCombo.add((String) iterator.next());

		fContextCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String name= fContextCombo.getText();
				fProcessor.setContextType(ContextTypeRegistry.getInstance().getContextType(name));				
			}
		});
		
		createLabel(parent, TemplateMessages.getString("EditTemplateDialog.description")); //$NON-NLS-1$		
		fDescriptionText= createText(parent);

		createLabel(parent, TemplateMessages.getString("EditTemplateDialog.pattern")); //$NON-NLS-1$
		fPatternEditor= createEditor(parent);
		
		fInsertVariableButton= new Button(parent, SWT.NONE);
		GridData data = getButtonGridData(fInsertVariableButton);
		
		fInsertVariableButton.setLayoutData(data);
		fInsertVariableButton.setText(TemplateMessages.getString("EditTemplateDialog.insert.variable")); //$NON-NLS-1$
		fInsertVariableButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				fPatternEditor.getTextWidget().setFocus();
				fPatternEditor.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);			
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		fNameText.setText(fTemplate.getName());
		fDescriptionText.setText(fTemplate.getDescription());
		fContextCombo.select(getIndex(fTemplate.getContextTypeName()));

		initializeActions();

		return composite;
	}

	private static GridData getButtonGridData(Button button) {
		GridData data= new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint= SWTUtil.getButtonHeigthHint(button);
		return data;
	}

	private static Label createLabel(Composite parent, String name) {
		Label label= new Label(parent, SWT.NULL);
		label.setText(name);
		label.setLayoutData(new GridData());

		return label;
	}

	private static Text createText(Composite parent) {
		Text text= new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		
		return text;
	}

	private SourceViewer createEditor(Composite parent) {
		SourceViewer viewer= new SourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		CTextTools tools= CUIPlugin.getDefault().getTextTools();
		viewer.configure(new SimpleCSourceViewerConfiguration(tools, null, fProcessor));
		viewer.setEditable(true);
		viewer.setDocument(new Document(fTemplate.getPattern()));
		
		Font font= JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT);
		viewer.getTextWidget().setFont(font);
		
		Control control= viewer.getControl();
		GridData data= new GridData(GridData.FILL_BOTH);
		data.widthHint= convertWidthInCharsToPixels(60);
		data.heightHint= convertHeightInCharsToPixels(5);
		control.setLayoutData(data);
		
		control.getAccessible().addAccessibleListener(new AccessibleAdapter() {			
			public void getName(AccessibleEvent e) {
				e.result = TemplateMessages.getString("EditTemplateDialog.pattern"); //$NON-NLS-1$
		}});
		
		viewer.addTextListener(new ITextListener() {
			public void textChanged(TextEvent event) {
			    try {
					fTranslator.translate(event.getDocumentEvent().getDocument().get());
			    } catch (CoreException e) {
			     	CUIPlugin.getDefault().log(e);
			     	// XXX dialog   
			    }
				
				updateUndoAction();
				updateButtons();
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {			
			public void selectionChanged(SelectionChangedEvent event) {
				updateSelectionDependentActions();
			}
		});

		if (viewer instanceof ITextViewerExtension) {
			 ((ITextViewerExtension) viewer).prependVerifyKeyListener(new VerifyKeyListener() {
				public void verifyKey(VerifyEvent event) {
					handleVerifyKeyPressed(event);
				}
			});
		} else {			
			viewer.getTextWidget().addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					handleKeyPressed(e);
				}
	
				public void keyReleased(KeyEvent e) {}
			});
		}
		
		return viewer;
	}

	protected void handleKeyPressed(KeyEvent event) {
		if (event.stateMask != SWT.CTRL)
			return;
			
		switch (event.character) {
			case ' ':
				fPatternEditor.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
				break;

			// CTRL-Z
			case (int) 'z' - (int) 'a' + 1:
				fPatternEditor.doOperation(ITextOperationTarget.UNDO);
				break;				
		}
	}

	protected void handleVerifyKeyPressed(VerifyEvent event) {
		if (!event.doit)
			return;

		if (event.stateMask != SWT.CTRL)
			return;
			
		switch (event.character) {
			case ' ':
				fPatternEditor.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
				event.doit= false;
				break;

			// CTRL-Z
			case (int) 'z' - (int) 'a' + 1:
				fPatternEditor.doOperation(ITextOperationTarget.UNDO);
				event.doit= false;
				break;				
		}
	}

	private void initializeActions() {
		TextViewerAction action= new TextViewerAction(fPatternEditor, ITextOperationTarget.UNDO);
		action.setText(TemplateMessages.getString("EditTemplateDialog.undo")); //$NON-NLS-1$
		fGlobalActions.put(ITextEditorActionConstants.UNDO, action);

		action= new TextViewerAction(fPatternEditor, ITextOperationTarget.CUT);
		action.setText(TemplateMessages.getString("EditTemplateDialog.cut")); //$NON-NLS-1$
		fGlobalActions.put(ITextEditorActionConstants.CUT, action);

		action= new TextViewerAction(fPatternEditor, ITextOperationTarget.COPY);
		action.setText(TemplateMessages.getString("EditTemplateDialog.copy")); //$NON-NLS-1$
		fGlobalActions.put(ITextEditorActionConstants.COPY, action);

		action= new TextViewerAction(fPatternEditor, ITextOperationTarget.PASTE);
		action.setText(TemplateMessages.getString("EditTemplateDialog.paste")); //$NON-NLS-1$
		fGlobalActions.put(ITextEditorActionConstants.PASTE, action);

		action= new TextViewerAction(fPatternEditor, ITextOperationTarget.SELECT_ALL);
		action.setText(TemplateMessages.getString("EditTemplateDialog.select.all")); //$NON-NLS-1$
		fGlobalActions.put(ITextEditorActionConstants.SELECT_ALL, action);

		action= new TextViewerAction(fPatternEditor, ISourceViewer.CONTENTASSIST_PROPOSALS);
		action.setText(TemplateMessages.getString("EditTemplateDialog.content.assist")); //$NON-NLS-1$
		fGlobalActions.put("ContentAssistProposal", action); //$NON-NLS-1$

		fSelectionActions.add(ITextEditorActionConstants.CUT);
		fSelectionActions.add(ITextEditorActionConstants.COPY);
		fSelectionActions.add(ITextEditorActionConstants.PASTE);
		
		// create context menu
		MenuManager manager= new MenuManager(null, null);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});

		StyledText text= fPatternEditor.getTextWidget();		
		Menu menu= manager.createContextMenu(text);
		text.setMenu(menu);
	}

	protected void fillContextMenu(IMenuManager menu) {
		menu.add(new GroupMarker(ITextEditorActionConstants.GROUP_UNDO));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_UNDO, (IAction) fGlobalActions.get(ITextEditorActionConstants.UNDO));
		
		menu.add(new Separator(ITextEditorActionConstants.GROUP_EDIT));		
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, (IAction) fGlobalActions.get(ITextEditorActionConstants.CUT));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, (IAction) fGlobalActions.get(ITextEditorActionConstants.COPY));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, (IAction) fGlobalActions.get(ITextEditorActionConstants.PASTE));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, (IAction) fGlobalActions.get(ITextEditorActionConstants.SELECT_ALL));

		menu.add(new Separator(IContextMenuConstants.GROUP_GENERATE));
		menu.appendToGroup(IContextMenuConstants.GROUP_GENERATE, (IAction) fGlobalActions.get("ContentAssistProposal")); //$NON-NLS-1$
	}

	protected void updateSelectionDependentActions() {
		Iterator iterator= fSelectionActions.iterator();
		while (iterator.hasNext())
			updateAction((String)iterator.next());		
	}

	protected void updateUndoAction() {
		IAction action= (IAction) fGlobalActions.get(ITextEditorActionConstants.UNDO);
		if (action instanceof IUpdate)
			((IUpdate) action).update();
	}

	protected void updateAction(String actionId) {
		IAction action= (IAction) fGlobalActions.get(actionId);
		if (action instanceof IUpdate)
			((IUpdate) action).update();
	}

	private int getIndex(String context) {
		ContextTypeRegistry registry= ContextTypeRegistry.getInstance();
		registry.getContextType(context);
		
		if (context == null)
			return -1;
			
		return fContextTypes.indexOf(context);
	}
	
	protected void okPressed() {
		fTemplate.setName(fNameText.getText());
		fTemplate.setDescription(fDescriptionText.getText());
		fTemplate.setContext(fContextCombo.getText());
		fTemplate.setPattern(fPatternEditor.getTextWidget().getText());
		
		super.okPressed();
	}
	
	protected void updateButtons() {		
		boolean valid= fNameText.getText().trim().length() != 0;

		StatusInfo status= new StatusInfo();
		
		if (!valid) {
			if (fSuppressError)
				status.setError(""); //$NON-NLS-1$							
			else
				status.setError(TemplateMessages.getString("EditTemplateDialog.error.noname")); //$NON-NLS-1$
 		} else if (fTranslator.getErrorMessage() != null) {
 			status.setError(fTranslator.getErrorMessage());	
		}

		updateStatus(status);
	}

}

