
#include "DateTime.h"
#include <QDateTime>

DateTime::DateTime()
{
	startTimer( 500 );
}

DateTime::~DateTime()
{
}

void DateTime::timerEvent( QTimerEvent * )
{
	emit changed( QDateTime::currentDateTime().toString( "yyyy-MM-dd hh:mm:ss" ) );
}
