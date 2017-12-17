// MoveMouseTest.cpp : Defines the entry point for the console application.
//

#undef UNICODE

#include "stdafx.h"

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif

#include <windows.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <stdlib.h>
#include <stdio.h>
#include <string>

#pragma comment (lib, "Ws2_32.lib")

#define DEFAULT_BUFLEN 32
#define DEFAULT_PORT "27015"

void mouseClick() {
	mouse_event(MOUSEEVENTF_LEFTDOWN | MOUSEEVENTF_LEFTUP, 0, 0, 0, 0);
}

void rightClick() {
	mouse_event(MOUSEEVENTF_RIGHTDOWN | MOUSEEVENTF_RIGHTUP, 0, 0, 0, 0);
}

void doubleClick() {
	mouse_event(MOUSEEVENTF_LEFTDOWN | MOUSEEVENTF_LEFTUP, 0, 0, 0, 0);
	mouse_event(MOUSEEVENTF_LEFTDOWN | MOUSEEVENTF_LEFTUP, 0, 0, 0, 0);
}

void mouseDragStart() {
	mouse_event(MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0);
}

void mouseDragEnd() {
	mouse_event(MOUSEEVENTF_LEFTUP, 0, 0, 0, 0);
}


void mouseMove(std::string xAmt, std::string yAmt) {
	POINT p;

	if (GetCursorPos(&p)) {
		int resultX, resultY;
		int directionX = 1, directionY = 1;
		if (sscanf_s(xAmt.c_str(), "%d", &resultX) == 1) {
			if (resultX < 0) {
				directionX = -1;
			}
			resultX = abs(resultX);
		}
		if (sscanf_s(yAmt.c_str(), "%d", &resultY) == 1) {
			if (resultY < 0) {
				directionY = -1;
			}
			resultY = abs(resultY);
		}

		int counterX = 0, counterY = 0;
		while (counterX < resultX || counterY < resultY) {
			if (counterX < resultX) {
				if (counterY < resultY) {
					SetCursorPos(p.x += 1 * directionX, p.y += 1 * directionY);
					counterY++;
				}
				else {
					SetCursorPos(p.x += 1 * directionX, p.y);
				}
				counterX++;
			}else if (counterY < resultY) {
				if (counterX < resultX) {
					SetCursorPos(p.x += 1 * directionX, p.y += 1 * directionY);
					counterX++;
				}
				else {
					SetCursorPos(p.x, p.y += 1 * directionY);
				}
				counterY++;
			}
			
			Sleep(1);
		}
	}
}

int main(void)
{
	ShowWindow(GetConsoleWindow(), SW_HIDE);

	while (true) {
		WSADATA wsaData;

		SOCKET ListenSocket = INVALID_SOCKET;
		SOCKET ClientSocket = INVALID_SOCKET;

		struct addrinfo *result = NULL;
		struct addrinfo hints;

		int iSendResult;
		char recvbuf[DEFAULT_BUFLEN];
		int recvbuflen = DEFAULT_BUFLEN;

		// Initialize Winsock
		if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
			printf("WSAStartup failed with error: %d\n", WSAGetLastError());
			return 1;
		}

		ZeroMemory(&hints, sizeof(hints));
		hints.ai_family = AF_INET;
		hints.ai_socktype = SOCK_STREAM;
		hints.ai_protocol = IPPROTO_TCP;
		hints.ai_flags = AI_PASSIVE;

		// Resolve the server address and port
		if (getaddrinfo(NULL, DEFAULT_PORT, &hints, &result) != 0) {
			printf("getaddrinfo failed with error: %d\n", WSAGetLastError());
			WSACleanup();
			return 1;
		}

	
		// Create a SOCKET for connecting to server
		ListenSocket = socket(result->ai_family, result->ai_socktype, result->ai_protocol);
		if (ListenSocket == INVALID_SOCKET) {
			printf("socket failed with error: %ld\n", WSAGetLastError());
			freeaddrinfo(result);
			WSACleanup();
			return 1;
		}

		// Setup the TCP listening socket
		if (bind(ListenSocket, result->ai_addr, (int)result->ai_addrlen) == SOCKET_ERROR) {
			printf("bind failed with error: %d\n", WSAGetLastError());
			freeaddrinfo(result);
			closesocket(ListenSocket);
			WSACleanup();
			return 1;
		}

		freeaddrinfo(result);

		if (listen(ListenSocket, SOMAXCONN) == SOCKET_ERROR) {
			printf("listen failed with error: %d\n", WSAGetLastError());
			closesocket(ListenSocket);
			WSACleanup();
			return 1;
		}

		// Accept a client socket
		ClientSocket = accept(ListenSocket, NULL, NULL);
		if (ClientSocket == INVALID_SOCKET) {
			printf("accept failed with error: %d\n", WSAGetLastError());
			closesocket(ListenSocket);
			WSACleanup();
			return 1;
		}

		// No longer need server socket
		closesocket(ListenSocket);

		// Receive until the peer shuts down the connection
		int len;
		do {

			len = recv(ClientSocket, recvbuf, recvbuflen, 0);
			if (len > 0) {
				std::string tempX = "", tempY = "";
				if (recvbuf[0] == 'c') {
					mouseClick();
				}
				else if (recvbuf[0] == 'r') {
					rightClick();
				}
				else if (recvbuf[0] == 'x') {
					doubleClick();
				}
				else if (recvbuf[0] == 'd') {
					mouseDragStart();
				}
				else if (recvbuf[0] == 'e') {
					mouseDragEnd();
				}
				else {
					int counter = 0;
					while (recvbuf[counter] != ' ') {
						tempX += recvbuf[counter];
						counter++;
					}
					counter++;
					while (recvbuf[counter] != ';') {
						tempY += recvbuf[counter];
						counter++;
					}
					printf("x: %s y: %s\n", tempX.c_str(), tempY.c_str());
					mouseMove(tempX, tempY);
					
				}
			}
			else if (len == 0)
				printf("Connection closing...\n");
			else {
				printf("recv failed with error: %d\n", WSAGetLastError());
				closesocket(ClientSocket);
				WSACleanup();
				return 1;
			}

		} while (len > 0);

		int err = (shutdown(ClientSocket, SD_SEND) == SOCKET_ERROR) ? 1 : 0;
		if (err) {
			printf("shutdown failed with error: %d\n", WSAGetLastError());
		}
		closesocket(ClientSocket);
		WSACleanup();
	}
}