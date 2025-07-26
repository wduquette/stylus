package stylus.fx;

import javafx.scene.control.Label;

@SuppressWarnings("unused")
public record LabelMolder(Label object)
    implements LabeledMolderBase<Label, LabelMolder>
{
    // See LabeledMolderBase for setters
}
