import pyautogui
import win32api
import win32gui
import ctypes
from win32con import *


def click(amount=1, btn='left'):
    pyautogui.click(clicks=amount, button=btn)


def move_rel(x, y):
    (c_x, c_y) = win32gui.GetCursorPos()
    ctypes.windll.user32.SetCursorPos(c_x + int(x), c_y + int(y))


def drag_rel():
    pyautogui.mouseDown()


def vscroll_wheel(scroll=-4):
    x, y = win32api.GetCursorPos();
    win32api.mouse_event(MOUSEEVENTF_WHEEL, x, y, int(scroll), 0)


def hscroll_wheel(scroll=-4):
    x, y = win32api.GetCursorPos();
    win32api.mouse_event(0x01000, x, y, int(scroll), 0) #MOUSEEVENTF_HWHEEL


def key_press(key):
    pyautogui.press(key)


def key_down(key):
    pyautogui.keyDown(key)


def key_up(key):
    pyautogui.keyUp(key)
