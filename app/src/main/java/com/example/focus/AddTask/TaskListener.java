package com.example.focus.AddTask;

public interface TaskListener {
    void onTaskAdded(String title, String priority, String tag);
}