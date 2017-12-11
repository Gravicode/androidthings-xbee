package com.google.android.things.contrib.driver.fezhat;

/**
 * Created by mifmasterz on 12/29/16.
 */

/// <summary>
/// Represents a color of the onboard LEDs.
/// </summary>
public class Color {
    /// <summary>
    /// The red channel intensity.
    /// </summary>
    public int R;
    /// <summary>
    /// The green channel intensity.
    /// </summary>
    public int G;
    /// <summary>
    /// The blue channel intensity.
    /// </summary>
    public int B;

    /// <summary>
    /// Constructs a new color.
    /// </summary>
    /// <param name="red">The red channel intensity.</param>
    /// <param name="green">The green channel intensity.</param>
    /// <param name="blue">The blue channel intensity.</param>
    public Color(int red, int green, int blue) {
        this.R = red;
        this.G = green;
        this.B = blue;
    }

    /// <summary>
    /// A predefined red color.
    /// </summary>
    public static Color Red() {
        return new Color( 255,  0,  0);
    }

    /// <summary>
    /// A predefined green color.
    /// </summary>
    public static Color Green() {
        return new Color( 0,  255,  0);
    }

    /// <summary>
    /// A predefined blue color.
    /// </summary>
    public static Color Blue() {
        return new Color( 0,  0, 255);
    }

    /// <summary>
    /// A predefined cyan color.
    /// </summary>
    public static Color Cyan() {
        return new Color( 0, 255,  255);
    }

    /// <summary>
    /// A predefined magneta color.
    /// </summary>
    public static Color Magneta() {
        return new Color( 255, 0,  255);
    }

    /// <summary>
    /// A predefined yellow color.
    /// </summary>
    public static Color Yellow() {
        return new Color( 255, 255, 0);
    }

    /// <summary>
    /// A predefined white color.
    /// </summary>
    public static Color White() {
        return new Color(255,  255,  255);
    }

    /// <summary>
    /// A predefined black color.
    /// </summary>
    public static Color Black() {
        return new Color( 0,  0, 0);
    }
}