/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jun 10, 2003
 */
package org.eclipse.cdt.internal.ui.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.search.internal.ui.util.RowLayouter;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchResultViewEntry;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CSearchPage extends DialogPage implements ISearchPage, ICSearchConstants {

	public static final String EXTENSION_POINT_ID= "org.eclipse.cdt.ui.CSearchPage"; //$NON-NLS-1$
	
	public boolean performAction() {
		SearchUI.activateSearchResultView();

		SearchPatternData data = getPatternData();
		IWorkspace workspace = CUIPlugin.getWorkspace();
		
		ICSearchScope scope = null;
		String scopeDescription = ""; //$NON-NLS-1$
		
		switch( getContainer().getSelectedScope() ) {
			case ISearchPageContainer.WORKSPACE_SCOPE:
				scopeDescription = CSearchMessages.getString("WorkspaceScope"); //$NON-NLS-1$
				scope = SearchEngine.createWorkspaceScope();
				break;
			case ISearchPageContainer.SELECTION_SCOPE:
				scopeDescription = CSearchMessages.getString("SelectionScope"); //$NON-NLS-1$
				scope = CSearchScopeFactory.getInstance().createCSearchScope(fStructuredSelection);
				break;
			case ISearchPageContainer.WORKING_SET_SCOPE:
				IWorkingSet[] workingSets= getContainer().getSelectedWorkingSets();
				// should not happen - just to be sure
				if (workingSets == null || workingSets.length < 1)
					return false;
				scopeDescription = CSearchMessages.getFormattedString("WorkingSetScope", CSearchUtil.toString(workingSets)); //$NON-NLS-1$
				scope= CSearchScopeFactory.getInstance().createCSearchScope(getContainer().getSelectedWorkingSets());
				CSearchUtil.updateLRUWorkingSets(getContainer().getSelectedWorkingSets());
		}
		
		data.cElement= null;
		
		CSearchResultCollector collector= new CSearchResultCollector();
		
		List searching = null;
		
		if( data.searchFor.contains( UNKNOWN_SEARCH_FOR ) ){
			//UNKNOWN_SEARCH_FOR means search for anything, make a list with everything
			searching = new LinkedList();
			for( int i = 0; i < fSearchFor.length - 1; i++ ){
				searching.add( fSearchForValues[ i ] );		
			}
		} else {
			searching = data.searchFor;
		}
		
		CSearchOperation op = new CSearchOperation(workspace, data.pattern, data.isCaseSensitive, searching, data.limitTo, scope, scopeDescription, collector);

		
		try {
			getContainer().getRunnableContext().run(true, true, op);
		} catch (InvocationTargetException ex) {
			Shell shell = getControl().getShell();
			//ExceptionHandler.handle(ex, shell, CSearchMessages.getString("Search.Error.search.title"), CSearchMessages.getString("Search.Error.search.message")); //$NON-NLS-2$ //$NON-NLS-1$
			return false;
		} catch (InterruptedException ex) {
			return false;
		}
		return true;
	}

	public void createControl(Composite parent) {
		initializeDialogUnits( parent );
		readConfiguration();
		
		GridData gd;
		Composite  result = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout( 2, false );
		layout.horizontalSpacing = 10;
		result.setLayout( layout );
		result.setLayoutData( new GridData(GridData.FILL_HORIZONTAL) );
		
		RowLayouter layouter = new RowLayouter( layout.numColumns );
		gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment   = GridData.VERTICAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_FILL;
	
		layouter.setDefaultGridData( gd, 0 );
		layouter.setDefaultGridData( gd, 1 );
		layouter.setDefaultSpan();

		layouter.perform( createExpression(result) );
		layouter.perform( createSearchFor(result), createLimitTo(result), -1 );
		
		SelectionAdapter cElementInitializer = new SelectionAdapter() {
			public void widgetSelected( SelectionEvent event ) {
				if( getSearchFor() == fInitialData.searchFor )
					fCElement= fInitialData.cElement;
				else
					fCElement= null;
				handleAllElements( event );
				setLimitTo( getSearchFor() );
				updateCaseSensitiveCheckbox();
			}
		};

		for( int i = 0; i < fSearchFor.length; i++ ){
			fSearchFor[ i ].addSelectionListener( cElementInitializer );		
		}

		setControl( result );

		Dialog.applyDialogFont( result );
		//WorkbenchHelp.setHelp(result, IJavaHelpContextIds.JAVA_SEARCH_PAGE);	
	}
	
	private Control createExpression( Composite parent ) {
		Composite  result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		result.setLayout(layout);
		GridData gd = new GridData( GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL );
		gd.horizontalSpan = 2;
		gd.horizontalIndent = 0;
		result.setLayoutData( gd );

		// Pattern text + info
		Label label = new Label( result, SWT.LEFT );
		label.setText( CSearchMessages.getString( "CSearchPage.expression.label" ) ); //$NON-NLS-1$
		gd = new GridData( GridData.BEGINNING );
		gd.horizontalSpan = 2;
		label.setLayoutData( gd );

		// Pattern combo
		fPattern = new Combo( result, SWT.SINGLE | SWT.BORDER );
		fPattern.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected( SelectionEvent e ) {
				handlePatternSelected();
			}
		});
		
		fPattern.addModifyListener( new ModifyListener() {
			public void modifyText( ModifyEvent e ) {
				getContainer().setPerformActionEnabled( getPattern().length() > 0 );
				updateCaseSensitiveCheckbox();
			}
		});
		
		gd = new GridData( GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL );
		gd.horizontalIndent = -gd.horizontalIndent;
		fPattern.setLayoutData( gd );


		// Ignore case checkbox		
		fCaseSensitive= new Button(result, SWT.CHECK);
		fCaseSensitive.setText(CSearchMessages.getString("CSearchPage.expression.caseSensitive")); //$NON-NLS-1$
		gd= new GridData();
		fCaseSensitive.setLayoutData(gd);
		fCaseSensitive.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected( SelectionEvent e ) {
				fIsCaseSensitive= fCaseSensitive.getSelection();
				writeConfiguration();
			}
		});
	
		return result;
	}

	private void handleAllElements( SelectionEvent event ){
		Button allElements = fSearchFor[ fSearchFor.length - 1 ];
		if( event.widget == allElements ){
			for( int i = 0; i < fSearchFor.length - 1; i++ )
				fSearchFor[i].setEnabled( ! allElements.getSelection() );
		}
	}
	
	private void handlePatternSelected() {
		if( fPattern.getSelectionIndex() < 0 )
			return;
			
		int index = fgPreviousSearchPatterns.size() - 1 - fPattern.getSelectionIndex();
		fInitialData = (SearchPatternData) fgPreviousSearchPatterns.get( index );
		
		updateSelections();

		if( fInitialData.workingSets != null )
			getContainer().setSelectedWorkingSets( fInitialData.workingSets );
		else
			getContainer().setSelectedScope( fInitialData.scope );
	}
	
	private String getPattern() {
		return fPattern.getText();
	}
	
	private Control createLimitTo( Composite parent ) {
		Group result = new Group(parent, SWT.NONE);
		result.setText( CSearchMessages.getString("CSearchPage.limitTo.label") ); //$NON-NLS-1$
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		result.setLayout( layout );

		fLimitTo = new Button[fLimitToText.length];
		for( int i = 0; i < fLimitToText.length; i++ ){
			Button button = new Button(result, SWT.RADIO);
			button.setText( fLimitToText[i] );
			fLimitTo[i] = button;
		}

		// Fill with dummy radio buttons
		Button filler= new Button(result, SWT.RADIO);
		filler.setVisible(false);
		filler= new Button(result, SWT.RADIO);
		filler.setVisible(false);		
		
		return result;		
	}
	
	private LimitTo getLimitTo() {
		for (int i= 0; i < fLimitTo.length; i++) {
			if (fLimitTo[i].getSelection())
				return fLimitToValues[ i ];
		}
		return null;
	}
	
	private void setLimitTo( List searchFor ) {
		HashSet set = new HashSet();

		set.add( DEFINITIONS );
		set.add( DECLARATIONS );
		set.add( REFERENCES );
		set.add( ALL_OCCURRENCES );
				
		for (Iterator iter = searchFor.iterator(); iter.hasNext();) {
			SearchFor element = (SearchFor) iter.next();
			if( element != FUNCTION && element != METHOD ){
				set.remove( DEFINITIONS );
				break;
			}
			
		}
		
		for( int i = 0; i < fLimitTo.length; i++ )
			fLimitTo[ i ].setEnabled( set.contains( fLimitToValues[ i ] ) );
	}
	
	private Control createSearchFor(Composite parent) {
		Group result= new Group(parent, SWT.NONE);
		result.setText(CSearchMessages.getString("CSearchPage.searchFor.label")); //$NON-NLS-1$
		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		result.setLayout(layout);

		fSearchFor= new Button[fSearchForText.length];
		for (int i= 0; i < fSearchForText.length; i++) {
			Button button= new Button(result, SWT.CHECK);
			button.setText(fSearchForText[i]);
			fSearchFor[i]= button;
		}

		return result;		
	}
	
	private List getSearchFor() {
		List search = new LinkedList( );
		
//		boolean all = fSearchFor[ fSearchFor.length - 1 ].getSelection();
		
		for (int i= 0; i < fSearchFor.length; i++) {
			if( fSearchFor[i].getSelection() /*|| all */)
				search.add( fSearchForValues[i] );
		}
		
		return search;
	}
	
	public void setContainer(ISearchPageContainer container) {
		fContainer = container;
	}
	
	private ISearchPageContainer getContainer() {
		return fContainer;
	}
	

	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = CUIPlugin.getDefault().getDialogSettings();
		fDialogSettings = settings.getSection( PAGE_NAME );
		if( fDialogSettings == null )
			fDialogSettings = settings.addNewSection( PAGE_NAME );
			
		return fDialogSettings;
	}
		
	private void readConfiguration() {
		IDialogSettings s = getDialogSettings();
		fIsCaseSensitive = s.getBoolean( STORE_CASE_SENSITIVE );
	}

	private void writeConfiguration() {
		IDialogSettings s = getDialogSettings();
		s.put( STORE_CASE_SENSITIVE, fIsCaseSensitive );
	}

	public void setVisible(boolean visible) {
		if (visible && fPattern != null) {
			if (fFirstTime) {
				fFirstTime= false;
				// Set item and text here to prevent page from resizing
				fPattern.setItems(getPreviousSearchPatterns());
				initSelections();
			}
			fPattern.setFocus();
			getContainer().setPerformActionEnabled(fPattern.getText().length() > 0);
		}
		super.setVisible(visible);
	}
	
	private void updateCaseSensitiveCheckbox() {
		if (fInitialData != null && getPattern().equals(fInitialData.pattern) && fCElement != null) {
			fCaseSensitive.setEnabled(false);
			fCaseSensitive.setSelection(true);
		}
		else {
			fCaseSensitive.setEnabled(true);
			fCaseSensitive.setSelection(fIsCaseSensitive);
		}
	}
	
	private void initSelections() {
		fStructuredSelection = asStructuredSelection();
		fInitialData = tryStructuredSelection( fStructuredSelection );
		updateSelections();
	}
	
	private void updateSelections(){
		if (fInitialData == null)
			fInitialData = trySimpleTextSelection( getContainer().getSelection() );
		if (fInitialData == null)
			fInitialData = getDefaultInitValues();

		fCElement = fInitialData.cElement;
		fIsCaseSensitive = fInitialData.isCaseSensitive;
		fCaseSensitive.setSelection( fInitialData.isCaseSensitive );
		fCaseSensitive.setEnabled( fInitialData.cElement == null );
		
		HashSet set = new HashSet( fInitialData.searchFor );
		
		boolean enabled = ! set.contains( fSearchForValues[ fSearchFor.length - 1 ] );
		
		for (int i = 0; i < fSearchFor.length; i++){
			fSearchFor[i].setSelection( set.contains( fSearchForValues[i] ) );
			fSearchFor[i].setEnabled( enabled );			
		}
		
		if( !enabled )
			fSearchFor[ fSearchFor.length - 1 ].setEnabled( true );
			
		setLimitTo( fInitialData.searchFor );
			
		for (int i = 0; i < fLimitTo.length; i++)
			fLimitTo[i].setSelection( fLimitToValues[i] == fInitialData.limitTo );

		fPattern.setText( fInitialData.pattern );
	}
	
	private SearchPatternData tryStructuredSelection( IStructuredSelection selection ) {
		if( selection == null || selection.size() > 1 )
			return null;

		Object o = selection.getFirstElement();
		if( o instanceof ICElement ) {
			return determineInitValuesFrom( (ICElement)o );
		} else if( o instanceof ISearchResultViewEntry ) {
			ICElement element = CSearchUtil.getCElement( ((ISearchResultViewEntry)o).getSelectedMarker() );
			return determineInitValuesFrom( element );
		//} else if( o instanceof LogicalPackage ) {
		//	LogicalPackage lp = (LogicalPackage)o;
		//	return new SearchPatternData( PACKAGE, REFERENCES, fIsCaseSensitive, lp.getElementName(), null );
		} else if( o instanceof IAdaptable ) {
			ICElement element = (ICElement)((IAdaptable)o).getAdapter( ICElement.class );
			if( element != null ) {
				return determineInitValuesFrom( element );
			} else {
				IWorkbenchAdapter adapter= (IWorkbenchAdapter)((IAdaptable)o).getAdapter( IWorkbenchAdapter.class );
				if( adapter != null ){
					List searchFor = new LinkedList();
					searchFor.add( UNKNOWN_SEARCH_FOR );
					return new SearchPatternData( searchFor, DECLARATIONS, fIsCaseSensitive, adapter.getLabel(o), null );
				}
			}
		}
		return null;
	}
	
	/**
	 * try to initialize the Search pattern data based on the current text selection.
	 * @param selection
	 * @return
	 */
	private SearchPatternData trySimpleTextSelection(ISelection selection) {
		SearchPatternData result= null;
		if (selection instanceof ITextSelection) {
			BufferedReader reader= new BufferedReader(new StringReader(((ITextSelection)selection).getText()));
			String text;
			try {
				text= reader.readLine();
				if (text == null)
					text= ""; //$NON-NLS-1$
			} catch (IOException ex) {
				text= ""; //$NON-NLS-1$
			}
			
			List searchFor = new LinkedList();
			searchFor.add( UNKNOWN_SEARCH_FOR );
			result= new SearchPatternData( searchFor, DECLARATIONS, fIsCaseSensitive, text, null);
		}
		return result;
	}
	
	private SearchPatternData getDefaultInitValues() {
		List searchFor = new LinkedList();
		searchFor.add( CLASS_STRUCT );
		return new SearchPatternData( searchFor, DECLARATIONS, fIsCaseSensitive, "", null); //$NON-NLS-1$
	}
		
	private String[] getPreviousSearchPatterns() {
		// Search results are not persistent
		int patternCount= fgPreviousSearchPatterns.size();
		String [] patterns= new String[patternCount];
		for (int i= 0; i < patternCount; i++)
			patterns[i]= ((SearchPatternData) fgPreviousSearchPatterns.get(patternCount - 1 - i)).pattern;
		return patterns;
	}	
	
	private IStructuredSelection asStructuredSelection() {
		IWorkbenchWindow wbWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (wbWindow != null) {
			IWorkbenchPage page= wbWindow.getActivePage();
			if (page != null) {
				IWorkbenchPart part= page.getActivePart();
				if (part != null){
					ISelectionProvider provider = part.getSite().getSelectionProvider();
					if( provider != null ){
						ISelection selection = provider.getSelection();
						if( selection instanceof IStructuredSelection ){
							return (IStructuredSelection)selection;
						}
					}
				}
			}
		}
		return StructuredSelection.EMPTY;
	}

	private SearchPatternData determineInitValuesFrom( ICElement element ) {
		if( element == null )
			return null;
		
		List searchFor = new LinkedList();
		
		//outliune view will confuse methods with functions, so if the 
		//name contains a "::", treat it as a method
		String pattern = element.getElementName();
		boolean forceMethod = ( pattern.indexOf("::") != -1 );
		
		switch ( element.getElementType() ){
			case ICElement.C_FUNCTION_DECLARATION: /*fall through to function */
			case ICElement.C_FUNCTION:	if( forceMethod ) searchFor.add( METHOD ); 
										else 			  searchFor.add( FUNCTION );		
										break;
			case ICElement.C_VARIABLE:	searchFor.add( VAR );			break;
			case ICElement.C_STRUCT:	/*   fall through to CLASS   */	 
			case ICElement.C_CLASS:		searchFor.add( CLASS_STRUCT );	break;
			case ICElement.C_UNION:		searchFor.add( UNION );			break;
			case ICElement.C_ENUMERATOR: /* fall through to FIELD    */
			case ICElement.C_FIELD:		searchFor.add( FIELD );			break;
			case ICElement.C_METHOD_DECLARATION : /*fall through to METHOD */
			case ICElement.C_METHOD:	searchFor.add( METHOD );		break;
			case ICElement.C_NAMESPACE: searchFor.add( NAMESPACE );		break;
		}

		LimitTo limitTo = ALL_OCCURRENCES;			
		
		return new SearchPatternData( searchFor, limitTo, true, pattern, element );
	}

	private SearchPatternData getPatternData() {
		String pattern= getPattern();
		SearchPatternData match= null;
		int i= 0;
		int size= fgPreviousSearchPatterns.size();
		while (match == null && i < size) {
			match= (SearchPatternData) fgPreviousSearchPatterns.get(i);
			i++;
			if (!pattern.equals(match.pattern))
				match= null;
		};
		if (match == null) {
			match= new SearchPatternData(
							getSearchFor(),
							getLimitTo(),
							pattern,
							fCaseSensitive.getSelection(),
							fCElement,
							getContainer().getSelectedScope(),
							getContainer().getSelectedWorkingSets());
			fgPreviousSearchPatterns.add(match);
		}
		else {
			match.searchFor= getSearchFor();
			match.limitTo= getLimitTo();
			match.isCaseSensitive= fCaseSensitive.getSelection();
			match.cElement= fCElement;
			match.scope= getContainer().getSelectedScope();
			match.workingSets= getContainer().getSelectedWorkingSets();
		};
		return match;
	}
	
	private static class SearchPatternData {
		List 		searchFor;
		LimitTo		limitTo;
		String		pattern;
		boolean		isCaseSensitive;
		ICElement	cElement;
		int			scope;
		IWorkingSet[]	 	workingSets;
	
		public SearchPatternData(List s, LimitTo l, boolean i, String p, ICElement element) {
			this(s, l, p, i, element, ISearchPageContainer.WORKSPACE_SCOPE, null);
		}
	
		public SearchPatternData(List s, LimitTo l, String p, boolean i, ICElement element, int scope, IWorkingSet[] workingSets) {
			searchFor= s;
			limitTo= l;
			pattern= p;
			isCaseSensitive= i;
			cElement= element;
			this.scope = scope;
			this.workingSets = workingSets;
		}
	}
	
	//Dialog store id constants
	private final static String PAGE_NAME= "CSearchPage"; //$NON-NLS-1$
	private final static String STORE_CASE_SENSITIVE= PAGE_NAME + "CASE_SENSITIVE"; //$NON-NLS-1$

	private static List fgPreviousSearchPatterns = new ArrayList(20);

	private Button[] fSearchFor;
	private SearchFor[] fSearchForValues = { CLASS_STRUCT, FUNCTION, VAR, UNION, METHOD, FIELD, ENUM, NAMESPACE, UNKNOWN_SEARCH_FOR };
	
	private String[] fSearchForText= {
		CSearchMessages.getString("CSearchPage.searchFor.classStruct"), //$NON-NLS-1$
		CSearchMessages.getString("CSearchPage.searchFor.function"),	//$NON-NLS-1$
		CSearchMessages.getString("CSearchPage.searchFor.variable"), 	//$NON-NLS-1$
		CSearchMessages.getString("CSearchPage.searchFor.union"),		//$NON-NLS-1$
		CSearchMessages.getString("CSearchPage.searchFor.method"), 		//$NON-NLS-1$
		CSearchMessages.getString("CSearchPage.searchFor.field"),		//$NON-NLS-1$
		CSearchMessages.getString("CSearchPage.searchFor.enum"),		//$NON-NLS-1$
		CSearchMessages.getString("CSearchPage.searchFor.namespace"),	//$NON-NLS-1$
		CSearchMessages.getString("CSearchPage.searchFor.any") }; 		//$NON-NLS-1$
		
		
	private Button[] fLimitTo;
	private LimitTo[] fLimitToValues = { DECLARATIONS, DEFINITIONS, REFERENCES, ALL_OCCURRENCES };
	private String[] fLimitToText= {
		CSearchMessages.getString("CSearchPage.limitTo.declarations"), //$NON-NLS-1$
		CSearchMessages.getString("CSearchPage.limitTo.definitions"), //$NON-NLS-1$
		CSearchMessages.getString("CSearchPage.limitTo.references"), //$NON-NLS-1$
		CSearchMessages.getString("CSearchPage.limitTo.allOccurrences") }; //$NON-NLS-1$
		//CSearchMessages.getString("CSearchPage.limitTo.readReferences"), //$NON-NLS-1$		
		//CSearchMessages.getString("CSearchPage.limitTo.writeReferences")}; //$NON-NLS-1$

	private SearchPatternData fInitialData;
	private IStructuredSelection fStructuredSelection;
	private ICElement fCElement;
	private boolean fFirstTime= true;
	private IDialogSettings fDialogSettings;
	private boolean fIsCaseSensitive;

	private Combo fPattern;
	private ISearchPageContainer fContainer;
	private Button fCaseSensitive;
}
