package hk.math;

public class Vector {

    private double x;
    private double y;

    public Vector() {
        this.x = Math.random() - 0.5;
        this.y = Math.random() - 0.5;
    }

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void limit(double maximumForce) {
        var magnitude = getMagnitude();
        var multiplier = 0.0;
        if (magnitude > maximumForce)
            multiplier = maximumForce / magnitude;
        else
            multiplier = 1.0;

        x *= multiplier;
        y *= multiplier;

    }

    public void add(Vector parent) {
        this.x += parent.getX();
        this.y += parent.getY();
    }

    public void subtract(Vector parent) {
        this.x -= parent.getX();
        this.y -= parent.getY();
    }

    public void multiply(double multiplier) {
        this.x *= multiplier;
        this.y *= multiplier;
    }

    public void divide(double divider) {
        this.x /= divider;
        this.y /= divider;
    }

    public double dir() {
        return Math.atan2(this.y, this.x);
    }

    public double movement() {
        return this.x + this.y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Vector setMagnitude(double newMagnitude) {
        double currentMagnitude = getMagnitude();
        this.x *= newMagnitude / currentMagnitude;
        this.y *= newMagnitude / currentMagnitude;
        return this;
    }

    public double getMagnitude() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

}
