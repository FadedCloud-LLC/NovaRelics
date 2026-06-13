package dev.aponder.novarelics.aura;

public class AuraDefinition {

    private boolean enabled;
    private AuraType type;
    private AuraMode mode;
    private String particle;
    private String color;
    private String fromColor;
    private String toColor;
    private float dustSize;
    private double radius;
    private double speed;
    private int amount;
    private int updateTicks;
    private boolean onlyWhenHeld;
    private double offsetX;
    private double offsetY;
    private double offsetZ;

    public AuraDefinition() {
        this.enabled = false;
        this.type = AuraType.SPIRAL_EFFECT;
        this.mode = AuraMode.AROUND_PLAYER;
        this.particle = "ENCHANT";
        this.dustSize = 1.0f;
        this.radius = 1.0;
        this.speed = 0.05;
        this.amount = 10;
        this.updateTicks = 5;
        this.onlyWhenHeld = false;
        this.offsetX = 0.2;
        this.offsetY = 0.2;
        this.offsetZ = 0.2;
    }

    public boolean isEnabled() { return enabled; }
    public AuraType getType() { return type; }
    public AuraMode getMode() { return mode; }
    public String getParticle() { return particle; }
    public String getColor() { return color; }
    public String getFromColor() { return fromColor; }
    public String getToColor() { return toColor; }
    public float getDustSize() { return dustSize; }
    public double getRadius() { return radius; }
    public double getSpeed() { return speed; }
    public int getAmount() { return amount; }
    public int getUpdateTicks() { return updateTicks; }
    public boolean isOnlyWhenHeld() { return onlyWhenHeld; }
    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
    public double getOffsetZ() { return offsetZ; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setType(AuraType type) { this.type = type; }
    public void setMode(AuraMode mode) { this.mode = mode; }
    public void setParticle(String particle) { this.particle = particle; }
    public void setColor(String color) { this.color = color; }
    public void setFromColor(String fromColor) { this.fromColor = fromColor; }
    public void setToColor(String toColor) { this.toColor = toColor; }
    public void setDustSize(float dustSize) { this.dustSize = dustSize; }
    public void setRadius(double radius) { this.radius = radius; }
    public void setSpeed(double speed) { this.speed = speed; }
    public void setAmount(int amount) { this.amount = amount; }
    public void setUpdateTicks(int updateTicks) { this.updateTicks = updateTicks; }
    public void setOnlyWhenHeld(boolean onlyWhenHeld) { this.onlyWhenHeld = onlyWhenHeld; }
    public void setOffsetX(double offsetX) { this.offsetX = offsetX; }
    public void setOffsetY(double offsetY) { this.offsetY = offsetY; }
    public void setOffsetZ(double offsetZ) { this.offsetZ = offsetZ; }
}
