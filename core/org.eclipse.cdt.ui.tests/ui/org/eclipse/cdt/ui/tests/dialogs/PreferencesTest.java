/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.cdt.ui.tests.dialogs;

import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.internal.corext.template.c.ICompilationUnit;
import org.eclipse.cdt.testplugin.TestPluginLauncher;
import org.eclipse.cdt.testplugin.util.DialogCheck;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;
import org.eclipse.ui.model.IWorkbenchAdapter;


public class PreferencesTest extends TestCase {

	public static void main(String[] args) {
		TestPluginLauncher.run(TestPluginLauncher.getLocationFromProperties(), PreferencesTest.class, args);
	}
	
	public static Test suite() {
		TestSuite suite= new TestSuite(PreferencesTest.class.getName());
		suite.addTest(new PreferencesTest("testCBasePrefPage"));
		suite.addTest(new PreferencesTest("testTemplatePrefPage"));
		suite.addTest(new PreferencesTest("testProjectPropertyPrefPage"));
		suite.addTest(new PreferencesTest("testCEditorPrefPage"));
		return suite;
	}	
	
	private static class PreferenceDialogWrapper extends PreferenceDialog {
		
		public PreferenceDialogWrapper(Shell parentShell, PreferenceManager manager) {
			super(parentShell, manager);
		}
		protected boolean showPage(IPreferenceNode node) {
			return super.showPage(node);
		}
	}
	
	private class PropertyDialogWrapper extends PropertyDialog {
		
		public PropertyDialogWrapper(Shell parentShell, PreferenceManager manager, ISelection selection) {
			super(parentShell, manager, selection);
		}
		protected boolean showPage(IPreferenceNode node) {
			return super.showPage(node);
		}
	}		
	
	
	private boolean fIsInteractive= true;
	
	private static final String PROJECT_NAME = "DummyProject";
	
	public PreferencesTest(String name) {
		super(name);
	}

	private Shell getShell() {
		return DialogCheck.getShell();
	}
	
	public void assertDialog(Dialog dialog, Assert assertTrue) {
		if (fIsInteractive) {
			DialogCheck.assertDialog(dialog, this);
		} else {
			DialogCheck.assertDialogTexts(dialog, this);
		}
	}
	
	
	private PreferenceDialog getPreferenceDialog(String id) {
		PreferenceDialogWrapper dialog = null;
		PreferenceManager manager = WorkbenchPlugin.getDefault().getPreferenceManager();
		if (manager != null) {
			dialog = new PreferenceDialogWrapper(getShell(), manager);
			dialog.create();	
			WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.PREFERENCE_DIALOG);

			for (Iterator iterator = manager.getElements(PreferenceManager.PRE_ORDER).iterator();
			     iterator.hasNext();)
			{
				IPreferenceNode node = (IPreferenceNode)iterator.next();
				if ( node.getId().equals(id) ) {
					dialog.showPage(node);
					break;
				}
			}
		}
		return dialog;
	}
	
	private PropertyDialog getPropertyDialog(String id, IAdaptable element) {
		PropertyDialogWrapper dialog = null;

		PropertyPageManager manager = new PropertyPageManager();
		String title = "";
		String name  = "";

		// load pages for the selection
		// fill the manager with contributions from the matching contributors
		PropertyPageContributorManager.getManager().contribute(manager, element);
		
		IWorkbenchAdapter adapter = (IWorkbenchAdapter)element.getAdapter(IWorkbenchAdapter.class);
		if (adapter != null) {
			name = adapter.getLabel(element);
		}
		
		// testing if there are pages in the manager
		Iterator pages = manager.getElements(PreferenceManager.PRE_ORDER).iterator();		
		if (!pages.hasNext()) {
			return null;
		} else {
			title = WorkbenchMessages.format("PropertyDialog.propertyMessage", new Object[] {name});
			dialog = new PropertyDialogWrapper(getShell(), manager, new StructuredSelection(element)); 
			dialog.create();
			dialog.getShell().setText(title);
			WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.PROPERTY_DIALOG);
			for (Iterator iterator = manager.getElements(PreferenceManager.PRE_ORDER).iterator();
			     iterator.hasNext();)
			{
				IPreferenceNode node = (IPreferenceNode)iterator.next();
				if ( node.getId().equals(id) ) {
					dialog.showPage(node);
					break;
				}
			}
		}
		return dialog;
	}
	
	public void testCBasePrefPage() {
		Dialog dialog = getPreferenceDialog("org.eclipse.cdt.ui.preferences.CPluginPreferencePage");
		assertDialog(dialog, this);
	}
	
	public void testProjectPropertyPrefPage() {
		Dialog dialog = getPreferenceDialog("org.eclipse.cdt.ui.preferences.CProjectPropertyPage");
		assertDialog(dialog, this);
	}
	
	public void testTemplatePrefPage() {
		Dialog dialog = getPreferenceDialog("org.eclipse.cdt.ui.preferences.TemplatePreferencePage");
		assertDialog(dialog, this);
	}
	
	
	public void testCEditorPrefPage() {
		Dialog dialog = getPreferenceDialog("org.eclipse.cdt.ui.preferences.CEditorPreferencePage");
		assertDialog(dialog, this);
	}
	
	
	/* public void testInfoPropPage() throws Exception {
		IJavaProject jproject= JavaProjectHelper.createJavaProject(PROJECT_NAME, "bin");
		IPackageFragmentRoot root= JavaProjectHelper.addSourceContainer(jproject, "src");
		IPackageFragment pack= root.createPackageFragment("org.eclipse.jdt.internal.ui.wizards.dummy", true, null);
		ICompilationUnit cu= pack.getCompilationUnit("DummyCompilationUnitWizard.java");
		IType type= cu.createType("public class DummyCompilationUnitWizard {\n\n}\n", null, true, null);	
		
		Dialog dialog = getPropertyDialog("org.eclipse.jdt.ui.propertyPages.InfoPage", cu);
		assertDialog(dialog, this);
		
		JavaProjectHelper.delete(jproject);
	} */
	
	

	
}

