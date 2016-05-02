/*******************************************************************************
 * Copyright (c) 2006, 2016 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ken Ryall (Nokia) - initial API and implementation
 * Ken Ryall (Nokia) - Option to open disassembly view when no source ( 81353 )
 * Stefan Sprenger - Additional heuristics and options 491514
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.sourcelookup;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceNotFoundElement;
import org.eclipse.cdt.debug.internal.core.sourcelookup.ICSourceNotFoundDescription;
import org.eclipse.cdt.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.internal.core.model.ExternalTranslationUnit;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.ui.sourcelookup.CommonSourceNotFoundEditor;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Editor that lets you select a replacement for the missing source file
 * and modifies the source locator accordingly.
 */
public class CSourceNotFoundEditor extends CommonSourceNotFoundEditor {
	public final String foundMappingsContainerName = "Found Mappings"; //$NON-NLS-1$
	private static final String UID_KEY = ".uid"; //$NON-NLS-1$
	private static final String UID_CLASS_NAME = CSourceNotFoundEditor.class.getName();
	public static final String UID_DISASSEMBLY_BUTTON = UID_CLASS_NAME+ "disassemblyButton"; //$NON-NLS-1$
	public static final String UID_LOCATE_FILE_BUTTON = UID_CLASS_NAME+ "locateFileButton"; //$NON-NLS-1$
	public static final String UID_LOCATE_FILE_WORKSPACE_BUTTON = UID_CLASS_NAME+ "locateFileWorkspaceButton"; //$NON-NLS-1$
	public static final String UID_EDIT_LOOKUP_BUTTON = UID_CLASS_NAME+ "editLookupButton"; //$NON-NLS-1$
		
	private String missingFile = ""; //$NON-NLS-1$
	private ILaunchConfiguration launch;
	private IAdaptable context;
	private ITranslationUnit tunit;

	private Button disassemblyButton;

	private Button locateFileButton;

	private Button locateFileWorkspaceButton;
	
	private Button editLookupButton;
	private boolean isDebugElement;
	private boolean isTranslationUnit;

	public CSourceNotFoundEditor() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);	
		Composite container = new Composite(sc, SWT.NONE);
	    sc.setContent(container);
	    sc.setBackgroundMode(SWT.INHERIT_DEFAULT);
	    sc.setExpandHorizontal(true);
	    sc.setExpandVertical(true);
		super.createPartControl(container);
	    sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ICDebugHelpContextIds.SOURCE_NOT_FOUND);
	}

	@Override
	public void setInput(IEditorInput input) {
		if (input instanceof CSourceNotFoundEditorInput) {
			isDebugElement = false;
			isTranslationUnit =  false;
			Object artifact = ((CSourceNotFoundEditorInput) input).getArtifact();
			if (artifact instanceof CSourceNotFoundElement) {
				CSourceNotFoundElement element = (CSourceNotFoundElement) artifact;
				missingFile = element.getFile();
				launch = element.getLaunch();
				context = element.getElement();
				isDebugElement = true;
			} else if (artifact instanceof ITranslationUnit) {
				isTranslationUnit = true;
				tunit = (ITranslationUnit) artifact;
				IPath tuPath = tunit.getLocation();
				if (tuPath != null)
					missingFile = tuPath.toOSString();
			} else {
				missingFile = ""; //$NON-NLS-1$
			}
		}
		super.setInput(input);
		syncButtons();
	}

	private void syncButtons() {
		if (locateFileButton != null)
			locateFileButton.setVisible(missingFile.length() > 0);
		if (editLookupButton != null)
			editLookupButton.setVisible(missingFile.length() > 0);
	}

	@Override
	protected String getText() {
		if (missingFile.length() > 0) {
			return NLS.bind(SourceLookupUIMessages.CSourceNotFoundEditor_0, missingFile);
		} else {
			if (context == null)
				return super.getText();
			String contextDescription;
			ICSourceNotFoundDescription description = context.getAdapter(ICSourceNotFoundDescription.class);
			if (description != null)
				contextDescription = description.getDescription();
			else
				contextDescription = context.toString();
			return NLS.bind(SourceLookupUIMessages.CSourceNotFoundEditor_3, contextDescription);		
		}
	}

	@Override
	protected void createButtons(Composite parent) {
							
		Composite optionComposite = new Composite(parent,SWT.NONE);
		optionComposite.setLayout(new RowLayout(SWT.VERTICAL));
				
		Label helpText = new Label(optionComposite,SWT.NONE);
		helpText.setText(SourceLookupUIMessages.CSourceNotFoundEditor_7);
		
		Group optionContainer = new Group(optionComposite, SWT.SHADOW_NONE);
		optionContainer.setLayout(new RowLayout(SWT.VERTICAL));
				
	    Group groupVBOXHeuristics = new Group(optionContainer, SWT.SHADOW_IN);
	    groupVBOXHeuristics.setText(SourceLookupUIMessages.CSourceNotFoundEditor_8);
	    groupVBOXHeuristics.setLayout(new RowLayout(SWT.VERTICAL));
	    
	    fillEstimatedResults(groupVBOXHeuristics);
	    
	    Group groupVBOXManual = new Group(optionContainer, SWT.SHADOW_IN);
	    groupVBOXManual.setText(SourceLookupUIMessages.CSourceNotFoundEditor_9);
	    groupVBOXManual.setLayout(new RowLayout(SWT.VERTICAL));

		if (isDebugElement) {
		    Group groupVBOXAlternative = new Group(optionContainer, SWT.SHADOW_IN);
		    groupVBOXAlternative.setText(SourceLookupUIMessages.CSourceNotFoundEditor_10);
		    groupVBOXAlternative.setLayout(new RowLayout(SWT.VERTICAL));
		    
			disassemblyButton = new Button(groupVBOXAlternative, SWT.PUSH);
			disassemblyButton.setText(SourceLookupUIMessages.CSourceNotFoundEditor_4);
			disassemblyButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					viewDisassembly();
				}
			});
			disassemblyButton.setData(UID_KEY, UID_DISASSEMBLY_BUTTON);
			
			editLookupButton = new Button(groupVBOXAlternative, SWT.PUSH);
			editLookupButton.setText(SourceLookupUIMessages.CSourceNotFoundEditor_5);
			editLookupButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					editSourceLookupPath();
				}
			});
			editLookupButton.setData(UID_KEY, UID_EDIT_LOOKUP_BUTTON);
		}

		{
			locateFileButton = new Button(groupVBOXManual, SWT.PUSH);
			locateFileButton.setText(SourceLookupUIMessages.CSourceNotFoundEditor_1);
			locateFileButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					locateFileFromHardDiskDialog();
				}
			});
			locateFileButton.setData(UID_KEY, UID_LOCATE_FILE_BUTTON);
		}
		
		{
			locateFileWorkspaceButton = new Button(groupVBOXManual, SWT.PUSH);
			locateFileWorkspaceButton.setText(SourceLookupUIMessages.CSourceNotFoundEditor_6);
			locateFileWorkspaceButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					locateFileFromWorkspaceDialog();
				}
			});
			locateFileWorkspaceButton.setData(UID_KEY, UID_LOCATE_FILE_WORKSPACE_BUTTON);
		}
		
		syncButtons();
	}

	private void fillEstimatedResults(Group groupVBOXHeuristics) {
		
		final Table estimationTable = new Table(groupVBOXHeuristics, SWT.BORDER | SWT.MULTI);
		estimationTable.setLinesVisible(true);
		
		final TableColumn columnPath = new TableColumn(estimationTable, SWT.NONE);
		
		final TableColumn columnBtn = new TableColumn(estimationTable, SWT.NONE);
		columnBtn.setWidth(100);
		
		String associatedProjectName = null;
		try {
			associatedProjectName = CDebugUtils.getProjectName(launch);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		final IProject[] projects;
		
		// If launch has no associated project use all projects in workspace for the search
		if(associatedProjectName != null){
			projects = new IProject[]{ ResourcesPlugin.getWorkspace().getRoot().getProject(associatedProjectName)};
		}else{
			projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		}
		
		CDebugUtils.FileParts missingFileParts = CDebugUtils.getFileParts(missingFile);
		
		final IFile[] foundFiles = ResourceLookup.findFilesByName(new Path(missingFileParts.getFileName()),projects, true);
		
		if(foundFiles != null){
			
			int minWidth = 0;			
			if(foundFiles.length > 0){
			
			    for (int i = 0; i < foundFiles.length; i++) {
			        new TableItem(estimationTable, SWT.NONE);
			    }
			    
			    TableItem[] items = estimationTable.getItems();
			    for (int i = 0; i < items.length; i++) {
			    	final IFile currentFile = foundFiles[i];
			    	
					TableEditor editor = new TableEditor(estimationTable);
					Label text = new Label(estimationTable, SWT.NONE);
					text.setText(currentFile.getRawLocation().toString());
					editor.grabHorizontal = true;
					editor.setEditor(text, items[i], 0);
					editor = new TableEditor(estimationTable);
					
					Button button = new Button(estimationTable, SWT.PUSH);
					button.setText(SourceLookupUIMessages.CSourceNotFoundEditor_11);
					button.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							locateFileFromResource(currentFile.getRawLocation().toString());
						}
					});
	
					button.pack();
					editor.minimumWidth = button.getSize().x;
					editor.horizontalAlignment = SWT.RIGHT;
					editor.setEditor(button, items[i], 1);
					
					if(currentFile.getRawLocation().toString().length() > minWidth){
						minWidth = currentFile.getRawLocation().toString().length();
					}
			    }
			}else{
				// No file found
				new TableItem(estimationTable,SWT.NONE).setText(SourceLookupUIMessages.CSourceNotFoundEditor_12);
				columnPath.pack();
				columnBtn.pack();
			}
			
			columnPath.setWidth(minWidth*7);
		}
	}

	protected void viewDisassembly() {		
		IWorkbenchPage page = CUIPlugin.getActivePage();
		if (page != null) {		
			try {
				page.showView("org.eclipse.cdt.dsf.debug.ui.disassembly.view"); //$NON-NLS-1$
			} catch (PartInitException e) {
			}
		}
	}

	private void addSourceMappingToDirector(String missingPath, IPath newSourcePath, AbstractSourceLookupDirector director) throws CoreException {
		ArrayList<ISourceContainer> containerList = new ArrayList<ISourceContainer>(Arrays.asList(director.getSourceContainers()));
		MappingSourceContainer foundMappings = null;
		for (ISourceContainer container : containerList) {
			if (container instanceof MappingSourceContainer) {
				if (container.getName().equals(foundMappingsContainerName)) {
					foundMappings = (MappingSourceContainer) container;
					break;
				}
			}
		}

		if (foundMappings == null) {
			foundMappings = new MappingSourceContainer(foundMappingsContainerName);
			foundMappings.init(director);
			containerList.add(foundMappings);
		}
		
		foundMappings.addMapEntry(new MapEntrySourceContainer(missingPath, newSourcePath));
		director.setSourceContainers(containerList.toArray(new ISourceContainer[containerList.size()]));
	}

	/**
	 * Add a path mapping source locator to the global director.
	 * 
	 * @param missingPath
	 *            the compilation source path that was not found on the local
	 *            machine
	 * @param newSourcePath
	 *            the location of the file locally; the user led us to it
	 * @throws CoreException
	 */	
	private void addSourceMappingToCommon(String missingPath, IPath newSourcePath) throws CoreException {
		CSourceLookupDirector director = CDebugCorePlugin.getDefault().getCommonSourceLookupDirector();
		addSourceMappingToDirector(missingPath, newSourcePath, director);
		CDebugCorePlugin.getDefault().savePluginPreferences();
	}
	
	private void addSourceMappingToLaunch(String missingPath, IPath newSourcePath) throws CoreException {
		String memento = null;
		String type = null;

		ILaunchConfigurationWorkingCopy configuration = launch.getWorkingCopy();
		memento = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String) null);
		type = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String) null);
		if (type == null) {
			type = configuration.getType().getSourceLocatorId();
		}
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ISourceLocator locator = launchManager.newSourceLocator(type);
		if (locator instanceof AbstractSourceLookupDirector) {
			AbstractSourceLookupDirector director = (AbstractSourceLookupDirector) locator;
			if (memento == null) {
				director.initializeDefaults(configuration);
			} else {
				director.initializeFromMemento(memento, configuration);
			}

			addSourceMappingToDirector(missingPath, newSourcePath, director);
			configuration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, director.getMemento());
			configuration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, director.getId());
			configuration.doSave();
		}
	}
	
	
	protected void locateFileFromWorkspaceDialog(){
		ElementTreeSelectionDialog edialog = new ElementTreeSelectionDialog(
				getEditorSite().getShell(), new WorkbenchLabelProvider(),
				new WorkbenchContentProvider());
		edialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
		edialog.setAllowMultiple(false);
		if(edialog.open() == IDialogConstants.OK_ID){
			IFile file=(IFile)edialog.getFirstResult();
			if( file != null && file.getLocation() != null ){
				if(file.getLocation() != null){
					locateFileFromResource(file.getLocation().toString());
				}else{
					MessageDialog.openWarning(getSite().getShell(),
							SourceLookupUIMessages.CSourceNotFoundEditor_13,
							SourceLookupUIMessages.CSourceNotFoundEditor_14);
				}
			}	
		}
	}
	
	protected void locateFileFromHardDiskDialog(){
		FileDialog dialog = new FileDialog(getEditorSite().getShell(), SWT.NONE);
		dialog.setFilterNames(new String[] {SourceLookupUIMessages.CSourceNotFoundEditor_2});
		// We cannot use IPaths when manipulating the missingFile (aka compilation file name) otherwise
		// we end up converting windows paths to Linux and/or other canonicalisation of the names
		CDebugUtils.FileParts missingFileParts = CDebugUtils.getFileParts(missingFile);
		dialog.setFilterExtensions(new String[] {"*." + missingFileParts.getExtension()}); //$NON-NLS-1$
		dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
		
		String hardDiskDialogResult = dialog.open();
		if( hardDiskDialogResult !=null ){
			locateFileFromResource(dialog.open());
		}
	}
	
	protected void locateFileFromResource(String res) {
		
		if (res == null){
			throw new NullPointerException(SourceLookupUIMessages.CSourceNotFoundEditor_15);
		}
		
		CDebugUtils.FileParts missingFileParts = CDebugUtils.getFileParts(missingFile);
		CDebugUtils.FileParts resParts = CDebugUtils.getFileParts(res);
		if (resParts.getFileName().toLowerCase().equals(missingFileParts.getFileName().toLowerCase())) {
			String compPath = missingFileParts.getFolder();
			IPath newSourcePath = new Path(resParts.getFolder());
			if (compPath.length() > 0) {
				try {
					if (isDebugElement)
						addSourceMappingToLaunch(compPath, newSourcePath);
					else
						addSourceMappingToCommon(compPath, newSourcePath);
				} catch (CoreException e) {
				}
			}
			
			IWorkbenchPage page = getEditorSite().getPage();
			
			if (isDebugElement) {
				ISourceDisplay adapter = context.getAdapter(ISourceDisplay.class);
				if (adapter != null) {						
					adapter.displaySource(context, page, true);
				}					
			} else if (isTranslationUnit) {
				reopenTranslationUnit(tunit);
			}
			closeEditor();
		}
	}

	private boolean reopenTranslationUnit(ITranslationUnit tu){
		if (tu != null){
			IPath tuPath = tu.getLocation();
			if (tuPath != null){
				String filePath = tuPath.toOSString();
				try {
					Object[] foundElements = CDebugCorePlugin.getDefault().getCommonSourceLookupDirector().findSourceElements(filePath);
					if (foundElements.length == 1 && foundElements[0] instanceof IFile){
						EditorUtility.openInEditor(foundElements[0]);
						return true;						
					} else if (foundElements.length == 1 && foundElements[0] instanceof LocalFileStorage) {
						LocalFileStorage newLocation = (LocalFileStorage) foundElements[0];
						if (newLocation.getFullPath().toFile().exists()) {
							ITranslationUnit remappedTU = tu;
							if (tu instanceof ExternalTranslationUnit)
								// TODO:  source lookup needs to be modified to use URIs
								remappedTU = new ExternalTranslationUnit(tu.getParent(), URIUtil.toURI(newLocation.getFullPath()), tu.getContentTypeId());										
							EditorUtility.openInEditor(remappedTU);
							return true;
						}
					}
				} catch (CoreException e) {
				}
			}
		}
		return false;
	}

	/**
	 * @Override
	 * @see org.eclipse.debug.ui.sourcelookup.CommonSourceNotFoundEditor#getArtifact()
	 */
	@Override
	protected Object getArtifact() {
		Object o = super.getArtifact();
		if (o instanceof CSourceNotFoundElement) {
			return ((CSourceNotFoundElement) o).getElement();
		}
		return o;
	}
}
