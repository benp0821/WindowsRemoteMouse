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
#include <shellapi.h>
#include <tchar.h>  
#include "resource.h"

#pragma comment (lib, "Ws2_32.lib")

#define DEFAULT_BUFLEN 200
#define DEFAULT_PORT "27015"

#define WM_MYMESSAGE (WM_USER + 1)
#define ID_TRAY_EXIT_CONTEXT_MENU_ITEM  3000

static TCHAR szWindowClass[] = _T("win32app");
static TCHAR szTitle[] = _T("");

HMENU rightClickMenu;

void mouseClick() {
	mouse_event(MOUSEEVENTF_LEFTDOWN | MOUSEEVENTF_LEFTUP, 0, 0, 0, 0);
}

void scrollDown() {
	POINT P;
	HWND Handle;
	GetCursorPos(&P);
	Handle = WindowFromPoint(P);

	LPARAM lParam = MAKELPARAM(P.x, P.y);
	PostMessage(Handle, WM_MOUSEWHEEL, 0xFF880000, lParam);
	PostMessage(Handle, WM_MOUSEWHEEL, 0xFF880000, lParam);
	PostMessage(Handle, WM_VSCROLL, SB_LINEDOWN, 0);
	PostMessage(Handle, WM_VSCROLL, SB_LINEDOWN, 0);
}

void scrollUp() {
	POINT P;
	HWND Handle;
	GetCursorPos(&P);
	Handle = WindowFromPoint(P);

	LPARAM lParam = MAKELPARAM(P.x, P.y);
	PostMessage(Handle, WM_MOUSEWHEEL, 0x00780000, lParam);
	PostMessage(Handle, WM_MOUSEWHEEL, 0x00780000, lParam);
	PostMessage(Handle, WM_VSCROLL, SB_LINEUP, 0);
	PostMessage(Handle, WM_VSCROLL, SB_LINEUP, 0);
}

void scrollLeft() {
	POINT P;
	HWND Handle;
	GetCursorPos(&P);
	Handle = WindowFromPoint(P);

	LPARAM lParam = MAKELPARAM(P.x, P.y);
	PostMessage(Handle, WM_MOUSEHWHEEL, 0xFF880000, lParam);
	PostMessage(Handle, WM_MOUSEHWHEEL, 0xFF880000, lParam);
	PostMessage(Handle, WM_HSCROLL, SB_LINEUP, 0);
	PostMessage(Handle, WM_HSCROLL, SB_LINEUP, 0);
}

void scrollRight() {
	POINT P;
	HWND Handle;
	GetCursorPos(&P);
	Handle = WindowFromPoint(P);

	LPARAM lParam = MAKELPARAM(P.x, P.y);
	PostMessage(Handle, WM_MOUSEHWHEEL, 0x00780000, lParam);
	PostMessage(Handle, WM_MOUSEHWHEEL, 0x00780000, lParam);
	PostMessage(Handle, WM_HSCROLL, SB_LINEDOWN, 0);
	PostMessage(Handle, WM_HSCROLL, SB_LINEDOWN, 0);
}

void printBufferToActiveWindow(std::string message) {
	INPUT ip;
	ip.type = INPUT_KEYBOARD;
	ip.ki.time = 0;
	ip.ki.wVk = 0;
	ip.ki.dwExtraInfo = 0;
	for (int i = 0; i < message.length(); i++) {
		if (message[i] == '\\') {
			if (message[i + 1] == 'n') {
				ip.ki.wVk = VK_RETURN;
				ip.ki.wScan = MapVirtualKeyEx(VK_RETURN, 0, GetKeyboardLayout(0));
				ip.ki.dwFlags = KEYEVENTF_EXTENDEDKEY; 
				i++;
			}
			else if (message[i + 1] == 't') {
				ip.ki.wVk = VK_TAB;
				ip.ki.wScan = 0;
				ip.ki.dwFlags = 0;
				i++;

			}
			else if (message[i + 1] == 'b'){
				ip.ki.wVk = VK_BACK;
				ip.ki.wScan = MapVirtualKeyEx(VK_BACK, 0, GetKeyboardLayout(0));
				ip.ki.dwFlags = KEYEVENTF_EXTENDEDKEY; 
				i++;
			}
			else if (message[i + 1] == 'l') {
				ip.ki.wVk = VK_LEFT;
				ip.ki.wScan = MapVirtualKeyEx(VK_LEFT, 0, GetKeyboardLayout(0));
				ip.ki.dwFlags = KEYEVENTF_EXTENDEDKEY;
				i++;
			}
			else if (message[i + 1] == 'r') {
				ip.ki.wVk = VK_RIGHT;
				ip.ki.wScan = MapVirtualKeyEx(VK_RIGHT, 0, GetKeyboardLayout(0));
				ip.ki.dwFlags = KEYEVENTF_EXTENDEDKEY;
				i++;
			}
			else if (message[i + 1] == 'u') {
				ip.ki.wVk = VK_UP;
				ip.ki.wScan = MapVirtualKeyEx(VK_UP, 0, GetKeyboardLayout(0));
				ip.ki.dwFlags = KEYEVENTF_EXTENDEDKEY;
				i++;
			}
			else if (message[i + 1] == 'd') {
				ip.ki.wVk = VK_DOWN;
				ip.ki.wScan = MapVirtualKeyEx(VK_DOWN, 0, GetKeyboardLayout(0));
				ip.ki.dwFlags = KEYEVENTF_EXTENDEDKEY;
				i++;
			}
			else {
				ip.ki.dwFlags = KEYEVENTF_UNICODE;
				ip.ki.wScan = message[i];
			}
		}
		else {
			ip.ki.dwFlags = KEYEVENTF_UNICODE;
			ip.ki.wScan = message[i];
		}

		SendInput(1, &ip, sizeof(INPUT));

		ip.ki.dwFlags |= KEYEVENTF_KEYUP;
		SendInput(1, &ip, sizeof(INPUT));
	}
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
		int directionX = 0, directionY = 0;
		if (sscanf_s(xAmt.c_str(), "%d", &resultX) == 1) {
			if (abs(resultX) > 5) {
				directionX = resultX / 5;
			}
		}
		if (sscanf_s(yAmt.c_str(), "%d", &resultY) == 1) {
			if (abs(resultY) > 5) {
				directionY = resultY / 5;
			}
		}

		for (int i = 0; i < 5; i++) {
			SetCursorPos(p.x += directionX, p.y += directionY);
			Sleep(5);
		}

		/*while (i < abs(directionX) || i < abs(directionY)) {
			if (i < abs(directionX) && i < abs(directionY)) {
				SetCursorPos(p.x += directionX, p.y += directionY);
			}
			else if (i < abs(directionX)) {
				SetCursorPos(p.x += directionX, p.y);
			}
			else if (i < abs(directionY)) {
				SetCursorPos(p.x, p.y += directionY);
			}
			i++;
			Sleep(1);
		}*/
	}
}

DWORD WINAPI logicThread(__in LPVOID lpParameter)
{
	while (true) {
		WSADATA wsaData;

		SOCKET ListenSocket = INVALID_SOCKET;
		SOCKET ClientSocket = INVALID_SOCKET;
		int SOCKET_READ_TIMEOUT_SEC = 10;

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
		printf("Connection open\n");
		int len;
		do {
			fd_set set;
			struct timeval timeout;
			FD_ZERO(&set);
			FD_SET(ClientSocket, &set);
			timeout.tv_sec = SOCKET_READ_TIMEOUT_SEC;
			timeout.tv_usec = 0;
			int rv = select(ClientSocket + 1, &set, NULL, NULL, &timeout);
			if (rv == SOCKET_ERROR)
			{
				printf("recv failed with error: %d\n", WSAGetLastError());
				closesocket(ClientSocket);
				WSACleanup();
				return 1;
			}
			else if (rv == 0)
			{
				printf("timeout\n");
				len = 0;
			}
			else
			{
				len = recv(ClientSocket, recvbuf, recvbuflen, 0);
				if (len > 0) {
					std::string tempX = "", tempY = "";
					if (recvbuf[0] == 'g') {
						printf("still connected\n");
					}
					else if (recvbuf[0] == 's') {
						if (recvbuf[1] == 'd') {
							scrollDown();
						}
						else if (recvbuf[1] == 'u') {
							scrollUp();
						}
						else if (recvbuf[1] == 'l') {
							scrollLeft();
						}
						else if (recvbuf[1] == 'r') {
							scrollRight();
						}
					}
					else if (recvbuf[0] == 'c') {
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
					else if (recvbuf[0] == 'k') {
						int counter = 1;
						std::string message = "";
						while (counter < len) {
							message += recvbuf[counter];
							counter++;
						}
						printBufferToActiveWindow(message);
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
			}
		} while (len > 0);

		int err = (shutdown(ClientSocket, SD_SEND) == SOCKET_ERROR) ? 1 : 0;
		if (err) {
			printf("shutdown failed with error: %d\n", WSAGetLastError());
		}
		printf("Connection closed\n");
		closesocket(ClientSocket);
		WSACleanup();
	}
	return 0;
}

LRESULT CALLBACK WndProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
	switch (msg)
	{
		case WM_CREATE:
			rightClickMenu = CreatePopupMenu();
			AppendMenu(rightClickMenu, MF_STRING, ID_TRAY_EXIT_CONTEXT_MENU_ITEM, TEXT("Exit"));
		break;
		case WM_MYMESSAGE:
			switch (lParam)
			{
				case WM_LBUTTONDBLCLK:
					MessageBox(NULL, L"Settings Window Not Yet Implemented", L"Not Implemented", MB_OK);
				break;
				case WM_RBUTTONDOWN:
				{
					POINT curPoint;
					GetCursorPos(&curPoint);

					SetForegroundWindow(hWnd);

					UINT clicked = TrackPopupMenu(
						rightClickMenu,
						TPM_RETURNCMD | TPM_NONOTIFY,
						curPoint.x,
						curPoint.y,
						0,
						hWnd,
						NULL
					);

					if (clicked == ID_TRAY_EXIT_CONTEXT_MENU_ITEM)
					{
						PostQuitMessage(0);
					}
				}
				break;
				case WM_DESTROY:
					PostQuitMessage(0);
				break;
			};
		break;
	};
	
	return DefWindowProc(hWnd, msg, wParam, lParam);
}

int WINAPI wWinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, PWSTR pCmdLine, int nCmdShow)
{
	WNDCLASSEX wcex;

	wcex.cbSize = sizeof(WNDCLASSEX);
	wcex.style = CS_HREDRAW | CS_VREDRAW;
	wcex.lpfnWndProc = WndProc;
	wcex.cbClsExtra = 0;
	wcex.cbWndExtra = 0;
	wcex.hInstance = hInstance;
	wcex.hIcon = NULL;
	wcex.hCursor = LoadCursor(NULL, IDC_ARROW);
	wcex.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
	wcex.lpszMenuName = NULL;
	wcex.lpszClassName = szWindowClass;
	wcex.hIconSm = NULL;

	if (!RegisterClassEx(&wcex))
	{
		return 1;
	}

	HWND hWnd = CreateWindow(
		szWindowClass,
		szTitle,
		WS_OVERLAPPEDWINDOW,
		CW_USEDEFAULT, CW_USEDEFAULT,
		0, 0,
		NULL,
		NULL,
		hInstance,
		NULL
	);
	if (!hWnd)
	{
		return 1;
	}

	NOTIFYICONDATA nid;
	ZeroMemory(&nid, sizeof(nid));
	nid.cbSize = sizeof(NOTIFYICONDATA);
	nid.hWnd = hWnd;
	nid.uID = 100;
	nid.uVersion = NOTIFYICON_VERSION;
	nid.uCallbackMessage = WM_MYMESSAGE;
	nid.hIcon = (HICON)LoadImage(GetModuleHandle(NULL), MAKEINTRESOURCE("resources\\mouse_icon.png"), IMAGE_ICON, 16, 16, 0);
	wcscpy_s(nid.szTip, L"Android Mouse Server");
	nid.uFlags = NIF_MESSAGE | NIF_ICON | NIF_TIP;

	Shell_NotifyIcon(NIM_ADD, &nid);

	HANDLE threadHandle;
	DWORD threadid;
	threadHandle = CreateThread(0, 0, logicThread, 0, 0, &threadid);

	MSG msg;
	while (GetMessage(&msg, NULL, 0, 0))
	{
		TranslateMessage(&msg);
		DispatchMessage(&msg);
	}

	return 0;
}