#include <opencv\opencv.hpp>
#include <vector>

using namespace cv;
using namespace std;

//Converts a input with a list of integers to a Vector of points
Vector<Point>* decodePointVector(char* input, int sizeOfInput){
    Vector<Point>* resultVector = new Vector<Point>();
    int* iterator = (int*)input;
    int i=0;
    for(;i<sizeOfInput;i+=2){
        Point p(input,++input);
        resultVector.add(p);
    }
    return resultVector;
}


int main( int argc, char** argv ){
    int sizeOfNumbers = 10;
    int numbers[sizeOfNumbers];

    for (int i=0 ; i<sizeOfNumbers;i++){
        numbers[i] = i;
    }

    char* charPtr = (char*) numbers;

    Vector<Point>* vector = decodePointVector(charPtr,sizeOfInput);

    for (int i=0; i< vector->size();i++){
        Point p = vector->get(i);
        cout << p.x << " " << p.y << "\n" ;
    }


}

