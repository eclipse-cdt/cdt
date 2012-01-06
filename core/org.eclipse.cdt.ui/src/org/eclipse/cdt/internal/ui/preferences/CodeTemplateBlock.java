/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateReaderWriter;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.corext.template.c.CodeTemplateContextType;
import org.eclipse.cdt.internal.corext.template.c.FileTemplateContextType;
import org.eclipse.cdt.internal.corext.util.Messages;

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.editor.CSourceViewer;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.text.template.TemplateVariableProcessor;
import org.eclipse.cdt.internal.ui.viewsupport.ProjectTemplateStore;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.TreeListDialogField;

/**
 */
public class CodeTemplateBlock extends OptionsConfigurationBlock {
	
	private class CodeTemplateAdapter extends ViewerComparator
			implements ITreeListAdapter<Object>, IDialogFieldListener {
		private final Object[] NO_CHILDREN= new Object[0];

		@Override
		public void customButtonPressed(TreeListDialogField<Object> field, int index) {
			doButtonPressed(index, field.getSelectedElements());
		}
		
		@Override
		public void selectionChanged(TreeListDialogField<Object> field) {
			List<Object> selected= field.getSelectedElements();
			field.enableButton(IDX_ADD, canAdd(selected));
			field.enableButton(IDX_EDIT, canEdit(selected));
			field.enableButton(IDX_REMOVE, canRemove(selected));
			field.enableButton(IDX_EXPORT, !selected.isEmpty());
			
			updateSourceViewerInput(selected);
		}

		@Override
		public void doubleClicked(TreeListDialogField<Object> field) {
			List<Object> selected= field.getSelectedElements();
			if (canEdit(selected)) {
				doButtonPressed(IDX_EDIT, selected);
			}
		}

		@Override
		public Object[] getChildren(TreeListDialogField<Object> field, Object element) {
			if (element == COMMENT_NODE || element == CODE_NODE) {
				return getCodeTemplatesOfCategory(element == COMMENT_NODE);
			} else if (element == FILE_NODE) {
				return getFileTemplateContextTypes();
			} else if (element instanceof TemplateContextType) {
				return getTemplatesOfContextType(((TemplateContextType) element).getId());
			}
			return NO_CHILDREN;
		}

		@Override
		public Object getParent(TreeListDialogField<Object> field, Object element) {
			if (element instanceof TemplatePersistenceData) {
				TemplatePersistenceData data= (TemplatePersistenceData) element;
				if (data.getTemplate().getName().endsWith(CodeTemplateContextType.COMMENT_SUFFIX)) {
					return COMMENT_NODE;
				}
				if (FileTemplateContextType.isFileTemplateContextType(data.getTemplate().getContextTypeId())) {
					return getFileTemplateContextRegistry().getContextType(data.getTemplate().getContextTypeId());
				}
				return CODE_NODE;
			} else if (element instanceof TemplateContextType) {
				return FILE_NODE;
			}
			return null;
		}

		@Override
		public boolean hasChildren(TreeListDialogField<Object> field, Object element) {
			return (element == COMMENT_NODE || element == CODE_NODE || element == FILE_NODE || element instanceof TemplateContextType);
		}

		@Override
		public void dialogFieldChanged(DialogField field) {
			if (field == fGenerateComments) {
				setValue(PREF_GENERATE_COMMENTS, fGenerateComments.isSelected());
			}
		}

		@Override
		public void keyPressed(TreeListDialogField<Object> field, KeyEvent event) {
		}
		
		/*
		 * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
		 */
		@Override
		public int category(Object element) {
			if (element == COMMENT_NODE) {
				return 1;
			} else if (element == CODE_NODE) {
				return 2;
			} else if (element == FILE_NODE) {
				return 3;
			} else if (element instanceof TemplateContextType) {
				TemplateContextType type= (TemplateContextType) element;
				String id= type.getId();
				if (CodeTemplateContextType.CPPSOURCEFILE_CONTEXTTYPE.equals(id)) {
					return 1;
				} else if (CodeTemplateContextType.CPPHEADERFILE_CONTEXTTYPE.equals(id)) {
					return 2;
				} else if (CodeTemplateContextType.CSOURCEFILE_CONTEXTTYPE.equals(id)) {
					return 10;
				} else if (CodeTemplateContextType.CHEADERFILE_CONTEXTTYPE.equals(id)) {
					return 11;
				} else if (CodeTemplateContextType.ASMSOURCEFILE_CONTEXTTYPE.equals(id)) {
					return 100;
				} else if (id.startsWith("org.eclipse.cdt.")) { //$NON-NLS-1$
					return 101;
				}
				return 1000;
			}
			
			TemplatePersistenceData data= (TemplatePersistenceData) element;
			String id= data.getId();
			
			if (CodeTemplateContextType.ASMSOURCEFILE_CONTEXTTYPE.equals(id)) {
				return 105;
			} else if (CodeTemplateContextType.NAMESPACE_BEGIN_ID.equals(id)) {
				return 106;
			} else if (CodeTemplateContextType.NAMESPACE_END_ID.equals(id)) {
				return 107;
			} else if (CodeTemplateContextType.CLASS_BODY_ID.equals(id)) {
				return 108;
			} else if (CodeTemplateContextType.METHODSTUB_ID.equals(id)) {
				return 109;
			} else if (CodeTemplateContextType.CONSTRUCTORSTUB_ID.equals(id)) {
				return 110;
			} else if (CodeTemplateContextType.DESTRUCTORSTUB_ID.equals(id)) {
				return 111;
			} else if (CodeTemplateContextType.FILECOMMENT_ID.equals(id)) {
				return 1;
			} else if (CodeTemplateContextType.TYPECOMMENT_ID.equals(id)) {
				return 2;
			} else if (CodeTemplateContextType.FIELDCOMMENT_ID.equals(id)) {
				return 3;
			} else if (CodeTemplateContextType.METHODCOMMENT_ID.equals(id)) {
				return 4;
			} else if (CodeTemplateContextType.CONSTRUCTORCOMMENT_ID.equals(id)) {
				return 5;
			} else if (CodeTemplateContextType.DESTRUCTORCOMMENT_ID.equals(id)) {
				return 6;
			}
			return 1000;
		}
	}

	private static class CodeTemplateLabelProvider extends LabelProvider {
		/*
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		 */
		@Override
		public Image getImage(Object element) {
//			if (element == COMMENT_NODE || element == CODE_NODE || element == FILE_NODE) {
//				return null;
//			}
//			if (element instanceof TemplateContextType) {
//				String id= ((TemplateContextType)element).getId();
//				if (FileTemplateContextType.isFileTemplateContextType(id)) {
//					IContentType contentType= FileTemplateContextType.getContentTypeFromConextType(id);
//					if (contentType != null) {
//						String dummyFileName;
//						String[] fileSpecs= contentType.getFileSpecs(IContentType.FILE_NAME_SPEC);
//						if (fileSpecs.length > 0) {
//							dummyFileName= fileSpecs[0];
//						} else {
//							String[] extSpecs= contentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
//							if (extSpecs.length > 0) {
//								dummyFileName= "dummy" + extSpecs[0]; //$NON-NLS-1$
//							} else {
//								dummyFileName= "dummy"; //$NON-NLS-1$
//							}
//						}
//						IEditorRegistry editorRegistry= PlatformUI.getWorkbench().getEditorRegistry();
//						IEditorDescriptor[] editorDesc= editorRegistry.getEditors(dummyFileName, contentType);
//						if (editorDesc.length > 0) {
//							ImageDescriptor desc= editorDesc[0].getImageDescriptor();
//							return CUIPlugin.getImageDescriptorRegistry().get(desc);
//						}
//					}
//				}
//			}
			return null;
		}

		/*
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element) {
			if (element == COMMENT_NODE || element == CODE_NODE || element == FILE_NODE) {
				return (String) element;
			}
			if (element instanceof TemplateContextType) {
				return ((TemplateContextType) element).getName();
			}
			TemplatePersistenceData data= (TemplatePersistenceData) element;
			String id= data.getId();
			if (FileTemplateContextType.isFileTemplateContextType(data.getTemplate().getContextTypeId())) {
				return data.getTemplate().getName();
			} else if (CodeTemplateContextType.NAMESPACE_BEGIN_ID.equals(id)) {
				return PreferencesMessages.CodeTemplateBlock_namespace_begin_label;
			} else if (CodeTemplateContextType.NAMESPACE_END_ID.equals(id)) {
				return PreferencesMessages.CodeTemplateBlock_namespace_end_label;
			} else if (CodeTemplateContextType.CLASS_BODY_ID.equals(id)) {
				return PreferencesMessages.CodeTemplateBlock_class_body_label;
			} else if (CodeTemplateContextType.CONSTRUCTORSTUB_ID.equals(id)) {
				return PreferencesMessages.CodeTemplateBlock_constructorstub_label;
			} else if (CodeTemplateContextType.DESTRUCTORSTUB_ID.equals(id)) {
				return PreferencesMessages.CodeTemplateBlock_destructorstub_label;
			} else if (CodeTemplateContextType.METHODSTUB_ID.equals(id)) {
				return PreferencesMessages.CodeTemplateBlock_methodstub_label;
			} else if (CodeTemplateContextType.FILECOMMENT_ID.equals(id)) {
				return PreferencesMessages.CodeTemplateBlock_filecomment_label;
			} else if (CodeTemplateContextType.TYPECOMMENT_ID.equals(id)) {
				return PreferencesMessages.CodeTemplateBlock_typecomment_label;
			} else if (CodeTemplateContextType.FIELDCOMMENT_ID.equals(id)) {
				return PreferencesMessages.CodeTemplateBlock_fieldcomment_label;
			} else if (CodeTemplateContextType.METHODCOMMENT_ID.equals(id)) {
				return PreferencesMessages.CodeTemplateBlock_methodcomment_label;
			} else if (CodeTemplateContextType.CONSTRUCTORCOMMENT_ID.equals(id)) {
				return PreferencesMessages.CodeTemplateBlock_constructorcomment_label;
			} else if (CodeTemplateContextType.DESTRUCTORCOMMENT_ID.equals(id)) {
				return PreferencesMessages.CodeTemplateBlock_destructorcomment_label;
			}
			return data.getTemplate().getName();
		}
	}		
	
	private static final Key PREF_GENERATE_COMMENTS= getCDTUIKey(PreferenceConstants.CODEGEN_ADD_COMMENTS);
	
	private static Key[] getAllKeys() {
		return new Key[] {
			PREF_GENERATE_COMMENTS
		};	
	}	
	
	private final static int IDX_ADD= 0;
	private final static int IDX_EDIT= 1;
	private final static int IDX_REMOVE= 2;
	private final static int IDX_IMPORT= 3;
	private final static int IDX_EXPORT= 4;
	private final static int IDX_EXPORTALL= 5;
	
	protected final static Object COMMENT_NODE= PreferencesMessages.CodeTemplateBlock_templates_comment_node; 
	protected final static Object CODE_NODE= PreferencesMessages.CodeTemplateBlock_templates_code_node; 
	protected final static Object FILE_NODE= PreferencesMessages.CodeTemplateBlock_templates_file_node; 

	private TreeListDialogField<Object> fCodeTemplateTree;
	private SelectionButtonDialogField fGenerateComments;
	
	protected ProjectTemplateStore fTemplateStore;
	
	private PixelConverter fPixelConverter;
	private SourceViewer fPatternViewer;

	private TemplateVariableProcessor fTemplateProcessor;
	private ContextTypeRegistry fFileTemplateContextTypes;
	
	public CodeTemplateBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getAllKeys(), container);
		
		fTemplateStore= new ProjectTemplateStore(project);
		try {
			fTemplateStore.load();
		} catch (IOException e) {
			CUIPlugin.log(e);
		}
		
		fTemplateProcessor= new TemplateVariableProcessor();
		
		CodeTemplateAdapter adapter= new CodeTemplateAdapter();

		String[] buttonLabels= new String[] {
			PreferencesMessages.CodeTemplateBlock_templates_new_button,	
			PreferencesMessages.CodeTemplateBlock_templates_edit_button,
			PreferencesMessages.CodeTemplateBlock_templates_remove_button,	
			PreferencesMessages.CodeTemplateBlock_templates_import_button, 
			PreferencesMessages.CodeTemplateBlock_templates_export_button, 
			PreferencesMessages.CodeTemplateBlock_templates_exportall_button
		};		
		fCodeTemplateTree= new TreeListDialogField<Object>(adapter, buttonLabels, new CodeTemplateLabelProvider());
		fCodeTemplateTree.setDialogFieldListener(adapter);
		fCodeTemplateTree.setLabelText(PreferencesMessages.CodeTemplateBlock_templates_label);
		fCodeTemplateTree.setViewerComparator(adapter);

		fCodeTemplateTree.enableButton(IDX_EXPORT, false);
		fCodeTemplateTree.enableButton(IDX_ADD, false);
		fCodeTemplateTree.enableButton(IDX_EDIT, false);
		fCodeTemplateTree.enableButton(IDX_REMOVE, false);
		
		fCodeTemplateTree.addElement(COMMENT_NODE);
		fCodeTemplateTree.addElement(CODE_NODE);
		fCodeTemplateTree.addElement(FILE_NODE);

		fCodeTemplateTree.selectFirstElement();	
		
		fGenerateComments= new SelectionButtonDialogField(SWT.CHECK | SWT.WRAP);
		fGenerateComments.setDialogFieldListener(adapter);
		fGenerateComments.setLabelText(PreferencesMessages.CodeTemplateBlock_createcomment_label); 
		
		updateControls();
	}

	public void postSetSelection(Object element) {
		fCodeTemplateTree.postSetSelection(new StructuredSelection(element));
	}

	@Override
	public boolean hasProjectSpecificOptions(IProject project) {
		if (super.hasProjectSpecificOptions(project))
			return true;
		
		if (project != null) {
			return ProjectTemplateStore.hasProjectSpecificTempates(project);
		}
		return false;
	}	
	
	@Override
	protected Control createContents(Composite parent) {
		fPixelConverter=  new PixelConverter(parent);

		setShell(parent.getShell());
		
		Composite composite=  new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		composite.setLayout(layout);
		
		fCodeTemplateTree.doFillIntoGrid(composite, 3);
		LayoutUtil.setHorizontalSpan(fCodeTemplateTree.getLabelControl(null), 2);
		LayoutUtil.setHorizontalGrabbing(fCodeTemplateTree.getTreeControl(null), true);
		
		fPatternViewer= createViewer(composite, 2);
		
		fGenerateComments.doFillIntoGrid(composite, 2);
		
		return composite;
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.OptionsConfigurationBlock#updateControls()
	 */
	@Override
	protected void updateControls() {
		fGenerateComments.setSelection(getBooleanValue(PREF_GENERATE_COMMENTS));
	}
	
	private SourceViewer createViewer(Composite parent, int nColumns) {
		Label label= new Label(parent, SWT.NONE);
		label.setText(PreferencesMessages.CodeTemplateBlock_preview); 
		GridData data= new GridData();
		data.horizontalSpan= nColumns;
		label.setLayoutData(data);
		
		IDocument document= new Document();
		CTextTools tools= CUIPlugin.getDefault().getTextTools();
		tools.setupCDocumentPartitioner(document, ICPartitions.C_PARTITIONING, null);
		IPreferenceStore store= CUIPlugin.getDefault().getCombinedPreferenceStore();
		SourceViewer viewer= new CSourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL, store);
		CodeTemplateSourceViewerConfiguration configuration= new CodeTemplateSourceViewerConfiguration(tools.getColorManager(), store, null, fTemplateProcessor);
		viewer.configure(configuration);
		viewer.setEditable(false);
		viewer.setDocument(document);
	
		Font font= JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
		viewer.getTextWidget().setFont(font);
		new CSourcePreviewerUpdater(viewer, configuration, store);
		
		Control control= viewer.getControl();
		data= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		data.horizontalSpan= nColumns;
		data.heightHint= fPixelConverter.convertHeightInCharsToPixels(5);
		control.setLayoutData(data);
		
		return viewer;
	}

	private void reconfigurePatternViewer() {
		if (fPatternViewer == null)
			return;
		CTextTools tools= CUIPlugin.getDefault().getTextTools();
		IPreferenceStore store= CUIPlugin.getDefault().getCombinedPreferenceStore();
		CodeTemplateSourceViewerConfiguration configuration= new CodeTemplateSourceViewerConfiguration(tools.getColorManager(), store, null, fTemplateProcessor);
		fPatternViewer.unconfigure();
		fPatternViewer.configure(configuration);
		fPatternViewer.invalidateTextPresentation();
	}

	protected TemplatePersistenceData[] getCodeTemplatesOfCategory(boolean isComment) {
		ArrayList<TemplatePersistenceData> res=  new ArrayList<TemplatePersistenceData>();
		TemplatePersistenceData[] templates= fTemplateStore.getTemplateData();
		for (TemplatePersistenceData curr : templates) {
			boolean isUserAdded= curr.getId() == null;
			boolean isFileTemplate= FileTemplateContextType.isFileTemplateContextType(curr.getTemplate().getContextTypeId());
			if (!isUserAdded && !isFileTemplate && isComment == curr.getTemplate().getName().endsWith(CodeTemplateContextType.COMMENT_SUFFIX)) {
				res.add(curr);
			}
		}
		return res.toArray(new TemplatePersistenceData[res.size()]);
	}
	
	private TemplatePersistenceData[] getTemplatesOfContextType(TemplateContextType contextType) {
		return getTemplatesOfContextType(contextType.getId());
	}

	protected TemplatePersistenceData[] getTemplatesOfContextType(String contextTypeId) {
		ArrayList<TemplatePersistenceData> res=  new ArrayList<TemplatePersistenceData>();
		TemplatePersistenceData[] templates= fTemplateStore.getTemplateData();
		for (TemplatePersistenceData curr : templates) {
			if (contextTypeId.equals(curr.getTemplate().getContextTypeId())) {
				res.add(curr);
			}
		}
		return res.toArray(new TemplatePersistenceData[res.size()]);
	}

	protected ContextTypeRegistry getFileTemplateContextRegistry() {
		if (fFileTemplateContextTypes == null) {
			fFileTemplateContextTypes= new ContextTypeRegistry();
			Iterator<?> contextTypesIter= CUIPlugin.getDefault().getCodeTemplateContextRegistry().contextTypes();
			while(contextTypesIter.hasNext()) {
				TemplateContextType contextType= (TemplateContextType)contextTypesIter.next();
				final String contextTypeId= contextType.getId();
				// add if at least one template registered
				if (FileTemplateContextType.isFileTemplateContextType(contextTypeId)) {
					fFileTemplateContextTypes.addContextType(contextType);
				}
			}
		}
		return fFileTemplateContextTypes;
	}
	
	protected TemplateContextType[] getFileTemplateContextTypes() {
		Iterator<?> iter= getFileTemplateContextRegistry().contextTypes();
		ArrayList<TemplateContextType> result= new ArrayList<TemplateContextType>();
		while (iter.hasNext()) {
			TemplateContextType contextType= (TemplateContextType)iter.next();
			if (getTemplatesOfContextType(contextType).length > 0) {
				result.add(contextType);
			}
		}
		return result.toArray(new TemplateContextType[0]);
	}

	protected static boolean canAdd(List<Object> selected) {
		if (selected.size() == 1) {
			Object element= selected.get(0);
			if (element instanceof TemplateContextType || element == FILE_NODE) {
				return true;
			}
			if (element instanceof TemplatePersistenceData) {
				TemplatePersistenceData data = (TemplatePersistenceData) element;
				if (FileTemplateContextType.isFileTemplateContextType(data.getTemplate().getContextTypeId())) {
					return true;
				}
			}
		}
		return false;
	}	
	
	protected static boolean canEdit(List<Object> selected) {
		return selected.size() == 1 && (selected.get(0) instanceof TemplatePersistenceData);
	}	
	
	protected static boolean canRemove(List<Object> selected) {
		if (selected.size() == 1 && (selected.get(0) instanceof TemplatePersistenceData)) {
			TemplatePersistenceData data= (TemplatePersistenceData)selected.get(0);
			return data.isUserAdded();
		}
		return false;
	}
	
	protected void updateSourceViewerInput(List<Object> selection) {
		if (fPatternViewer == null || fPatternViewer.getTextWidget().isDisposed()) {
			return;
		}
		if (selection.size() == 1 && selection.get(0) instanceof TemplatePersistenceData) {
			TemplatePersistenceData data= (TemplatePersistenceData) selection.get(0);
			Template template= data.getTemplate();
			TemplateContextType type= CUIPlugin.getDefault().getCodeTemplateContextRegistry().getContextType(template.getContextTypeId());
			fTemplateProcessor.setContextType(type);
			reconfigurePatternViewer();
			fPatternViewer.getDocument().set(template.getPattern());
		} else {
			fPatternViewer.getDocument().set(""); //$NON-NLS-1$
		}		
	}

	protected void doButtonPressed(int buttonIndex, List<Object> selected) {
		switch (buttonIndex) {
		case IDX_EDIT:
			edit((TemplatePersistenceData) selected.get(0), false);
			break;
		case IDX_ADD: {
			Object element= selected.get(0);
			Template orig= null;
			String contextTypeId;
			if (element instanceof TemplatePersistenceData) {
				orig= ((TemplatePersistenceData)element).getTemplate();
				contextTypeId= orig.getContextTypeId();
			} else if (element instanceof TemplateContextType) {
				TemplateContextType type= (TemplateContextType)selected.get(0);
				contextTypeId= type.getId();
			} else {
				// default: text file
				contextTypeId= FileTemplateContextType.TEXTFILE_CONTEXTTYPE;
			}
			Template newTemplate;
			if (orig != null) {
				newTemplate= new Template("", "", contextTypeId, orig.getPattern(), false);  //$NON-NLS-1$//$NON-NLS-2$
			} else {
				newTemplate= new Template("", "", contextTypeId, "", false);   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			}
			TemplatePersistenceData newData= new TemplatePersistenceData(newTemplate, true);
			edit(newData, true);
			break;
		}
		case IDX_REMOVE:
			remove((TemplatePersistenceData) selected.get(0));
			break;
		case IDX_EXPORT:
			export(selected);
			break;
		case IDX_EXPORTALL:
			exportAll();
			break;
		case IDX_IMPORT:
			import_();
			break;
		}
	}
	
	private void remove(TemplatePersistenceData data) {
		if (data.isUserAdded()) {
			fTemplateStore.delete(data);
			fCodeTemplateTree.refresh();
		}
	}

	private void edit(TemplatePersistenceData data, boolean isNew) {
		Template newTemplate= new Template(data.getTemplate());
		boolean isFileTemplate= FileTemplateContextType.isFileTemplateContextType(newTemplate.getContextTypeId());
		ContextTypeRegistry contextTypeRegistry;
		if (isFileTemplate) {
			contextTypeRegistry= getFileTemplateContextRegistry();
		} else {
			contextTypeRegistry= CUIPlugin.getDefault().getCodeTemplateContextRegistry();
		}
		EditTemplateDialog dialog= new EditTemplateDialog(getShell(), newTemplate, !isNew, data.isUserAdded(), isFileTemplate, contextTypeRegistry);
		if (dialog.open() == Window.OK) {
			// changed
			data.setTemplate(dialog.getTemplate());
			if (isNew) {
				// add to store
				fTemplateStore.addTemplateData(data);
			}
			if (isNew || isFileTemplate) {
				fCodeTemplateTree.refresh();
			} else {
				fCodeTemplateTree.refresh(data);
			}
			fCodeTemplateTree.selectElements(new StructuredSelection(data));
		}
	}
		
	private void import_() {
		FileDialog dialog= new FileDialog(getShell());
		dialog.setText(PreferencesMessages.CodeTemplateBlock_import_title); 
		dialog.setFilterExtensions(new String[] {PreferencesMessages.CodeTemplateBlock_import_extension}); 
		String path= dialog.open();
		
		if (path == null)
			return;
		
		try {
			TemplateReaderWriter reader= new TemplateReaderWriter();
			File file= new File(path);
			if (file.exists()) {
				InputStream input= new BufferedInputStream(new FileInputStream(file));
				try {
					TemplatePersistenceData[] datas= reader.read(input, null);
					for (TemplatePersistenceData data : datas) {
						updateTemplate(data);
					}
				} finally {
					try {
						input.close();
					} catch (IOException x) {
					}
				}
			}

			fCodeTemplateTree.refresh();
			updateSourceViewerInput(fCodeTemplateTree.getSelectedElements());
		} catch (FileNotFoundException e) {
			openReadErrorDialog(e);
		} catch (IOException e) {
			openReadErrorDialog(e);
		}
	}
	
	private void updateTemplate(TemplatePersistenceData data) {
		String dataId= data.getId();
		TemplatePersistenceData[] datas= fTemplateStore.getTemplateData();
		if (dataId != null) {
			// predefined
			for (TemplatePersistenceData data2 : datas) {
				String id= data2.getId();
				if (id != null && id.equals(dataId)) {
					data2.setTemplate(data.getTemplate());
					return;
				}
			}
		} else {
			// user added
			String dataName= data.getTemplate().getName();
			for (TemplatePersistenceData data2 : datas) {
				if (data2.getId() == null) {
					String name= data2.getTemplate().getName();
					String contextTypeId= data2.getTemplate().getContextTypeId();
					if (name != null && name.equals(dataName) && contextTypeId.equals(data.getTemplate().getContextTypeId())) {
						data2.setTemplate(data.getTemplate());
						return;
					}
				}
			}
			// new
			fTemplateStore.addTemplateData(data);
		}
	}
	
	private void exportAll() {
		export(fTemplateStore.getTemplateData());	
	}
	
	private void export(List<Object> selected) {
		Set<Object> datas= new HashSet<Object>();
		for (int i= 0; i < selected.size(); i++) {
			Object curr= selected.get(i);
			if (curr instanceof TemplatePersistenceData) {
				datas.add(curr);
			} else if (curr instanceof TemplateContextType) {
				TemplatePersistenceData[] cat= getTemplatesOfContextType((TemplateContextType)curr);
				datas.addAll(Arrays.asList(cat));
			} else if (curr == FILE_NODE) {
				TemplateContextType[] types= getFileTemplateContextTypes();
				for (TemplateContextType contextType : types) {
					TemplatePersistenceData[] cat= getTemplatesOfContextType(contextType);
					datas.addAll(Arrays.asList(cat));
				}
			} else if (curr == COMMENT_NODE || curr == CODE_NODE) {
				TemplatePersistenceData[] cat= getCodeTemplatesOfCategory(curr == COMMENT_NODE);
				datas.addAll(Arrays.asList(cat));
			}
		}
		export(datas.toArray(new TemplatePersistenceData[datas.size()]));
	}
	
	private void export(TemplatePersistenceData[] templates) {
		FileDialog dialog= new FileDialog(getShell(), SWT.SAVE);
		dialog.setText(Messages.format(PreferencesMessages.CodeTemplateBlock_export_title, String.valueOf(templates.length))); 
		dialog.setFilterExtensions(new String[] {PreferencesMessages.CodeTemplateBlock_export_extension}); 
		dialog.setFileName(PreferencesMessages.CodeTemplateBlock_export_filename); 
		String path= dialog.open();

		if (path == null)
			return;
		
		File file= new File(path);		

		if (file.isHidden()) {
			String title= PreferencesMessages.CodeTemplateBlock_export_error_title; 
			String message= Messages.format(PreferencesMessages.CodeTemplateBlock_export_error_hidden, file.getAbsolutePath()); 
			MessageDialog.openError(getShell(), title, message);
			return;
		}
		
		if (file.exists() && !file.canWrite()) {
			String title= PreferencesMessages.CodeTemplateBlock_export_error_title; 
			String message= Messages.format(PreferencesMessages.CodeTemplateBlock_export_error_canNotWrite, file.getAbsolutePath()); 
			MessageDialog.openError(getShell(), title, message);
			return;
		}

		if (!file.exists() || confirmOverwrite(file)) {
			OutputStream output= null;
			try {
				output= new BufferedOutputStream(new FileOutputStream(file));
				TemplateReaderWriter writer= new TemplateReaderWriter();
				writer.save(templates, output);
				output.close();
			} catch (IOException e) {
				if (output != null) {
					try {
						output.close();
					} catch (IOException e2) {
						// ignore 
					}
				}
				openWriteErrorDialog();
			}
		}
	}

	private boolean confirmOverwrite(File file) {
		return MessageDialog.openQuestion(getShell(),
			PreferencesMessages.CodeTemplateBlock_export_exists_title, 
			Messages.format(PreferencesMessages.CodeTemplateBlock_export_exists_message, file.getAbsolutePath())); 
	}

	@Override
	public void performDefaults() {
		super.performDefaults();
		fTemplateStore.restoreDefaults();
		
		// refresh
		fCodeTemplateTree.refresh();
		updateSourceViewerInput(fCodeTemplateTree.getSelectedElements());
	}
	
	public boolean performOk(boolean enabled) {
		boolean res= super.performOk();
		if (!res)
			return false;
		
		if (fProject != null) {
			TemplatePersistenceData[] templateData= fTemplateStore.getTemplateData();
			for (TemplatePersistenceData element : templateData) {
				fTemplateStore.setProjectSpecific(element.getId(), enabled);
			}
		}
		try {
			fTemplateStore.save();
		} catch (IOException e) {
			CUIPlugin.log(e);
			openWriteErrorDialog();
		}
		return true;
	}
	
	public void performCancel() {
		try {
			fTemplateStore.revertChanges();
		} catch (IOException e) {
			openReadErrorDialog(e);
		}
	}
	
	private void openReadErrorDialog(Exception e) {
		String title= PreferencesMessages.CodeTemplateBlock_error_read_title; 
		
		String message= e.getLocalizedMessage();
		if (message != null)
			message= Messages.format(PreferencesMessages.CodeTemplateBlock_error_parse_message, message); 
		else
			message= PreferencesMessages.CodeTemplateBlock_error_read_message; 
		MessageDialog.openError(getShell(), title, message);
	}
	
	private void openWriteErrorDialog() {
		String title= PreferencesMessages.CodeTemplateBlock_error_write_title; 
		String message= PreferencesMessages.CodeTemplateBlock_error_write_message; 
		MessageDialog.openError(getShell(), title, message);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.OptionsConfigurationBlock#validateSettings(java.lang.String, java.lang.String)
	 */
	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		// no validation here
	}
}
