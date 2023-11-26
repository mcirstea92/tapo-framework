package ro.builditsmart.models.tapo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class TapoColor {

    @JsonProperty("hue")
    private Integer hue;
    @JsonProperty("saturation")
    private Integer saturation;
    @JsonProperty("brightness")
    private Integer brightness;
    @JsonProperty("color_temp")
    private Integer colorTemp;

    protected TapoColor(Integer hue, Integer saturation, Integer brightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
        this.colorTemp = 0;
    }

    protected TapoColor(Integer colorTemp, Integer brightness) {
        this.hue = null;
        this.saturation = null;
        this.brightness = brightness;
        this.colorTemp = colorTemp;
    }

    /**
     * @param color Supports values like "#42903a", "3600k", "hsl(220, 60, 80)", "rgb(200,100,20)"
     * @return a TapoColor object
     */
    public static TapoColor fromString(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color must not be null");
        }

        color = color.toLowerCase();

        if (color.startsWith("#")) {
            return fromHex(color);
        }
        if (color.endsWith("k")) {
            return fromTemperature(color, null);
        }
        if (color.startsWith("hsl")) {
            return fromHsl(color);
        }
        if (color.startsWith("rgb")) {
            return fromRgb(color);
        }

        throw new IllegalArgumentException("Invalid Color");
    }

    /**
     * @param temp       - 2500K up to 6500K
     * @param brightness - 0 up to 100
     * @return a TapoColor object
     */
    public static TapoColor fromTemperature(int temp, Integer brightness) {
        if (temp < 2500 || temp > 6500) {
            throw new IllegalArgumentException("Value must be between 2500 and 6500. (" + temp + ")");
        }

        if (brightness != null && (brightness < 0 || brightness > 100)) {
            throw new IllegalArgumentException("Value must be between 0 and 100. (" + brightness + ")");
        }

        return new TapoColor(temp, brightness);
    }

    public static TapoColor fromTemperature(String temp, Integer brightness) {
        if (temp == null) {
            throw new IllegalArgumentException("temp must not be null");
        }

        Pattern pattern = Pattern.compile("^(\\d*)\\s*k?$");
        Matcher result = pattern.matcher(temp.toLowerCase());

        if (!result.matches()) {
            throw new IllegalArgumentException("Invalid temperature string: " + temp);
        }

        int k = Integer.parseInt(result.group(1));
        return fromTemperature(k, brightness);
    }

    /**
     * @param hex #000000 Not allowed - values allowed are from #000001 to #ffffff
     * @return a TapoColor object
     */
    public static TapoColor fromHex(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("hex must not be null");
        }

        if (hex.equals("#000000")) {
            throw new IllegalArgumentException("Invalid hex string: " + hex + " (Cannot be black)");
        }

        Pattern pattern = Pattern.compile("^#?([a-f\\d]{2})([a-f\\d]{2})([a-f\\d]{2})$");
        Matcher result = pattern.matcher(hex.toLowerCase());

        if (!result.matches()) {
            throw new IllegalArgumentException("Invalid hex string: " + hex);
        }

        int r = Integer.parseInt(result.group(1), 16);
        int g = Integer.parseInt(result.group(2), 16);
        int b = Integer.parseInt(result.group(3), 16);

        return fromRgb(r, g, b);
    }

    public static TapoColor fromRgb(int r, int g, int b) {
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            throw new IllegalArgumentException("RGB values must be between 0 and 255.");
        }

        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));

        int hue;
        int saturation;
        int brightness = (max + min) / 2;

        if (max == min) {
            hue = saturation = 0; // achromatic
        } else {
            int d = max - min;
            saturation = brightness > 128 ? d / (510 - max - min) : d / (max + min);
            if (max == r) {
                hue = (g - b) / d + (g < b ? 6 : 0);
            } else if (max == g) {
                hue = (b - r) / d + 2;
            } else {
                hue = (r - g) / d + 4;
            }
            hue = hue * 60;
        }

        if (hue < 0) {
            hue += 360;
        }

        return new TapoColor(hue, saturation, brightness);
    }

    public static TapoColor fromRgb(String rgb) {
        if (rgb == null) {
            throw new IllegalArgumentException("rgb must not be null");
        }

        Pattern pattern = Pattern.compile("^(?:rgb\\()?([0-9]+)(?:[\\s,])+([0-9]+)(?:[\\s,])+([0-9]+)\\)?$");
        Matcher result = pattern.matcher(rgb.toLowerCase());

        if (!result.matches()) {
            throw new IllegalArgumentException("Invalid rgb string: " + rgb);
        }

        int r = Integer.parseInt(result.group(1));
        int g = Integer.parseInt(result.group(2));
        int b = Integer.parseInt(result.group(3));

        return fromRgb(r, g, b);
    }

    /**
     * @param hue        0-360
     * @param saturation 0-100
     * @param lightness  0-100
     * @return a TapoColor object
     */
    public static TapoColor fromHsl(int hue, int saturation, Integer lightness) {
        if (hue < 0 || hue > 360) {
            throw new IllegalArgumentException("Value must be between 0 and 360. (" + hue + ")");
        }

        if (saturation < 0 || saturation > 100) {
            throw new IllegalArgumentException("Value must be between 0 and 100. (" + saturation + ")");
        }

        if (lightness != null && (lightness < 0 || lightness > 100)) {
            throw new IllegalArgumentException("Value must be between 0 and 100. (" + lightness + ")");
        }

        return new TapoColor(hue, saturation, lightness);
    }

    public static TapoColor fromHsl(String hsl) {
        if (hsl == null) {
            throw new IllegalArgumentException("hsl must not be null");
        }

        Pattern pattern = Pattern.compile("^(?:hsl\\()?([0-9]+)(?:[\\s,])+([0-9]+)(?:[\\s,])+([0-9]+)\\)?$");
        Matcher result = pattern.matcher(hsl.toLowerCase());

        if (!result.matches()) {
            throw new IllegalArgumentException("Invalid hsl string: " + hsl);
        }

        int hue = Integer.parseInt(result.group(1));
        int saturation = Integer.parseInt(result.group(2));
        int lightness = Integer.parseInt(result.group(3));

        return fromHsl(hue, saturation, lightness);
    }
}
