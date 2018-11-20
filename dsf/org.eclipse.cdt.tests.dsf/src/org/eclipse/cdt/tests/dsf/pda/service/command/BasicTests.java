/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.pda.service.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandListener;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.examples.dsf.pda.PDAPlugin;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDACommandResult;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class BasicTests extends CommandControlTestsBase {

	@BeforeClass
	public static void setProgram() {
		File programFile = PDAPlugin.getFileInPlugin(new Path("samples/example.pda"));
		fProgram = programFile.getPath();
	}

	@Test
	public void testCommandListener() throws CoreException, InterruptedException, ExecutionException {

		class CommandInfo {
			CommandInfo(ICommand<?> command, ICommandResult result) {
				fCommand = command;
				fResult = result;
			}

			ICommand<?> fCommand;
			ICommandResult fResult;
		}

		class CommandListener implements ICommandListener {

			List<CommandInfo> fDoneCommands = new LinkedList<>();
			List<CommandInfo> fQueuedCommands = new LinkedList<>();
			List<CommandInfo> fRemovedCommands = new LinkedList<>();
			List<CommandInfo> fSentCommands = new LinkedList<>();

			@Override
			public void commandDone(ICommandToken token, ICommandResult result) {
				fDoneCommands.add(new CommandInfo(token.getCommand(), result));
			}

			@Override
			public void commandQueued(ICommandToken token) {
				fQueuedCommands.add(new CommandInfo(token.getCommand(), null));
			}

			@Override
			public void commandRemoved(ICommandToken token) {
				fRemovedCommands.add(new CommandInfo(token.getCommand(), null));
			}

			@Override
			public void commandSent(ICommandToken token) {
				fSentCommands.add(new CommandInfo(token.getCommand(), null));
			}

			void reset() {
				fDoneCommands.clear();
				fQueuedCommands.clear();
				fRemovedCommands.clear();
				fSentCommands.clear();
			}
		}

		final CommandListener listener = new CommandListener();
		fExecutor.execute(new DsfRunnable() {
			@Override
			public void run() {
				fCommandControl.addCommandListener(listener);
			}
		});

		final PDATestCommand testCommand = new PDATestCommand(fCommandControl.getContext(), "data 1");

		// Test sending the command and checking all listeners were called.
		Query<PDACommandResult> sendCommandQuery = new Query<PDACommandResult>() {
			@Override
			protected void execute(DataRequestMonitor<PDACommandResult> rm) {
				fCommandControl.queueCommand(testCommand, rm);
			}
		};
		fExecutor.execute(sendCommandQuery);
		PDACommandResult result = sendCommandQuery.get();
		assertEquals(1, listener.fQueuedCommands.size());
		assertEquals(testCommand, listener.fQueuedCommands.get(0).fCommand);
		assertEquals(0, listener.fRemovedCommands.size());
		assertEquals(1, listener.fSentCommands.size());
		assertEquals(testCommand, listener.fSentCommands.get(0).fCommand);
		assertEquals(1, listener.fDoneCommands.size());
		assertEquals(testCommand, listener.fDoneCommands.get(0).fCommand);
		assertEquals(result, listener.fDoneCommands.get(0).fResult);

		// Test queuing then removing command
		listener.reset();
		Query<Object> queueRemoveCommandQuery = new Query<Object>() {
			@Override
			protected void execute(DataRequestMonitor<Object> rm) {
				ICommandToken token = fCommandControl.queueCommand(testCommand,
						new DataRequestMonitor<PDACommandResult>(fExecutor, null) {
							@Override
							protected void handleCompleted() {
								fail("This command should never have been executed.");
							}
						});
				fCommandControl.removeCommand(token);

				rm.setData(new Object());
				rm.done();
			}
		};
		fExecutor.execute(queueRemoveCommandQuery);
		queueRemoveCommandQuery.get();
		assertEquals(1, listener.fQueuedCommands.size());
		assertEquals(testCommand, listener.fQueuedCommands.get(0).fCommand);
		assertEquals(1, listener.fRemovedCommands.size());
		assertEquals(testCommand, listener.fRemovedCommands.get(0).fCommand);
		assertEquals(0, listener.fSentCommands.size());
		assertEquals(0, listener.fDoneCommands.size());

	}
}
