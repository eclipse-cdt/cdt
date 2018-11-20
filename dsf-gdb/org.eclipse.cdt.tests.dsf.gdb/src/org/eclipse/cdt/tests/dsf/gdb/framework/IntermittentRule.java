/*******************************************************************************
 * Copyright (c) 2016 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.framework;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * This is the rule to add to tests that rarely fail randomly and you want to keep them but cannot figure out they fail.
 * It is safe to use it in any class, it will only apply to tests which have @Intermittent annotation
 <code>
 import org.eclipse.cdt.tests.dsf.gdb.framework.Intermittent
 import org.eclipse.cdt.tests.dsf.gdb.framework.IntermittentRule

 class SomeTest {
 public @Rule IntermittentRule rule = new IntermittentRule();

 @Test
 @Intermittent(repetition = 3)
 public void someTest (){...}
 }
 </code>

 You can also ally this to the whole class
 <code>
 import org.eclipse.cdt.tests.dsf.gdb.framework.Intermittent
 import org.eclipse.cdt.tests.dsf.gdb.framework.IntermittentRule

 @Intermittent(repetition = 3)
 class SomeTest {
 public @Rule IntermittentRule rule = new IntermittentRule();
 ...
 }

 </code>
 */
public class IntermittentRule implements MethodRule {
	public static class RunIntermittent extends Statement {
		private final FrameworkMethod method;
		private final Statement statement;

		public RunIntermittent(FrameworkMethod method, Statement statement) {
			this.method = method;
			this.statement = statement;
		}

		@Override
		public void evaluate() throws Throwable {
			int repetition = 1;
			Intermittent methodAnnot = method.getAnnotation(Intermittent.class);
			if (methodAnnot != null) {
				repetition = methodAnnot.repetition();
			} else {
				Intermittent classAnnot = method.getDeclaringClass().getAnnotation(Intermittent.class);
				if (classAnnot != null) {
					repetition = classAnnot.repetition();
				}
			}
			if (repetition > 1) {
				for (int i = 0; i < repetition; i++) {
					try {
						statement.evaluate();
						break; // did not fail yay, we are done
					} catch (Throwable e) {
						if (i < repetition - 1)
							continue; // try again
						throw e;
					}
				}
			} else
				statement.evaluate();
		}
	}

	@Override
	public Statement apply(Statement base, final FrameworkMethod method, final Object target) {
		return new RunIntermittent(method, base);
	}
}
