/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.tests

import com.google.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.formatter.FormatterTester
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(XtextRunner)
@InjectWith(LinkerScriptInjectorProvider)
class FormatterTest {

	@Inject extension FormatterTester

	@Test def void formatEmptyMemory() {
		assertFormatted[
			expectation = '''
				MEMORY
				{
				}
			'''
			toBeFormatted = '''
				MEMORY {}
			'''
		]
	}

	@Test def void emptyLinesAtTopOfFile() {
		assertFormatted[
			expectation = '''
				MEMORY
				{
				}
			'''
			toBeFormatted = '''

				MEMORY
				{
				}
			'''
		]
	}

	@Test def void emptyLinesAtTopOfFileComment() {
		assertFormatted[
			expectation = '''
				/* comment */
				MEMORY
				{
				}
			'''
			toBeFormatted = '''

				/* comment */

				MEMORY
				{
				}
			'''
		]
	}

	@Test def void indentedMemory() {
		assertFormatted[
			expectation = '''
				MEMORY
				{
				}
			'''
			toBeFormatted = '''
				    MEMORY
				{
				}
			'''
		]
	}

	@Test def void emptyMemoryAlreadyFormatted() {
		assertFormatted[
			toBeFormatted = '''
				MEMORY
				{
				}
			'''
		]
	}

	@Test def void memRegion() {
		assertFormatted[
			expectation = '''
				MEMORY
				{
					RAM : ORIGIN = 0, LENGTH = 0
				}
			'''
			toBeFormatted = '''
				MEMORY{RAM : ORIGIN = 0, LENGTH = 0}
			'''
		]
	}

	@Test def void twoMemRegion() {
		assertFormatted[
			expectation = '''
				MEMORY
				{
					RAM : ORIGIN = 0, LENGTH = 0
					ROM : ORIGIN = 0, LENGTH = 0
				}
			'''
			toBeFormatted = '''
				MEMORY{RAM : ORIGIN = 0, LENGTH = 0ROM : ORIGIN = 0, LENGTH = 0}
			'''
		]
	}

	@Test def void twoMemRegionNoSpaces() {
		assertFormatted[
			expectation = '''
				MEMORY
				{
					RAM : ORIGIN = 0, LENGTH = 0
					ROM : ORIGIN = 0, LENGTH = 0
				}
			'''
			toBeFormatted = '''
				MEMORY{RAM  :ORIGIN=0,LENGTH=0ROM  :ORIGIN=0,LENGTH=0}
			'''
		]
	}

	@Test def void twoMemRegionNoSpacesAltNames() {
		assertFormatted[
			expectation = '''
				MEMORY
				{
					RAM : o = 0, l = 0
					ROM : org = 0, len = 0
				}
			'''
			toBeFormatted = '''
				MEMORY{RAM  :o=0,l=0ROM  :org=0,len=0}
			'''
		]
	}

	@Test def void expressionSimple() {
		assertFormatted[
			useSerializer = false
			expectation = '''
				MEMORY
				{
					RAM : o = 0, l = 1 + 1
				}
			'''
			toBeFormatted = '''
				MEMORY{RAM  :o=0,l= 1  +  1
				}
			'''
		]
	}

	@Test def void expressionParens() {
		assertFormatted[
			useSerializer = false
			expectation = '''
				MEMORY
				{
					RAM : o = 0, l = (1 + 1)
				}
			'''
			toBeFormatted = '''
				MEMORY{RAM  :o=0,l=   (  1  +  1  )
				}
			'''
		]
	}

	@Test def void expressionLessSimple() {
		assertFormatted[
			useSerializer = false
			expectation = '''
				MEMORY
				{
					RAM : o = 0, l = 1 + (1 - 1)
				}
			'''
			toBeFormatted = '''
				MEMORY{RAM  :o=0,l= 1  +  (   1  -  1  )  }
			'''
		]
	}

	@Test def void expressionLengthCall() {
		assertFormatted[
			expectation = '''
				MEMORY
				{
					RAM : o = 0, l = LENGTH(RAM)
				}
			'''
			toBeFormatted = '''
				MEMORY{RAM  :o=0,l=  LENGTH  (  RAM  )  }
			'''
		]
	}

	@Test def void sections() {
		assertFormatted[
			expectation = '''
				SECTIONS
				{
				}
			'''
			toBeFormatted = '''
				SECTIONS{}
			'''
		]
	}

	@Test def void outputSectionsAllFields() {
		assertFormatted[
			expectation = '''
				SECTIONS
				{
					name1 0 (NOLOAD) : AT(0) ALIGN(0) SUBALIGN(0) ONLY_IF_RO
					{
					} >RAM AT>RAM :phdr1 :phdr2 =0,
					name2 0 (NOLOAD) : AT(0) ALIGN(0) SUBALIGN(0) ONLY_IF_RO
					{
					} >RAM AT>RAM :phdr1 :phdr2 =0,
				}
			'''
			toBeFormatted = '''
				SECTIONS{   name1   0  (  NOLOAD  )  :  AT  (  0  )   ALIGN
				(  0  )  SUBALIGN  (  0  )  ONLY_IF_RO  {  }  >  RAM  AT  >
				RAM  :  phdr1   :  phdr2  =  0  ,  name2   0  (  NOLOAD  )
				:  AT  (  0  )   ALIGN  (  0  )  SUBALIGN  (  0  )
				ONLY_IF_RO  {  }  >  RAM  AT  >  RAM  :  phdr1   :  phdr2  =  0  ,  }
			'''
		]
	}

	@Test def void outputSectionsRegion() {
		assertFormatted[
			toBeFormatted = '''
				SECTIONS
				{
					name :
					{
					} >RAM
				}
			'''
		]
	}

	@Test def void outputSectionsAtRegion() {
		assertFormatted[
			toBeFormatted = '''
				SECTIONS
				{
					name :
					{
					} AT>RAM
				}
			'''
		]
	}

	@Test def void outputSectionsRegionAndAtRegion() {
		assertFormatted[
			toBeFormatted = '''
				SECTIONS
				{
					name :
					{
					} >RAM AT>RAM
				}
			'''
		]
	}

	@Test def void outputSectionsNoFieldsAfter() {
		assertFormatted[
			expectation = '''
				SECTIONS
				{
					name1 0 (NOLOAD) : AT(0) ALIGN(0) SUBALIGN(0) ONLY_IF_RO
					{
					}
					name2 0 (NOLOAD) : AT(0) ALIGN(0) SUBALIGN(0) ONLY_IF_RO
					{
					}
				}
			'''
			toBeFormatted = '''
				SECTIONS{   name1   0  (  NOLOAD  )  :  AT  (  0  )
				ALIGN  (  0  )  SUBALIGN  (  0  )
				ONLY_IF_RO  {  }     name2   0  (  NOLOAD  )
				:  AT  (  0  )   ALIGN  (  0  )  SUBALIGN
				(  0  )  ONLY_IF_RO  {  }  }
			'''
		]
	}

	@Test def void statements() {
		assertFormatted[
			useSerializer = false
			expectation = '''
				STARTUP(name)
				ENTRY(name2)
				EXTERN(name3 name4 name5)
				ASSERT((1 + 2) - 3, message)
				FORCE_COMMON_ALLOCATION
				INCLUDE includefile
			'''
			toBeFormatted = '''
				STARTUP  (  name  )  ENTRY  (  name2  )  EXTERN  (
				name3  name4  name5  )  ASSERT  (  (  1  +  2  )  -  3  ,  message
				)  FORCE_COMMON_ALLOCATION  INCLUDE  includefile
			'''
		]
	}

	@Test def void statementsGroupInput() {
		assertFormatted[
			expectation = '''
				GROUP(file1, file2)
				GROUP(file1 file2)
				GROUP(-lfile1 -lfile2)
				INPUT(file1 file2)
				INPUT(file1, file2)
				INPUT(AS_NEEDED(file3 file4))
				INPUT(AS_NEEDED(file3, file4))
			'''
			toBeFormatted  =  '''
				GROUP  (  file1,  file2
				)  GROUP  (  file1  file2
				)  GROUP  (  -lfile1  -lfile2
				)  INPUT  (  file1  file2
				)  INPUT  (  file1,  file2
				)  INPUT  (  AS_NEEDED  (  file3  file4)
				)  INPUT  (  AS_NEEDED  (  file3,  file4)
				)  '''
		]
	}

	@Test def void statementsNop() {
		assertFormatted[
			expectation = '''
				;;;
			'''
			toBeFormatted = '''
				;
				  ;
				    ;
			'''
		]
	}

	@Test def void statementsNopUnjoined() {
		assertFormatted[
			expectation = '''
				ENTRY(name2)
				;
			'''
			toBeFormatted = '''
				ENTRY(name2);
			'''
		]
	}

	@Test def void assignment() {
		assertFormatted[
			expectation = '''
				symbol1 = name1,
				symbol2 = name2;
				symbol3 = name3;
			'''
			toBeFormatted = '''
				symbol1  =  name1 ,  symbol2  =  name2  ;   symbol3  =  name3  ;
			'''
		]
	}

	@Test def void assignmentFeatures() {
		assertFormatted[
			expectation = '''
				symbol1 = name1;
				symbol2 += name2;
				symbol3 /= name3;
				symbol4 |= name4;
			'''
			toBeFormatted = '''
				symbol1  =  name1
				; symbol2   +=  name2
				; symbol3  /=  name3
				; symbol4  |=  name4
				;
			'''
		]
	}

	@Test def void assignmentProvidesHiddens() {
		assertFormatted[
			expectation = '''
				HIDDEN(symbol1 = name1),
				PROVIDE(symbol2 = name2);
				PROVIDE_HIDDEN(symbol3 = name3);
			'''
			toBeFormatted = '''
				HIDDEN  (  symbol1  =  name1  )
				, PROVIDE  ( symbol2  =  name2 )
				;  PROVIDE_HIDDEN  ( symbol3  =  name3 ) ;
			'''
		]
	}

	@Test def void assignmentExpression() {
		assertFormatted[
			useSerializer = false
			expectation = '''
				symbol1 = name1 + name2 * 3 + (2 + 1);
				PROVIDE(symbol2 = name4 + name5 * 3 + (2 + 1));
			'''
			toBeFormatted = '''
				symbol1 =  name1  +  name2  *  3  +  ( 2  +   1 )
				;  PROVIDE ( symbol2 =  name4  +  name5  *  3  +  ( 2  +   1 )
				)  ;
			'''
		]
	}

	@Test def void inputSections() {
		assertFormatted[
			expectation = '''
				SECTIONS
				{
					name :
					{
						*(*)
						*(.text .text.*)
						*(.text, .text.*)
						file.o
					}
				}
			'''
			toBeFormatted = '''
				SECTIONS
				{
					name :
					{
						*  (  *
						) * ( .text  .text.*
						) * ( .text,  .text.*
						) file.o
					}
				}
			'''
		]
	}

	@Test def void inputSectionFlags() {
		assertFormatted[
			expectation = '''
				SECTIONS
				{
					name :
					{
						INPUT_SECTION_FLAGS(FLAG1) file.o
						INPUT_SECTION_FLAGS(FLAG1) *(.text .text.*)
						INPUT_SECTION_FLAGS(FLAG1 & FLAG2) file.o
						INPUT_SECTION_FLAGS(FLAG1 & FLAG2) *(.text .text.*)
					}
				}
			'''
			toBeFormatted = '''
				SECTIONS
				{
					name :
					{
						INPUT_SECTION_FLAGS ( FLAG1 )
						   file.o
						INPUT_SECTION_FLAGS ( FLAG1 )  *  (.text .text.*)
						INPUT_SECTION_FLAGS ( FLAG1  &  FLAG2 )  file.o
						INPUT_SECTION_FLAGS ( FLAG1  &  FLAG2 )  * (.text .text.*)
					}
				}
			'''
		]
	}

	@Test def void inputSectionKeep() {
		assertFormatted[
			expectation = '''
				SECTIONS
				{
					name :
					{
						KEEP(file.o)
						KEEP(*(.text .text.*))
					}
				}
			'''
			toBeFormatted = '''
				SECTIONS
				{
					name :
					{
						KEEP ( file.o ) KEEP ( * ( .text  .text.*
						) )
					}
				}
			'''
		]
	}

	@Test def void inputSectionKeepFlags() {
		assertFormatted[
			expectation = '''
				SECTIONS
				{
					name :
					{
						KEEP(INPUT_SECTION_FLAGS(FLAG1) file.o)
						KEEP(INPUT_SECTION_FLAGS(FLAG1) *(.text .text.*))
					}
				}
			'''
			toBeFormatted = '''
				SECTIONS
				{
					name :
					{
						KEEP
						( INPUT_SECTION_FLAGS (
						FLAG1 )
						 file.o
						 ) KEEP ( INPUT_SECTION_FLAGS (
						 FLAG1 )
						  * ( .text  .text.* ) )
					}
				}
			'''
		]
	}

	@Test def void inputSectionExcludeFiles() {
		assertFormatted[
			expectation = '''
				SECTIONS
				{
					name :
					{
						EXCLUDE_FILE(file1 file2) *(.text .text.*)
						*(EXCLUDE_FILE(file1 file2) .text .text.*)
						*(.text EXCLUDE_FILE(file1 file2) .text.*)
						*(EXCLUDE_FILE(file1 file2) .text EXCLUDE_FILE(file1 file2) .text.*)
						EXCLUDE_FILE(file1 file2) *(EXCLUDE_FILE(file1 file2) .text EXCLUDE_FILE(file1 file2) .text.*)
						EXCLUDE_FILE(file1 file2) *(.text, .text.*)
						*(EXCLUDE_FILE(file1 file2) .text, .text.*)
						*(.text, EXCLUDE_FILE(file1 file2) .text.*)
						*(EXCLUDE_FILE(file1 file2) .text, EXCLUDE_FILE(file1 file2) .text.*)
						EXCLUDE_FILE(file1 file2) *(EXCLUDE_FILE(file1 file2) .text, EXCLUDE_FILE(file1 file2) .text.*)
					}
				}
			'''
			toBeFormatted = '''
				SECTIONS
				{
					name :
					{
						EXCLUDE_FILE ( file1 file2
						)  * ( .text .text.*
						)
						* ( EXCLUDE_FILE ( file1 file2
						)  .text .text.*
						)
						* ( .text EXCLUDE_FILE ( file1 file2
						)  .text.*
						)
						* ( EXCLUDE_FILE ( file1 file2
						)  .text EXCLUDE_FILE ( file1 file2
						)  .text.*
						)
						EXCLUDE_FILE ( file1 file2
						)  * ( EXCLUDE_FILE ( file1 file2
						)  .text EXCLUDE_FILE ( file1 file2
						)  .text.*
						)
						EXCLUDE_FILE ( file1 file2
						)  * ( .text, .text.*
						)
						* ( EXCLUDE_FILE ( file1 file2
						)  .text, .text.*
						)
						* ( .text, EXCLUDE_FILE ( file1 file2
						)  .text.*
						)
						* ( EXCLUDE_FILE ( file1 file2
						)  .text, EXCLUDE_FILE ( file1 file2
						)  .text.*
						)
						EXCLUDE_FILE ( file1 file2
						)  * ( EXCLUDE_FILE ( file1 file2
						)  .text, EXCLUDE_FILE ( file1 file2
						)  .text.*
						)
					}
				}
			'''
		]
	}

	@Test def void inputSectionSorts() {
		assertFormatted[
			expectation = '''
				SECTIONS
				{
					name :
					{
						*(*)
						*(SORT(*))
						SORT(*)(*)
						SORT(*)(SORT(*))
						*(SORT_BY_NAME(*))
						*(SORT_BY_ALIGNMENT(*))
						*(SORT_NONE(*))
						*(SORT_BY_NAME(SORT_BY_ALIGNMENT(*)))
						*(SORT_BY_INIT_PRIORITY(*))
					}
				}
			'''
			toBeFormatted = '''
				SECTIONS
				{
					name :
					{
						* ( * )  * ( SORT ( * ) ) SORT ( * ) ( * )  SORT ( * ) ( SORT ( * ) )
						* ( SORT_BY_NAME ( * ) ) * ( SORT_BY_ALIGNMENT ( * ) )
						* ( SORT_NONE ( * ) ) * ( SORT_BY_NAME ( SORT_BY_ALIGNMENT ( * ) ) )
						* ( SORT_BY_INIT_PRIORITY ( * ) )
					}
				}
			'''
		]
	}

	@Test def void inputSectionSortExcludes() {
		assertFormatted[
			expectation = '''
				SECTIONS
				{
					name :
					{
						SORT(EXCLUDE_FILE(file1 file2) *)(.text .text.*)
					}
				}
			'''
			toBeFormatted = '''
				SECTIONS
				{
					name :
					{
						SORT ( EXCLUDE_FILE ( file1
						file2 )  * ) ( .text  .text.*
						)
					}
				}
			'''
		]
	}

	@Test def void inputSectionSortKeepExclude() {
		assertFormatted[
			expectation = '''
				SECTIONS
				{
					name :
					{
						KEEP(SORT(EXCLUDE_FILE(file1 file2) *)(.text .text.*))
					}
				}
			'''
			toBeFormatted = '''
				SECTIONS
				{
					name :
					{
						KEEP (
						SORT ( EXCLUDE_FILE ( file1
						file2 )  * ) ( .text  .text.*
						) )
					}
				}
			'''
		]
	}

	@Test def void inputSectionSortKeep() {
		assertFormatted[
			expectation = '''
				SECTIONS
				{
					name :
					{
						KEEP(SORT(*)(.text .text.*))
					}
				}
			'''
			toBeFormatted = '''
				SECTIONS
				{
					name :
					{
						KEEP (
						SORT ( * ) ( .text  .text.*
						) )
					}
				}
			'''
		]
	}

	@Test def void outputSectionFollowedByAssignment() {
		assertFormatted[
			toBeFormatted = '''
				SECTIONS
				{
					.new_section :
					{
					}
					symbol = 0x0;
				}
			'''
		]

	}

}
