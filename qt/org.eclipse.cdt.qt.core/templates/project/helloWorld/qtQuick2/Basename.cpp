#include <QGuiApplication>
#include <QQuickView>

int main(int argc, char *argv[])
{
    QGuiApplication app(argc, argv);

    QQuickView viewer(QStringLiteral("$(baseName).qml"));
    viewer.show();

    return app.exec();
}
