package stylus.fx;

import javafx.scene.control.ToolBar;

@SuppressWarnings("unused")
public record ToolBarMolder(ToolBar object)
    implements ToolBarMolderBase<ToolBar, ToolBarMolder>
{
}
