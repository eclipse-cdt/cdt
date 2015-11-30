package org.eclipse.cdt.internal.qt.core.qml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.eclipse.cdt.internal.qt.core.Activator;

@SuppressWarnings("nls")
public class QMLAnalyzer {

	private final ScriptEngine engine;

	public QMLAnalyzer() {
		engine = new ScriptEngineManager().getEngineByName("nashorn");

		ScriptContext context = engine.getContext();
		context.setWriter(new OutputStreamWriter(System.out));
		context.setErrorWriter(new OutputStreamWriter(System.err));
	}

	public void load() throws ScriptException, IOException {
		load("/tern-qml/node_modules/acorn/dist/acorn.js");
		load("/tern-qml/node_modules/acorn/dist/acorn_loose.js");
		load("/tern-qml/node_modules/acorn/dist/walk.js");
		load("/tern-qml/node_modules/acorn-qml/inject.js");
		load("/tern-qml/node_modules/acorn-qml/index.js");
		load("/tern-qml/node_modules/acorn-qml/loose/inject.js");
		load("/tern-qml/node_modules/acorn-qml/loose/index.js");
		load("/tern-qml/node_modules/acorn-qml/walk/index.js");

		load("/tern-qml/node_modules/tern/lib/signal.js");
		load("/tern-qml/node_modules/tern/lib/tern.js");
		load("/tern-qml/node_modules/tern/lib/def.js");
		load("/tern-qml/node_modules/tern/lib/comment.js");
		load("/tern-qml/node_modules/tern/lib/infer.js");

		load("/tern-qml/qml.js");
		load("/tern-qml/qml-nsh.js");
	}

	public Object load(String file) throws ScriptException, IOException {
		URL scriptURL = Activator.getDefault().getBundle().getEntry(file);
		if (scriptURL == null) {
			throw new FileNotFoundException(file);
		}
		engine.getContext().setAttribute(ScriptEngine.FILENAME, file, ScriptContext.ENGINE_SCOPE);
		return engine.eval(new BufferedReader(new InputStreamReader(scriptURL.openStream())));
	}

	public ScriptEngine getEngine() {
		return engine;
	}

	public Invocable getInvocable() {
		return (Invocable) engine;
	}

	public Object createTernServer(Object options) throws NoSuchMethodException, ScriptException {
		return getInvocable().invokeFunction("newTernServer", options);
	}

	public interface RequestCallback {
		void callback(Bindings err, Bindings data);
	}

}
