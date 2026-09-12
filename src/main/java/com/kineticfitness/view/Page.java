package com.kineticfitness.view;

import javafx.scene.Node;

/**
 * Convention every screen in the app implements so it can plug into the {@link AppShell}.
 * A page supplies only its own centre content; the shell owns the window and the sidebar.
 * Teammates: implement this on your screen and add it to the shell in Main.
 */
public interface Page {

    /** The label shown in the sidebar and used to navigate to this page. */
    String label();

    /** The content node for this page (title, body, etc.) — no sidebar. */
    Node getContent();
}
