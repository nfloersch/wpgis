/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import java.awt.Point;
import java.awt.Rectangle;
import org.pepsoft.minecraft.Direction;

/**
 *
 * @author pepijn
 */
public abstract class CoordinateTransform {
    public abstract Point transform(int x, int y);

    public abstract Point transform(Point coords);
    
    public abstract void transformInPlace(Point coords);
    
    public Rectangle transform(Rectangle rectangle) {
        Point corner1 = rectangle.getLocation();
        Point corner2 = new Point(rectangle.x + rectangle.width - 1, rectangle.y + rectangle.height - 1);
        transformInPlace(corner1);
        transformInPlace(corner2);
        return new Rectangle(Math.min(corner1.x, corner2.x), Math.min(corner1.y, corner2.y), Math.abs(corner2.x - corner1.x) + 1, Math.abs(corner2.y - corner1.y) + 1);
    }
    
    public abstract Direction transform(Direction direction);
    
    public abstract Direction inverseTransform(Direction direction);
    
    public abstract float transform(float angle);
    
    public static final CoordinateTransform ROTATE_CLOCKWISE_90_DEGREES = new CoordinateTransform() {
        @Override
        public Point transform(int x, int y) {
            return new Point(-y - 1, x);
        }

        @Override
        public Point transform(Point coords) {
            return new Point(-coords.y - 1, coords.x);
        }
        
        @Override
        public void transformInPlace(Point coords) {
            int tmp = coords.x;
            coords.x = -coords.y - 1;
            coords.y = tmp;
        }

        @Override
        public Direction transform(Direction direction) {
            return direction.right();
        }

        @Override
        public Direction inverseTransform(Direction direction) {
            return direction.left();
        }
        
        @Override
        public float transform(float angle) {
            angle = angle - HALF_PI;
            while (angle < 0) {
                angle += TWO_PI;
            }
            return angle;
        }
        
        private static final float HALF_PI = (float) (Math.PI / 2);
        private static final float TWO_PI  = (float) (Math.PI * 2);
    };

    public static final CoordinateTransform ROTATE_180_DEGREES = new CoordinateTransform() {
        @Override
        public Point transform(int x, int y) {
            return new Point(-x - 1, -y - 1);
        }

        @Override
        public Point transform(Point coords) {
            return new Point(-coords.x - 1, -coords.y - 1);
        }
        
        @Override
        public void transformInPlace(Point coords) {
            coords.x = -coords.x - 1;
            coords.y = -coords.y - 1;
        }

        @Override
        public Direction transform(Direction direction) {
            return direction.opposite();
        }

        @Override
        public Direction inverseTransform(Direction direction) {
            return direction.opposite();
        }

        @Override
        public float transform(float angle) {
            angle = angle + PI;
            while (angle >= TWO_PI) {
                angle -= TWO_PI;
            }
            return angle;
        }
        
        private static final float PI      = (float) Math.PI;
        private static final float TWO_PI  = (float) (Math.PI * 2);
    };

    public static final CoordinateTransform ROTATE_CLOCKWISE_270_DEGREES = new CoordinateTransform() {
        @Override
        public Point transform(int x, int y) {
            return new Point(y, -x - 1);
        }

        @Override
        public Point transform(Point coords) {
            return new Point(coords.y, -coords.x - 1);
        }
        
        @Override
        public void transformInPlace(Point coords) {
            int tmp = -coords.x - 1;
            coords.x = coords.y;
            coords.y = tmp;
        }

        @Override
        public Direction transform(Direction direction) {
            return direction.left();
        }

        @Override
        public Direction inverseTransform(Direction direction) {
            return direction.right();
        }

        @Override
        public float transform(float angle) {
            angle = angle + HALF_PI;
            while (angle >= TWO_PI) {
                angle -= TWO_PI;
            }
            return angle;
        }
        
        private static final float HALF_PI = (float) (Math.PI / 2);
        private static final float TWO_PI  = (float) (Math.PI * 2);
    };
}