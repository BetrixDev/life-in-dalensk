package dev.betrix.exclusioncraft.client.hud;

public class FloatingCurrencyNumber {

    private final long amount;
    private final boolean isAddition;
    private final float startX;
    private final float startY;
    private int ticksAlive;
    private final int maxTicks;

    public FloatingCurrencyNumber(long amount, boolean isAddition, float startX, float startY) {
        this.amount = amount;
        this.isAddition = isAddition;
        this.startX = startX;
        this.startY = startY;
        this.ticksAlive = 0;
        this.maxTicks = 40; // 2 seconds
    }

    public void tick() {
        ticksAlive++;
    }

    public boolean isExpired() {
        return ticksAlive >= maxTicks;
    }

    public long getAmount() {
        return amount;
    }

    public boolean isAddition() {
        return isAddition;
    }

    public float getProgress() {
        return (float) ticksAlive / maxTicks;
    }

    public float getAlpha() {
        float progress = getProgress();
        // Fade out in the last 40% of the lifetime
        if (progress > 0.6f) {
            return 1.0f - ((progress - 0.6f) / 0.4f);
        }
        return 1.0f;
    }

    public float getYOffset() {
        // Rise up over time with easing
        float progress = getProgress();
        return -30f * easeOutQuad(progress);
    }

    public float getXOffset() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }

    public float getScale() {
        float progress = getProgress();
        // Pop in effect at the start
        if (progress < 0.1f) {
            return 0.8f + 0.4f * easeOutBack(progress / 0.1f);
        }
        return 1.0f;
    }

    private float easeOutQuad(float t) {
        return 1 - (1 - t) * (1 - t);
    }

    private float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }
}
