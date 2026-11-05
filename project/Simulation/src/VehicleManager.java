// VehicleManager.java manages a collection of vehicles in the simulation, allowing for adding, removing, and retrieving vehicles.
// It also checks for distances and collisions between vehicles and other objects in the simulation.
// It DOES NOT handle the behavior or movement of vehicles.

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;


public class VehicleManager {
    private List<Car> vehicles;
    private List<Debris> debris;
    static private int lastCarSpawnTime = 0;
    static private final int CAR_SPAWN_INTERVAL_MS = 1500;
    private static final double CRASHED_CAR_LIFETIME_SECONDS = 3.0;
    private static final float RECKLESS_CAR_SPEED = 160f;
    private LightManager trafficLights;

    public VehicleManager(LightManager trafficLights) {
        this.trafficLights = trafficLights;
        vehicles = new ArrayList<>();
        debris = new ArrayList<>();
        // Create cars
        Car new1_car = new Car(0, 340, 30f, 50f, 0f, Color.MAGENTA);
        Car new2_car = new Car(450, 0, 20f, 30f, (float) (Math.PI / 2), Color.MAGENTA);
        Car new3_car = new Car(740, 440, 10f, 40f, (float) Math.PI, Color.MAGENTA);
        vehicles.add(new1_car);
        vehicles.add(new2_car);
        vehicles.add(new3_car);
    }


    // THE MAIN UPDATE METHOD
    // Update the state of all vehicles in the manager, including moving them and spawning new cars at intervals.
    public void update(double deltaTime, float worldTimer) {
        // Move all vehicles
        Iterator<Car> vehicleIterator = vehicles.iterator();
        while (vehicleIterator.hasNext()) {
            Car car = vehicleIterator.next();
            if (car.hasCrashed()) {
                car.updateCrashTime(deltaTime);
                car.slowDown(deltaTime);
                car.move(deltaTime);
                if (car.getCrashTimeSeconds() >= CRASHED_CAR_LIFETIME_SECONDS && car.getSpeed() <= 0f) {
                    vehicleIterator.remove();
                }
                continue;
            } else {
                car.getDrivingStrategy().update(car, this, deltaTime);
            }
            car.move(deltaTime);
        }
        // Spawn new cars at intervals
        if (worldTimer - lastCarSpawnTime >= CAR_SPAWN_INTERVAL_MS) {
            Random rand = new Random();
            int randomNumber = rand.nextInt(4) + 1; // Generates 0-3, then adds 1
            Car newCar = null;
            switch (randomNumber) {
                case 1 -> newCar = new Car(0, 340, 20, 50, 0, Color.CYAN); // West lane
                case 2 -> newCar = new Car(450, 0, 20, 50, (float) (Math.PI / 2), Color.CYAN); // North lane
                case 3 -> newCar = new Car(350, 740, 20, 50, (float) (3 * Math.PI / 2), Color.CYAN); // South lane
                case 4 -> newCar = new Car(740, 440, 20, 50, (float) Math.PI, Color.CYAN); // East lane
            }
            vehicles.add(newCar);
            lastCarSpawnTime = (int) worldTimer;
        }
        // Check for collisions between vehicles
        checkCollisions();
        checkDebrisCollisions();
        
    }


    public void draw(Graphics g) {
        for (Debris item : debris) {
            item.draw(g);
        }
        for (Car car : vehicles) {
            car.draw(g);
        }
    }

    public void addDebris(float x, float y) {
        debris.add(new Debris(x, y));
    }


    public void addVehicle(Car vehicle) {
        vehicles.add(vehicle);
    }

    public void spawnRecklessCar() {
        Random random = new Random();
        Point spawnPoint = switch (random.nextInt(4)) {
            case 0 -> new Point(20, random.nextInt(760) + 20);
            case 1 -> new Point(random.nextInt(760) + 20, 20);
            case 2 -> new Point(780, random.nextInt(760) + 20);
            default -> new Point(random.nextInt(760) + 20, 780);
        };
        float centerDirection = (float) Math.atan2(400 - spawnPoint.y, 400 - spawnPoint.x);
        float randomOffset = (random.nextFloat() * 2 - 1) * (float) (Math.PI / 3);
        float direction = centerDirection + randomOffset;
        Car recklessCar = new Car(spawnPoint.x, spawnPoint.y, 20f,
                RECKLESS_CAR_SPEED, direction, Color.ORANGE);
        recklessCar.setDrivingStrategy(new RecklessDrivingStrategy(RECKLESS_CAR_SPEED));
        vehicles.add(recklessCar);
    }

    public boolean shouldSlowDown(Car car) {
        return shouldStopFor2(car) || tooCloseToCarAhead(car);
    }

    public void removeVehicle(Car vehicle) {
        vehicles.remove(vehicle);
    }

    public List<Car> getVehicles() {
        return vehicles;
    }

    public Map<Integer, Long> getVehicleCountsByDirectionDegrees() {
        return vehicles.stream().collect(Collectors.groupingBy(
            car -> Math.floorMod(Math.round(car.getDirectionDegrees()), 360), Collectors.counting()));
    }

    public boolean isCollidingWithCar(Car car1, Car car2) {
        float[] pos1 = car1.getPosition();
        float[] pos2 = car2.getPosition();
        float distance = (float) Math.sqrt(Math.pow(pos1[0] - pos2[0], 2) + Math.pow(pos1[1] - pos2[1], 2));
        return distance < (car1.getRadius() + car2.getRadius()); // Check if distance is less than the sum of the radii
    }
    

    private void checkCollisions() {
        for (Car car1 : vehicles) {
            for (Car car2 : vehicles) {
                if (car1 != car2 && isCollidingWithCar(car1, car2)) {
                    handleCollision(car1, car2);
                    break; // Exit the inner loop if a collision is detected
                }
            }
            //if (!collisionDetected) {
            //    car1.setSpeed(50); // Reset speed to normal if no collision
            //}
        }
    }

    private void handleCollision(Car car1, Car car2) {
        float dx = car2.getX() - car1.getX();
        float dy = car2.getY() - car1.getY();
        float distance = (float) Math.hypot(dx, dy);
        if (distance == 0f) {
            distance = 0.01f;
        }

        float normalX = dx / distance;
        float normalY = dy / distance;
        float relativeVelocityX = car1.getVelocityX() - car2.getVelocityX();
        float relativeVelocityY = car1.getVelocityY() - car2.getVelocityY();
        float velocityAlongNormal = relativeVelocityX * normalX + relativeVelocityY * normalY;
        if (velocityAlongNormal < 0) {
            car1.crash();
            car2.crash();
            return;
        }

        float restitution = 0.4f;
        float impulse = -(1 + restitution) * velocityAlongNormal
                / (1f / car1.getMass() + 1f / car2.getMass());
        float impulseX = impulse * normalX;
        float impulseY = impulse * normalY;
        car1.setVelocity(car1.getVelocityX() + impulseX / car1.getMass(),
                car1.getVelocityY() + impulseY / car1.getMass());
        car2.setVelocity(car2.getVelocityX() - impulseX / car2.getMass(),
                car2.getVelocityY() - impulseY / car2.getMass());

        separateOverlap(car1, car2, normalX, normalY, distance);
        car1.crash();
        car2.crash();
    }

    private void separateOverlap(Car car1, Car car2, float normalX, float normalY, float distance) {
        float overlap = car1.getRadius() + car2.getRadius() - distance;
        if (overlap <= 0) {
            return;
        }
        car1.setPosition(car1.getX() - normalX * overlap / 1000,
                car1.getY() - normalY * overlap / 1000);
        car2.setPosition(car2.getX() + normalX * overlap / 1000,
                car2.getY() + normalY * overlap / 1000);
    }

    private void checkDebrisCollisions() {
        for (Car car : vehicles) {
            for (Debris item : debris) {
                double distance = Math.hypot(car.getX() - item.getX(), car.getY() - item.getY());
                if (distance < car.getRadius() + item.getRadius()) {
                    car.crash();
                    break;
                }
            }
        }
    }

    


    public boolean shouldStopFor2(Car car) {
        float STOP_LINE_BUFFER = 10f; // standoff so the car doesn't hug the light
        Lights light = getGoverningLight(car);
        if (light == null || light.getColour() != 3) return false; // not red

        float distance = distanceToStopLine(car, light);
        if (distance < 0) return false;
        float brakingDistance = getStoppingDistance(car) + STOP_LINE_BUFFER;

        return distance <= brakingDistance;
    }

    private Lights getGoverningLight(Car car) {
        float EPSILON = 0.01f; // small threshold for floating-point comparison
        float dir = normalizeAngle(car.getDirectionRadians());
        if (Math.abs(dir - 0) < EPSILON) return trafficLights.getLightLeft();
        if (Math.abs(dir - (float) (Math.PI / 2)) < EPSILON) return trafficLights.getLightTop();
        if (Math.abs(dir - (float) Math.PI) < EPSILON) return trafficLights.getLightRight();
        if (Math.abs(dir - (float) (3 * Math.PI / 2)) < EPSILON) return trafficLights.getLightBottom();
        return null;
    }

    private float distanceToStopLine(Car car, Lights light) {
        float EPSILON = 0.01f; // small threshold for floating-point comparison
        float[] pos = car.getPosition();
        float dir = normalizeAngle(car.getDirectionRadians());

        if (Math.abs(dir - 0) < EPSILON) {                             // eastbound
            return light.getX() - (pos[0] + car.getRadius());
        } else if (Math.abs(dir - (float) (Math.PI / 2)) < EPSILON) {  // southbound
            return light.getY() - (pos[1] + car.getRadius());
        } else if (Math.abs(dir - (float) Math.PI) < EPSILON) {        // westbound
            return (pos[0] - car.getRadius()) - (light.getX() + light.getWidth());
        } else {                                                        // northbound
            return (pos[1] - car.getRadius()) - (light.getY() + light.getHeight());
        }
    }

    private float normalizeAngle(float angle) {
        float twoPi = (float) (2 * Math.PI);
        return ((angle % twoPi) + twoPi) % twoPi;
    }





    private Car findCarAhead(Car car) {
        float[] pos = car.getPosition();
        float dir = car.getDirectionRadians();
        Car closest = null;
        float closestDistance = Float.MAX_VALUE;

        for (Car other : vehicles) {
            if (other == car || other.getDirectionRadians() != dir) continue;

            float[] otherPos = other.getPosition();
            float dx = otherPos[0] - pos[0];
            float dy = otherPos[1] - pos[1];
            float forwardDistance = dx * (float) Math.cos(dir) + dy * (float) Math.sin(dir);

            if (forwardDistance > 0 && forwardDistance < closestDistance) {
                closestDistance = forwardDistance;
                closest = other;
            }
        }
        return closest;
    }

    private boolean tooCloseToCarAhead(Car car) {
        //float TIME_HEADWAY = 0.8f; // seconds of following distance per unit speed
        float BASE_GAP = 20f;      // minimum gap when both are stopped
        
        Car ahead = findCarAhead(car);
        if (ahead == null) return false;
        double distance = getDistance(car.getPosition()[0], car.getPosition()[1], ahead.getPosition()[0], ahead.getPosition()[1]);
        float gap = (float) distance - car.getRadius() - ahead.getRadius();
        float desiredGap = getStoppingDistance(car) + BASE_GAP;
        return gap < desiredGap;
    }

    public float getStoppingDistance(Car car) {
        float speed = car.getSpeed();
        float acceleration = car.getAcceleration();
        return (speed * speed) / (2 * acceleration);
    }

    public static double getDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.hypot(dx, dy);
    }
    public long getRecklessCarCount() {
        return vehicles.stream()
            .filter(car -> car.getDrivingStrategy() instanceof RecklessDrivingStrategy)
            .count();
    }
}