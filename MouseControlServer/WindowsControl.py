from ctypes import windll, byref, ArgumentError
from ctypes.wintypes import POINT
from time import sleep

import win32clipboard
from pynput.keyboard import Controller
from win32con import *


def key_press(keys):
    for key in keys:
        windll.user32.keybd_event(key, 0, KEYEVENTF_EXTENDEDKEY, 0)

    for key in reversed(keys):
        windll.user32.keybd_event(key, 0, (KEYEVENTF_EXTENDEDKEY | KEYEVENTF_KEYUP), 0)


def click(amount=1, btn='left'):
    for i in range(0, int(amount)):
        drag_start(btn=btn)
        drag_end(btn=btn)


def move_rel(x, y):
    pt = POINT()
    windll.user32.GetCursorPos(byref(pt))
    windll.user32.SetCursorPos(pt.x + int(x), pt.y + int(y))


def drag_start(btn='left'):
    pt = POINT()
    windll.user32.GetCursorPos(byref(pt))
    if btn == 'left':
        windll.user32.mouse_event(MOUSEEVENTF_LEFTDOWN, pt.x, pt.y, 0, 0)
    elif btn == 'right':
        windll.user32.mouse_event(MOUSEEVENTF_RIGHTDOWN, pt.x, pt.y, 0, 0)
    elif btn == 'middle':
        windll.user32.mouse_event(MOUSEEVENTF_MIDDLEDOWN, pt.x, pt.y, 0, 0)


def drag_end(btn='left'):
    pt = POINT()
    windll.user32.GetCursorPos(byref(pt))
    if btn == 'left':
        windll.user32.mouse_event(MOUSEEVENTF_LEFTUP, pt.x, pt.y, 0, 0)
    elif btn == 'right':
        windll.user32.mouse_event(MOUSEEVENTF_RIGHTUP, pt.x, pt.y, 0, 0)
    elif btn == 'middle':
        windll.user32.mouse_event(MOUSEEVENTF_MIDDLEUP, pt.x, pt.y, 0, 0)


def vscroll_wheel(scroll=-4):
    pt = POINT()
    windll.user32.GetCursorPos(byref(pt))
    windll.user32.mouse_event(MOUSEEVENTF_WHEEL, pt.x, pt.y, int(scroll), 0)


def hscroll_wheel(scroll=-4):
    pt = POINT()
    windll.user32.GetCursorPos(byref(pt))
    windll.user32.mouse_event(0x01000, pt.x, pt.y, int(scroll), 0)  # MOUSEEVENTF_HWHEEL


def zoom(direction):
    if direction == "out":
        key_press([VK_CONTROL, 0xBD])  # MINUS
    else:
        key_press([VK_CONTROL, 0xBB])  # PLUS


def keyboard_entry(phrase):
    keyboard = Controller()

    phrase = phrase.replace("\\t", "\t")
    phrase = phrase.replace("\\s", " ")

    if phrase == "\\n":
        key_press([VK_RETURN])
        return
    elif phrase == "\\l":
        key_press([VK_LEFT])
        return
    elif phrase == "\\r":
        key_press([VK_RIGHT])
        return
    elif phrase == "\\u":
        key_press([VK_UP])
        return
    elif phrase == "\\d":
        key_press([VK_DOWN])
        return
    elif phrase == "\\hl":
        key_press([VK_SHIFT, VK_LEFT])
        return
    elif phrase == "\\hr":
        key_press([VK_SHIFT, VK_RIGHT])
        return
    elif phrase == "\\hu":
        key_press([VK_SHIFT, VK_UP])
        return
    elif phrase == "\\hd":
        key_press([VK_SHIFT, VK_DOWN])
        return
    elif phrase == "\\ha":
        key_press([VK_CONTROL, 0x41])  # A
        return

    try:
        i = 0
        while i < len(phrase):
            if i < len(phrase) - 1 and phrase[i] == "\\" and phrase[i+1] == "b":
                key_press([VK_BACK])
                i += 1
            else:
                keyboard.press(phrase[i])
                keyboard.release(phrase[i])
            i += 1
    except ArgumentError:
        # Workaround to get emojis to be handled correctly
        win32clipboard.OpenClipboard()
        data = win32clipboard.GetClipboardData()
        win32clipboard.EmptyClipboard()
        win32clipboard.SetClipboardData(CF_UNICODETEXT, phrase)
        win32clipboard.CloseClipboard()

        key_press([VK_CONTROL, 0x56])  # V
        sleep(0.1)

        win32clipboard.OpenClipboard()
        win32clipboard.EmptyClipboard()
        win32clipboard.SetClipboardData(CF_UNICODETEXT, data)
        win32clipboard.CloseClipboard()
