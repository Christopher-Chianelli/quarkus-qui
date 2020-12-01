package io.quarkus.qui.ui;

import io.quarkus.qui.Renderable;
import io.quarkus.qui.View;
import org.jetbrains.skija.Color;
import org.jetbrains.skija.Font;
import org.jetbrains.skija.Paint;
import org.jetbrains.skija.Rect;
import org.jetbrains.skija.TextBlob;
import org.jetbrains.skija.TextBlobBuilder;
import org.jetbrains.skija.Typeface;

public class Text implements View<Text.Props> {
    public interface Props extends io.quarkus.qui.Props<Props> {
        Props text(String text);
    }

    @Override
    public Renderable render(Props props) {
        var text = get(props::text);
        Font font = new Font(Typeface.makeDefault(), 40);
        return (surface) -> {
            surface.drawInsideBoundary((bounds, canvas) -> {
                TextBlobBuilder textBuilder = new TextBlobBuilder();
                textBuilder.appendRun(font, text, 0, 0, bounds.getBounds());
                TextBlob blob = textBuilder.build();
                Rect textBounds = font.measureText(text);
                float textWidth = textBounds.getWidth();
                float textHeight = textBounds.getHeight();
                float canvasWidth = bounds.getBounds().getWidth();
                float canvasHeight = bounds.getBounds().getHeight();

                Paint paint = new Paint();
                paint.setColor(Color.withAlpha(0, 255));
                canvas.drawTextBlob(blob, (canvasWidth / 2) - (textWidth / 2), (canvasHeight / 2) + (textHeight / 2), font, paint);
            });
        };
    }
}
