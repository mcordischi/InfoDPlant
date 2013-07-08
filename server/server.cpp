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
#define BUF_SIZE 2048
	
//----- HTTP response messages ----------------------------------------------
#define OK_IMAGE    "HTTP/1.0 200 OK\nContent-Type:image/gif\n\n"
#define OK_TEXT     "HTTP/1.0 200 OK\nContent-Type:text/html\n\n"
#define NOTOK_404   "HTTP/1.0 404 Not Found\nContent-Type:text/html\n\n"
#define MESS_404    "<html><body><h1>FILE NOT FOUND</h1></body></html>\n\n\n\0"
	
bool stop = 0;

void *my_thread(void * arg)
{
	unsigned int    myClient_s;         //copy socket
     
    /* other local variables ------------------------------------------------ */
	char           in_buf[BUF_SIZE];           // Input buffer for GET resquest
	char           out_buf[BUF_SIZE];          // Output buffer for HTML response
	char           *file_name;                 // File name
	char           *method;                 	// Method
	unsigned int   fh;                         // File handle (file descriptor)
	unsigned int   buf_len;                    // Buffer length for file reads
	unsigned int   retcode;                    // Return code
 
	myClient_s = *(unsigned int *)arg;        // copy the socket
 
	/* receive the first HTTP request (HTTP GET) ------- */
	retcode = recv(myClient_s, in_buf, BUF_SIZE, 0);
	
	printf(in_buf);
 
	/* if receive error --- */
	if (retcode < 0)
	{
		printf("recv error detected ...\n"); 
	}
     
	/* if HTTP command successfully received --- */
	else
	{    
		/* Parse out the filename from the GET request --- */
        method = strtok(in_buf, " ");
		if(0 == strcmp(method, "POST"))
		{
		}
		else
		{
			strcpy(out_buf, MESS_404);
			send(myClient_s, out_buf, strlen(out_buf), 0);
		}       
	}
	close(myClient_s);
	pthread_exit(NULL);
}

void listen()
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
        unsigned int * ids = (unsigned int*)malloc(sizeof(unsigned int));
		*ids = cli_socket;
        pthread_create (&threads, &attr, my_thread, ids);
	}
}