/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTTemplateDeclaration;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTTemplateInstantiation;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTTemplateSpecialization;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectMap;

/**
 * @author aniefer
 */
public class TemplateFactory extends ExtensibleSymbol implements ITemplateFactory {

	private IContainerSymbol lastSymbol; 
	
	private ArrayList templates = new ArrayList(4);
	private ArrayList symbols = new ArrayList(4);
	private Map  argMap = new HashMap();
	
	protected TemplateFactory( ParserSymbolTable table ){
		super( table );
	}
	
	public void pushTemplate(ITemplateSymbol template ) {
		templates.add( template );
	}

	public void pushSymbol(ISymbol symbol) {
		symbols.add( symbol );
	}
	
	public void pushTemplateId(ISymbol symbol, List args) {
		symbols.add( symbol );
		argMap.put( symbol, new ArrayList( args ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addTemplateId(org.eclipse.cdt.internal.core.parser.pst.ISymbol, java.util.List)
	 */
	public void addTemplateId(ISymbol symbol, List args) throws ParserSymbolTableException {
		ISymbol previous = findPreviousSymbol( symbol, args );
		ITemplateSymbol origTemplate = (previous != null && previous.getContainingSymbol() instanceof ITemplateSymbol ) 
		                                   ? (ITemplateSymbol) previous.getContainingSymbol() : null;
		
		if( origTemplate == null ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
		}
		
		ITemplateSymbol template = (ITemplateSymbol) templates.get( templates.size() - 1 );
		
		List params = ( template != null ) ? template.getParameterList() : null;
		if( params == null ){
			//explicit instantiation
			addExplicitInstantiation( origTemplate, args );
		} else if( params.size() == 0 ){
			//explicit specialization
			 addExplicitSpecialization( origTemplate, symbol, args );
			 
		} else {
			//partial speciailization
			ISpecializedSymbol spec = template.getSymbolTable().newSpecializedSymbol( symbol.getName() );
			int size = params.size();
			for( int i = 0; i < size; i++){
				spec.addTemplateParameter( (ISymbol) params.get( i ) );
			}
			size = args.size();
			spec.prepareArguments( size );
			for( int i = 0; i < size; i++){
				spec.addArgument( (ITypeInfo) args.get(i) );
			}
			
			spec.addSymbol( symbol );
			origTemplate.addSpecialization( spec );
			
			//replace the symbol attached to the AST node.
			if( getASTExtension() != null ){
				 TemplateSymbolExtension extension = (TemplateSymbolExtension) template.getASTExtension();
				 extension.replaceSymbol( spec );
				 ASTTemplateDeclaration templateDecl = (ASTTemplateDeclaration) getASTExtension().getPrimaryDeclaration();
				 templateDecl.releaseFactory();
				 templateDecl.setSymbol( spec );
				 templateDecl.setOwnedDeclaration( symbol );
			}
		}
		
		
	}
	
	public void addSymbol(ISymbol symbol) throws ParserSymbolTableException {
		lastSymbol = getLastSymbol();
		
		ISymbol sym = null;
		ISymbol container = null;
		boolean templateParamState = false;
		int size = symbols.size();
		int templatesSize = templates.size(), templatesIdx = 0;
		for( int i = 0; i < size; i++ ){
			sym = (ISymbol) symbols.get( i );
			if( !sym.getContainingSymbol().isType( ITypeInfo.t_template ) ){
				symbols.remove( i-- );
				size--;
			} else if( templatesIdx < templatesSize ) {
				ITemplateSymbol template = (ITemplateSymbol) templates.get( templatesIdx-- );
				if( template.getParameterList().size() == 0 ){
					templateParamState = true;
					container = sym;
				} else if( templateParamState )
					throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
			} else {
				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
			}
		}
		
		int numTemplates = templates.size();
		int numSymbols = symbols.size();
		
		if( templateParamState ){
			List args = (List) argMap.get( container );
			addExplicitSpecialization( (ITemplateSymbol) container.getContainingSymbol(), symbol, args );
			return;
		}
		
		if( numTemplates == numSymbols + 1 ){
			//basic template declaration or Definition
			basicTemplateDeclaration( symbol );
			return;
		}
		
		if( numTemplates == numSymbols ){
			//all of the templates were matched to a symbol, we are doing a member
			memberDeclaration( symbol );
			return;
		}
	}
	
	private ISymbol findPreviousSymbol( ISymbol symbol, List args ) throws ParserSymbolTableException{
		ISymbol previous = null;
		
		List argList = null;
		if( symbol instanceof IParameterizedSymbol ){
			List params = ((IParameterizedSymbol)symbol).getParameterList();
			int size = params.size();
			argList = new ArrayList( size );
			for( int i = 0; i < size; i++ ){
				ISymbol param = (ISymbol) params.get(i);
				argList.add( param.getTypeInfo() );
			}
		}
		
		if( symbol.isType( ITypeInfo.t_function ) ){
			if( args != null )
				previous = lookupFunctionTemplateId( symbol.getName(), argList, args, false );
			else
				previous = lookupMethodForDefinition( symbol.getName(), argList );
		} else if ( symbol.isType( ITypeInfo.t_constructor ) ){
			previous = lookupConstructor( argList );
		} else {
			previous = lookupMemberForDefinition( symbol.getName() );
		}
		return previous;
	}
	
	private void basicTemplateDeclaration( ISymbol symbol ) throws ParserSymbolTableException{
		ITemplateSymbol template = (ITemplateSymbol)templates.get( 0 );

		
		if( template.getParameterList().size() == 0 ){
			//explicit specialization, deduce some arguments and use addTemplateId
			ISymbol previous = findPreviousSymbol( symbol, new ArrayList() );
			if( previous == null || !(previous.getContainingSymbol() instanceof ITemplateSymbol) )
				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
			
			List args = null;
			if( symbol instanceof IParameterizedSymbol ){
				args = TemplateEngine.resolveTemplateFunctionArguments( null, (ITemplateSymbol)previous.getContainingSymbol(), (IParameterizedSymbol) symbol );
			}
			if( args != null )
				addTemplateId( symbol, args );
			else
				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
			
		} else {
			ISymbol previous = findPreviousSymbol( symbol, null );			
			
			if( previous == null ){
				//new template
				template.setName( symbol.getName () );
				template.addSymbol( symbol );
				getContainingSymbol().addSymbol( template );	
				if( getASTExtension() != null ){
					ASTTemplateDeclaration templateDecl = (ASTTemplateDeclaration) getASTExtension().getPrimaryDeclaration();
					templateDecl.releaseFactory();
					templateDecl.setOwnedDeclaration( symbol );
				}
			} else {
				//definition for something declared already
				ITemplateSymbol originalTemplate = null;
				ISymbol originalSymbol = null;
				
				if( previous instanceof ITemplateSymbol ){
					originalTemplate = (ITemplateSymbol) previous;
					originalSymbol = originalTemplate.getTemplatedSymbol();
				} else {
					if( previous.getContainingSymbol() instanceof ITemplateSymbol ){
						originalTemplate = (ITemplateSymbol) previous.getContainingSymbol();
						originalSymbol = previous;
					} else {
						throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
					}
				}
				
				if( originalSymbol.isForwardDeclaration() ){
					
					if( originalTemplate.getParameterList().size() != template.getParameterList().size() ){
						throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
					}
					
					symbols.add( originalSymbol );
					doDefinitionParameterMaps( symbol );
										
					originalTemplate.addSymbol( symbol );
					
					if( getASTExtension() != null ){
						ASTTemplateDeclaration templateDecl = (ASTTemplateDeclaration) getASTExtension().getPrimaryDeclaration();
						templateDecl.releaseFactory();
						templateDecl.setOwnedDeclaration( symbol );
					}
				} else {
					throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidOverload );
				}
			}
		}
	}
	
	private void memberDeclaration( ISymbol symbol ) throws ParserSymbolTableException{
		ISymbol previous = findPreviousSymbol( symbol, null );
		if( previous == null ) {
			//could happen in trying to define something for which we don't have a declaration
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
		} 
		IContainerSymbol originalContainer = previous.getContainingSymbol();
		
		if( previous.isForwardDeclaration() ){
			doDefinitionParameterMaps( symbol );
								
			originalContainer.addSymbol( symbol );
			
			if( getASTExtension() != null ){
				ASTTemplateDeclaration templateDecl = (ASTTemplateDeclaration) getASTExtension().getPrimaryDeclaration();
				templateDecl.releaseFactory();
				templateDecl.setOwnedDeclaration( symbol );
			}
		} else {
			throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidOverload );
		}		
	}
	
	private void doDefinitionParameterMaps( ISymbol newSymbol ) throws ParserSymbolTableException {
		if( templates.size() != symbols.size() ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
		}
		
		int size = templates.size();
		for( int i = 0; i < size; i++ ){
			Map defnMap = new HashMap();
			
			ITemplateSymbol template = (ITemplateSymbol) templates.get(i);
			ITemplateSymbol origTemplate = (ITemplateSymbol) ((ISymbol)symbols.get(i)).getContainingSymbol();
			
			List tList = template.getParameterList();
			List oList = origTemplate.getParameterList();
			int tListSize = tList.size();
			if( oList.size() < tListSize )
				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
			for( int j = 0; j < tListSize; j++ ){		
				ISymbol param = (ISymbol) tList.get(j);
				ISymbol origParam = (ISymbol) oList.get(j);
				defnMap.put( param, origParam );	
			}
			
			((TemplateSymbol)origTemplate).addToDefinitionParameterMap( newSymbol, defnMap );	
		}
	}
	
	private void addExplicitInstantiation( ITemplateSymbol origTemplate, List args ) throws ParserSymbolTableException {
		ISymbol instance = origTemplate.instantiate( args );
		
		if( getASTExtension() != null ){
			ASTTemplateInstantiation templateInstance = (ASTTemplateInstantiation) getASTExtension().getPrimaryDeclaration();
			templateInstance.releaseFactory();
			templateInstance.setInstanceSymbol( instance );
		}
	}
	private void addExplicitSpecialization( ITemplateSymbol template, ISymbol symbol, List arguments ) throws ParserSymbolTableException {
		template.addExplicitSpecialization( symbol, arguments );
		
		int size = symbols.size();
		for( int i = 0; i < size; i++ ){
			IContainerSymbol sym = (IContainerSymbol) symbols.get( 0 );
			ISymbol instantiated = sym.getInstantiatedSymbol();
			if( instantiated != null ){
				IContainerSymbol container = instantiated.getContainingSymbol();
				if( container.isType( ITypeInfo.t_template ) ){
					((ITemplateSymbol) container ).removeInstantiation( sym );
				}
			}
		}
		
		if( getASTExtension() != null ){
		 	ASTTemplateSpecialization spec = (ASTTemplateSpecialization) getASTExtension().getPrimaryDeclaration();
		 	spec.setOwnedDeclaration( symbol );
		 }
	}
	
	private IContainerSymbol getLastSymbol() {
		if( lastSymbol != null )
			return lastSymbol;
		else if( !symbols.isEmpty() ) {
			ISymbol symbol = (ISymbol) symbols.get( symbols.size() - 1 );
			if( symbol instanceof IDeferredTemplateInstance )
				return ((IDeferredTemplateInstance)symbol).getTemplate().getTemplatedSymbol();
			else if( symbol instanceof IContainerSymbol )
				return (IContainerSymbol) symbol;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateFactory#lookupMemberForDefinition(java.lang.String)
	 */
	public ISymbol lookupMemberForDefinition(String name) throws ParserSymbolTableException {
		ISymbol look = null;
		IContainerSymbol last = getLastSymbol();
		if( last != null ){
			look = last.lookupMemberForDefinition( name );
		} else {
			look = getContainingSymbol().lookupMemberForDefinition( name );
		}
		if( look instanceof ITemplateSymbol ){
			return ((ITemplateSymbol)look).getTemplatedSymbol();
		} 
		return look;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#elaboratedLookup(org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType, java.lang.String)
	 */
	public ISymbol elaboratedLookup(ITypeInfo.eType type, String name) throws ParserSymbolTableException {
		ListIterator iter = templates.listIterator( templates.size() );
		while( iter.hasPrevious() ){
			ITemplateSymbol template = (ITemplateSymbol) iter.previous();
			
			ISymbol look = template.lookupMemberForDefinition( name );
			if( look != null && look.isType( type ) ){
				return look;
			}
		}
		
		return getContainingSymbol().elaboratedLookup( type, name );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookup(java.lang.String)
	 */
	public ISymbol lookup(String name) throws ParserSymbolTableException {
		ListIterator iter = templates.listIterator( templates.size() );
		while( iter.hasPrevious() ){
			ITemplateSymbol template = (ITemplateSymbol) iter.previous();
			
			ISymbol look = template.lookupMemberForDefinition( name );
			if( look != null ){
				return look;
			}
		}
		
		return getContainingSymbol().lookup( name );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupMethodForDefinition(java.lang.String, java.util.List)
	 */
	public IParameterizedSymbol lookupMethodForDefinition(String name, List parameters) throws ParserSymbolTableException {
		IContainerSymbol last = getLastSymbol();
		if( last != null ){
			IParameterizedSymbol found = last.lookupMethodForDefinition( name, parameters );
			if( found != null ){
				return found;
			}
		}
		return getContainingSymbol().lookupMethodForDefinition( name, parameters );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupNestedNameSpecifier(java.lang.String)
	 */
	public IContainerSymbol lookupNestedNameSpecifier(String name) throws ParserSymbolTableException {
		return getContainingSymbol().lookupNestedNameSpecifier( name );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#qualifiedLookup(java.lang.String)
	 */
	public ISymbol qualifiedLookup(String name) throws ParserSymbolTableException {
		return getContainingSymbol().qualifiedLookup( name );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#qualifiedLookup(java.lang.String, org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType)
	 */
	public ISymbol qualifiedLookup(String name, ITypeInfo.eType t) throws ParserSymbolTableException {
		return getContainingSymbol().qualifiedLookup( name, t );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#unqualifiedFunctionLookup(java.lang.String, java.util.List)
	 */
	public IParameterizedSymbol unqualifiedFunctionLookup(String name, List parameters) throws ParserSymbolTableException {
		return getContainingSymbol().unqualifiedFunctionLookup( name, parameters );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#memberFunctionLookup(java.lang.String, java.util.List)
	 */
	public IParameterizedSymbol memberFunctionLookup(String name, List parameters) throws ParserSymbolTableException {
		return getContainingSymbol().memberFunctionLookup( name, parameters );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#qualifiedFunctionLookup(java.lang.String, java.util.List)
	 */
	public IParameterizedSymbol qualifiedFunctionLookup(String name, List parameters) throws ParserSymbolTableException {
		return getContainingSymbol().qualifiedFunctionLookup( name, parameters );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupTemplate(java.lang.String, java.util.List)
	 */
	public ISymbol lookupTemplateId(String name, List arguments) throws ParserSymbolTableException {
		ISymbol look = null;
		IContainerSymbol last = getLastSymbol();
		if( last != null ){
			look = last.lookupTemplateId( name, arguments );
		} else {
			look = getContainingSymbol().lookupTemplateId( name, arguments );
		}
		return look;
	}
	
	public IContainerSymbol lookupTemplateIdForDefinition(String name, List arguments) throws ParserSymbolTableException {
		ISymbol look = null;
		IContainerSymbol last = getLastSymbol();
		if( last != null ){
			look = last.lookupMemberForDefinition( name );
		} else {
			look = getContainingSymbol().lookupMemberForDefinition( name );
		}
		
		if( look instanceof ITemplateSymbol ){
			ITemplateSymbol t = TemplateEngine.selectTemplateOrSpecialization( (ITemplateSymbol) look, getNextAvailableTemplate().getParameterList(), arguments );
			look =  t.getTemplatedSymbol();
		}
		return (IContainerSymbol) (( look instanceof IContainerSymbol) ? look : null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupFunctionTemplateId(java.lang.String, java.util.List, java.util.List)
	 */
	public ISymbol lookupFunctionTemplateId(String name, List parameters, List arguments, boolean forDefinition) throws ParserSymbolTableException {
		IContainerSymbol last = getLastSymbol();
		if( last != null ){
			IParameterizedSymbol found = (IParameterizedSymbol) last.lookupFunctionTemplateId( name, parameters, arguments, forDefinition );
			if( found != null ){
				return found;
			}
		}
		return getContainingSymbol().lookupFunctionTemplateId( name, parameters, arguments, forDefinition );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#lookupConstructor(java.util.List)
	 */
	public IParameterizedSymbol lookupConstructor(List parameters) throws ParserSymbolTableException {
		IContainerSymbol last = getLastSymbol();
		if( last != null && last instanceof IDerivableContainerSymbol ){
			IDerivableContainerSymbol derivable = (IDerivableContainerSymbol) last;
			IParameterizedSymbol found = derivable.lookupConstructor( parameters );
			if( found != null )
				return found;
		}
		if( getContainingSymbol() instanceof IDerivableContainerSymbol )
			return ((IDerivableContainerSymbol) getContainingSymbol()).lookupConstructor( parameters );
		
		return null;
	}
	
	private ITemplateSymbol getNextAvailableTemplate() throws ParserSymbolTableException{
		int numSymbols = symbols.size();
		int numTemplates = templates.size();
		int templateIdx = 0;
		for( int i = 0; i < numSymbols; i++ ){
			ISymbol symbol = (ISymbol) symbols.get(i);
			if( symbol.getContainingSymbol().isType( ITypeInfo.t_template ) ){
				if( templateIdx < numTemplates )
					templateIdx++;
				else
					throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
			}
		}
		
		if( templateIdx >= numTemplates )
			return null;
		return (ITemplateSymbol) templates.get( templateIdx );
	}

	
	//TODO: Do any of these other functions need to be implemented?
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#removeSymbol(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public boolean removeSymbol(ISymbol symbol) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#hasUsingDirectives()
	 */
	public boolean hasUsingDirectives() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#getUsingDirectives()
	 */
	public List getUsingDirectives() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDirective(org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public IUsingDirectiveSymbol addUsingDirective(IContainerSymbol namespace) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDeclaration(java.lang.String)
	 */
	public IUsingDeclarationSymbol addUsingDeclaration(String name) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDeclaration(java.lang.String, org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public IUsingDeclarationSymbol addUsingDeclaration(String name, IContainerSymbol declContext) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#getContainedSymbols()
	 */
	public ObjectMap getContainedSymbols() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#prefixLookup(org.eclipse.cdt.internal.core.parser.pst.TypeFilter, java.lang.String, boolean)
	 */
	public List prefixLookup(TypeFilter filter, String prefix, boolean qualified, List paramList) {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#isVisible(org.eclipse.cdt.internal.core.parser.pst.ISymbol, org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public boolean isVisible(ISymbol symbol, IContainerSymbol qualifyingSymbol) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#getContentsIterator()
	 */
	public Iterator getContentsIterator() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#clone()
	 */
	public Object clone() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#instantiate(org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol, java.util.Map)
	 */
	public ISymbol instantiate(ITemplateSymbol template, Map argMapParm) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setName(java.lang.String)
	 */
	public void setName(String name) {
	    /* nothing */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getName()
	 */
	public String getName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#isType(org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType)
	 */
	public boolean isType(ITypeInfo.eType type) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#isType(org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType, org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType)
	 */
	public boolean isType(ITypeInfo.eType type, ITypeInfo.eType upperType) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getType()
	 */
	public ITypeInfo.eType getType() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setType(org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType)
	 */
	public void setType(ITypeInfo.eType t) {
	    /* nothing */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getTypeInfo()
	 */
	public ITypeInfo getTypeInfo() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setTypeInfo(org.eclipse.cdt.internal.core.parser.pst.TypeInfo)
	 */
	public void setTypeInfo(ITypeInfo info) {
	    /* nothing */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getTypeSymbol()
	 */
	public ISymbol getTypeSymbol() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setTypeSymbol(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void setTypeSymbol(ISymbol type) {
	    /* nothing */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#isForwardDeclaration()
	 */
	public boolean isForwardDeclaration() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setIsForwardDeclaration(boolean)
	 */
	public void setIsForwardDeclaration(boolean forward) {
	    /* nothing */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#compareCVQualifiersTo(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public int compareCVQualifiersTo(ISymbol symbol) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getPtrOperators()
	 */
	public List getPtrOperators() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#addPtrOperator(org.eclipse.cdt.internal.core.parser.pst.TypeInfo.PtrOp)
	 */
	public void addPtrOperator(ITypeInfo.PtrOp ptrOp) {
	    /* nothing */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#isTemplateInstance()
	 */
	public boolean isTemplateInstance() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getInstantiatedSymbol()
	 */
	public ISymbol getInstantiatedSymbol() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setInstantiatedSymbol(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void setInstantiatedSymbol(ISymbol symbol) {
	    /* nothing */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#isTemplateMember()
	 */
	public boolean isTemplateMember() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setIsTemplateMember(boolean)
	 */
	public void setIsTemplateMember(boolean isMember) {
	    /* nothing */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getDepth()
	 */
	public int getDepth() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getIsInvisible()
	 */
	public boolean getIsInvisible() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setIsInvisible(boolean)
	 */
	public void setIsInvisible(boolean invisible) {
	    /* nothing */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#addParent(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addParent(ISymbol parent) {
	    /* nothing */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#addParent(org.eclipse.cdt.internal.core.parser.pst.ISymbol, boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility, int, java.util.List)
	 */
	public void addParent(ISymbol parent, boolean virtual, ASTAccessVisibility visibility, int offset, List references) {
	    /* nothing */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#getParents()
	 */
	public List getParents() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#hasParents()
	 */
	public boolean hasParents() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#addConstructor(org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol)
	 */
	public void addConstructor(IParameterizedSymbol constructor) {
	    /* nothing */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#addCopyConstructor()
	 */
	public void addCopyConstructor() {
	    /* nothing */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#getConstructors()
	 */
	public List getConstructors() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#addFriend(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addFriend(ISymbol friend) {
	    /* nothing */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#lookupForFriendship(java.lang.String)
	 */
	public ISymbol lookupForFriendship(String name) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#lookupFunctionForFriendship(java.lang.String, java.util.List)
	 */
	public IParameterizedSymbol lookupFunctionForFriendship(String name, List parameters) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#getFriends()
	 */
	public List getFriends() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#preparePtrOperatros(int)
	 */
	public void preparePtrOperatros(int numPtrOps) {
	    /* nothing */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setForwardSymbol(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void setForwardSymbol(ISymbol forward) {
	    /* nothing */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getForwardSymbol()
	 */
	public ISymbol getForwardSymbol() {
		return null;
	}
}
