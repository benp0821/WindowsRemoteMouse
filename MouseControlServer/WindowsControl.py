import pyautogui
import win32api
import win32gui
import ctypes
import win32clipboard
from win32con import *
from pynput.keyboard import Key, Controller


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
    win32api.mouse_event(0x01000, x, y, int(scroll), 0)  # MOUSEEVENTF_HWHEEL


def keyboard_entry(phrase):
    keyboard = Controller()

    if phrase == "\\t":
        keyboard.press('\t')
        return
    elif phrase == "\\s":
        keyboard.press(' ')
        return
    elif phrase == "\\n":
        keyboard.press('\n')
        return
    elif phrase == "\\b":
        keyboard.press(Key.backspace)
        return
    elif phrase == "\\l":
        keyboard.press(Key.left)
        return
    elif phrase == "\\r":
        keyboard.press(Key.right)
        return
    elif phrase == "\\u":
        keyboard.press(Key.up)
        return
    elif phrase == "\\d":
        keyboard.press(Key.down)
        return
    # pyautogui can't handle shift + arrow combinations, as it requires the KEYEVENTF_EXTENDEDKEY flag
    elif phrase == "\\hl":
        ctypes.windll.user32.keybd_event(VK_SHIFT, 0, 0, 0)
        ctypes.windll.user32.keybd_event(VK_LEFT, 0, 1, 0)
        ctypes.windll.user32.keybd_event(VK_LEFT, 0, (1 | 2), 0)
        ctypes.windll.user32.keybd_event(VK_SHIFT, 0, 2, 0)
        return
    elif phrase == "\\hr":
        ctypes.windll.user32.keybd_event(VK_SHIFT, 0, 0, 0)
        ctypes.windll.user32.keybd_event(VK_RIGHT, 0, 1, 0)
        ctypes.windll.user32.keybd_event(VK_RIGHT, 0, (1 | 2), 0)
        ctypes.windll.user32.keybd_event(VK_SHIFT, 0, 2, 0)
        return
    elif phrase == "\\hu":
        ctypes.windll.user32.keybd_event(VK_SHIFT, 0, 0, 0)
        ctypes.windll.user32.keybd_event(VK_UP, 0, 1, 0)
        ctypes.windll.user32.keybd_event(VK_UP, 0, (1 | 2), 0)
        ctypes.windll.user32.keybd_event(VK_SHIFT, 0, 2, 0)
        return
    elif phrase == "\\hd":
        ctypes.windll.user32.keybd_event(VK_SHIFT, 0, 0, 0)
        ctypes.windll.user32.keybd_event(VK_DOWN, 0, 1, 0)
        ctypes.windll.user32.keybd_event(VK_DOWN, 0, (1 | 2), 0)
        ctypes.windll.user32.keybd_event(VK_SHIFT, 0, 2, 0)
        return
    elif phrase == "\\ha":
        pyautogui.hotkey("ctrl", "a")
        return

    try:
        keyboard.type(phrase)
    except ctypes.ArgumentError:
        # Workaround to get emojis to be handled correctly
        win32clipboard.OpenClipboard()
        data = win32clipboard.GetClipboardData()
        win32clipboard.EmptyClipboard()
        win32clipboard.SetClipboardData(CF_UNICODETEXT, phrase)
        win32clipboard.CloseClipboard()

        pyautogui.hotkey("ctrl", "v")

        win32clipboard.OpenClipboard()
        win32clipboard.EmptyClipboard()
        win32clipboard.SetClipboardData(CF_UNICODETEXT, data)
        win32clipboard.CloseClipboard()
