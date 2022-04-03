package io.rocketbase.commons.vaadin.component;

import com.vaadin.flow.component.UI;
import org.vaadin.firitin.components.dialog.VDialog;

/**
 * util to run a task with a loading-indicator
 */
public class ExecuteLoadingThread extends Thread {

    private final UI ui;
    private final Runnable runnable;

    private VDialog dialog;

    public ExecuteLoadingThread(UI ui, Runnable runnable) {
        this.ui = ui;
        this.runnable = runnable;
        this.dialog = Dialogs.pleaseWait();
    }

    @Override
    public void run() {
        runnable.run();
        ui.access(() -> dialog.close());
    }
}
