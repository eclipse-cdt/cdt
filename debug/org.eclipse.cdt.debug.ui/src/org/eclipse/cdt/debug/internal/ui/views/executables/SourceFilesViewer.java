/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.executables;

import java.io.File;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.CSourceNotFoundEditorInput;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.cdt.internal.core.util.LRUCache;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * Displays the list of source files for the executable selected in the
 * ExecutablesViewer.
 */
public class SourceFilesViewer extends BaseViewer implements ISourceLookupParticipant, ILaunchConfigurationListener {

	/** Information from an ITranslationUnit for the various displayed columns */
	static class TranslationUnitInfo {
		/** when do we next check these attributes?  time in ms */
		long nextCheckTimestamp;
		/** the source file location */
		IPath location;
		/** does the file exist? */
		boolean exists;
		/** length of actual file in bytes */
		long fileLength;
		/** {@link File#lastModified()} time for source file */
		long lastModified;
		/** the original source file location, e.g. from debug info */
		IPath originalLocation;
		/** does the original file exist? */
		boolean originalExists;
	}

	private static final String P_COLUMN_ORDER_KEY_SF = "columnOrderKeySF"; //$NON-NLS-1$
	private static final String P_SORTED_COLUMN_INDEX_KEY_SF = "sortedColumnIndexKeySF"; //$NON-NLS-1$
	private static final String P_COLUMN_SORT_DIRECTION_KEY_SF = "columnSortDirectionKeySF"; //$NON-NLS-1$
	private static final String P_VISIBLE_COLUMNS_KEY_SF = "visibleColumnsKeySF"; //$NON-NLS-1$


	TreeColumn originalLocationColumn;
	private Tree sourceFilesTree;
	/** Tradeoff expensiveness of checking filesystem against likelihood 
	 * that files will be added/removed/changed in the given time period */
	static final long FILE_CHECK_DELTA = 30 * 1000;
	private static LRUCache<Object, TranslationUnitInfo> translationUnitInfoCache = new LRUCache<Object, TranslationUnitInfo>(1024);

	public SourceFilesViewer(ExecutablesView view, Composite parent, int style) {
		super(view, parent, style);

		setContentProvider(new SourceFilesContentProvider(this));
		setLabelProvider(new SourceFilesLabelProvider(this));
		sourceFilesTree = getTree();
		sourceFilesTree.setHeaderVisible(true);
		sourceFilesTree.setLinesVisible(true);

		createColumns();

		this.addOpenListener(new IOpenListener() {

			public void open(OpenEvent event) {
				openSourceFile(event);
			}
		});

		// We implement ISourceLookupParticipant so we can listen for changes to
		// source lookup as this viewer shows both original and remapped
		// locations
		CDebugCorePlugin.getDefault().getCommonSourceLookupDirector().addParticipants(new ISourceLookupParticipant[] { this });
		
		// We also look for launch configuration changes, since their source
		// locators are involved in source path remapping, too
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(this);
	
		sourceFilesTree.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(SourceFilesViewer.this);
				
				CDebugCorePlugin.getDefault().getCommonSourceLookupDirector().removeParticipants(
						new ISourceLookupParticipant[] { SourceFilesViewer.this });
			}
		});
	}

	private void openSourceFile(OpenEvent event) {
		boolean opened = false;
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object element = selection.getFirstElement();
		if (element instanceof ICElement) {
			if (element instanceof ITranslationUnit) {
				ITranslationUnit tu = (ITranslationUnit) element;
				IPath path = tu.getLocation();
				if (path != null && !path.toFile().exists()) {
					// Open the source not found editor
					IWorkbenchPage p = CUIPlugin.getActivePage();
					if (p != null) {
						try {
							String editorID = ICDebugUIConstants.CSOURCENOTFOUND_EDITOR_ID;
							p.openEditor(new CSourceNotFoundEditorInput(tu), editorID, true);
							opened = true;
						} catch (PartInitException e) {
						}
					}

				}

			}
			if (!opened) {
				try {
					IEditorPart part = EditorUtility.openInEditor(element);
					if (part != null) {
						IWorkbenchPage page = getExecutablesView().getSite().getPage();
						page.bringToTop(part);
						if (element instanceof ISourceReference) {
							EditorUtility.revealInEditor(part, (ICElement) element);
						}
					}
				} catch (Exception e) {
				}
			}
		}
	}

	private void createColumns() {
		nameColumn = new TreeColumn(sourceFilesTree, SWT.NONE);
		nameColumn.setWidth(100);
		nameColumn.setText(Messages.SourceFilesViewer_SourceFileName);
		nameColumn.setMoveable(true);
		nameColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.NAME));

		locationColumn = new TreeColumn(sourceFilesTree, SWT.NONE);
		locationColumn.setWidth(100);
		locationColumn.setText(Messages.SourceFilesViewer_Location);
		locationColumn.setMoveable(true);
		locationColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.LOCATION));

		originalLocationColumn = new TreeColumn(sourceFilesTree, SWT.NONE);
		originalLocationColumn.setWidth(100);
		originalLocationColumn.setText(Messages.SourceFilesViewer_Original);
		originalLocationColumn.setMoveable(true);
		originalLocationColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.ORG_LOCATION));

		sizeColumn = new TreeColumn(sourceFilesTree, SWT.NONE);
		sizeColumn.setWidth(100);
		sizeColumn.setText(Messages.SourceFilesViewer_Size);
		sizeColumn.setMoveable(true);
		sizeColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.SIZE));

		modifiedColumn = new TreeColumn(sourceFilesTree, SWT.NONE);
		modifiedColumn.setWidth(100);
		modifiedColumn.setText(Messages.SourceFilesViewer_Modified);
		modifiedColumn.setMoveable(true);
		modifiedColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.MODIFIED));

		typeColumn = new TreeColumn(sourceFilesTree, SWT.NONE);
		typeColumn.setWidth(100);
		typeColumn.setText(Messages.SourceFilesViewer_Type);
		typeColumn.setMoveable(true);
		typeColumn.addSelectionListener(new ColumnSelectionAdapter(ExecutablesView.TYPE));
	}

	protected ViewerComparator getViewerComparator(int sortType) {
		if (sortType == ExecutablesView.ORG_LOCATION) {
			return new ExecutablesViewerComparator(sortType, column_sort_order[ExecutablesView.ORG_LOCATION]) {

				@SuppressWarnings("unchecked")
				public int compare(Viewer viewer, Object e1, Object e2) {
					if (e1 instanceof ITranslationUnit && e2 instanceof ITranslationUnit) {
						ITranslationUnit entry1 = (ITranslationUnit) e1;
						ITranslationUnit entry2 = (ITranslationUnit) e2;
						Executable exe = (Executable) getInput();
						String originalLocation1 = exe.getOriginalLocation(entry1);
						String originalLocation2 = exe.getOriginalLocation(entry2);
						return getComparator().compare(originalLocation1, originalLocation2) * column_sort_order[ExecutablesView.ORG_LOCATION];
					}
					return super.compare(viewer, e1, e2);
				}
			};
		} else
			return new ExecutablesViewerComparator(sortType, column_sort_order[sortType]);
	}

	public void dispose() {
	}

	public Object[] findSourceElements(Object object) throws CoreException {
		return new Object[0];
	}

	public String getSourceName(Object object) throws CoreException {
		return ""; //$NON-NLS-1$
	}

	public void init(ISourceLookupDirector director) {
	}

	public void sourceContainersChanged(ISourceLookupDirector director) {
		refreshContent();
	}

	private void refreshContent() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Object input = getInput();
				if (input != null && input instanceof Executable) {
					((Executable)input).setRemapSourceFiles(true);
					
					// TODO: be more selective; we don't know what TUs go with which executables yet
					flushTranslationUnitCache();
			
					refresh(true);
				}
			}
		});
	}

	@Override
	protected String getColumnOrderKey() {
		return P_COLUMN_ORDER_KEY_SF;
	}

	@Override
	protected String getSortedColumnIndexKey() {
		return P_SORTED_COLUMN_INDEX_KEY_SF;
	}

	@Override
	protected String getSortedColumnDirectionKey() {
		return P_COLUMN_SORT_DIRECTION_KEY_SF;
	}

	@Override
	protected String getVisibleColumnsKey() {
		return P_VISIBLE_COLUMNS_KEY_SF;
	}

	@Override
	protected String getDefaultVisibleColumnsValue() {
		// default visible columns
		return "1,1,0,0,0,0"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		if (!configuration.isWorkingCopy()) {
			refreshContent();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		if (!configuration.isWorkingCopy()) {
			refreshContent();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		if (!configuration.isWorkingCopy()) {
			refreshContent();
		}
	}


	static TranslationUnitInfo fetchTranslationUnitInfo(Executable executable, Object element) {
		if (!(element instanceof ITranslationUnit)) {
			return null;
		}
		
		ITranslationUnit tu = (ITranslationUnit) element;
		long now = System.currentTimeMillis();
		TranslationUnitInfo info;
		
		synchronized (translationUnitInfoCache) {
			info = (TranslationUnitInfo) translationUnitInfoCache.get(element);
		}
		if (info == null || info.nextCheckTimestamp <= now) {
			if (info == null)
				info = new TranslationUnitInfo();
			
			info.location = tu.getLocation();
			if (info.location != null) {
				File file = info.location.toFile();
				info.exists = file.exists();
				info.fileLength = file.length();
				info.lastModified = file.lastModified();
				
				info.originalLocation = new Path(executable.getOriginalLocation(tu));
				info.originalExists = info.originalLocation.toFile().exists();
			} else {
				info.exists = false;
				info.fileLength = 0;
				info.lastModified = 0;
				info.originalExists = false;
				info.originalLocation = null;
			}
			
			info.nextCheckTimestamp = System.currentTimeMillis() + FILE_CHECK_DELTA;
			
			synchronized (translationUnitInfoCache) {
				translationUnitInfoCache.put(element, info);
			}
		}
		return info;
	}

	/**
	 * 
	 */
	static void flushTranslationUnitCache() {
		synchronized (translationUnitInfoCache) {
			translationUnitInfoCache.flush();
		}
		
	}

}