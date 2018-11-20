package org.eclipse.cdt.internal.docker.launcher.ui.preferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.cdt.internal.docker.launcher.Messages;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DockerHeaderPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, Listener {

	// SWT Widgets and content providers
	private Table hdrTable;
	private TableViewer hdrTableViewer;
	private HeaderContentProvider provider;
	private Button removeButton;
	private List<IPath> directories;

	private final class HeaderContentProvider implements IStructuredContentProvider, ITableLabelProvider {

		/**
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
		 */
		@Override
		public Object[] getElements(Object inputElement) {
			return directories.toArray();
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		@Override
		public void dispose() {
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer,
		 *      Object, Object)
		 */
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(Object,
		 *      int)
		 */
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		private String readNameFile(IPath path) {
			// try and read real name from special .name file found in
			// directory.
			IPath namePath = path.append(".name"); //$NON-NLS-1$
			// default to use last directory segment if any problems occur.
			String name = path.lastSegment();
			if (namePath.toFile().exists()) {
				try (FileReader reader = new FileReader(namePath.toFile());
						BufferedReader bufferReader = new BufferedReader(reader);) {
					name = bufferReader.readLine();
				} catch (IOException e) {
					// ignore
				}
			}
			return name;
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(Object,
		 *      int)
		 */
		@Override
		public String getColumnText(Object element, int columnIndex) {
			IPath path = (IPath) element;
			if (columnIndex == 0) {
				IPath connectionPath = path.removeLastSegments(1);
				String connectionName = readNameFile(connectionPath);
				return connectionName;
			}
			String imageName = readNameFile(path);
			return imageName;
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(ILabelProviderListener)
		 */
		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(Object,
		 *      String)
		 */
		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(ILabelProviderListener)
		 */
		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

	}

	public DockerHeaderPreferencePage() {
		noDefaultAndApplyButton();
		provider = new HeaderContentProvider();
	}

	@Override
	public void init(IWorkbench workbench) {
		directories = new ArrayList<>();
		IPath pluginPath = Platform.getStateLocation(Platform.getBundle(DockerLaunchUIPlugin.PLUGIN_ID))
				.append("HEADERS"); //$NON-NLS-1$
		File d = pluginPath.toFile();

		if (d.exists() && d.isDirectory()) {
			File[] connections = d.listFiles();
			for (File connection : connections) {
				if (connection.isDirectory()) {
					File[] images = connection.listFiles();
					for (File image : images) {
						if (image.isDirectory()) {
							directories.add(pluginPath.append(connection.getName()).append(image.getName()));
						}
					}
				}
			}
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite page = createComposite(parent, 1, 2, false, null, -1, -1, GridData.FILL);
		GridData gd = (GridData) page.getLayoutData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;

		// SystemWidgetHelpers.createLabel(page,
		// SystemResources.RESID_PREF_SIGNON_DESCRIPTION, 2);

		// Header table
		hdrTable = new Table(page, SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		hdrTable.setLinesVisible(true);
		hdrTable.setHeaderVisible(true);
		hdrTable.addListener(SWT.Selection, this);

		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(60, true));
		tableLayout.addColumnData(new ColumnWeightData(40, true));
		hdrTable.setLayout(tableLayout);

		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;

		hdrTable.setLayoutData(gd);

		// Connection column
		TableColumn connectionColumn = new TableColumn(hdrTable, SWT.NONE);
		connectionColumn.setText(Messages.HeaderPreferencePage_Connection_Label);

		// Image column
		TableColumn imageColumn = new TableColumn(hdrTable, SWT.NONE);
		imageColumn.setText(Messages.HeaderPreferencePage_Image_Label);

		hdrTableViewer = new TableViewer(hdrTable);
		hdrTableViewer.setContentProvider(provider);
		hdrTableViewer.setLabelProvider(provider);
		hdrTableViewer.setInput(directories);

		// Create the Button bar for add, change and remove
		Composite buttonBar = createComposite(page, 1, 1, false, null, -1, -1, GridData.FILL);
		gd = (GridData) buttonBar.getLayoutData();
		gd.grabExcessHorizontalSpace = false;
		gd.grabExcessVerticalSpace = true;

		removeButton = createPushButton(buttonBar, this, Messages.HeaderPreferencePage_Remove_Label,
				Messages.HeaderPreferencePage_Remove_Tooltip);

		removeButton.setEnabled(false);
		return parent;
	}

	private static Composite createComposite(Composite parent, int parentSpan, int numColumns, boolean border,
			String label, int marginSize, int spacingSize, int verticalAlignment) {
		// border = true;
		boolean borderNeeded = border;
		if (label != null)
			borderNeeded = true; // force the case
		int style = SWT.NULL;
		if (borderNeeded)
			style |= SWT.SHADOW_ETCHED_IN;
		Composite composite = null;
		if (borderNeeded) {
			composite = new Group(parent, style);
			if (label != null)
				((Group) composite).setText(label);
		} else {
			composite = new Composite(parent, style);
		}
		// GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		if (marginSize != -1) {
			layout.marginWidth = 0;
			layout.marginHeight = 0;
		}
		if (spacingSize != -1) {
			layout.horizontalSpacing = 0;
			layout.verticalSpacing = 0;
		}
		composite.setLayout(layout);
		// GridData
		GridData data = new GridData();
		data.horizontalSpan = parentSpan;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;

		data.verticalAlignment = verticalAlignment;
		data.grabExcessVerticalSpace = false;

		composite.setLayoutData(data);
		return composite;
	}

	public static Button createPushButton(Composite group, Listener listener, String label, String tooltip) {
		Button button = new Button(group, SWT.PUSH);
		button.setText(label);
		if (listener != null)
			button.addListener(SWT.Selection, listener);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		button.setLayoutData(data);
		if (tooltip != null)
			button.setToolTipText(tooltip);
		return button;
	}

	private class DialogStatus {
		private boolean status;

		public DialogStatus(boolean status) {
			this.status = status;
		}

		public void setStatus(boolean status) {
			this.status = status;
		}

		public boolean getStatus() {
			return status;
		}
	}

	/**
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(Event)
	 */
	@Override
	public void handleEvent(Event event) {
		if (event.type == SWT.Selection) {
			if (event.widget == removeButton) {
				final DialogStatus confirmed = new DialogStatus(false);
				Display.getDefault().syncExec(() -> {
					boolean status = MessageDialog.openConfirm(getShell(),
							Messages.HeaderPreferencePage_Confirm_Removal_Title,
							Messages.HeaderPreferencePage_Confirm_Removal_Msg);
					confirmed.setStatus(status);
				});
				if (!confirmed.getStatus()) {
					return;
				}
				int[] indicies = hdrTable.getSelectionIndices();
				for (int idx = indicies.length - 1; idx >= 0; idx--) {
					IPath dirPath = directories.get(idx);
					File f = dirPath.toFile();
					if (f.exists() && f.isDirectory()) {
						recursiveDelete(f);
					}
					directories.remove(idx);
				}

				hdrTableViewer.refresh();
			}

			// Update table buttons based on changes
			if (hdrTable.getSelectionCount() > 0) {
				removeButton.setEnabled(true);
			} else {
				removeButton.setEnabled(false);
			}
		}
	}

	private void recursiveDelete(File dir) {
		File[] contents = dir.listFiles();
		if (contents != null) {
			for (File f : contents) {
				recursiveDelete(f);
			}
		}
		dir.delete();
	}
}
