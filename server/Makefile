CV_LIBS :=  -llibopencv_calib3d246 -lopencv_features2d246 -lopencv_flann246 -lopencv_imgproc246 -lopencv_core246 -lopencv_highgui246

ifeq ($(OS),Windows_NT)
	WS_LIBS := -lws2_32
	TH_LIBS := -llibpthread
	CV_INCLUDE := -I"c:\opencv\build\include"
else
endif
	
LIBS := $(CV_LIBS) $(WS_LIBS) $(TH_LIBS)
	
all: main.exe

main.o: main.cpp
	g++ $(CV_INCLUDE) -c main.cpp 
	
process.o: process.cpp
	g++ $(CV_INCLUDE) -c process.cpp 

server.o: server.cpp
	g++ $(INCLUDE) -c server.cpp -std=gnu++0x
	
utils.o: utils.cpp
	g++ $(CV_INCLUDE) -c utils.cpp
	
utils.exe: utils.o
	g++ utils.o -L"C:\opencv\build\x86\mingw\lib" $(LIBS) -o utils.exe

main.exe: main.o process.o server.o utils.o
	g++ *.o -L"C:\opencv\build\x86\mingw\lib" $(LIBS) -o main.exe
