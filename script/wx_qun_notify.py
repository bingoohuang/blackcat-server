#!/usr/bin/env python
# coding: utf-8

import wxpy
import time

from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

DIRECTORY_TO_WATCH = "./msgs"


class Watcher:
    def __init__(self):
        self.observer = Observer()

    def run(self):
        event_handler = Handler()
        self.observer.schedule(event_handler, DIRECTORY_TO_WATCH, recursive=True)
        self.observer.start()
        try:
            while True:
                time.sleep(5)
        except:
            self.observer.stop()
            print "Error"

        self.observer.join()


# 初始化机器人，扫码登陆
bot = wxpy.Bot(console_qr=True, cache_path=True)
qun = wxpy.ensure_one(bot.groups().search(u'奕起嗨运行保障群'))


def send_msg(src_path):
    with open(src_path, 'r') as msg_file:
        msg = msg_file.read()
        qun.send(msg.decode('utf-8'))


class Handler(FileSystemEventHandler):
    @staticmethod
    def on_any_event(event):
        if event.is_directory:
            return None

        elif event.event_type == 'created':
            send_msg(event.src_path)

        elif event.event_type == 'modified':
            send_msg(event.src_path)


w = Watcher()
w.run()
