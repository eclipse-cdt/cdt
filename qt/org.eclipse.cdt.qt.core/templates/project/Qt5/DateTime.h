
#ifndef DATETIME_H
#define DATETIME_H

#include <QObject>

class DateTime : public QObject
{
Q_OBJECT

public:
	DateTime();
	virtual ~DateTime();

protected:
    virtual void timerEvent( QTimerEvent * );

signals:
    void changed( QString now );
};

#endif
