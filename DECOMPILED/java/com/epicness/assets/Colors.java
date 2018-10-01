package com.epicness.assets;

import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;
import java.util.Random;

public class Colors {
    private static ArrayList<Color> colors = new ArrayList();
    public static Color darkGreen = new Color(0.0f, 0.7f, 0.0f, 1.0f);
    private static Colors instance = new Colors();
    public static Color limeGreen = new Color(0.6f, 1.0f, 0.1f, 1.0f);
    public static Color purple = new Color(0.7f, 0.0f, 1.0f, 1.0f);

    private Colors() {
    }

    public static Colors getInstance() {
        return instance;
    }

    public static void create() {
        colors.add(Color.RED);
        colors.add(Color.ORANGE);
        colors.add(Color.YELLOW);
        colors.add(limeGreen);
        colors.add(darkGreen);
        colors.add(Color.CYAN);
        colors.add(Color.BLUE);
        colors.add(purple);
        colors.add(Color.PINK);
    }

    public Color nextColor(Color color) {
        int i = 0;
        while (i < colors.size()) {
            if (color != colors.get(i)) {
                i++;
            } else if (i == colors.size() - 1) {
                return (Color) colors.get(0);
            } else {
                return (Color) colors.get(i + 1);
            }
        }
        return color;
    }

    public Color nextUnusedColor(ArrayList<Color> usedColors) {
        return nextUnusedColor((Color) colors.get(0), usedColors);
    }

    public Color nextUnusedColor(Color current, ArrayList<Color> usedColors) {
        do {
            current = nextColor(current);
        } while (usedColors.contains(current));
        return current;
    }

    public Color randomColor() {
        return (Color) colors.get(new Random().nextInt(9));
    }
}
