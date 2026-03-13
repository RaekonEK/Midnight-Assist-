package com.one_studio

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.vehicle.BoatEntity
import net.minecraft.entity.vehicle.AbstractMinecartEntity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

import net.minecraft.util.Hand
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult

object MidnightAssisitClient : ClientModInitializer {
    private var attackDelay = 0
    private var isWaitingForAttack = false
    private var lastTargetId: Int = -1
    
    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient ->
            val player = client.player ?: return@EndTick
            
            if (!MidnightAssisitConfig.data.globalEnabled) return@EndTick

            val world = client.world ?: return@EndTick
            val interactionManager = client.interactionManager ?: return@EndTick

            // التحقق مما إذا كان اللاعب يقوم بالتعدين (تكسير بلوك)
            // إذا كان يكسر بلوك، نتوقف عن المساعدة لتجنب الضرب العشوائي بعد التكسير
            val isMining = interactionManager.isBreakingBlock

            // التحقق من ضغط زر الهجوم
            if (client.options.attackKey.isPressed && !isWaitingForAttack && !isMining) {
                // نتحقق مما إذا كان هناك هدف (Entity) قريب قبل البدء
                val target = findTarget(client, 5.0)
                if (target != null) {
                    isWaitingForAttack = true
                    attackDelay = 1 // تقليل التأخير لسرعة الاستجابة (50ms)
                    lastTargetId = target.id
                }
            }

            if (isWaitingForAttack) {
                if (attackDelay > 0) {
                    attackDelay--
                    // الاستمرار في تتبع الهدف أثناء التأخير
                    findTarget(client, 5.0)?.let { snapToEntity(player, it) }
                } else {
                    // تنفيذ الهجوم
                    val target = findTarget(client, 5.0)
                    if (target != null) {
                        interactionManager.attackEntity(player, target)
                        player.swingHand(Hand.MAIN_HAND)
                    }
                    isWaitingForAttack = false
                }
            }
        })
    }

    private fun findTarget(client: MinecraftClient, range: Double): Entity? {
        val player = client.player ?: return null
        val world = client.world ?: return null
        
        val rotation = player.getRotationVec(1.0f)
        
        // البحث عن الكائنات في نطاق أوسع قليلاً لتحسين الـ Range
        val entities = world.getOtherEntities(player, 
            player.boundingBox.stretch(rotation.multiply(range)).expand(2.0))

        var bestTarget: Entity? = null
        var closestDist = Double.MAX_VALUE

        for (entity in entities) {
            // حظر استهداف اللاعبين نهائياً لأسباب أخلاقية (Anti-Cheat)
            if (entity is PlayerEntity) continue

            // استهداف الوحوش والحيوانات، وكذلك القوارب والعربات والـ Armor Stands إذا تم تفعيلها
            if (entity is MobEntity || entity is ArmorStandEntity || entity is BoatEntity || entity is AbstractMinecartEntity) {
                // التحقق من الإعدادات لكل كائن
                if (!MidnightAssisitConfig.isEntityEnabled(entity)) continue

                val dist = entity.distanceTo(player).toDouble()
                if (dist <= range) {
                    // التحقق من الرؤية (عدم وجود جدران)
                    if (!player.canSee(entity)) continue

                    // دقة أقل قليلاً (0.85) للسماح بمدى رؤية أوسع للاستهداف
                    if (isLookingAt(player, entity, 0.85)) {
                        if (dist < closestDist) {
                            closestDist = dist
                            bestTarget = entity
                        }
                    }
                }
            }
        }
        return bestTarget
    }

    private fun isLookingAt(player: PlayerEntity, entity: Entity, accuracy: Double): Boolean {
        val playerPos = player.getCameraPosVec(1.0f)
        val targetPos = entity.boundingBox.center
        val dirToTarget = targetPos.subtract(playerPos).normalize()
        val playerRotation = player.getRotationVec(1.0f)
        return playerRotation.dotProduct(dirToTarget) > accuracy
    }

    private fun snapToEntity(player: PlayerEntity, entity: Entity) {
        val targetPos = entity.boundingBox.center
        val dx = targetPos.x - player.getX()
        val dy = targetPos.y - player.getEyeY()
        val dz = targetPos.z - player.getZ()
        val dh = MathHelper.sqrt((dx * dx + dz * dz).toFloat()).toDouble()

        val yaw = (MathHelper.atan2(dz, dx) * MathHelper.DEGREES_PER_RADIAN).toFloat() - 90.0f
        val pitch = (-(MathHelper.atan2(dy, dh) * MathHelper.DEGREES_PER_RADIAN)).toFloat()

        player.setYaw(yaw)
        player.setPitch(pitch)
    }
}