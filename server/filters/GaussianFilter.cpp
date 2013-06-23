#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"
#include <stdlib.h>
#include <stdio.h>

using namespace cv;

Mat* filter(Mat* input){
  int scale = 1;
  int delta = 0;
  int ddepth = CV_16S;


  Mat* output = new Mat(*input);
  GaussianBlur( *input, *output, Size(3,3), 0, 0, BORDER_DEFAULT );

  /// Convert it to gray
  cvtColor( *output, *output, CV_RGB2GRAY );

  /// Generate grad_x and grad_y
  Mat grad_x, grad_y;
  Mat abs_grad_x, abs_grad_y;

  /// Gradient X
  //Scharr( src_gray, grad_x, ddepth, 1, 0, scale, delta, BORDER_DEFAULT );
//  Sobel( *output, grad_x, ddepth, 1, 0, 3, scale, delta, BORDER_DEFAULT );
//  convertScaleAbs( grad_x, abs_grad_x );

  /// Gradient Y
  //Scharr( src_gray, grad_y, ddepth, 0, 1, scale, delta, BORDER_DEFAULT );
//  Sobel( *output , grad_y, ddepth, 0, 1, 3, scale, delta, BORDER_DEFAULT );
//  convertScaleAbs( grad_y, abs_grad_y );

  /// Total Gradient (approximate)
//  addWeighted( abs_grad_x, 0.5, abs_grad_y, 0.5, 0, *output );

  return output;
}

/** @function main */
int main( int argc, char** argv )
{

  Mat src;
  Mat* grad;
  char* window_name = "Sobel Demo - Simple Edge Detector";
  int c;

  /// Load an image
  src = imread( argv[1] );

  if( !src.data )
  { return -1; }

  grad = filter(&src);

  /// Create window
  namedWindow( window_name, CV_WINDOW_AUTOSIZE );


  imshow( window_name, *grad );

  waitKey(0);

  return 0;
  }
