import pyautogui
import win32api
import win32con
import win32gui
import ctypes


def click(amount=1, btn='left'):
    pyautogui.click(clicks=amount, button=btn)


def move_rel(x, y):
    (c_x, c_y) = win32gui.GetCursorPos()
    ctypes.windll.user32.SetCursorPos(c_x + int(x), c_y + int(y))


def drag_rel(x, y):
    pyautogui.mouseDown()
    (c_x, c_y) = win32gui.GetCursorPos()
    ctypes.windll.user32.SetCursorPos(c_x + int(x), c_y + int(y))


def vscroll_wheel(scroll=-4):
    pyautogui.scroll(scroll)


def hscroll_wheel(scroll=-4):
    pyautogui.hscroll(scroll)


def key_press(key):
    pyautogui.press(key)


def key_down(key):
    pyautogui.keyDown(key)


def key_up(key):
    pyautogui.keyUp(key)
