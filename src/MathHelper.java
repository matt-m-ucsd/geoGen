import javafx.scene.shape.Line;
import javafx.util.Pair;

public class MathHelper {

    /**
     * Calculates the distance between two given points
     * @param startX starting x coordinate
     * @param startY starting y coordinate
     * @param endX ending x coordinate
     * @param endY ending y coordinate
     * @return the distance between the two points
     */
    public static double pointDist(double startX, double startY, double endX, double endY) {
        return Math.sqrt(((endY - startY)*(endY - startY)) + ((endX - startX) * (endX - startX)));
    }

    /**
     * finds the intersection point if it exists in quarter of canvas
     * @param line1 input line 1
     * @param line2 input line 2
     * @return true if point found in quarter of canvas, false if not
     */
    public static Pair<Boolean, Pair<Double, Double>> foundIntersection(Line line1, Line line2) {
        double slope1, slope2, b1, b2, interX, interY, yDiff, xDiff;

        yDiff = line1.getEndY() - line1.getStartY();
        xDiff = line1.getEndX() - line1.getStartX();
        slope1 = yDiff/xDiff;
        yDiff = line2.getEndY() - line2.getStartY();
        xDiff = line2.getEndX() - line2.getStartX();
        slope2 = yDiff/xDiff;

        if (slope1 == slope2) {
            return new Pair<>(false, new Pair<>( null, null));
        }

        b1 = line1.getStartY() - (slope1 * line1.getStartX());
        b2 = line2.getStartY() - (slope2 * line2.getStartX());

        interX = (b2 - b1)/(slope1 - slope2);
        interY = (slope1 * interX) + b1;

        if ((interX <= line2.getEndX()) && (interX > 0) && (interY > 0)) {
            return new Pair<>(true, new Pair<>( interX, interY));
        }

        return new Pair<>(false, new Pair<>( null, null));
    }
}
