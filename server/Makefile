CV_LIBS :=  -llibopencv_calib3d246 -lopencv_features2d246 -lopencv_flann246 -lopencv_imgproc246 -lopencv_core246 -lopencv_highgui246

ifeq ($(OS),Windows_NT)
	WS_LIBS := -lws2_32
	CV_INCLUDE := -I"c:\opencv\build\include"
else
endif

main.o: main.cpp
	g++ $(CV_INCLUDE) -c main.cpp 
	
process.o: process.cpp
	g++ $(CV_INCLUDE) -c process.cpp 

server.o: server.cpp
	g++ $(INCLUDE) -c server.cpp -std=gnu++0x

main.exe: main.o process.o server.o
	g++ *.o -L"C:\opencv\build\x86\mingw\lib" $(CV_LIBS) $(WS_LIBS) -o main.exe
	
all: main.exe