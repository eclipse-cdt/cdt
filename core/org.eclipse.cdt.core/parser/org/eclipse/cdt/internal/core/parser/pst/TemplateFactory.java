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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTTemplateDeclaration;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo.PtrOp;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType;

/**
 * @author aniefer
 */
public class TemplateFactory extends ExtensibleSymbol implements ITemplateFactory {

	private IContainerSymbol lastSymbol; 
	
	private List templates = new LinkedList();
	private List symbols = new LinkedList();
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
		argMap.put( symbol, new LinkedList( args ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addTemplateId(org.eclipse.cdt.internal.core.parser.pst.ISymbol, java.util.List)
	 */
	public void addTemplateId(ISymbol symbol, List args) throws ParserSymbolTableException {
		ISymbol previous = findPreviousSymbol( symbol );
		ITemplateSymbol origTemplate = (previous != null ) ? (ITemplateSymbol) previous.getContainingSymbol() : null;
		
		if( origTemplate == null ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
		}
		
		ITemplateSymbol template = (ITemplateSymbol) templates.get( templates.size() - 1 );
		
		List params = template.getParameterList();
		if( params.size() == 0 ){
			//explicit specialization
			 addExplicitSpecialization( origTemplate, symbol, args );
		} else {
			//partial speciailization
			ISpecializedSymbol spec = template.getSymbolTable().newSpecializedSymbol( symbol.getName() );
			Iterator iter = params.iterator();
			while( iter.hasNext() ){
				spec.addTemplateParameter( (ISymbol) iter.next() );
			}
			iter = args.iterator();
			while( iter.hasNext() ){
				spec.addArgument( (TypeInfo) iter.next() );
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
			}
		}
		
		
	}
	
	public void addSymbol(ISymbol symbol) throws ParserSymbolTableException {
		lastSymbol = (IContainerSymbol) (( symbols.size() > 0 ) ? symbols.get( symbols.size() - 1) : null);
		
		Iterator iter = symbols.iterator();
		ListIterator tIter = templates.listIterator();
		
		ISymbol sym = null;
		while( iter.hasNext() ){
			sym = (ISymbol) iter.next();
			if( !sym.getContainingSymbol().isType( TypeInfo.t_template ) ){
				iter.remove();
			} else if( tIter.hasNext() ) {
//				ITemplateSymbol template = (ITemplateSymbol) tIter.next();
//				List args = (List) argMap.get( sym );
//				template = TemplateEngine.selectTemplateOrSpecialization( (ITemplateSymbol) sym.getContainingSymbol(), template.getParameterList(), args );
//				tIter.set( template );
			} else {
				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
			}
		}
		
		int numTemplates = templates.size();
		int numSymbols = symbols.size();
		
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
	
	private ISymbol findPreviousSymbol( ISymbol symbol ) throws ParserSymbolTableException{
		ISymbol previous = null;
		
		List argList = null;
		if( symbol instanceof IParameterizedSymbol ){
			argList = new LinkedList();
			Iterator i = ((IParameterizedSymbol)symbol).getParameterList().iterator();
			while( i.hasNext() ){
				ISymbol param = (ISymbol) i.next();
				argList.add( param.getTypeInfo() );
			}
		}
		
		if( symbol.isType( TypeInfo.t_function ) ){
			previous = lookupMethodForDefinition( symbol.getName(), argList );
		} else if ( symbol.isType( TypeInfo.t_constructor ) ){
			previous = lookupConstructor( argList );
		} else {
			previous = lookupMemberForDefinition( symbol.getName() );
		}
		return previous;
	}
	
	private void basicTemplateDeclaration( ISymbol symbol ) throws ParserSymbolTableException{
		ITemplateSymbol template = (ITemplateSymbol)templates.get( 0 );
		if( template.getParameterList().size() == 0 ){
			//explicit specialization
		} else {
			ISymbol previous = findPreviousSymbol( symbol );
			
			if( previous == null ){
				//new template
				template.setName( symbol.getName () );
				template.addSymbol( symbol );
				getContainingSymbol().addSymbol( template );	
				if( getASTExtension() != null ){
					ASTTemplateDeclaration templateDecl = (ASTTemplateDeclaration) getASTExtension().getPrimaryDeclaration();
					templateDecl.releaseFactory();
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
					}
				} else {
					throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidOverload );
				}
			}
		}
	}
	
	private void memberDeclaration( ISymbol symbol ) throws ParserSymbolTableException{
		ISymbol previous = findPreviousSymbol( symbol );
		if( previous == null ) {
			//??
		} else {
			IContainerSymbol originalContainer = previous.getContainingSymbol();
			
			if( previous.isForwardDeclaration() ){
				doDefinitionParameterMaps( symbol );
									
				originalContainer.addSymbol( symbol );
				
				if( getASTExtension() != null ){
					ASTTemplateDeclaration templateDecl = (ASTTemplateDeclaration) getASTExtension().getPrimaryDeclaration();
					templateDecl.releaseFactory();
				}
			} else {
				throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidOverload );
			}
		}
		
	}
	
	private void doDefinitionParameterMaps( ISymbol newSymbol ) throws ParserSymbolTableException {
		if( templates.size() != symbols.size() ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
		}
		
		Iterator tempIter = templates.iterator();
		Iterator symIter = symbols.iterator();
		
		while( tempIter.hasNext() ){
			Map defnMap = new HashMap();
			
			ITemplateSymbol template = (ITemplateSymbol) tempIter.next();
			ITemplateSymbol origTemplate = (ITemplateSymbol) ((ISymbol)symIter.next()).getContainingSymbol();
			
			Iterator params = template.getParameterList().iterator();
			Iterator origParams = origTemplate.getParameterList().iterator();
						
			while( params.hasNext() ){
				ISymbol param = (ISymbol) params.next();
				ISymbol origParam = (ISymbol) origParams.next();
				defnMap.put( param, origParam );	
			}
			
			origTemplate.getDefinitionParameterMap().put( newSymbol, defnMap );	
		}
	}
	
	private void addExplicitSpecialization( ITemplateSymbol template, ISymbol symbol, List arguments ) throws ParserSymbolTableException {
		Iterator templatesIter = templates.iterator();
		Iterator argsIter = arguments.iterator();
		
//		while( templatesIter.hasNext() ){
//			ITemplateSymbol template = (ITemplateSymbol)templatesIter.next();
			
			template.addExplicitSpecialization( symbol, arguments );
		//}
		
//		if( getTemplateFunctions() != null && argsIter.hasNext() ){
//			List args = (List) argsIter.next();
//			ITemplateSymbol template = TemplateEngine.resolveTemplateFunctions( getTemplateFunctions(), args, symbol );
//			if( template != null ){
//				template.addExplicitSpecialization( symbol, args );
//			} else {
//				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
//			}
//		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateFactory#lookupMemberForDefinition(java.lang.String)
	 */
	public ISymbol lookupMemberForDefinition(String name) throws ParserSymbolTableException {
		ISymbol look = null;
		if( lastSymbol != null || !symbols.isEmpty() ){
			IContainerSymbol symbol = (lastSymbol != null ) ? lastSymbol : (IContainerSymbol) symbols.get( symbols.size() - 1 );
			look = ((IContainerSymbol)symbol).lookupMemberForDefinition( name );
		} else {
			look = getContainingSymbol().lookupMemberForDefinition( name );
		}
		if( look instanceof ITemplateSymbol ){
			return ((ITemplateSymbol)look).getTemplatedSymbol();
		} else {
			return look;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#removeSymbol(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public boolean removeSymbol(ISymbol symbol) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#hasUsingDirectives()
	 */
	public boolean hasUsingDirectives() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#getUsingDirectives()
	 */
	public List getUsingDirectives() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDirective(org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public IUsingDirectiveSymbol addUsingDirective(IContainerSymbol namespace) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDeclaration(java.lang.String)
	 */
	public IUsingDeclarationSymbol addUsingDeclaration(String name) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDeclaration(java.lang.String, org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public IUsingDeclarationSymbol addUsingDeclaration(String name, IContainerSymbol declContext) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#getContainedSymbols()
	 */
	public Map getContainedSymbols() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#prefixLookup(org.eclipse.cdt.internal.core.parser.pst.TypeFilter, java.lang.String, boolean)
	 */
	public List prefixLookup(TypeFilter filter, String prefix, boolean qualified) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#elaboratedLookup(org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType, java.lang.String)
	 */
	public ISymbol elaboratedLookup(eType type, String name) throws ParserSymbolTableException {
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
		if( lastSymbol != null || !symbols.isEmpty() ){
			IContainerSymbol symbol = (lastSymbol != null ) ? lastSymbol : (IContainerSymbol) symbols.get( symbols.size() - 1 );
			IParameterizedSymbol found = ((IContainerSymbol)symbol).lookupMethodForDefinition( name, parameters );
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
	public ISymbol qualifiedLookup(String name, eType t) throws ParserSymbolTableException {
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
		if( lastSymbol != null || !symbols.isEmpty() ){
			IContainerSymbol symbol = (lastSymbol != null ) ? lastSymbol : (IContainerSymbol) symbols.get( symbols.size() - 1 );
			look = ((IContainerSymbol)symbol).lookupTemplateId( name, arguments );
		} else {
			look = getContainingSymbol().lookupTemplateId( name, arguments );
		}
		return look;
	}
	
	public IContainerSymbol lookupTemplateIdForDefinition(String name, List arguments) throws ParserSymbolTableException {
		ISymbol look = null;
		if( lastSymbol != null || !symbols.isEmpty() ){
			IContainerSymbol symbol = (lastSymbol != null ) ? lastSymbol : (IContainerSymbol) symbols.get( symbols.size() - 1 );
			look = ((IContainerSymbol)symbol).lookupMemberForDefinition( name );
		} else {
			look = getContainingSymbol().lookupMemberForDefinition( name );
		}
		
		if( look instanceof ITemplateSymbol ){
			ITemplateSymbol t = TemplateEngine.selectTemplateOrSpecialization( (ITemplateSymbol) look, getNextAvailableTemplate().getParameterList(), arguments );
			look = ((ITemplateSymbol) look).getTemplatedSymbol();
		}
		return (IContainerSymbol) (( look instanceof IContainerSymbol) ? look : null);
	}

	private ITemplateSymbol getNextAvailableTemplate() throws ParserSymbolTableException{
		Iterator tIter = templates.iterator();
		Iterator sIter = symbols.iterator();
		
		while( sIter.hasNext() ){
			ISymbol symbol = (ISymbol) sIter.next();
			if( symbol.getContainingSymbol().isType( TypeInfo.t_template ) ){
				if( tIter.hasNext() )
					tIter.next();
				else
					throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
				
			}
		}
		
		if( !tIter.hasNext() )
			return null;
		else
			return (ITemplateSymbol) tIter.next();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#isVisible(org.eclipse.cdt.internal.core.parser.pst.ISymbol, org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public boolean isVisible(ISymbol symbol, IContainerSymbol qualifyingSymbol) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#getContentsIterator()
	 */
	public Iterator getContentsIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#clone()
	 */
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#instantiate(org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol, java.util.Map)
	 */
	public ISymbol instantiate(ITemplateSymbol template, Map argMap) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setName(java.lang.String)
	 */
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#isType(org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType)
	 */
	public boolean isType(eType type) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#isType(org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType, org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType)
	 */
	public boolean isType(eType type, eType upperType) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getType()
	 */
	public eType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setType(org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType)
	 */
	public void setType(eType t) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getTypeInfo()
	 */
	public TypeInfo getTypeInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setTypeInfo(org.eclipse.cdt.internal.core.parser.pst.TypeInfo)
	 */
	public void setTypeInfo(TypeInfo info) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getTypeSymbol()
	 */
	public ISymbol getTypeSymbol() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setTypeSymbol(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void setTypeSymbol(ISymbol type) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#isForwardDeclaration()
	 */
	public boolean isForwardDeclaration() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setIsForwardDeclaration(boolean)
	 */
	public void setIsForwardDeclaration(boolean forward) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#compareCVQualifiersTo(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public int compareCVQualifiersTo(ISymbol symbol) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getPtrOperators()
	 */
	public List getPtrOperators() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#addPtrOperator(org.eclipse.cdt.internal.core.parser.pst.TypeInfo.PtrOp)
	 */
	public void addPtrOperator(PtrOp ptrOp) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#isTemplateInstance()
	 */
	public boolean isTemplateInstance() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getInstantiatedSymbol()
	 */
	public ISymbol getInstantiatedSymbol() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setInstantiatedSymbol(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void setInstantiatedSymbol(ISymbol symbol) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#isTemplateMember()
	 */
	public boolean isTemplateMember() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setIsTemplateMember(boolean)
	 */
	public void setIsTemplateMember(boolean isMember) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getDepth()
	 */
	public int getDepth() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#getIsInvisible()
	 */
	public boolean getIsInvisible() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbol#setIsInvisible(boolean)
	 */
	public void setIsInvisible(boolean invisible) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#addParent(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addParent(ISymbol parent) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#addParent(org.eclipse.cdt.internal.core.parser.pst.ISymbol, boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility, int, java.util.List)
	 */
	public void addParent(ISymbol parent, boolean virtual, ASTAccessVisibility visibility, int offset, List references) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#getParents()
	 */
	public List getParents() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#hasParents()
	 */
	public boolean hasParents() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#addConstructor(org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol)
	 */
	public void addConstructor(IParameterizedSymbol constructor) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#addCopyConstructor()
	 */
	public void addCopyConstructor() throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#lookupConstructor(java.util.List)
	 */
	public IParameterizedSymbol lookupConstructor(List parameters) throws ParserSymbolTableException {
		if( lastSymbol != null || !symbols.isEmpty() ){
			IContainerSymbol symbol = (lastSymbol != null ) ? lastSymbol : (IContainerSymbol) symbols.get( symbols.size() - 1 );
			if( symbol instanceof IDerivableContainerSymbol ){
				IParameterizedSymbol found = ((IDerivableContainerSymbol)symbol).lookupConstructor( parameters );
				if( found != null )
					return found;
			}
		}
		return ((IDerivableContainerSymbol) getContainingSymbol()).lookupConstructor( parameters );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#getConstructors()
	 */
	public List getConstructors() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#addFriend(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addFriend(ISymbol friend) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#lookupForFriendship(java.lang.String)
	 */
	public ISymbol lookupForFriendship(String name) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#lookupFunctionForFriendship(java.lang.String, java.util.List)
	 */
	public IParameterizedSymbol lookupFunctionForFriendship(String name, List parameters) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol#getFriends()
	 */
	public List getFriends() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupFunctionTemplateId(java.lang.String, java.util.List, java.util.List)
	 */
	public ISymbol lookupFunctionTemplateId(String name, List parameters, List arguments) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		return null;
	}
}
