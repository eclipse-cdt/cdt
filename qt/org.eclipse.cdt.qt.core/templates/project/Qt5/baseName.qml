import QtQuick 2.0

Rectangle {
    width: 480
    height: 320
    color: "lightblue"

    MouseArea {
        anchors.fill: parent
        onClicked: Qt.quit()
    }

    Text {
        id: title
        text: "Hello World from Qt5"
        font.family: "Helvetica"
        font.pointSize: 24
        anchors.centerIn: parent
    }

    Text {
        id: datetime
        objectName: "datetime"
        font.family: "Helvetica"
        font.pointSize: 16
        anchors {
            horizontalCenter: title.horizontalCenter
            top: title.bottom
        }

        Connections {
            target: datetimeModel
            onChanged: {
                datetime.text = now
            }
        }
    }
}
