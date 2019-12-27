import socket
import sys
import threading
import base64

import win32gui

import WindowsControl
from tendo import singleton
from desktopmagic.screengrab_win32 import getRectAsImage

try:
    me = singleton.SingleInstance()
except singleton.SingleInstanceException:
    sys.exit(0)


HOST = ''
PORT = 8888


def parse_command(data):
    if data[0] == "ping":
        if data[1] == "true":
            _, _, (x, y) = win32gui.GetCursorInfo()
            image = getRectAsImage((x - 200, y - 300, x + 200, y + 300))
            image.save('im.png', format='png')

            val = ""
            with open("im.png", "rb") as imageFile:
                val += str(base64.b64encode(imageFile.read()))

            return val + ";"
        else:
            return "pong;"
    elif data[0] == "mouseClick":
        amount = 1
        btn = 'left'
        for param in data:
            if param[:7] == "amount=":
                amount = param[7:]
            elif param[:4] == "btn=":
                btn = param[4:]
        WindowsControl.click(amount=amount, btn=btn)
        return "mouse click;"
    elif data[0] == "mouseMove":
        WindowsControl.move_rel(data[1], data[2])
        return "mouse moved " + data[1] + " " + data[2] + ";"
    elif data[0] == "mouseDrag":
        btn = 'left'
        if data[1]:
            btn = data[1]
        WindowsControl.drag_start(btn=btn)
        return "mouse drag start;"
    elif data[0] == "mouseDragEnd":
        btn = 'left'
        if data[1]:
            btn = data[1]
        WindowsControl.drag_end(btn=btn)
        return "mouse drag end;"
    elif data[0] == "vscroll":
        WindowsControl.vscroll_wheel(data[1])
        return "vertical scroll;"
    elif data[0] == "hscroll":
        WindowsControl.hscroll_wheel(data[1])
        return "horizontal scroll;"
    elif data[0] == "zoom":
        WindowsControl.zoom(data[1])
        return "zoom;"
    elif data[0] == "k":
        WindowsControl.keyboard_entry(data[1])
        return "keyboard: " + data[1] + ";"
    else:
        return data[0] + " is not a recognized command;"


def client_thread(conn):
    conn.settimeout(10.0)

    while 1:
        try:
            data = conn.recv(1024).decode("utf8")
            if data:
                print(data)
                reply = parse_command(data.split())
                conn.send(str(reply).encode("utf8"))
            else:
                print("Connection Lost")
                break
        except socket.timeout:
            print("Timeout")
            break
        except ConnectionAbortedError:
            print("Connection Aborted")
            break
        except UnicodeDecodeError:
            print("Error Decoding Unicode, Connection Aborted")
            break

    conn.settimeout(None)
    conn.close()


s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
print('Socket created')

# Bind socket to local host and port
try:
    s.bind((HOST, PORT))
except socket.error as msg:
    print('Bind failed. Error Code : ' + str(msg[0]) + ' Message ' + msg[1])
    sys.exit()

print('Socket bind complete')

# Start listening on socket
s.listen(10)
print('Socket now listening')

while 1:
    (conn, addr) = s.accept()
    print('Connection from ' + str(addr[0]) + ':' + str(addr[1]))

    try:
        threading.Thread(target=client_thread,
                         args=(conn,)
                         ).start()
    except:
        print("Error Creating Thread")
