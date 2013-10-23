
#include "DateTime.hh"
#include <QtDeclarative/QDeclarativeContext>
#include <QtDeclarative/QDeclarativeEngine>
#include <QtDeclarative/QDeclarativeView>
#include <QtGui/QApplication>

int main( int argc, char * argv[] )
{
    QApplication app( argc, argv );

	DateTime datetime;

    QDeclarativeView view;
    view.rootContext()->setContextProperty( "datetimeModel", &datetime );
    view.setSource( QUrl::fromLocalFile( "$(baseName).qml" ) );

    app.connect( view.engine(), SIGNAL( quit() ), SLOT( quit() ) );

    view.show();
    return app.exec();
}
