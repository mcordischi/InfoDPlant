#ifdef WIN32 
	#include <winsock.h> 
#else 
	#include <sys/socket.h> 
	#include <sys/un.h> 
#endif
#include <pthread.h>
#include <stdio.h>
#include <fcntl.h>


#define MAX_CON 3
#define PORT 80
#define BUF_SIZE 10240*4
	
//----- HTTP response messages ----------------------------------------------
#define OK_IMAGE    "HTTP/1.0 200 OK\nContent-Type:image/gif\r\n\r\n"
#define UPLOAD		"<html><body><h1>Subir</h1><form method='POST' enctype='multipart/form-data'><input type='file' name='img'/><br><input type='submit'></form></body></html>\r\n\r\n"
#define OK_TEXT     "HTTP/1.0 200 OK\r\nContent-Type:text/html; charset=UTF-8\r\nTransfer-Encoding: chunked\r\nContent-Length: %d\r\nConnection: close\r\n\r\n"
#define NOTOK_404   "HTTP/1.0 404 Not Found\nContent-Type:text/html\r\n\r\n"
#define MESS_404    "<html><body><h1>FILE NOT FOUND</h1></body></html>\r\n\r\n"
	
bool stop = 0;

typedef struct
{
	unsigned int client_socket;
	char* (*proc)(char*);
} ThArgs;

void *my_thread(void * arg)
{
	unsigned int    myClient_s;         //copy socket
     
    /* other local variables ------------------------------------------------ */
	char           in_buf[BUF_SIZE];           // Input buffer for GET resquest
	char           out_buf[BUF_SIZE];          // Output buffer for HTML response
	char           *file_name;                 // File name
	unsigned int   fh;                         // File handle (file descriptor)
	unsigned int   buf_len;                    // Buffer length for file reads
	unsigned int   retcode;                    // Return code
	ThArgs		   thArgs;							// Thread argument mess
	thArgs = *(ThArgs*)arg;        // copy the socket and etc
	free(arg);
	myClient_s = thArgs.client_socket;
	/* receive the first HTTP request (HTTP GET) ------- */
	
	retcode = recv(myClient_s, in_buf, BUF_SIZE, 0);
	
	//printf(in_buf);
 
	/* if receive error --- */
	if (retcode < 0)
	{
		printf("recv error detected ...\n"); 
	}
     
	/* if HTTP command successfully received --- */
	else
	{    
		/* Parse out the filename from the GET request --- */
        char method[5];
		strncpy(method, in_buf, 4);
		method[4] = 0;
		if(0 == strcmp(method,"POST"))
		{
			char * img = "";
			if(0 == strstr(in_buf,"Content-Type: multipart/form-data;"))
			{
				img = strtok(strstr(in_buf, "\nimg=")+5, "\n");
			}else{
				// TODO: multipart/form-data
				/*
				char * begBoundary = strstr(in_buf, "boundary=")+strlen("boundary=");
				char * endBoundary = strchr(begBoundary,'\r');
				char boundary[endBoundary-begBoundary+1];
				strncpy(boundary, begBoundary, endBoundary-begBoundary);
				printf("Boundary %s\n", boundary);
				img = strstr(strstr(strstr(strstr(strstr(endBoundary, boundary),"\r\n"),"\r\n"),"\r\n"),"\r\n");
				printf("img: %d\n", img);
				char * end = strstr(img, boundary)-2;
				
				FILE * f = fopen("temp.bmp","w");
				fwrite (img , sizeof(char), end-img, f);
				fclose(f);
				*/
				
			}
			char * ans = thArgs.proc(img);
			sprintf(out_buf, OK_TEXT, strlen(ans));
			send(myClient_s, out_buf, strlen(out_buf), 0);
			strcpy(out_buf, ans);
			free(ans);
			send(myClient_s, out_buf, strlen(out_buf), 0);
			strcpy(out_buf, "\r\n\r\n\0");
			send(myClient_s, out_buf, strlen(out_buf), 0);
		}
		else
		{
			sprintf(out_buf, OK_TEXT, strlen(UPLOAD));
			send(myClient_s, out_buf, strlen(out_buf), 0);
			strcpy(out_buf, UPLOAD);
			send(myClient_s, out_buf, strlen(out_buf), 0);
		}       
	}
	close(myClient_s);
	pthread_exit(NULL);
}

void listen(char* (*proc)(char*))
{
	#ifdef WIN32
	WSADATA wsaData;
	WSAStartup( MAKEWORD( 2, 2 ), &wsaData );
	#endif

	int err = 0;
	unsigned int server_socket;
	struct sockaddr_in server_addr;
	struct sockaddr_in client_addr;
	pthread_t threads;
	pthread_attr_t attr;
		
	err = server_socket = socket(AF_INET,SOCK_STREAM,0);
	
	if (err == -1)
	{
		printf("Error creating socket: %d\n", WSAGetLastError());
		exit(0);
	}
	
	server_addr.sin_family = AF_INET;
	server_addr.sin_addr.s_addr = INADDR_ANY;
	server_addr.sin_port = htons(PORT);
	//bzero(&(server_addr.sin_zero),8); 
	
	err = bind(server_socket,(struct sockaddr *)&server_addr,sizeof(struct sockaddr));
	
	if (err == -1)
	{
		printf("Error binding: %d\n", WSAGetLastError());
		exit(0);
	}
	
	err = listen(server_socket, MAX_CON);
	if (err == -1)
	{
		printf("Error listening\n");
		exit(0);
	}
	
	pthread_attr_init(&attr);
	
	while(!stop)
	{
		int	addrlen = sizeof(struct sockaddr);
		unsigned int cli_socket = accept(server_socket, (struct sockaddr *)&client_addr, &addrlen);
		
		
		/* Create a child thread --------------------------------------- */
        ThArgs * args = (ThArgs*)malloc(sizeof(ThArgs));
		(*args).client_socket = cli_socket;
		(*args).proc = proc;
        pthread_create (&threads, &attr, my_thread, args);
	}
}