package stylus.fx;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonBase;

@SuppressWarnings({"unchecked", "unused"})
public interface ButtonBaseMolderBase<C extends ButtonBase, Self>
    extends LabeledMolderBase<C, Self>
{
    default Self onAction(EventHandler<ActionEvent> value) {
        object().setOnAction(value);
        return (Self)this;
    }

    default Self action(Runnable value) {
        object().setOnAction(evt -> value.run());
        return (Self)this;
    }
}
