package org.eclipse.internal.remote.jsch.core.commands;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.internal.remote.jsch.core.JSchConnection;
import org.eclipse.remote.core.exception.RemoteConnectionException;

public class DeleteCommand extends AbstractRemoteCommand<Void> {

	private final IPath fRemotePath;

	public DeleteCommand(JSchConnection connection, IPath path) {
		super(connection);
		fRemotePath = path;
	}

	@Override
	public Void getResult(IProgressMonitor monitor) throws RemoteConnectionException {
		final SubMonitor subMon = SubMonitor.convert(monitor, 10);

		ExecCommand command = new ExecCommand(getConnection());
		command.setCommand("/bin/rm -rf " + quote(fRemotePath.toString(), true)); //$NON-NLS-1$
		String result = command.getResult(subMon.newChild(10));
		if (!result.equals("")) { //$NON-NLS-1$
			throw new RemoteConnectionException(result);
		}
		return null;
	}

	private String quote(String path, boolean full) {
		StringBuffer buffer = new StringBuffer();
		StringCharacterIterator iter = new StringCharacterIterator(path);
		for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
			switch (c) {
			case '(':
			case ')':
			case '[':
			case ']':
			case '{':
			case '}':
			case '|':
			case '\\':
			case '*':
			case '&':
			case '^':
			case '%':
			case '$':
			case '#':
			case '@':
			case '!':
			case '~':
			case '`':
			case '\'':
			case '"':
			case ':':
			case ';':
			case '?':
			case '<':
			case '>':
			case ',':
			case '\n':
				if (full) {
					buffer.append('\\');
				}
				buffer.append(c);
				continue;
			case ' ':
				buffer.append('\\');
				buffer.append(c);
				continue;
			default:
				buffer.append(c);
				continue;
			}
		}
		return buffer.toString();
	}
}
