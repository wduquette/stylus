package stylus.fx;

import javafx.scene.control.ScrollPane;

@SuppressWarnings("unused")
public record ScrollPaneMolder(ScrollPane object)
    implements ScrollPaneMolderBase<ScrollPane, ScrollPaneMolder>
{
}
