package com.neo.caption.ocr.util;

import javafx.concurrent.Task;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Order:
 * call -> scheduled -> running -> updateValue... -> success (When a exception is throwing, still goto updateValue)
 * call -> scheduled -> running -> cancel -> cancelled
 */
@Accessors(chain = true)
@Setter
@Slf4j
public class AsyncTask extends Task<Integer> {

    private TaskListen taskListen;

    @Override
    protected Integer call() {
        if (taskListen != null) {
            taskListen.onPreExecute();
            return taskListen.call();
        }
        return null;
    }

    @Override
    protected void scheduled() {
        if (taskListen != null) {
            taskListen.scheduled();
        }
        super.scheduled();
    }

    @Override
    protected void running() {
        if (taskListen != null) {
            taskListen.running();
        }
        super.running();
    }

    @Override
    protected void succeeded() {
        if (taskListen != null) {
            taskListen.onPostExecute();
            taskListen.succeeded();
        }
        super.succeeded();
    }

    @Override
    protected void cancelled() {
        if (taskListen != null) {
            taskListen.onPostExecute();
            taskListen.cancelled();
        }
        super.cancelled();
    }

    @Override
    protected void failed() {
        if (taskListen != null) {
            taskListen.onPostExecute();
            taskListen.failed();
        }
        super.failed();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (taskListen != null) {
            taskListen.cancel(mayInterruptIfRunning);
        }
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    protected void updateProgress(long workDone, long max) {
        if (taskListen != null) {
            taskListen.updateProgress(workDone, max);
        }
        super.updateProgress(workDone, max);
    }

    @Override
    protected void updateProgress(double workDone, double max) {
        if (taskListen != null) {
            taskListen.updateProgress(workDone, max);
        }
        super.updateProgress(workDone, max);
    }

    @Override
    protected void updateMessage(String message) {
        if (taskListen != null) {
            taskListen.updateMessage(message);
        }
        super.updateMessage(message);
    }

    @Override
    protected void updateTitle(String title) {
        if (taskListen != null) {
            taskListen.updateTitle(title);
        }
        super.updateTitle(title);
    }

    @Override
    protected void updateValue(Integer value) {
        if (taskListen != null) {
            taskListen.onResult(value);
        }
        super.updateValue(value);
    }

    public interface TaskListen {

        void onPreExecute();

        void onPostExecute();

        Integer call();

        void onResult(Integer result);

        default void scheduled() {
        }

        default void running() {
        }

        default void succeeded() {
        }

        default void cancelled() {
        }

        default void failed() {
        }

        default void cancel(boolean mayInterruptIfRunning) {
        }

        default void updateProgress(long workDone, long max) {
        }

        default void updateProgress(double workDone, double max) {
        }

        default void updateMessage(String message) {
        }

        default void updateTitle(String title) {
        }
    }
}
