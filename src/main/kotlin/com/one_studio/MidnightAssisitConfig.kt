package com.one_studio

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.io.File

object MidnightAssisitConfig {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile: File = FabricLoader.getInstance().configDir.resolve("midnight-assisit.json").toFile()

    data class ConfigData(
        var globalEnabled: Boolean = true,
        val enabledEntities: MutableMap<String, Boolean> = mutableMapOf()
    )

    var data = ConfigData()

    fun load() {
        if (configFile.exists()) {
            try {
                data = gson.fromJson(configFile.readText(), ConfigData::class.java)
            } catch (e: Exception) {
                save()
            }
        }
        
        // Populate missing entities from registry with smart AI detection
        var changed = false
        Registries.ENTITY_TYPE.forEach { type ->
            val id = Registries.ENTITY_TYPE.getId(type).toString()
            
            if (id != "minecraft:player" && !data.enabledEntities.containsKey(id)) {
                data.enabledEntities[id] = isSmartDefault(type, id)
                changed = true
            }
        }
        
        if (changed || !configFile.exists()) {
            save()
        }
    }

    fun isSmartDefault(type: EntityType<*>, id: String): Boolean {
        if (id == "minecraft:player") return false
        
        // الكيانات التي لها AI تنتمي عادة لمجموعات غير MISC
        // مجموعات الـ AI تشمل: MONSTER, CREATURE, AMBIENT, AXOLOTLS, etc.
        val group = type.spawnGroup
        val hasAI = group != SpawnGroup.MISC
        
        // استثناءات لبعض الكيانات التي قد تكون MISC ولكن نريد استهدافها (مثل الجولم والقرويين)
        val isSpecialMob = id.contains("golem") || id.contains("villager")
        
        // إذا كان الكيان ليس له AI وليس من الاستثناءات، يكون OFF افتراضياً
        return hasAI || isSpecialMob
    }

    fun save() {
        try {
            configFile.writeText(gson.toJson(data))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getEntityType(id: String): EntityType<*>? {
        for (type in Registries.ENTITY_TYPE) {
            if (Registries.ENTITY_TYPE.getId(type).toString() == id) {
                return type
            }
        }
        return null
    }

    fun isEntityEnabled(entity: Entity): Boolean {
        val id = Registries.ENTITY_TYPE.getId(entity.type).toString()
        return data.enabledEntities[id] ?: isSmartDefault(entity.type, id)
    }
}
