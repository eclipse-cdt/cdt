/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import java.util.ArrayList;

import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.cdt.internal.ui.dialogs.TypedViewerFilter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CElementSorter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

public class SourceFileSelectionDialog extends SelectionStatusDialog {
    
    TreeViewer fViewer;
    private final ITreeContentProvider fContentProvider = new CElementContentProvider();
    private final ILabelProvider fLabelProvider = new CElementLabelProvider(CElementLabelProvider.SHOW_DEFAULT);
    IStatus fCurrStatus = new StatusInfo();
    IStatus fFolderNameStatus = new StatusInfo();
    IStatus fFileNameStatus = new StatusInfo();
    ICElement fInput;
    private int fWidth = 60;
    private int fHeight = 18;
    StringDialogField fFolderNameDialogField;
    StringDialogField fFileNameDialogField;
    private IWorkspaceRoot fWorkspaceRoot;
    private final FieldsAdapter fFieldsAdapter = new FieldsAdapter();
    
	private ICElement fCurrentFolder = null;
	private String fCurrentFileString = null;
    String fInitialFolderName = null;
    String fInitialFileName = null;
    
    private final class FieldsAdapter extends SelectionAdapter
    	implements ISelectionChangedListener, IDoubleClickListener, IDialogFieldListener {
        
        // -- SelectionAdapter --
        public void widgetDefaultSelected(SelectionEvent e) {
            doStatusUpdate();
            if (fCurrStatus.isOK())
                buttonPressed(IDialogConstants.OK_ID);
        }
        
        // -- ISelectionChangedListener --
        public void selectionChanged(SelectionChangedEvent event) {
            setResult(((IStructuredSelection) event.getSelection()).toList());
            ISelection sel = event.getSelection();
            if (sel instanceof IStructuredSelection) {
                Object obj = ((IStructuredSelection) sel).getFirstElement();
                if (obj instanceof ICElement) {
                    ICElement elem = (ICElement) obj;
                    IPath path = elem.getPath();
                    String fileName = fFileNameDialogField.getText();
                    String folderName = fFolderNameDialogField.getText();
                    if (elem instanceof ICContainer || elem instanceof ICProject) {
                        folderName = path.toString();
                    } else {
                        folderName = path.removeLastSegments(1).toString();
                        fileName = path.lastSegment();
                    }
                    setPathFields(folderName, fileName);
                }
            }
            doStatusUpdate();
        }
        
        // -- IDoubleClickListener --
        public void doubleClick(DoubleClickEvent event) {
            doStatusUpdate();
            
            ISelection selection = event.getSelection();
            if (selection instanceof IStructuredSelection) {
                Object item = ((IStructuredSelection)selection).getFirstElement();
	            if (fCurrStatus.getSeverity() != IStatus.ERROR) {
                    if (item instanceof ITranslationUnit) {
                        setResult(((IStructuredSelection)selection).toList());
                        close();
                        return;
                    }
	            }
                if (fViewer.getExpandedState(item))
                    fViewer.collapseToLevel(item, 1);
                else
                    fViewer.expandToLevel(item, 1);
            }
        }

        // -- IDialogFieldListener --
        public void dialogFieldChanged(DialogField field) {
            if (field == fFolderNameDialogField) {
    			fFolderNameStatus = folderNameChanged();
    			fFileNameStatus = fileNameChanged();
            } else if (field == fFileNameDialogField) {
    			fFileNameStatus = fileNameChanged();
            }
            doStatusUpdate();
        }
    }
    
    static final Class[] FILTER_TYPES = new Class[] {
            ICModel.class,
            ICProject.class,
            ICContainer.class,
            ITranslationUnit.class
        };

    private final class Filter extends TypedViewerFilter {
        
        private Filter() {
            super(FILTER_TYPES);
        }
        
        public boolean select(Viewer viewer, Object parent, Object obj) {
            if (obj instanceof ICElement) {
                ICElement elem = (ICElement)obj;
                if (!(fInput instanceof ICModel)) {
                    return elem.getCProject().equals(fInput.getCProject());
                }
                return true;
            }
            return super.select(viewer, parent, obj);
        }
    }
    
    /**
     * Constructs an instance of <code>ElementTreeSelectionDialog</code>.
     * 
     * @param parent
     *            The parent shell for the dialog
     * @param labelProvider
     *            the label provider to render the entries
     * @param contentProvider
     *            the content provider to evaluate the tree structure
     */
    public SourceFileSelectionDialog(Shell parent) {
        super(parent);
        
        fWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        fInput = CoreModel.create(fWorkspaceRoot);
        
        fFolderNameDialogField = new StringDialogField();
        fFolderNameDialogField.setDialogFieldListener(fFieldsAdapter);
        fFolderNameDialogField.setLabelText(NewClassWizardMessages.getString("SourceFileSelectionDialog.folderName.label")); //$NON-NLS-1$
        
        fFileNameDialogField = new StringDialogField();
        fFileNameDialogField.setDialogFieldListener(fFieldsAdapter);
        fFileNameDialogField.setLabelText(NewClassWizardMessages.getString("SourceFileSelectionDialog.fileName.label")); //$NON-NLS-1$
        
        setResult(new ArrayList(0));
        setStatusLineAboveButtons(true);
        
        int shellStyle = getShellStyle();
        setShellStyle(shellStyle | SWT.MAX | SWT.RESIZE);
    }
    
    /**
     * Sets the tree input.
     * 
     * @param input
     *            the tree input.
     */
    public void setInput(ICElement input) {
        fInput = input;
    }
    
 	protected void doStatusUpdate() {
		// status of all used components
		IStatus[] status = new IStatus[] {
			fFolderNameStatus,
			fFileNameStatus
		};
		
		// the mode severe status will be displayed and the ok button enabled/disabled.
		updateStatus(status);
	}
    
	/**
	 * Updates the status line and the ok button according to the given status
	 * 
	 * @param status status to apply
	 */
	protected void updateStatus(IStatus status) {
		fCurrStatus = status;
	    super.updateStatus(status);
	}
	
	/**
	 * Updates the status line and the ok button according to the status evaluate from
	 * an array of status. The most severe error is taken.  In case that two status with 
	 * the same severity exists, the status with lower index is taken.
	 * 
	 * @param status the array of status
	 */
	protected void updateStatus(IStatus[] status) {
		updateStatus(StatusUtil.getMostSevere(status));
	}
	
	IStatus folderNameChanged() {
        StatusInfo status = new StatusInfo();
        
		fCurrentFolder = null;
        String str = fFolderNameDialogField.getText();
		if (str.length() == 0) {
			status.setError(NewClassWizardMessages.getString("SourceFileSelectionDialog.error.EnterFolderName")); //$NON-NLS-1$
			return status;
		}

		IPath path = new Path(str);
		IResource res = fWorkspaceRoot.findMember(path);
		if (res != null && res.exists()) {
			int resType = res.getType();
			if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
				IProject proj = res.getProject();
				if (!proj.isOpen()) {
					status.setError(NewClassWizardMessages.getFormattedString("SourceFileSelectionDialog.error.NotAFolder", str)); //$NON-NLS-1$
					return status;
				}
			    ICElement e = CoreModel.getDefault().create(res.getFullPath());
			    fCurrentFolder = CModelUtil.getSourceFolder(e);
				if (fCurrentFolder == null) {
					status.setError(NewClassWizardMessages.getFormattedString("SourceFileSelectionDialog.error.NotASourceFolder", str)); //$NON-NLS-1$
					return status;
				}
			    if (!CoreModel.hasCCNature(proj) && !CoreModel.hasCNature(proj)) {
					if (resType == IResource.PROJECT) {
						status.setError(NewClassWizardMessages.getString("SourceFileSelectionDialog.warning.NotACProject")); //$NON-NLS-1$
						return status;
					}
					status.setWarning(NewClassWizardMessages.getString("SourceFileSelectionDialog.warning.NotInACProject")); //$NON-NLS-1$
				}
			} else {
				status.setError(NewClassWizardMessages.getFormattedString("SourceFileSelectionDialog.error.NotAFolder", str)); //$NON-NLS-1$
				return status;
			}
		} else {
			status.setError(NewClassWizardMessages.getFormattedString("SourceFileSelectionDialog.error.FolderDoesNotExist", str)); //$NON-NLS-1$
			return status;
		}
        return status;
	}

	IStatus fileNameChanged() {
        StatusInfo status = new StatusInfo();
        
        fCurrentFileString = null;
		ICElement existingFile = null;
        String str = fFileNameDialogField.getText();
		if (str.length() == 0) {
			status.setError(NewClassWizardMessages.getString("SourceFileSelectionDialog.error.EnterFileName")); //$NON-NLS-1$
			return status;
		}

		if (fCurrentFolder != null) {
			IPath folderPath = fCurrentFolder.getPath();
			IPath path = new Path(str);
			IResource res = fWorkspaceRoot.findMember(folderPath.append(path));
			if (res == null)
				res = fWorkspaceRoot.findMember(path);
			if (res != null && res.exists()) {
				int resType = res.getType();
				if (resType == IResource.FILE) {
					IProject proj = res.getProject();
					if (!proj.isOpen()) {
						status.setError(NewClassWizardMessages.getFormattedString("SourceFileSelectionDialog.error.NotAFile", str)); //$NON-NLS-1$
						return status;
					}
				    ICElement e = CoreModel.getDefault().create(res.getFullPath());
				    if (e instanceof ITranslationUnit) {
				        existingFile = (ITranslationUnit) e;
				    }
					if (existingFile == null) {
						status.setError(NewClassWizardMessages.getFormattedString("SourceFileSelectionDialog.error.NotASourceFile", str)); //$NON-NLS-1$
						return status;
					}
				    if (!CoreModel.hasCCNature(proj) && !CoreModel.hasCNature(proj)) {
						status.setWarning(NewClassWizardMessages.getString("SourceFileSelectionDialog.warning.NotInACProject")); //$NON-NLS-1$
					}
				} else {
					status.setError(NewClassWizardMessages.getFormattedString("SourceFileSelectionDialog.error.NotAFile", str)); //$NON-NLS-1$
					return status;
				}
			}
		}
		if (existingFile != null) {
			status.setWarning(NewClassWizardMessages.getFormattedString("SourceFileSelectionDialog.warning.SourceFileExists", str)); //$NON-NLS-1$
		}
		fCurrentFileString = str;
        return status;
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#open()
     */
    public int open() {
        super.open();
        return getReturnCode();
    }
    
    /**
     * Handles cancel button pressed event.
     */
    protected void cancelPressed() {
        setResult(null);
        super.cancelPressed();
    }
    
    /*
     * @see SelectionStatusDialog#computeResult()
     */
    protected void computeResult() {
        setResult(((IStructuredSelection) fViewer.getSelection()).toList());
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#create()
     */
    public void create() {
        BusyIndicator.showWhile(null, new Runnable() {
            public void run() {
                superCreate();
                fViewer.setSelection(new StructuredSelection(getInitialElementSelections()), true);
                setPathFields(fInitialFolderName, fInitialFileName);
                fFileNameDialogField.setFocus();
                doStatusUpdate();
            }
        });
    }
    void superCreate() {
        super.create();
    }
    
    /*
     * @see Dialog#createDialogArea(Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        // Label messageLabel = createMessageArea(composite);
        int nColumns = 4;
        fFolderNameDialogField.doFillIntoGrid(composite, nColumns - 1);
        DialogField.createEmptySpace(composite);
        LayoutUtil.setWidthHint(fFolderNameDialogField.getTextControl(null), getMaxFieldWidth());
        
        TreeViewer treeViewer = createTreeViewer(composite);
        
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = convertWidthInCharsToPixels(fWidth);
        data.heightHint = convertHeightInCharsToPixels(fHeight);
        
        Tree treeWidget = treeViewer.getTree();
        treeWidget.setLayoutData(data);
        treeWidget.setFont(parent.getFont());
        
        fFileNameDialogField.doFillIntoGrid(composite, nColumns - 1);
        DialogField.createEmptySpace(composite);
        LayoutUtil.setWidthHint(fFileNameDialogField.getTextControl(null), getMaxFieldWidth());
        
        return composite;
    }
    
    /**
     * Returns the recommended maximum width for text fields (in pixels). This
     * method requires that createContent has been called before this method is
     * call. Subclasses may override to change the maximum width for text
     * fields.
     * 
     * @return the recommended maximum width for text fields.
     */
    protected int getMaxFieldWidth() {
        return convertWidthInCharsToPixels(60);
    }
    
    /**
     * Creates the tree viewer.
     * 
     * @param parent
     *            the parent composite
     * @return the tree viewer
     */
    protected TreeViewer createTreeViewer(Composite parent) {
        int style = (SWT.BORDER | SWT.SINGLE);
        
        fViewer = new TreeViewer(new Tree(parent, style));
        fViewer.setContentProvider(fContentProvider);
        fViewer.setLabelProvider(fLabelProvider);
        fViewer.addSelectionChangedListener(fFieldsAdapter);
        
        fViewer.setSorter(new CElementSorter());
        fViewer.addFilter(new Filter());
        
        Tree tree = fViewer.getTree();
        tree.addSelectionListener(fFieldsAdapter);
        fViewer.addDoubleClickListener(fFieldsAdapter);
        
        fViewer.setInput(fInput.getCModel());
        
        return fViewer;
    }
    
    /**
     * Returns the tree viewer.
     * 
     * @return the tree viewer
     */
    protected TreeViewer getTreeViewer() {
        return fViewer;
    }
    
    /**
     * @see org.eclipse.jface.window.Window#handleShellCloseEvent()
     */
    protected void handleShellCloseEvent() {
        super.handleShellCloseEvent();
        
        //Handle the closing of the shell by selecting the close icon
        if (getReturnCode() == CANCEL)
            setResult(null);
    }
    
    public String getFolderName() {
        if (fCurrentFolder != null) {
            return fCurrentFolder.getPath().toString();
        }
        return null;
    }
    
    public String getFileName() {
        return fCurrentFileString;
    }
    
    public IPath getFilePath() {
        IPath path = null;
        if (fCurrentFolder != null) {
            path = fCurrentFolder.getPath();
            if (fCurrentFileString != null)
                path = path.append(fCurrentFileString);
        } else if (fCurrentFileString != null) {
            path = new Path(fCurrentFileString);
        }
        return path;
    }

    void setPathFields(String folderName, String fileName) {
	    fFolderNameDialogField.setTextWithoutUpdate(folderName != null ? folderName : ""); //$NON-NLS-1$
	    fFileNameDialogField.setTextWithoutUpdate(fileName != null ? fileName : ""); //$NON-NLS-1$
		fFolderNameStatus = folderNameChanged();
		fFileNameStatus = fileNameChanged();
    }
    
    /**
     * Sets the initial selection. Convenience method.
     * 
     * @param initialPath
     *            the initial selection.
     */
    public void setInitialSelection(String folderName, String fileName) {
        fInitialFileName = (fileName != null && fileName.length() > 0) ? fileName : null;
        fInitialFolderName = null;
        if (folderName != null && folderName.length() > 0) {
	        // find a folder that actually exists
            IPath initialFolderPath = new Path(folderName);
            final IPath folderPath = PathUtil.getValidEnclosingFolder(initialFolderPath);
            if (folderPath != null) {
                fInitialFolderName = folderPath.toString();
				if (fInput != null) {
		            final ICElement[] foundElem = {/*base_folder*/ null, /*exact_folder*/ null, /*exact_file*/ null};
		            try {
			            fInput.accept(new ICElementVisitor() {
			                public boolean visit(ICElement elem) {
			                    IPath path = elem.getPath();
			                    if (path.isPrefixOf(folderPath)) {
			                        if (foundElem[0] == null || path.segmentCount() > foundElem[0].getPath().segmentCount()) {
			                            foundElem[0] = elem; /*base_folder*/
			                        }
		                            if (path.equals(folderPath)) {
		                                foundElem[1] = elem; /*exact_folder*/
			                            if (fInitialFileName == null)
			                                return false; // no need to search children
		                            } else if (fInitialFileName != null && elem.getElementName().equals(fInitialFileName)) {
		                                foundElem[2] = elem; /*exact_file*/
		                                return false; // no need to search children
		                            }
		                            return true;
			                    }
			                    return false;
			                }
			            });
		
			            ICElement selectedElement = foundElem[2]; /*exact_file*/
			            if (selectedElement == null)
			                selectedElement = foundElem[1]; /*exact_folder*/
			            if (selectedElement == null)
			                selectedElement = foundElem[0]; /*base_folder*/
		
			            if (selectedElement != null) {
			                setInitialSelections(new Object[] { selectedElement });
			            }
			        } catch (CoreException e) {
			        }
		        }
	        }
        }
    }
}
