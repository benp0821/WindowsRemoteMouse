import socket
import sys
import threading
import WindowsControl
from tendo import singleton

try:
    me = singleton.SingleInstance()
except singleton.SingleInstanceException:
    sys.exit(0)


HOST = ''
PORT = 8888


def parse_command(data):
    if data[0] == "ping":
        return "still connected;"
    elif data[0] == "mouseClick":
        amount = 1
        btn = 'left'
        for param in data:
            if param[:7] == "amount=":
                amount = int(param[7:])
            elif param[:4] == "btn=":
                btn = param[4:]
        WindowsControl.click(amount=amount, btn=btn)
        return "mouse clicked " + str(amount) + ";"
    elif data[0] == "mouseMove":
        WindowsControl.move_rel(data[1], data[2])
        return "mouse moved " + data[1] + " " + data[2] + ";"
    elif data[0] == "mouseDrag":
        WindowsControl.drag_rel(data[1], data[2])
        return "mouse dragged " + data[1] + " " + data[2] + ";"
    elif data[0] == "vscroll":
        next
    elif data[0] == "hscroll":
        next
    elif data[0] == "keyPress":
        next
    elif data[0] == "keyDown":
        next
    elif data[0] == "keyUp":
        next
    else:
        return data[0] + " is not a recognized command;"
    return 0


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
