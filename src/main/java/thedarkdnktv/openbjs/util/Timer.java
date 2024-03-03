package thedarkdnktv.openbjs.util;

import java.time.Duration;
import java.util.function.Supplier;

public final class Timer {

    private long setting;
    private long lastActuation;

    private Supplier<Long> timeSupplier = System::currentTimeMillis;

    public boolean tick() {
        var now = this.timeSupplier.get();
        if (now - this.lastActuation >= this.setting) {
            this.lastActuation = now;
            return true;
        }

        return false;
    }

    public void setTime(long ms) {
        this.setting = ms;
        this.lastActuation = this.timeSupplier.get();
    }

    public void setTime(Duration duration) {
        this.setTime(duration.toMillis());
    }

    public void setTimeSupplier(Supplier<Long> supplier) {
        this.timeSupplier = supplier;
    }
}
