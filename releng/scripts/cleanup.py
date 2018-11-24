loadModule('/System/UI')
while getActiveEditor():
	# We need to cleanup twice. The first one may remove some code (like unneeded
	# type parameters) that then needs to be reformatted.
	executeUI("org.eclipse.jdt.internal.ui.actions.AllCleanUpsAction(getActiveEditor()).run()")
	executeUI("org.eclipse.jdt.internal.ui.actions.AllCleanUpsAction(getActiveEditor()).run()")
	executeUI("getActiveEditor().close(True)")
