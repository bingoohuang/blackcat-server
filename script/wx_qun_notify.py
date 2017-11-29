#!/usr/bin/env python
# coding: utf-8

import wxpy
import time
import os

from optparse import OptionParser
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

user_home = os.path.expanduser("~")

parser = OptionParser()
parser.add_option("-p", "--path", dest="msg_path", default=user_home + '/.qun_msg/',
                  help="read message files from the path", metavar="Path")
parser.add_option("-q", "--qun", dest="qun_name", default='奕起嗨运行保障群',
                  help="wx qun name", metavar="QunName")

(options, args) = parser.parse_args()

print('qun_name:' + options.qun_name)
print('msg_path:' + options.msg_path)

# 初始化机器人，扫码登陆
bot = wxpy.Bot(console_qr=True, cache_path=True)
qun = wxpy.ensure_one(bot.groups().search(options.qun_name.decode('utf-8')))


def send_msg(src_path):
    with open(src_path, 'r') as msg_file:
        msg = msg_file.read()
        qun.send(msg.decode('utf-8'))


class Watcher:
    def __init__(self):
        self.observer = Observer()
        if not os.path.exists(options.msg_path):
            os.makedirs(options.msg_path)

    def run(self):
        event_handler = Handler()
        self.observer.schedule(event_handler, options.msg_path, recursive=True)
        self.observer.start()
        try:
            while True:
                time.sleep(5)
        except:
            self.observer.stop()
            print "Error"

        self.observer.join()


class Handler(FileSystemEventHandler):
    @staticmethod
    def on_any_event(event):
        event_src_path = event.src_path
        if event.is_directory:
            return None

        elif event.event_type == 'created':
            send_msg(event_src_path)
            os.remove(event_src_path)

        elif event.event_type == 'modified':
            send_msg(event_src_path)
            os.remove(event_src_path)


watcher = Watcher()
watcher.run()
bot.join()
