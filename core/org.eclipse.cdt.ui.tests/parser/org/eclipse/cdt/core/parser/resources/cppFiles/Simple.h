#ifndef SIMPLE_H
#define SIMPLE_H

class OtherClass;

class SimpleClass
{
public:
	SimpleClass( void );
	SimpleClass( const SimpleClass & arg );
	
	virtual ~SimpleClass( void );
	
	SimpleClass & operator=( const SimpleClass & arg );
	
private:
	struct InnerStruct
	{
		inline InnerStruct( int a ){ _a = a; }
		inline ~InnerStruct( void ){}
		unsigned int _a;
	};

	InnerStruct inner;

	void init( void * );

public:
	InnerStruct & getInner( void );
};

#endif /* SIMPLE_H */
