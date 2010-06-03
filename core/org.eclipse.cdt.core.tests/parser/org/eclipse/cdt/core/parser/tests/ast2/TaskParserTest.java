/*******************************************************************************
 * Copyright (c) 2007, 2009 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.pdom.indexer.TodoTaskParser;
import org.eclipse.cdt.internal.core.pdom.indexer.TodoTaskParser.Task;

public class TaskParserTest extends AST2BaseTest {
	
	public static TestSuite suite() {
		return suite(TaskParserTest.class);
	}

	public void testTaskParser() throws Exception {
		final char[][] taskTags = new char[][] {
				"TODO".toCharArray(),
				"TODO(my name):".toCharArray(),
				"FIXME".toCharArray()
			};
		final int PRIORITY_LOW = 1; 
		final int PRIORITY_NORMAL = 2; 
		final int PRIORITY_HIGH = 3; 
		final int[] taskPriorities = new int[] {
				PRIORITY_LOW,
				PRIORITY_NORMAL,
				PRIORITY_HIGH
			};
		final boolean isTaskCaseSensitive = true;
		
		String code = "/* TODO tag 1\n" + 
		              " * FIXME   tag 2\n" + 
		              " */\n" + 
		              "\n" + 
		              "// TODO(my name): tag 3\n" + 
		              "// TODO(his name): tag 4\n" + 
		              "// todo Not a tag\n" + 
		              "// TODO FIXME tag 5\n" + 
		              "\n" + 
		              "const char* x = \"TODO Not a tag\";"; 
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP, false, true);
		TodoTaskParser parser =	new TodoTaskParser(taskTags, taskPriorities, isTaskCaseSensitive);
		Task[] tasks = parser.parse(tu.getComments());
	
		assertEquals(6, tasks.length);
		assertEquals("TODO", tasks[0].getTag());
		assertEquals("tag 1", tasks[0].getMessage());
		assertEquals(PRIORITY_LOW, tasks[0].getPriority());
		assertEquals(1, tasks[0].getLineNumber());
		assertEquals(3, tasks[0].getStart());
		assertEquals(13, tasks[0].getEnd());
		
		assertEquals("FIXME", tasks[1].getTag());
		assertEquals("tag 2", tasks[1].getMessage());
		assertEquals(PRIORITY_HIGH, tasks[1].getPriority());
		assertEquals(2, tasks[1].getLineNumber());
		
		assertEquals("TODO(my name):", tasks[2].getTag());
		assertEquals("tag 3", tasks[2].getMessage());
		assertEquals(PRIORITY_NORMAL, tasks[2].getPriority());
		assertEquals(5, tasks[2].getLineNumber());
	
		assertEquals("TODO", tasks[3].getTag());
		assertEquals("(his name): tag 4", tasks[3].getMessage());
		assertEquals(PRIORITY_LOW, tasks[3].getPriority());
		assertEquals(6, tasks[3].getLineNumber());

		assertEquals("TODO", tasks[4].getTag());
		assertEquals("tag 5", tasks[4].getMessage());
		assertEquals(PRIORITY_LOW, tasks[4].getPriority());
		assertEquals(8, tasks[4].getLineNumber());
	
		assertEquals("FIXME", tasks[5].getTag());
		assertEquals("tag 5", tasks[5].getMessage());
		assertEquals(PRIORITY_HIGH, tasks[5].getPriority());
		assertEquals(8, tasks[5].getLineNumber());
	}
}
