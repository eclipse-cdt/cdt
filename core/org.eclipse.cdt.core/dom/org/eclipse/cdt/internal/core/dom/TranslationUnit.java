package org.eclipse.cdt.internal.core.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;


/**
 */
public class TranslationUnit implements IScope {

	private List declarations = new LinkedList();
	private List macros = new ArrayList(); 
	private List inclusions = new ArrayList(); 
	
	public void addDeclaration(Declaration declaration) {
		declarations.add(declaration);
	}

	public List getDeclarations() {
		return Collections.unmodifiableList( declarations );
	}
	
	/**
	 * @return
	 */
	public List getInclusions() {
		return Collections.unmodifiableList( inclusions );
	}

	/**
	 * @return
	 */
	public List getMacros() {
		return Collections.unmodifiableList( macros );
	}

	public void addMacro(Macro macro) {
		macros.add(macro);
	}

	public void addInclusion(Inclusion inclusion) {
		inclusions.add(inclusion);
	}


	public Iterator iterateOffsetableElements()
	{
		return new OffsetableIterator();
	}

	public class OffsetableIterator implements Iterator 
	{		
		private final Iterator declarationIter; 
		private final Iterator inclusionIter; 
		private final Iterator macroIter; 
		
		private IOffsetable currentMacro = null, currentInclusion= null, currentDeclaration= null; 
		
		public OffsetableIterator()
		{
			declarationIter = getDeclarations().iterator();
			inclusionIter = getInclusions().iterator();
			macroIter = getMacros().iterator();		
			updateInclusionIterator(); 
			updateDeclarationIterator();
			updateMacroIterator(); 
		}
		
		private Object updateDeclarationIterator()
		{
			Object offsetable = currentDeclaration; 			
			currentDeclaration = ( declarationIter.hasNext() ) ? (IOffsetable)declarationIter.next() : null; 
			return offsetable; 
		}

		private Object updateMacroIterator()
		{
			Object offsetable = currentMacro; 
			currentMacro = ( macroIter.hasNext() ) ? (IOffsetable)macroIter.next() : null; 
			return offsetable;
		}
		
		private Object updateInclusionIterator()
		{
			Object offsetable = currentInclusion;
			currentInclusion = ( inclusionIter.hasNext() ) ? (IOffsetable)inclusionIter.next() : null;
			return offsetable;
		}
		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return (( currentMacro == null && currentInclusion == null && currentDeclaration == null ) ? 
				false : true);
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			// case 1: all are null 
			if( ! hasNext() ) 
				throw new NoSuchElementException();
				
			// case 2: two of three are null 
			if( currentMacro == null && currentInclusion == null )
				return updateDeclarationIterator();
			if( currentDeclaration == null && currentInclusion == null )
				return updateMacroIterator();
			if( currentMacro == null && currentDeclaration == null )
				return updateInclusionIterator();
			
			// case 3: 1 is null
			if( currentMacro == null )
				if( currentDeclaration.getStartingOffset() < currentInclusion.getStartingOffset() )
					return updateDeclarationIterator(); 
				else
					return updateInclusionIterator(); 

			if( currentInclusion == null )
				if( currentDeclaration.getStartingOffset() < currentMacro.getStartingOffset() )
					return updateDeclarationIterator(); 
				else
					return updateMacroIterator(); 

			if( currentDeclaration == null )
				if( currentInclusion.getStartingOffset() < currentMacro.getStartingOffset() )
					return updateInclusionIterator(); 
				else
					return updateMacroIterator(); 
			
			// case 4: none are null 
			if( currentInclusion.getStartingOffset() < currentMacro.getStartingOffset() && 
				currentInclusion.getStartingOffset() < currentDeclaration.getStartingOffset() ) 
				return updateInclusionIterator(); 
				
			if( currentMacro.getStartingOffset() < currentInclusion.getStartingOffset() && 
				currentMacro.getStartingOffset() < currentDeclaration.getStartingOffset() ) 
				return updateMacroIterator();
			// only remaining case
			return updateDeclarationIterator();
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException( "OffsetableIterator is a const iterator"); 
		}
	}

}
