HearthMonitor
=============

A monitoring tool for the game HearthStone.

Concept
--------

The concept of this tool is to read a logfile from HearthStone and offer the ability to analyze that logfile, it also has the ability to work in real-time. For the real-time visualization there is an Android app that will visualize data in real-time.

Structure
--------

This tool is split up in multiple components, which can all be found in their own libraries.

1. **The log API**: A format that describes how the log entries look like and how they are stored. It can be found at https://github.com/skiwi2/HearthMonitor-LogAPI.
2. **The log reader implementation**: A log reader implementation that can be used to read logfiles with. It can be found at https://github.com/skiwi2/HearthMonitor-LogReader.
3. **The Android app**: An Android app to visualize data from the logfiles. It can be found at https://github.com/skiwi2/HearthMonitor-AndroidApp.
4. **The tool**: The tool itself can be found in this repository. It will read the logfiles and offer ways to communicate with the apps.