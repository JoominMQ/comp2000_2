import java.awt.Graphics;
import java.util.ArrayList;

/* hi alex the left light is 0, 
the right light is 1, 
the top light is 2, 
and the bottom light is 3. 
*/

public class LightManager {

    // w generics implementation
    private ArrayList<Lights> lights = new ArrayList<>();

    private int state;
    private int elapsedTime;

    public LightManager() {

        // creating lights
        lights.add(new Lights(290, 300, 10, 200)); // 0 = left
        lights.add(new Lights(500, 300, 10, 200)); // 1 = right
        lights.add(new Lights(300, 290, 200, 10)); // 2 = top
        lights.add(new Lights(300, 500, 200, 10)); // 3 = bottom

        state = 1;
        elapsedTime = 0;

        setLights();
    }

    public void update() {

        elapsedTime += 16;

        // change state based on time
        if (state == 1 && elapsedTime >= 8000) { // 8 seconds for green
            state = 2;
            elapsedTime = 0;
        }

        else if (state == 2 && elapsedTime >= 3000) { // 3 seconds for yellow
            state = 3;
            elapsedTime = 0;
        }

        else if (state == 3 && elapsedTime >= 3000) { // 3 seconds for all red
            state = 4;
            elapsedTime = 0;
        }

        else if (state == 4 && elapsedTime >= 8000) { // 8 seconds for green
            state = 5;
            elapsedTime = 0;
        }

        else if (state == 5 && elapsedTime >= 3000) { // 3 seconds for yellow
            state = 6;
            elapsedTime = 0;
        }
 
        else if (state == 6 && elapsedTime >= 3000) { // 3 seconds for all red
            state = 1;
            elapsedTime = 0;
        }

        setLights();
    }

    private void setLights() {

        // Get lights from the generic ArrayList
        Lights lightLeft = lights.get(0);
        Lights lightRight = lights.get(1);
        Lights lightTop = lights.get(2);
        Lights lightBottom = lights.get(3);

        try {

            switch (state) {

                // Left and right green
                case 1:
                    lightLeft.changeLight(1);
                    lightRight.changeLight(1);
                    lightTop.changeLight(3);
                    lightBottom.changeLight(3);
                    break;

                // Left and right yellow
                case 2:
                    lightLeft.changeLight(2);
                    lightRight.changeLight(2);
                    lightTop.changeLight(3);
                    lightBottom.changeLight(3);
                    break;

                // All red
                case 3:
                    lightLeft.changeLight(3);
                    lightRight.changeLight(3);
                    lightTop.changeLight(3);
                    lightBottom.changeLight(3);
                    break;

                // Top and bottom green
                case 4:
                    lightLeft.changeLight(3);
                    lightRight.changeLight(3);
                    lightTop.changeLight(1);
                    lightBottom.changeLight(1);
                    break;

                // Top and bottom yellow
                case 5:
                    lightLeft.changeLight(3);
                    lightRight.changeLight(3);
                    lightTop.changeLight(2);
                    lightBottom.changeLight(2);
                    break;

                // All red
                case 6:
                    lightLeft.changeLight(3);
                    lightRight.changeLight(3);
                    lightTop.changeLight(3);
                    lightBottom.changeLight(3);
                    break;
            }

        } catch (IllegalArgumentException e) {
            System.out.println("traffic light error" + e.getMessage());
        }
    }

    public void draw(Graphics g) {
        // loop to draw all lights
        for (Lights light : lights) {
            light.spawnLight(g);
        }
    }

    public int getState() {
        return state;
    }

    public Lights getLightLeft() {
        return lights.get(0);
    }
    public Lights getLightRight() {
        return lights.get(1);
    }
    public Lights getLightTop() {
        return lights.get(2);
    }
    public Lights getLightBottom() {
        return lights.get(3);
    }
}