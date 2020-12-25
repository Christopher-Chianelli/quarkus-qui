package io.quarkus.qui.ui;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import io.quarkus.qui.Renderable;
import io.quarkus.qui.View;

public class Text implements View<Text.Props> {
    public interface Props extends io.quarkus.qui.Props<Props> {
        Props text(String text);
    }

    @Override
    public Renderable render(Props props) {
        var text = get(props::text);
        return (surface) -> {
            surface.drawInsideBoundary((bounds, canvas) -> {
                Rectangle2D textBounds = canvas.getFontMetrics().getStringBounds(text, canvas);
                double textWidth = textBounds.getWidth();
                double textHeight = textBounds.getHeight();
                float canvasWidth = bounds.getBounds().getWidth();
                float canvasHeight = bounds.getBounds().getHeight();


                canvas.setColor(Color.BLACK);
                canvas.drawString(text, (int)((canvasWidth / 2) - (textWidth / 2)),
                                        (int)((canvasHeight / 2) + (textHeight / 2)));
            });
        };
    }
}
