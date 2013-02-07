/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.internal.core.dom.ast.tag;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.tag.IBindingTagger;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPScope;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Internal container for extensions of org.eclipse.cdt.core.tagger.  The implementation of the
 * tagger is instantiated only after checking the enablement expression (if present) for the
 * specified binding.  This avoids activating the contributing plugin until it is actually needed.
 */
public class TaggerDescriptor
{
	private static final String Attr_LocalId = "local-id"; //$NON-NLS-1$
	private static final String Attr_Class = "class"; //$NON-NLS-1$

	private final IConfigurationElement element;
	private final Expression enablementExpression;
	private Boolean fStatus = null;

	private String id;
	private IBindingTagger tagger;

	private static final String Var_projectNature = "projectNatures"; //$NON-NLS-1$
	private static final String Var_languageId = "languageId"; //$NON-NLS-1$

	public TaggerDescriptor( IConfigurationElement element )
	{
		this.element = element;

		Expression expr = null;
		IConfigurationElement[] children = element.getChildren( ExpressionTagNames.ENABLEMENT );
		switch (children.length) {
		case 0:
			fStatus = Boolean.TRUE;
			break;
		case 1:
			try {
				ExpressionConverter parser = ExpressionConverter.getDefault();
				expr = parser.perform( children[0] );
			} catch (CoreException e) {
				CCorePlugin.log( "Error in enablement expression of " + id, e ); //$NON-NLS-1$
			}
			break;
		default:
			CCorePlugin.log( "Too many enablement expressions for " + id ); //$NON-NLS-1$
			fStatus = Boolean.FALSE;
			break;
		}
		enablementExpression = expr;
	}

	public String getId()
	{
		if( id != null )
			return id;

		String globalId = element.getContributor().getName();
		String localId = element.getAttribute( Attr_LocalId );

		// there must be a valid local id
		if( localId == null )
		{
			String extId = element.getDeclaringExtension().getSimpleIdentifier();
			CCorePlugin.log( "Invalid extension " + globalId + '.' + extId + " must provide tagger's local-id" ); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}

		// the extension should not include the plugin id, but return immediately if it does
		if( localId.startsWith( globalId )
		 && localId.length() > globalId.length() )
			return localId;

		// make sure the local id has real content
		if( localId.isEmpty() )
		{
			String extId = element.getDeclaringExtension().getSimpleIdentifier();
			CCorePlugin.log( "Invalid extension " + globalId + '.' + extId + " must provide value for tagger's local-id" ); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}

		// otherwise prepend with the globalId, and ensure a dot between them
		if( localId.charAt( 0 ) == '.' )
			return globalId + localId;
		return  globalId + '.' + localId;
	}

	private static final Pattern PDOMFileRegex = Pattern.compile( ".*/(.*)\\.\\d+\\.pdom$" ); //$NON-NLS-1$

	private static IProject getIndexingProject( IBinding binding )
	{
		// TODO The goal is to get a list of this index's project natures.  The natures are then
		//      used to compute enablement for the taggers.  This avoids activating plugins that
		//      should not contribute to the current index.
		//
		//      This solution uses a hack that assumes a specific name for the file in which the
		//      PDOM is stored.  If the binding is not from the PDOM, then there's an assumption that
		//      it is from a translation unit that is contained by the project being index.  This is
		//      probably not always true, although I don't have a concrete example yet.
		//
		//      Hopefully there is a better way to do this.

		IPDOMBinding pdomBinding = (IPDOMBinding)binding.getAdapter( IPDOMBinding.class );
		if( pdomBinding != null )
		{
			PDOM pdom = pdomBinding.getPDOM();
			if( pdom == null )
				return null;

			File pdom_storage = pdom.getPath();
			if( pdom_storage == null )
				return null;

			Matcher m = PDOMFileRegex.matcher( pdom_storage.getPath() );
			if( m.matches() )
				return ResourcesPlugin.getWorkspace().getRoot().getProject( m.group( 1 ) );
			return null;
		}

		try
		{
			IScope scope = binding.getScope();
			while( scope != null
				&& ! ( scope instanceof CPPScope ) )
				scope = scope.getParent();

			if( scope != null
			 && scope instanceof CPPScope )
			{
				CPPScope cppScope = (CPPScope)scope;
				IASTNode node = cppScope.getPhysicalNode();
				if( node == null )
					return null;

				IASTTranslationUnit astTU = node.getTranslationUnit();
				if( astTU == null )
					return null;

				ITranslationUnit tu = astTU.getOriginatingTranslationUnit();
				if( tu == null )
					return null;

				ICProject cProject = tu.getCProject();
				if( cProject == null )
					return null;

				return cProject.getProject();
			}
		}
		catch( DOMException e ) { e.printStackTrace(); }
		return null;
	}

	private boolean matches( IBinding binding )
	{
		// if the enablement expression is missing or structurally invalid, then return immediately
		if( fStatus != null )
			return fStatus.booleanValue();

		if(	enablementExpression != null )
			try
			{
				IProject project = getIndexingProject( binding );
				EvaluationContext evalContext = new EvaluationContext( null, project );

				String[] natures = project.getDescription().getNatureIds();
				evalContext.addVariable( Var_projectNature, Arrays.asList( natures ) );

//				ILanguage language = null;//tu.getLanguage();
//				if( language != null )
//					evalContext.addVariable( Var_languageId, language.getId() );

				return enablementExpression.evaluate( evalContext ) == EvaluationResult.TRUE;
			}
			catch( CoreException e )
			{
				CCorePlugin.log( "Error while evaluating enablement expression for " + id, e ); //$NON-NLS-1$
			}

		fStatus = Boolean.FALSE;
		return false;
	}

	private IBindingTagger createTagger()
	{
		try
		{
			return (IBindingTagger)element.createExecutableExtension( Attr_Class );
		}
		catch( CoreException e )
		{
			String id = element.getDeclaringExtension().getNamespaceIdentifier() + '.'
					  + element.getDeclaringExtension().getSimpleIdentifier();
			CCorePlugin.log( "Error in class attribute of " + id, e ); //$NON-NLS-1$
		}

		return null;
	}

	/**
	 * Activates the plugin if needed.
	 */
	public IBindingTagger getBindingTaggerFor( IBinding binding )
	{
		if( ! matches( binding ) )
			return null;

		if( tagger == null )
			synchronized( this )
			{
				if( tagger == null )
					tagger = createTagger();
			}

		return tagger;
	}
}
