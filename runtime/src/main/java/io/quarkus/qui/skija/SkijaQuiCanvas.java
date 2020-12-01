package io.quarkus.qui.skija;

import java.util.function.BiConsumer;

import io.quarkus.qui.QuiCanvas;
import org.jetbrains.skija.Canvas;
import org.jetbrains.skija.Matrix33;
import org.jetbrains.skija.Path;
import org.jetbrains.skija.Rect;

public class SkijaQuiCanvas implements QuiCanvas {
    private Canvas canvas;
    private Path bounds;
    private Matrix33 transformMatrix;

    public SkijaQuiCanvas(Canvas canvas, Path bounds) {
        this(canvas, bounds, Matrix33.IDENTITY);
    }

    public SkijaQuiCanvas(Canvas canvas, Path bounds, Matrix33 transformMatrix) {
        this.canvas = canvas;
        this.bounds = bounds;
        this.transformMatrix = transformMatrix;
    }

    @Override
    public Path getBoundary() {
        return bounds;
    }

    @Override
    public QuiCanvas getSubcanvas(Path newBoundary) {
        Rect bounds = newBoundary.getBounds();
        Matrix33 newTransformMatrix = this.transformMatrix
                .makeConcat(Matrix33.makeTranslate(bounds.getLeft(), bounds.getTop()));
        Matrix33 inverse = Matrix33.makeTranslate(-bounds.getLeft(), -bounds.getTop());
        newBoundary.transform(inverse);
        return new SkijaQuiCanvas(canvas, newBoundary, newTransformMatrix);
    }

    @Override
    public void drawInsideBoundary(BiConsumer<Path, Canvas> drawer) {
        if (bounds.isRect() != null) {
            // It is a rectangle, so it inner boundary
            // the same as its outer boundary
            drawBoundary(drawer);
        }
        else {
            Rect innerBounds = computeInnerBounds();
            getSubcanvas(innerBounds).drawBoundary(drawer);
        }
    }

    private Rect computeInnerBounds() {
        final float TOLERANCE = 1.0f;
        Rect outerBounds = bounds.getBounds();
        float width = outerBounds.getWidth();
        float height = outerBounds.getHeight();
        Rect guess = new Rect(outerBounds.getLeft() + (width / 4),
                              outerBounds.getTop() + (height / 4),
                              outerBounds.getRight() - (width / 4),
                              outerBounds.getBottom() - (height / 4));
        while (!bounds.conservativelyContainsRect(guess)) {
            outerBounds = guess;
            width = outerBounds.getWidth();
            height = outerBounds.getHeight();
            guess = new Rect(outerBounds.getLeft() + (width / 4),
                             outerBounds.getTop() + (height / 4),
                             outerBounds.getRight() - (width / 4),
                             outerBounds.getBottom() - (height / 4));
        }
        float changeWidth = width / 4;
        float changeHeight = height / 4;
        while (changeWidth > TOLERANCE || changeHeight > TOLERANCE) {
            Rect newGuess = new Rect(guess.getLeft() - changeWidth,
                                     guess.getTop() - changeHeight,
                                     guess.getRight() + changeWidth,
                                     guess.getBottom() + changeHeight);
            if (bounds.conservativelyContainsRect(newGuess)) {
                guess = newGuess;
            }
            else if (newGuess.getWidth() < newGuess.getHeight()) {
                newGuess = new Rect(guess.getLeft() - changeWidth,
                                    guess.getTop(),
                                    guess.getRight() + changeWidth,
                                    guess.getBottom());
                if (bounds.conservativelyContainsRect(newGuess)) {
                    guess = newGuess;
                }
                else {
                    newGuess = new Rect(guess.getLeft(),
                                        guess.getTop() - changeHeight,
                                        guess.getRight(),
                                        guess.getBottom() + changeHeight);
                    if (bounds.conservativelyContainsRect(newGuess)) {
                        guess = newGuess;
                    }
                }
            }
            else {
                newGuess = new Rect(guess.getLeft(),
                                    guess.getTop() - changeHeight,
                                    guess.getRight(),
                                    guess.getBottom() + changeHeight);
                if (bounds.conservativelyContainsRect(newGuess)) {
                    guess = newGuess;
                }
                else {
                    newGuess = new Rect(guess.getLeft() - changeWidth,
                                        guess.getTop(),
                                        guess.getRight() + changeWidth,
                                        guess.getBottom());
                    if (bounds.conservativelyContainsRect(newGuess)) {
                        guess = newGuess;
                    }
                }
            }
            changeWidth = changeWidth / 2;
            changeHeight = changeHeight / 2;
        }
        return guess;
    }

    @Override
    public void drawBoundary(BiConsumer<Path, Canvas> drawer) {
        int saveCount = canvas.save();
        canvas.setMatrix(transformMatrix);
        drawer.accept(bounds, canvas);
        canvas.restoreToCount(saveCount);
    }
}
