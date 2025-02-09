package me.delta.mc.marker.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class RGBController {

  private final Plugin plugin;
  private final Set<BlockDisplay> blockDisplays = new HashSet<>();
  private int tickInterval;
  private int colorDelta;
  private boolean stopped = false;
  private BukkitTask task;

  public RGBController(Plugin plugin, int tickInterval, int colorDelta) {
    this.plugin = plugin;
    this.tickInterval = tickInterval;

    if (255 % colorDelta == 0) this.colorDelta = colorDelta;
    else this.colorDelta = 15;
  }

  public int getDeltaMultiplier() {
    return this.colorDelta;
  }

  public void setColorDelta(int colorDelta) {
    this.colorDelta = colorDelta;
  }

  public void startRBG() {

    if (this.task != null) return;

    this.stopped = false;

    AtomicInteger r = new AtomicInteger(255);
    AtomicInteger g = new AtomicInteger();
    AtomicInteger b = new AtomicInteger();

    this.task =
        Bukkit.getScheduler()
            .runTaskTimer(
                this.plugin,
                () -> {
                  if (r.get() > 0 && b.get() == 0) {
                    r.getAndAdd(-this.colorDelta);
                    g.getAndAdd(this.colorDelta);
                  }
                  if (g.get() > 0 && r.get() == 0) {
                    g.getAndAdd(-this.colorDelta);
                    b.getAndAdd(this.colorDelta);
                  }
                  if (b.get() > 0 && g.get() == 0) {
                    r.getAndAdd(this.colorDelta);
                    b.getAndAdd(-this.colorDelta);
                  }

                  this.blockDisplays.forEach(
                      blockDisplay -> {
                        blockDisplay.setGlowColorOverride(Color.fromRGB(r.get(), g.get(), b.get()));
                        blockDisplay.setGlowing(true);
                      });
                },
                0,
                this.tickInterval);
  }

  public void stopRGB() {
    if (task == null) return;
    this.task.cancel();
    this.task = null;
    this.stopped = true;
  }

  public int getTickInterval() {
    return tickInterval;
  }

  public void setTickInterval(int tickInterval) {
    this.tickInterval = tickInterval;
  }

  public void addDisplays(Collection<BlockDisplay> blockDisplay) {
    this.blockDisplays.addAll(blockDisplay);
    if (!this.stopped) this.startRBG();
  }

  public void removeDisplays(Collection<BlockDisplay> blockDisplay) {
    this.blockDisplays.removeAll(blockDisplay);
    if (this.blockDisplays.isEmpty()) this.stopRGB();
  }

  public void addDisplay(BlockDisplay blockDisplay) {
    this.blockDisplays.add(blockDisplay);
    if (!this.stopped) this.startRBG();
  }

  public void removeDisplay(BlockDisplay blockDisplay) {
    this.blockDisplays.remove(blockDisplay);
    if (this.blockDisplays.isEmpty()) this.stopRGB();
  }

  public boolean isStopped() {
    return stopped;
  }

  public Set<BlockDisplay> getBlockDisplays() {
    return blockDisplays;
  }
}
