#include <opencv2\opencv.hpp>
#include <vector>

using namespace cv;
using namespace std;

int inline decode64(char * c)
{
	if( *c == 0x2D) return 0;
	if( *c <  0x3A) return (*c-0x2F);
	if( *c <  0x5B) return (*c-0x36);
	if( *c == 0x5F) return 0x25;
	return (*c-0x3B);
}

char inline encode64(int i)
{
	if( i == 0x00) return 0x2D;
	if( i <  0x0B) return (i+0x2F);
	if( i <  0x25) return (i+0x36);
	if( i == 0x25) return 0x5F;
	return (i+0x3B);
}

//Converts a input with a list of integers to a Vector of points
Vector<Point> * decodePointVector(char * input)
{
    Vector<Point> * resultVector = new Vector<Point>();
    
	char * iterator = input;
    
	while(*iterator!='\0')
	{
		int x = (decode64(iterator++)<<6)+(decode64(iterator++));
		int y = (decode64(iterator++)<<6)+(decode64(iterator++));
        Point p(x,y);
        resultVector->push_back(p);
    }
    return resultVector;
}

//Converts a input with a list of integers to a Vector of points
char * encodePointVector(Vector<Point> * inputVector)
{
    char * resultStr = (char*)malloc( inputVector->size()<<2 + 1 );
    char * it = resultStr;
	
	for(Vector<Point>::iterator iterator = inputVector->begin(); iterator!= inputVector->end(); iterator++)
	{	
		*(it++) = encode64(iterator->x>>6);
		*(it++) = encode64(iterator->x&0x3F);
		*(it++) = encode64(iterator->y>>6);
		*(it++) = encode64(iterator->y&0x3F);
    }
	*it = 0;
    return resultStr;
}
