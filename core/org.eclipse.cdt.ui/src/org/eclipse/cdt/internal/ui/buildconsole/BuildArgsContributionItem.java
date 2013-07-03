/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dmitry Kozlov (CodeSourcery) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.buildconsole;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

/**
 * This is the contribution item that is used to add a help search field to
 * the cool bar.
 */
public class BuildArgsContributionItem extends ControlContribution {
	private static final String ID = "org.eclipse.cdt.ui.buildconsole.buildargs"; //$NON-NLS-1$
	
	private BuildConsolePage fConsolePage;
	private BuildConsoleCCombo buildAgrsText;	
	private BuildArgsHistory fBuildArgsHistory;
	
	/**
	 * Creates the contribution item. 
	 * @param window the window
	 */
	public BuildArgsContributionItem(BuildConsolePage page) { 
		super(ID);
		fConsolePage = page;
		fBuildArgsHistory = new BuildArgsHistory();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ControlContribution#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createControl(Composite parent) {
		Composite composite = ControlFactory.createComposite(parent, 2);
		RowLayout l = new RowLayout();
		l.type = SWT.HORIZONTAL;
		l.marginBottom = 0;
		l.marginTop = 0;
		l.fill = true;
		composite.setLayout(l);
		
		fBuildArgsHistory.loadBuildArgs(fConsolePage.getProject());
		buildAgrsText = new BuildConsoleCCombo(composite,SWT.BORDER);
		buildAgrsText.setWidth(60);
		buildAgrsText.setToolTipText(ConsoleMessages.BuildArgs_BuildArgsTooltip);		
		updateBuildArgs();
		buildAgrsText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == SWT.CR) {					
					fConsolePage.fRunBuildAction.run();
				}
			}
		});
		return composite;
	}

	void updateBuildArgs() {
		// Sometimes this method is called before createControl, check this
		if ( buildAgrsText == null ) return;
		
		String buildArgsItems[] = fBuildArgsHistory.getItems();
		if ( buildArgsItems.length > 0 ) {
			buildAgrsText.setItems(buildArgsItems);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ControlContribution#computeWidth(org.eclipse.swt.widgets.Control)
	 */
	protected int computeWidth(Control control) {
		return control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
	}
	
	public String getArgs() {
		String args = " "; //$NON-NLS-1$
		args += buildAgrsText.getText() + " "; //$NON-NLS-1$
		fBuildArgsHistory.add(buildAgrsText.getText());
		buildAgrsText.setItems(fBuildArgsHistory.getItems());
		return args;
	}
	
	class BuildArgsHistory {
		Set<String> history = new HashSet<String>();
		
		public void loadBuildArgs(IProject project) {			
			try {
				if (project != null) {
					IMakeTarget[] targets;
					targets = MakeCorePlugin.getDefault().getTargetManager().getTargets(project);
					if (targets != null) {
						for (IMakeTarget t : targets) {							
							add(t.getName());
						}
					}
				}
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		}
		
		public String[] getItems() {
			return history.toArray(new String[]{});
		}
		
		public void add(String command) {
			history.add(command);
		}
	}

}
