TEMPLATE = app

QT += qml quick
CONFIG += c++11

RESOURCES += ${projectName}.qrc

qml.files = src/${projectName}.qml

launch_modeall {
	CONFIG(debug, debug|release) {
	    DESTDIR = debug
	} else {
	    DESTDIR = release
	}
}
