#!/usr/bin/env python
# coding: utf-8

import wxpy
import time
import os

import sys

from optparse import OptionParser
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

reload(sys)
if sys.getdefaultencoding() != 'utf-8':  # maybe ascii
    sys.setdefaultencoding('utf-8')  # 解决parser help中的中文显示问题

parser = OptionParser()
parser.add_option("-p", "--path", dest="msg_path", default='~/.qun_msg/',
                  help="messages' path, default ~/.qun_msg/", metavar="Path")
parser.add_option("-q", "--qun", dest="qun_name", default='奕起嗨运行保障群',
                  help='wx qun name, default 奕起嗨运行保障群', metavar="QunName")

(options, args) = parser.parse_args()
options.msg_path = os.path.expanduser(options.msg_path)

print('qun_name:' + options.qun_name)
print('msg_path:' + options.msg_path)

# 初始化机器人，扫码登陆
bot = wxpy.Bot(console_qr=True, cache_path=True)
qun = wxpy.ensure_one(bot.groups().search(options.qun_name.decode('utf-8')))


def send_msg(msg_file):
    with open(msg_file, 'r') as file_content:
        msg = file_content.read()
        qun.send(msg.decode('utf-8'))
    os.remove(msg_file)


for subdir, dirs, files in os.walk(options.msg_path):
    for f in files:
        send_msg(subdir + f)


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

        elif event.event_type == 'modified':
            send_msg(event_src_path)


watcher = Watcher()
watcher.run()
bot.join()
