/*******************************************************************************
 * Copyright (c) 2008,2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.ui.widgets;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.internal.ui.DeferredFileStore;
import org.eclipse.remote.internal.ui.DeferredFileStoreComparer;
import org.eclipse.remote.internal.ui.PendingUpdateAdapter;
import org.eclipse.remote.internal.ui.RemoteContentProvider;
import org.eclipse.remote.internal.ui.RemoteResourceComparator;
import org.eclipse.remote.internal.ui.RemoteTreeViewer;
import org.eclipse.remote.internal.ui.RemoteUIImages;
import org.eclipse.remote.internal.ui.messages.Messages;
import org.eclipse.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.remote.ui.RemoteUIServices;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Generic file/directory browser for remote resources.
 * 
 * @author greg
 * 
 */
public class RemoteResourceBrowserWidget extends Composite {
	/**
	 * Browse for files
	 */
	public static final int FILE_BROWSER = 0x01;
	/**
	 * Browse for directories (files are not shown)
	 */
	public static final int DIRECTORY_BROWSER = 0x02;
	/**
	 * Display checkbox to show/hide hidden files
	 */
	public static final int SHOW_HIDDEN_CHECKBOX = 0x10;
	/**
	 * Display button to create new folders
	 */
	public static final int SHOW_NEW_FOLDER_BUTTON = 0x20;
	/**
	 * Display widget to select a connection
	 */
	public static final int SHOW_CONNECTIONS = 0x40;

	private static final int minimumWidth = 200;
	private static final int heightHint = 300;

	private RemoteTreeViewer treeViewer;
	private Text remotePathText;
	private Button upButton;
	private Button newFolderButton;
	private RemoteConnectionWidget fRemoteConnectionWidget;

	private String dialogTitle;
	private String dialogLabel;

	private boolean showHidden;
	private final List<IFileStore> fResources = new ArrayList<IFileStore>();
	private String fInitialPath;
	private IPath fRootPath;
	private IRemoteFileManager fFileMgr;
	private IRemoteConnection fConnection;

	private final ListenerList fSelectionListeners = new ListenerList();

	private int optionFlags = FILE_BROWSER | SHOW_HIDDEN_CHECKBOX | SHOW_NEW_FOLDER_BUTTON;

	private IRunnableContext fRunnableContext;

	public RemoteResourceBrowserWidget(Composite parent, int style, int flags) {
		super(parent, style);
		setTitle(Messages.RemoteResourceBrowser_resourceTitle);

		if (flags != 0) {
			optionFlags = flags;
		}

		setType();

		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);

		final Composite mainComp = new Composite(this, SWT.NONE);
		mainComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		layout = new GridLayout();
		layout.numColumns = 4;
		mainComp.setLayout(layout);

		if ((optionFlags & SHOW_CONNECTIONS) != 0) {
			fRemoteConnectionWidget = new RemoteConnectionWidget(mainComp, SWT.NONE, null,
					RemoteConnectionWidget.FLAG_NO_LOCAL_SELECTION);
			fRemoteConnectionWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
			fRemoteConnectionWidget.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					connectionSelected();
					updateEnablement();
					notifySelectionChangedListeners(new SelectionChangedEvent(treeViewer, new ISelection() {
						@Override
						public boolean isEmpty() {
							return true;
						}
					}));
				}
			});
		}

		Label label = new Label(mainComp, SWT.NONE);
		label.setText(dialogLabel);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		remotePathText = new Text(mainComp, SWT.BORDER | SWT.SINGLE);
		remotePathText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				remotePathText.setSelection(remotePathText.getText().length());
				setRoot(remotePathText.getText());
			}
		});
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.minimumWidth = minimumWidth;
		remotePathText.setLayoutData(gd);

		upButton = new Button(mainComp, SWT.PUSH | SWT.FLAT);
		upButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		upButton.setImage(RemoteUIImages.get(RemoteUIImages.IMG_ELCL_UP_NAV));
		upButton.setToolTipText(Messages.RemoteResourceBrowser_UpOneLevel);
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!fRootPath.isRoot()) {
					setRoot(fRootPath.removeLastSegments(1).toOSString());
				}
			}
		});

		if ((optionFlags & SHOW_NEW_FOLDER_BUTTON) != 0) {
			// new folder: See Bug 396334
			newFolderButton = new Button(mainComp, SWT.PUSH | SWT.FLAT);
			newFolderButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			newFolderButton.setImage(RemoteUIImages.get(RemoteUIImages.IMG_ELCL_NEW_FOLDER));
			newFolderButton.setToolTipText(Messages.RemoteResourceBrowser_NewFolder);
			newFolderButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ISelection selection = treeViewer.getSelection();
					if (!selection.isEmpty()) {
						if (selection instanceof TreeSelection) {
							TreePath[] treePaths = ((TreeSelection) selection).getPaths();
							/*
							 * There should only be one path
							 */
							if (treePaths.length > 0) {
								TreePath treePath = treePaths[0];
								if (treePath.getLastSegment() instanceof DeferredFileStore) {
									DeferredFileStore element = ((DeferredFileStore) treePath.getLastSegment());
									String path = element.getFileStore().toURI().getPath();
									String newPath = createNewFolder(path);
									if (newPath != null) {
										treeViewer.expandToLevel(element, 1);
										treeViewer.refresh(element);
										Object[] children = element.getChildren(null);
										for (Object child : children) {
											if (child instanceof DeferredFileStore
													&& newPath.equals(((DeferredFileStore) child).getFileStore().getName())) {
												treeViewer.deferSelection(new StructuredSelection(child));
											}
										}
									}
								}
							}
						}
					} else {
						DeferredFileStore root = (DeferredFileStore) treeViewer.getInput();
						String path = root.getFileStore().toURI().getPath();
						String newPath = createNewFolder(path);
						if (newPath != null) {
							treeViewer.refresh();
							treeViewer.getTree().setFocus();
							Object[] children = root.getChildren(null);
							for (Object child : children) {
								if (child instanceof DeferredFileStore
										&& newPath.equals(((DeferredFileStore) child).getFileStore().getName())) {
									treeViewer.deferSelection(new StructuredSelection(child));
								}
							}
						}
					}
				}
			});
		} else {
			gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			gd.horizontalSpan = 2;
			upButton.setLayoutData(gd);
		}

		if ((style & SWT.MULTI) == SWT.MULTI) {
			treeViewer = new RemoteTreeViewer(mainComp, SWT.MULTI | SWT.BORDER);
		} else {
			treeViewer = new RemoteTreeViewer(mainComp, SWT.SINGLE | SWT.BORDER);
		}
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 4;
		// see bug 158380
		gd.heightHint = Math.max(parent.getSize().y, heightHint);
		treeViewer.getTree().setLayoutData(gd);
		treeViewer.setUseHashlookup(true);
		treeViewer.setComparer(new DeferredFileStoreComparer());
		treeViewer.setComparator(new RemoteResourceComparator());
		treeViewer.setContentProvider(new RemoteContentProvider());
		treeViewer.setLabelProvider(new WorkbenchLabelProvider());
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection) selection;
					fResources.clear();
					for (Object currentSelection : ss.toArray()) {
						if (currentSelection instanceof DeferredFileStore) {
							IFileStore store = ((DeferredFileStore) currentSelection).getFileStore();
							fResources.add(store);
						}
					}
					if (fResources.size() > 0) {
						remotePathText.setText(fResources.get(0).toURI().getPath());
					}
					updateEnablement();
					notifySelectionChangedListeners(event);
				}
			}
		});
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection s = (IStructuredSelection) event.getSelection();
				Object o = s.getFirstElement();
				if (treeViewer.isExpandable(o)) {
					treeViewer.setExpandedState(o, !treeViewer.getExpandedState(o));
				}
			}

		});
		if ((optionFlags & DIRECTORY_BROWSER) != 0) {
			treeViewer.addFilter(new ViewerFilter() {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					if ((element instanceof DeferredFileStore)) {
						return ((DeferredFileStore) element).isContainer();
					}
					return element instanceof PendingUpdateAdapter;
				}
			});
		}

		if ((optionFlags & SHOW_HIDDEN_CHECKBOX) != 0) {
			final Button showHiddenButton = new Button(mainComp, SWT.CHECK);
			showHiddenButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			showHiddenButton.setText(Messages.RemoteResourceBrowser_Show_hidden_files);
			showHiddenButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					showHidden = showHiddenButton.getSelection();
					setRoot(fRootPath.toString());
				}
			});
		}

		updateEnablement();
	}

	/**
	 * Add a listener that will be notified when the selection is changed.
	 * 
	 * @param listener
	 *            listener to add
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.add(listener);
	}

	/**
	 * Change the viewers input. Called when a new connection is selected.
	 * 
	 * @param conn
	 *            new connection
	 * @return true if input successfully changed
	 */
	private boolean changeInput(final IRemoteConnection conn) {
		if (conn == null) {
			return false;
		}
		IRemoteUIConnectionManager uiMgr = RemoteUIServices.getRemoteUIServices(conn.getRemoteServices()).getUIConnectionManager();
		if (uiMgr != null) {
			uiMgr.openConnectionWithProgress(getShell(), getRunnableContext(), conn);
		}
		if (!conn.isOpen()) {
			return false;
		}

		fFileMgr = conn.getFileManager();
		if (fFileMgr != null) {
			/*
			 * Note: the call to findInitialPath must happen before the
			 * treeViewer input is set or the treeViewer fails. No idea why this
			 * is.
			 */
			String cwd = conn.getWorkingDirectory();
			IPath initial = findInitialPath(cwd, fInitialPath);

			// TODO: not platform independent - needs IRemotePath
			setRoot(initial.toString());

			fConnection = conn;
			return true;
		}

		return false;
	}

	/**
	 * When a new connection is selected, make sure it is open before using it.
	 */
	private void connectionSelected() {
		/*
		 * Make sure the connection is open before we try and read from the
		 * connection.
		 */
		final IRemoteConnection conn = fRemoteConnectionWidget.getConnection();
		if (!changeInput(conn)) {
			/*
			 * Reset combo back to the previous selection
			 */
			fRemoteConnectionWidget.setConnection(fConnection);
		}
	}

	/**
	 * @return
	 */
	private String createNewFolder(final String parent) {
		final String[] name = new String[1];
		name[0] = null;
		try {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) {
					SubMonitor progress = SubMonitor.convert(monitor, 10);
					String baseName = "newfolder"; //$NON-NLS-1$
					IFileStore path = fConnection.getFileManager().getResource(parent);
					IFileStore child = path.getChild(baseName);
					int count = 1;
					try {
						while (!progress.isCanceled() && child.fetchInfo(EFS.NONE, progress.newChild(1)).exists()) {
							progress.setWorkRemaining(10);
							child = path.getChild(baseName + " (" + count++ + ")"); //$NON-NLS-1$//$NON-NLS-2$
						}
						if (!progress.isCanceled()) {
							child.mkdir(EFS.SHALLOW, progress.newChild(10));
							name[0] = child.getName();
						}
					} catch (CoreException e) {
						ErrorDialog.openError(getShell(), Messages.RemoteResourceBrowserWidget_New_Folder,
								Messages.RemoteResourceBrowserWidget_Unable_to_create_new_folder, e.getStatus());
					}
				}
			};
			getRunnableContext().run(true, true, runnable);
		} catch (InvocationTargetException e) {
			// Ignore, return null
		} catch (InterruptedException e) {
			// Ignore, return null
		}
		return name[0];
	}

	/**
	 * Determine the initial path for the browser. If the initial path is not
	 * supplied or does not exist on the remote machine, then the initial path
	 * will be the cwd.
	 * 
	 * @param cwd
	 * @param initialPath
	 * @return initial path
	 */
	private IPath findInitialPath(String cwd, String initialPath) {
		if (initialPath != null) {
			IPath path = new Path(initialPath);
			if (!path.isAbsolute()) {
				path = new Path(cwd).append(path);
			}
			if (fFileMgr.getResource(path.toString()).fetchInfo().exists()) {
				return path;
			}
		}
		return new Path(cwd);
	}

	/**
	 * Get the connection that was selected
	 * 
	 * @return selected connection
	 */
	public IRemoteConnection getConnection() {
		return fConnection;
	}

	/**
	 * Get the resources that were selected.
	 * 
	 * @return selected resources
	 */
	public List<IFileStore> getResources() {
		return fResources;
	}

	public IRunnableContext getRunnableContext() {
		if (fRunnableContext == null) {
			return new ProgressMonitorDialog(getShell());
		}
		return fRunnableContext;
	}

	private void notifySelectionChangedListeners(SelectionChangedEvent e) {
		for (Object listener : fSelectionListeners.getListeners()) {
			((ISelectionChangedListener) listener).selectionChanged(e);
		}
	}

	/**
	 * Remove a listener that will be notified when the selection is changed
	 * 
	 * @param listener
	 *            listener to remove
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.remove(listener);
	}

	public void setConnection(IRemoteConnection connection) {
		changeInput(connection);
		updateEnablement();
	}

	/**
	 * Set the initial path to start browsing. This will be set in the browser
	 * text field, and in a future version should expand the browser to this
	 * location if it exists.
	 * 
	 * @param path
	 */
	public void setInitialPath(String path) {
		fInitialPath = path;
		updateEnablement();
	}

	/**
	 * Set the root directory for the browser. This will also update the text
	 * field with the path.
	 * 
	 * @param path
	 *            path of root directory
	 */
	private void setRoot(String path) {
		if (fFileMgr != null) {
			IFileStore root = fFileMgr.getResource(path);
			treeViewer.setInput(new DeferredFileStore(root, !showHidden));
			remotePathText.setText(path);
			remotePathText.setSelection(remotePathText.getText().length());
			fResources.clear();
			fResources.add(root);
			fRootPath = new Path(path);
		}
	}

	public void setRunnableContext(IRunnableContext context) {
		fRunnableContext = context;
	}

	/**
	 * Set the dialogTitle of the dialog.
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		dialogTitle = title;
		if (dialogTitle == null) {
			dialogTitle = ""; //$NON-NLS-1$
		}
		Shell shell = getShell();
		if ((shell != null) && !shell.isDisposed()) {
			shell.setText(dialogTitle);
		}
	}

	/**
	 * Set the type of browser. Can be either a file browser (allows selection
	 * of files only) or a directory browser (allows selection of directories only), or
	 * both files and directories.
	 */
	public void setType() {
		if ((optionFlags & DIRECTORY_BROWSER) == 0) {
			dialogLabel = Messages.RemoteResourceBrowser_fileLabel;
			setTitle(Messages.RemoteResourceBrowser_fileTitle);
		} else if ((optionFlags & FILE_BROWSER) == 0) {
			dialogLabel = Messages.RemoteResourceBrowser_directoryLabel;
			setTitle(Messages.RemoteResourceBrowser_directoryTitle);
		} else {
			dialogLabel = Messages.RemoteResourceBrowser_resourceLabel;
			setTitle(Messages.RemoteResourceBrowser_resourceTitle);

		}
	}

	private void updateEnablement() {
		boolean upEnabled = false;
		boolean newFolderEnabled = false;

		if (fConnection != null && fConnection.isOpen()) {
			if (fResources.size() == 1) {
				IFileStore store = fResources.get(0);
				/*
				 * Assume that we have already called fetchInfo() on the file store, so this should
				 * effectively be a noop.
				 */
				if (store.fetchInfo().isDirectory()) {
					newFolderEnabled = true;
				}
				if (store.getParent() != null) {
					upEnabled = true;
				}
			}
		}

		if (upButton != null) {
			upButton.setEnabled(upEnabled);
		}
		if (newFolderButton != null) {
			newFolderButton.setEnabled(newFolderEnabled);
		}
	}
}
