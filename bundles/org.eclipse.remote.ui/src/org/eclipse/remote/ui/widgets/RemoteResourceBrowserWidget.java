/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 * Martin Oberhuber - [468889] Support Eclipse older than Mars
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
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
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.remote.core.RemoteServicesUtils;
import org.eclipse.remote.internal.ui.DeferredFileStore;
import org.eclipse.remote.internal.ui.DeferredFileStoreComparer;
import org.eclipse.remote.internal.ui.PendingUpdateAdapter;
import org.eclipse.remote.internal.ui.RemoteContentProvider;
import org.eclipse.remote.internal.ui.RemoteResourceComparator;
import org.eclipse.remote.internal.ui.RemoteTreeViewer;
import org.eclipse.remote.internal.ui.RemoteUIImages;
import org.eclipse.remote.internal.ui.RemoteUIPlugin;
import org.eclipse.remote.internal.ui.messages.Messages;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.progress.UIJob;

/**
 * Generic file/directory browser for remote resources.
 * 
 * @author greg
 * 
 */
public class RemoteResourceBrowserWidget extends Composite {
	/**
	 * Delayed input dialog uses {@link ValidateJob} to create an InputDialog that only validates it's text field after an
	 * appropriate timeout has occurred. This is to prevent excessive network traffic when checking the existence of a remote
	 * directory on a target system.
	 * 
	 * Due to the timing of the validation, it is possible to close the dialog prior to the validation completing. However since the
	 * validation is only used to check for the existence of a remote file/directory, the worst that can happen is that the user
	 * will not be notified that the directory already exists.
	 * 
	 */
	private class DelayedInputDialog extends InputDialog {
		public DelayedInputDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue,
				IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		}

		@Override
		protected void buttonPressed(int buttonId) {
			/*
			 * Cancel the job as soon as the dialog is closed to avoid SWTException
			 */
			fValidateJob.cancel();
			super.buttonPressed(buttonId);
		}

		protected void doValidate() {
			super.validateInput();
		}

		@Override
		protected void validateInput() {
			fValidateJob.cancel();
			if (!getText().getText().equals("")) { //$NON-NLS-1$
				fValidateJob.schedule(VALIDATE_DELAY);
			} else {
				super.validateInput();
			}
		}
	}

	/**
	 * Validation job that will call the {@link DelayedInputDialog#doValidate()} method when run. The job should be scheduled with a
	 * delay to limit the frequency of validation.
	 */
	private class ValidateJob extends UIJob {
		private DelayedInputDialog fDialog;

		public ValidateJob() {
			super(Messages.RemoteResourceBrowserWidget_0);
			setSystem(true);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			fDialog.doValidate();
			return Status.OK_STATUS;
		}

		public void setDialog(DelayedInputDialog dialog) {
			fDialog = dialog;
		}
	}

	private static final int VALIDATE_DELAY = 100;
	private final ValidateJob fValidateJob = new ValidateJob();

	/**
	 * Browse for files
	 */
	public static final int FILE_BROWSER = 0x01;
	/**
	 * Browse for directories (files are not shown)
	 */
	public static final int DIRECTORY_BROWSER = 0x02;
	/**
	 * Show local selection button
	 */
	public static final int SHOW_LOCAL_SELECTION = 0x04;
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

	@SuppressWarnings("unused")
	private static final int minimumWidth = 200;
	private static final int heightHint = 300;

	private RemoteTreeViewer fTreeViewer;
	private Text fRemotePathText;
	private Button fUpButton;
	private Button fNewFolderButton;
	private Button fShowHiddenButton;
	private RemoteConnectionWidget fRemoteConnectionWidget;

	private String fDialogTitle;
	private String fDialogLabel;

	private boolean fShowHidden;
	private final List<IFileStore> fResources = new ArrayList<IFileStore>();
	private String fResource;
	private String fInitialPath;
	private IPath fRootPath;
	private IRemoteFileService fFileMgr;
	private IRemoteConnection fConnection;

	private final ListenerList fSelectionListeners = new ListenerList();

	private int fOptionFlags = FILE_BROWSER | SHOW_HIDDEN_CHECKBOX | SHOW_NEW_FOLDER_BUTTON;

	private IRunnableContext fRunnableContext;

	public RemoteResourceBrowserWidget(Composite parent, int style, int flags) {
		super(parent, style);
		setTitle(Messages.RemoteResourceBrowser_resourceTitle);

		if (flags != 0) {
			fOptionFlags = flags;
		}

		setType();

		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);

		final Composite mainComp = new Composite(this, SWT.NONE);
		mainComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComp.setLayout(new GridLayout(1, false));

		if ((fOptionFlags & SHOW_CONNECTIONS) != 0) {
			fRemoteConnectionWidget = new RemoteConnectionWidget(mainComp, SWT.NONE, "", //$NON-NLS-1$
					(fOptionFlags & SHOW_LOCAL_SELECTION) == 0 ? RemoteConnectionWidget.FLAG_NO_LOCAL_SELECTION : 0);
			fRemoteConnectionWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			// Must happen before adding selection listener as this will trigger selection event
			fRemoteConnectionWidget.filterConnections(IRemoteFileService.class);
			fRemoteConnectionWidget.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					try {
						connectionSelected();
					} catch (CoreException e) {
						RemoteUIPlugin.log(e.getStatus());
					}
					updateEnablement();
					notifySelectionChangedListeners(new SelectionChangedEvent(fTreeViewer, new ISelection() {
						@Override
						public boolean isEmpty() {
							return true;
						}
					}));
				}
			});
		}

		Composite textComp = new Composite(mainComp, SWT.NONE);
		layout = new GridLayout(4, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		textComp.setLayout(layout);
		textComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label label = new Label(textComp, SWT.NONE);
		label.setText(fDialogLabel);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		fRemotePathText = new Text(textComp, SWT.BORDER | SWT.SINGLE);
		fRemotePathText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				fRemotePathText.setSelection(fRemotePathText.getText().length());
				setRoot(fRemotePathText.getText());
			}
		});
		fRemotePathText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				fResource = fRemotePathText.getText().trim();
			}
		});
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		// gd.minimumWidth = minimumWidth;
		fRemotePathText.setLayoutData(gd);

		fUpButton = new Button(textComp, SWT.PUSH | SWT.FLAT);
		fUpButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fUpButton.setImage(RemoteUIImages.get(RemoteUIImages.IMG_ELCL_UP_NAV));
		fUpButton.setToolTipText(Messages.RemoteResourceBrowser_UpOneLevel);
		fUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!fRootPath.isRoot()) {
					setRoot(fRootPath.removeLastSegments(1).toOSString());
				}
			}
		});

		if ((fOptionFlags & SHOW_NEW_FOLDER_BUTTON) != 0) {
			// new folder: See Bug 396334
			fNewFolderButton = new Button(textComp, SWT.PUSH | SWT.FLAT);
			fNewFolderButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			fNewFolderButton.setImage(RemoteUIImages.get(RemoteUIImages.IMG_ELCL_NEW_FOLDER));
			fNewFolderButton.setToolTipText(Messages.RemoteResourceBrowser_NewFolder);
			fNewFolderButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ISelection selection = fTreeViewer.getSelection();
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
										fTreeViewer.expandToLevel(element, 1);
										fTreeViewer.refresh(element);
										Object[] children = element.getChildren(null);
										for (Object child : children) {
											if (child instanceof DeferredFileStore
													&& newPath.equals(((DeferredFileStore) child).getFileStore().getName())) {
												fTreeViewer.deferSelection(new StructuredSelection(child));
											}
										}
									}
								}
							}
						}
					} else {
						DeferredFileStore root = (DeferredFileStore) fTreeViewer.getInput();
						String path = root.getFileStore().toURI().getPath();
						String newPath = createNewFolder(path);
						if (newPath != null) {
							fTreeViewer.refresh();
							fTreeViewer.getTree().setFocus();
							Object[] children = root.getChildren(null);
							for (Object child : children) {
								if (child instanceof DeferredFileStore
										&& newPath.equals(((DeferredFileStore) child).getFileStore().getName())) {
									fTreeViewer.deferSelection(new StructuredSelection(child));
								}
							}
						}
					}
				}
			});
		} else {
			gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			gd.horizontalSpan = 2;
			fUpButton.setLayoutData(gd);
		}

		if ((style & SWT.MULTI) == SWT.MULTI) {
			fTreeViewer = new RemoteTreeViewer(mainComp, SWT.MULTI | SWT.BORDER);
		} else {
			fTreeViewer = new RemoteTreeViewer(mainComp, SWT.SINGLE | SWT.BORDER);
		}
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		// see bug 158380
		gd.heightHint = Math.max(parent.getSize().y, heightHint);
		fTreeViewer.getTree().setLayoutData(gd);
		fTreeViewer.setUseHashlookup(true);
		fTreeViewer.setComparer(new DeferredFileStoreComparer());
		fTreeViewer.setComparator(new RemoteResourceComparator());
		fTreeViewer.setContentProvider(new RemoteContentProvider());
		fTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
		fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
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
						fRemotePathText.setText(fResources.get(0).toURI().getPath());
					}
					updateEnablement();
					notifySelectionChangedListeners(event);
				}
			}
		});
		fTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection s = (IStructuredSelection) event.getSelection();
				Object o = s.getFirstElement();
				if (fTreeViewer.isExpandable(o)) {
					fTreeViewer.setExpandedState(o, !fTreeViewer.getExpandedState(o));
				}
			}

		});
		/*
		 * Only add filter if we are a directory browser. File and resource browsers show everything.
		 */
		if ((fOptionFlags & DIRECTORY_BROWSER) != 0) {
			fTreeViewer.addFilter(new ViewerFilter() {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					if ((element instanceof DeferredFileStore)) {
						return ((DeferredFileStore) element).isContainer();
					}
					return element instanceof PendingUpdateAdapter;
				}
			});
		}

		if ((fOptionFlags & SHOW_HIDDEN_CHECKBOX) != 0) {
			fShowHiddenButton = new Button(mainComp, SWT.CHECK);
			fShowHiddenButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			fShowHiddenButton.setText(Messages.RemoteResourceBrowser_Show_hidden_files);
			fShowHiddenButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					fShowHidden = fShowHiddenButton.getSelection();
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
			setRoot(null);
			return true;
		}
		IRemoteUIConnectionService uiMgr = conn.getConnectionType().getService(IRemoteUIConnectionService.class);
		if (uiMgr != null) {
			uiMgr.openConnectionWithProgress(getShell(), getRunnableContext(), conn);
		}
		if (!conn.isOpen()) {
			return false;
		}

		fFileMgr = conn.getService(IRemoteFileService.class);
		if (fFileMgr != null) {
			/*
			 * Note: the call to findInitialPath must happen before the
			 * fTreeViewer input is set or the fTreeViewer fails. No idea why this
			 * is.
			 */
			IRemoteProcessService processService = conn.getService(IRemoteProcessService.class);
			if (processService != null) {
				String cwd = processService.getWorkingDirectory();
				IPath initial = findInitialPath(cwd, fInitialPath);

				// TODO: not platform independent - needs IRemotePath
				setRoot(initial.toString());
			}

			fConnection = conn;
			return true;
		}

		return false;
	}

	/**
	 * When a new connection is selected, make sure it is open before using it.
	 * 
	 * @throws CoreException
	 */
	private void connectionSelected() throws CoreException {
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
					final IFileStore path = fConnection.getService(IRemoteFileService.class).getResource(parent);
					IFileStore child = path.getChild(baseName);
					int count = 1;
					try {
						while (!progress.isCanceled() && child.fetchInfo(EFS.NONE, progress.newChild(1)).exists()) {
							progress.setWorkRemaining(10);
							child = path.getChild(baseName + " (" + count++ + ")"); //$NON-NLS-1$//$NON-NLS-2$
						}
					} catch (final CoreException e) {
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								ErrorDialog.openError(getShell(), Messages.RemoteResourceBrowserWidget_New_Folder,
										Messages.RemoteResourceBrowserWidget_Unable_to_create_new_folder, e.getStatus());
							}
						});
					}
					final IFileStore basePath = child;
					final String[] userPath = new String[1];
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							DelayedInputDialog dialog = new DelayedInputDialog(getShell(), Messages.RemoteResourceBrowserWidget_1,
									Messages.RemoteResourceBrowserWidget_2, basePath.getName(), new IInputValidator() {
								@Override
								public String isValid(String newText) {
									if (!newText.equals("")) { //$NON-NLS-1$
										IFileStore newPath = path.getChild(newText);
										if (newPath.fetchInfo().exists()) {
											return Messages.RemoteResourceBrowserWidget_3;
										}
									} else {
										return Messages.RemoteResourceBrowserWidget_4;
									}
									return null;
								}
							});
							fValidateJob.setDialog(dialog);
							if (dialog.open() == Dialog.OK) {
								userPath[0] = dialog.getValue();
							}
						}
					});
					if (userPath[0] != null) {
						try {
							IFileStore newPath = path.getChild(userPath[0]);
							if (!progress.isCanceled()) {
								newPath.mkdir(EFS.SHALLOW, progress.newChild(10));
								name[0] = newPath.getName();
							}
						} catch (final CoreException e) {
							Display.getDefault().syncExec(new Runnable() {
								@Override
								public void run() {
									ErrorDialog.openError(getShell(), Messages.RemoteResourceBrowserWidget_New_Folder,
											Messages.RemoteResourceBrowserWidget_Unable_to_create_new_folder, e.getStatus());
								}
							});
						}
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
			IPath path = RemoteServicesUtils.posixPath(initialPath);
			if (!path.isAbsolute()) {
				path = RemoteServicesUtils.posixPath(cwd).append(path);
			}
			if (fFileMgr.getResource(path.toString()).fetchInfo().exists()) {
				return path;
			}
		}
		return RemoteServicesUtils.posixPath(cwd);
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
	 * Get a resource that corresponds to the text field
	 * 
	 * @return resource corresponding to the text field
	 * @since 1.1
	 */
	public IFileStore getResource() {
		if (fResource != null) {
			if (!fResource.equals("") && getConnection() != null) { //$NON-NLS-1$
				return getConnection().getService(IRemoteFileService.class).getResource(fResource);
			}
		}
		return null;
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

	/**
	 * Set the connection for the browser. The connection must support the IRemoteFileService service or this method will have no
	 * effect.
	 * 
	 * @param connection
	 *            connection that supports the IRemoteFileService service
	 */
	public void setConnection(IRemoteConnection connection) {
		if (connection.hasService(IRemoteFileService.class)) {
			changeInput(connection);
			if (fRemoteConnectionWidget != null) {
				fRemoteConnectionWidget.setConnection(connection);
			}
			updateEnablement();
		}
	}

	/**
	 * Set the initial path to start browsing. This will be set in the browser
	 * text field, and in a future version should expand the browser to this
	 * location if it exists.
	 * 
	 * NOTE: This must be called *before* {@link #setConnection(IRemoteConnection)} to have any effect.
	 * 
	 * @param path
	 */
	public void setInitialPath(String path) {
		fInitialPath = path;
		updateEnablement();
	}

	/**
	 * Set the root directory for the browser. This will also update the text
	 * field with the path. If the path is null, the browser will be set to the initial state.
	 * 
	 * @param path
	 *            path of root directory or null
	 */
	private void setRoot(String path) {
		fResources.clear();
		fRootPath = null;
		if (path == null) {
			fTreeViewer.setInput(null);
		} else if (fFileMgr != null) {
			IFileStore root = fFileMgr.getResource(path);
			fTreeViewer.setInput(new DeferredFileStore(root, !fShowHidden));
			fRemotePathText.setText(path);
			fRemotePathText.setSelection(fRemotePathText.getText().length());
			fResources.add(root);
			fRootPath = RemoteServicesUtils.posixPath(path);
		}
	}

	public void setRunnableContext(IRunnableContext context) {
		fRunnableContext = context;
	}

	/**
	 * Set the fDialogTitle of the dialog.
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		fDialogTitle = title;
		if (fDialogTitle == null) {
			fDialogTitle = ""; //$NON-NLS-1$
		}
		Shell shell = getShell();
		if ((shell != null) && !shell.isDisposed()) {
			shell.setText(fDialogTitle);
		}
	}

	/**
	 * Set the type of browser. Can be either a file browser (allows selection
	 * of files only) or a directory browser (allows selection of directories only), or
	 * both files and directories.
	 */
	public void setType() {
		if ((fOptionFlags & DIRECTORY_BROWSER) == 0) {
			fDialogLabel = Messages.RemoteResourceBrowser_fileLabel;
			setTitle(Messages.RemoteResourceBrowser_fileTitle);
		} else if ((fOptionFlags & FILE_BROWSER) == 0) {
			fDialogLabel = Messages.RemoteResourceBrowser_directoryLabel;
			setTitle(Messages.RemoteResourceBrowser_directoryTitle);
		} else {
			fDialogLabel = Messages.RemoteResourceBrowser_resourceLabel;
			setTitle(Messages.RemoteResourceBrowser_resourceTitle);
		}
	}

	private void updateEnablement() {
		boolean upEnabled = false;
		boolean newFolderEnabled = false;
		boolean connectionOpen = fConnection != null && fConnection.isOpen();

		if (connectionOpen) {
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

		if (fUpButton != null) {
			fUpButton.setEnabled(upEnabled);
		}
		if (fNewFolderButton != null) {
			fNewFolderButton.setEnabled(newFolderEnabled);
		}
		if (fRemotePathText != null) {
			fRemotePathText.setEnabled(connectionOpen);
		}
		if (fTreeViewer != null) {
			fTreeViewer.getTree().setEnabled(connectionOpen);
		}
		if (fShowHiddenButton != null) {
			fShowHiddenButton.setEnabled(connectionOpen);
		}
	}
}
