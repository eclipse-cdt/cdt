package org.eclipse.cdt.internal.core.model;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.parser.util.DeclSpecifier;
import org.eclipse.cdt.internal.core.parser.util.Name;

/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SimpleDeclarationWrapper extends DeclSpecifier implements DeclSpecifier.Container, ICElementWrapper {

	private CElement element = null; 
	private CElement parent = null; 
	int kind; 
	private Name name = null;
	private boolean functionDefinition = false; 

	public SimpleDeclarationWrapper( CElement item )
	{
		this.element = item; 
	}
	
	public SimpleDeclarationWrapper()
	{
	}
	
	/**
	 * Returns the item.
	 * @return CElement
	 */
	public CElement getElement() {
		return element;
	}

	/**
	 * Sets the item.
	 * @param item The item to set
	 */
	public void setElement (CElement item) {
		this.element = (CElement)item;
	}

	/**
	 * Returns the parent.
	 * @return CElement
	 */
	public CElement getParent() {
		return parent;
	}

	/**
	 * Sets the parent.
	 * @param parent The parent to set
	 */
	public void setParent(CElement parent) {
		this.parent = parent;
	}
	
	public void createElements()
	{
		// creates the appropriate C Elements 
		List declaratorList = getDeclarators();
		Declarator [] declarators = (Declarator []) declaratorList.toArray( new Declarator[ declaratorList.size() ] );
		CElement parentElement = getParent(); 
		
		for( int i = 0; i < declarators.length; ++i )
		{
			Declarator currentDeclarator = declarators[i];
			CElement declaration  = null;
			
			// instantiate the right element   
			List clause =currentDeclarator.getParameterDeclarationClause(); 
			if( clause == null )
			{
				// TODO - this was to get rid of the NULL pointer we've been seeing
				if (currentDeclarator.getName() == null)
					return;

				//	this is an attribute or a varaible
				if( parentElement instanceof IStructure )
				{
					declaration = new Field( parentElement, currentDeclarator.getName().toString() ); 
				}
				else if( parentElement instanceof ITranslationUnit )
				{
					declaration = new Variable( parentElement, currentDeclarator.getName().toString() );
				}
			}
			else
			{
				// this is a function or a method
				if( parentElement instanceof IStructure )
				{
					declaration = new Method( parentElement, currentDeclarator.getName().toString() ); 
		
				}
				else if( parentElement instanceof ITranslationUnit )
				{
					declaration = new FunctionDeclaration( parentElement, currentDeclarator.getName().toString() ); 
				}
				
				Parameter [] parameters = (Parameter []) clause.toArray( new Parameter[ clause.size() ]);
				
				for( int j = 0; j< parameters.length; ++j )
				{
					Parameter parm = parameters[j];
					 
				}
				
			}
			
			// hook up the offsets
			declaration.setIdPos( currentDeclarator.getName().getStartOffset(), currentDeclarator.getName().toString().length());
			declaration.setPos( currentDeclarator.getName().getStartOffset(), currentDeclarator.getName().toString().length() );
			
			// add to parent
			parentElement.addChild( declaration ); 	
		}
		
	}
	
	List declarators = new LinkedList();
	
	public void addDeclarator( Object in )
	{
		declarators.add( in ); 
	}
		
	public List getDeclarators()
	{
		return declarators; 
	}
	
	DeclSpecifier declSpec = null; 

	/**
	 * @see org.eclipse.cdt.internal.core.dom.DeclarationSpecifier.CElementWrapper#getDeclSpecifier()
	 */
	public DeclSpecifier getDeclSpecifier() {
		if( declSpec == null )
			declSpec = new DeclSpecifier(); 
			
		return declSpec; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.dom.DeclarationSpecifier.CElementWrapper#setDeclSpecifier(org.eclipse.cdt.internal.core.dom.DeclarationSpecifier)
	 */
	public void setDeclSpecifier(DeclSpecifier in) {
		declSpec = in; 
	}
	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.util.DeclarationSpecifier.Container#removeDeclarator(java.lang.Object)
	 */
	public void removeDeclarator(Object declarator) {
		declarators.remove( declarator );
	}

	/**
	 * Returns the name.
	 * @return Name
	 */
	public Name getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(Name name) {
		this.name = name;
	}

	/**
	 * Returns the kind.
	 * @return int
	 */
	public int getKind() {
		return kind;
	}

	/**
	 * Sets the kind.
	 * @param kind The kind to set
	 */
	public void setKind(int kind) {
		this.kind = kind;
	}

	/**
	 * Returns the functionDefinition.
	 * @return boolean
	 */
	public boolean isFunctionDefinition() {
		return functionDefinition;
	}

	/**
	 * Sets the functionDefinition.
	 * @param functionDefinition The functionDefinition to set
	 */
	public void setFunctionDefinition(boolean functionDefinition) {
		this.functionDefinition = functionDefinition;
	}

}
