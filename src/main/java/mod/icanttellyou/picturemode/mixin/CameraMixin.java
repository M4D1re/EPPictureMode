package mod.icanttellyou.picturemode.mixin;

import mod.icanttellyou.picturemode.client.PictureModeClient;
import mod.icanttellyou.picturemode.client.PictureModeState;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private boolean detached;

    @Shadow protected abstract void setRotation(float yRot, float xRot);

    @Shadow protected abstract void move(float distanceOffset, float verticalOffset, float horizontalOffset);
    //@Shadow protected abstract void move(double distanceOffset, double verticalOffset, double horizontalOffset);
    @Shadow
    private float getMaxZoom(float distance) {
        throw new AssertionError();
    }


    @Inject(
        //? if >=26.1 {
        /*method = "alignWithEntity",
        *///? } else {
        method = "setup",
        //? }
        at = @At("TAIL")
    )
    private void setupPMRotation(
        //? if <26.1 {
        @org.spongepowered.asm.mixin.injection.Coerce net.minecraft.world.level.BlockGetter level,
        net.minecraft.world.entity.Entity entity,
        boolean detached,
        boolean mirror,
        //? }
        float delta,
        CallbackInfo ci
    ) {
        PictureModeState state = PictureModeClient.getState();

        if (state == null || !state.isEnabled())
            return;


        state.setupCameraAngles(delta, this::setRotation);

        this.detached = !state.isPlayerShown();

        float zoom = (float) state.cameraZoom.getValue(delta);

        float orbitRadius = 12.0F + zoom * 2.0F;
        orbitRadius = Math.max(3.0F, Math.min(32.0F, orbitRadius));

        float safeOrbitRadius = this.getMaxZoom(orbitRadius);

        this.move(-safeOrbitRadius, 0.0F, 0.0F);


    }

}
