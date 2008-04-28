/**
 * 
 */
package org.eclipse.cdt.ui.actions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.CDTHelpContextIds;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.newui.UIMessages;

import org.eclipse.cdt.internal.ui.CPluginImages;

/**
 */
public class WorkingSetConfigAction implements IWorkbenchWindowActionDelegate, IPropertyChangeListener {
	private static final String DELIMITER = " "; //$NON-NLS-1$
	private static final String EMPTY_STR = ""; //$NON-NLS-1$
	private static final String CURRENT = UIMessages.getString("WorkingSetConfigAction.10"); //$NON-NLS-1$
	public static final Image IMG_PROJ = CPluginImages.get(CPluginImages.IMG_OBJS_CFOLDER);
	public static final Image IMG_CONF = CPluginImages.get(CPluginImages.IMG_OBJS_CONFIG);
	private static final Shell sh = CUIPlugin.getDefault().getShell();
	private static final IWorkingSetManager wsm = CUIPlugin.getDefault().getWorkbench().getWorkingSetManager();  
	private LinkedHashMap<String, IWorkingSet> workingSetsMap;
	private LinkedHashMap<String, ConfigSet> configSetMap;
	private boolean enabled = true;
	
	public void run(IAction action) {
		LocalDialog dlg = new LocalDialog(sh);		
		dlg.open();
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		if (action.isEnabled() != enabled)
			action.setEnabled(enabled);
	}
	public void dispose() {
		wsm.removePropertyChangeListener(this);
		
	}
	public void init(IWorkbenchWindow window) {
		wsm.addPropertyChangeListener(this);
		checkWS();
	}
	
	private IWorkingSet[] checkWS() {
		IWorkingSet[] w = wsm.getWorkingSets();
		if (w == null)
			w = new IWorkingSet[0]; 
		enabled = w.length > 0;
		return w;
	}
	
	private String[] getWSnames() {
		IWorkingSet[] w = checkWS();
		workingSetsMap = new LinkedHashMap<String, IWorkingSet>(w.length); 
		for (IWorkingSet ws : w)
			workingSetsMap.put(ws.getLabel(), ws);
		return workingSetsMap.keySet().toArray(new String[w.length]);
	}

	public void propertyChange(PropertyChangeEvent event) {
		checkWS();
	}
	
	private class LocalDialog extends TrayDialog {
		private List wsets;
		private List csets;
		private Tree tree;
		private Button b1, b2, b3, b4;
		
		LocalDialog(Shell parentShell) {
			super(parentShell);
			setHelpAvailable(false);
			setShellStyle(getShellStyle()|SWT.RESIZE);
		}
		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == IDialogConstants.OK_ID) {
				saveConfigSets();
				saveActiveConfigs();
			} else {}
			super.buttonPressed(buttonId);
		}

		private void saveConfigSets() {
			ArrayList<String> out = new ArrayList<String>(configSetMap.size());
			for (ConfigSet cs : configSetMap.values()) 
				if (cs.isValid() && !cs.name.equals(CURRENT))
					out.add(cs.toString());
			CDTPrefUtil.saveConfigSets(out);
		}
		
		private void saveActiveConfigs() {
			for (TreeItem ti : tree.getItems()) {
				ICProjectDescription pd = (ICProjectDescription)ti.getData();
				for (TreeItem ti1: ti.getItems()) {
					if (!ti1.getChecked())
						continue;
					((ICConfigurationDescription)ti1.getData()).setActive();
					break;
				}
				try {
					CoreModel.getDefault().setProjectDescription(pd.getProject(), pd);
				} catch (CoreException e) {
					e.printStackTrace();
					CUIPlugin.log(e);
				}
			}
		}
		
		@Override
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			shell.setText(UIMessages.getString("WorkingSetConfigAction.1")); //$NON-NLS-1$
		}
		 
		@Override
		protected Control createDialogArea(Composite parent) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp( getShell(), CDTHelpContextIds.MAN_PROJ_BUILD_PROP);

			Composite comp = new Composite(parent, SWT.NULL);
			comp.setFont(parent.getFont());
			comp.setLayout(new GridLayout(2, false));
			comp.setLayoutData(new GridData(GridData.FILL_BOTH));

			// Create the sash form
			SashForm sashForm = new SashForm(comp, SWT.NONE);
			sashForm.setOrientation(SWT.VERTICAL);
			GridData gd = new GridData(GridData.FILL_VERTICAL);
			gd.verticalSpan = 3;
			sashForm.setLayoutData(gd);

			Composite ws = new Composite(sashForm, SWT.NULL);
			ws.setLayoutData(new GridData(GridData.FILL_BOTH));
			ws.setLayout(new GridLayout(1, false));
			Label l1 = new Label(ws, SWT.NONE);
			l1.setText(UIMessages.getString("WorkingSetConfigAction.2")); //$NON-NLS-1$
			l1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			wsets = new List(ws, SWT.SINGLE | SWT.BORDER);
			wsets.setLayoutData(new GridData(GridData.FILL_BOTH));
			wsets.setItems(getWSnames());

			wsets.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					workingSetChanged();
				}
				public void widgetSelected(SelectionEvent e) {
					workingSetChanged();
				}});

			if (wsets.getItemCount() == 0) {
				wsets.add(UIMessages.getString("WorkingSetConfigAction.3")); //$NON-NLS-1$
				wsets.setEnabled(false);
			} else {
				IWorkingSet[] w = wsm.getRecentWorkingSets();
				if (w == null || w.length == 0)
					wsets.setSelection(0);
				else {
					String s = w[0].getLabel();
					String[] ss = wsets.getItems();
					for (int i=0; i<ss.length; i++) {
						if (ss[i].equals(s)) {
							wsets.setSelection(i);
							break;
						}
					}
				}
			}

			Composite cs = new Composite(sashForm, SWT.NULL);
			cs.setLayoutData(new GridData(GridData.FILL_BOTH));
			cs.setLayout(new GridLayout(1, false));
			l1 = new Label(cs, SWT.NONE);
			l1.setText(UIMessages.getString("WorkingSetConfigAction.4")); //$NON-NLS-1$
			l1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			csets = new List(cs, SWT.SINGLE | SWT.BORDER);
			csets.setLayoutData(new GridData(GridData.FILL_BOTH));
			csets.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					configSetChanged();
				}
				public void widgetSelected(SelectionEvent e) {
					configSetChanged();
				}});

			sashForm.setWeights(new int[]{50, 50});

			
			l1 = new Label(comp, SWT.NONE);
			l1.setText(UIMessages.getString("WorkingSetConfigAction.5")); //$NON-NLS-1$
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.verticalIndent = 5;
			l1.setLayoutData(gd);
			
			tree = new Tree(comp, SWT.SINGLE | SWT.CHECK | SWT.BORDER);
			gd = new GridData(GridData.FILL_BOTH);
			gd.heightHint = 200;
			gd.widthHint  = 200;
			tree.setLayoutData(gd);
			tree.getAccessible().addAccessibleListener(
				new AccessibleAdapter() {                       
					@Override
					public void getName(AccessibleEvent e) {
						e.result = UIMessages.getString("WorkingSetConfigAction.17"); //$NON-NLS-1$
					}
				}
			);
			tree.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (((e.detail & SWT.CHECK) == SWT.CHECK) && 
						 (e.item != null && (e.item instanceof TreeItem))) {
						TreeItem sel = (TreeItem)e.item;
						if (sel.getData() instanceof ICProjectDescription) {
							sel.setChecked(false); // do not check projects !
						} else {
							// If current item is checked, uncheck remaining ones.
							// If current item is unchecked, check the 1st instead.
							String txt = sel.getChecked() ? sel.getText() : EMPTY_STR;  
							for (TreeItem obj : sel.getParentItem().getItems()) {
								if (txt == null)
									obj.setChecked(false);
								else if (txt == EMPTY_STR || txt.equals(obj.getText())) {
									obj.setChecked(true);
									txt = null; // do not perform further checks.
								} else  
									obj.setChecked(false); 
							}
						}
					}
				}
			});

			tree.addTreeListener(new TreeListener() {
				public void treeCollapsed(TreeEvent e) {
				}
				public void treeExpanded(TreeEvent e) {
				}});
			
			// Buttons pane
			Composite c = new Composite(comp, SWT.NONE);
			c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
			c.setLayout(new GridLayout(2, true));

			b2 = new Button(c, SWT.PUSH);
			b2.setText(UIMessages.getString("WorkingSetConfigAction.7")); //$NON-NLS-1$
			b2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
			b2.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String newS = getString(UIMessages.getString("WorkingSetConfigAction.18"), null); //$NON-NLS-1$ 
				    if (newS == null)
				    	return;
				    String key = wsets.getSelection()[0] + DELIMITER + newS;
					if (configSetMap.containsKey(key))
						// error message
						return;
					ConfigSet cs = new ConfigSet(newS, wsets.getSelection()[0], tree);
					configSetMap.put(key, cs);
					csets.add(newS);
					csets.setSelection(csets.getItemCount() - 1);
					updateButtons();
				}});

			b1 = new Button(c, SWT.PUSH);
			b1.setText(UIMessages.getString("WorkingSetConfigAction.6")); //$NON-NLS-1$
			b1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
			b1.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String oldS = csets.getItem(csets.getSelectionIndex());
					ConfigSet cs = new ConfigSet(oldS, wsets.getSelection()[0], tree);
					configSetMap.put(wsets.getSelection()[0] + DELIMITER + oldS, cs); // overwrite
				}});

			b3 = new Button(c, SWT.PUSH);
			b3.setText(UIMessages.getString("WorkingSetConfigAction.8")); //$NON-NLS-1$
			b3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
			b3.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int n = csets.getSelectionIndex();
					if (n > 0) {
						String oldS = csets.getItem(n);
						String newS = getString(UIMessages.getString("WorkingSetConfigAction.19"), oldS);  //$NON-NLS-1$
					    if (newS == null)
					    	return;
						if (oldS.equals(newS)) // nothing to do
							return;
						String key = wsets.getSelection()[0] + DELIMITER + newS;
						if (configSetMap.containsKey(key)) {
							ExistsMessage(newS);
							return;
						}
						ConfigSet cs = configSetMap.get(wsets.getSelection()[0] + DELIMITER + oldS);
						configSetMap.remove(cs);
						cs.name = newS;
						configSetMap.put(key, cs);
						csets.setItem(n, newS);
					}
				}});

			b4 = new Button(c, SWT.PUSH);
			b4.setText(UIMessages.getString("WorkingSetConfigAction.9")); //$NON-NLS-1$
			b4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
			b4.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String[] ss = csets.getSelection();
					if (ss != null && ss.length > 0) {
						configSetMap.remove(wsets.getSelection()[0] + DELIMITER + ss[0]);
						csets.remove(ss[0]);
						csets.setSelection(0);
						updateButtons();
					}
				}});

			initData();
			return comp;
		}

		private void initData() {
			configSetMap = new LinkedHashMap<String, ConfigSet>();
			for (String s : CDTPrefUtil.readConfigSets()) {
				ConfigSet cs = new ConfigSet(s);
				if (cs.isValid())
					configSetMap.put(cs.workingSetLabel + DELIMITER + cs.name, cs);
			}
			workingSetChanged();
		}
		
		private void updateButtons() {
			Button ok =	this.getButton(IDialogConstants.OK_ID);
			boolean en = csets.getSelectionIndex() > 0;
			if (! wsets.getEnabled()) {
				b2.setEnabled(false); // new
				if (ok != null)
					ok.setEnabled(false); // OK
				en = false;
			}
			b1.setEnabled(en); // update
			b3.setEnabled(en); // rename
			b4.setEnabled(en); // delete
		}
		private void workingSetChanged() {
//			tree.setRedraw(false);
			tree.removeAll();
			String[] ss = wsets.getSelection();
			if (ss == null || ss.length == 0)
				return;
			IWorkingSet ws = workingSetsMap.get(ss[0]);
			fillTree(ws);
			tree.setRedraw(true);
//			csets.setRedraw(false);
			csets.removeAll();
			csets.add(CURRENT);
			csets.setSelection(0);
			
			configSetMap.remove(CURRENT); // previous default object, if any
			for (Map.Entry<String, ConfigSet> me : configSetMap.entrySet()) {
				if (me.getValue().workingSetLabel.equals(ws.getLabel()))
					csets.add(me.getValue().name);
			}
			configSetMap.put(CURRENT, new ConfigSet(CURRENT, ws.getLabel(), tree));
			csets.setRedraw(true);
			updateButtons();
			
			// calls from FillTree does not work...
			for (TreeItem ti : tree.getItems())
				ti.setExpanded(true);
		}
		
		/**
		 * Update projects tree for selected working set.
		 * @param ws - working set selected.
		 */
		private void fillTree(IWorkingSet ws) {
			if (ws == null) 
				return;
			for (IAdaptable ad : ws.getElements()) {
				IProject p = (IProject)ad.getAdapter(IProject.class);
				if (p == null) 
					continue;
				ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(p, true);
				if (prjd == null)
					continue;
				ICConfigurationDescription[] cfgs = prjd.getConfigurations();
				if (cfgs == null || cfgs.length == 0)
					continue;
				TreeItem ti = new TreeItem(tree, SWT.NONE);
				ti.setText(prjd.getName());
				ti.setImage(IMG_PROJ);
				ti.setData(prjd);
				for (ICConfigurationDescription c : cfgs) {
					TreeItem ti1 = new TreeItem(ti, SWT.NONE);
					ti1.setText(c.getName());
					ti1.setImage(IMG_CONF);
					ti1.setChecked(c.isActive());
					ti1.setData(c);
				}
			}
		}
		
		private void configSetChanged() {
			int i = csets.getSelectionIndex();
			if (i >= 0) {
				String key = (i == 0) ? CURRENT : wsets.getSelection()[0] + DELIMITER + csets.getItem(i);
				ConfigSet cs = configSetMap.get(key);
				if (cs != null && cs.isValid()) {
					for (TreeItem ti : tree.getItems()) {
						ICProjectDescription prjd = (ICProjectDescription)ti.getData();
						String cid = cs.data.get(prjd.getName());
						if (cid == null)
							continue; // the project not in the list
						for (TreeItem ti1 : ti.getItems()) {
							ICConfigurationDescription cfg = (ICConfigurationDescription)ti1.getData();
							ti1.setChecked(cid.equals(cfg.getId()));
						}
					}
				}
			}
			updateButtons();
		}

		private String getString(String title, String value) { 
			InputDialog d = new InputDialog(sh, title, 
					UIMessages.getString("WorkingSetConfigAction.11"), //$NON-NLS-1$ 
					value, new IInputValidator() {
						public String isValid(String newText) {
							if (newText.indexOf(CDTPrefUtil.CONFSETDEL) >= 0)
								return UIMessages.getString("WorkingSetConfigAction.0") + CDTPrefUtil.CONFSETDEL + UIMessages.getString("WorkingSetConfigAction.14"); //$NON-NLS-1$ //$NON-NLS-2$
							if (configSetMap.containsKey(wsets.getSelection()[0] + DELIMITER + newText))
								return UIMessages.getString("WorkingSetConfigAction.15") + newText + UIMessages.getString("WorkingSetConfigAction.16"); //$NON-NLS-1$ //$NON-NLS-2$
							return null;
						}}); 
			if (d.open() == Window.OK) 
				return d.getValue().replace(' ', '_'); // space is delimiter.
			return null;
		}
		
		private void ExistsMessage(String s) {
			MessageBox box = new MessageBox(sh, SWT.ICON_ERROR);
			box.setMessage(UIMessages.getString("WorkingSetConfigAction.12") + s); //$NON-NLS-1$
			box.open();
		}
	}
	
	private static class ConfigSet {
		String name;
		String workingSetLabel;
		LinkedHashMap<String, String> data;
		
		private ConfigSet(String s) {
			data = new LinkedHashMap<String, String>();
			String[] ss = s.split(DELIMITER);
			if (ss == null || ss.length < 4 || ss.length %2 == 1) {
				CUIPlugin.getDefault().logErrorMessage(UIMessages.getString("WorkingSetConfigAction.13") + s); //$NON-NLS-1$
				return; // not valid
			}
			name = ss[0];
			workingSetLabel = ss[1];
			int n = (ss.length - 2) / 2;
			for (int i=0; i<n; i++) 
				data.put(ss[2 + i * 2], ss[3 + i * 2]);
		}
		
		private ConfigSet(String n, String w, Tree t) {
			data = new LinkedHashMap<String, String>();
			name = n;
			workingSetLabel = w;
			for (TreeItem ti : t.getItems()) {
				ICProjectDescription prjd = (ICProjectDescription)ti.getData();
				for (TreeItem ti1 : ti.getItems()) {
					if (ti1.getChecked()) {
						ICConfigurationDescription cfg = (ICConfigurationDescription)ti1.getData();
						data.put(prjd.getName(), cfg.getId());
						break;
					}
				}
			}

		}
		
		private boolean isValid() {
			return data.size() > 0;
		}
		
		@Override
		public String toString() {
			if (!isValid())
				return EMPTY_STR;
			StringBuilder b = new StringBuilder();
			b.append(name);
			b.append(DELIMITER);
			b.append(workingSetLabel);
			b.append(DELIMITER);
			for (Map.Entry<String, String> me : data.entrySet()) {
				b.append(me.getKey());
				b.append(DELIMITER);
				b.append(me.getValue());
				b.append(DELIMITER);
			}
			return b.toString().trim();
		}
	}
}
