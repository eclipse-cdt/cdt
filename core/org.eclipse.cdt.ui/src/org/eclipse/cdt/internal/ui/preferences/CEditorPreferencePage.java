package org.eclipse.cdt.internal.ui.preferences;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.WorkbenchChainedTextFontFieldEditor;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.text.ContentAssistPreference;
import org.eclipse.cdt.internal.ui.text.ICColorConstants;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.core.runtime.IStatus;


/*
 * The page for setting the editor options.
 */
public class CEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
			
	public final OverlayPreferenceStore.OverlayKey[] fKeys= new OverlayPreferenceStore.OverlayKey[] {
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CEditor.PREFERENCE_COLOR_FOREGROUND),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT),

		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CEditor.PREFERENCE_COLOR_BACKGROUND),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT),

		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, CSourceViewerConfiguration.PREFERENCE_TAB_WIDTH),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ICColorConstants.C_MULTI_LINE_COMMENT),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ICColorConstants.C_MULTI_LINE_COMMENT + "_bold"),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ICColorConstants.C_SINGLE_LINE_COMMENT),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ICColorConstants.C_SINGLE_LINE_COMMENT + "_bold"),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ICColorConstants.C_KEYWORD),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ICColorConstants.C_KEYWORD + "_bold"),

		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ICColorConstants.C_TYPE),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ICColorConstants.C_TYPE + "_bold"),
				
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ICColorConstants.C_STRING),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ICColorConstants.C_STRING + "_bold"),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ICColorConstants.C_DEFAULT),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ICColorConstants.C_DEFAULT + "_bold"),

		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CEditor.MATCHING_BRACKETS_COLOR),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.MATCHING_BRACKETS),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.MATCHING_BRACKETS_NOBOX),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CEditor.CURRENT_LINE_COLOR),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.CURRENT_LINE),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CEditor.PROBLEM_INDICATION_COLOR),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.PROBLEM_INDICATION),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.SPACES_FOR_TABS),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CEditor.PRINT_MARGIN_COLOR),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, CEditor.PRINT_MARGIN_COLUMN),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.PRINT_MARGIN),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CEditor.LINKED_POSITION_COLOR),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CEditor.LINE_NUMBER_COLOR),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.LINE_NUMBER_RULER),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.OVERVIEW_RULER),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.AUTOACTIVATION),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, ContentAssistPreference.AUTOACTIVATION_DELAY),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.AUTOINSERT),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.PROPOSALS_BACKGROUND),
		//new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.PROPOSALS_FOREGROUND),
		//new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.PARAMETERS_BACKGROUND),
		//new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.PARAMETERS_FOREGROUND),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_C),
		//new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_JAVADOC),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.SHOW_DOCUMENTED_PROPOSALS),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.ORDER_PROPOSALS),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.CASE_SENSITIVITY),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.ADD_INCLUDE)
		
	};
	
	protected final String[][] fListModel= new String[][] {
		{ "Multi-line comment", ICColorConstants.C_MULTI_LINE_COMMENT },
		{ "Single-line comment", ICColorConstants.C_SINGLE_LINE_COMMENT },
		{ "Keywords", ICColorConstants.C_KEYWORD },
		{ "Built-in types", ICColorConstants.C_TYPE },
		{ "Strings", ICColorConstants.C_STRING },
		{ "Others", ICColorConstants.C_DEFAULT }
	};
	
	protected final String[][] fAppearanceColorListModel= new String[][] {
		{"Line number color", CEditor.LINE_NUMBER_COLOR}, //$NON-NLS-1$
		{"Matching bracket color", CEditor.MATCHING_BRACKETS_COLOR}, //$NON-NLS-1$
		{"Current line highlight color", CEditor.CURRENT_LINE_COLOR}, //$NON-NLS-1$
		{"Problem indicator color", CEditor.PROBLEM_INDICATION_COLOR}, //$NON-NLS-1$
		{"Print margin color", CEditor.PRINT_MARGIN_COLOR}, //$NON-NLS-1$
		{"Linked position color", CEditor.LINKED_POSITION_COLOR}, //$NON-NLS-1$
	};
	
	protected OverlayPreferenceStore fOverlayStore;
	private CTextTools fCTextTools;
	
	protected Map fColorButtons= new HashMap();
	private SelectionListener fColorButtonListener= new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		public void widgetSelected(SelectionEvent e) {
			ColorEditor editor= (ColorEditor) e.widget.getData();
			PreferenceConverter.setValue(fOverlayStore, (String) fColorButtons.get(editor), editor.getColorValue());
		}
	};
	
	protected Map fCheckBoxes= new HashMap();
	private SelectionListener fCheckBoxListener= new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		public void widgetSelected(SelectionEvent e) {
			Button button= (Button) e.widget;
			fOverlayStore.setValue((String) fCheckBoxes.get(button), button.getSelection());
		}
	};
	
	protected Map fTextFields= new HashMap();
	private ModifyListener fTextFieldListener= new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			Text text= (Text) e.widget;
			fOverlayStore.setValue((String) fTextFields.get(text), text.getText());
		}
	};
	
	private WorkbenchChainedTextFontFieldEditor fFontEditor;
	protected List fList;
	protected ColorEditor fForegroundColorEditor;
	protected ColorEditor fBackgroundColorEditor;
	private Button fBackgroundDefaultRadioButton;
	protected Button fBackgroundCustomRadioButton;
	protected Button fBackgroundColorButton;
	protected Button fBoldCheckBox;
	protected SourceViewer fPreviewViewer;
	
	protected List fAppearanceColorList;
	private ColorEditor fSyntaxForegroundColorEditor;
	protected ColorEditor fAppearanceForegroundColorEditor;
	
	public CEditorPreferencePage() {
		setDescription(CUIPlugin.getResourceString("CEditorPreferencePage.description"));
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		fOverlayStore= new OverlayPreferenceStore(getPreferenceStore(), fKeys);
	}
	
	public static void initDefaults(IPreferenceStore store) {
		
		Color color;
		Display display= Display.getDefault();
		
		store.setDefault(CEditor.MATCHING_BRACKETS, true);
		store.setDefault(CEditor.MATCHING_BRACKETS_NOBOX, false);
		color= display.getSystemColor(SWT.COLOR_GRAY);
		PreferenceConverter.setDefault(store, CEditor.MATCHING_BRACKETS_COLOR,  color.getRGB());
		
		store.setDefault(CEditor.CURRENT_LINE, true);
		PreferenceConverter.setDefault(store, CEditor.CURRENT_LINE_COLOR, new RGB(225, 235, 224));
		
		store.setDefault(CEditor.PRINT_MARGIN, true);
		store.setDefault(CEditor.PRINT_MARGIN_COLUMN, 80);
		PreferenceConverter.setDefault(store, CEditor.PRINT_MARGIN_COLOR, new RGB(176, 180 , 185));

		//PreferenceConverter.setDefault(store, CEditor.PREFERENCE_COLOR_FIND_SCOPE, new RGB(185, 176 , 180));
		
		store.setDefault(CEditor.PROBLEM_INDICATION, true);
		PreferenceConverter.setDefault(store, CEditor.PROBLEM_INDICATION_COLOR, new RGB(255, 0 , 128));
		
		//store.setDefault(CompilationUnitEditor.OVERVIEW_RULER, false);
		
		WorkbenchChainedTextFontFieldEditor.startPropagate(store, JFaceResources.TEXT_FONT);
		
		color= display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		PreferenceConverter.setDefault(store,  CEditor.PREFERENCE_COLOR_FOREGROUND, color.getRGB());
		store.setDefault(CEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT, true);
		
		color= display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		PreferenceConverter.setDefault(store,  CEditor.PREFERENCE_COLOR_BACKGROUND, color.getRGB());		
		store.setDefault(CEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, false);
		
		store.setDefault(CSourceViewerConfiguration.PREFERENCE_TAB_WIDTH, 4);
		
		store.setDefault(CEditor.SPACES_FOR_TABS, false);
		
		PreferenceConverter.setDefault(store, ICColorConstants.C_MULTI_LINE_COMMENT, new RGB(63, 127, 95));
		store.setDefault(ICColorConstants.C_MULTI_LINE_COMMENT + "_bold", false);
		
		PreferenceConverter.setDefault(store, ICColorConstants.C_SINGLE_LINE_COMMENT, new RGB(63, 125, 95));
		store.setDefault(ICColorConstants.C_SINGLE_LINE_COMMENT + "_bold", false);
		
		PreferenceConverter.setDefault(store, ICColorConstants.C_KEYWORD, new RGB(127, 0, 85));
		store.setDefault(ICColorConstants.C_KEYWORD + "_bold", true);
		
		PreferenceConverter.setDefault(store, ICColorConstants.C_TYPE, new RGB(127, 0, 85));
		store.setDefault(ICColorConstants.C_TYPE + "_bold", true);
				
		PreferenceConverter.setDefault(store, ICColorConstants.C_STRING, new RGB(42, 0, 255));
		store.setDefault(ICColorConstants.C_STRING + "_bold", false);
		
		PreferenceConverter.setDefault(store, ICColorConstants.C_DEFAULT, new RGB(0, 0, 0));
		store.setDefault(ICColorConstants.C_DEFAULT + "_bold", false);	
		
		
		PreferenceConverter.setDefault(store, CEditor.LINKED_POSITION_COLOR, new RGB(0, 200 , 100));	
		
		store.setDefault(CEditor.LINE_NUMBER_RULER, false);
		PreferenceConverter.setDefault(store, CEditor.LINE_NUMBER_COLOR, new RGB(0, 0, 0));
		
		store.setDefault(CEditor.OVERVIEW_RULER, true);
		
		store.setDefault(ContentAssistPreference.AUTOACTIVATION, false);
		store.setDefault(ContentAssistPreference.AUTOACTIVATION_DELAY, 500);
		
		store.setDefault(ContentAssistPreference.AUTOINSERT, true);
		PreferenceConverter.setDefault(store, ContentAssistPreference.PROPOSALS_BACKGROUND, new RGB(254, 241, 233));
		//PreferenceConverter.setDefault(store, ContentAssistPreference.PROPOSALS_FOREGROUND, new RGB(0, 0, 0));
		//PreferenceConverter.setDefault(store, ContentAssistPreference.PARAMETERS_BACKGROUND, new RGB(254, 241, 233));
		//PreferenceConverter.setDefault(store, ContentAssistPreference.PARAMETERS_FOREGROUND, new RGB(0, 0, 0));
		//store.setDefault(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_C, ".,");
		//store.setDefault(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_JAVADOC, "@");
		//store.setDefault(ContentAssistPreference.SHOW_VISIBLE_PROPOSALS, true);
		store.setDefault(ContentAssistPreference.CASE_SENSITIVITY, false);
		store.setDefault(ContentAssistPreference.ORDER_PROPOSALS, false);
		store.setDefault(ContentAssistPreference.ADD_INCLUDE, true);				

	}

	/*
	 * @see IWorkbenchPreferencePage#init()
	 */	
	public void init(IWorkbench workbench) {
	}

	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		//WorkbenchHelp.setHelp(getControl(), ICHelpContextIds.JAVA_EDITOR_PREFERENCE_PAGE);
	}

	protected void handleListSelection() {	
		int i= fList.getSelectionIndex();
		String key= fListModel[i][1];
		RGB rgb= PreferenceConverter.getColor(fOverlayStore, key);
		fForegroundColorEditor.setColorValue(rgb);		
		fBoldCheckBox.setSelection(fOverlayStore.getBoolean(key + "_bold"));
	}
	
	private Control createColorPage(Composite parent) {
		
		Composite colorComposite= new Composite(parent, SWT.NULL);
		colorComposite.setLayout(new GridLayout());

		Composite backgroundComposite= new Composite(colorComposite, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		backgroundComposite.setLayout(layout);

		Label label= new Label(backgroundComposite, SWT.NULL);
		label.setText("Bac&kground Color:");
		GridData gd= new GridData();
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);

		SelectionListener backgroundSelectionListener= new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {				
				boolean custom= fBackgroundCustomRadioButton.getSelection();
				fBackgroundColorButton.setEnabled(custom);
				fOverlayStore.setValue(CEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, !custom);
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		};

		fBackgroundDefaultRadioButton= new Button(backgroundComposite, SWT.RADIO | SWT.LEFT);
		fBackgroundDefaultRadioButton.setText("S&ystem Default");
		gd= new GridData();
		gd.horizontalSpan= 2;
		fBackgroundDefaultRadioButton.setLayoutData(gd);
		fBackgroundDefaultRadioButton.addSelectionListener(backgroundSelectionListener);

		fBackgroundCustomRadioButton= new Button(backgroundComposite, SWT.RADIO | SWT.LEFT);
		fBackgroundCustomRadioButton.setText("C&ustom");
		fBackgroundCustomRadioButton.addSelectionListener(backgroundSelectionListener);

		fBackgroundColorEditor= new ColorEditor(backgroundComposite);
		fBackgroundColorButton= fBackgroundColorEditor.getButton();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		fBackgroundColorButton.setLayoutData(gd);

		label= new Label(colorComposite, SWT.LEFT);
		label.setText("Fo&reground:");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite editorComposite= new Composite(colorComposite, SWT.NULL);
		layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		gd= new GridData(GridData.FILL_BOTH);
		editorComposite.setLayoutData(gd);		

		fList= new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL);
		gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= convertHeightInCharsToPixels(5);
		fList.setLayoutData(gd);
						
		Composite stylesComposite= new Composite(editorComposite, SWT.NULL);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		label= new Label(stylesComposite, SWT.LEFT);
		label.setText("C&olor:");
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label.setLayoutData(gd);

		fForegroundColorEditor= new ColorEditor(stylesComposite);
		Button foregroundColorButton= fForegroundColorEditor.getButton();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);
		
		label= new Label(stylesComposite, SWT.LEFT);
		label.setText("&Bold:");
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label.setLayoutData(gd);
		
		fBoldCheckBox= new Button(stylesComposite, SWT.CHECK);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		fBoldCheckBox.setLayoutData(gd);
		
		label= new Label(colorComposite, SWT.LEFT);
		label.setText("Preview:");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Control previewer= createPreviewer(colorComposite);
		gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= convertWidthInCharsToPixels(80);
		gd.heightHint= convertHeightInCharsToPixels(15);
		previewer.setLayoutData(gd);

		
		fList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				handleListSelection();
			}
		});
		
		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				int i= fList.getSelectionIndex();
				String key= fListModel[i][1];
				
				PreferenceConverter.setValue(fOverlayStore, key, fForegroundColorEditor.getColorValue());
			}
		});

		fBackgroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				PreferenceConverter.setValue(fOverlayStore, CEditor.PREFERENCE_COLOR_BACKGROUND, fBackgroundColorEditor.getColorValue());					
			}
		});

		fBoldCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				int i= fList.getSelectionIndex();
				String key= fListModel[i][1];
				fOverlayStore.setValue(key + "_bold", fBoldCheckBox.getSelection());
			}
		});
				
		return colorComposite;
	}
	
	private Control createPreviewer(Composite parent) {
		
		fCTextTools= new CTextTools(fOverlayStore);
		
		fPreviewViewer= new SourceViewer(parent, null, SWT.V_SCROLL | SWT.H_SCROLL);
		fPreviewViewer.configure(new CSourceViewerConfiguration(fCTextTools, null));
		fPreviewViewer.getTextWidget().setFont(JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT));
		fPreviewViewer.setEditable(false);
		
		initializeViewerColors(fPreviewViewer);
		
		String content= loadPreviewContentFromFile("ColorSettingPreviewCode.txt");
		IDocument document= new Document(content);
		IDocumentPartitioner partitioner= fCTextTools.createDocumentPartitioner();
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);
		
		fPreviewViewer.setDocument(document);
		
		fOverlayStore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String p= event.getProperty();
				if (p.equals(CEditor.PREFERENCE_COLOR_BACKGROUND) ||
					p.equals(CEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT))
				{
					initializeViewerColors(fPreviewViewer);
				}

				fPreviewViewer.getDocument().set(fPreviewViewer.getDocument().get());
				//fPreviewViewer.refresh();
			}
		});
		
		return fPreviewViewer.getControl();
	}
	
	private Color fBackgroundColor;
	
	/**
	 * Initializes the given viewer's colors.
	 * 
	 * @param viewer the viewer to be initialized
	 */
	protected void initializeViewerColors(ISourceViewer viewer) {
		
		IPreferenceStore store= fOverlayStore;
		if (store != null) {
			
			StyledText styledText= viewer.getTextWidget();
						
			// ---------- background color ----------------------
			Color color= store.getBoolean(CEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
				? null
				: createColor(store, CEditor.PREFERENCE_COLOR_BACKGROUND, styledText.getDisplay());
			styledText.setBackground(color);
				
			if (fBackgroundColor != null)
				fBackgroundColor.dispose();
				
			fBackgroundColor= color;
		}
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
	
	// sets enabled flag for a control and all its sub-tree
	protected static void setEnabled(Control control, boolean enable) {
		control.setEnabled(enable);
		if (control instanceof Composite) {
			Composite composite= (Composite) control;
			Control[] children= composite.getChildren();
			for (int i= 0; i < children.length; i++)
				setEnabled(children[i], enable);
		}
	}
	
	private ArrayList fNumberFields= new ArrayList();
	private ModifyListener fNumberFieldListener= new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			numberFieldChanged((Text) e.widget);
		}
	};
	
	private Button fBracketHighlightButton;
	private Button fBracketHighlightBoxButton;
	private Control fBracketHighlightColor;
	private Button fLineHighlightButton;
	private Control fLineHighlightColor;
	protected Button fPrintMarginButton;
	protected Control fPrintMarginColor;
	protected Control fPrintMarginColumn;
	private Button fProblemIndicationButton;
	private Control fProblemIndicationColor;
	private Control fFindScopeColor;
	private Control fLinkedPositionColor;
	
	protected void handleAppearanceColorListSelection() {	
		int i= fAppearanceColorList.getSelectionIndex();
		String key= fAppearanceColorListModel[i][1];
		RGB rgb= PreferenceConverter.getColor(fOverlayStore, key);
		fAppearanceForegroundColorEditor.setColorValue(rgb);		
	}
	private Control createBehaviorPage(Composite parent) {

		Composite behaviorComposite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout(); layout.numColumns= 2;
		behaviorComposite.setLayout(layout);
		
		
		String label= "Text &font:";
		addTextFontEditor(behaviorComposite, label, CEditor.PREFERENCE_FONT);
		
		label= "Displayed &tab width:";
		addTextField(behaviorComposite, label, CSourceViewerConfiguration.PREFERENCE_TAB_WIDTH, 2, 0, true);
		
		label= "Print margin col&umn:";
		fPrintMarginColumn= addTextField(behaviorComposite, label, CEditor.PRINT_MARGIN_COLUMN, 4, 0, true);
		
		label= "Insert &space for tabs";
		addCheckBox(behaviorComposite, label, CEditor.SPACES_FOR_TABS, 0);
		
		//label= "Show overview &ruler";
		//addCheckBox(behaviorComposite, label, CompilationUnitEditor.OVERVIEW_RULER, 0);
		
		label= "Highlight &matching brackets";
		fBracketHighlightButton= addCheckBox(behaviorComposite, label, CEditor.MATCHING_BRACKETS, 0);

		label= "Only c&olor bracket text";
		fBracketHighlightBoxButton= addCheckBox(behaviorComposite, label, CEditor.MATCHING_BRACKETS_NOBOX, 0);
		
		label= "Show line numbers"; //$NON-NLS-1$
		addCheckBox(behaviorComposite, label, CEditor.LINE_NUMBER_RULER, 0);
		
		label= "Highlight &current line";
		fLineHighlightButton= addCheckBox(behaviorComposite, label, CEditor.CURRENT_LINE, 0);
		
		label= "Highlight &problems";
		fProblemIndicationButton= addCheckBox(behaviorComposite, label, CEditor.PROBLEM_INDICATION, 0);
		
		label= "Show overview ruler"; //$NON-NLS-1$
		addCheckBox(behaviorComposite, label, CEditor.OVERVIEW_RULER, 0);
		
		label= "Show print &margin";
		fPrintMarginButton= addCheckBox(behaviorComposite, label, CEditor.PRINT_MARGIN, 0);
		
		fPrintMarginButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean enabled= fPrintMarginButton.getSelection();
				setEnabled(fPrintMarginColor, enabled);
				setEnabled(fPrintMarginColumn, enabled);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Label l= new Label(behaviorComposite, SWT.LEFT );
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		gd.heightHint= convertHeightInCharsToPixels(1) / 2;
		l.setLayoutData(gd);
		
		l= new Label(behaviorComposite, SWT.LEFT);
		l.setText("Appearance color options"); //$NON-NLS-1$
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		l.setLayoutData(gd);

		Composite editorComposite= new Composite(behaviorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);	
		
		fAppearanceColorList= new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= convertHeightInCharsToPixels(5);
		fAppearanceColorList.setLayoutData(gd);
		
		Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		l= new Label(stylesComposite, SWT.LEFT);
		l.setText("Color:"); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		l.setLayoutData(gd);

		fAppearanceForegroundColorEditor= new ColorEditor(stylesComposite);
		Button foregroundColorButton= fAppearanceForegroundColorEditor.getButton();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		fAppearanceColorList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				handleAppearanceColorListSelection();
			}
		});
		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				int i= fAppearanceColorList.getSelectionIndex();
				String key= fAppearanceColorListModel[i][1];
				
				PreferenceConverter.setValue(fOverlayStore, key, fAppearanceForegroundColorEditor.getColorValue());
			}
		});
		
		return behaviorComposite;
	}
	
	private Control createContentAssistPage(Composite parent) {

		Composite contentAssistComposite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout(); layout.numColumns= 2;
		contentAssistComposite.setLayout(layout);
				
		String label= "Insert single &proposals automatically";
		addCheckBox(contentAssistComposite, label, ContentAssistPreference.AUTOINSERT, 0);
		
		//label= "Show only proposals visible in the invocation conte&xt";
		//addCheckBox(contentAssistComposite, label, ContentAssistPreference.SHOW_VISIBLE_PROPOSALS, 0);
		
		//label= "Show only proposals with &matching cases";
		//addCheckBox(contentAssistComposite, label, ContentAssistPreference.CASE_SENSITIVITY, 0);
		
		//label= "Present proposals in a&lphabetical order";
		//addCheckBox(contentAssistComposite, label, ContentAssistPreference.ORDER_PROPOSALS, 0);
		
		label= "&Enable auto activation";
		addCheckBox(contentAssistComposite, label, ContentAssistPreference.AUTOACTIVATION, 0);

		//label= "Automatically add &include for proposals from system functions";
		//addCheckBox(contentAssistComposite, label, ContentAssistPreference.ADD_INCLUDE, 0);
		
		label= "Auto activation dela&y:";
		addTextField(contentAssistComposite, label, ContentAssistPreference.AUTOACTIVATION_DELAY, 4, 0, true);
		
		label= "Auto activation &triggers for C:";
		addTextField(contentAssistComposite, label, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_C, 25, 0, true);
		
		//label= "Auto activation triggers for &JavaDoc:";
		//addTextField(contentAssistComposite, label, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_JAVADOC, 25, 0);
				
		label= "&Background for completion proposals:";
		addColorButton(contentAssistComposite, label, ContentAssistPreference.PROPOSALS_BACKGROUND, 0);
		
		//label= "&Foreground for completion proposals:";
		//addColorButton(contentAssistComposite, label, ContentAssistPreference.PROPOSALS_FOREGROUND, 0);
		
		//label= "Bac&kground for method parameters:";
		//addColorButton(contentAssistComposite, label, ContentAssistPreference.PARAMETERS_BACKGROUND, 0);
		
		//label= "Fo&reground for method parameters:";
		//addColorButton(contentAssistComposite, label, ContentAssistPreference.PARAMETERS_FOREGROUND, 0);
				
		return contentAssistComposite;
	}

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		
		fOverlayStore.load();
		fOverlayStore.start();
		
		TabFolder folder= new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());	
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TabItem item= new TabItem(folder, SWT.NONE);
		item.setText("&General");
		item.setImage(CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT));
		item.setControl(createBehaviorPage(folder));
		
		item= new TabItem(folder, SWT.NONE);
		item.setText("&Colors");
		item.setImage(CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT));
		item.setControl(createColorPage(folder));
		
		item= new TabItem(folder, SWT.NONE);
		item.setText("Code A&ssist");
		item.setImage(CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT));
		item.setControl(createContentAssistPage(folder));
		
		initialize();
		
		return folder;
	}
	
	private void initialize() {
		
		fFontEditor.setPreferenceStore(getPreferenceStore());
		fFontEditor.setPreferencePage(this);
		fFontEditor.load();
		
		initializeFields();
		
		for (int i= 0; i < fListModel.length; i++)
			fList.add(fListModel[i][0]);
					
		for (int i= 0; i < fAppearanceColorListModel.length; i++)
			fAppearanceColorList.add(fAppearanceColorListModel[i][0]);
			
		fList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				fList.select(0);
				handleListSelection();
			}
		});
		
		fAppearanceColorList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				fAppearanceColorList.select(0);
				handleAppearanceColorListSelection();
			}
		});
	}
	
	private void initializeFields() {
		
		Iterator e= fColorButtons.keySet().iterator();
		while (e.hasNext()) {
			ColorEditor c= (ColorEditor) e.next();
			String key= (String) fColorButtons.get(c);
			RGB rgb= PreferenceConverter.getColor(fOverlayStore, key);
			c.setColorValue(rgb);
		}
		
		e= fCheckBoxes.keySet().iterator();
		while (e.hasNext()) {
			Button b= (Button) e.next();
			String key= (String) fCheckBoxes.get(b);
			b.setSelection(fOverlayStore.getBoolean(key));
		}
		
		e= fTextFields.keySet().iterator();
		while (e.hasNext()) {
			Text t= (Text) e.next();
			String key= (String) fTextFields.get(t);
			t.setText(fOverlayStore.getString(key));
		}
		
		RGB rgb= PreferenceConverter.getColor(fOverlayStore, CEditor.PREFERENCE_COLOR_BACKGROUND);
		fBackgroundColorEditor.setColorValue(rgb);		
		
		boolean default_= fOverlayStore.getBoolean(CEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);
		fBackgroundDefaultRadioButton.setSelection(default_);
		fBackgroundCustomRadioButton.setSelection(!default_);
		fBackgroundColorButton.setEnabled(!default_);
		
		//updateAutoactivationControls();
	}
	
	/*
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk() {
		fFontEditor.store();
		fOverlayStore.propagate();
		return true;
	}
	
	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		
		fFontEditor.loadDefault();
		
		fOverlayStore.loadDefaults();
		initializeFields();
		handleListSelection();
		handleAppearanceColorListSelection();
		
		super.performDefaults();
		
		fPreviewViewer.invalidateTextPresentation();
	}
	
	/*
	 * @see DialogPage#dispose()
	 */
	public void dispose() {
		
		if (fCTextTools != null) {
			fCTextTools= null;
		}
		
		fFontEditor.setPreferencePage(null);
		fFontEditor.setPreferenceStore(null);
		
		if (fOverlayStore != null) {
			fOverlayStore.stop();
			fOverlayStore= null;
		}
		
		super.dispose();
	}
	
	private Control addColorButton(Composite parent, String label, String key, int indentation) {

		Composite composite= new Composite(parent, SWT.NONE);
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan= 2;
		composite.setLayoutData(gd);
		
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		composite.setLayout(layout);
				
		Label labelControl= new Label(composite, SWT.NONE);
		labelControl.setText(label);
		
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent= indentation;
		labelControl.setLayoutData(gd);
		
		ColorEditor editor= new ColorEditor(composite);
		Button button= editor.getButton();
		button.setData(editor);
		
		gd= new GridData();
		gd.horizontalAlignment= GridData.END;
		button.setLayoutData(gd);
		button.addSelectionListener(fColorButtonListener);
		
		fColorButtons.put(editor, key);
		
		return composite;
	}
	
	private Button addCheckBox(Composite parent, String label, String key, int indentation) {		
		Button checkBox= new Button(parent, SWT.CHECK);
		checkBox.setText(label);
		
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent= indentation;
		gd.horizontalSpan= 2;
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(fCheckBoxListener);
		
		fCheckBoxes.put(checkBox, key);
		
		return checkBox;
	}
	
	private Control addTextField(Composite composite, String label, String key, int textLimit, int indentation, boolean isNumber) {
		
		Label labelControl= new Label(composite, SWT.NONE);
		labelControl.setText(label);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		labelControl.setLayoutData(gd);
		
		Text textControl= new Text(composite, SWT.BORDER | SWT.SINGLE);		
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint= convertWidthInCharsToPixels(textLimit + 1);
		textControl.setLayoutData(gd);
		textControl.setTextLimit(textLimit);
		fTextFields.put(textControl, key);
		if (isNumber) {
			fNumberFields.add(textControl);
			textControl.addModifyListener(fNumberFieldListener);
		} else {
			textControl.addModifyListener(fTextFieldListener);
		}
			
		return textControl;
	}
	
	private void addTextFontEditor(Composite parent, String label, String key) {
		
		Composite editorComposite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		editorComposite.setLayout(layout);		
		fFontEditor= new WorkbenchChainedTextFontFieldEditor(key, label, editorComposite);
		fFontEditor.setChangeButtonText("C&hange...");
				
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);
	}
	
	private String loadPreviewContentFromFile(String filename) {
		String line;
		String separator= System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer buffer= new StringBuffer(512);
		BufferedReader reader= null;
		try {
			reader= new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
			while ((line= reader.readLine()) != null) {
				buffer.append(line);
				buffer.append(separator);
			}
		} catch (IOException io) {
			CUIPlugin.log(io);
		} finally {
			if (reader != null) {
				try { reader.close(); } catch (IOException e) {}
			}
		}
		return buffer.toString();
	}
	
	protected void numberFieldChanged(Text textControl) {
		String number= textControl.getText();
		IStatus status= validatePositiveNumber(number);
		if (!status.matches(IStatus.ERROR))
			fOverlayStore.setValue((String) fTextFields.get(textControl), number);
		updateStatus(status);
	}
	
	private IStatus validatePositiveNumber(String number) {
		StatusInfo status= new StatusInfo();
		if (number.length() == 0) {
			//status.setError("CEditorPreferencePage.empty_input"); //$NON-NLS-1$
		} else {
			try {
				int value= Integer.parseInt(number);
				if (value < 0)
					status.setError("CEditorPreferencePage.invalid_input"); //$NON-NLS-1$
			} catch (NumberFormatException e) {
				status.setError("CEditorPreferencePage.invalid_input"); //$NON-NLS-1$
			}
		}
		return status;
	}
	
	private void updateStatus(IStatus status) {
		if (!status.matches(IStatus.ERROR)) {
			for (int i= 0; i < fNumberFields.size(); i++) {
				Text text= (Text) fNumberFields.get(i);
				IStatus s= validatePositiveNumber(text.getText());
				status= StatusUtil.getMoreSevere(s, status);
			}
		}	
		setValid(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}
}


